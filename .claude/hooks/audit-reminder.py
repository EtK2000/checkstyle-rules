#!/usr/bin/env python3
"""
Stop hook: blocks Claude from finishing if it edited check/fixer/test/input
files without invoking the required audit agent(s) since.

Required audits per edited file:
- Source check/fixer (`src/main/java/.../*Check.java`, `.../gradle/fix/*Fixer.java`):
    BOTH test-coverage-auditor AND security-auditor
- Tests, input fixtures, test infra (`src/test/...`, `.../inputs/.../Input*.java`):
    test-coverage-auditor ONLY

Hook is invocation-level (option A): it blocks if the required agent has not
been invoked since the edit. It does not inspect audit findings or severity —
that's the user's judgment call.

On a re-fired stop (`stop_hook_active=true`), the hook still re-blocks if the
pending audit set has materially changed since the last block (e.g. deslop
just satisfied, coverage/security newly visible). A pending-signature cache
key prevents looping on identical pending state.
"""
import hashlib
import json
import os
import re
import sys
import time

import audit_lock
import audit_stamp

# Cache: memoize the last decision keyed by transcript fingerprint (size, mtime_ns).
# If the transcript hasn't been written since the last run, reuse the cached
# decision instead of re-walking the whole JSONL.
_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
_STATE_DIR = os.path.join(_HOOK_DIR, "..", "state")
_CACHE_FILE = os.path.join(_STATE_DIR, "audit-reminder-cache.json")
# Bump this when the hook's logic changes in a way that could produce a
# different decision from the same transcript (e.g. new patterns, new guards).
# Bumping invalidates all cached entries.
_CACHE_VERSION = 7

_STAMPS_FILE = os.path.join(_STATE_DIR, "audit-stamps.json")
_PERMISSIONS_FILE = os.path.join(_STATE_DIR, "audit-permissions.json")


def _grant_permissions(missing_agents):
	"""Record per-agent permission entries so the audit-gate PreToolUse hook
	allows the upcoming Agent call to each missing auditor. Idempotent.
	subagent-stop.py removes a permission once its agent finishes."""
	if not missing_agents:
		return
	try:
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
		for agent in missing_agents:
			perms[agent] = {"granted_at": now, "source": "stop-hook"}
		data["version"] = 1
		data["permissions"] = perms
		tmp = _PERMISSIONS_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f, indent=2, sort_keys=True)
		os.replace(tmp, _PERMISSIONS_FILE)
	except OSError:
		pass


def _read_cache():
	try:
		with open(_CACHE_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return {}
		if data.get("version") != _CACHE_VERSION:
			return {}
		return data
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def _write_cache(data):
	try:
		os.makedirs(_STATE_DIR, exist_ok=True)
		tmp = _CACHE_FILE + ".tmp"
		with open(tmp, "w", encoding="utf-8") as f:
			json.dump(data, f)
		os.replace(tmp, _CACHE_FILE)
	except OSError:
		# Caching is best-effort; a failure to write does not break the hook.
		pass


def _transcript_fingerprint(path):
	try:
		st = os.stat(path)
	except OSError:
		return None
	return [st.st_size, st.st_mtime_ns]


def _read_stamps():
	"""Read the audit-stamps.json written by subagent-stop.py (SubagentStop hook)."""
	try:
		with open(_STAMPS_FILE, encoding="utf-8") as f:
			data = json.load(f)
		if not isinstance(data, dict):
			return {}
		files = data.get("files")
		return files if isinstance(files, dict) else {}
	except (FileNotFoundError, OSError, json.JSONDecodeError):
		return {}


def _hash_file(path):
	"""Return SHA-256 hex digest of file content, or None if unreadable."""
	try:
		with open(path, "rb") as f:
			return hashlib.sha256(f.read()).hexdigest()
	except (FileNotFoundError, OSError):
		return None


def _file_audited_for(path, agent, stamps):
	"""True if file's current hash matches the stamp for the given agent."""
	canonical = os.path.realpath(path)
	entry = stamps.get(canonical)
	if not isinstance(entry, dict):
		return False
	stamp = entry.get(agent)
	if not isinstance(stamp, dict):
		return False
	current = _hash_file(canonical)
	if current is None:
		return False
	return current == stamp.get("hash")

# Patterns that require BOTH coverage-auditor AND security-auditor
SOURCE_PATTERNS = [
	# Production checks: src/main/java/com/etk2000/checkstyle/*Check.java
	re.compile(r"/src/main/java/com/etk2000/checkstyle/[^/]*Check\.java$"),
	# Production fixers: src/main/java/com/etk2000/checkstyle/gradle/fix/*Fixer.java
	re.compile(r"/src/main/java/com/etk2000/checkstyle/gradle/fix/[^/]*Fixer\.java$"),
	# Shared utilities that both checks and fixers depend on
	re.compile(r"/src/main/java/com/etk2000/checkstyle/(AstUtil|ReflectionUtil)\.java$"),
	re.compile(r"/src/main/java/com/etk2000/checkstyle/gradle/fix/(AnnotationFixerUtil|CheckstyleFix(Task|Action|er)|FixResult)\.java$"),
	re.compile(r"/src/main/java/com/etk2000/checkstyle/gradle/FixableCheckNames\.java$"),
]

# Patterns that require COVERAGE-auditor only (tests, fixtures, test infra)
COVERAGE_ONLY_PATTERNS = [
	# Check tests: src/test/java/com/etk2000/checkstyle/*Check*Test.java
	re.compile(r"/src/test/java/com/etk2000/checkstyle/[^/]*Check[^/]*Test\.java$"),
	# Fixer tests: src/test/java/com/etk2000/checkstyle/gradle/fix/*FixerTest.java
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/fix/[^/]*FixerTest\.java$"),
	# Integration tests for the fixer pipeline
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/fix/CheckstyleFix.*Test\.java$"),
	# Plugin tests
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/CheckstylePlugin.*Test\.java$"),
	# Test input resources: .../inputs/<dir>/Input*.java
	re.compile(r"/inputs/[^/]+/Input[^/]*\.java$"),
	# Test infra
	re.compile(r"/src/test/java/com/etk2000/checkstyle/(BaseCheckTest|RegexRulesTest|MessagesFileSortedTest|AstUtilTest|ReflectionUtilTest)\.java$"),
]

EDIT_TOOLS = {"Edit", "Write", "MultiEdit", "NotebookEdit"}

COVERAGE_AGENT = "test-coverage-auditor"
DESLOP_AGENT = "deslop-fixer"
SECURITY_AGENT = "security-auditor"


def _pending_signature(pending):
	"""Deterministic fingerprint of the per-file required-audit state. Used to
	detect whether `pending` has materially changed between successive Stop
	hook firings, so a re-fired stop (stop_hook_active=true) can re-block when
	the missing audits have shifted (e.g., deslop just satisfied, coverage and
	security newly visible) without looping when nothing has changed."""
	return "|".join(
		f"{p}:{','.join(sorted(agents))}"
		for p, agents in sorted(pending.items())
	)


def _wait_notice(target_desc, blockers, agents_to_run):
	"""Lines appended to a block message when the required agent(s) can't acquire
	the audit/deslop mutex because ANOTHER session holds it. (A lock held by this
	session's own run takes the defer path instead.) Tells the model to wait
	in-turn via wait-for-audit-lock.py rather than launch-and-die."""
	agents_arg = " ".join(agents_to_run)
	return [
		"",
		"LOCK — do NOT launch yet:",
		f"  {target_desc} is blocked by a conflicting agent already running in "
		f"another session: {', '.join(blockers)}. Launching now is denied "
		"by the gate and dies instantly.",
		f"  Wait in-turn, then launch in the SAME turn: "
		f".claude/hooks/wait-for-audit-lock.py {agents_arg}",
		"  Run it via Bash first (exits 0 when clear); non-zero = timed-out leaked "
		"lock, report it.",
	]


def _inflight_block_reason(agents, self_run):
	"""Block reason for when every required agent is already running on the same
	files, unchanged since that run started (an identical in-flight run). It will
	satisfy the requirement when it finishes, so the model must NOT launch or
	rerun. `self_run` is True when THIS session owns that run (its completion
	wakes us and re-checks), False when another session does."""
	names = ", ".join(agents)
	if self_run:
		origin = "a run you already started"
		tail = ("Do NOT launch or rerun — just end your turn; its completion wakes "
			"this session and re-checks.")
	else:
		origin = "a run from another session"
		tail = ("Do NOT launch or rerun — just end your turn. (If a later stop still "
			"reports these files pending, that run finished without covering them; "
			"follow that stop's instructions then.)")
	return [
		"Stop blocked by audit-reminder hook: already in flight.",
		"",
		f"The file(s) you edited still need {names}, but {origin} is already in "
		"progress on the same files, unchanged since it started. It will satisfy "
		"this requirement when it finishes, so a rerun would be redundant.",
		"",
		tail,
	]


def _classify_blockers(agents, blocker_sessions, current_session):
	"""Classify the union of lock blockers across `agents`.

	Returns (all_self, foreign_blockers): all_self is True iff there is at least
	one blocker and EVERY blocker is held by current_session (so its completion
	will wake this session); foreign_blockers is the sorted list of blocker agent
	names NOT owned by this session. A missing current_session, or a blocker whose
	holder session is unknown, counts as foreign — we only defer-to-wake when we
	can prove this session owns every lock."""
	holders = {}
	for a in agents:
		holders.update(blocker_sessions.get(a, {}))
	if not holders:
		return False, []
	foreign = sorted(
		b for b, sess in holders.items()
		if not (current_session and sess == current_session)
	)
	return (not foreign), foreign


def _self_locked_reason(target_desc, self_blockers):
	"""Block reason for when the ONLY thing preventing the required agent(s) from
	launching is THIS session's own in-flight agent holding the mutex. Unlike a
	foreign lock we do NOT tell the model to wait synchronously: its own agent's
	completion wakes this session and re-evaluates, so the model just ends the
	turn."""
	names = ", ".join(self_blockers)
	return [
		"Stop blocked by audit-reminder hook: locked by this session's own run.",
		"",
		f"{target_desc} cannot start yet: {names} is still running in THIS session "
		"and holds the audit/deslop mutex. Launching now is denied by the gate and "
		"dies instantly.",
		"",
		f"Do NOT launch, and do NOT wait synchronously. Just end your turn — when "
		f"{names} finishes it wakes this session and re-evaluates: files it covers "
		"clear automatically, and anything still pending is re-flagged then with "
		"fresh instructions.",
	]


def required_audits(path):
	"""Return set of audit names required for an edit to this path. Every
	tracked file is also deslop-eligible — deslop runs on the same file set
	covered by the audit hooks today."""
	if not path or not isinstance(path, str):
		return set()
	for p in SOURCE_PATTERNS:
		if p.search(path):
			return {COVERAGE_AGENT, DESLOP_AGENT, SECURITY_AGENT}
	for p in COVERAGE_ONLY_PATTERNS:
		if p.search(path):
			return {COVERAGE_AGENT, DESLOP_AGENT}
	return set()


def extract_edited_paths(tool_input):
	if not isinstance(tool_input, dict):
		return []
	paths = []
	fp = tool_input.get("file_path") or tool_input.get("notebook_path")
	if fp:
		paths.append(fp)
	return paths


def audit_agent_name(tool_name, tool_input):
	"""Return the audit agent name if this tool use is an audit invocation, else None."""
	if tool_name != "Agent" or not isinstance(tool_input, dict):
		return None
	sub = tool_input.get("subagent_type")
	if sub in (COVERAGE_AGENT, SECURITY_AGENT):
		return sub
	return None


def parse_ack_skip(tool_name, tool_input):
	"""If this tool use is an ack-skip.py invocation, return ("all"|"coverage"|"security"|"files", [paths]).

	Otherwise return None. Args parsing is crude (shell split) because we only
	need to recognize a few known flags.
	"""
	if tool_name != "Bash" or not isinstance(tool_input, dict):
		return None
	command = tool_input.get("command", "")
	if not isinstance(command, str) or "ack-skip.py" not in command:
		return None
	# Isolate the tokens after the script name.
	tokens = command.split()
	try:
		idx = next(i for i, t in enumerate(tokens) if t.endswith("ack-skip.py"))
	except StopIteration:
		return None
	args = tokens[idx + 1 :]
	# Strip trailing shell noise (redirects, pipes) — stop at first non-flag, non-path token.
	clean = []
	for t in args:
		if t in (">", ">>", "2>", "2>>", "|", "&&", "||", ";"):
			break
		clean.append(t)
	args = clean

	if not args or args == ["--all"]:
		return ("all", [])
	if args == ["--coverage"]:
		return ("coverage", [])
	if args == ["--security"]:
		return ("security", [])
	if args and args[0] == "--files" and len(args) > 1:
		return ("files", args[1:])
	# Unknown flag combination — treat as no-op so the hook doesn't silently clear.
	return None


def walk_transcript(path):
	"""Yield tagged events in chronological order:
	- ("user_msg", None, None)                  — a genuine user turn (not a tool_result)
	- ("tool_use", tool_name, tool_input)       — an assistant tool invocation
	"""
	with open(path, encoding="utf-8") as f:
		for raw in f:
			raw = raw.strip()
			if not raw:
				continue
			try:
				event = json.loads(raw)
			except json.JSONDecodeError:
				continue
			msg = event.get("message")
			if not isinstance(msg, dict):
				continue
			role = msg.get("role")
			content = msg.get("content")

			# Detect genuine user messages. Tool results also have role="user"
			# but their content is a list of {type: "tool_result", ...}.
			# A genuine user turn has at least one text block (or string content).
			if role == "user":
				is_genuine = False
				if isinstance(content, str) and content.strip():
					is_genuine = True
				elif isinstance(content, list):
					for block in content:
						if not isinstance(block, dict):
							continue
						if block.get("type") == "text" and block.get("text", "").strip():
							is_genuine = True
							break
				if is_genuine:
					yield ("user_msg", None, None)
				continue

			# Assistant tool_use events.
			if not isinstance(content, list):
				continue
			for block in content:
				if not isinstance(block, dict):
					continue
				if block.get("type") != "tool_use":
					continue
				yield ("tool_use", block.get("name", ""), block.get("input") or {})


def main():
	try:
		hook_input = json.load(sys.stdin)
	except json.JSONDecodeError:
		sys.stderr.write("audit-reminder: failed to parse hook input JSON\n")
		sys.exit(0)

	# stop_hook_active is set by Claude Code on stops that re-fire after a
	# prior block, to break infinite loops. We don't bail unconditionally —
	# the staggered audit flow (deslop first, then coverage/security) depends
	# on re-blocking when `pending` materially changes between firings.
	# Final loop guard lives further down, after `pending` is computed.
	stop_hook_active = bool(hook_input.get("stop_hook_active"))

	# Session that is stopping now. Used to tell whether a lock-holding agent is
	# THIS session's own in-flight run (which wakes us on completion) or a foreign
	# session's (which does not) — see the locked-message branches below.
	current_session = hook_input.get("session_id")

	transcript_path = hook_input.get("transcript_path")
	if not transcript_path or not os.path.exists(transcript_path):
		sys.exit(0)

	# Fast path: if the transcript fingerprint (size + mtime) hasn't changed
	# since the last run, no new events could have occurred, so the decision
	# must be identical. On a re-fired stop we exit silently (the model
	# already saw the block); on a fresh stop we re-print the cached reason.
	fingerprint = _transcript_fingerprint(transcript_path)
	cache = _read_cache()
	cache_by_transcript = cache.get("by_transcript", {})
	cached = cache_by_transcript.get(transcript_path) if isinstance(cache_by_transcript, dict) else None
	if (
		fingerprint is not None
		and isinstance(cached, dict)
		and cached.get("fingerprint") == fingerprint
	):
		if cached.get("blocked") and not stop_hook_active:
			print(cached.get("reason", ""))
		sys.exit(0)

	# path -> set of still-required audit agents
	pending = {}
	# True if there has been a relevant edit with NO intervening user_msg since.
	# An ack-skip is only honored when this is False (user had a chance to speak
	# between the last edit and the skip approval).
	edits_without_user_msg = False
	# Count of ack-skip invocations rejected because no user message preceded them.
	rejected_ack_skips = 0

	for kind, name, inp in walk_transcript(transcript_path):
		if kind == "user_msg":
			edits_without_user_msg = False
			continue

		# kind == "tool_use"
		tool_name, tool_input = name, inp

		# Note: Agent invocations no longer clear pending here. The SubagentStop
		# hook (subagent-stop.py) records file hashes when an agent finishes.
		# Below, after walking the transcript, we drop any pending entry whose
		# file currently hashes to its stamped value for the required agent.
		# This catches the cascade case (Claude edits the file post-audit, hash
		# diverges from stamp, file stays pending) and the revert case (file
		# reverts to a previously-audited state, hash matches stamp, cleared).

		ack = parse_ack_skip(tool_name, tool_input)
		if ack is not None:
			if edits_without_user_msg:
				# Claude ran ack-skip without any intervening user turn since the
				# last relevant edit. Treat as a protocol violation — do not clear
				# pending. The rejection is surfaced in the block reason below.
				rejected_ack_skips += 1
				continue
			mode, paths = ack
			if mode == "all":
				pending.clear()
			elif mode == "coverage":
				to_drop = []
				for p, reqs in pending.items():
					reqs.discard(COVERAGE_AGENT)
					if not reqs:
						to_drop.append(p)
				for p in to_drop:
					del pending[p]
			elif mode == "security":
				to_drop = []
				for p, reqs in pending.items():
					reqs.discard(SECURITY_AGENT)
					if not reqs:
						to_drop.append(p)
				for p in to_drop:
					del pending[p]
			elif mode == "files":
				for p in paths:
					pending.pop(p, None)
			continue

		if tool_name in EDIT_TOOLS:
			for p in extract_edited_paths(tool_input):
				reqs = required_audits(p)
				if reqs:
					pending[p] = set(reqs)
					edits_without_user_msg = True

	# Fingerprint clearing: for each pending file, drop required agents whose
	# stamps match the file's current hash. The stamps were written by
	# subagent-stop.py (SubagentStop hook) when each agent ran.
	stamps = _read_stamps()
	to_drop_paths = []
	for path, reqs in pending.items():
		if not os.path.isfile(path):
			# File no longer exists — can't audit a deleted file. Drop it.
			to_drop_paths.append(path)
			continue
		for agent in list(reqs):
			if _file_audited_for(path, agent, stamps):
				reqs.discard(agent)
		if not reqs:
			to_drop_paths.append(path)
	for p in to_drop_paths:
		del pending[p]

	def _update_cache(blocked, reason, signature):
		if fingerprint is None:
			return
		by_transcript = cache.get("by_transcript") if isinstance(cache.get("by_transcript"), dict) else {}
		by_transcript = dict(by_transcript)  # copy in case it's shared
		by_transcript[transcript_path] = {
			"fingerprint": fingerprint,
			"blocked": blocked,
			"reason": reason if blocked else "",
			"pending_signature": signature,
		}
		# Limit cache size: keep at most the last 50 transcripts to avoid
		# unbounded growth across many sessions.
		if len(by_transcript) > 50:
			# Drop the entries with the smallest fingerprint[1] (oldest mtime).
			sorted_entries = sorted(
				by_transcript.items(),
				key=lambda kv: kv[1].get("fingerprint", [0, 0])[1] if isinstance(kv[1], dict) else 0,
			)
			by_transcript = dict(sorted_entries[-50:])
		_write_cache({"version": _CACHE_VERSION, "by_transcript": by_transcript})

	# Lock awareness: for each still-required agent, which currently-running
	# agents would deny its launch (mutex conflict, usually a cross-session
	# deslop). Computed once here so it can both fold into the loop-guard
	# signature and drive the wait messaging below.
	agent_blockers = {}
	agent_blocker_sessions = {}
	for reqs in pending.values():
		for agent in reqs:
			if agent not in agent_blockers:
				holders = audit_lock.blocking_sessions(agent)
				agent_blocker_sessions[agent] = holders
				agent_blockers[agent] = sorted(holders.keys())

	# Loop guard for re-fired stops. The "pending signature" is a deterministic
	# string derived from `pending`. On a re-fire (stop_hook_active=true),
	# if the signature matches the one cached at the previous block, the
	# user already saw this exact state — exit silently. If it differs
	# (e.g., deslop just satisfied, coverage/security newly visible, or a
	# blocking lock just cleared), fall through and emit a fresh block. Folding
	# the blocker set into the signature is what lets a passive wait recover:
	# when the conflicting agent finishes, the signature changes and the block
	# re-fires with the plain run instruction instead of the wait notice.
	blocker_tags = set()
	for holders in agent_blocker_sessions.values():
		for b, sess in holders.items():
			tag = "self" if (current_session and sess == current_session) else "foreign"
			blocker_tags.add(f"{b}@{tag}")
	signature = _pending_signature(pending)
	if blocker_tags:
		signature += "|LOCK:" + ",".join(sorted(blocker_tags))
	cached_sig = cached.get("pending_signature", "") if isinstance(cached, dict) else ""
	if stop_hook_active and signature == cached_sig:
		sys.exit(0)

	if not pending:
		_update_cache(blocked=False, reason="", signature=signature)
		sys.exit(0)

	# Collapse per-file requirements into which agents must still be invoked.
	missing_agents = set()
	for reqs in pending.values():
		missing_agents.update(reqs)

	# Staggering: when any file is missing the deslop sweep, report deslop
	# ALONE this turn. Coverage/security messaging is suppressed until deslop
	# is satisfied. Rationale: deslop's edits may revert files to a
	# previously-audited hash (clearing coverage/security automatically), so
	# reporting all three at once creates churn and dumps three audit asks in
	# one turn.
	if DESLOP_AGENT in missing_agents:
		deslop_files_sorted = sorted(p for p, reqs in pending.items() if DESLOP_AGENT in reqs)
		if (DESLOP_AGENT in agent_blockers.get(DESLOP_AGENT, [])
				and audit_stamp.unchanged_inflight(DESLOP_AGENT, deslop_files_sorted)):
			self_run, _ = _classify_blockers([DESLOP_AGENT], agent_blocker_sessions, current_session)
			block_output = json.dumps({
				"decision": "block",
				"reason": "\n".join(_inflight_block_reason(["deslop-fixer"], self_run)),
			})
			_update_cache(blocked=True, reason=block_output, signature=signature)
			print(block_output)
			sys.exit(0)
		deslop_all_self, deslop_foreign = _classify_blockers(
			[DESLOP_AGENT], agent_blocker_sessions, current_session
		)
		if deslop_all_self:
			block_output = json.dumps({
				"decision": "block",
				"reason": "\n".join(
					_self_locked_reason("deslop-fixer", agent_blockers[DESLOP_AGENT])
				),
			})
			_update_cache(blocked=True, reason=block_output, signature=signature)
			print(block_output)
			sys.exit(0)
		deslop_files_list = "\n".join(f"  - {p}" for p in deslop_files_sorted)
		reason_parts = [
			"Stop blocked by audit-reminder hook: deslop sweep pending.",
			"",
			"You edited Java files that have not been swept by the deslop-fixer "
			"agent since. Deslop is a fixer (not an auditor) — it removes AI "
			"slop comments, redundant intermediate variables, one-call helper "
			"methods, defensive null checks on @NonNull params, swallowed "
			"exception rewraps, verbose assertion messages, emdashes, and other "
			"cruft the project's checkstyle config cannot express. It runs "
			"BEFORE coverage/security audits so those audits never see "
			"pre-slop code.",
			"",
			"Files needing deslop:",
			deslop_files_list,
			"",
			"What to do — invoke the deslop-fixer Agent on these files:",
			"  Tool: Agent",
			"  subagent_type: deslop-fixer",
			"  prompt: include the absolute paths above as an explicit list. "
			"The agent reads .claude/commands/deslop.md for the patterns spec, "
			"then applies edits directly via the Edit tool.",
			"",
			"NO USER APPROVAL is required for deslop. The agent applies its "
			"edits inline; the main thread just acknowledges the agent's "
			"compact summary and ends the turn. Coverage/security audits (if "
			"still pending after deslop's edits) will be reported on the next "
			"stop.",
			"",
			"Stamp behavior: deslop-fixer's edits are recorded at completion by "
			"subagent-stop. If a file's pre-deslop hash matched an existing coverage/security "
			"stamp, that stamp is promoted to the post-deslop hash — files "
			"that were already audited stay audited. New audit requirements "
			"only appear for files that already needed them before deslop ran.",
		]
		# Deslop is not in GATED_AGENTS, so audit-gate.py does not require a
		# permission entry for it. Do NOT call _grant_permissions for the
		# auditors here either — grant only when we actually report them.
		if deslop_foreign:
			reason_parts += _wait_notice("deslop-fixer", deslop_foreign, [DESLOP_AGENT])
		block_output = json.dumps({"decision": "block", "reason": "\n".join(reason_parts)})
		_update_cache(blocked=True, reason=block_output, signature=signature)
		print(block_output)
		sys.exit(0)

	agent_order = [COVERAGE_AGENT, SECURITY_AGENT]
	missing_sorted = [a for a in agent_order if a in missing_agents]
	files_sorted = sorted(pending.keys())

	# If every missing auditor is already running (identical in-flight run) on
	# the same files unchanged since it started, the requirement clears on its
	# own — tell the model not to rerun rather than dumping the full run body.
	if all(
		a in agent_blockers.get(a, [])
		and audit_stamp.unchanged_inflight(a, [p for p in files_sorted if a in pending[p]])
		for a in missing_sorted
	):
		self_run, _ = _classify_blockers(missing_sorted, agent_blocker_sessions, current_session)
		block_output = json.dumps({
			"decision": "block",
			"reason": "\n".join(_inflight_block_reason(missing_sorted, self_run)),
		})
		_update_cache(blocked=True, reason=block_output, signature=signature)
		print(block_output)
		sys.exit(0)

	audits_all_self, audits_foreign = _classify_blockers(
		missing_sorted, agent_blocker_sessions, current_session
	)
	if audits_all_self:
		target = "the audit(s)" if len(missing_sorted) != 1 else missing_sorted[0]
		self_blockers = sorted({b for a in missing_sorted for b in agent_blockers.get(a, [])})
		block_output = json.dumps({
			"decision": "block",
			"reason": "\n".join(_self_locked_reason(target, self_blockers)),
		})
		_update_cache(blocked=True, reason=block_output, signature=signature)
		print(block_output)
		sys.exit(0)

	files_list = "\n".join(f"  - {p}  (needs: {', '.join(sorted(pending[p]))})" for p in files_sorted)
	agents_list = "\n".join(f"  - {a}" for a in missing_sorted)

	reason_parts = [
		"Stop blocked by audit-reminder hook.",
		"",
		"You edited files that require one or more audits before finishing, "
		"but the required agent(s) have not been invoked since. Per docs/testing.md, "
		"the exhaustive coverage audit is mandatory before declaring testing tasks "
		"complete. The security audit is mandatory for any change to production "
		"check/fixer code.",
		"",
		"Files needing audit:",
		files_list,
		"",
		"Missing audit(s):",
		agents_list,
	]

	if rejected_ack_skips > 0:
		reason_parts += [
			"",
			f"NOTE: {rejected_ack_skips} ack-skip invocation(s) were REJECTED because no "
			"user message appeared between the most recent edit and the ack-skip call. "
			"This is the anti-abuse guard — skip approval must come FROM THE USER in a "
			"fresh turn after seeing a summary of the edits. An ack-skip with no "
			"intervening user turn looks like an auto-skip and is ignored.",
		]

	reason_parts += [
		"",
		"Option 1 — run the audits:",
		"  Invoke the Agent tool with subagent_type set to each missing agent, "
		"passing the relevant source / test / input file paths explicitly. When "
		"the agent returns its report:",
		"    - Show the report to the user.",
		"    - For HIGH-severity findings, propose a concrete fix (file:line "
		"diff sketch) but DO NOT apply it.",
		"    - For MED and LOW findings, list them and ask which the user wants "
		"addressed.",
		"    - If a finding includes a `Patch:` block, the auditor has pre-typed "
		"the exact edit. List those patches in the user-facing summary so the user "
		"knows what would be applied. Do NOT re-type the fix or restate it in your "
		"own words. Once the user approves, apply each patch via Edit (one Edit "
		"per anchor, batched in a single turn). Patch-bearing findings still wait "
		"for explicit user approval — patches do not authorize auto-apply.",
		"  WAIT for the user to instruct you which fixes to apply. Do NOT "
		"auto-apply fixes between the audit returning and stopping. If you do "
		"apply fixes, those edits will create new pending entries that require "
		"another audit cycle (correct behavior, but avoidable churn).",
		"  Detection note: pending is determined by file content hashes, not "
		"by transcript events. After an agent runs, the file's current state "
		"is stamped. If the file still hashes to that value at stop time, it's "
		"considered audited. If it has changed (or you reverted to a previously-"
		"audited state), the hash comparison handles it correctly.",
		"",
		"Option 2 — request explicit skip approval (ONLY for edits with no "
		"behavioral impact — e.g. a slop-comment removal, a reverted change, a "
		"typo fix). The correct order is IMPORTANT:",
		"  Step (a): present a one-paragraph summary of what you edited to the "
		"user. Ask for explicit approval to skip. STOP and wait for the user to "
		"respond.",
		"  Step (b): AFTER the user responds with approval, run "
		"`.claude/hooks/ack-skip.py` via the Bash tool. The hook enforces this "
		"ordering: it requires a user message in the transcript between the last "
		"relevant edit and the ack-skip invocation. A skip request from the PREVIOUS "
		"turn does NOT carry over to new edits. Each batch of edits needs its own "
		"fresh approval.",
		"  Available modes:",
		"    - `.claude/hooks/ack-skip.py`            (default: skip ALL pending)",
		"    - `.claude/hooks/ack-skip.py --coverage` (skip only the coverage audit)",
		"    - `.claude/hooks/ack-skip.py --security` (skip only the security audit)",
		"    - `.claude/hooks/ack-skip.py --files <path> [<path>...]`  (skip specific files)",
		"",
		"  Running ack-skip.py without prior user approval (or before the user "
		"responds in THIS turn) is a protocol violation and will be mechanically "
		"rejected by the hook.",
	]

	if audits_foreign:
		target = "the audit(s)" if len(missing_sorted) != 1 else missing_sorted[0]
		reason_parts += _wait_notice(target, audits_foreign, missing_sorted)

	_grant_permissions(missing_sorted)
	block_output = json.dumps({"decision": "block", "reason": "\n".join(reason_parts)})
	_update_cache(blocked=True, reason=block_output, signature=signature)
	print(block_output)
	sys.exit(0)


if __name__ == "__main__":
	main()