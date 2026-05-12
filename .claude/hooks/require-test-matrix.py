#!/usr/bin/env python3
"""
PreToolUse hook: blocks Edit/Write on production source files until the
conversation transcript contains evidence that a test matrix was presented.

Production files: src/main/java/**/*.java
Exempt files: test files, docs, configs, build files, resources, hooks, etc.

The hook is bypassed when a /deslop skill invocation is found in the
transcript (comment-only cleanup doesn't need a test matrix).

Otherwise, the hook looks for assistant messages containing test-matrix
markers: at least 3 of these indicators in the same assistant message:
  - "clean" or "clean file" or "clean case"
  - "violation" or "violation file" or "violation case"
  - "boundary" or "boundary pair"
  - "token type" or "AST token"
  - "permutation" or "matrix" or "coverage matrix"
  - "edge case"

This is a heuristic, not a guarantee. It catches the obvious case of jumping
straight to code without presenting any test plan.
"""
import json
import os
import sys

# Minimum number of distinct matrix indicators in a single assistant message
_MIN_INDICATORS = 3

_INDICATORS = [
	["clean", "clean file", "clean case"],
	["violation", "violation file", "violation case"],
	["boundary", "boundary pair"],
	["token type", "ast token"],
	["permutation", "matrix", "coverage matrix"],
	["edge case"],
]


def _is_production_source(file_path):
	"""Return True if the file is a production Java source file."""
	if not file_path:
		return False
	normalized = file_path.replace("\\", "/")
	return "src/main/java/" in normalized and normalized.endswith(".java")


def _count_indicators(text):
	"""Count how many distinct indicator groups appear in the text."""
	lower = text.lower()
	count = 0
	for group in _INDICATORS:
		if any(term in lower for term in group):
			count += 1
	return count


def _scan_transcript(transcript_path):
	"""Walk the transcript looking for a matrix OR a deslop skill invocation."""
	if not transcript_path or not os.path.exists(transcript_path):
		return False
	with open(transcript_path, encoding="utf-8") as f:
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
			if role == "assistant":
				if isinstance(content, str):
					if _count_indicators(content) >= _MIN_INDICATORS:
						return True
				elif isinstance(content, list):
					full_text = ""
					for block in content:
						if isinstance(block, dict) and block.get("type") == "text":
							full_text += block.get("text", "") + "\n"
					if _count_indicators(full_text) >= _MIN_INDICATORS:
						return True
			elif role == "user":
				if isinstance(content, str) and "<command-name>/deslop</command-name>" in content:
					return True
				elif isinstance(content, list):
					for block in content:
						if isinstance(block, dict) and block.get("type") == "text":
							if "<command-name>/deslop</command-name>" in block.get("text", ""):
								return True
	return False


def main():
	try:
		hook_input = json.load(sys.stdin)
	except json.JSONDecodeError:
		sys.exit(0)

	tool_input = hook_input.get("tool_input", {})
	file_path = tool_input.get("file_path", "")

	if not _is_production_source(file_path):
		sys.exit(0)

	transcript_path = hook_input.get("transcript_path")
	if _scan_transcript(transcript_path):
		sys.exit(0)

	reason = (
		"BLOCKED: You are editing a production source file without presenting a "
		"test matrix first. Per docs/testing.md, the test matrix is the FIRST "
		"deliverable before any implementation code.\n\n"
		"Present the matrix now. It must include:\n"
		"  1. Every AST token type the check/fixer will handle\n"
		"  2. Clean examples (code that should NOT trigger)\n"
		"  3. Violation examples (code that SHOULD trigger)\n"
		"  4. Boundary pairs (nearby clean/violation values)\n"
		"  5. Edge cases (empty, nested, unusual constructs)\n"
		"  6. Fixer return paths (null, SkipResult, FixResult triggers)\n\n"
		"Write actual code snippets, not descriptions. The matrix IS the plan."
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
