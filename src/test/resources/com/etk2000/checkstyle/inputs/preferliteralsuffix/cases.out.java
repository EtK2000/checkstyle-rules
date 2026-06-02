package com.etk2000.checkstyle.inputs.preferliteralsuffix;

// === case: binary_literal_small ===
class InputPreferLiteralSuffixBinaryLiteralSmallSliceViolation {
	long m(int x) {
		return x | 0b1010L;
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

// === case: complex_cast_subject_field_access ===
// skip-reason: complex-cast-subject
class InputPreferLiteralSuffixComplexCastSubjectFieldAccessSliceViolation {
	long m(int[] arr) {
		return (long) arr.length * 100;
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

// === case: literal_on_left_float ===
class InputPreferLiteralSuffixLiteralOnLeftFloatSliceViolation {
	float m(int x) {
		return 100f * x;
	}
}
// === end ===

// === case: multi_line_cast ===
// skip-reason: multi-line-cast
class InputPreferLiteralSuffixMultiLineCastSliceViolation {
	long m(int x) {
		return (long
			) x * 100;
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

// === case: non_identifier_cast_subject ===
// skip-reason: non-identifier-cast-subject
class InputPreferLiteralSuffixNonIdentifierCastSubjectSliceViolation {
	long m(int x) {
		return (long) (x) * 100;
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

// === case: supplementary_codepoint_in_subject ===
class InputPreferLiteralSuffixSupplementaryCodepointInSubjectSliceViolation {
	long m(int x𝟙) {
		return x𝟙 * 100L;
	}
}
// === end ===

// === case: supplementary_non_identifier_terminator ===
// skip-reason: complex-cast-subject
class InputPreferLiteralSuffixSupplementaryNonIdentifierTerminatorSliceViolation {
	long m(int x) {
		return (long) x😀 + 100;
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