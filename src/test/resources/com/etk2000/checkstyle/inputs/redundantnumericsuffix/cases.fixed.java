// === case: cast_long_zero_return ===
class InputRedundantSuffixCastLongZeroReturnSliceViolation {
	long m() {
		return 0;
	}
}
// === end ===

// === case: for_init_decimal_double_suffix ===
class InputRedundantSuffixForInitDecimalDoubleSuffixSliceViolation {
	void m() {
		for (var d = 0.0; d < 1; ++d)
			System.out.println(d);
	}
}
// === end ===

// === case: zero_long_suffix ===
class InputRedundantSuffixZeroLongSliceViolation {
	long x;
}
// === end ===