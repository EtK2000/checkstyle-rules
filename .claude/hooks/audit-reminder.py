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

A block can be cleared without auditing by running `.claude/hooks/ack-skip.py`,
but only when the user picked the "Skip the audit" option in an AskUserQuestion
— in this session, not yet spent on an earlier ack-skip, and since the last
relevant edit. Claude Code writes that answer text, so it is an approval the
model cannot issue itself — and unlike a permission prompt, a
refusal comes back as an ordinary tool result instead of aborting the turn,
which lets the model go straight on to running the audits.

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

# Bash commands name paths relative to the repo, unlike the Edit tool's absolute ones.
_PROJECT_ROOT = os.environ.get("CLAUDE_PROJECT_DIR") or os.path.join(
	os.path.dirname(os.path.abspath(__file__)), "..", ".."
)


def _resolve_repo_path(raw):
	return os.path.realpath(raw if os.path.isabs(raw) else os.path.join(_PROJECT_ROOT, raw))

# Cache: memoize the last decision keyed by transcript fingerprint (size, mtime_ns).
# If the transcript hasn't been written since the last run, reuse the cached
# decision instead of re-walking the whole JSONL.
_HOOK_DIR = os.path.dirname(os.path.abspath(__file__))
_STATE_DIR = os.path.join(_HOOK_DIR, "..", "state")
_CACHE_FILE = os.path.join(_STATE_DIR, "audit-reminder-cache.json")
# Bump this when the hook's logic changes in a way that could produce a
# different decision from the same transcript (e.g. new patterns, new guards).
# Bumping invalidates all cached entries.
_CACHE_VERSION = 12

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
	re.compile(r"/src/main/java/com/etk2000/checkstyle/ReflectionUtil\.java$"),
	re.compile(r"/src/main/java/com/etk2000/checkstyle/ast/Ast(Display|Query|Resolve|Text)\.java$"),
	# Check logic lifted out of MultilineCallFormattingCheck (item 2.1 of the consolidation doc)
	re.compile(r"/src/main/java/com/etk2000/checkstyle/(ContextReceiverIndex|JsonObjectPutCollapse|LineCollapse|MultilineCallMoves)\.java$"),
	re.compile(r"/src/main/java/com/etk2000/checkstyle/gradle/fix/(AnnotationFixerUtil|CheckstyleFix(Task|Action|er)|FixResult|LambdaCallParser)\.java$"),
	re.compile(r"/src/main/java/com/etk2000/checkstyle/gradle/FixableCheckNames\.java$"),
	# Every reformatter/classifier in the format package: fixers delegate their re-emission to these, so
	# an edit here rewrites source exactly as a fixer edit does. Matched by package rather than by name,
	# because hand-listing is what let the whole package fall through the gate in the first place
	re.compile(r"/src/main/java/com/etk2000/checkstyle/format/(?!package-info)[^/]*\.java$"),
]

# Patterns that require COVERAGE-auditor only (tests, fixtures, test infra)
COVERAGE_ONLY_PATTERNS = [
	# Check tests: src/test/java/com/etk2000/checkstyle/*Check*Test.java
	re.compile(r"/src/test/java/com/etk2000/checkstyle/[^/]*Check[^/]*Test\.java$"),
	# Tests for the classes split out of a check, which no longer carry "Check" in the name
	re.compile(r"/src/test/java/com/etk2000/checkstyle/MultilineCallMovesTest\.java$"),
	# Fixer tests: src/test/java/com/etk2000/checkstyle/gradle/fix/*FixerTest.java
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/fix/[^/]*FixerTest\.java$"),
	# Integration tests for the fixer pipeline
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/fix/CheckstyleFix.*Test\.java$"),
	# Plugin tests
	re.compile(r"/src/test/java/com/etk2000/checkstyle/gradle/CheckstylePlugin.*Test\.java$"),
	# Test input resources: .../inputs/<dir>/Input*.java
	re.compile(r"/inputs/[^/]+/Input[^/]*\.java$"),
	# Test infra
	re.compile(r"/src/test/java/com/etk2000/checkstyle/(BaseCheckTest|RegexRulesTest|MessagesFileSortedTest|ReflectionUtilTest)\.java$"),
	re.compile(r"/src/test/java/com/etk2000/checkstyle/ast/Ast[A-Za-z]*\.java$"),
]

EDIT_TOOLS = {"Edit", "Write", "MultiEdit", "NotebookEdit"}

# A Bash command counts as an edit when a write construct names an audit-relevant path
# as its TARGET. Auto mode routes file changes through `sed -i`, redirects and python
# heredocs instead of the Edit tool, so without this a session that did all its work in
# Bash raises no audit at all — which is exactly how the item 2.1 split shipped unaudited.
#
# Only the target counts. Matching any write-looking token against any path in the same
# command flagged a heredoc that wrote a fixture elsewhere while quoting a source path as
# a string constant. The cost of the precision is a write whose target is a VARIABLE
# (`open(p, "w")`, then `f.write(...)`): it names no path here and is invisible to this
# heuristic, which is one more reason file changes go through the Edit tool.
_PATH = r"[\w./-]*src/(?:main|test)/(?:java|resources)/[\w./-]+"

# Write constructs whose own match captures the path(s) they write.
BASH_WRITE_TARGET_RES = [
	# `> path`, `>> path`, `2> path`
	re.compile(r"\d?>>?\s*['\"]?(" + _PATH + r")"),
	# `tee path`, `tee -a path`
	re.compile(r"\btee\b\s+(?:-a\s+)?['\"]?(" + _PATH + r")"),
	# `open('path', 'w')`, `open("path", mode="a")`, and the `\"path\"` spelling
	# a nested double-quoted command produces
	re.compile(
		r"open\(\s*\\?['\"](" + _PATH + r")\\?['\"]\s*,\s*(?:mode\s*=\s*)?\\?['\"][wax]"
	),
	# `Files.writeString(Path.of("path"), ...)` — the path is the FIRST argument,
	# so a quoted path elsewhere in the call is content, not a target
	re.compile(r"writeString\(\s*(?:Path\.of\(|Paths\.get\()?\s*['\"](" + _PATH + r")['\"]"),
	# `git mv a b`, `cp a b`, `mv a b` — source and destination both change
	re.compile(
		r"(?:git\s+mv|\b(?:cp|mv))\s+['\"]?(" + _PATH + r")['\"]?"
		r"(?:\s+['\"]?(" + _PATH + r")['\"]?)?"
	),
]

# Write constructs whose targets are the standalone arguments that follow them,
# up to the next command separator.
BASH_WRITE_ARG_RES = [re.compile(r"sed\s+-i\b")]

_ARG_PATH_RE = re.compile(r"(?:^|(?<=\s))['\"]?(" + _PATH + r")['\"]?(?=\s|$)")
_SEPARATOR_RE = re.compile(r"[;|&\n]")

# Where an ack-skip.py invocation's arguments end: a redirect, a pipe or a
# command separator, in any of their glued spellings (`2>&1`, `>out`, `;`).
_SHELL_NOISE_RE = re.compile(r"^\d*[<>|&;]")

_ACK_SKIP_SCRIPT = "ack-skip.py"

# The script counts as RUN only where it sits in command position: the start of
# the command or just after a separator, behind nothing but env assignments. A
# command that merely names it - a quoted path inside a heredoc, a grep pattern -
# used to parse as a real invocation, and since it exits 0 it silently spent the
# user's standing approval and denied the call that followed.
_ACK_SKIP_INVOCATION_RE = re.compile(
	r"(?:^|[;&|(){}\n])\s*(?:[A-Za-z_]\w*=\S*\s+)*(?:\S*/)?" + re.escape(_ACK_SKIP_SCRIPT) + r"(?=\s|$)"
)

COVERAGE_AGENT = "test-coverage-auditor"
DESLOP_AGENT = "deslop-fixer"
SECURITY_AGENT = "security-auditor"

# How a skip is approved: the user PICKS this option label in an AskUserQuestion.
# Claude Code writes the answer text, not the model, so a chosen label is the one
# approval signal that cannot be self-issued — and unlike a permission prompt,
# a refusal comes back as an ordinary tool result instead of aborting the turn.
SKIP_APPROVAL_LABEL = "Skip the audit"
SKIP_QUESTION_TOOL = "AskUserQuestion"

# Chosen labels in an answered result: `..."<question>"="<label>[, <label>...]"`.
_ANSWER_RE = re.compile(r'="(.*?)"')


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


def _ack_notices(unapproved):
	"""Note for ack-skip invocations that ran unapproved since the last edit, so
	their cheerful "acknowledging skip" output isn't read as success.

	Only reachable when require-skip-approval.py is absent or failed open — it
	denies these at call time, with a fuller reason than this could carry. A
	DENIED ack needs no note at all for the same reason."""
	if not unapproved:
		return []
	return [
		"",
		f"NOTE: {unapproved} unapproved ack-skip run(s) ignored — the approval is the user "
		f"picking \"{SKIP_APPROVAL_LABEL}\", not the run. See Option 2.",
	]


def _protocol(already_printed):
	"""The two ways out of an audit block.

	Printed in full on a transcript's first block and in short form after: the
	protocol never changes, so repeating ~3.4k characters every cycle is pure
	context cost. The short form keeps what cannot be reconstructed — the exact
	option label and the ack-skip modes — and drops the prose around them.

	What to do with a returned audit report is NOT here: it belongs with the
	report, and each auditor's own output format carries it."""
	if already_printed:
		return [
			"",
			"Option 1 — run the audits: invoke the Agent tool with subagent_type set "
			"to each missing agent, passing the file paths above explicitly.",
			"Option 2 — ask to skip (no-behavioral-impact edits only), all in ONE "
			"turn: state the case, then AskUserQuestion with options labeled exactly "
			f"\"{SKIP_APPROVAL_LABEL}\" and \"Run the audits\". On "
			f"\"{SKIP_APPROVAL_LABEL}\": `.claude/hooks/ack-skip.py` "
			"[--coverage|--security|--files <path>...]. On anything else, including "
			"a typed reply: re-ask once for a picked option, or run the audits.",
			"(Full protocol was printed with this session's first audit block.)",
		]
	return [
		"",
		"Option 1 — run the audits:",
		"  Invoke the Agent tool with subagent_type set to each missing agent, "
		"passing the relevant source / test / input file paths explicitly. Each "
		"auditor's report ends with a Handling section — follow it when the report "
		"returns.",
		"  Detection note: pending is determined by file content hashes, not "
		"by transcript events. After an agent runs, the file's current state "
		"is stamped. If the file still hashes to that value at stop time, it's "
		"considered audited. If it has changed (or you reverted to a previously-"
		"audited state), the hash comparison handles it correctly.",
		"",
		"Option 2 — ask to skip, inline (ONLY for edits with no behavioral impact "
		"— e.g. a slop-comment removal, a reverted change, a typo fix). It all "
		"happens in ONE turn: never end your turn on the question.",
		"  Step (a): state the case for the skip in your message — what you "
		"edited, and why it cannot change behavior. Keep it to a few lines.",
		"  Step (b): in the SAME turn, call the AskUserQuestion tool with exactly "
		f"two options: one labeled exactly \"{SKIP_APPROVAL_LABEL}\", one labeled "
		"\"Run the audits\". Do not append \"(Recommended)\" or any other text to "
		"the skip label — the hook matches it exactly. Your Step (a) text is what "
		"the user is deciding on.",
		"  Step (c): the answer returns as an ordinary tool result, so your turn "
		"keeps running. Act on it immediately, in the same turn:",
		f"    - \"{SKIP_APPROVAL_LABEL}\" picked: run `.claude/hooks/ack-skip.py` "
		"(modes below) and carry on.",
		"    - \"Run the audits\" picked: go straight to Option 1. Do NOT re-ask, "
		"and do NOT run ack-skip.py.",
		"    - A free-typed reply or no option selected — including one that "
		"plainly means yes: only a picked label is recorded, so it cannot be the "
		"approval. Re-ask ONCE, saying you need the button because that is what "
		"the hook reads, and act on that answer. Never treat the typed text as "
		"approval, and never silently run the audits against what they said.",
		"  Available modes:",
		"    - `.claude/hooks/ack-skip.py`            (default: skip ALL pending)",
		"    - `.claude/hooks/ack-skip.py --coverage` (skip only the coverage audit)",
		"    - `.claude/hooks/ack-skip.py --security` (skip only the security audit)",
		"    - `.claude/hooks/ack-skip.py --files <path> [<path>...]`  (skip specific files)",
		"",
		f"  What the hook honors: an ack-skip that ran with a \"{SKIP_APPROVAL_LABEL}\" "
		"answer recorded since the last audit-relevant edit, in this session, not "
		"already spent on an earlier ack-skip. Running the script is not the "
		"approval — the recorded answer is, and Claude Code writes that text, not "
		"you. Any edit afterwards voids it: later edits need their own question.",
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


def bash_edited_paths(command):
	"""Audit-relevant existing files a Bash command writes, as the target of a
	write construct. A path the command merely mentions — a string constant, a
	path inside a sed script, a grep argument — is not an edit.

	Requiring the path to resolve to a real file also filters captures that were
	never a target, such as a path appearing inside a regex or a log message.
	"""
	if not isinstance(command, str):
		return []
	raw_paths = []
	for pattern in BASH_WRITE_TARGET_RES:
		for match in pattern.finditer(command):
			raw_paths.extend(group for group in match.groups() if group)
	for pattern in BASH_WRITE_ARG_RES:
		for match in pattern.finditer(command):
			tail = command[match.end():]
			separator = _SEPARATOR_RE.search(tail)
			raw_paths.extend(_ARG_PATH_RE.findall(tail[:separator.start()] if separator else tail))
	found = []
	for raw in raw_paths:
		path = _resolve_repo_path(raw)
		if path not in found and os.path.isfile(path) and required_audits(path):
			found.append(path)
	return found


def _ack_mode(args):
	"""Map ack-skip's argument tokens to ("all"|"coverage"|"security"|"files", [paths]).

	None for an unrecognized combination, so neither caller acts on a command
	the script itself would reject. Parsing is crude (shell split) because we
	only need to recognize a few known flags.
	"""
	# Stop at the first shell-noise token. Matching by leading character rather
	# than by an exact list so glued forms (`2>&1`, `>out.txt`, `&&foo`) end the
	# args too — an unlisted `2>&1` used to land in `args` and turn the whole
	# invocation into an unrecognized no-op.
	clean = []
	for t in args:
		if _SHELL_NOISE_RE.match(t):
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
	return None


def names_ack_skip(tool_name, tool_input):
	"""The command NAMES ack-skip.py anywhere, disguised spellings included.

	What the PreToolUse gate asks, because its two error directions are not
	equal: denying a command that merely mentions the script costs a retry,
	while letting a disguised call through spends the approval the gate exists
	to protect.
	"""
	if tool_name != "Bash" or not isinstance(tool_input, dict):
		return None
	command = tool_input.get("command", "")
	if not isinstance(command, str) or _ACK_SKIP_SCRIPT not in command:
		return None
	tokens = command.split()
	try:
		idx = next(i for i, t in enumerate(tokens) if t.endswith(_ACK_SKIP_SCRIPT))
	except StopIteration:
		return None
	return _ack_mode(tokens[idx + 1:])


def parse_ack_skip(tool_name, tool_input):
	"""The command RAN ack-skip.py, with the script in command position.

	What the Stop hook asks, where the directions reverse: counting a mere
	mention spends the standing approval and clears pending that nobody
	acknowledged, so this one demands a real invocation.
	"""
	if tool_name != "Bash" or not isinstance(tool_input, dict):
		return None
	command = tool_input.get("command", "")
	if not isinstance(command, str):
		return None
	match = _ACK_SKIP_INVOCATION_RE.search(command)
	if match is None:
		return None
	return _ack_mode(command[match.end():].split())


def _iter_messages(path):
	"""Yield (role, content, session_id) for every transcript line that carries
	a message. The session id is what scopes a skip approval to the session it
	was given in."""
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
			if isinstance(msg, dict):
				yield msg.get("role"), msg.get("content"), event.get("sessionId")


def tool_results(path):
	"""Map tool_use_id -> (is_error, result_text).

	An id missing from this map has no recorded result at all — the call never
	completed, so it is never one we act on. is_error covers a refusal by the
	user, by the auto-mode classifier, and by a PreToolUse hook alike."""
	results = {}
	for role, content, _ in _iter_messages(path):
		if role != "user" or not isinstance(content, list):
			continue
		for block in content:
			if not isinstance(block, dict) or block.get("type") != "tool_result":
				continue
			uid = block.get("tool_use_id")
			if uid:
				text = block.get("content")
				results[uid] = (bool(block.get("is_error")), text if isinstance(text, str) else "")
	return results


def chose_skip(result_text):
	"""True when SKIP_APPROVAL_LABEL is among the options the user picked in an
	AskUserQuestion result. A free-typed answer records `(no option selected)`
	and deliberately does not count — only a click is mechanically verifiable."""
	for answer in _ANSWER_RE.findall(result_text or ""):
		for label in answer.split(","):
			if label.strip().strip('"').lower() == SKIP_APPROVAL_LABEL.lower():
				return True
	return False


def edited_audit_paths(tool_name, tool_input):
	"""Audit-relevant paths this tool use writes. Empty for everything else,
	including an edit to a file no audit covers."""
	if tool_name in EDIT_TOOLS:
		paths = extract_edited_paths(tool_input)
	elif tool_name == "Bash":
		paths = bash_edited_paths(tool_input.get("command")) if isinstance(tool_input, dict) else []
	else:
		return []
	return [p for p in paths if required_audits(p)]


def is_skip_approval(tool_name, tool_use_id, results, event_session=None, session=None):
	"""True when this tool use is an AskUserQuestion whose recorded answer
	picked SKIP_APPROVAL_LABEL.

	`session` scopes the grant: an approval counts only when the event naming it
	belongs to the session now asking. Strict on purpose — an approval with no
	session on it grants nothing — so a grant can never be read, or consumed, by
	a session other than the one the user gave it in. `session=None` (a hook that
	passed no session id) turns the scoping off, not the approval itself."""
	if tool_name != SKIP_QUESTION_TOOL:
		return False
	if session is not None and event_session != session:
		return False
	error, text = results.get(tool_use_id, (True, ""))
	return not error and chose_skip(text)


def ack_consumed_approval(tool_name, tool_input, tool_use_id, results):
	"""True when this tool use is an ack-skip that RAN, so it spends the standing
	approval: one approval authorizes one skip. A denied or never-completed
	invocation spends nothing."""
	if parse_ack_skip(tool_name, tool_input) is None:
		return False
	status = results.get(tool_use_id)
	return status is not None and not status[0]


def skip_approval_on_record(transcript_path, session=None):
	"""Whether a skip approval stands as of the end of the transcript: picked by
	this session, not yet spent on an ack-skip, and with no audit-relevant edit
	after it. The require-skip-approval PreToolUse gate asks this before letting
	ack-skip.py run, so a call it allows is one this hook would go on to honor.

	A write voids a standing approval whoever made it, while only this session's
	own grants count — lenient about voiding, strict about granting."""
	results = tool_results(transcript_path)
	approved = False
	for tool_name, tool_input, tool_use_id, event_session in walk_transcript(transcript_path):
		if is_skip_approval(tool_name, tool_use_id, results, event_session, session):
			approved = True
		elif ack_consumed_approval(tool_name, tool_input, tool_use_id, results):
			approved = False
		elif edited_audit_paths(tool_name, tool_input):
			approved = False
	return approved


def walk_transcript(path):
	"""Yield (tool_name, tool_input, tool_use_id, session_id) for each assistant
	tool invocation, in chronological order."""
	for role, content, session in _iter_messages(path):
		if role != "assistant" or not isinstance(content, list):
			continue
		for block in content:
			if not isinstance(block, dict):
				continue
			if block.get("type") != "tool_use":
				continue
			yield block.get("name", ""), block.get("input") or {}, block.get("id"), session


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
	# session's (which does not) — see the locked-message branches below — and to
	# scope a skip approval to the session the user gave it in.
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

	results = tool_results(transcript_path)

	# path -> set of still-required audit agents
	pending = {}
	# Whether the user picked SKIP_APPROVAL_LABEL since the last relevant edit.
	# Reset by every audit-relevant edit, so an approval never covers work the
	# user had not seen when they gave it.
	skip_approved = False
	# ack-skip invocations that ran unapproved since the last edit. Reset with
	# the approval, so an old mistake stops being reported once it is moot.
	unapproved_ack_skips = 0

	for tool_name, tool_input, tool_use_id, event_session in walk_transcript(transcript_path):
		if tool_name == SKIP_QUESTION_TOOL:
			if is_skip_approval(tool_name, tool_use_id, results, event_session, current_session):
				skip_approved = True
			continue

		# Note: Agent invocations no longer clear pending here. The SubagentStop
		# hook (subagent-stop.py) records file hashes when an agent finishes.
		# Below, after walking the transcript, we drop any pending entry whose
		# file currently hashes to its stamped value for the required agent.
		# This catches the cascade case (Claude edits the file post-audit, hash
		# diverges from stamp, file stays pending) and the revert case (file
		# reverts to a previously-audited state, hash matches stamp, cleared).

		ack = parse_ack_skip(tool_name, tool_input)
		if ack is not None:
			status = results.get(tool_use_id)
			if status is None:
				# No result recorded — the invocation never completed.
				continue
			if status[0]:
				# Blocked before it ran — require-skip-approval.py already told
				# the model why, at the call. Clears nothing, needs no note.
				continue
			if not skip_approved:
				unapproved_ack_skips += 1
				continue
			# One approval authorizes one skip: spend it here, so a second
			# ack-skip needs the user to be asked again.
			skip_approved = False
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
				# ack-skip is run from the repo root, so its paths are usually
				# relative while `pending` is keyed absolute. Matching on the
				# resolved path rather than the spelling keeps a relative call
				# from clearing nothing while still spending the approval
				for p in paths:
					target = _resolve_repo_path(p)
					for key in [k for k in pending if _resolve_repo_path(k) == target]:
						del pending[key]
			continue

		for p in edited_audit_paths(tool_name, tool_input):
			pending[p] = set(required_audits(p))
			skip_approved = False
			unapproved_ack_skips = 0

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

	# The audit protocol is long and identical every time, so it is printed in
	# full once per transcript and in short form afterwards. Carried in the
	# cache, which is already per-transcript.
	protocol_printed_before = bool(cached.get("protocol_printed")) if isinstance(cached, dict) else False

	def _update_cache(blocked, reason, signature, printed_protocol=False):
		if fingerprint is None:
			return
		by_transcript = cache.get("by_transcript") if isinstance(cache.get("by_transcript"), dict) else {}
		by_transcript = dict(by_transcript)  # copy in case it's shared
		by_transcript[transcript_path] = {
			"fingerprint": fingerprint,
			"blocked": blocked,
			"reason": reason if blocked else "",
			"pending_signature": signature,
			"protocol_printed": printed_protocol or protocol_printed_before,
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
		# What deslop is hiding. Reported but not actionable: the audits stay
		# suppressed until deslop clears. Saying it here is what lets a skip
		# request cover the whole set in one ask instead of one ask per cycle.
		also_stale = sorted(
			{a for p in deslop_files_sorted for a in pending[p] if a != DESLOP_AGENT}
		)
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
		]
		if also_stale:
			reason_parts += [
				"",
				"Also stale for these files, reported once deslop clears: "
				+ ", ".join(also_stale)
				+ ". Not actionable yet — deslop's edits can change what they see. "
				"Named here so a skip request covers the whole set in one ask "
				"instead of one ask per cycle.",
			]
		reason_parts += [
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
		reason_parts += _ack_notices(unapproved_ack_skips)
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

	reason_parts += _ack_notices(unapproved_ack_skips)

	reason_parts += _protocol(protocol_printed_before)

	if audits_foreign:
		target = "the audit(s)" if len(missing_sorted) != 1 else missing_sorted[0]
		reason_parts += _wait_notice(target, audits_foreign, missing_sorted)

	_grant_permissions(missing_sorted)
	block_output = json.dumps({"decision": "block", "reason": "\n".join(reason_parts)})
	_update_cache(blocked=True, reason=block_output, signature=signature, printed_protocol=True)
	print(block_output)
	sys.exit(0)


if __name__ == "__main__":
	main()