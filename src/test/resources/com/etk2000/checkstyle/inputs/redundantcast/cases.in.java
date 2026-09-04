package com.etk2000.checkstyle.inputs.redundantcast;

// === case: block_comment_after_subject_in_wrap ===
class InputRedundantCastCommentAfterSubjectSliceViolation {
	int m(String s) {
		return ((String) s /* legacy */).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: block_comment_both_sides_of_subject ===
class InputRedundantCastCommentBothSidesSliceViolation {
	int m(String s) {
		return (/* a */ (String) s /* b */).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: block_comment_hides_method_call ===
class InputRedundantCastBlockCommentHidesCallSliceViolation {
	String foo(String x) {
		return x;
	}

	int m(String s) {
		return foo /* note */((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: block_comment_in_receiver_wrap ===
class InputRedundantCastBlockCommentInReceiverSliceViolation {
	int m(String s) {
		return (/* note */ (String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: block_comment_in_receiver_wrap_no_space ===
class InputRedundantCastCommentInWrapNoSpaceSliceViolation {
	int m(String s) {
		return(/* note */ (String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: cast_with_inline_block_comment ===
class InputRedundantCastInlineBlockCommentSliceViolation {
	void m() {
		final String s = (String) /* preserved */ null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: cast_with_leading_block_comment ===
class InputRedundantCastLeadingBlockCommentSliceViolation {
	void m() {
		/* leading */ final String s = (String) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: cast_with_two_block_comments ===
class InputRedundantCastTwoBlockCommentsSliceViolation {
	void m() {
		final String s = (/* a */ String /* b */) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: compound_assignment_band ===
class InputRedundantCastCompoundBandSliceViolation {
	void m(long x, int y) {
		x &= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bor ===
class InputRedundantCastCompoundBorSliceViolation {
	void m(long x, int y) {
		x |= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bsr ===
class InputRedundantCastCompoundBsrSliceViolation {
	void m(long x, int y) {
		x >>>= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_bxor ===
class InputRedundantCastCompoundBxorSliceViolation {
	void m(long x, int y) {
		x ^= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_div ===
class InputRedundantCastCompoundDivSliceViolation {
	void m(long x, int y) {
		x /= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_minus ===
class InputRedundantCastCompoundMinusSliceViolation {
	void m(long x, int y) {
		x -= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_mod ===
class InputRedundantCastCompoundModSliceViolation {
	void m(long x, int y) {
		x %= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_plus ===
class InputRedundantCastCompoundPlusSliceViolation {
	void m(long x, int y) {
		x += (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_plus_double_target ===
class InputRedundantCastCompoundPlusDoubleTargetSliceViolation {
	void m(double d, int y) {
		d += (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_sl ===
class InputRedundantCastCompoundSlSliceViolation {
	void m(long x, int y) {
		x <<= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_sr ===
class InputRedundantCastCompoundSrSliceViolation {
	void m(long x, int y) {
		x >>= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: compound_assignment_star ===
class InputRedundantCastCompoundStarSliceViolation {
	void m(long x, int y) {
		x *= (long) y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: division_before_relocated_comment ===
class InputRedundantCastDivisionBeforeCommentSliceViolation {
	int m(int a, int b) {
		return a/(int)/* round down */b; // violation: Remove the redundant cast to 'int' (the expression is already 'int').
	}
}
// === end ===

// === case: division_before_wrapped_cast_comment ===
class InputRedundantCastDivisionBeforeWrappedCommentSliceViolation {
	int m(int a, int i) {
		return a/(/* c */(int) i)*2; // violation: Remove the redundant cast to 'int' (the expression is already 'int').
	}
}
// === end ===

// === case: double_wrap_receiver ===
class InputRedundantCastDoubleWrapReceiverSliceViolation {
	int m(String s) {
		return (((String) s)).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: instanceof_after_receiver_wrap ===
class InputRedundantCastInstanceofAfterWrapSliceViolation {
	boolean m(String s) {
		return ((String) s)instanceof Object; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: line_comment_between_cast_and_subject ===
// skip-reason: cannot remove the cast: the text it spans cannot be rewritten without losing a comment or a literal
class InputRedundantCastLineCommentBeforeSubjectSliceViolation {
	void m(String s) {
		final String t = (String) // violation: Remove the redundant cast to 'String' (the expression is already 'String').
				// note
				s;
	}
}
// === end ===

// === case: multi_line_cast_parens ===
class InputRedundantCastMultiLineCastParensSliceViolation {
	void m(String s) {
		final String t = ( // violation: Remove the redundant cast to 'String' (the expression is already 'String').
				String) s;
	}
}
// === end ===

// === case: multi_line_receiver_wrap_dot_below ===
class InputRedundantCastMultiLineReceiverWrapSliceViolation {
	int m(String s) {
		return ((String) s) // violation: Remove the redundant cast to 'String' (the expression is already 'String').
				.length();
	}
}
// === end ===

// === case: multi_line_receiver_wrap_open_above ===
class InputRedundantCastMultiLineWrapOpenSliceViolation {
	int m(String s) {
		return (
				(String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: multi_line_subject_below ===
class InputRedundantCastMultiLineSubjectBelowSliceViolation {
	void m(String s) {
		final String t = (String) // violation: Remove the redundant cast to 'String' (the expression is already 'String').
				s;
	}
}
// === end ===

// === case: null_cast_assignment ===
class InputRedundantCastNullAssignmentSliceViolation {
	void m() {
		final String s = (String) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: null_cast_assignment_different_target ===
class InputRedundantCastNullDifferentTargetSliceViolation {
	void m() {
		final Object o = (String) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: null_cast_return ===
class InputRedundantCastNullReturnSliceViolation {
	String m() {
		return (String) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: null_cast_return_different ===
class InputRedundantCastNullReturnDifferentSliceViolation {
	Object m() {
		return (String) null; // violation: Remove the redundant cast to 'String' (the expression is already 'null').
	}
}
// === end ===

// === case: outer_paren_at_line_start ===
class InputRedundantCastOuterParenLineStartSliceViolation {
	void m(String s) {
((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
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
((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: path_b_inner_string_literal ===
class InputRedundantCastPathBInnerStringSliceViolation {
	String m() {
		return ((String) "x"); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: path_b_outer_not_adjacent ===
class InputRedundantCastPathBOuterNotAdjacentSliceViolation {
	String m(String s) {
		return ( (String) s ); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: receiver_wrap_line_comment_after_subject ===
class InputRedundantCastReceiverWrapLineCommentSliceViolation {
	int m(String s) {
		return ((String) s // violation: Remove the redundant cast to 'String' (the expression is already 'String').
				// note
				).length();
	}
}
// === end ===

// === case: return_with_trailing_block_comment ===
class InputRedundantCastReturnTrailingCommentSliceViolation {
	int m(String s) {
		return /* note */((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_array_one ===
class InputRedundantCastSameTypeArrayOneSliceViolation {
	void m(String[] arr) {
		final Object o = (String[]) arr; // violation: Remove the redundant cast to 'String[]' (the expression is already 'String[]').
	}
}
// === end ===

// === case: same_type_cast_field ===
class InputRedundantCastSameTypeFieldRefSliceViolation {
	int sameField;

	void m() {
		final int x = (int) sameField; // violation: Remove the redundant cast to 'int' (the expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_foreach ===
// imports: java.util.List
class InputRedundantCastSameTypeForeachSliceViolation {
	void m(List<String> list) {
		for (String s : list)
			System.out.println((String) s); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_forinit ===
class InputRedundantCastSameTypeForInitSliceViolation {
	void m() {
		for (int i = 0; i < 10; i++)
			System.out.println((int) i); // violation: Remove the redundant cast to 'int' (the expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_generic ===
// imports: java.util.List
class InputRedundantCastSameTypeGenericSliceViolation {
	void m(List<String> l) {
		final Object o = (List<String>) l; // violation: Remove the redundant cast to 'List<String>' (the expression is already 'List<String>').
	}
}
// === end ===

// === case: same_type_cast_new ===
class InputRedundantCastSameTypeNewSliceViolation {
	void m() {
		final Object x = (InputRedundantCastSameTypeNewSliceViolation) new InputRedundantCastSameTypeNewSliceViolation(); // violation: Remove the redundant cast to 'InputRedundantCastSameTypeNewSliceViolation' (the expression is already 'InputRedundantCastSameTypeNewSliceViolation').
	}
}
// === end ===

// === case: same_type_cast_parameter ===
class InputRedundantCastSameTypeParameterSliceViolation {
	void m(String s) {
		final String x = (String) s; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_raw_list ===
// imports: java.util.List
class InputRedundantCastSameTypeRawListSliceViolation {
	void m(List raw) {
		final Object o = (List) raw; // violation: Remove the redundant cast to 'List' (the expression is already 'List').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren ===
class InputRedundantCastReceiverParenSliceViolation {
	int m(String s) {
		return ((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_dollar_id ===
class InputRedundantCastDollarIdSliceViolation {
	void foo$(String x) {}

	void m(String s) {
		foo$((String) s); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_line_start ===
class InputRedundantCastReceiverParenLineStartSliceViolation {
	int m(String s) {
		return
((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_no_dot ===
class InputRedundantCastReceiverParenNoDotSliceViolation {
	String m(String s) {
		return ((String) s); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_return_no_space ===
class InputRedundantCastReceiverParenReturnNoSpaceSliceViolation {
	int m(String s) {
		return((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_string_with_parens ===
class InputRedundantCastReceiverParenStringSliceViolation {
	int m() {
		return ((String) "x)y").length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_receiver_paren_throw ===
class InputRedundantCastReceiverParenThrowSliceViolation extends RuntimeException {
	static void m(InputRedundantCastReceiverParenThrowSliceViolation e) {
		throw ((InputRedundantCastReceiverParenThrowSliceViolation) e).self(); // violation: Remove the redundant cast to 'InputRedundantCastReceiverParenThrowSliceViolation' (the expression is already 'InputRedundantCastReceiverParenThrowSliceViolation').
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
				yield ((String) s).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
			}
			default -> 0;
		};
	}
}
// === end ===

// === case: same_type_cast_return_no_space_no_wrap ===
class InputRedundantCastReturnNoSpaceNoWrapSliceViolation {
	String m(String s) {
		return(String) s; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_cast_this ===
class InputRedundantCastSameTypeThisSliceViolation {
	InputRedundantCastSameTypeThisSliceViolation m() {
		return (InputRedundantCastSameTypeThisSliceViolation) this; // violation: Remove the redundant cast to 'InputRedundantCastSameTypeThisSliceViolation' (the expression is already 'InputRedundantCastSameTypeThisSliceViolation').
	}
}
// === end ===

// === case: same_type_cast_this_anonymous_subclass ===
class InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation {
	void m() {
		final InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation sub =
				new InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation() {
					void inner() {
						final Object o = (InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation) this; // violation: Remove the redundant cast to 'InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation' (the expression is already 'InputRedundantCastSameTypeThisAnonymousSubclassSliceViolation').
					}
				};
	}
}
// === end ===

// === case: same_type_cast_this_enum_constant_body ===
enum InputRedundantCastSameTypeThisEnumConstantBodySliceViolation {
	A {
		@Override
		Object self() {
			return (InputRedundantCastSameTypeThisEnumConstantBodySliceViolation) this; // violation: Remove the redundant cast to 'InputRedundantCastSameTypeThisEnumConstantBodySliceViolation' (the expression is already 'InputRedundantCastSameTypeThisEnumConstantBodySliceViolation').
		}
	};

	abstract Object self();
}
// === end ===

// === case: same_type_cast_this_local_class ===
class InputRedundantCastSameTypeThisLocalClassSliceViolation {
	void m() {
		class Local {
			Object self() {
				return (Local) this; // violation: Remove the redundant cast to 'Local' (the expression is already 'Local').
			}
		}
	}
}
// === end ===

// === case: same_type_cast_variable_int ===
class InputRedundantCastSameTypeVarIntSliceViolation {
	void m() {
		final int x = 5;
		final int y = (int) x; // violation: Remove the redundant cast to 'int' (the expression is already 'int').
	}
}
// === end ===

// === case: same_type_cast_variable_string ===
class InputRedundantCastSameTypeVarStringSliceViolation {
	void m() {
		final String s = "hi";
		final String t = (String) s; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: same_type_field_char ===
class InputRedundantCastSameTypeCharFieldSliceViolation {
	char x = (char) 'a'; // violation: Remove the redundant cast to 'char' (the expression is already 'char').
}
// === end ===

// === case: same_type_field_double ===
class InputRedundantCastSameTypeDoubleFieldSliceViolation {
	double x = (double) 5.0; // violation: Remove the redundant cast to 'double' (the expression is already 'double').
}
// === end ===

// === case: same_type_field_float ===
class InputRedundantCastSameTypeFloatFieldSliceViolation {
	float x = (float) 5.0f; // violation: Remove the redundant cast to 'float' (the expression is already 'float').
}
// === end ===

// === case: same_type_field_int ===
class InputRedundantCastSameTypeIntFieldSliceViolation {
	int x = (int) 5; // violation: Remove the redundant cast to 'int' (the expression is already 'int').
}
// === end ===

// === case: same_type_field_long ===
class InputRedundantCastSameTypeLongFieldSliceViolation {
	long x = (long) 5L; // violation: Remove the redundant cast to 'long' (the expression is already 'long').
}
// === end ===

// === case: same_type_field_string ===
class InputRedundantCastSameTypeStringFieldSliceViolation {
	String x = (String) "hi"; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
}
// === end ===

// === case: same_type_nested_cast ===
class InputRedundantCastSameTypeNestedSliceViolation {
	void m(Object obj) {
		final String s = (String) (String) obj; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: sibling_double_literal ===
class InputRedundantCastSiblingDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double d = (double) x * 1.5; // violation: Remove the redundant cast to 'double' (the expression is already 'int').
	}
}
// === end ===

// === case: sibling_double_literal_long_cast ===
class InputRedundantCastSiblingDoubleLongCastSliceViolation {
	void m(int x) {
		final double d = (long) x * 1.5; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: sibling_float_literal ===
class InputRedundantCastSiblingFloatSliceViolation {
	void m() {
		final int x = 5;
		final float d = (float) x * 1.5f; // violation: Remove the redundant cast to 'float' (the expression is already 'int').
	}
}
// === end ===

// === case: sibling_long_literal ===
class InputRedundantCastSiblingLongLiteralSliceViolation {
	void m() {
		final int x = 5;
		final long y = (long) x * 100L; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: sibling_long_variable ===
class InputRedundantCastSiblingLongVarSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final long z = (long) x * y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: sibling_wider_comparison ===
class InputRedundantCastSiblingComparisonSliceViolation {
	void m() {
		final int x = 5;
		final long y = 10;
		final boolean b = (long) x == y; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: string_escape_quote ===
class InputRedundantCastStringEscapeQuoteSliceViolation {
	int m() {
		return ((String) "a\")b").length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: supplementary_identifier_after_return ===
class InputRedundantCastSupplementaryIdentifierSliceViolation {
	String m(String 𝒜) {
		return(String) 𝒜; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: supplementary_identifier_before_instanceof ===
class InputRedundantCastSupplementaryBeforeInstanceofSliceViolation {
	boolean m(String 𝒜) {
		return ((String) 𝒜)instanceof Object; // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: ternary_false_branch ===
class InputRedundantCastTernaryFalseBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? 0L : (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: ternary_return ===
class InputRedundantCastTernaryReturnSliceViolation {
	long m(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0L; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: ternary_sibling_gate_var_target ===
class InputRedundantCastTernarySiblingGateVarTargetSliceViolation {
	void m(boolean flag, int x) {
		final var z = flag ? (long) x : 1L; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: ternary_true_branch ===
class InputRedundantCastTernaryTrueBranchSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : 0L; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: ternary_with_long_reassignment ===
class InputRedundantCastTernaryLongReassignSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		long y = 0;
		y = flag ? (long) x : z; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: ternary_with_long_target ===
class InputRedundantCastTernaryLongTargetSliceViolation {
	void m(boolean flag) {
		final int x = 5;
		final int z = 10;
		final long y = flag ? (long) x : z; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: two_casts_one_line ===
class InputRedundantCastTwoCastsOneLineSliceViolation {
	int m(String a, String b) {
		return ((String) a).length() + ((String) b).length(); // violation: Remove the redundant cast to 'String' (the expression is already 'String'). // violation: Remove the redundant cast to 'String' (the expression is already 'String').
	}
}
// === end ===

// === case: widening_byte_to_int ===
class InputRedundantCastWideningByteToIntSliceViolation {
	void m() {
		final byte b = 5;
		final int x = (int) b; // violation: Remove the redundant cast to 'int' (the expression is already 'byte').
	}
}
// === end ===

// === case: widening_byte_to_short ===
class InputRedundantCastWideningByteToShortSliceViolation {
	void m() {
		final byte b = 5;
		final short x = (short) b; // violation: Remove the redundant cast to 'short' (the expression is already 'byte').
	}
}
// === end ===

// === case: widening_char_to_int ===
class InputRedundantCastWideningCharToIntSliceViolation {
	void m() {
		final char c = 'a';
		final int x = (int) c; // violation: Remove the redundant cast to 'int' (the expression is already 'char').
	}
}
// === end ===

// === case: widening_float_to_double ===
class InputRedundantCastWideningFloatToDoubleSliceViolation {
	void m() {
		final float f = 5.0f;
		final double x = (double) f; // violation: Remove the redundant cast to 'double' (the expression is already 'float').
	}
}
// === end ===

// === case: widening_in_return ===
class InputRedundantCastWideningInReturnSliceViolation {
	long m() {
		final int x = 5;
		return (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_in_return_wider ===
class InputRedundantCastWideningInReturnWiderSliceViolation {
	double m() {
		final int x = 5;
		return (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_in_standalone_assign ===
class InputRedundantCastWideningStandaloneAssignSliceViolation {
	void m() {
		final int x = 5;
		long y = 0;
		y = (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_double ===
class InputRedundantCastWideningIntToDoubleSliceViolation {
	void m() {
		final int x = 5;
		final double widened = (double) x; // violation: Remove the redundant cast to 'double' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_float ===
class InputRedundantCastWideningIntToFloatSliceViolation {
	void m() {
		final int x = 5;
		final float widened = (float) x; // violation: Remove the redundant cast to 'float' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_int_to_long ===
class InputRedundantCastWideningIntToLongSliceViolation {
	void m() {
		final int x = 5;
		final long widened = (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_long_to_double ===
class InputRedundantCastWideningLongToDoubleSliceViolation {
	void m() {
		final long x = 5L;
		final double d = (double) x; // violation: Remove the redundant cast to 'double' (the expression is already 'long').
	}
}
// === end ===

// === case: widening_long_to_float ===
class InputRedundantCastWideningLongToFloatSliceViolation {
	void m() {
		final long x = 5L;
		final float f = (float) x; // violation: Remove the redundant cast to 'float' (the expression is already 'long').
	}
}
// === end ===

// === case: widening_on_parameter ===
class InputRedundantCastWideningOnParameterSliceViolation {
	void m(int x) {
		final long y = (long) x; // violation: Remove the redundant cast to 'long' (the expression is already 'int').
	}
}
// === end ===

// === case: widening_short_to_int ===
class InputRedundantCastWideningShortToIntSliceViolation {
	void m() {
		final short s = 5;
		final int x = (int) s; // violation: Remove the redundant cast to 'int' (the expression is already 'short').
	}
}
// === end ===

// === case: widening_short_to_long ===
class InputRedundantCastWideningShortToLongSliceViolation {
	void m() {
		final short s = 5;
		final long x = (long) s; // violation: Remove the redundant cast to 'long' (the expression is already 'short').
	}
}
// === end ===

// === case: wrap_keeps_comment_inside_subject ===
class InputRedundantCastCommentInsideSubjectSliceViolation {
	static class Foo {
		int hash() {
			return 0;
		}
	}

	int m() {
		return ((Foo) new Foo(/* note */)).hash(); // violation: Remove the redundant cast to 'Foo' (the expression is already 'Foo').
	}
}
// === end ===

// === case: wrap_keeps_multi_line_subject ===
class InputRedundantCastMultiLineSubjectWrapSliceViolation {
	abstract static class Base {
		abstract String name();

		abstract int size();
	}

	String m() {
		return ((Base) new Base() { // violation: Remove the redundant cast to 'Base' (the expression is already 'Base').
			@Override
			String name() {
				return "x";
			}

			@Override
			int size() {
				return 1;
			}
		}).name();
	}
}
// === end ===