#!/usr/bin/env python3
"""Shared audit-stamping + in-flight state for the audit/deslop agent hooks.

Why this module exists: an `Agent` tool call is ASYNC. The tool result
("Async agent launched successfully") comes back at DISPATCH, so the PostToolUse
hook fires while the subagent is still running — minutes before it truly
finishes. Releasing the lock or stamping files from PostToolUse therefore
happens at the wrong time: the mutex is freed early (audits overlap deslop) and
deslop's stamp captures the PRE-edit hash (files re-flag as pending after the
real edit lands).

The fix splits the lifecycle across the two events that fire at the RIGHT times:

    audit-gate.py    PreToolUse(Agent)  — dispatch:   acquire lock + record_inflight
    subagent-stop.py SubagentStop       — completion: release lock + stamp_completion

SubagentStop fires at the subagent's true completion, but its stdin carries only
`agent_type`/`agent_id` — NOT the original prompt. So audit-gate records the
file paths (and, for deslop, their pre-edit hashes) at dispatch into
audit-inflight.json, keyed by agent. The mutex guarantees at most one instance
of each agent runs at once, so `agent` is a safe correlation key. subagent-stop
consumes that record at completion.

All state files live under .claude/state. Paths are module-level so tests can
redirect them to a temp dir.
"""
import hashlib
import json
import os
import re
import sys
import time

import audit_lock

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
# CLAUDE_AUDIT_STATE_DIR lets tests redirect subprocess hooks off the real
# .claude/state; unset in production.
_STATE_DIR = os.environ.get("CLAUDE_AUDIT_STATE_DIR") or os.path.join(_HOOK_DIR, "..", "state")
_STAMPS_FILE = os.path.join(_STATE_DIR, "audit-stamps.json")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")
_INFLIGHT_FILE = os.path.join(_STATE_DIR, "audit-inflight.json")
_STAMPS_VERSION = 1
_INFLIGHT_VERSION = 1

COVERAGE_AGENT = "test-coverage-auditor"
DESLOP_AGENT = "deslop-fixer"
SECURITY_AGENT = "security-auditor"
KNOWN_AGENTS = {COVERAGE_AGENT, DESLOP_AGENT, SECURITY_AGENT}
# Agents whose stamps follow a file across a deslop-fixer edit, IF the file's
# pre-deslop hash matched the existing stamp.
PROMOTABLE_AGENTS = (COVERAGE_AGENT, SECURITY_AGENT)

# Match Java paths in agent prompts. Agents are invoked with explicit absolute
# paths via the slash commands; relative src/... paths are a fallback.
_ABS_PATH_RE = re.compile(r"(/[A-Za-z0-9_\-./]+\.java)\b")
_REL_PATH_RE = re.compile(r"\b(src/[A-Za-z0-9_\-./]+\.java)\b")


def _now():
	return int(time.time())


def hash_file(path):
	try:
		with open(path, "rb") as f:
			return hashlib.sha256(f.read()).hexdigest()
	except (FileNotFoundError, OSError):
		return None


def extract_paths(prompt):
	"""Return canonicalized absolute paths of the Java files named in `prompt`
	that exist on disk."""
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


def _write_json(path, data):
	os.makedirs(_STATE_DIR, exist_ok=True)
	tmp = path + ".tmp"
	with open(tmp, "w", encoding="utf-8") as f:
		json.dump(data, f, indent=2, sort_keys=True)
	os.replace(tmp, path)


# --- in-flight (paths recorded at dispatch, consumed at completion) ---------

def _read_inflight_all():
	try:
		with open(_INFLIGHT_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict) or data.get("version") != _INFLIGHT_VERSION:
			return {}
		inflight = data.get("inflight")
		return inflight if isinstance(inflight, dict) else {}
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def record_inflight(agent, session, paths):
	"""Record, at dispatch, the files `agent` will operate on. For deslop-fixer
	each path is stored with its PRE-edit hash so completion can promote matching
	coverage/security stamps; for the read-only auditors the hash is null.

	Overwrites any prior record for the same agent (a stale dispatch that never
	completed must not pin the wrong paths). Best-effort — a write failure just
	means completion has nothing to stamp, which the Stop hook re-flags."""
	if agent not in KNOWN_AGENTS:
		return
	try:
		path_hashes = {
			p: (hash_file(p) if agent == DESLOP_AGENT else None) for p in paths
		}
		inflight = _read_inflight_all()
		inflight[agent] = {
			"session": session,
			"recorded_at": _now(),
			"paths": path_hashes,
		}
		_write_json(_INFLIGHT_FILE, {"version": _INFLIGHT_VERSION, "inflight": inflight})
	except (OSError, ValueError) as e:
		sys.stderr.write(f"audit_stamp: failed to record in-flight for {agent}: {e}\n")


def read_inflight(agent):
	"""Return (path_hashes, session) recorded at dispatch for `agent`, where
	path_hashes maps canonical path -> pre-edit hash (null for auditors). Empty
	dict / None if nothing was recorded."""
	entry = _read_inflight_all().get(agent)
	if not isinstance(entry, dict):
		return {}, None
	paths = entry.get("paths")
	if not isinstance(paths, dict):
		paths = {}
	return paths, entry.get("session")


def inflight_recorded_at(agent):
	"""The dispatch timestamp recorded for `agent`'s in-flight run, or None if
	there is no record. Lets callers (e.g. unlock-audit.py) report a lock's age
	against audit_lock.TTL_SECONDS rather than duplicating a threshold."""
	entry = _read_inflight_all().get(agent)
	if isinstance(entry, dict) and isinstance(entry.get("recorded_at"), (int, float)):
		return int(entry["recorded_at"])
	return None


def clear_inflight(agent):
	try:
		inflight = _read_inflight_all()
		if agent not in inflight:
			return
		del inflight[agent]
		_write_json(_INFLIGHT_FILE, {"version": _INFLIGHT_VERSION, "inflight": inflight})
	except (OSError, ValueError):
		pass


def unchanged_inflight(agent, paths):
	"""True if `agent` has an in-flight run whose recorded paths cover every path
	in `paths` and none has been modified since that run's dispatch
	(mtime <= recorded_at). Lets a caller tell the model an identical run already
	covers the current file state, so re-running would be redundant.

	False on empty `paths`, an unknown agent, a missing/partial record, a record
	older than audit_lock.TTL_SECONDS (a leaked dispatch from a killed agent, the
	same staleness bound the mutex uses), or any stat error — any doubt errs
	toward 'not covered' (toward re-running)."""
	if agent not in KNOWN_AGENTS or not paths:
		return False
	entry = _read_inflight_all().get(agent)
	if not isinstance(entry, dict):
		return False
	recorded_at = entry.get("recorded_at")
	inflight_paths = entry.get("paths")
	if not isinstance(recorded_at, (int, float)) or not isinstance(inflight_paths, dict):
		return False
	if _now() - recorded_at >= audit_lock.TTL_SECONDS:
		return False
	for p in paths:
		canonical = os.path.realpath(p)
		if canonical not in inflight_paths:
			return False
		try:
			if os.path.getmtime(canonical) > recorded_at:
				return False
		except OSError:
			return False
	return True


# --- stamps ----------------------------------------------------------------

def read_stamps():
	try:
		with open(_STAMPS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict) or data.get("version") != _STAMPS_VERSION:
			return {"version": _STAMPS_VERSION, "files": {}}
		if not isinstance(data.get("files"), dict):
			data["files"] = {}
		return data
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {"version": _STAMPS_VERSION, "files": {}}


def write_stamps(data):
	try:
		_write_json(_STAMPS_FILE, data)
	except OSError as e:
		sys.stderr.write(f"audit_stamp: failed to write stamps: {e}\n")


def consume_permission(agent):
	"""Remove the permission entry for the agent that just finished. One grant =
	one run; re-running needs a fresh grant from audit-reminder or allow-audit."""
	try:
		with open(_PERMISSIONS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return
		perms = data.get("permissions") if isinstance(data.get("permissions"), dict) else {}
		if agent not in perms:
			return
		del perms[agent]
		data["permissions"] = perms
		_write_json(_PERMISSIONS_FILE, data)
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		pass


def stamp_completion(agent):
	"""Stamp every in-flight file for `agent` at its CURRENT (completion-time)
	hash, then consume the agent's permission and clear its in-flight record.

	For deslop-fixer, any coverage/security stamp whose hash matches the file's
	recorded PRE-deslop hash is promoted to the post-deslop hash first, so files
	that were already audited at the pre-edit state stay audited.

	Returns (stamped_count, promoted_count). A no-op returning (0, 0) if nothing
	was in flight (e.g. a leaked/late SubagentStop)."""
	path_hashes, _session = read_inflight(agent)
	if not path_hashes:
		return 0, 0
	now = _now()
	stamps = read_stamps()
	files = stamps.setdefault("files", {})
	stamped = 0
	promoted = 0
	for path, pre_hash in path_hashes.items():
		current = hash_file(path)
		if current is None:
			continue
		entry = files.setdefault(path, {})
		if agent == DESLOP_AGENT and pre_hash:
			for promote_agent in PROMOTABLE_AGENTS:
				existing = entry.get(promote_agent)
				if isinstance(existing, dict) and existing.get("hash") == pre_hash:
					existing["hash"] = current
					existing["stamped_at"] = now
					promoted += 1
		entry[agent] = {"hash": current, "stamped_at": now, "source": "agent"}
		stamped += 1
	write_stamps(stamps)
	consume_permission(agent)
	clear_inflight(agent)
	return stamped, promoted
