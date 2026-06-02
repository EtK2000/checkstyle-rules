package com.etk2000.checkstyle.inputs.redundantcast;

// === case: block_comment_hides_method_call ===
class InputRedundantCastBlockCommentHidesCallSliceViolation {
	String foo(String x) {
		return x;
	}

	int m(String s) {
		return foo /* note */(s).length();
	}
}
// === end ===

// === case: block_comment_in_receiver_wrap ===
class InputRedundantCastBlockCommentInReceiverSliceViolation {
	int m(String s) {
		return (/* note */ s).length();
	}
}
// === end ===

// === case: cast_with_inline_block_comment ===
class InputRedundantCastInlineBlockCommentSliceViolation {
	void m() {
		final String s = /* preserved */ null;
	}
}
// === end ===

// === case: cast_with_leading_block_comment ===
class InputRedundantCastLeadingBlockCommentSliceViolation {
	void m() {
		/* leading */ final String s = null;
	}
}
// === end ===

// === case: compound_assignment_band ===
class InputRedundantCastCompoundBandSliceViolation {
	void m(long x, int y) {
		x &= y;
	}
}
// === end ===

// === case: compound_assignment_bor ===
class InputRedundantCastCompoundBorSliceViolation {
	void m(long x, int y) {
		x |= y;
	}
}
// === end ===

// === case: compound_assignment_bsr ===
class InputRedundantCastCompoundBsrSliceViolation {
	void m(long x, int y) {
		x >>>= y;
	}
}
// === end ===

// === case: compound_assignment_bxor ===
class InputRedundantCastCompoundBxorSliceViolation {
	void m(long x, int y) {
		x ^= y;
	}
}
// === end ===

// === case: compound_assignment_div ===
class InputRedundantCastCompoundDivSliceViolation {
	void m(long x, int y) {
		x /= y;
	}
}
// === end ===

// === case: compound_assignment_minus ===
class InputRedundantCastCompoundMinusSliceViolation {
	void m(long x, int y) {
		x -= y;
	}
}
// === end ===

// === case: compound_assignment_mod ===
class InputRedundantCastCompoundModSliceViolation {
	void m(long x, int y) {
		x %= y;
	}
}
// === end ===

// === case: compound_assignment_plus ===
class InputRedundantCastCompoundPlusSliceViolation {
	void m(long x, int y) {
		x += y;
	}
}
// === end ===

// === case: compound_assignment_sl ===
class InputRedundantCastCompoundSlSliceViolation {
	void m(long x, int y) {
		x <<= y;
	}
}
// === end ===

// === case: compound_assignment_sr ===
class InputRedundantCastCompoundSrSliceViolation {
	void m(long x, int y) {
		x >>= y;
	}
}
// === end ===

// === case: compound_assignment_star ===
class InputRedundantCastCompoundStarSliceViolation {
	void m(long x, int y) {
		x *= y;
	}
}
// === end ===

// === case: null_cast_assignment ===
class InputRedundantCastNullAssignmentSliceViolation {
	void m() {
		final String s = null;
	}
}
// === end ===

// === case: null_cast_assignment_different_target ===
class InputRedundantCastNullDifferentTargetSliceViolation {
	void m() {
		final Object o = null;
	}
}
// === end ===

// === case: null_cast_return ===
class InputRedundantCastNullReturnSliceViolation {
	String m() {
		return null;
	}
}
// === end ===

// === case: null_cast_return_different ===
class InputRedundantCastNullReturnDifferentSliceViolation {
	Object m() {
		return null;
	}
}
// === end ===

// === case: outer_paren_at_line_start ===
class InputRedundantCastOuterParenLineStartSliceViolation {
	void m(String s) {
s.length();
	}
}
// === end ===

// === case: path_a_prior_line_ident_rejected ===
class InputRedundantCastPriorLineIdentSliceViolation {
	String foo(String x) {
		return x;
	}

	int m(String s) {
		return foo
(s).length();
	}
}
// === end ===

// === case: path_b_inner_string_literal ===
class InputRedundantCastPathBInnerStringSliceViolation {
	String m() {
		return ("x");
	}
}
// === end ===

// === case: path_b_outer_not_adjacent ===
class InputRedundantCastPathBOuterNotAdjacentSliceViolation {
	String m(String s) {
		return ( s );
	}
}
// === end ===

// === case: return_with_trailing_block_comment ===
class InputRedundantCastReturnTrailingCommentSliceViolation {
	int m(String s) {
		return /* note */s.length();
	}
}
// === end ===

// === case: same_type_cast_field ===
class InputRedundantCastSameTypeFieldRefSliceViolation {
	int sameField;

	void m() {
		final int x = sameField;
	}
}
// === end ===

// === case: same_type_cast_foreach ===
// imports: java.util.List
class InputRedundantCastSameTypeForeachSliceViolation {
	void m(List<String> list) {
		for (String s : list)
			System.out.println(s);
	}
}
// === end ===

// === case: same_type_cast_forinit ===
class InputRedundantCastSameTypeForInitSliceViolation {
	void m() {
		for (int i = 0; i < 10; i++)
			System.out.println(i);
	}
}
// === end ===

// === case: same_type_cast_new ===
class InputRedundantCastSameTypeNewSliceViolation {
	void m() {
		final Object x = new InputRedundantCastSameTypeNewSliceViolation();
	}
}
// === end ===

// === case: same_type_cast_parameter ===
class InputRedundantCastSameTypeParameterSliceViolation {
	void m(String s) {
		final String x = s;
	}
}
// === end ===

// === case: same_type_cast_receiver_paren ===
class InputRedundantCastReceiverParenSliceViolation {
	int m(String s) {
		return s.length();
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_dollar_id ===
class InputRedundantCastDollarIdSliceViolation {
	void foo$(String x) {}

	void m(String s) {
		foo$(s);
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_line_start ===
class InputRedundantCastReceiverParenLineStartSliceViolation {
	int m(String s) {
		return
s.length();
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_no_dot ===
class InputRedundantCastReceiverParenNoDotSliceViolation {
	String m(String s) {
		return s;
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_return_no_space ===
class InputRedundantCastReceiverParenReturnNoSpaceSliceViolation {
	int m(String s) {
		return s.length();
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_string_with_parens ===
class InputRedundantCastReceiverParenStringSliceViolation {
	int m() {
		return "x)y".length();
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_throw ===
class InputRedundantCastReceiverParenThrowSliceViolation extends RuntimeException {
	static void m(InputRedundantCastReceiverParenThrowSliceViolation e) {
		throw e.self();
	}

	InputRedundantCastReceiverParenThrowSliceViolation self() {
		return this;
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_yield ===
class InputRedundantCastReceiverParenYieldSliceViolation {
	int m(int n, String s) {
		return switch (n) {
			case 1 -> {
				yield s.length();
			}
			default -> 0;
		};
	}
}
// === end ===

// === case: same_type_cast_this ===
class InputRedundantCastSameTypeThisSliceViolation {
	InputRedundantCastSameTypeThisSliceViolation m() {
		return this;
	}
}
// === end ===

// === case: same_type_cast_variable_int ===
class InputRedundantCastSameTypeVarIntSliceViolation {
	void m() {
		final int x = 5;
		final int y = x;
	}
}
// === end ===

// === case: same_type_cast_variable_string ===
class InputRedundantCastSameTypeVarStringSliceViolation {
	void m() {
		final String s = "hi";
		final String t = s;
	}
}
// === end ===

// === case: same_type_field_char ===
class InputRedundantCastSameTypeCharFieldSliceViolation {
	char x = 'a';
}
// === end ===

// === case: same_type_field_double ===
class InputRedundantCastSameTypeDoubleFieldSliceViolation {
	double x = 5.0;
}
// === end ===

// === case: same_type_field_float ===
class InputRedundantCastSameTypeFloatFieldSliceViolation {
	float x = 5.0f;
}
// === end ===

// === case: same_type_field_int ===
class InputRedundantCastSameTypeIntFieldSliceViolation {
	int x = 5;
}
// === end ===

// === case: same_type_field_long ===
class InputRedundantCastSameTypeLongFieldSliceViolation {
	long x = 5L;
}
// === end ===

// === case: same_type_field_string ===
class InputRedundantCastSameTypeStringFieldSliceViolation {
	String x = "hi";
}
// === end ===

// === case: same_type_nested_cast ===
class InputRedundantCastSameTypeNestedSliceViolation {
	void m(Object obj) {
		final String s = (String) obj;
	}
}
// === end ===

// === case: sibling_double_literal ===
class InputRedundantCastSiblingDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double d = x * 1.5;
	}
}
// === end ===

// === case: sibling_float_literal ===
class InputRedundantCastSiblingFloatSliceViolation {
	void m() {
		final int x = 5;
		final float d = x * 1.5f;
	}
}
// === end ===

// === case: sibling_long_literal ===
class InputRedundantCastSiblingLongLiteralSliceViolation {
	void m() {
		final int x = 5;
		final long y = x * 100L;
	}
}
// === end ===

// === case: sibling_long_variable ===
class InputRedundantCastSiblingLongVarSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final long z = x * y;
	}
}
// === end ===

// === case: sibling_wider_comparison ===
class InputRedundantCastSiblingComparisonSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final boolean b = x == y;
	}
}
// === end ===

// === case: string_escape_quote ===
class InputRedundantCastStringEscapeQuoteSliceViolation {
	int m() {
		return "a\")b".length();
	}
}
// === end ===

// === case: ternary_false_branch ===
class InputRedundantCastTernaryFalseBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? 0L : x;
	}
}
// === end ===

// === case: ternary_return ===
class InputRedundantCastTernaryReturnSliceViolation {
	long m(boolean flag) {
		final int x = 5;
		return flag ? x : 0L;
	}
}
// === end ===

// === case: ternary_true_branch ===
class InputRedundantCastTernaryTrueBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? x : 0L;
	}
}
// === end ===

// === case: ternary_with_long_reassignment ===
class InputRedundantCastTernaryLongReassignSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		long y = 0;
		y = flag ? x : z;
	}
}
// === end ===

// === case: ternary_with_long_target ===
class InputRedundantCastTernaryLongTargetSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		final long y = flag ? x : z;
	}
}
// === end ===

// === case: widening_byte_to_int ===
class InputRedundantCastWideningByteToIntSliceViolation {
	void m() {
		final byte b = 5;
		final int x = b;
	}
}
// === end ===

// === case: widening_byte_to_short ===
class InputRedundantCastWideningByteToShortSliceViolation {
	void m() {
		final byte b = 5;
		final short x = b;
	}
}
// === end ===

// === case: widening_char_to_int ===
class InputRedundantCastWideningCharToIntSliceViolation {
	void m() {
		final char c = 'a';
		final int x = c;
	}
}
// === end ===

// === case: widening_float_to_double ===
class InputRedundantCastWideningFloatToDoubleSliceViolation {
	void m() {
		final float f = 5.0f;
		final double x = f;
	}
}
// === end ===

// === case: widening_in_return ===
class InputRedundantCastWideningInReturnSliceViolation {
	long m() {
		final int x = 5;
		return x;
	}
}
// === end ===

// === case: widening_in_return_wider ===
class InputRedundantCastWideningInReturnWiderSliceViolation {
	double m() {
		final int x = 5;
		return x;
	}
}
// === end ===

// === case: widening_in_standalone_assign ===
class InputRedundantCastWideningStandaloneAssignSliceViolation {
	void m() {
		final int x = 5;
		long y = 0;
		y = x;
	}
}
// === end ===

// === case: widening_int_to_double ===
class InputRedundantCastWideningIntToDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double x = x;
	}
}
// === end ===

// === case: widening_int_to_float ===
class InputRedundantCastWideningIntToFloatSliceViolation {
	void m() {
		final int x = 5;
		final float x = x;
	}
}
// === end ===

// === case: widening_int_to_long ===
class InputRedundantCastWideningIntToLongSliceViolation {
	void m() {
		final int x = 5;
		final long x = x;
	}
}
// === end ===

// === case: widening_long_to_double ===
class InputRedundantCastWideningLongToDoubleSliceViolation {
	void m() {
		final long x = 5L;
		final double d = x;
	}
}
// === end ===

// === case: widening_long_to_float ===
class InputRedundantCastWideningLongToFloatSliceViolation {
	void m() {
		final long x = 5L;
		final float f = x;
	}
}
// === end ===

// === case: widening_on_parameter ===
class InputRedundantCastWideningOnParameterSliceViolation {
	void m(int x) {
		final long y = x;
	}
}
// === end ===

// === case: widening_short_to_int ===
class InputRedundantCastWideningShortToIntSliceViolation {
	void m() {
		final short s = 5;
		final int x = s;
	}
}
// === end ===

// === case: widening_short_to_long ===
class InputRedundantCastWideningShortToLongSliceViolation {
	void m() {
		final short s = 5;
		final long x = s;
	}
}
// === end ===