// === case: cast_in_for_condition ===
class InputPreferLiteralSuffixCastInForConditionSliceViolation {
	long m(long total) {
		for (var i = 1; i < 100L; ++i)
			total += i;
		return total;
	}
}
// === end ===

// === case: multi_line_ternary ===
class InputPreferLiteralSuffixMultiLineTernarySliceViolation {
	long m(boolean flag, int x) {
		return flag ? x : 0;
	}
}
// === end ===

// === case: nested_same_type_cast ===
class InputPreferLiteralSuffixNestedSameTypeCastSliceViolation {
	long m(int x) {
		return x * 100L;
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