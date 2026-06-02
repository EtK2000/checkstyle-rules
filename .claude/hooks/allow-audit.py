#!/usr/bin/env python3
"""Grant explicit permission for an audit to run outside the Stop-hook cycle.

Run by the user (or by Claude on the user's direct instruction) before
invoking `security-auditor` or `test-coverage-auditor` when the audit is
not being driven by the audit-reminder Stop hook.

The audit-gate PreToolUse hook reads the permissions file on each Agent
call; once permission is recorded here, the next Agent call to the named
auditor is allowed. The SubagentStop subagent-stop.py removes the permission
after the agent finishes, so one grant = one run.

Usage:
  allow-audit.py                 # grant both auditors
  allow-audit.py --security      # grant security-auditor only
  allow-audit.py --coverage      # grant test-coverage-auditor only
"""
import json
import os
import sys
import time

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
_STATE_DIR = os.path.join(_HOOK_DIR, "..", "state")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")


def main():
	args = sys.argv[1:]
	if not args:
		agents = ["security-auditor", "test-coverage-auditor"]
	elif args == ["--security"]:
		agents = ["security-auditor"]
	elif args == ["--coverage"]:
		agents = ["test-coverage-auditor"]
	else:
		sys.stderr.write(
			f"allow-audit: unrecognized args {args!r}. "
			"Use no args (grant both) | --security | --coverage\n"
		)
		sys.exit(2)

	os.makedirs(_STATE_DIR, exist_ok=True)
	try:
		with open(_PERMISSIONS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			data = {}
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		data = {}

	perms = data.get("permissions") if isinstance(data.get("permissions"), dict) else {}
	now = int(time.time())
	for agent in agents:
		perms[agent] = {"granted_at": now, "source": "user-allow"}

	data["version"] = 1
	data["permissions"] = perms

	tmp = _PERMISSIONS_FILE + ".tmp"
	with open(tmp, "w", encoding="utf-8") as f:
		json.dump(data, f, indent=2, sort_keys=True)
	os.replace(tmp, _PERMISSIONS_FILE)

	sys.stderr.write(
		f"allow-audit: granted permission for: {', '.join(agents)}\n"
	)


if __name__ == "__main__":
	main()
