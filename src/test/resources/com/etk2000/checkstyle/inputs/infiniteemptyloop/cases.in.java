package com.etk2000.checkstyle.inputs.infiniteemptyloop;

// === case: do_while_true_braced ===
class InputInfiniteEmptyLoopDoWhileTrueBracedSliceViolation {
	void m() {
		do { // violation: Infinite do-while loop must not have an empty body (it will hang).
		} while (true);
	}
}
// === end ===

// Pins for the permanent comment-node opt-out: each of these goes silent if the check ever opts
// in, so they are the regression net for a guaranteed hang. The bare forms above are NOT pins,
// they fire either way.
// === case: do_while_true_braced_block_comment ===
class InputInfiniteEmptyLoopDoWhileTrueBracedBlockCommentSliceViolation {
	void m() {
		do { /* spin */ } while (true); // violation: Infinite do-while loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: do_while_true_braced_line_comment ===
class InputInfiniteEmptyLoopDoWhileTrueBracedLineCommentSliceViolation {
	void m() {
		do { // violation: Infinite do-while loop must not have an empty body (it will hang).
			// spin
		} while (true);
	}
}
// === end ===

// === case: do_while_true_condition_comment ===
class InputInfiniteEmptyLoopDoWhileTrueConditionCommentSliceViolation {
	void m() {
		do {} while (/* c */ true); // violation: Infinite do-while loop must not have an empty body (it will hang).
	}
}
// === end ===

// Unlike while and for, do-while reaches its EXPR backwards from the `)`, so a trailing comment
// is a pin here too.
// === case: do_while_true_condition_trailing_comment ===
class InputInfiniteEmptyLoopDoWhileTrueConditionTrailingCommentSliceViolation {
	void m() {
		do {} while (true /* c */); // violation: Infinite do-while loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: do_while_true_naked ===
class InputInfiniteEmptyLoopDoWhileTrueNakedSliceViolation {
	void m() {
		do; // violation: Infinite do-while loop must not have an empty body (it will hang).
		while (true);
	}
}
// === end ===

// === case: do_while_true_naked_comment_before_semi ===
class InputInfiniteEmptyLoopDoWhileTrueNakedCommentBeforeSemiSliceViolation {
	void m() {
		do /* spin */; // violation: Infinite do-while loop must not have an empty body (it will hang).
		while (true);
	}
}
// === end ===

// === case: for_empty_braced ===
class InputInfiniteEmptyLoopForEmptyBracedSliceViolation {
	void m() {
		for (;;) { // violation: Infinite for loop must not have an empty body (it will hang).
		}
	}
}
// === end ===

// === case: for_empty_braced_block_comment ===
class InputInfiniteEmptyLoopForEmptyBracedBlockCommentSliceViolation {
	void m() {
		for (;;) { /* spin */ } // violation: Infinite for loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: for_empty_braced_line_comment ===
class InputInfiniteEmptyLoopForEmptyBracedLineCommentSliceViolation {
	void m() {
		for (;;) { // violation: Infinite for loop must not have an empty body (it will hang).
			// spin
		}
	}
}
// === end ===

// === case: for_empty_naked ===
class InputInfiniteEmptyLoopForEmptyNakedSliceViolation {
	void m() {
		for (;;); // violation: Infinite for loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: for_empty_naked_comment_before_semi ===
class InputInfiniteEmptyLoopForEmptyNakedCommentBeforeSemiSliceViolation {
	void m() {
		for (;;) /* spin */; // violation: Infinite for loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: for_true_braced ===
class InputInfiniteEmptyLoopForTrueBracedSliceViolation {
	void m() {
		for (;true;) { // violation: Infinite for loop must not have an empty body (it will hang).
		}
	}
}
// === end ===

// === case: for_true_condition_comment ===
class InputInfiniteEmptyLoopForTrueConditionCommentSliceViolation {
	void m() {
		for (; /* c */ true;) {} // violation: Infinite for loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: for_true_naked ===
class InputInfiniteEmptyLoopForTrueNakedSliceViolation {
	void m() {
		for (;true;); // violation: Infinite for loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: while_true_braced ===
class InputInfiniteEmptyLoopWhileTrueBracedSliceViolation {
	void m() {
		while (true) { // violation: Infinite while loop must not have an empty body (it will hang).
		}
	}
}
// === end ===

// === case: while_true_braced_block_comment ===
class InputInfiniteEmptyLoopWhileTrueBracedBlockCommentSliceViolation {
	void m() {
		while (true) { /* spin */ } // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: while_true_braced_line_comment ===
class InputInfiniteEmptyLoopWhileTrueBracedLineCommentSliceViolation {
	void m() {
		while (true) { // violation: Infinite while loop must not have an empty body (it will hang).
			// spin
		}
	}
}
// === end ===

// === case: while_true_condition_comment ===
class InputInfiniteEmptyLoopWhileTrueConditionCommentSliceViolation {
	void m() {
		while (/* c */ true) {} // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===

// For while and for, a comment after the condition leaves EXPR's first child alone, so this one
// fires either way. It is the boundary partner that makes the do-while trailing pin meaningful.
// === case: while_true_condition_trailing_comment ===
class InputInfiniteEmptyLoopWhileTrueConditionTrailingCommentSliceViolation {
	void m() {
		while (true /* c */) {} // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: while_true_naked ===
class InputInfiniteEmptyLoopWhileTrueNakedSliceViolation {
	void m() {
		while (true); // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===

// === case: while_true_naked_comment_before_semi ===
class InputInfiniteEmptyLoopWhileTrueNakedCommentBeforeSemiSliceViolation {
	void m() {
		while (true) /* spin */; // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===

// A comment on the semicolon excuses an empty clause body for EmptyBodyCheck, but a
// guaranteed hang is a bug whatever the comment says, so this check ignores comments.
// The comment here trails the `;` and so attaches past the loop entirely: it fires either way,
// which is what distinguishes it from the pin above.
// === case: while_true_naked_with_comment ===
class InputInfiniteEmptyLoopWhileTrueNakedWithCommentSliceViolation {
	void m() {
		while (true); // spin on purpose // violation: Infinite while loop must not have an empty body (it will hang).
	}
}
// === end ===