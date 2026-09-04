package com.etk2000.checkstyle.inputs.redundantequalitybranch;

// === case: array_target_assign ===
class InputRedundantEqualityArrayTargetAssignSliceViolation {
	void m(int[] arr, int i, int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'a' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a != b) // violation: Use 'a' directly instead of the redundant equality if-else.
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
		if (a != b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r + 1;
	}
}
// === end ===

// === case: block_comment_before_if_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityBlockCommentBeforeIfSkipSliceViolation {
	int r;

	void m(int a, int b) {
		/* note */ if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: block_comment_spans_lines_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityBlockCommentSpansLinesSkipSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) { // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) { // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) { // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) { // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		}
		else
			r = b;
	}
}
// === end ===

// === case: comment_inside_body_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityCommentInsideBodySkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			// note
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: cuddled_else_if_braced ===
class InputRedundantEqualityCuddledElseIfBracedSliceViolation {
	int r;

	void m(int a, int b, boolean c) {
		if (c) {
			r = 0;
		} else if (a == b) { // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		} else {
			r = b;
		}
	}
}
// === end ===

// === case: decl_annotated_bare_collapses ===
class InputRedundantEqualityDeclAnnotatedSliceViolation {
	int m(int a, int b) {
		@SuppressWarnings("unused")
		final int r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: decl_beside_another_declarator_bare_collapses ===
class InputRedundantEqualityDeclBesideAnotherDeclaratorSliceViolation {
	int compute() {
		return 1;
	}

	int m(int a, int b) {
		final int s = compute(), r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: decl_beside_another_declarator_not_equal_bare_collapses ===
class InputRedundantEqualityDeclBesideAnotherDeclaratorNotEqualSliceViolation {
	int compute() {
		return 1;
	}

	int m(int b) {
		final int s = compute(), r;
		if (s != b) // violation: Use 's' directly instead of the redundant equality if-else.
			r = s;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: decl_collapse_after_open_brace ===
class InputRedundantEqualityDeclCollapseAfterOpenBraceSliceViolation {
	int m(int a, int b) { final int r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: decl_collapse_comment_on_if_line ===
class InputRedundantEqualityDeclCollapseCommentOnIfLineSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) r = a; else r = b; return r; // note // violation: Use 'b' directly instead of the redundant equality if-else.
	}
}
// === end ===

// === case: decl_collapse_tail_brace ===
class InputRedundantEqualityDeclCollapseTailBraceSliceViolation {
	int m(int a, int b) {
		final int r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r; }
}
// === end ===

// === case: decl_first_of_two_declarators_bare_collapses ===
class InputRedundantEqualityDeclFirstOfTwoDeclaratorsSliceViolation {
	int m(int a, int b) {
		final int r, s;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			s = a;
		else
			s = b;
		return s;
	}
}
// === end ===

// === case: decl_in_arrow_case_collapses ===
class InputRedundantEqualityDeclInArrowCaseSliceViolation {
	int m(int x, int a, int b) {
		switch (x) {
			case 1 -> {
				final int r;
				if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
					r = a;
				else
					r = b;
				return r;
			}
			default -> {
				return 0;
			}
		}
	}
}
// === end ===

// === case: decl_in_lambda_bare_collapses ===
// imports: java.util.function.IntBinaryOperator
class InputRedundantEqualityDeclInLambdaSliceViolation {
	final IntBinaryOperator pick = (a, b) -> {
		final int r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r;
	};
}
// === end ===

// === case: decl_type_differs_from_return_type_bare_collapses ===
class InputRedundantEqualityDeclTypeDiffersFromReturnTypeSliceViolation {
	Object m(int a, int b) {
		final double r;
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: else_if_assign_equal ===
class InputRedundantEqualityElseIfAssignEqualSliceViolation {
	int r;

	void m(int a, int b, boolean c) {
		if (c)
			r = 0;
		else if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: else_if_return_equal ===
class InputRedundantEqualityElseIfReturnEqualSliceViolation {
	int m(int a, int b, boolean c) {
		if (c)
			return 0;
		else if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_after_do ===
class InputRedundantEqualityIfAfterDoSliceViolation {
	int r;

	void m(int a, int b) {
		do if (a == b) r = a; else r = b; while (r < 0); // violation: Use 'b' directly instead of the redundant equality if-else.
	}
}
// === end ===

// === case: if_after_label ===
class InputRedundantEqualityIfAfterLabelSliceViolation {
	int r;

	void m(int a, int b) {
		out: if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_after_open_brace ===
class InputRedundantEqualityIfAfterOpenBraceSliceViolation {
	int r;

	void m(int a, int b) { if (a == b) r = a; else r = b; } // violation: Use 'b' directly instead of the redundant equality if-else.
}
// === end ===

// === case: if_after_outer_if_header ===
class InputRedundantEqualityIfAfterOuterIfHeaderSliceViolation {
	int r;

	void m(int a, int b, boolean c) {
		if (c) if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_after_statement ===
class InputRedundantEqualityIfAfterStatementSliceViolation {
	int r;

	void m(int a, int b) {
		r = 1; if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'a' directly instead of the redundant equality if-else.
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
		if (a != b) // violation: Use 'a' directly instead of the redundant equality if-else.
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
		if (a != b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_else_return_equal_swapped ===
class InputRedundantEqualityReturnEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'a' directly instead of the redundant equality if-else.
			return b;
		else
			return a;
	}
}
// === end ===

// === case: if_else_return_not_equal ===
class InputRedundantEqualityReturnNotEqualSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Use 'a' directly instead of the redundant equality if-else.
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_else_return_not_equal_swapped ===
class InputRedundantEqualityReturnNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Use 'b' directly instead of the redundant equality if-else.
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
		if (a == b) /* note */ // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_line_code_after_comment_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityIfLineCodeAfterCommentSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) /* note */ r = a; // violation: Use 'b' directly instead of the redundant equality if-else.
		else
			r = b;
	}
}
// === end ===

// === case: if_line_line_comment_assign ===
class InputRedundantEqualityIfLineLineCommentAssignSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b;
	}
}
// === end ===

// === case: multiline_condition_return ===
class InputRedundantEqualityMultilineConditionReturnSliceViolation {
	int m(int a, int b) {
		if (a // violation: Use 'b' directly instead of the redundant equality if-else.
				== b)
			return a;
		else
			return b;
	}
}
// === end ===

// === case: multiple_comments_span_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityMultipleCommentsSpanSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b; // trailing
	}
}
// === end ===

// === case: single_line_tail_code_and_comment ===
class InputRedundantEqualitySingleLineTailCodeAndCommentSliceViolation {
	int count, r;

	void m(int a, int b) {
		if (a == b) r = a; else r = b; ++count; // note // violation: Use 'b' directly instead of the redundant equality if-else.
	}
}
// === end ===

// === case: supplementary_char_ahead_of_collapse ===
class InputRedundantEqualitySupplementaryCharAheadOfCollapseSliceViolation {
	int count, r;
	String s;

	void m(int a, int b) {
		s = "𝑥"; if (a == b) r = a; else r = b; ++count; // violation: Use 'b' directly instead of the redundant equality if-else.
	}
}
// === end ===

// === case: tail_brace_after_else ===
class InputRedundantEqualityTailBraceAfterElseSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b; }
}
// === end ===

// === case: tail_brace_with_if_line_comment ===
class InputRedundantEqualityTailBraceWithIfLineCommentSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note // violation: Use 'b' directly instead of the redundant equality if-else.
			r = a;
		else
			r = b; }
}
// === end ===

// === case: text_block_closer_lead ===
class InputRedundantEqualityTextBlockCloserLeadSliceViolation {
	int r;
	String s;

	void m(int a, int b) {
		s = """
			x
			"""; if (a == b) r = a; // keep me // violation: Use 'b' directly instead of the redundant equality if-else.
		else r = b;
	}
}
// === end ===

// === case: this_target_assign ===
class InputRedundantEqualityThisTargetAssignSliceViolation {
	int x;

	void m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			this.x = a;
		else
			this.x = b;
	}
}
// === end ===

// === case: trailing_return_equal ===
class InputRedundantEqualityTrailingEqualSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return a;
		return b;
	}
}
// === end ===

// === case: trailing_return_equal_swapped ===
class InputRedundantEqualityTrailingEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'a' directly instead of the redundant equality if-else.
			return b;
		return a;
	}
}
// === end ===

// === case: trailing_return_equal_with_extra_line ===
class InputRedundantEqualityTrailingEqualWithExtraLineSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return a;
		return b;
		// extra line
	}
}
// === end ===

// === case: trailing_return_not_equal ===
class InputRedundantEqualityTrailingNotEqualSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Use 'a' directly instead of the redundant equality if-else.
			return a;
		return b;
	}
}
// === end ===

// === case: trailing_return_not_equal_swapped ===
class InputRedundantEqualityTrailingNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		if (a != b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return b;
		return a;
	}
}
// === end ===

// === case: trailing_return_tail_brace ===
class InputRedundantEqualityTrailingReturnTailBraceSliceViolation {
	int m(int a, int b) {
		if (a == b) // violation: Use 'b' directly instead of the redundant equality if-else.
			return a;
		return b; }
}
// === end ===