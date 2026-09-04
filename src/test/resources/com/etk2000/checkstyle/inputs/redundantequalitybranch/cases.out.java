package com.etk2000.checkstyle.inputs.redundantequalitybranch;

// === case: array_target_assign ===
class InputRedundantEqualityArrayTargetAssignSliceViolation {
	void m(int[] arr, int i, int a, int b) {
		arr[i] = b;
	}
}
// === end ===

// === case: assign_bare_collapse ===
class InputRedundantEqualityAssignBareCollapseSliceViolation {
	int r;

	void m(int a, int b) {
		r = b;
	}
}
// === end ===

// === case: assign_bare_collapse_equal_swapped ===
class InputRedundantEqualityAssignBareCollapseEqualSwappedSliceViolation {
	int r;

	void m(int a, int b) {
		r = a;
	}
}
// === end ===

// === case: assign_bare_collapse_if_on_first_line_with_trailing_return ===
class InputRedundantEqualityAssignBareCollapseIfOnFirstLineWithTrailingReturnSliceViolation {
	int r;

	int m(int a, int b) {
		r = b;
		return r;
	}
}
// === end ===

// === case: assign_bare_collapse_not_equal ===
class InputRedundantEqualityAssignBareCollapseNotEqualSliceViolation {
	int r;

	void m(int a, int b) {
		r = a;
	}
}
// === end ===

// === case: assign_bare_collapse_not_equal_swapped ===
class InputRedundantEqualityAssignBareCollapseNotEqualSwappedSliceViolation {
	int r;

	void m(int a, int b) {
		r = b;
	}
}
// === end ===

// === case: assign_decl_line_does_not_match_decl ===
class InputRedundantEqualityAssignDeclLineDoesNotMatchDeclSliceViolation {
	int r;

	int m(int a, int b, int c) {
		final var notDecl = a + c;
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
		r = b;
		System.out.println(r);
	}
}
// === end ===

// === case: assign_different_targets_trailing_return ===
class InputRedundantEqualityAssignDifferentTargetsTrailingReturnSliceViolation {
	int m(int a, int b, int s) {
		final int r;
		r = b;
		return s;
	}
}
// === end ===

// === case: assign_trailing_line_not_return_var ===
class InputRedundantEqualityAssignTrailingLineNotReturnVarSliceViolation {
	int m(int a, int b) {
		final int r;
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
		/* note */ if (a == b)
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
		if (a == b)
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
		r = b;
	}
}
// === end ===

// === case: braced_both_branches_return ===
class InputRedundantEqualityBracedBothBranchesReturnSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: braced_decl_collapse ===
class InputRedundantEqualityBracedDeclCollapseSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: braced_else_only_assign ===
class InputRedundantEqualityBracedElseOnlyAssignSliceViolation {
	int r;

	void m(int a, int b) {
		r = b;
	}
}
// === end ===

// === case: braced_then_only_assign ===
class InputRedundantEqualityBracedThenOnlyAssignSliceViolation {
	int r;

	void m(int a, int b) {
		r = b;
	}
}
// === end ===

// === case: comment_inside_body_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityCommentInsideBodySkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b)
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
		} else
			r = b;
	}
}
// === end ===

// === case: decl_annotated_bare_collapses ===
class InputRedundantEqualityDeclAnnotatedSliceViolation {
	int m(int a, int b) {
		@SuppressWarnings("unused")
		final int r;
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
		r = s;
		return r;
	}
}
// === end ===

// === case: decl_collapse_after_open_brace ===
class InputRedundantEqualityDeclCollapseAfterOpenBraceSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: decl_collapse_comment_on_if_line ===
class InputRedundantEqualityDeclCollapseCommentOnIfLineSliceViolation {
	int m(int a, int b) {
		return b; // note
	}
}
// === end ===

// === case: decl_collapse_tail_brace ===
class InputRedundantEqualityDeclCollapseTailBraceSliceViolation {
	int m(int a, int b) {
		return b; }
}
// === end ===

// === case: decl_first_of_two_declarators_bare_collapses ===
class InputRedundantEqualityDeclFirstOfTwoDeclaratorsSliceViolation {
	int m(int a, int b) {
		final int r, s;
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
				return b;
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
		r = b;
		return r;
	};
}
// === end ===

// === case: decl_type_differs_from_return_type_bare_collapses ===
class InputRedundantEqualityDeclTypeDiffersFromReturnTypeSliceViolation {
	Object m(int a, int b) {
		final double r;
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
		else
			return b;
	}
}
// === end ===

// === case: if_after_do ===
class InputRedundantEqualityIfAfterDoSliceViolation {
	int r;

	void m(int a, int b) {
		do
			r = b; while (r < 0);
	}
}
// === end ===

// === case: if_after_label ===
class InputRedundantEqualityIfAfterLabelSliceViolation {
	int r;

	void m(int a, int b) {
		out:
			r = b;
	}
}
// === end ===

// === case: if_after_open_brace ===
class InputRedundantEqualityIfAfterOpenBraceSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; }
}
// === end ===

// === case: if_after_outer_if_header ===
class InputRedundantEqualityIfAfterOuterIfHeaderSliceViolation {
	int r;

	void m(int a, int b, boolean c) {
		if (c)
			r = b;
	}
}
// === end ===

// === case: if_after_statement ===
class InputRedundantEqualityIfAfterStatementSliceViolation {
	int r;

	void m(int a, int b) {
		r = 1;
		r = b;
	}
}
// === end ===

// === case: if_else_assign_equal ===
class InputRedundantEqualityAssignEqualSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: if_else_assign_equal_swapped ===
class InputRedundantEqualityAssignEqualSwappedSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: if_else_assign_not_equal ===
class InputRedundantEqualityAssignNotEqualSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: if_else_assign_not_equal_swapped ===
class InputRedundantEqualityAssignNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: if_else_return_equal ===
class InputRedundantEqualityReturnEqualSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: if_else_return_equal_swapped ===
class InputRedundantEqualityReturnEqualSwappedSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: if_else_return_not_equal ===
class InputRedundantEqualityReturnNotEqualSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: if_else_return_not_equal_swapped ===
class InputRedundantEqualityReturnNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: if_line_block_comment_assign ===
class InputRedundantEqualityIfLineBlockCommentAssignSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; /* note */
	}
}
// === end ===

// === case: if_line_code_after_comment_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityIfLineCodeAfterCommentSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) /* note */ r = a;
		else
			r = b;
	}
}
// === end ===

// === case: if_line_line_comment_assign ===
class InputRedundantEqualityIfLineLineCommentAssignSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; // note
	}
}
// === end ===

// === case: multiline_condition_return ===
class InputRedundantEqualityMultilineConditionReturnSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: multiple_comments_span_skip ===
// skip-reason: cannot collapse the if-else: a comment in it would be lost
class InputRedundantEqualityMultipleCommentsSpanSkipSliceViolation {
	int r;

	void m(int a, int b) {
		if (a == b) // note
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
		r = b; ++count; // note
	}
}
// === end ===

// === case: supplementary_char_ahead_of_collapse ===
class InputRedundantEqualitySupplementaryCharAheadOfCollapseSliceViolation {
	int count, r;
	String s;

	void m(int a, int b) {
		s = "𝑥";
		r = b; ++count;
	}
}
// === end ===

// === case: tail_brace_after_else ===
class InputRedundantEqualityTailBraceAfterElseSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; }
}
// === end ===

// === case: tail_brace_with_if_line_comment ===
class InputRedundantEqualityTailBraceWithIfLineCommentSliceViolation {
	int r;

	void m(int a, int b) {
		r = b; } // note
}
// === end ===

// === case: text_block_closer_lead ===
class InputRedundantEqualityTextBlockCloserLeadSliceViolation {
	int r;
	String s;

	void m(int a, int b) {
		s = """
			x
			""";
		r = b; // keep me
	}
}
// === end ===

// === case: this_target_assign ===
class InputRedundantEqualityThisTargetAssignSliceViolation {
	int x;

	void m(int a, int b) {
		this.x = b;
	}
}
// === end ===

// === case: trailing_return_equal ===
class InputRedundantEqualityTrailingEqualSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: trailing_return_equal_swapped ===
class InputRedundantEqualityTrailingEqualSwappedSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: trailing_return_equal_with_extra_line ===
class InputRedundantEqualityTrailingEqualWithExtraLineSliceViolation {
	int m(int a, int b) {
		return b;
		// extra line
	}
}
// === end ===

// === case: trailing_return_not_equal ===
class InputRedundantEqualityTrailingNotEqualSliceViolation {
	int m(int a, int b) {
		return a;
	}
}
// === end ===

// === case: trailing_return_not_equal_swapped ===
class InputRedundantEqualityTrailingNotEqualSwappedSliceViolation {
	int m(int a, int b) {
		return b;
	}
}
// === end ===

// === case: trailing_return_tail_brace ===
class InputRedundantEqualityTrailingReturnTailBraceSliceViolation {
	int m(int a, int b) {
		return b; }
}
// === end ===