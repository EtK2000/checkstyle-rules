package com.etk2000.checkstyle.inputs.preferliteralsuffix;

// === case: annotated_parameter_subject ===
class InputPreferLiteralSuffixAnnotatedParameterSubjectSliceViolation {
	long m(@Deprecated int x) {
		return x * 100L;
	}
}
// === end ===

// === case: binary_literal_small ===
class InputPreferLiteralSuffixBinaryLiteralSmallSliceViolation {
	long m(int x) {
		return x | 0b1010L;
	}
}
// === end ===

// === case: block_comment_spanning_the_fold ===
class InputPreferLiteralSuffixBlockCommentSpanningTheFoldSliceViolation {
	long m(int x) {
		return x * 100L; // c
	}
}
// === end ===

// === case: cast_at_end_of_line_subject_next ===
class InputPreferLiteralSuffixCastAtEndOfLineSubjectNextSliceViolation {
	long m(int x) {
		return x * 100L;
	}
}
// === end ===

// === case: cast_in_braceless_if_body ===
class InputPreferLiteralSuffixCastInBracelessIfBodySliceViolation {
	long m(boolean flag, int x, long total) {
		if (flag)
			total = x * 100L;
		return total;
	}
}
// === end ===

// === case: cast_in_for_condition ===
class InputPreferLiteralSuffixCastInForConditionSliceViolation {
	long m(long total) {
		for (int i = 1; i < 100L; ++i)
			total += i;
		return total;
	}
}
// === end ===

// === case: cast_in_multi_declarator ===
class InputPreferLiteralSuffixCastInMultiDeclaratorSliceViolation {
	long m(int x) {
		final long a = x * 100L, b = 2;
		return a + b;
	}
}
// === end ===

// === case: cast_in_switch_rule ===
class InputPreferLiteralSuffixCastInSwitchRuleSliceViolation {
	long m(int sel, int x) {
		return switch (sel) {
			case 1 -> x * 100L;
			default -> 0;
		};
	}
}
// === end ===

// === case: cast_in_ternary_condition ===
class InputPreferLiteralSuffixCastInTernaryConditionSliceViolation {
	long m(int x) {
		return (x > 100L) ? 1 : 2;
	}
}
// === end ===

// === case: cast_inside_anonymous_class ===
class InputPreferLiteralSuffixCastInsideAnonymousClassSliceViolation {
	interface Calc {
		long value();
	}

	Calc m(int x) {
		return new Calc() {
			@Override
			public long value() {
				return x * 100L;
			}
		};
	}
}
// === end ===

// === case: cast_inside_block_lambda ===
class InputPreferLiteralSuffixCastInsideBlockLambdaSliceViolation {
	interface Calc {
		long value();
	}

	Calc m(int x) {
		return () -> {
			final long base = x * 100L;
			return base + 1;
		};
	}
}
// === end ===

// === case: cast_inside_lambda_body ===
class InputPreferLiteralSuffixCastInsideLambdaBodySliceViolation {
	interface Calc {
		long value();
	}

	Calc m(int x) {
		return () -> x * 100L;
	}
}
// === end ===

// === case: cast_inside_static_initializer ===
class InputPreferLiteralSuffixCastInsideStaticInitializerSliceViolation {
	private static final int BASE = 7;

	private static final long TOTAL;

	static {
		TOTAL = BASE * 100L;
	}

	long m() {
		return TOTAL;
	}
}
// === end ===

// === case: cast_subject_array_access ===
class InputPreferLiteralSuffixCastSubjectArrayAccessSliceViolation {
	long m(int[] arr, int i) {
		return arr[i] * 100L;
	}
}
// === end ===

// === case: cast_subject_bitwise_not ===
class InputPreferLiteralSuffixCastSubjectBitwiseNotSliceViolation {
	long m(int x) {
		return ~x * 100L;
	}
}
// === end ===

// === case: cast_subject_chained_call ===
class InputPreferLiteralSuffixCastSubjectChainedCallSliceViolation {
	long m(String s) {
		return s.trim().length() * 100L;
	}
}
// === end ===

// === case: cast_subject_char_literal_paren ===
class InputPreferLiteralSuffixCastSubjectCharLiteralParenSliceViolation {
	long m(String s) {
		return s.indexOf(')') * 100L;
	}
}
// === end ===

// === case: cast_subject_char_variable ===
class InputPreferLiteralSuffixCastSubjectCharVariableSliceViolation {
	long m(char c) {
		return c * 100L;
	}
}
// === end ===

// === case: cast_subject_float_under_double_cast ===
class InputPreferLiteralSuffixCastSubjectFloatUnderDoubleCastSliceViolation {
	double m(float f) {
		return f * 100d;
	}
}
// === end ===

// === case: cast_subject_long_under_double_cast ===
class InputPreferLiteralSuffixCastSubjectLongUnderDoubleCastSliceViolation {
	double m(long v) {
		return v * 100d;
	}
}
// === end ===

// === case: cast_subject_long_under_float_cast ===
class InputPreferLiteralSuffixCastSubjectLongUnderFloatCastSliceViolation {
	float m(long v) {
		return v * 100f;
	}
}
// === end ===

// === case: cast_subject_long_variable ===
class InputPreferLiteralSuffixCastSubjectLongVariableSliceViolation {
	long m(long v) {
		return v * 100L;
	}
}
// === end ===

// === case: cast_subject_method_call ===
class InputPreferLiteralSuffixCastSubjectMethodCallSliceViolation {
	int len(String s) {
		return s.length();
	}

	long m(String s) {
		return len(s) * 100L;
	}
}
// === end ===

// === case: cast_subject_nested_array_access ===
class InputPreferLiteralSuffixCastSubjectNestedArrayAccessSliceViolation {
	long m(int[][] grid, int i, int j) {
		return grid[i][j] * 100L;
	}
}
// === end ===

// === case: cast_subject_nested_cast ===
class InputPreferLiteralSuffixCastSubjectNestedCastSliceViolation {
	long m(long x) {
		return (int) x * 100L;
	}
}
// === end ===

// === case: cast_subject_nested_field_access ===
class InputPreferLiteralSuffixCastSubjectNestedFieldAccessSliceViolation {
	long m(int[][] grid) {
		return grid[0].length * 100L;
	}
}
// === end ===

// === case: cast_subject_nested_parenthesized_binary ===
class InputPreferLiteralSuffixCastSubjectNestedParenthesizedBinarySliceViolation {
	long m(int a, int b, int c) {
		return ((a + b) + c) * 100L;
	}
}
// === end ===

// === case: cast_subject_parenthesized_binary ===
class InputPreferLiteralSuffixCastSubjectParenthesizedBinarySliceViolation {
	long m(int x, int y) {
		return (x + y) * 100L;
	}
}
// === end ===

// === case: cast_subject_prefix_increment ===
class InputPreferLiteralSuffixCastSubjectPrefixIncrementSliceViolation {
	long m(int i) {
		return ++i * 100L;
	}
}
// === end ===

// === case: cast_subject_split_across_lines ===
class InputPreferLiteralSuffixCastSubjectSplitAcrossLinesSliceViolation {
	long m(String s) {
		return s.length() * 100L;
	}
}
// === end ===

// === case: cast_subject_string_with_operator ===
class InputPreferLiteralSuffixCastSubjectStringWithOperatorSliceViolation {
	long m() {
		return "a*b".indexOf('*') * 100L;
	}
}
// === end ===

// === case: cast_subject_string_with_paren ===
class InputPreferLiteralSuffixCastSubjectStringWithParenSliceViolation {
	long m() {
		return "a)b".length() * 100L;
	}
}
// === end ===

// === case: cast_subject_text_block ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixCastSubjectTextBlockSliceViolation {
	long m() {
		return (long) """
				ab""".length() * 100;
	}
}
// === end ===

// === case: cast_subject_this_field ===
class InputPreferLiteralSuffixCastSubjectThisFieldSliceViolation {
	private int value;

	long m(int value) {
		return this.value * 100L + value;
	}
}
// === end ===

// === case: cast_subject_unary_minus ===
class InputPreferLiteralSuffixCastSubjectUnaryMinusSliceViolation {
	long m(int x) {
		return -x * 100L;
	}
}
// === end ===

// === case: cast_to_double_multiply ===
class InputPreferLiteralSuffixCastToDoubleMultiplySliceViolation {
	double m(int x) {
		return x * 100d;
	}
}
// === end ===

// === case: cast_to_float_multiply ===
class InputPreferLiteralSuffixCastToFloatMultiplySliceViolation {
	float m(int x) {
		return x * 100f;
	}
}
// === end ===

// === case: cast_to_long_add ===
class InputPreferLiteralSuffixCastToLongAddSliceViolation {
	long m(int x) {
		return x + 100L;
	}
}
// === end ===

// === case: cast_to_long_bitwise_and ===
class InputPreferLiteralSuffixCastToLongBitwiseAndSliceViolation {
	long m(int x) {
		return x & 255L;
	}
}
// === end ===

// === case: cast_to_long_bitwise_or ===
class InputPreferLiteralSuffixCastToLongBitwiseOrSliceViolation {
	long m(int x) {
		return x | 255L;
	}
}
// === end ===

// === case: cast_to_long_bitwise_xor ===
class InputPreferLiteralSuffixCastToLongBitwiseXorSliceViolation {
	long m(int x) {
		return x ^ 255L;
	}
}
// === end ===

// === case: cast_to_long_divide ===
class InputPreferLiteralSuffixCastToLongDivideSliceViolation {
	long m(int x) {
		return x / 10L;
	}
}
// === end ===

// === case: cast_to_long_equals ===
class InputPreferLiteralSuffixCastToLongEqualsSliceViolation {
	boolean m(int x) {
		return x == 100L;
	}
}
// === end ===

// === case: cast_to_long_greater_than ===
class InputPreferLiteralSuffixCastToLongGreaterThanSliceViolation {
	boolean m(int x) {
		return x > 100L;
	}
}
// === end ===

// === case: cast_to_long_greater_than_or_equal ===
class InputPreferLiteralSuffixCastToLongGreaterThanOrEqualSliceViolation {
	boolean m(int x) {
		return x >= 100L;
	}
}
// === end ===

// === case: cast_to_long_less_than ===
class InputPreferLiteralSuffixCastToLongLessThanSliceViolation {
	boolean m(int x) {
		return x < 100L;
	}
}
// === end ===

// === case: cast_to_long_less_than_or_equal ===
class InputPreferLiteralSuffixCastToLongLessThanOrEqualSliceViolation {
	boolean m(int x) {
		return x <= 100L;
	}
}
// === end ===

// === case: cast_to_long_modulo ===
class InputPreferLiteralSuffixCastToLongModuloSliceViolation {
	long m(int x) {
		return x % 7L;
	}
}
// === end ===

// === case: cast_to_long_multiply ===
class InputPreferLiteralSuffixCastToLongMultiplySliceViolation {
	long m(int x) {
		return x * 100L;
	}
}
// === end ===

// === case: cast_to_long_not_equal ===
class InputPreferLiteralSuffixCastToLongNotEqualSliceViolation {
	boolean m(int x) {
		return x != 100L;
	}
}
// === end ===

// === case: cast_to_long_subtract ===
class InputPreferLiteralSuffixCastToLongSubtractSliceViolation {
	long m(int x) {
		return x - 50L;
	}
}
// === end ===

// === case: comment_before_cast ===
class InputPreferLiteralSuffixCommentBeforeCastSliceViolation {
	long m(int x) {
		return /* c */ x * 100L;
	}
}
// === end ===

// === case: comment_between_cast_and_subject ===
class InputPreferLiteralSuffixCommentBetweenCastAndSubjectSliceViolation {
	long m(int x) {
		return x * 100L; // c
	}
}
// === end ===

// === case: comment_between_operator_and_literal ===
class InputPreferLiteralSuffixCommentBetweenOperatorAndLiteralSliceViolation {
	long m(int x) {
		return x * /* c */ 100L;
	}
}
// === end ===

// === case: comment_between_subject_and_operator ===
class InputPreferLiteralSuffixCommentBetweenSubjectAndOperatorSliceViolation {
	long m(int x) {
		return x /* c */ * 100L;
	}
}
// === end ===

// === case: comment_in_folded_subject ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixCommentInFoldedSubjectSliceViolation {
	long m(int x) {
		return (long) x /* c
			*/ * 100;
	}
}
// === end ===

// === case: comment_inside_cast_parens ===
class InputPreferLiteralSuffixCommentInsideCastParensSliceViolation {
	long m(int x) {
		return x * 100L; // c
	}
}
// === end ===

// === case: comment_trailing_the_line ===
class InputPreferLiteralSuffixCommentTrailingTheLineSliceViolation {
	long m(int x) {
		return x * 100L; // keep me
	}
}
// === end ===

// === case: complex_cast_subject_field_access ===
class InputPreferLiteralSuffixComplexCastSubjectFieldAccessSliceViolation {
	long m(int[] arr) {
		return arr.length * 100L;
	}
}
// === end ===

// === case: decimal_underscores_with_double_cast ===
class InputPreferLiteralSuffixDecimalUnderscoresWithDoubleCastSliceViolation {
	double m(int x) {
		return x * 1_000d;
	}
}
// === end ===

// === case: displaced_comment_with_open_literal_tail ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixDisplacedCommentWithOpenLiteralTailSliceViolation {
	long m(int x) {
		return (/* c */ long) x * 100; /* tail
			*/
	}
}
// === end ===

// === case: folded_line_over_budget ===
// skip-reason: cannot replace the cast with a suffix: the rewritten expression would exceed the line length limit
class InputPreferLiteralSuffixFoldedLineOverBudgetSliceViolation {
	long m(int aLongParameterName, int anotherLongParameterName, int aThirdLongParameterName) {
		return (long) (aLongParameterName + anotherLongParameterName + aThirdLongParameterName
				+ aLongParameterName + anotherLongParameterName) * 100;
	}
}
// === end ===

// === case: hex_literal ===
class InputPreferLiteralSuffixHexLiteralSliceViolation {
	long m(int x) {
		return x * 0xFFL;
	}
}
// === end ===

// === case: hex_literal_positive_31bit ===
class InputPreferLiteralSuffixHexLiteralPositive31BitSliceViolation {
	long m(int x) {
		return x | 0x7FFFFFFFL;
	}
}
// === end ===

// === case: hex_literal_with_underscores ===
class InputPreferLiteralSuffixHexLiteralWithUnderscoresSliceViolation {
	long m(int x) {
		return x | 0xFF_FFL;
	}
}
// === end ===

// === case: line_comment_before_next_line_subject ===
class InputPreferLiteralSuffixLineCommentBeforeNextLineSubjectSliceViolation {
	long m(int x) {
		return x * 100L; // c
	}
}
// === end ===

// === case: line_comment_inside_multi_line_cast ===
class InputPreferLiteralSuffixLineCommentInsideMultiLineCastSliceViolation {
	long m(int x) {
		return x * 100L; // c
	}
}
// === end ===

// === case: literal_binary_underscores ===
class InputPreferLiteralSuffixLiteralBinaryUnderscoresSliceViolation {
	long m(int x) {
		return x | 0b1010_1010L;
	}
}
// === end ===

// === case: literal_decimal_underscores ===
class InputPreferLiteralSuffixLiteralDecimalUnderscoresSliceViolation {
	long m(int x) {
		return x * 1_000L;
	}
}
// === end ===

// === case: literal_integer_max_value ===
class InputPreferLiteralSuffixLiteralIntegerMaxValueSliceViolation {
	long m(int x) {
		return x * 2147483647L;
	}
}
// === end ===

// === case: literal_integer_min_value ===
class InputPreferLiteralSuffixLiteralIntegerMinValueSliceViolation {
	long m(int x) {
		return x * -2147483648L;
	}
}
// === end ===

// === case: literal_octal ===
class InputPreferLiteralSuffixLiteralOctalSliceViolation {
	long m(int x) {
		return x * 010L;
	}
}
// === end ===

// === case: literal_on_left_arithmetic ===
class InputPreferLiteralSuffixLiteralOnLeftArithmeticSliceViolation {
	long m(int x) {
		return 100L + x;
	}
}
// === end ===

// === case: literal_on_left_bitwise ===
class InputPreferLiteralSuffixLiteralOnLeftBitwiseSliceViolation {
	long m(int x) {
		return 255L & x;
	}
}
// === end ===

// === case: literal_on_left_comparison ===
class InputPreferLiteralSuffixLiteralOnLeftComparisonSliceViolation {
	boolean m(int x) {
		return 100L < x;
	}
}
// === end ===

// === case: literal_on_left_double ===
class InputPreferLiteralSuffixLiteralOnLeftDoubleSliceViolation {
	double m(int x) {
		return 100d * x;
	}
}
// === end ===

// === case: literal_on_left_equals ===
class InputPreferLiteralSuffixLiteralOnLeftEqualsSliceViolation {
	boolean m(int x) {
		return 100L == x;
	}
}
// === end ===

// === case: literal_on_left_float ===
class InputPreferLiteralSuffixLiteralOnLeftFloatSliceViolation {
	float m(int x) {
		return 100f * x;
	}
}
// === end ===

// === case: literal_on_left_greater_than ===
class InputPreferLiteralSuffixLiteralOnLeftGreaterThanSliceViolation {
	boolean m(int x) {
		return 100L > x;
	}
}
// === end ===

// === case: literal_on_left_greater_than_or_equal ===
class InputPreferLiteralSuffixLiteralOnLeftGreaterThanOrEqualSliceViolation {
	boolean m(int x) {
		return 100L >= x;
	}
}
// === end ===

// === case: literal_on_left_less_than_or_equal ===
class InputPreferLiteralSuffixLiteralOnLeftLessThanOrEqualSliceViolation {
	boolean m(int x) {
		return 100L <= x;
	}
}
// === end ===

// === case: literal_on_left_not_equal ===
class InputPreferLiteralSuffixLiteralOnLeftNotEqualSliceViolation {
	boolean m(int x) {
		return 100L != x;
	}
}
// === end ===

// === case: literal_on_line_above ===
class InputPreferLiteralSuffixLiteralOnLineAboveSliceViolation {
	long m(int x) {
		return 100L + x;
	}
}
// === end ===

// === case: literal_on_next_line ===
class InputPreferLiteralSuffixLiteralOnNextLineSliceViolation {
	long m(int x) {
		return x * 100L;
	}
}
// === end ===

// === case: literal_sign_with_space ===
class InputPreferLiteralSuffixLiteralSignWithSpaceSliceViolation {
	long m(int x) {
		return x * - 100L;
	}
}
// === end ===

// === case: multi_line_cast ===
class InputPreferLiteralSuffixMultiLineCastSliceViolation {
	long m(int x) {
		return x * 100L;
	}
}
// === end ===

// === case: multi_line_cast_literal_on_left ===
class InputPreferLiteralSuffixMultiLineCastLiteralOnLeftSliceViolation {
	long m(int x) {
		return 100L + x;
	}
}
// === end ===

// === case: multi_line_ternary ===
class InputPreferLiteralSuffixMultiLineTernarySliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : 0L;
	}
}
// === end ===

// === case: negated_literal_left ===
class InputPreferLiteralSuffixNegatedLiteralLeftSliceViolation {
	long m(int x) {
		return -100L + x;
	}
}
// === end ===

// === case: negated_literal_right ===
class InputPreferLiteralSuffixNegatedLiteralRightSliceViolation {
	long m(int x) {
		return x * -100L;
	}
}
// === end ===

// === case: negated_literal_with_double_cast ===
class InputPreferLiteralSuffixNegatedLiteralWithDoubleCastSliceViolation {
	double m(int x) {
		return x * -100d;
	}
}
// === end ===

// === case: negated_zero_with_long_cast ===
class InputPreferLiteralSuffixNegatedZeroWithLongCastSliceViolation {
	long m(int x) {
		return x * -0L;
	}
}
// === end ===

// === case: nested_same_type_cast ===
class InputPreferLiteralSuffixNestedSameTypeCastSliceViolation {
	long m(int x) {
		return (long) x * 100L;
	}
}
// === end ===

// === case: non_identifier_cast_subject ===
class InputPreferLiteralSuffixNonIdentifierCastSubjectSliceViolation {
	long m(int x) {
		return (x) * 100L;
	}
}
// === end ===

// === case: operator_on_next_line ===
class InputPreferLiteralSuffixOperatorOnNextLineSliceViolation {
	long m(int x) {
		return x * 100L;
	}
}
// === end ===

// === case: positive_unary_literal_left ===
class InputPreferLiteralSuffixPositiveUnaryLiteralLeftSliceViolation {
	long m(int x) {
		return +100L + x;
	}
}
// === end ===

// === case: positive_unary_literal_right ===
class InputPreferLiteralSuffixPositiveUnaryLiteralRightSliceViolation {
	long m(int x) {
		return x * +100L;
	}
}
// === end ===

// === case: positive_zero_with_double_cast ===
class InputPreferLiteralSuffixPositiveZeroWithDoubleCastSliceViolation {
	double m(int x) {
		return x * +0d;
	}
}
// === end ===

// === case: supplementary_codepoint_in_subject ===
class InputPreferLiteralSuffixSupplementaryCodepointInSubjectSliceViolation {
	long m(int x𝟙) {
		return x𝟙 * 100L;
	}
}
// === end ===

// === case: ternary_cast_double ===
class InputPreferLiteralSuffixTernaryCastDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? x : 0d;
	}
}
// === end ===

// === case: ternary_cast_float ===
class InputPreferLiteralSuffixTernaryCastFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? x : 0f;
	}
}
// === end ===

// === case: ternary_cast_on_false ===
class InputPreferLiteralSuffixTernaryCastOnFalseSliceViolation {
	long m(boolean flag, int x) {
		return flag ? 0L : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_double ===
class InputPreferLiteralSuffixTernaryCastOnFalseDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? 0d : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_float ===
class InputPreferLiteralSuffixTernaryCastOnFalseFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? 0f : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_with_negative_literal_left ===
class InputPreferLiteralSuffixTernaryCastOnFalseWithNegativeLiteralLeftSliceViolation {
	long m(boolean flag, int x) {
		return flag ? -1L : x;
	}
}
// === end ===

// === case: ternary_cast_on_true ===
class InputPreferLiteralSuffixTernaryCastOnTrueSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : 0L;
	}
}
// === end ===

// === case: ternary_cast_with_negative_literal ===
class InputPreferLiteralSuffixTernaryCastWithNegativeLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : -1L;
	}
}
// === end ===

// === case: ternary_cast_with_positive_literal ===
class InputPreferLiteralSuffixTernaryCastWithPositiveLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : +1L;
	}
}
// === end ===

// === case: two_casts_on_one_line ===
// multi-fix-expected
class InputPreferLiteralSuffixTwoCastsOnOneLineSliceViolation {
	long m(int a, int b) {
		return a * 100L + b * 200L;
	}
}
// === end ===

// === case: unrelated_wrap_outside_the_span ===
class InputPreferLiteralSuffixUnrelatedWrapOutsideTheSpanSliceViolation {
	int len(int a, int b) {
		return a + b;
	}

	long m(int a, int b, int x) {
		return len(
				a,
				b
		) + x * 100L;
	}
}
// === end ===