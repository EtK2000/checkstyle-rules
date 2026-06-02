package com.etk2000.checkstyle.inputs.redundantequalitybranch;

// === case: if_line_code_after_comment_skip ===
// skip-reason: cannot collapse: a comment in the if-else would be lost
class InputRedundantEqualityIfLineCodeAfterCommentSkipSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; /* note */
	}
}
// === end ===