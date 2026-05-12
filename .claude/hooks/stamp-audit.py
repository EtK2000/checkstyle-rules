#!/usr/bin/env python3
"""
PostToolUse hook for Agent invocations.

When the test-coverage-auditor or security-auditor subagent finishes, this
hook captures the current state (SHA-256 hash) of the files the agent
inspected and writes them to .claude/state/audit-stamps.json. The Stop hook
(audit-reminder.py) reads these stamps to determine if a file has been
modified since its last audit.

If the file matches its stamp for the required agent, no re-audit is needed.
If it doesn't match (or has no stamp), it is pending.

This handles three cases that the previous transcript-only approach missed
or got wrong:
- Cascade detection: if Claude fixes the file after the audit, the hash
  changes, and the file is correctly flagged as pending again.
- Revert detection: if Claude (or the user) reverts a change so the file
  matches a previously-audited state, the file is automatically cleared
  with no need to re-run the audit.
- Robustness against transcript ambiguity.

If this hook fails to run for any reason, no stamps are written and the
Stop hook will conservatively flag affected files as pending. The user can
ack-skip if needed.
"""
import hashlib
import json
import os
import re
import sys
import time

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
_STATE_DIR = os.path.join(_HOOK_DIR, "..", "state")
_STAMPS_FILE = os.path.join(_STATE_DIR, "audit-stamps.json")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")
_DESLOP_SNAPSHOT_FILE = os.path.join(_STATE_DIR, "deslop-pre-snapshot.json")
_STAMPS_VERSION = 1


def consume_permission(subagent):
	"""Remove the permission entry for the agent that just finished.
	One grant = one run; re-running needs a fresh grant from audit-reminder
	or allow-audit.py."""
	try:
		with open(_PERMISSIONS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return
		perms = data.get("permissions") if isinstance(data.get("permissions"), dict) else {}
		if subagent not in perms:
			return
		del perms[subagent]
		data["permissions"] = perms
		tmp = _PERMISSIONS_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f, indent=2, sort_keys=True)
		os.replace(tmp, _PERMISSIONS_FILE)
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		pass

COVERAGE_AGENT = "test-coverage-auditor"
DESLOP_AGENT = "deslop-fixer"
SECURITY_AGENT = "security-auditor"
KNOWN_AGENTS = {COVERAGE_AGENT, DESLOP_AGENT, SECURITY_AGENT}
# Agents whose stamps follow a file across a deslop-fixer edit, IF the file's
# pre-deslop hash matched the existing stamp. See plan
# ~/.claude/plans/curious-puzzling-fern.md for the rule and rationale.
PROMOTABLE_AGENTS = (COVERAGE_AGENT, SECURITY_AGENT)

# Match absolute Java paths in agent prompts. Agents are invoked with
# explicit absolute paths via the slash commands, so this is the primary
# discovery path. Relative paths (e.g. src/main/...) are also captured
# as a fallback.
_ABS_PATH_RE = re.compile(r"(/[A-Za-z0-9_\-./]+\.java)\b")
_REL_PATH_RE = re.compile(r"\b(src/[A-Za-z0-9_\-./]+\.java)\b")


def read_stamps():
	try:
		with open(_STAMPS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return {"version": _STAMPS_VERSION, "files": {}}
		if data.get("version") != _STAMPS_VERSION:
			return {"version": _STAMPS_VERSION, "files": {}}
		if "files" not in data or not isinstance(data["files"], dict):
			data["files"] = {}
		return data
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {"version": _STAMPS_VERSION, "files": {}}


def write_stamps(data):
	try:
		os.makedirs(_STATE_DIR, exist_ok=True)
		tmp = _STAMPS_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f, indent=2, sort_keys=True)
		os.replace(tmp, _STAMPS_FILE)
	except OSError as e:
		sys.stderr.write(f"stamp-audit: failed to write stamps: {e}\n")


def hash_file(path):
	try:
		with open(path, "rb") as f:
			return hashlib.sha256(f.read()).hexdigest()
	except (FileNotFoundError, OSError):
		return None


def read_pre_deslop_snapshot():
	"""Return the {canonical_path: pre_edit_hash} map written by audit-gate.py
	when the deslop-fixer Agent call started. Missing/corrupt file → {}."""
	try:
		with open(_DESLOP_SNAPSHOT_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return {}
		snapshots = data.get("snapshots")
		if not isinstance(snapshots, dict):
			return {}
		result = {}
		for path, entry in snapshots.items():
			if isinstance(entry, dict) and isinstance(entry.get("hash"), str):
				result[path] = entry["hash"]
		return result
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def clear_pre_deslop_snapshot_entries(paths):
	"""Remove the listed paths from deslop-pre-snapshot.json so a future deslop
	run doesn't pick up stale pre-edit hashes."""
	if not paths:
		return
	try:
		with open(_DESLOP_SNAPSHOT_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return
		snapshots = data.get("snapshots")
		if not isinstance(snapshots, dict):
			return
		changed = False
		for path in paths:
			if path in snapshots:
				del snapshots[path]
				changed = True
		if not changed:
			return
		data["snapshots"] = snapshots
		tmp = _DESLOP_SNAPSHOT_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f, indent=2, sort_keys=True)
		os.replace(tmp, _DESLOP_SNAPSHOT_FILE)
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		pass


def extract_paths(prompt):
	"""Extract Java file paths mentioned in the agent's prompt.

	Returns a list of canonicalized absolute paths that exist on disk.
	"""
	if not isinstance(prompt, str):
		return []
	candidates = set()
	candidates.update(_ABS_PATH_RE.findall(prompt))
	candidates.update(_REL_PATH_RE.findall(prompt))
	results = []
	for p in candidates:
		canonical = os.path.realpath(p)
		if os.path.isfile(canonical):
			results.append(canonical)
	return results


def main():
	try:
		hook_input = json.load(sys.stdin)
	except json.JSONDecodeError:
		sys.exit(0)

	tool_name = hook_input.get("tool_name")
	if tool_name != "Agent":
		sys.exit(0)

	tool_input = hook_input.get("tool_input") or {}
	subagent = tool_input.get("subagent_type")
	if subagent not in KNOWN_AGENTS:
		sys.exit(0)

	prompt = tool_input.get("prompt", "")
	paths = extract_paths(prompt)
	if not paths:
		consume_permission(subagent)
		sys.stderr.write(
			f"stamp-audit: no file paths found in prompt for {subagent}; nothing stamped\n"
		)
		sys.exit(0)

	now = int(time.time())
	stamps = read_stamps()
	files = stamps.setdefault("files", {})
	stamped = []
	promoted = 0
	pre_snapshot = read_pre_deslop_snapshot() if subagent == DESLOP_AGENT else {}

	for path in paths:
		h = hash_file(path)
		if h is None:
			continue
		entry = files.setdefault(path, {})

		# Stamp promotion: when deslop-fixer finishes, any coverage/security
		# stamp whose hash matched the file's PRE-deslop hash gets promoted to
		# the POST-deslop hash. This preserves the invariant that deslop's
		# edits do not introduce new audit requirements on files that were
		# already audited at the pre-edit state.
		if subagent == DESLOP_AGENT:
			pre_hash = pre_snapshot.get(path)
			if pre_hash:
				for promote_agent in PROMOTABLE_AGENTS:
					existing = entry.get(promote_agent)
					if isinstance(existing, dict) and existing.get("hash") == pre_hash:
						existing["hash"] = h
						existing["stamped_at"] = now
						promoted += 1

		entry[subagent] = {"hash": h, "stamped_at": now, "source": "agent"}
		stamped.append(path)

	write_stamps(stamps)
	consume_permission(subagent)
	if subagent == DESLOP_AGENT:
		clear_pre_deslop_snapshot_entries(stamped)
		sys.stderr.write(
			f"stamp-audit: stamped {len(stamped)} file(s) under {subagent}; "
			f"promoted {promoted} pre-existing stamp(s)\n"
		)
	else:
		sys.stderr.write(
			f"stamp-audit: stamped {len(stamped)} file(s) under {subagent}\n"
		)
	sys.exit(0)


if __name__ == "__main__":
	main()