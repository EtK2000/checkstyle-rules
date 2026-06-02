package com.etk2000.checkstyle.inputs.prefermathmethod;

// === case: abs_ge ===
class InputPreferMathMethodTernaryAbsGeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_ge_zero_left ===
class InputPreferMathMethodTernaryAbsGeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_gt ===
class InputPreferMathMethodTernaryAbsGtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_gt_zero_left ===
class InputPreferMathMethodTernaryAbsGtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_le ===
class InputPreferMathMethodTernaryAbsLeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_le_zero_left ===
class InputPreferMathMethodTernaryAbsLeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_lt ===
class InputPreferMathMethodTernaryAbsLtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_lt_zero_left ===
class InputPreferMathMethodTernaryAbsLtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: abs_with_array_access ===
class InputPreferMathMethodTernaryAbsWithArrayAccessSliceViolation {
	int m(int[] arr) {
		return Math.abs(arr[0]);
	}
}
// === end ===

// === case: abs_with_field_access ===
class InputPreferMathMethodTernaryAbsWithFieldAccessSliceViolation {
	int x;

	int m(InputPreferMathMethodTernaryAbsWithFieldAccessSliceViolation a) {
		return Math.abs(a.x);
	}
}
// === end ===

// === case: abs_with_long_param ===
class InputPreferMathMethodTernaryAbsWithLongParamSliceViolation {
	long m(long a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: clamp_cast_arg ===
class InputPreferMathMethodClampCastArgSliceViolation {
	int m(int value, long lo, int hi) {
		return Math.max((int) lo, Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_comment_paren_in_arg ===
class InputPreferMathMethodClampCommentParenInArgSliceViolation {
	int m(int lo, int value, int hi) {
		return Math.max(lo /* ) */, Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_literal_paren_in_arg ===
class InputPreferMathMethodClampLiteralParenInArgSliceViolation {
	int m(String s, int value, int hi) {
		return Math.max(s.indexOf(')'), Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_max_min ===
class InputPreferMathMethodClampMaxMinSliceViolation {
	int m(int value, int lo, int hi) {
		return Math.max(lo, Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_max_min_nested_inner_arg ===
class InputPreferMathMethodClampMaxMinNestedInnerArgSliceViolation {
	int m(int lo, int hi, int a, int b) {
		return Math.max(lo, Math.min(hi, foo(a, b)));
	}
}
// === end ===

// === case: clamp_max_min_nested_outer_arg ===
class InputPreferMathMethodClampMaxMinNestedOuterArgSliceViolation {
	int m(int x, int y, int hi, int value) {
		return Math.max(bar(x, y), Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_max_min_reversed_args ===
class InputPreferMathMethodClampMaxMinReversedArgsSliceViolation {
	int m(int value, int lo, int hi) {
		return Math.max(Math.min(hi, value), lo);
	}
}
// === end ===

// === case: clamp_max_min_reversed_nested_arg ===
class InputPreferMathMethodClampMaxMinReversedNestedArgSliceViolation {
	int m(int hi, int a, int b, int lo) {
		return Math.max(Math.min(hi, foo(a, b)), lo);
	}
}
// === end ===

// === case: clamp_min_max ===
class InputPreferMathMethodClampMinMaxSliceViolation {
	int m(int value, int lo, int hi) {
		return Math.min(hi, Math.max(lo, value));
	}
}
// === end ===

// === case: clamp_min_max_nested_inner_arg ===
class InputPreferMathMethodClampMinMaxNestedInnerArgSliceViolation {
	int m(int hi, int lo, int a, int b) {
		return Math.min(hi, Math.max(lo, foo(a, b)));
	}
}
// === end ===

// === case: clamp_min_max_reversed_args ===
class InputPreferMathMethodClampMinMaxReversedArgsSliceViolation {
	int m(int value, int lo, int hi) {
		return Math.min(Math.max(lo, value), hi);
	}
}
// === end ===

// === case: clamp_min_max_reversed_nested_arg ===
class InputPreferMathMethodClampMinMaxReversedNestedArgSliceViolation {
	int m(int lo, int a, int b, int hi) {
		return Math.min(Math.max(lo, foo(a, b)), hi);
	}
}
// === end ===

// === case: clamp_string_paren_in_arg ===
class InputPreferMathMethodClampStringParenInArgSliceViolation {
	int m(int value, int hi) {
		return Math.max("(".length(), Math.min(hi, value));
	}
}
// === end ===

// === case: clamp_ternary_arg ===
class InputPreferMathMethodClampTernaryArgSliceViolation {
	int m(int value, int a, int b, int hi, boolean cond) {
		return Math.max(cond ? a : b, Math.min(hi, value));
	}
}
// === end ===

// === case: if_abs_decl_array_target ===
class InputPreferMathMethodIfAbsDeclArrayTargetSliceViolation {
	int m(int[] r, int i, int a) {
		r[i] = Math.abs(a);
		return r[i];
	}
}
// === end ===

// === case: if_abs_decl_ge ===
class InputPreferMathMethodIfAbsDeclGeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_ge_zero_left ===
class InputPreferMathMethodIfAbsDeclGeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_gt ===
class InputPreferMathMethodIfAbsDeclGtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_gt_zero_left ===
class InputPreferMathMethodIfAbsDeclGtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_le ===
class InputPreferMathMethodIfAbsDeclLeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_le_zero_left ===
class InputPreferMathMethodIfAbsDeclLeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_lt ===
class InputPreferMathMethodIfAbsDeclLtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_lt_zero_left ===
class InputPreferMathMethodIfAbsDeclLtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_decl_this_target ===
class InputPreferMathMethodIfAbsDeclThisTargetSliceViolation {
	int x;

	int m(int a) {
		this.x = Math.abs(a);
		return x;
	}
}
// === end ===

// === case: if_abs_if_else_return_ge ===
class InputPreferMathMethodIfAbsIfElseReturnGeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_ge_zero_left ===
class InputPreferMathMethodIfAbsIfElseReturnGeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_gt ===
class InputPreferMathMethodIfAbsIfElseReturnGtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_gt_zero_left ===
class InputPreferMathMethodIfAbsIfElseReturnGtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_le ===
class InputPreferMathMethodIfAbsIfElseReturnLeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_le_zero_left ===
class InputPreferMathMethodIfAbsIfElseReturnLeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt ===
class InputPreferMathMethodIfAbsIfElseReturnLtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt_braced_both ===
class InputPreferMathMethodIfAbsIfElseReturnLtBracedBothSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt_braced_cuddled ===
class InputPreferMathMethodIfAbsIfElseReturnLtBracedCuddledSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt_braced_else ===
class InputPreferMathMethodIfAbsIfElseReturnLtBracedElseSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt_braced_if ===
class InputPreferMathMethodIfAbsIfElseReturnLtBracedIfSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_if_else_return_lt_zero_left ===
class InputPreferMathMethodIfAbsIfElseReturnLtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_ge ===
class InputPreferMathMethodIfAbsTrailingReturnGeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_ge_zero_left ===
class InputPreferMathMethodIfAbsTrailingReturnGeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_gt ===
class InputPreferMathMethodIfAbsTrailingReturnGtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_gt_zero_left ===
class InputPreferMathMethodIfAbsTrailingReturnGtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_le ===
class InputPreferMathMethodIfAbsTrailingReturnLeSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_le_zero_left ===
class InputPreferMathMethodIfAbsTrailingReturnLeZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_lt ===
class InputPreferMathMethodIfAbsTrailingReturnLtSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_lt_braced ===
class InputPreferMathMethodIfAbsTrailingReturnLtBracedSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_abs_trailing_return_lt_zero_left ===
class InputPreferMathMethodIfAbsTrailingReturnLtZeroLeftSliceViolation {
	int m(int a) {
		return Math.abs(a);
	}
}
// === end ===

// === case: if_comparison_not_equality ===
class InputPreferMathMethodIfComparisonNotEqualitySliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_init_overwrite_merge_into_decl ===
class InputPreferMathMethodIfInitOverwriteMergeIntoDeclSliceViolation {
	void m(int a, int b) {
		var r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_max_compound_assign ===
class InputPreferMathMethodIfMaxCompoundAssignSliceViolation {
	int m(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_bit_and ===
class InputPreferMathMethodIfMaxCompoundAssignBitAndSliceViolation {
	int m(int r, int a, int b) {
		r &= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_braced_both ===
class InputPreferMathMethodIfMaxCompoundAssignBracedBothSliceViolation {
	int m(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_braced_cuddled ===
class InputPreferMathMethodIfMaxCompoundAssignBracedCuddledSliceViolation {
	int m(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_braced_else ===
class InputPreferMathMethodIfMaxCompoundAssignBracedElseSliceViolation {
	int m(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_braced_if ===
class InputPreferMathMethodIfMaxCompoundAssignBracedIfSliceViolation {
	int m(int r, int a, int b) {
		r += Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_compound_assign_mul ===
class InputPreferMathMethodIfMaxCompoundAssignMulSliceViolation {
	int m(int r, int a, int b) {
		r *= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_max_decl_array_target ===
class InputPreferMathMethodIfMaxDeclArrayTargetSliceViolation {
	int m(int[] r, int i, int a, int b) {
		r[i] = Math.max(a, b);
		return r[i];
	}
}
// === end ===

// === case: if_max_decl_ge ===
class InputPreferMathMethodIfMaxDeclGeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_gt ===
class InputPreferMathMethodIfMaxDeclGtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_gt_braced_both ===
class InputPreferMathMethodIfMaxDeclGtBracedBothSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_gt_braced_cuddled ===
class InputPreferMathMethodIfMaxDeclGtBracedCuddledSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_gt_braced_else ===
class InputPreferMathMethodIfMaxDeclGtBracedElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_gt_braced_if ===
class InputPreferMathMethodIfMaxDeclGtBracedIfSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_le ===
class InputPreferMathMethodIfMaxDeclLeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_lt ===
class InputPreferMathMethodIfMaxDeclLtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_decl_pre_decrement ===
class InputPreferMathMethodIfMaxDeclPreDecrementSliceViolation {
	int m(int a, int b) {
		return Math.max(--a, b);
	}
}
// === end ===

// === case: if_max_decl_pre_increment ===
class InputPreferMathMethodIfMaxDeclPreIncrementSliceViolation {
	int m(int a, int b) {
		return Math.max(++a, b);
	}
}
// === end ===

// === case: if_max_decl_pre_increment_right_operand ===
class InputPreferMathMethodIfMaxDeclPreIncrementRightOperandSliceViolation {
	int m(int a, int b) {
		return Math.max(a, ++b);
	}
}
// === end ===

// === case: if_max_else_if_chain ===
// skip-reason: if-else not auto-fixable
class InputPreferMathMethodIfMaxElseIfChainSliceViolation {
	int m(boolean cond, int a, int b) {
		if (cond)
			return 0;
		else if (a > b)
			return a;
		else
			return b;
	}
}
// === end ===

// === case: if_max_field_target ===
class InputPreferMathMethodIfMaxFieldTargetSliceViolation {
	int x;

	int m(InputPreferMathMethodIfMaxFieldTargetSliceViolation t, int a, int b) {
		t.x = Math.max(a, b);
		return t.x;
	}
}
// === end ===

// === case: if_max_if_else_return_ge ===
class InputPreferMathMethodIfMaxIfElseReturnGeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt ===
class InputPreferMathMethodIfMaxIfElseReturnGtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_blank_before_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBlankBeforeElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_both ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedBothSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_both_blank_before_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedBothBlankBeforeElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_cuddled ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedCuddledSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_cuddled_unbraced_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedCuddledUnbracedElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_else_blank_before_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedElseBlankBeforeElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_if ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedIfSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_gt_braced_if_blank_before_else ===
class InputPreferMathMethodIfMaxIfElseReturnGtBracedIfBlankBeforeElseSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_le ===
class InputPreferMathMethodIfMaxIfElseReturnLeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_else_return_lt ===
class InputPreferMathMethodIfMaxIfElseReturnLtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_if_return_pre_increment ===
class InputPreferMathMethodIfMaxIfReturnPreIncrementSliceViolation {
	int m(int a, int b) {
		return Math.max(++a, b);
	}
}
// === end ===

// === case: if_max_in_try_block ===
class InputPreferMathMethodIfMaxInTryBlockSliceViolation {
	int m(int a, int b) {
		try {
			return Math.max(a, b);
		}
		finally {
			System.out.println();
		}
	}
}
// === end ===

// === case: if_max_init_overwrite ===
class InputPreferMathMethodIfMaxInitOverwriteSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_init_overwrite_braced ===
class InputPreferMathMethodIfMaxInitOverwriteBracedSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_lambda_if_return ===
// imports: java.util.function.IntBinaryOperator
class InputPreferMathMethodIfMaxLambdaIfReturnSliceViolation {
	IntBinaryOperator m() {
		return (a, b) -> {
			return Math.max(a, b);
		};
	}
}
// === end ===

// === case: if_max_this_target ===
class InputPreferMathMethodIfMaxThisTargetSliceViolation {
	int x;

	int m(int a, int b) {
		this.x = Math.max(a, b);
		return x;
	}
}
// === end ===

// === case: if_max_trailing_return_ge ===
class InputPreferMathMethodIfMaxTrailingReturnGeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_trailing_return_gt ===
class InputPreferMathMethodIfMaxTrailingReturnGtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_trailing_return_gt_braced ===
class InputPreferMathMethodIfMaxTrailingReturnGtBracedSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_trailing_return_le ===
class InputPreferMathMethodIfMaxTrailingReturnLeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_max_trailing_return_lt ===
class InputPreferMathMethodIfMaxTrailingReturnLtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_min_decl_ge ===
class InputPreferMathMethodIfMinDeclGeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_decl_gt ===
class InputPreferMathMethodIfMinDeclGtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_decl_le ===
class InputPreferMathMethodIfMinDeclLeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_decl_lt ===
class InputPreferMathMethodIfMinDeclLtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_ge ===
class InputPreferMathMethodIfMinIfElseReturnGeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_gt ===
class InputPreferMathMethodIfMinIfElseReturnGtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_le ===
class InputPreferMathMethodIfMinIfElseReturnLeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_lt ===
class InputPreferMathMethodIfMinIfElseReturnLtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_lt_braced_both ===
class InputPreferMathMethodIfMinIfElseReturnLtBracedBothSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_lt_braced_cuddled ===
class InputPreferMathMethodIfMinIfElseReturnLtBracedCuddledSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_lt_braced_else ===
class InputPreferMathMethodIfMinIfElseReturnLtBracedElseSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_if_else_return_lt_braced_if ===
class InputPreferMathMethodIfMinIfElseReturnLtBracedIfSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_this_target ===
class InputPreferMathMethodIfMinThisTargetSliceViolation {
	int x;

	int m(int a, int b) {
		this.x = Math.min(a, b);
		return x;
	}
}
// === end ===

// === case: if_min_trailing_return_ge ===
class InputPreferMathMethodIfMinTrailingReturnGeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_trailing_return_gt ===
class InputPreferMathMethodIfMinTrailingReturnGtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_trailing_return_le ===
class InputPreferMathMethodIfMinTrailingReturnLeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_trailing_return_lt ===
class InputPreferMathMethodIfMinTrailingReturnLtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_min_trailing_return_lt_braced ===
class InputPreferMathMethodIfMinTrailingReturnLtBracedSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: if_plain_assign_bare ===
class InputPreferMathMethodIfPlainAssignBareSliceViolation {
	void m(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_bare_braced_both ===
class InputPreferMathMethodIfPlainAssignBareBracedBothSliceViolation {
	void m(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_bare_braced_cuddled ===
class InputPreferMathMethodIfPlainAssignBareBracedCuddledSliceViolation {
	void m(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_bare_braced_else ===
class InputPreferMathMethodIfPlainAssignBareBracedElseSliceViolation {
	void m(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_bare_braced_if ===
class InputPreferMathMethodIfPlainAssignBareBracedIfSliceViolation {
	void m(int r, int a, int b) {
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_decl_clause_false_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclClauseFalseFallsBackToBareSliceViolation {
	int m(int a, int b, int r) {
		System.out.println("hi");
		r = Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_plain_assign_decl_return ===
class InputPreferMathMethodIfPlainAssignDeclReturnSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: if_plain_assign_decl_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int r) {
		int s;
		r = Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: if_plain_assign_decl_without_trailing_return_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignDeclWithoutTrailingReturnFallsBackToBareSliceViolation {
	void m(int a, int b) {
		int r;
		r = Math.max(a, b);
	}
}
// === end ===

// === case: if_plain_assign_return_clause_false_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnClauseFalseFallsBackToBareSliceViolation {
	void m(int a, int b) {
		int r;
		r = Math.max(a, b);
		System.out.println(r);
	}
}
// === end ===

// === case: if_plain_assign_return_var_mismatch_falls_back_to_bare ===
class InputPreferMathMethodIfPlainAssignReturnVarMismatchFallsBackToBareSliceViolation {
	int m(int a, int b, int s) {
		int r;
		r = Math.max(a, b);
		return s;
	}
}
// === end ===

// === case: if_return_else_clause_false_falls_to_trailing ===
class InputPreferMathMethodIfReturnElseClauseFalseFallsToTrailingSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
		// dummy
	}
}
// === end ===

// === case: max_compound_assign_bit_or ===
class InputPreferMathMethodIfCompoundAssignBitOrSliceViolation {
	int m(int r, int a, int b) {
		r |= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_bit_xor ===
class InputPreferMathMethodIfCompoundAssignBitXorSliceViolation {
	int m(int r, int a, int b) {
		r ^= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_div ===
class InputPreferMathMethodIfCompoundAssignDivSliceViolation {
	int m(int r, int a, int b) {
		r /= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_left_shift ===
class InputPreferMathMethodIfCompoundAssignLeftShiftSliceViolation {
	int m(int r, int a, int b) {
		r <<= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_mod ===
class InputPreferMathMethodIfCompoundAssignModSliceViolation {
	int m(int r, int a, int b) {
		r %= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_right_shift ===
class InputPreferMathMethodIfCompoundAssignRightShiftSliceViolation {
	int m(int r, int a, int b) {
		r >>= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_sub ===
class InputPreferMathMethodIfCompoundAssignSubSliceViolation {
	int m(int r, int a, int b) {
		r -= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_compound_assign_unsigned_right_shift ===
class InputPreferMathMethodIfCompoundAssignUnsignedRightShiftSliceViolation {
	int m(int r, int a, int b) {
		r >>>= Math.max(a, b);
		return r;
	}
}
// === end ===

// === case: max_ge ===
class InputPreferMathMethodTernaryMaxGeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: max_gt ===
class InputPreferMathMethodTernaryMaxGtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: max_le ===
class InputPreferMathMethodTernaryMaxLeSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: max_lt ===
class InputPreferMathMethodTernaryMaxLtSliceViolation {
	int m(int a, int b) {
		return Math.max(a, b);
	}
}
// === end ===

// === case: max_with_array_access ===
class InputPreferMathMethodTernaryMaxWithArrayAccessSliceViolation {
	int m(int[] arr) {
		return Math.max(arr[0], arr[1]);
	}
}
// === end ===

// === case: max_with_field_access ===
class InputPreferMathMethodTernaryMaxWithFieldAccessSliceViolation {
	int x;

	int m(InputPreferMathMethodTernaryMaxWithFieldAccessSliceViolation a, InputPreferMathMethodTernaryMaxWithFieldAccessSliceViolation b) {
		return Math.max(a.x, b.x);
	}
}
// === end ===

// === case: max_with_literal ===
class InputPreferMathMethodTernaryMaxWithLiteralSliceViolation {
	int m(int a) {
		return Math.max(a, 5);
	}
}
// === end ===

// === case: max_with_pre_decrement ===
class InputPreferMathMethodTernaryMaxWithPreDecrementSliceViolation {
	int m(int a, int b) {
		return Math.max(--a, b);
	}
}
// === end ===

// === case: max_with_pre_increment ===
class InputPreferMathMethodTernaryMaxWithPreIncrementSliceViolation {
	int m(int a, int b) {
		return Math.max(++a, b);
	}
}
// === end ===

// === case: min_ge ===
class InputPreferMathMethodTernaryMinGeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: min_gt ===
class InputPreferMathMethodTernaryMinGtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: min_le ===
class InputPreferMathMethodTernaryMinLeSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===

// === case: min_lt ===
class InputPreferMathMethodTernaryMinLtSliceViolation {
	int m(int a, int b) {
		return Math.min(a, b);
	}
}
// === end ===