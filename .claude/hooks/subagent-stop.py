#!/usr/bin/env python3
"""SubagentStop hook — the TRUE-completion end of the audit/deslop lifecycle.

An `Agent` tool call is async: its tool result returns at dispatch, so the
PostToolUse hook fires while the subagent is still running. Releasing the lock
or stamping files there is too early (see audit_stamp.py). SubagentStop fires
when the subagent actually finishes, so release + stamp belong here.

SubagentStop stdin carries `agent_type` and `session_id` (but not the original
prompt). The file paths were recorded at dispatch by audit-gate.py into
audit-inflight.json, keyed by agent; audit_stamp.stamp_completion reads them.

Fires only for our three audit/deslop agents; anything else exits 0 untouched.
Fails open (exit 0) on any error — a missed stamp just means the Stop hook
conservatively re-flags the file, which is the safe direction.
"""
import json
import os
import sys

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, _HOOK_DIR)
import audit_lock
import audit_stamp


def main():
	try:
		hook_input = json.load(sys.stdin)
	except (json.JSONDecodeError, ValueError):
		sys.exit(0)

	agent = hook_input.get("agent_type")
	session = hook_input.get("session_id")
	if agent not in audit_stamp.KNOWN_AGENTS:
		sys.exit(0)

	# Stamp BEFORE releasing the mutex: anything that sees the lock cleared must
	# be able to trust the stamp is already written (else wait-for-audit-lock
	# exits 0 while the Stop hook still sees the file pending). The finally
	# guarantees the lock frees even if stamping raises.
	try:
		stamped, promoted = audit_stamp.stamp_completion(agent)
		if agent == audit_stamp.DESLOP_AGENT:
			sys.stderr.write(
				f"subagent-stop: stamped {stamped} file(s) under {agent}; "
				f"promoted {promoted} pre-existing stamp(s)\n"
			)
		else:
			sys.stderr.write(f"subagent-stop: stamped {stamped} file(s) under {agent}\n")
	except (OSError, ValueError) as e:
		sys.stderr.write(f"subagent-stop: stamping failed for {agent}: {e}\n")
	finally:
		audit_lock.release(agent, session)


if __name__ == "__main__":
	main()
