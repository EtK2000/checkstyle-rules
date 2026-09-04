package com.etk2000.checkstyle.inputs.preferliteralsuffix;

// === case: annotated_parameter_subject ===
class InputPreferLiteralSuffixAnnotatedParameterSubjectSliceViolation {
	long m(@Deprecated int x) {
		return (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: binary_literal_small ===
class InputPreferLiteralSuffixBinaryLiteralSmallSliceViolation {
	long m(int x) {
		return (long) x | 0b1010; // violation: Use 'L' suffix on '0b1010' instead of a cast.
	}
}
// === end ===

// === case: block_comment_spanning_the_fold ===
class InputPreferLiteralSuffixBlockCommentSpanningTheFoldSliceViolation {
	long m(int x) {
		return (long /* c // violation: Use 'L' suffix on '100' instead of a cast.
			*/ ) x * 100;
	}
}
// === end ===

// === case: cast_at_end_of_line_subject_next ===
class InputPreferLiteralSuffixCastAtEndOfLineSubjectNextSliceViolation {
	long m(int x) {
		return (long) // violation: Use 'L' suffix on '100' instead of a cast.
			x * 100;
	}
}
// === end ===

// === case: cast_in_braceless_if_body ===
class InputPreferLiteralSuffixCastInBracelessIfBodySliceViolation {
	long m(boolean flag, int x, long total) {
		if (flag)
			total = (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
		return total;
	}
}
// === end ===

// === case: cast_in_for_condition ===
class InputPreferLiteralSuffixCastInForConditionSliceViolation {
	long m(long total) {
		for (int i = 1; (long) i < 100; ++i) // violation: Use 'L' suffix on '100' instead of a cast.
			total += i;
		return total;
	}
}
// === end ===

// === case: cast_in_multi_declarator ===
class InputPreferLiteralSuffixCastInMultiDeclaratorSliceViolation {
	long m(int x) {
		final long a = (long) x * 100, b = 2; // violation: Use 'L' suffix on '100' instead of a cast.
		return a + b;
	}
}
// === end ===

// === case: cast_in_switch_rule ===
class InputPreferLiteralSuffixCastInSwitchRuleSliceViolation {
	long m(int sel, int x) {
		return switch (sel) {
			case 1 -> (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
			default -> 0;
		};
	}
}
// === end ===

// === case: cast_in_ternary_condition ===
class InputPreferLiteralSuffixCastInTernaryConditionSliceViolation {
	long m(int x) {
		return ((long) x > 100) ? 1 : 2; // violation: Use 'L' suffix on '100' instead of a cast.
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
				return (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
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
			final long base = (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
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
		return () -> (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_inside_static_initializer ===
class InputPreferLiteralSuffixCastInsideStaticInitializerSliceViolation {
	private static final int BASE = 7;

	private static final long TOTAL;

	static {
		TOTAL = (long) BASE * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	long m() {
		return TOTAL;
	}
}
// === end ===

// === case: cast_subject_array_access ===
class InputPreferLiteralSuffixCastSubjectArrayAccessSliceViolation {
	long m(int[] arr, int i) {
		return (long) arr[i] * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_bitwise_not ===
class InputPreferLiteralSuffixCastSubjectBitwiseNotSliceViolation {
	long m(int x) {
		return (long) ~x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_chained_call ===
class InputPreferLiteralSuffixCastSubjectChainedCallSliceViolation {
	long m(String s) {
		return (long) s.trim().length() * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_char_literal_paren ===
class InputPreferLiteralSuffixCastSubjectCharLiteralParenSliceViolation {
	long m(String s) {
		return (long) s.indexOf(')') * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_char_variable ===
class InputPreferLiteralSuffixCastSubjectCharVariableSliceViolation {
	long m(char c) {
		return (long) c * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_float_under_double_cast ===
class InputPreferLiteralSuffixCastSubjectFloatUnderDoubleCastSliceViolation {
	double m(float f) {
		return (double) f * 100; // violation: Use 'd' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_long_under_double_cast ===
class InputPreferLiteralSuffixCastSubjectLongUnderDoubleCastSliceViolation {
	double m(long v) {
		return (double) v * 100; // violation: Use 'd' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_long_under_float_cast ===
class InputPreferLiteralSuffixCastSubjectLongUnderFloatCastSliceViolation {
	float m(long v) {
		return (float) v * 100; // violation: Use 'f' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_long_variable ===
class InputPreferLiteralSuffixCastSubjectLongVariableSliceViolation {
	long m(long v) {
		return (long) v * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_method_call ===
class InputPreferLiteralSuffixCastSubjectMethodCallSliceViolation {
	int len(String s) {
		return s.length();
	}

	long m(String s) {
		return (long) len(s) * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_nested_array_access ===
class InputPreferLiteralSuffixCastSubjectNestedArrayAccessSliceViolation {
	long m(int[][] grid, int i, int j) {
		return (long) grid[i][j] * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_nested_cast ===
class InputPreferLiteralSuffixCastSubjectNestedCastSliceViolation {
	long m(long x) {
		return (long) (int) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_nested_field_access ===
class InputPreferLiteralSuffixCastSubjectNestedFieldAccessSliceViolation {
	long m(int[][] grid) {
		return (long) grid[0].length * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_nested_parenthesized_binary ===
class InputPreferLiteralSuffixCastSubjectNestedParenthesizedBinarySliceViolation {
	long m(int a, int b, int c) {
		return (long) ((a + b) + c) * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_parenthesized_binary ===
class InputPreferLiteralSuffixCastSubjectParenthesizedBinarySliceViolation {
	long m(int x, int y) {
		return (long) (x + y) * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_prefix_increment ===
class InputPreferLiteralSuffixCastSubjectPrefixIncrementSliceViolation {
	long m(int i) {
		return (long) ++i * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_split_across_lines ===
class InputPreferLiteralSuffixCastSubjectSplitAcrossLinesSliceViolation {
	long m(String s) {
		return (long) s // violation: Use 'L' suffix on '100' instead of a cast.
			.length() * 100;
	}
}
// === end ===

// === case: cast_subject_string_with_operator ===
class InputPreferLiteralSuffixCastSubjectStringWithOperatorSliceViolation {
	long m() {
		return (long) "a*b".indexOf('*') * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_string_with_paren ===
class InputPreferLiteralSuffixCastSubjectStringWithParenSliceViolation {
	long m() {
		return (long) "a)b".length() * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_text_block ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixCastSubjectTextBlockSliceViolation {
	long m() {
		return (long) """ // violation: Use 'L' suffix on '100' instead of a cast.
				ab""".length() * 100;
	}
}
// === end ===

// === case: cast_subject_this_field ===
class InputPreferLiteralSuffixCastSubjectThisFieldSliceViolation {
	private int value;

	long m(int value) {
		return (long) this.value * 100 + value; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_subject_unary_minus ===
class InputPreferLiteralSuffixCastSubjectUnaryMinusSliceViolation {
	long m(int x) {
		return (long) -x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_double_multiply ===
class InputPreferLiteralSuffixCastToDoubleMultiplySliceViolation {
	double m(int x) {
		return (double) x * 100; // violation: Use 'd' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_float_multiply ===
class InputPreferLiteralSuffixCastToFloatMultiplySliceViolation {
	float m(int x) {
		return (float) x * 100; // violation: Use 'f' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_add ===
class InputPreferLiteralSuffixCastToLongAddSliceViolation {
	long m(int x) {
		return (long) x + 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_bitwise_and ===
class InputPreferLiteralSuffixCastToLongBitwiseAndSliceViolation {
	long m(int x) {
		return (long) x & 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_bitwise_or ===
class InputPreferLiteralSuffixCastToLongBitwiseOrSliceViolation {
	long m(int x) {
		return (long) x | 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_bitwise_xor ===
class InputPreferLiteralSuffixCastToLongBitwiseXorSliceViolation {
	long m(int x) {
		return (long) x ^ 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_divide ===
class InputPreferLiteralSuffixCastToLongDivideSliceViolation {
	long m(int x) {
		return (long) x / 10; // violation: Use 'L' suffix on '10' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_equals ===
class InputPreferLiteralSuffixCastToLongEqualsSliceViolation {
	boolean m(int x) {
		return (long) x == 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_greater_than ===
class InputPreferLiteralSuffixCastToLongGreaterThanSliceViolation {
	boolean m(int x) {
		return (long) x > 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_greater_than_or_equal ===
class InputPreferLiteralSuffixCastToLongGreaterThanOrEqualSliceViolation {
	boolean m(int x) {
		return (long) x >= 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_less_than ===
class InputPreferLiteralSuffixCastToLongLessThanSliceViolation {
	boolean m(int x) {
		return (long) x < 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_less_than_or_equal ===
class InputPreferLiteralSuffixCastToLongLessThanOrEqualSliceViolation {
	boolean m(int x) {
		return (long) x <= 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_modulo ===
class InputPreferLiteralSuffixCastToLongModuloSliceViolation {
	long m(int x) {
		return (long) x % 7; // violation: Use 'L' suffix on '7' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_multiply ===
class InputPreferLiteralSuffixCastToLongMultiplySliceViolation {
	long m(int x) {
		return (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_not_equal ===
class InputPreferLiteralSuffixCastToLongNotEqualSliceViolation {
	boolean m(int x) {
		return (long) x != 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: cast_to_long_subtract ===
class InputPreferLiteralSuffixCastToLongSubtractSliceViolation {
	long m(int x) {
		return (long) x - 50; // violation: Use 'L' suffix on '50' instead of a cast.
	}
}
// === end ===

// === case: comment_before_cast ===
class InputPreferLiteralSuffixCommentBeforeCastSliceViolation {
	long m(int x) {
		return /* c */ (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: comment_between_cast_and_subject ===
class InputPreferLiteralSuffixCommentBetweenCastAndSubjectSliceViolation {
	long m(int x) {
		return (long) /* c */ x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: comment_between_operator_and_literal ===
class InputPreferLiteralSuffixCommentBetweenOperatorAndLiteralSliceViolation {
	long m(int x) {
		return (long) x * /* c */ 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: comment_between_subject_and_operator ===
class InputPreferLiteralSuffixCommentBetweenSubjectAndOperatorSliceViolation {
	long m(int x) {
		return (long) x /* c */ * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: comment_in_folded_subject ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixCommentInFoldedSubjectSliceViolation {
	long m(int x) {
		return (long) x /* c // violation: Use 'L' suffix on '100' instead of a cast.
			*/ * 100;
	}
}
// === end ===

// === case: comment_inside_cast_parens ===
class InputPreferLiteralSuffixCommentInsideCastParensSliceViolation {
	long m(int x) {
		return (/* c */ long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: comment_trailing_the_line ===
class InputPreferLiteralSuffixCommentTrailingTheLineSliceViolation {
	long m(int x) {
		return (long) x * 100; // keep me // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: complex_cast_subject_field_access ===
class InputPreferLiteralSuffixComplexCastSubjectFieldAccessSliceViolation {
	long m(int[] arr) {
		return (long) arr.length * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: decimal_underscores_with_double_cast ===
class InputPreferLiteralSuffixDecimalUnderscoresWithDoubleCastSliceViolation {
	double m(int x) {
		return (double) x * 1_000; // violation: Use 'd' suffix on '1_000' instead of a cast.
	}
}
// === end ===

// === case: displaced_comment_with_open_literal_tail ===
// skip-reason: cannot replace the cast with a suffix: a text block or comment inside the expression cannot survive the rewrite
class InputPreferLiteralSuffixDisplacedCommentWithOpenLiteralTailSliceViolation {
	long m(int x) {
		return (/* c */ long) x * 100; /* tail // violation: Use 'L' suffix on '100' instead of a cast.
			*/
	}
}
// === end ===

// === case: folded_line_over_budget ===
// skip-reason: cannot replace the cast with a suffix: the rewritten expression would exceed the line length limit
class InputPreferLiteralSuffixFoldedLineOverBudgetSliceViolation {
	long m(int aLongParameterName, int anotherLongParameterName, int aThirdLongParameterName) {
		return (long) (aLongParameterName + anotherLongParameterName + aThirdLongParameterName // violation: Use 'L' suffix on '100' instead of a cast.
				+ aLongParameterName + anotherLongParameterName) * 100;
	}
}
// === end ===

// === case: hex_literal ===
class InputPreferLiteralSuffixHexLiteralSliceViolation {
	long m(int x) {
		return (long) x * 0xFF; // violation: Use 'L' suffix on '0xFF' instead of a cast.
	}
}
// === end ===

// === case: hex_literal_positive_31bit ===
class InputPreferLiteralSuffixHexLiteralPositive31BitSliceViolation {
	long m(int x) {
		return (long) x | 0x7FFFFFFF; // violation: Use 'L' suffix on '0x7FFFFFFF' instead of a cast.
	}
}
// === end ===

// === case: hex_literal_with_underscores ===
class InputPreferLiteralSuffixHexLiteralWithUnderscoresSliceViolation {
	long m(int x) {
		return (long) x | 0xFF_FF; // violation: Use 'L' suffix on '0xFF_FF' instead of a cast.
	}
}
// === end ===

// === case: line_comment_before_next_line_subject ===
class InputPreferLiteralSuffixLineCommentBeforeNextLineSubjectSliceViolation {
	long m(int x) {
		return (long) // c // violation: Use 'L' suffix on '100' instead of a cast.
			x * 100;
	}
}
// === end ===

// === case: line_comment_inside_multi_line_cast ===
class InputPreferLiteralSuffixLineCommentInsideMultiLineCastSliceViolation {
	long m(int x) {
		return (long // c // violation: Use 'L' suffix on '100' instead of a cast.
			) x * 100;
	}
}
// === end ===

// === case: literal_binary_underscores ===
class InputPreferLiteralSuffixLiteralBinaryUnderscoresSliceViolation {
	long m(int x) {
		return (long) x | 0b1010_1010; // violation: Use 'L' suffix on '0b1010_1010' instead of a cast.
	}
}
// === end ===

// === case: literal_decimal_underscores ===
class InputPreferLiteralSuffixLiteralDecimalUnderscoresSliceViolation {
	long m(int x) {
		return (long) x * 1_000; // violation: Use 'L' suffix on '1_000' instead of a cast.
	}
}
// === end ===

// === case: literal_integer_max_value ===
class InputPreferLiteralSuffixLiteralIntegerMaxValueSliceViolation {
	long m(int x) {
		return (long) x * 2147483647; // violation: Use 'L' suffix on '2147483647' instead of a cast.
	}
}
// === end ===

// === case: literal_integer_min_value ===
class InputPreferLiteralSuffixLiteralIntegerMinValueSliceViolation {
	long m(int x) {
		return (long) x * -2147483648; // violation: Use 'L' suffix on '-2147483648' instead of a cast.
	}
}
// === end ===

// === case: literal_octal ===
class InputPreferLiteralSuffixLiteralOctalSliceViolation {
	long m(int x) {
		return (long) x * 010; // violation: Use 'L' suffix on '010' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_arithmetic ===
class InputPreferLiteralSuffixLiteralOnLeftArithmeticSliceViolation {
	long m(int x) {
		return 100 + (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_bitwise ===
class InputPreferLiteralSuffixLiteralOnLeftBitwiseSliceViolation {
	long m(int x) {
		return 255 & (long) x; // violation: Use 'L' suffix on '255' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_comparison ===
class InputPreferLiteralSuffixLiteralOnLeftComparisonSliceViolation {
	boolean m(int x) {
		return 100 < (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_double ===
class InputPreferLiteralSuffixLiteralOnLeftDoubleSliceViolation {
	double m(int x) {
		return 100 * (double) x; // violation: Use 'd' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_equals ===
class InputPreferLiteralSuffixLiteralOnLeftEqualsSliceViolation {
	boolean m(int x) {
		return 100 == (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_float ===
class InputPreferLiteralSuffixLiteralOnLeftFloatSliceViolation {
	float m(int x) {
		return 100 * (float) x; // violation: Use 'f' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_greater_than ===
class InputPreferLiteralSuffixLiteralOnLeftGreaterThanSliceViolation {
	boolean m(int x) {
		return 100 > (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_greater_than_or_equal ===
class InputPreferLiteralSuffixLiteralOnLeftGreaterThanOrEqualSliceViolation {
	boolean m(int x) {
		return 100 >= (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_less_than_or_equal ===
class InputPreferLiteralSuffixLiteralOnLeftLessThanOrEqualSliceViolation {
	boolean m(int x) {
		return 100 <= (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_left_not_equal ===
class InputPreferLiteralSuffixLiteralOnLeftNotEqualSliceViolation {
	boolean m(int x) {
		return 100 != (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_line_above ===
class InputPreferLiteralSuffixLiteralOnLineAboveSliceViolation {
	long m(int x) {
		return 100
			+ (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: literal_on_next_line ===
class InputPreferLiteralSuffixLiteralOnNextLineSliceViolation {
	long m(int x) {
		return (long) x * // violation: Use 'L' suffix on '100' instead of a cast.
			100;
	}
}
// === end ===

// === case: literal_sign_with_space ===
class InputPreferLiteralSuffixLiteralSignWithSpaceSliceViolation {
	long m(int x) {
		return (long) x * - 100; // violation: Use 'L' suffix on '-100' instead of a cast.
	}
}
// === end ===

// === case: multi_line_cast ===
class InputPreferLiteralSuffixMultiLineCastSliceViolation {
	long m(int x) {
		return (long // violation: Use 'L' suffix on '100' instead of a cast.
			) x * 100;
	}
}
// === end ===

// === case: multi_line_cast_literal_on_left ===
class InputPreferLiteralSuffixMultiLineCastLiteralOnLeftSliceViolation {
	long m(int x) {
		return 100 + ( // violation: Use 'L' suffix on '100' instead of a cast.
			long
			) x;
	}
}
// === end ===

// === case: multi_line_ternary ===
class InputPreferLiteralSuffixMultiLineTernarySliceViolation {
	long m(boolean flag, int x) {
		return flag
			? (long) x // violation: Use 'L' suffix on '0' instead of a cast.
			: 0;
	}
}
// === end ===

// === case: negated_literal_left ===
class InputPreferLiteralSuffixNegatedLiteralLeftSliceViolation {
	long m(int x) {
		return -100 + (long) x; // violation: Use 'L' suffix on '-100' instead of a cast.
	}
}
// === end ===

// === case: negated_literal_right ===
class InputPreferLiteralSuffixNegatedLiteralRightSliceViolation {
	long m(int x) {
		return (long) x * -100; // violation: Use 'L' suffix on '-100' instead of a cast.
	}
}
// === end ===

// === case: negated_literal_with_double_cast ===
class InputPreferLiteralSuffixNegatedLiteralWithDoubleCastSliceViolation {
	double m(int x) {
		return (double) x * -100; // violation: Use 'd' suffix on '-100' instead of a cast.
	}
}
// === end ===

// === case: negated_zero_with_long_cast ===
class InputPreferLiteralSuffixNegatedZeroWithLongCastSliceViolation {
	long m(int x) {
		return (long) x * -0; // violation: Use 'L' suffix on '-0' instead of a cast.
	}
}
// === end ===

// === case: nested_same_type_cast ===
class InputPreferLiteralSuffixNestedSameTypeCastSliceViolation {
	long m(int x) {
		return (long) (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: non_identifier_cast_subject ===
class InputPreferLiteralSuffixNonIdentifierCastSubjectSliceViolation {
	long m(int x) {
		return (long) (x) * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: operator_on_next_line ===
class InputPreferLiteralSuffixOperatorOnNextLineSliceViolation {
	long m(int x) {
		return (long) x // violation: Use 'L' suffix on '100' instead of a cast.
			* 100;
	}
}
// === end ===

// === case: positive_unary_literal_left ===
class InputPreferLiteralSuffixPositiveUnaryLiteralLeftSliceViolation {
	long m(int x) {
		return +100 + (long) x; // violation: Use 'L' suffix on '+100' instead of a cast.
	}
}
// === end ===

// === case: positive_unary_literal_right ===
class InputPreferLiteralSuffixPositiveUnaryLiteralRightSliceViolation {
	long m(int x) {
		return (long) x * +100; // violation: Use 'L' suffix on '+100' instead of a cast.
	}
}
// === end ===

// === case: positive_zero_with_double_cast ===
class InputPreferLiteralSuffixPositiveZeroWithDoubleCastSliceViolation {
	double m(int x) {
		return (double) x * +0; // violation: Use 'd' suffix on '+0' instead of a cast.
	}
}
// === end ===

// === case: supplementary_codepoint_in_subject ===
class InputPreferLiteralSuffixSupplementaryCodepointInSubjectSliceViolation {
	long m(int x𝟙) {
		return (long) x𝟙 * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_double ===
class InputPreferLiteralSuffixTernaryCastDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? (double) x : 0; // violation: Use 'd' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_float ===
class InputPreferLiteralSuffixTernaryCastFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? (float) x : 0; // violation: Use 'f' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_on_false ===
class InputPreferLiteralSuffixTernaryCastOnFalseSliceViolation {
	long m(boolean flag, int x) {
		return flag ? 0 : (long) x; // violation: Use 'L' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_on_false_double ===
class InputPreferLiteralSuffixTernaryCastOnFalseDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? 0 : (double) x; // violation: Use 'd' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_on_false_float ===
class InputPreferLiteralSuffixTernaryCastOnFalseFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? 0 : (float) x; // violation: Use 'f' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_on_false_with_negative_literal_left ===
class InputPreferLiteralSuffixTernaryCastOnFalseWithNegativeLiteralLeftSliceViolation {
	long m(boolean flag, int x) {
		return flag ? -1 : (long) x; // violation: Use 'L' suffix on '-1' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_on_true ===
class InputPreferLiteralSuffixTernaryCastOnTrueSliceViolation {
	long m(boolean flag, int x) {
		return flag ? (long) x : 0; // violation: Use 'L' suffix on '0' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_with_negative_literal ===
class InputPreferLiteralSuffixTernaryCastWithNegativeLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? (long) x : -1; // violation: Use 'L' suffix on '-1' instead of a cast.
	}
}
// === end ===

// === case: ternary_cast_with_positive_literal ===
class InputPreferLiteralSuffixTernaryCastWithPositiveLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? (long) x : +1; // violation: Use 'L' suffix on '+1' instead of a cast.
	}
}
// === end ===

// === case: two_casts_on_one_line ===
// multi-fix-expected
class InputPreferLiteralSuffixTwoCastsOnOneLineSliceViolation {
	long m(int a, int b) {
		return (long) a * 100 + (long) b * 200; // violation: Use 'L' suffix on '100' instead of a cast. // violation: Use 'L' suffix on '200' instead of a cast.
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
		) + (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===