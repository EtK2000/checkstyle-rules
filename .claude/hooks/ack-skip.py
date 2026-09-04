#!/usr/bin/env python3
"""Acknowledge that the user explicitly approved skipping audit(s).

This is a near-no-op script whose only job is to appear as a Bash tool_use
event in the session transcript so the Stop hook can detect that an
explicit skip was approved. The hook parses this script's args to
determine which audits and/or files to clear from pending.

Usage:
  ack-skip.py                          # skip ALL pending audits (default)
  ack-skip.py --all                    # same
  ack-skip.py --coverage               # skip coverage audit across all pending files
  ack-skip.py --security               # skip security audit across all pending files
  ack-skip.py --files path [path...]   # skip all audits for listed files only

Protocol (important):
  Running this does NOT approve anything. The Stop hook honors it only when the
  user picked the option labeled exactly "Skip the audit" in an AskUserQuestion
  since the last audit-relevant edit. So: state the case for the skip, ask with
  AskUserQuestion ("Skip the audit" / "Run the audits"), and run this only if
  they picked the first. All in one turn — the answer arrives as an ordinary
  tool result, so nothing stops.

  If they picked "Run the audits", run them in that same turn. If they typed a
  reply instead of picking — even one that plainly means yes — re-ask ONCE with
  the buttons; only a picked label is recorded, so the text can neither approve
  the skip nor be overridden by running the audits anyway.

  Don't run this hoping it clears: require-skip-approval.py denies the call
  outright when no approval is on record, and the Stop hook ignores any that
  slips past and names it in the next block message.
"""
import sys


def main():
	args = sys.argv[1:]

	if not args or args == ["--all"]:
		sys.stderr.write(
			"ack-skip: acknowledging skip for ALL pending audits. "
			"The Stop hook will clear pending on next stop.\n"
		)
		sys.exit(0)

	if args == ["--coverage"]:
		sys.stderr.write(
			"ack-skip: acknowledging skip for the coverage audit "
			"across all pending files.\n"
		)
		sys.exit(0)

	if args == ["--security"]:
		sys.stderr.write(
			"ack-skip: acknowledging skip for the security audit "
			"across all pending files.\n"
		)
		sys.exit(0)

	if args[0] == "--files" and len(args) > 1:
		paths = args[1:]
		sys.stderr.write(
			"ack-skip: acknowledging skip for files:\n  - "
			+ "\n  - ".join(paths)
			+ "\n"
		)
		sys.exit(0)

	sys.stderr.write(
		f"ack-skip: unrecognized args {args!r}. "
		"Use --all (default) | --coverage | --security | --files <path> [<path>...]\n"
	)
	sys.exit(2)


if __name__ == "__main__":
	main()