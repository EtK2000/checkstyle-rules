package com.etk2000.checkstyle.inputs.preferliteralsuffix;

// === case: binary_literal_small ===
class InputPreferLiteralSuffixBinaryLiteralSmallSliceViolation {
	long m(int x) {
		return (long) x | 0b1010; // violation: Use 'L' suffix on '0b1010' instead of a cast.
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

// === case: complex_cast_subject_field_access ===
// skip-reason: complex-cast-subject
class InputPreferLiteralSuffixComplexCastSubjectFieldAccessSliceViolation {
	long m(int[] arr) {
		return (long) arr.length * 100; // violation: Use 'L' suffix on '100' instead of a cast.
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

// === case: literal_on_left_float ===
class InputPreferLiteralSuffixLiteralOnLeftFloatSliceViolation {
	float m(int x) {
		return 100 * (float) x; // violation: Use 'f' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: multi_line_cast ===
// skip-reason: multi-line-cast
class InputPreferLiteralSuffixMultiLineCastSliceViolation {
	long m(int x) {
		return (long // violation: Use 'L' suffix on '100' instead of a cast.
			) x * 100;
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

// === case: non_identifier_cast_subject ===
// skip-reason: non-identifier-cast-subject
class InputPreferLiteralSuffixNonIdentifierCastSubjectSliceViolation {
	long m(int x) {
		return (long) (x) * 100; // violation: Use 'L' suffix on '100' instead of a cast.
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

// === case: supplementary_codepoint_in_subject ===
class InputPreferLiteralSuffixSupplementaryCodepointInSubjectSliceViolation {
	long m(int x𝟙) {
		return (long) x𝟙 * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}
}
// === end ===

// === case: supplementary_non_identifier_terminator ===
// skip-reason: complex-cast-subject
class InputPreferLiteralSuffixSupplementaryNonIdentifierTerminatorSliceViolation {
	long m(int x) {
		return (long) x😀 + 100; // violation: Use 'L' suffix on '100' instead of a cast.
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