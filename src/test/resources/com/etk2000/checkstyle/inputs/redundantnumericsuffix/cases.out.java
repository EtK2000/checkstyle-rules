package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

// === case: array_initializer_mixed_float ===
class InputRedundantSuffixArrayInitializerMixedFloatSliceViolation {
	float[] arr = {0, 1.5f};
}
// === end ===

// === case: array_initializer_single_float ===
class InputRedundantSuffixArrayInitializerSingleFloatSliceViolation {
	float[] arr = {0};
}
// === end ===

// === case: binary_long_field ===
class InputRedundantSuffixBinaryLongFieldSliceViolation {
	long x = 0b1010;
}
// === end ===

// === case: cast_long_zero_return ===
class InputRedundantSuffixCastLongZeroReturnSliceViolation {
	long m() {
		return (long) 0;
	}
}
// === end ===

// === case: compound_assignment_long_zero ===
class InputRedundantSuffixCompoundAssignmentLongZeroSliceViolation {
	long x = 100;

	void m() {
		x += 0;
	}
}
// === end ===

// === case: decimal_double_uppercase_field ===
class InputRedundantSuffixDecimalDoubleUppercaseFieldSliceViolation {
	double x = 3.14;
}
// === end ===

// === case: decimal_float_uppercase_field ===
class InputRedundantSuffixDecimalFloatUppercaseFieldSliceViolation {
	float x = 100;
}
// === end ===

// === case: decimal_long_negative_field ===
class InputRedundantSuffixDecimalLongNegativeFieldSliceViolation {
	long x = -1;
}
// === end ===

// === case: decimal_long_underscore_field ===
class InputRedundantSuffixDecimalLongUnderscoreFieldSliceViolation {
	long x = 2_147_483_647;
}
// === end ===

// === case: expression_arithmetic_double_zero ===
class InputRedundantSuffixExpressionArithmeticDoubleZeroSliceViolation {
	double x = 1.0 + 0.0;
}
// === end ===

// === case: expression_lambda_double_zero ===
// imports: java.util.function.DoubleSupplier
class InputRedundantSuffixExpressionLambdaDoubleZeroSliceViolation {
	DoubleSupplier x = () -> 0.0;
}
// === end ===

// === case: for_init_decimal_double_suffix ===
class InputRedundantSuffixForInitDecimalDoubleSuffixSliceViolation {
	void m() {
		for (double d = 0.0; d < 1; ++d)
			System.out.println(d);
	}
}
// === end ===

// === case: for_init_multi_var_long_suffix ===
class InputRedundantSuffixForInitMultiVarLongSuffixSliceViolation {
	void m(long total) {
		for (long i = 0, n = total; i < n; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: hex_double_p_exponent_field ===
class InputRedundantSuffixHexDoublePExponentFieldSliceViolation {
	double x = 0x1.0p10;
}
// === end ===

// === case: hex_long_field ===
class InputRedundantSuffixHexLongFieldSliceViolation {
	long x = 0xFF;
}
// === end ===

// === case: main ===
class InputRedundantSuffixViolation {
	double fieldDouble, fieldDouble2;
	double[] doubleArray = {0.0, 1.0};
	float fieldFloat;
	long fieldLong;
	long[] longArray = {0, 1};

	void localVariables() {
		final var a = 0L;
		final var b = 1_000L;
		final var c = 0f;
		final var d = 0.0;
	}

	void ternary(boolean flag) {
		final long x = flag ? 0 : 1;
	}
}
// === end ===

// === case: method_arg_decimal_double ===
class InputRedundantSuffixMethodArgDecimalDoubleSliceViolation {
	void m() {
		takesDouble(0.0);
	}

	void takesDouble(double x) {}
}
// === end ===

// === case: multi_var_local_double_suffix ===
class InputRedundantSuffixMultiVarLocalDoubleSuffixSliceViolation {
	void m() {
		final double a = 0, b = 1;
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_float_suffix ===
class InputRedundantSuffixMultiVarLocalFloatSuffixSliceViolation {
	void m() {
		final float a = 0, b = 1;
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_long_out_of_range ===
class InputRedundantSuffixMultiVarLocalLongOutOfRangeSliceViolation {
	void m() {
		final long a = 2_147_483_648L, b = 0;
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_long_suffix ===
class InputRedundantSuffixMultiVarLocalLongSuffixSliceViolation {
	void m() {
		final long a = 0, b = 1;
		System.out.println(a + b);
	}
}
// === end ===

// === case: negative_zero_double_field ===
class InputRedundantSuffixNegativeZeroDoubleFieldSliceViolation {
	double x = -0.0;
}
// === end ===

// === case: negative_zero_float_field ===
class InputRedundantSuffixNegativeZeroFloatFieldSliceViolation {
	float x = -0;
}
// === end ===

// === case: new_array_long_zero_field ===
class InputRedundantSuffixNewArrayLongZeroFieldSliceViolation {
	long[] arr = new long[]{0};
}
// === end ===

// === case: octal_long_field ===
class InputRedundantSuffixOctalLongFieldSliceViolation {
	long x = 07;
}
// === end ===

// === case: reassignment_long_zero ===
class InputRedundantSuffixReassignmentLongZeroSliceViolation {
	long x = 100;

	void m() {
		x = 0;
	}
}
// === end ===

// === case: remove_double_suffix ===
class InputRedundantSuffixRemoveDoubleSliceViolation {
	double x = 1.0;
}
// === end ===

// === case: remove_float_suffix ===
class InputRedundantSuffixRemoveFloatSliceViolation {
	float x = 1;
}
// === end ===

// === case: remove_long_suffix ===
class InputRedundantSuffixRemoveLongSliceViolation {
	long m() {
		return 100;
	}
}
// === end ===

// === case: remove_lowercase_long_suffix ===
class InputRedundantSuffixRemoveLowercaseLongSliceViolation {
	long x = 100;
}
// === end ===

// === case: return_double_decimal_zero_lowercase ===
class InputRedundantSuffixReturnDoubleDecimalZeroLowercaseSliceViolation {
	double m() {
		return 0.0;
	}
}
// === end ===

// === case: return_double_int_zero_lowercase ===
class InputRedundantSuffixReturnDoubleIntZeroLowercaseSliceViolation {
	double m() {
		return 0;
	}
}
// === end ===

// === case: return_float_int_zero_lowercase ===
class InputRedundantSuffixReturnFloatIntZeroLowercaseSliceViolation {
	float m() {
		return 0;
	}
}
// === end ===

// === case: return_long_int_zero_lowercase ===
class InputRedundantSuffixReturnLongIntZeroLowercaseSliceViolation {
	long m() {
		return 0;
	}
}
// === end ===

// === case: scientific_notation_double_suffix ===
class InputRedundantSuffixScientificNotationDoubleSliceViolation {
	double x = 1e10;
}
// === end ===

// === case: static_final_long_field ===
class InputRedundantSuffixStaticFinalLongFieldSliceViolation {
	static final long X = 100;
}
// === end ===

// === case: static_long_field ===
class InputRedundantSuffixStaticLongFieldSliceViolation {
	static long x = 100;
}
// === end ===

// === case: var_local_decimal_double_zero ===
class InputRedundantSuffixVarLocalDecimalDoubleZeroSliceViolation {
	void m() {
		final var x = 0.0;
	}
}
// === end ===

// === case: zero_long_suffix ===
class InputRedundantSuffixZeroLongSliceViolation {
	long x = 0;
}
// === end ===