package com.etk2000.checkstyle.inputs.redundantequalitybranch;

// === case: cuddled_else_if_braced ===
class InputRedundantEqualityCuddledElseIfBracedSliceViolation {
	int r;

	void m(int a, int b, boolean c) {
		if (c)
			r = 0;
		else
			r = b;
	}
}
// === end ===

// === case: decl_collapse_comment_on_if_line ===
class InputRedundantEqualityDeclCollapseCommentOnIfLineSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b)
			r = a;
		else
			r = b;
		return r; // note
	}
}
// === end ===

// === case: if_after_do ===
class InputRedundantEqualityIfAfterDoSliceViolation {
	int r;

	void m(int a, int b) {
		do r = b;
		while (r < 0);
	}
}
// === end ===

// === case: if_after_open_brace ===
class InputRedundantEqualityIfAfterOpenBraceSliceViolation {
	int r;

	void m(int a, int b) {
		r = b;
	}
}
// === end ===

// === case: if_line_code_after_comment_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityIfLineCodeAfterCommentSkipSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; /* note */
	}
}
// === end ===

// === case: single_line_tail_code_and_comment ===
class InputRedundantEqualitySingleLineTailCodeAndCommentSliceViolation {
	int count, r;

	void m(int a, int b) {
		r = b;
		++count; // note
	}
}
// === end ===

// === case: supplementary_char_ahead_of_collapse ===
class InputRedundantEqualitySupplementaryCharAheadOfCollapseSliceViolation {
	int count, r;
	String s;

	void m(int a, int b) {
		s = "𝑥";
		r = b;
		++count;
	}
}
// === end ===

// === case: text_block_closer_lead ===
class InputRedundantEqualityTextBlockCloserLeadSliceViolation {
	int r;
	String s;

	void m(int a, int b) {
		s = """
			x
			"""; if (a == b)
				r = a; // keep me
		else
			r = b;
	}
}
// === end ===