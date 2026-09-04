package com.etk2000.checkstyle.inputs.infiniteemptyloop;

// === case: do_while_true_braced ===
class InputInfiniteEmptyLoopDoWhileTrueBracedSliceViolation {
	void m() {
		do {
		} while (true);
	}
}
// === end ===

// === case: do_while_true_braced_block_comment ===
class InputInfiniteEmptyLoopDoWhileTrueBracedBlockCommentSliceViolation {
	void m() {
		do { /* spin */ } while (true);
	}
}
// === end ===

// === case: do_while_true_braced_line_comment ===
class InputInfiniteEmptyLoopDoWhileTrueBracedLineCommentSliceViolation {
	void m() {
		do {
			// spin
		} while (true);
	}
}
// === end ===

// === case: do_while_true_condition_comment ===
class InputInfiniteEmptyLoopDoWhileTrueConditionCommentSliceViolation {
	void m() {
		do {} while (/* c */ true);
	}
}
// === end ===

// === case: do_while_true_condition_trailing_comment ===
class InputInfiniteEmptyLoopDoWhileTrueConditionTrailingCommentSliceViolation {
	void m() {
		do {} while (true /* c */);
	}
}
// === end ===

// === case: do_while_true_naked ===
class InputInfiniteEmptyLoopDoWhileTrueNakedSliceViolation {
	void m() {
		do;
		while (true);
	}
}
// === end ===

// === case: do_while_true_naked_comment_before_semi ===
class InputInfiniteEmptyLoopDoWhileTrueNakedCommentBeforeSemiSliceViolation {
	void m() {
		do /* spin */;
		while (true);
	}
}
// === end ===

// === case: for_empty_braced ===
class InputInfiniteEmptyLoopForEmptyBracedSliceViolation {
	void m() {
		for (;;) {
		}
	}
}
// === end ===

// === case: for_empty_braced_block_comment ===
class InputInfiniteEmptyLoopForEmptyBracedBlockCommentSliceViolation {
	void m() {
		for (;;) { /* spin */ }
	}
}
// === end ===

// === case: for_empty_braced_line_comment ===
class InputInfiniteEmptyLoopForEmptyBracedLineCommentSliceViolation {
	void m() {
		for (;;) {
			// spin
		}
	}
}
// === end ===

// === case: for_empty_naked ===
class InputInfiniteEmptyLoopForEmptyNakedSliceViolation {
	void m() {
		for (;;);
	}
}
// === end ===

// === case: for_empty_naked_comment_before_semi ===
class InputInfiniteEmptyLoopForEmptyNakedCommentBeforeSemiSliceViolation {
	void m() {
		for (;;) /* spin */;
	}
}
// === end ===

// === case: for_true_braced ===
class InputInfiniteEmptyLoopForTrueBracedSliceViolation {
	void m() {
		for (;true;) {
		}
	}
}
// === end ===

// === case: for_true_condition_comment ===
class InputInfiniteEmptyLoopForTrueConditionCommentSliceViolation {
	void m() {
		for (; /* c */ true;) {}
	}
}
// === end ===

// === case: for_true_naked ===
class InputInfiniteEmptyLoopForTrueNakedSliceViolation {
	void m() {
		for (;true;);
	}
}
// === end ===

// === case: while_true_braced ===
class InputInfiniteEmptyLoopWhileTrueBracedSliceViolation {
	void m() {
		while (true) {
		}
	}
}
// === end ===

// === case: while_true_braced_block_comment ===
class InputInfiniteEmptyLoopWhileTrueBracedBlockCommentSliceViolation {
	void m() {
		while (true) { /* spin */ }
	}
}
// === end ===

// === case: while_true_braced_line_comment ===
class InputInfiniteEmptyLoopWhileTrueBracedLineCommentSliceViolation {
	void m() {
		while (true) {
			// spin
		}
	}
}
// === end ===

// === case: while_true_condition_comment ===
class InputInfiniteEmptyLoopWhileTrueConditionCommentSliceViolation {
	void m() {
		while (/* c */ true) {}
	}
}
// === end ===

// === case: while_true_condition_trailing_comment ===
class InputInfiniteEmptyLoopWhileTrueConditionTrailingCommentSliceViolation {
	void m() {
		while (true /* c */) {}
	}
}
// === end ===

// === case: while_true_naked ===
class InputInfiniteEmptyLoopWhileTrueNakedSliceViolation {
	void m() {
		while (true);
	}
}
// === end ===

// === case: while_true_naked_comment_before_semi ===
class InputInfiniteEmptyLoopWhileTrueNakedCommentBeforeSemiSliceViolation {
	void m() {
		while (true) /* spin */;
	}
}
// === end ===

// === case: while_true_naked_with_comment ===
class InputInfiniteEmptyLoopWhileTrueNakedWithCommentSliceViolation {
	void m() {
		while (true); // spin on purpose
	}
}
// === end ===