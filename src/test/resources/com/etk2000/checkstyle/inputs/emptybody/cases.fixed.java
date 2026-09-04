package com.etk2000.checkstyle.inputs.emptybody;

// `ControlFlowBracesFixer` braces the outer `if` first, which puts the empty `else`'s owner
// inside a block. The rebind the fixer alone refuses can no longer happen there, so the
// pipeline goes on to remove the `else`.
// === case: else_in_braceless_then_returns_skip ===
// skip-reason: cannot remove the else: a later else would bind to a nearer if
class InputEmptyBodyElseInBracelessThenReturnsSkipSliceViolation {
	void m(boolean a, boolean b) {
		if (a) {
			if (b)
				System.out.println("both");
		}
		else
			System.out.println("neither");
	}
}
// === end ===

// Empty loop bodies are warnings, and the fix pipeline only acts on errors, so the
// pipeline leaves every loop standing. What it does change here is `int i` to `var i`.
// === case: loop_warns ===
// imports: java.util.List
class InputEmptyLoopViolation {
	void emptyDoWhileBlock(int x) {
		do {
		} while (x > 0);
	}

	void emptyDoWhileStatement(int x) {
		do;
		while (x > 0);
	}

	void emptyForBlock(int x) {
		for (var i = 0; i < x; ++i) {
		}
	}

	void emptyForEachStatement(List<String> list) {
		for (var s : list);
	}

	void emptyForStatement(int x) {
		for (var i = 0; i < x; ++i);
	}

	void emptyWhileBlock(int x) {
		while (x > 0) {
		}
	}

	void emptyWhileStatement(int x) {
		while (x > 0);
	}
}
// === end ===