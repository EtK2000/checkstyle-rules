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
   - Consumed at true completion by subagent-stop.py (one grant = one run;
     re-running needs a fresh grant).
   If the permissions file is missing or doesn't include the requested
   agent, the call is blocked with a message explaining how to obtain
   permission.

2. Record, at dispatch, the file paths each invoked agent will operate on
   (audit_stamp.record_inflight). SubagentStop fires at the agent's true
   completion but its stdin lacks the prompt, so subagent-stop.py reads these
   recorded paths to know what to stamp. For deslop-fixer the record also
   captures each file's pre-edit hash so completion can promote matching
   coverage/security stamps to the post-deslop hash. deslop-fixer is ungated
   (manual /deslop and stop-hook-driven invocations both work without prior
   permission); the auditors are gated as in role 1.

State files used:
- .claude/state/audit-permissions.json — gating permissions
- .claude/state/audit-inflight.json — dispatch-time paths, via audit_stamp
"""
import json
import os
import sys

import audit_lock
import audit_stamp

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
# CLAUDE_AUDIT_STATE_DIR lets tests redirect subprocess hooks off the real
# .claude/state; unset in production.
_STATE_DIR = os.environ.get("CLAUDE_AUDIT_STATE_DIR") or os.path.join(_HOOK_DIR, "..", "state")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")

COVERAGE_AGENT = "test-coverage-auditor"
SECURITY_AGENT = "security-auditor"
DESLOP_AGENT = "deslop-fixer"
GATED_AGENTS = {COVERAGE_AGENT, SECURITY_AGENT}


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


def _deny_conflict(subagent, conflicts, tool_input):
	"""Emit a PreToolUse deny when `subagent` can't acquire the mutex. Uses the
	'already in flight' message when an identical run is underway on the same
	files, unchanged since it started (rerunning is pointless); otherwise the
	generic mutex-conflict message."""
	paths = audit_stamp.extract_paths(tool_input.get("prompt", ""))
	if subagent in conflicts and audit_stamp.unchanged_inflight(subagent, paths):
		reason = (
			f"ALREADY IN FLIGHT: an identical `{subagent}` run is already in "
			"progress on the same files, unchanged since it started — it will "
			"cover this. Do NOT rerun; let it finish."
		)
	else:
		running = ", ".join(f"`{c}`" for c in conflicts)
		reason = (
			f"BLOCKED: `{subagent}` conflicts with an audit agent already running: "
			f"{running}.\n"
			"Rules: deslop-fixer runs alone; coverage + security may share, but not "
			"two of the same, and neither with deslop.\n"
			f"Wait for it to finish, then re-invoke `{subagent}` (put conflicting "
			"agents in separate messages). A leaked lock self-heals after its TTL."
		)
	json.dump({
		"hookSpecificOutput": {
			"hookEventName": "PreToolUse",
			"permissionDecision": "deny",
			"permissionDecisionReason": reason,
		}
	}, sys.stdout)


def main():
	try:
		hook_input = json.load(sys.stdin)
	except json.JSONDecodeError:
		sys.exit(0)

	if hook_input.get("tool_name") != "Agent":
		sys.exit(0)

	tool_input = hook_input.get("tool_input") or {}
	subagent = tool_input.get("subagent_type")
	session = hook_input.get("session_id")

	if subagent == DESLOP_AGENT:
		ok, conflicts = audit_lock.acquire(subagent, session)
		if not ok:
			_deny_conflict(subagent, conflicts, tool_input)
			return
		audit_stamp.record_inflight(
			subagent, session, audit_stamp.extract_paths(tool_input.get("prompt", ""))
		)
		sys.exit(0)

	if subagent not in GATED_AGENTS:
		sys.exit(0)

	permissions = _read_permissions()
	if subagent in permissions:
		ok, conflicts = audit_lock.acquire(subagent, session)
		if not ok:
			_deny_conflict(subagent, conflicts, tool_input)
			return
		audit_stamp.record_inflight(
			subagent, session, audit_stamp.extract_paths(tool_input.get("prompt", ""))
		)
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
