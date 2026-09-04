package com.etk2000.checkstyle.inputs.prefermathmethod;

// === case: if_init_overwrite_merge_into_decl ===
class InputPreferMathMethodIfInitOverwriteMergeIntoDeclSliceViolation {
	void m(int a, int b) {
		final var r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_max_array_target_split_by_comment_refuses ===
// skip-reason: cannot rewrite the if-else: this shape is not supported
class InputPreferMathMethodIfMaxArrayTargetSplitByCommentRefusesSliceViolation {
	void m(int a, int b, int[] r, int i) {
		if (a > b) {
			r[/* idx */
					i] = a;
		}
		else
			r[i] = b;
	}
}
// === end ===

// === case: if_max_bare_return_below_no_collapse ===
class InputPreferMathMethodIfMaxBareReturnBelowNoCollapseSliceViolation {
	void m(int a, int b) {
		final int r;
		r = Math.max(a, b);
		return;
	}
}
// === end ===

// === case: if_max_decl_multi_declarator_refuses ===
// skip-reason: cannot rewrite the if-else: this shape is not supported
class InputPreferMathMethodIfMaxDeclMultiDeclaratorRefusesSliceViolation {
	long m(int b) {
		final int s, r;
		if (s > b)
			r = s;
		else
			r = b;
		return r;
	}
}
// === end ===

// === case: if_max_decl_return_expression_falls_back_to_bare ===
class InputPreferMathMethodIfMaxDeclReturnExpressionFallsBackToBareSliceViolation {
	int m(int a, int b) {
		final int r;
		r = Math.max(a, b);
		return r + 1;
	}
}
// === end ===

// === case: if_max_else_if_cuddled_brace ===
class InputPreferMathMethodIfMaxElseIfCuddledBraceSliceViolation {
	int m(boolean cond, int a, int b) {
		if (cond)
			return 0;
		else
			return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_init_overwrite_multi_decl_refuses ===
// skip-reason: cannot rewrite the if-else: this shape is not supported
class InputPreferMathMethodIfMaxInitOverwriteMultiDeclRefusesSliceViolation {
	long m(int b) {
		final long s, r = b;
		if (s > b)
			r = s;
		return r;
	}
}
// === end ===

// === case: if_plain_assign_decl_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int r) {
		final int s;
		r = Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_plain_assign_decl_without_trailing_return_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclWithoutTrailingReturnFallsBackToBareSliceViolation {
	void m(int a, int b) {
		final int r;
		r = Math.max(a, b);
	}
}
// === end ===

// === case: if_plain_assign_return_clause_false_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnClauseFalseFallsBackToBareSliceViolation {
	void m(int a, int b) {
		final int r;
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_return_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int s) {
		final int r;
		r = Math.max(a, b);
		return s;
	}
}
// === end ===

// === case: if_zero_indent_comment_ternary ===
class InputPreferMathMethodIfZeroIndentCommentTernarySliceViolation {
	int m(int a, int b, int x, int y) {
		final int r;
r = Math.max(a, b); // keep in sync with x > y ? x : y below
		return r + x + y;
	}
}
// === end ===