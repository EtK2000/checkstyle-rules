#!/usr/bin/env python3
"""Mutual-exclusion lock shared by the audit/deslop agent hooks.

Enforces which of the three stop-hook-driven agents may run concurrently:

    deslop-fixer          exclusive with everything (itself, coverage, security)
    test-coverage-auditor singleton; may share with security, not with deslop
    security-auditor      singleton; may share with coverage, not with deslop

This is a readers-writers lock: deslop-fixer is the exclusive writer;
coverage/security are distinct singleton readers that share with each other but
never with themselves or with deslop.

Acquire is called from the PreToolUse hook (audit-gate.py) at dispatch; release
from the SubagentStop hook (subagent-stop.py) at the agent's true completion.
An Agent call is async — its tool result returns at dispatch, so a PostToolUse
release would fire while the agent is still running and free the lock early.
Running agents live in
.claude/state/audit-running.json as { agent: {started_at, session} }. The
read-modify-write is serialized across hook processes with an flock on a sidecar
.lock file so two agents racing the same acquire cannot both win.

Robustness:
- TTL: an entry older than TTL_SECONDS is stale (the agent was interrupted, or
  its SubagentStop release never fired) and is ignored/pruned, so a leaked lock
  self-heals rather than wedging launches forever.
- Fail open: any error (corrupt state, flock unavailable) makes acquire allow.
  A missed exclusion is a far better failure than permanently blocked audits.
"""
import errno
import json
import os
import time

try:
	import fcntl
except ImportError:  # non-POSIX; acquire will fail open
	fcntl = None

_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
# CLAUDE_AUDIT_STATE_DIR lets tests redirect subprocess hooks off the real
# .claude/state; unset in production.
_STATE_DIR = os.environ.get("CLAUDE_AUDIT_STATE_DIR") or os.path.join(_HOOK_DIR, "..", "state")
_RUNNING_FILE = os.path.join(_STATE_DIR, "audit-running.json")
_LOCK_FILE = os.path.join(_STATE_DIR, "audit-running.lock")
_RUNNING_VERSION = 1

# Longer than any real audit/deslop run. An entry older than this is treated as
# a leaked lock (interrupted agent, missed release) and ignored.
TTL_SECONDS = 300 * 60

COVERAGE_AGENT = "test-coverage-auditor"
DESLOP_AGENT = "deslop-fixer"
SECURITY_AGENT = "security-auditor"
KNOWN_AGENTS = {COVERAGE_AGENT, DESLOP_AGENT, SECURITY_AGENT}


def _now():
	return int(time.time())


def _is_stale(entry, now):
	if not isinstance(entry, dict):
		return True
	started = entry.get("started_at")
	if not isinstance(started, (int, float)):
		return True
	return (now - started) >= TTL_SECONDS


def _read_running(now):
	"""Return {agent: entry} for currently-running, non-stale agents."""
	try:
		with open(_RUNNING_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict) or data.get("version") != _RUNNING_VERSION:
			return {}
		running = data.get("running")
		if not isinstance(running, dict):
			return {}
		return {
			agent: entry
			for agent, entry in running.items()
			if agent in KNOWN_AGENTS and not _is_stale(entry, now)
		}
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def _write_running(running):
	os.makedirs(_STATE_DIR, exist_ok=True)
	tmp = _RUNNING_FILE + ".tmp"
	with open(tmp, "w", encoding="utf-8") as f:
		json.dump(
			{"version": _RUNNING_VERSION, "running": running},
			f,
			indent=2,
			sort_keys=True,
		)
	os.replace(tmp, _RUNNING_FILE)


class _FileLock:
	"""Best-effort exclusive flock on the sidecar lockfile. A no-op if fcntl is
	unavailable — callers fail open in that case."""

	def __enter__(self):
		self._fd = None
		if fcntl is None:
			return self
		try:
			os.makedirs(_STATE_DIR, exist_ok=True)
			self._fd = os.open(_LOCK_FILE, os.O_CREAT | os.O_RDWR, 0o644)
			fcntl.flock(self._fd, fcntl.LOCK_EX)
		except OSError:
			if self._fd is not None:
				try:
					os.close(self._fd)
				except OSError:
					pass
				self._fd = None
		return self

	def __exit__(self, *exc):
		if self._fd is not None:
			try:
				fcntl.flock(self._fd, fcntl.LOCK_UN)
			except OSError:
				pass
			try:
				os.close(self._fd)
			except OSError:
				pass
			self._fd = None
		return False


def _conflicting(agent, running_agents):
	"""Return the sorted set of currently-running agents that block `agent`.

	deslop conflicts with anything; coverage/security conflict with deslop and
	with a second instance of themselves."""
	if agent == DESLOP_AGENT:
		return sorted(running_agents)
	conflicts = set()
	if DESLOP_AGENT in running_agents:
		conflicts.add(DESLOP_AGENT)
	if agent in running_agents:
		conflicts.add(agent)
	return sorted(conflicts)


def acquire(agent, session=None):
	"""Try to mark `agent` as running.

	Returns (ok, conflicts): ok=True and the entry is written when no
	conflicting agent is running; ok=False with the list of blocking agent
	names otherwise. Fails open (ok=True, conflicts=[]) on any internal error
	so a broken lock never wedges launches. Unknown agents are always allowed.
	"""
	if agent not in KNOWN_AGENTS:
		return True, []
	try:
		with _FileLock():
			now = _now()
			running = _read_running(now)
			conflicts = _conflicting(agent, set(running))
			if conflicts:
				return False, conflicts
			running[agent] = {"started_at": now, "session": session}
			_write_running(running)
			return True, []
	except (OSError, ValueError):
		return True, []


def blocking_agents(agent):
	"""Return the sorted list of currently-running agents that would deny
	`agent` if it tried to acquire right now. Empty list means `agent` can run.

	Read-only: never mutates running state and never holds the flock, so it is
	safe to call from the Stop hook on every stop. Stale entries are pruned by
	_read_running, so a leaked lock does not report as a live blocker. Fails
	open (returns []) on any error and for unknown agents, matching acquire()."""
	if agent not in KNOWN_AGENTS:
		return []
	try:
		return _conflicting(agent, set(_read_running(_now())))
	except (OSError, ValueError):
		return []


def blocking_sessions(agent):
	"""Like blocking_agents, but map each blocking agent name to the session that
	holds it (from the running entry; None if the entry predates session
	tracking). Lets the Stop hook tell a self-held lock (this session's own
	in-flight agent, which wakes us on completion) from a foreign one. Read-only;
	fails open (empty dict) on any error or unknown agent, matching acquire()."""
	if agent not in KNOWN_AGENTS:
		return {}
	try:
		running = _read_running(_now())
		return {
			b: (running.get(b) or {}).get("session")
			for b in _conflicting(agent, set(running))
		}
	except (OSError, ValueError):
		return {}


def release(agent, session=None):
	"""Remove `agent`'s running entry. A no-op if the entry belongs to a
	different session (prevents a duplicate/late SubagentStop from one session
	clobbering another session's live lock), unless that entry is already
	stale. Silent on any error."""
	if agent not in KNOWN_AGENTS:
		return
	try:
		with _FileLock():
			now = _now()
			try:
				with open(_RUNNING_FILE, encoding="utf-8") as f:
					data = json.load(f)
				running = data.get("running") if isinstance(data, dict) else None
			except (FileNotFoundError, OSError, json.JSONDecodeError):
				running = None
			if not isinstance(running, dict):
				return
			entry = running.get(agent)
			if entry is None:
				return
			owner = entry.get("session") if isinstance(entry, dict) else None
			if session and owner and owner != session and not _is_stale(entry, now):
				return
			del running[agent]
			_write_running(running)
	except (OSError, ValueError):
		pass
