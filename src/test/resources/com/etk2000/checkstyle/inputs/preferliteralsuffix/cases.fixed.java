// === case: cast_to_long_equals ===
class InputPreferLiteralSuffixCastToLongEqualsSliceViolation {
	boolean m(int x) {
		return x == 100;
	}
}
// === end ===

// === case: cast_to_long_greater_than ===
class InputPreferLiteralSuffixCastToLongGreaterThanSliceViolation {
	boolean m(int x) {
		return x > 100;
	}
}
// === end ===

// === case: cast_to_long_greater_than_or_equal ===
class InputPreferLiteralSuffixCastToLongGreaterThanOrEqualSliceViolation {
	boolean m(int x) {
		return x >= 100;
	}
}
// === end ===

// === case: cast_to_long_less_than ===
class InputPreferLiteralSuffixCastToLongLessThanSliceViolation {
	boolean m(int x) {
		return x < 100;
	}
}
// === end ===

// === case: cast_to_long_less_than_or_equal ===
class InputPreferLiteralSuffixCastToLongLessThanOrEqualSliceViolation {
	boolean m(int x) {
		return x <= 100;
	}
}
// === end ===

// === case: cast_to_long_not_equal ===
class InputPreferLiteralSuffixCastToLongNotEqualSliceViolation {
	boolean m(int x) {
		return x != 100;
	}
}
// === end ===

// === case: literal_on_left_comparison ===
class InputPreferLiteralSuffixLiteralOnLeftComparisonSliceViolation {
	boolean m(int x) {
		return 100 < x;
	}
}
// === end ===

// === case: ternary_cast_double ===
class InputPreferLiteralSuffixTernaryCastDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? x : 0;
	}
}
// === end ===

// === case: ternary_cast_float ===
class InputPreferLiteralSuffixTernaryCastFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? x : 0;
	}
}
// === end ===

// === case: ternary_cast_on_false ===
class InputPreferLiteralSuffixTernaryCastOnFalseSliceViolation {
	long m(boolean flag, int x) {
		return flag ? 0 : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_double ===
class InputPreferLiteralSuffixTernaryCastOnFalseDoubleSliceViolation {
	double m(boolean flag, int x) {
		return flag ? 0 : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_float ===
class InputPreferLiteralSuffixTernaryCastOnFalseFloatSliceViolation {
	float m(boolean flag, int x) {
		return flag ? 0 : x;
	}
}
// === end ===

// === case: ternary_cast_on_false_with_negative_literal_left ===
class InputPreferLiteralSuffixTernaryCastOnFalseWithNegativeLiteralLeftSliceViolation {
	long m(boolean flag, int x) {
		return flag ? -1 : x;
	}
}
// === end ===

// === case: ternary_cast_on_true ===
class InputPreferLiteralSuffixTernaryCastOnTrueSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : 0;
	}
}
// === end ===

// === case: ternary_cast_with_negative_literal ===
class InputPreferLiteralSuffixTernaryCastWithNegativeLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : -1;
	}
}
// === end ===

// === case: ternary_cast_with_positive_literal ===
class InputPreferLiteralSuffixTernaryCastWithPositiveLiteralSliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : +1;
	}
}
// === end ===