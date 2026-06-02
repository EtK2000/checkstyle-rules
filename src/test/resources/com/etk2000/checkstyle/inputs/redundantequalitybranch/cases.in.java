package com.etk2000.checkstyle.inputs.redundantequalitybranch;

// === case: array_target_assign ===
class InputRedundantEqualityArrayTargetAssignSliceViolation {
	void m(int[] arr, int i, int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			arr[i] = a;
		else
			arr[i] = b;
	}
}
// === end ===

// === case: assign_bare_collapse ===
class InputRedundantEqualityAssignBareCollapseSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: assign_bare_collapse_equal_swapped ===
class InputRedundantEqualityAssignBareCollapseEqualSwappedSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			r = b;
		else
			r = a;
	}
}
// === end ===

// === case: assign_bare_collapse_if_on_first_line_with_trailing_return ===
class InputRedundantEqualityAssignBareCollapseIfOnFirstLineWithTrailingReturnSliceViolation {
	int r;

	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: assign_bare_collapse_not_equal ===
class InputRedundantEqualityAssignBareCollapseNotEqualSliceViolation {
	int r;

	void m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: assign_bare_collapse_not_equal_swapped ===
class InputRedundantEqualityAssignBareCollapseNotEqualSwappedSliceViolation {
	int r;

	void m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'b' directly.
			r = b;
		else
			r = a;
	}
}
// === end ===

// === case: assign_decl_line_does_not_match_decl ===
class InputRedundantEqualityAssignDeclLineDoesNotMatchDeclSliceViolation {
	int r;

	int m(int a, int b, int c) {
		final var notDecl = a + c;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return r + notDecl;
	}
}
// === end ===

// === case: assign_decl_target_mismatch ===
class InputRedundantEqualityAssignDeclTargetMismatchSliceViolation {
	int r;

	int m(int a, int b) {
		final int s;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		s = a + b;
		return r + s;
	}
}
// === end ===

// === case: assign_decl_without_trailing_return ===
class InputRedundantEqualityAssignDeclWithoutTrailingReturnSliceViolation {
	void m(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		System.out.println(r);
	}
}
// === end ===

// === case: assign_different_targets_trailing_return ===
class InputRedundantEqualityAssignDifferentTargetsTrailingReturnSliceViolation {
	int m(int a, int b, int s) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return s;
	}
}
// === end ===

// === case: assign_trailing_line_not_return_var ===
class InputRedundantEqualityAssignTrailingLineNotReturnVarSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return r + 1;
	}
}
// === end ===

// === case: block_comment_spans_lines_skip ===
// skip-reason: cannot collapse: a comment in the if-else would be lost
class InputRedundantEqualityBlockCommentSpansLinesSkipSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		else
			return b; /* dangling
		comment */
	}
}
// === end ===

// === case: braced_both_branches_assign ===
class InputRedundantEqualityBracedBothBranchesAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) { // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		}
		else {
			r = b;
		}
	}
}
// === end ===

// === case: braced_both_branches_return ===
class InputRedundantEqualityBracedBothBranchesReturnSliceViolation {
	int m(int a, int b) {
		if (a == b) { // violation: Redundant equality if-else, use 'b' directly.
			return a;
		}
		else {
			return b;
		}
	}
}
// === end ===

// === case: braced_decl_collapse ===
class InputRedundantEqualityBracedDeclCollapseSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) { // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		}
		else {
			r = b;
		}
		return r;
	}
}
// === end ===

// === case: braced_else_only_assign ===
class InputRedundantEqualityBracedElseOnlyAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else {
			r = b;
		}
	}
}
// === end ===

// === case: braced_then_only_assign ===
class InputRedundantEqualityBracedThenOnlyAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) { // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		}
		else
			r = b;
	}
}
// === end ===

// === case: comment_inside_body_skip ===
// skip-reason: cannot collapse: a comment in the if-else would be lost
class InputRedundantEqualityCommentInsideBodySkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			// note
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_else_assign_equal ===
class InputRedundantEqualityAssignEqualSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: if_else_assign_equal_swapped ===
class InputRedundantEqualityAssignEqualSwappedSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			r = b;
		else
			r = a;
		return r;
	}
}
// === end ===

// === case: if_else_assign_not_equal ===
class InputRedundantEqualityAssignNotEqualSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: if_else_assign_not_equal_swapped ===
class InputRedundantEqualityAssignNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a != b) // violation: Redundant equality if-else, use 'b' directly.
			r = b;
		else
			r = a;
		return r;
	}
}
// === end ===

// === case: if_else_return_equal ===
class InputRedundantEqualityReturnEqualSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_else_return_equal_swapped ===
class InputRedundantEqualityReturnEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			return b;
		else
			return a;
	}
}
// === end ===

// === case: if_else_return_not_equal ===
class InputRedundantEqualityReturnNotEqualSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_else_return_not_equal_swapped ===
class InputRedundantEqualityReturnNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'b' directly.
			return b;
		else
			return a;
	}
}
// === end ===

// === case: if_line_block_comment_assign ===
class InputRedundantEqualityIfLineBlockCommentAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) /* note */ // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_line_code_after_comment_skip ===
// skip-reason: cannot collapse: a comment in the if-else would be lost
class InputRedundantEqualityIfLineCodeAfterCommentSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) /* note */ r = a; // violation: Redundant equality if-else, use 'b' directly.
		else
			r = b;
	}
}
// === end ===

// === case: if_line_line_comment_assign ===
class InputRedundantEqualityIfLineLineCommentAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: multiline_condition_return ===
class InputRedundantEqualityMultilineConditionReturnSliceViolation {
	int m(int a, int b) {
		if (a // violation: Redundant equality if-else, use 'b' directly.
				== b)
			return a;
		else
			return b;
	}
}
// === end ===

// === case: multiple_comments_span_skip ===
// skip-reason: cannot collapse: a comment in the if-else would be lost
class InputRedundantEqualityMultipleCommentsSpanSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b; // trailing
	}
}
// === end ===

// === case: this_target_assign ===
class InputRedundantEqualityThisTargetAssignSliceViolation {
	int x;

	void m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			this.x = a;
		else
			this.x = b;
	}
}
// === end ===

// === case: trailing_return_equal ===
class InputRedundantEqualityTrailingEqualSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		return b;
	}
}
// === end ===

// === case: trailing_return_equal_swapped ===
class InputRedundantEqualityTrailingEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			return b;
		return a;
	}
}
// === end ===

// === case: trailing_return_equal_with_extra_line ===
class InputRedundantEqualityTrailingEqualWithExtraLineSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		return b;
		// extra line
	}
}
// === end ===

// === case: trailing_return_not_equal ===
class InputRedundantEqualityTrailingNotEqualSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			return a;
		return b;
	}
}
// === end ===

// === case: trailing_return_not_equal_swapped ===
class InputRedundantEqualityTrailingNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'b' directly.
			return b;
		return a;
	}
}
// === end ===