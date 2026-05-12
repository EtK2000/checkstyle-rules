#!/usr/bin/env python3
"""
PreToolUse hook on Agent.

Two roles:

1. Gate calls to `security-auditor` and `test-coverage-auditor`. Both are
   read-only audit agents and may only run when explicit permission is on
   file. Permission lifecycle:
   - Granted by audit-reminder.py when its Stop block fires for a missing
     audit (per-agent grant, scoped to that audit cycle).
   - Granted by allow-audit.py when the user explicitly opts in.
   - Consumed by stamp-audit.py after the agent finishes (one grant = one
     run; re-running needs a fresh grant).
   If the permissions file is missing or doesn't include the requested
   agent, the call is blocked with a message explaining how to obtain
   permission.

2. Snapshot pre-deslop file hashes when `deslop-fixer` is invoked. The
   fixer is ungated (manual /deslop and stop-hook-driven invocations must
   both work without prior permission), but stamp-audit.py needs to know
   each file's pre-edit hash so it can decide whether to promote existing
   coverage/security stamps to the post-deslop hash. See the plan at
   ~/.claude/plans/curious-puzzling-fern.md for the promotion rule.

State files used:
- .claude/state/audit-permissions.json — gating permissions
- .claude/state/deslop-pre-snapshot.json — pre-deslop hashes for promotion
"""
import hashlib
import json
import os
import re
import sys
import time

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
_STATE_DIR = os.path.join(_HOOK_DIR, "..", "state")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")
_DESLOP_SNAPSHOT_FILE = os.path.join(_STATE_DIR, "deslop-pre-snapshot.json")
_DESLOP_SNAPSHOT_VERSION = 1

COVERAGE_AGENT = "test-coverage-auditor"
SECURITY_AGENT = "security-auditor"
DESLOP_AGENT = "deslop-fixer"
GATED_AGENTS = {COVERAGE_AGENT, SECURITY_AGENT}

# Same path regexes used by stamp-audit.py — kept in sync deliberately so the
# pre-snapshot and the post-stamp see the same file set.
_ABS_PATH_RE = re.compile(r"(/[A-Za-z0-9_\-./]+\.java)\b")
_REL_PATH_RE = re.compile(r"\b(src/[A-Za-z0-9_\-./]+\.java)\b")


def _read_permissions():
	try:
		with open(_PERMISSIONS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return {}
		perms = data.get("permissions")
		return perms if isinstance(perms, dict) else {}
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def _extract_paths(prompt):
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


def _hash_file(path):
	try:
		with open(path, "rb") as f:
			return hashlib.sha256(f.read()).hexdigest()
	except (FileNotFoundError, OSError):
		return None


def _snapshot_pre_deslop(prompt):
	"""Hash each Java file referenced in the deslop-fixer prompt and write
	the result to deslop-pre-snapshot.json. Idempotent — overwrites any
	prior snapshot keyed by the same canonical path (stale snapshots from
	a previous deslop run that never got consumed would otherwise pin the
	wrong pre-edit hash)."""
	paths = _extract_paths(prompt)
	if not paths:
		return
	try:
		os.makedirs(_STATE_DIR, exist_ok=True)
		try:
			with open(_DESLOP_SNAPSHOT_FILE, encoding="utf-8") as f:
				data = json.load(f)
			if not isinstance(data, dict) or data.get("version") != _DESLOP_SNAPSHOT_VERSION:
				data = {"version": _DESLOP_SNAPSHOT_VERSION, "snapshots": {}}
		except (FileNotFoundError, OSError, json.JSONDecodeError):
			data = {"version": _DESLOP_SNAPSHOT_VERSION, "snapshots": {}}
		snapshots = data.get("snapshots")
		if not isinstance(snapshots, dict):
			snapshots = {}
		now = int(time.time())
		for path in paths:
			h = _hash_file(path)
			if h is None:
				continue
			snapshots[path] = {"hash": h, "snapped_at": now}
		data["version"] = _DESLOP_SNAPSHOT_VERSION
		data["snapshots"] = snapshots
		tmp = _DESLOP_SNAPSHOT_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f, indent=2, sort_keys=True)
		os.replace(tmp, _DESLOP_SNAPSHOT_FILE)
	except OSError as e:
		sys.stderr.write(f"audit-gate: failed to write deslop snapshot: {e}\n")


def main():
	try:
		hook_input = json.load(sys.stdin)
	except json.JSONDecodeError:
		sys.exit(0)

	if hook_input.get("tool_name") != "Agent":
		sys.exit(0)

	tool_input = hook_input.get("tool_input") or {}
	subagent = tool_input.get("subagent_type")

	if subagent == DESLOP_AGENT:
		_snapshot_pre_deslop(tool_input.get("prompt", ""))
		sys.exit(0)

	if subagent not in GATED_AGENTS:
		sys.exit(0)

	permissions = _read_permissions()
	if subagent in permissions:
		sys.exit(0)

	flag = "security" if subagent == SECURITY_AGENT else "coverage"
	reason = (
		f"BLOCKED: cannot invoke `{subagent}` — no permission on file.\n\n"
		"Audits run only when:\n"
		f"  1. The audit-reminder Stop hook fires asking for `{subagent}` "
		"(it writes a permission entry as part of blocking), OR\n"
		f"  2. The user explicitly runs `.claude/hooks/allow-audit.py --{flag}` "
		"(or asks you to do so on their behalf via Bash).\n\n"
		"Why: running audits proactively (before the Stop hook asks) burns "
		"time, skews the user's review cadence, and conflates the audit "
		"cycle with the implementation cycle. Audits are the second step "
		"of audit→fix, not the first.\n\n"
		"If you believe the audit should run NOW, end your turn so the Stop "
		"hook can fire and grant permission. If the user explicitly asked "
		f"for an out-of-cycle audit, run `.claude/hooks/allow-audit.py --{flag}` "
		"via Bash first, then re-invoke the agent."
	)

	json.dump({
		"hookSpecificOutput": {
			"hookEventName": "PreToolUse",
			"permissionDecision": "deny",
			"permissionDecisionReason": reason,
		}
	}, sys.stdout)


if __name__ == "__main__":
	main()
