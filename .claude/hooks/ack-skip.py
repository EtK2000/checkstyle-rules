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
  Only run this AFTER the user has explicitly approved the skip in the
  conversation. The hook cannot verify approval — that is your
  responsibility. Running this without approval is a protocol violation.
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