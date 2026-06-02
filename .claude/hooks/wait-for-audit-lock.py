#!/usr/bin/env python3
"""Block until the named audit/deslop agents can all run concurrently, then
exit 0. Referenced by the audit-reminder Stop hook message: when a conflicting
agent is running (often in another session), launching immediately would be
denied by the audit-gate PreToolUse hook and the agent would die without doing
anything. Running this first lets the model wait in-turn instead of
launch-and-die thrashing.

Usage:
    wait-for-audit-lock.py <agent> [<agent>...]

Each <agent> is one of test-coverage-auditor, security-auditor, deslop-fixer.
Exits 0 once none of the listed agents is blocked (or immediately if they were
never blocked), 1 if the timeout elapses first (a likely-leaked lock the user
should be told about). Unknown agent names are ignored. Fails open (exit 0) on
any internal error so a broken lock never wedges a launch.
"""
import os
import sys
import time

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, _HOOK_DIR)
import audit_lock

_POLL_SECONDS = 3
_TIMEOUT_SECONDS = 15 * 60


def _blockers(agents):
	blocking = set()
	for a in agents:
		blocking.update(audit_lock.blocking_agents(a))
	return blocking


def main():
	agents = [a for a in sys.argv[1:] if a in audit_lock.KNOWN_AGENTS]
	if not agents:
		sys.exit(0)
	try:
		deadline = time.time() + _TIMEOUT_SECONDS
		while True:
			blocking = _blockers(agents)
			if not blocking:
				sys.exit(0)
			if time.time() >= deadline:
				sys.stderr.write(
					"wait-for-audit-lock: timed out after "
					f"{_TIMEOUT_SECONDS}s still blocked by "
					f"{', '.join(sorted(blocking))} — likely a leaked lock\n"
				)
				sys.exit(1)
			time.sleep(_POLL_SECONDS)
	except (OSError, ValueError):
		sys.exit(0)


if __name__ == "__main__":
	main()
