package com.etk2000.checkstyle.inputs.redundantcast;

// === case: block_comment_hides_method_call ===
class InputRedundantCastBlockCommentHidesCallSliceViolation {
	String foo(String x) {
		return x;
	}

	int m(String s) {
		return foo /* note */((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: block_comment_in_receiver_wrap ===
class InputRedundantCastBlockCommentInReceiverSliceViolation {
	int m(String s) {
		return (/* note */ (String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: cast_with_inline_block_comment ===
class InputRedundantCastInlineBlockCommentSliceViolation {
	void m() {
		final String s = (String) /* preserved */ null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: cast_with_leading_block_comment ===
class InputRedundantCastLeadingBlockCommentSliceViolation {
	void m() {
		/* leading */ final String s = (String) null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: compound_assignment_band ===
class InputRedundantCastCompoundBandSliceViolation {
	void m(long x, int y) {
		x &= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bor ===
class InputRedundantCastCompoundBorSliceViolation {
	void m(long x, int y) {
		x |= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bsr ===
class InputRedundantCastCompoundBsrSliceViolation {
	void m(long x, int y) {
		x >>>= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bxor ===
class InputRedundantCastCompoundBxorSliceViolation {
	void m(long x, int y) {
		x ^= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_div ===
class InputRedundantCastCompoundDivSliceViolation {
	void m(long x, int y) {
		x /= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_minus ===
class InputRedundantCastCompoundMinusSliceViolation {
	void m(long x, int y) {
		x -= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_mod ===
class InputRedundantCastCompoundModSliceViolation {
	void m(long x, int y) {
		x %= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_plus ===
class InputRedundantCastCompoundPlusSliceViolation {
	void m(long x, int y) {
		x += (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_sl ===
class InputRedundantCastCompoundSlSliceViolation {
	void m(long x, int y) {
		x <<= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_sr ===
class InputRedundantCastCompoundSrSliceViolation {
	void m(long x, int y) {
		x >>= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_star ===
class InputRedundantCastCompoundStarSliceViolation {
	void m(long x, int y) {
		x *= (long) y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: null_cast_assignment ===
class InputRedundantCastNullAssignmentSliceViolation {
	void m() {
		final String s = (String) null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: null_cast_assignment_different_target ===
class InputRedundantCastNullDifferentTargetSliceViolation {
	void m() {
		final Object o = (String) null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: null_cast_return ===
class InputRedundantCastNullReturnSliceViolation {
	String m() {
		return (String) null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: null_cast_return_different ===
class InputRedundantCastNullReturnDifferentSliceViolation {
	Object m() {
		return (String) null; // violation: Remove redundant cast to 'String' (expression is already 'null').
	}
}
// === end ===

// === case: outer_paren_at_line_start ===
class InputRedundantCastOuterParenLineStartSliceViolation {
	void m(String s) {
((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
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
((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: path_b_inner_string_literal ===
class InputRedundantCastPathBInnerStringSliceViolation {
	String m() {
		return ((String) "x"); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: path_b_outer_not_adjacent ===
class InputRedundantCastPathBOuterNotAdjacentSliceViolation {
	String m(String s) {
		return ( (String) s ); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: return_with_trailing_block_comment ===
class InputRedundantCastReturnTrailingCommentSliceViolation {
	int m(String s) {
		return /* note */((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_field ===
class InputRedundantCastSameTypeFieldRefSliceViolation {
	int sameField;

	void m() {
		final int x = (int) sameField; // violation: Remove redundant cast to 'int' (expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_foreach ===
// imports: java.util.List
class InputRedundantCastSameTypeForeachSliceViolation {
	void m(List<String> list) {
		for (String s : list)
			System.out.println((String) s); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_forinit ===
class InputRedundantCastSameTypeForInitSliceViolation {
	void m() {
		for (int i = 0; i < 10; i++)
			System.out.println((int) i); // violation: Remove redundant cast to 'int' (expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_new ===
class InputRedundantCastSameTypeNewSliceViolation {
	void m() {
		final Object x = (InputRedundantCastSameTypeNewSliceViolation) new InputRedundantCastSameTypeNewSliceViolation(); // violation: Remove redundant cast to 'InputRedundantCastSameTypeNewSliceViolation' (expression is already 'InputRedundantCastSameTypeNewSliceViolation').
	}
}
// === end ===

// === case: same_type_cast_parameter ===
class InputRedundantCastSameTypeParameterSliceViolation {
	void m(String s) {
		final String x = (String) s; // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren ===
class InputRedundantCastReceiverParenSliceViolation {
	int m(String s) {
		return ((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_dollar_id ===
class InputRedundantCastDollarIdSliceViolation {
	void foo$(String x) {}

	void m(String s) {
		foo$((String) s); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_line_start ===
class InputRedundantCastReceiverParenLineStartSliceViolation {
	int m(String s) {
		return
((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_no_dot ===
class InputRedundantCastReceiverParenNoDotSliceViolation {
	String m(String s) {
		return ((String) s); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_return_no_space ===
class InputRedundantCastReceiverParenReturnNoSpaceSliceViolation {
	int m(String s) {
		return((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_string_with_parens ===
class InputRedundantCastReceiverParenStringSliceViolation {
	int m() {
		return ((String) "x)y").length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_throw ===
class InputRedundantCastReceiverParenThrowSliceViolation extends RuntimeException {
	static void m(InputRedundantCastReceiverParenThrowSliceViolation e) {
		throw ((InputRedundantCastReceiverParenThrowSliceViolation) e).self(); // violation: Remove redundant cast to 'InputRedundantCastReceiverParenThrowSliceViolation' (expression is already 'InputRedundantCastReceiverParenThrowSliceViolation').
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
				yield ((String) s).length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
			}
			default -> 0;
		};
	}
}
// === end ===

// === case: same_type_cast_this ===
class InputRedundantCastSameTypeThisSliceViolation {
	InputRedundantCastSameTypeThisSliceViolation m() {
		return (InputRedundantCastSameTypeThisSliceViolation) this; // violation: Remove redundant cast to 'InputRedundantCastSameTypeThisSliceViolation' (expression is already 'InputRedundantCastSameTypeThisSliceViolation').
	}
}
// === end ===

// === case: same_type_cast_variable_int ===
class InputRedundantCastSameTypeVarIntSliceViolation {
	void m() {
		final int x = 5;
		final int y = (int) x; // violation: Remove redundant cast to 'int' (expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_variable_string ===
class InputRedundantCastSameTypeVarStringSliceViolation {
	void m() {
		final String s = "hi";
		final String t = (String) s; // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: same_type_field_char ===
class InputRedundantCastSameTypeCharFieldSliceViolation {
	char x = (char) 'a'; // violation: Remove redundant cast to 'char' (expression is already 'char').
}
// === end ===

// === case: same_type_field_double ===
class InputRedundantCastSameTypeDoubleFieldSliceViolation {
	double x = (double) 5.0; // violation: Remove redundant cast to 'double' (expression is already 'double').
}
// === end ===

// === case: same_type_field_float ===
class InputRedundantCastSameTypeFloatFieldSliceViolation {
	float x = (float) 5.0f; // violation: Remove redundant cast to 'float' (expression is already 'float').
}
// === end ===

// === case: same_type_field_int ===
class InputRedundantCastSameTypeIntFieldSliceViolation {
	int x = (int) 5; // violation: Remove redundant cast to 'int' (expression is already 'int').
}
// === end ===

// === case: same_type_field_long ===
class InputRedundantCastSameTypeLongFieldSliceViolation {
	long x = (long) 5L; // violation: Remove redundant cast to 'long' (expression is already 'long').
}
// === end ===

// === case: same_type_field_string ===
class InputRedundantCastSameTypeStringFieldSliceViolation {
	String x = (String) "hi"; // violation: Remove redundant cast to 'String' (expression is already 'String').
}
// === end ===

// === case: same_type_nested_cast ===
class InputRedundantCastSameTypeNestedSliceViolation {
	void m(Object obj) {
		final String s = (String) (String) obj; // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: sibling_double_literal ===
class InputRedundantCastSiblingDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double d = (double) x * 1.5; // violation: Remove redundant cast to 'double' (expression is already 'int').
	}
}
// === end ===

// === case: sibling_float_literal ===
class InputRedundantCastSiblingFloatSliceViolation {
	void m() {
		final int x = 5;
		final float d = (float) x * 1.5f; // violation: Remove redundant cast to 'float' (expression is already 'int').
	}
}
// === end ===

// === case: sibling_long_literal ===
class InputRedundantCastSiblingLongLiteralSliceViolation {
	void m() {
		final int x = 5;
		final long y = (long) x * 100L; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: sibling_long_variable ===
class InputRedundantCastSiblingLongVarSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final long z = (long) x * y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: sibling_wider_comparison ===
class InputRedundantCastSiblingComparisonSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final boolean b = (long) x == y; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: string_escape_quote ===
class InputRedundantCastStringEscapeQuoteSliceViolation {
	int m() {
		return ((String) "a\")b").length(); // violation: Remove redundant cast to 'String' (expression is already 'String').
	}
}
// === end ===

// === case: ternary_false_branch ===
class InputRedundantCastTernaryFalseBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? 0L : (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: ternary_return ===
class InputRedundantCastTernaryReturnSliceViolation {
	long m(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0L; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: ternary_true_branch ===
class InputRedundantCastTernaryTrueBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : 0L; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: ternary_with_long_reassignment ===
class InputRedundantCastTernaryLongReassignSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		long y = 0;
		y = flag ? (long) x : z; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: ternary_with_long_target ===
class InputRedundantCastTernaryLongTargetSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		final long y = flag ? (long) x : z; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_byte_to_int ===
class InputRedundantCastWideningByteToIntSliceViolation {
	void m() {
		final byte b = 5;
		final int x = (int) b; // violation: Remove redundant cast to 'int' (expression is already 'byte').
	}
}
// === end ===

// === case: widening_byte_to_short ===
class InputRedundantCastWideningByteToShortSliceViolation {
	void m() {
		final byte b = 5;
		final short x = (short) b; // violation: Remove redundant cast to 'short' (expression is already 'byte').
	}
}
// === end ===

// === case: widening_char_to_int ===
class InputRedundantCastWideningCharToIntSliceViolation {
	void m() {
		final char c = 'a';
		final int x = (int) c; // violation: Remove redundant cast to 'int' (expression is already 'char').
	}
}
// === end ===

// === case: widening_float_to_double ===
class InputRedundantCastWideningFloatToDoubleSliceViolation {
	void m() {
		final float f = 5.0f;
		final double x = (double) f; // violation: Remove redundant cast to 'double' (expression is already 'float').
	}
}
// === end ===

// === case: widening_in_return ===
class InputRedundantCastWideningInReturnSliceViolation {
	long m() {
		final int x = 5;
		return (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_in_return_wider ===
class InputRedundantCastWideningInReturnWiderSliceViolation {
	double m() {
		final int x = 5;
		return (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_in_standalone_assign ===
class InputRedundantCastWideningStandaloneAssignSliceViolation {
	void m() {
		final int x = 5;
		long y = 0;
		y = (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_double ===
class InputRedundantCastWideningIntToDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double x = (double) x; // violation: Remove redundant cast to 'double' (expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_float ===
class InputRedundantCastWideningIntToFloatSliceViolation {
	void m() {
		final int x = 5;
		final float x = (float) x; // violation: Remove redundant cast to 'float' (expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_long ===
class InputRedundantCastWideningIntToLongSliceViolation {
	void m() {
		final int x = 5;
		final long x = (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_long_to_double ===
class InputRedundantCastWideningLongToDoubleSliceViolation {
	void m() {
		final long x = 5L;
		final double d = (double) x; // violation: Remove redundant cast to 'double' (expression is already 'long').
	}
}
// === end ===

// === case: widening_long_to_float ===
class InputRedundantCastWideningLongToFloatSliceViolation {
	void m() {
		final long x = 5L;
		final float f = (float) x; // violation: Remove redundant cast to 'float' (expression is already 'long').
	}
}
// === end ===

// === case: widening_on_parameter ===
class InputRedundantCastWideningOnParameterSliceViolation {
	void m(int x) {
		final long y = (long) x; // violation: Remove redundant cast to 'long' (expression is already 'int').
	}
}
// === end ===

// === case: widening_short_to_int ===
class InputRedundantCastWideningShortToIntSliceViolation {
	void m() {
		final short s = 5;
		final int x = (int) s; // violation: Remove redundant cast to 'int' (expression is already 'short').
	}
}
// === end ===

// === case: widening_short_to_long ===
class InputRedundantCastWideningShortToLongSliceViolation {
	void m() {
		final short s = 5;
		final long x = (long) s; // violation: Remove redundant cast to 'long' (expression is already 'short').
	}
}
// === end ===