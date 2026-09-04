package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

// === case: annotation_default_primitive_elements ===
@interface InputRedundantSuffixAnnotationDefaultPrimitiveElementsSliceViolation {
	double d() default 1L; // violation: Remove the redundant 'L' suffix.

	float f() default 1f; // violation: Remove the redundant 'f' suffix.

	long n() default -1L; // violation: Remove the redundant 'L' suffix.

	long p() default (1L); // violation: Remove the redundant 'L' suffix.

	long v() default 1L; // violation: Remove the redundant 'L' suffix.

	long[] vs() default {1L, 2L}; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: annotation_value_primitive_elements ===
class InputRedundantSuffixAnnotationValuePrimitiveElementsSliceViolation {
	@interface Array {
		long[] value();
	}

	@interface Element {
		double d() default 0;

		long v() default 0;

		long[] vs() default {};
	}

	@interface Nested {
		Single value();
	}

	@interface Single {
		long value();
	}

	@Single(1L) // violation: Remove the redundant 'L' suffix.
	void a() {}

	@Element(v = 1L) // violation: Remove the redundant 'L' suffix.
	void b() {}

	@Array({1L, 2L}) // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	void c() {}

	@Element(vs = {1L, 2L}) // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	void e() {}

	@Single(-1L) // violation: Remove the redundant 'L' suffix.
	void f() {}

	@Nested(@Single(1L)) // violation: Remove the redundant 'L' suffix.
	void g() {}

	@Element(d = 1f) // violation: Remove the redundant 'f' suffix.
	void h() {}
}
// === end ===

// === case: array_declaration_variants ===
class InputRedundantSuffixArrayDeclarationVariantsSliceViolation {
	@interface A {
	}

	long a[] = {1L}; // violation: Remove the redundant 'L' suffix.
	long[] b[] = {{1L}}; // violation: Remove the redundant 'L' suffix.
	long[][] f = {{1L}, {2L}}; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	long @A [] g = {1L}; // violation: Remove the redundant 'L' suffix.
	@Deprecated
	long h = 1L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: array_initializer_mixed_float ===
class InputRedundantSuffixArrayInitializerMixedFloatSliceViolation {
	float[] arr = {0f, 1.5f}; // violation: Remove the redundant 'f' suffix.
}
// === end ===

// === case: array_initializer_single_float ===
class InputRedundantSuffixArrayInitializerSingleFloatSliceViolation {
	float[] arr = {0f}; // violation: Remove the redundant 'f' suffix.
}
// === end ===

// === case: assign_primitive_targets ===
class InputRedundantSuffixAssignPrimitiveTargetsSliceViolation {
	static class Holder {
		static long count;
	}

	static long s;

	long[] a = new long[1];
	double d;
	long l;
	Object o;
	long x;
	Runnable block = () -> {
		x = 3L; // violation: Remove the redundant 'L' suffix.
		System.out.println(x);
	};
	Runnable expression = () -> x = 4L; // violation: Remove the redundant 'L' suffix.

	static {
		s = 1L; // violation: Remove the redundant 'L' suffix.
	}

	{
		x = 1L; // violation: Remove the redundant 'L' suffix.
	}

	InputRedundantSuffixAssignPrimitiveTargetsSliceViolation() {
		x = 2L; // violation: Remove the redundant 'L' suffix.
	}

	void arrayElements() {
		a[0] = 1L; // violation: Remove the redundant 'L' suffix.
		longs()[0] = 1L; // violation: Remove the redundant 'L' suffix.
	}

	void chained() {
		o = l = 1L; // violation: Remove the redundant 'L' suffix.
	}

	void doubleTarget() {
		d = 1f; // violation: Remove the redundant 'f' suffix.
		d = 1L; // violation: Remove the redundant 'L' suffix.
	}

	void forEachVar(long[] values) {
		for (var v : values) {
			v = 1L; // violation: Remove the redundant 'L' suffix.
			System.out.println(v);
		}
	}

	void forInitExpression(long n) {
		long i;
		for (i = 1L; i < n; ++i) // violation: Remove the redundant 'L' suffix.
			System.out.println(i);
	}

	void inArgument() {
		System.out.println(x = 1L); // violation: Remove the redundant 'L' suffix.
	}

	void localAndParameter(long p) {
		var v = 0L;
		v = 1L; // violation: Remove the redundant 'L' suffix.
		p = 1L; // violation: Remove the redundant 'L' suffix.
		System.out.println(p + v);
	}

	long[] longs() {
		return a;
	}

	void qualifiedStatic() {
		Holder.count = 1L; // violation: Remove the redundant 'L' suffix.
	}

	void shadowingLongParameter(long o) {
		o = 1L; // violation: Remove the redundant 'L' suffix.
		System.out.println(o);
	}

	void thisField(long x) {
		this.x = 1L; // violation: Remove the redundant 'L' suffix.
		System.out.println(x);
	}
}
// === end ===

// === case: assign_record_compact_constructor ===
record InputRedundantSuffixAssignRecordCompactConstructorSliceViolation(long v) {
	InputRedundantSuffixAssignRecordCompactConstructorSliceViolation {
		v = 1L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: binary_long_field ===
class InputRedundantSuffixBinaryLongFieldSliceViolation {
	long x = 0b1010L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: cast_long_zero_return ===
class InputRedundantSuffixCastLongZeroReturnSliceViolation {
	long m() {
		return (long) 0L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: cast_primitive_type_by_suffix ===
class InputRedundantSuffixCastPrimitiveTypeBySuffixSliceViolation {
	byte byteD = (byte) 1d; // violation: Remove the redundant 'd' suffix.
	byte byteF = (byte) 1f; // violation: Remove the redundant 'f' suffix.
	byte byteL = (byte) 1L; // violation: Remove the redundant 'L' suffix.
	char charD = (char) 1d; // violation: Remove the redundant 'd' suffix.
	char charF = (char) 1f; // violation: Remove the redundant 'f' suffix.
	char charL = (char) 1L; // violation: Remove the redundant 'L' suffix.
	double doubleD = (double) 1d; // violation: Remove the redundant 'd' suffix.
	double doubleF = (double) 1f; // violation: Remove the redundant 'f' suffix.
	double doubleL = (double) 1L; // violation: Remove the redundant 'L' suffix.
	float floatD = (float) 1d; // violation: Remove the redundant 'd' suffix.
	float floatF = (float) 1f; // violation: Remove the redundant 'f' suffix.
	float floatL = (float) 1L; // violation: Remove the redundant 'L' suffix.
	int intD = (int) 1d; // violation: Remove the redundant 'd' suffix.
	int intF = (int) 1f; // violation: Remove the redundant 'f' suffix.
	int intL = (int) 1L; // violation: Remove the redundant 'L' suffix.
	long longD = (long) 1d; // violation: Remove the redundant 'd' suffix.
	long longF = (long) 1f; // violation: Remove the redundant 'f' suffix.
	long longL = (long) 1L; // violation: Remove the redundant 'L' suffix.
	short shortD = (short) 1d; // violation: Remove the redundant 'd' suffix.
	short shortF = (short) 1f; // violation: Remove the redundant 'f' suffix.
	short shortL = (short) 1L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: cast_shapes ===
class InputRedundantSuffixCastShapesSliceViolation {
	@interface A {
	}

	long[] a = new long[2];
	long annotated = (@A long) 1L; // violation: Remove the redundant 'L' suffix.
	int doubleCast = (int) (long) 1L; // violation: Remove the redundant 'L' suffix.
	long negated = (long) -1L; // violation: Remove the redundant 'L' suffix.
	double negatedOutside = -(double) 0f; // violation: Remove the redundant 'f' suffix.
	Object object = (long) 1L; // violation: Remove the redundant 'L' suffix.

	void inArgument() {
		System.out.println((long) 1L); // violation: Remove the redundant 'L' suffix.
	}

	long indexed() {
		return a[(int) 1L]; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: compound_assignment_long_every_operator ===
class InputRedundantSuffixCompoundAssignmentLongEveryOperatorSliceViolation {
	long x;

	void m() {
		x += 1L; // violation: Remove the redundant 'L' suffix.
		x -= 1L; // violation: Remove the redundant 'L' suffix.
		x *= 2L; // violation: Remove the redundant 'L' suffix.
		x /= 2L; // violation: Remove the redundant 'L' suffix.
		x %= 2L; // violation: Remove the redundant 'L' suffix.
		x <<= 1L; // violation: Remove the redundant 'L' suffix.
		x >>= 1L; // violation: Remove the redundant 'L' suffix.
		x >>>= 1L; // violation: Remove the redundant 'L' suffix.
		x &= 1L; // violation: Remove the redundant 'L' suffix.
		x |= 1L; // violation: Remove the redundant 'L' suffix.
		x ^= 1L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: compound_assignment_long_zero ===
class InputRedundantSuffixCompoundAssignmentLongZeroSliceViolation {
	long x = 100;

	void m() {
		x += 0L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: compound_assignment_wider_left_side ===
class InputRedundantSuffixCompoundAssignmentWiderLeftSideSliceViolation {
	long[] a = new long[1];
	double d;
	float f;
	long l;
	Runnable r = () -> l += 1L; // violation: Remove the redundant 'L' suffix.

	void arrayAndThis(float f) {
		a[0] += 1L; // violation: Remove the redundant 'L' suffix.
		this.f -= 1f; // violation: Remove the redundant 'f' suffix.
		System.out.println(f);
	}

	void doubleLeft() {
		d += 1L; // violation: Remove the redundant 'L' suffix.
		d -= 1f; // violation: Remove the redundant 'f' suffix.
		d /= 2D; // violation: Remove the redundant 'D' suffix.
	}

	void floatLeft() {
		f += 1L; // violation: Remove the redundant 'L' suffix.
		f *= 2f; // violation: Remove the redundant 'f' suffix.
	}

	void forUpdate(long n) {
		for (long i = 0; i < n; i += 2L) // violation: Remove the redundant 'L' suffix.
			System.out.println(i);
	}

	void zeroDivisors() {
		d /= 0f; // violation: Remove the redundant 'f' suffix.
		l /= 0L; // violation: Remove the redundant 'L' suffix.
		l %= 0L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: decimal_double_spellings ===
class InputRedundantSuffixDecimalDoubleSpellingsSliceViolation {
	double a = 1.d; // violation: Remove the redundant 'd' suffix.
	double b = .5d; // violation: Remove the redundant 'd' suffix.
	double c = 0X1P3D; // violation: Remove the redundant 'D' suffix.
	double e = 1e+2d; // violation: Remove the redundant 'd' suffix.
	double f = 4.9e-324d; // violation: Remove the redundant 'd' suffix.
	double g = 010.5d; // violation: Remove the redundant 'd' suffix.
	double h = 09.0d; // violation: Remove the redundant 'd' suffix.
	double i = 010e1d; // violation: Remove the redundant 'd' suffix.
	double j = -0e0d; // violation: Remove the redundant 'd' suffix.
	double k = 0x1p-3d; // violation: Remove the redundant 'd' suffix.
	double n = 1E-2D; // violation: Remove the redundant 'D' suffix.
}
// === end ===

// === case: decimal_double_uppercase_field ===
class InputRedundantSuffixDecimalDoubleUppercaseFieldSliceViolation {
	double x = 3.14D; // violation: Remove the redundant 'D' suffix.
}
// === end ===

// === case: decimal_float_uppercase_field ===
class InputRedundantSuffixDecimalFloatUppercaseFieldSliceViolation {
	float x = 100F; // violation: Remove the redundant 'F' suffix.
}
// === end ===

// === case: decimal_long_negative_field ===
class InputRedundantSuffixDecimalLongNegativeFieldSliceViolation {
	long x = -1L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: decimal_long_underscore_field ===
class InputRedundantSuffixDecimalLongUnderscoreFieldSliceViolation {
	long x = 2_147_483_647L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: expression_arithmetic_double_zero ===
class InputRedundantSuffixExpressionArithmeticDoubleZeroSliceViolation {
	double x = 1.0 + 0.0d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: expression_lambda_double_zero ===
// imports: java.util.function.DoubleSupplier
class InputRedundantSuffixExpressionLambdaDoubleZeroSliceViolation {
	DoubleSupplier x = () -> 0.0d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: for_init_decimal_double_suffix ===
class InputRedundantSuffixForInitDecimalDoubleSuffixSliceViolation {
	void m() {
		for (double d = 0.0d; d < 1; ++d) // violation: Remove the redundant 'd' suffix.
			System.out.println(d);
	}
}
// === end ===

// === case: for_init_multi_var_long_suffix ===
class InputRedundantSuffixForInitMultiVarLongSuffixSliceViolation {
	void m(long total) {
		for (long i = 0L, n = total; i < n; ++i) // violation: Remove the redundant 'L' suffix.
			System.out.println(i);
	}
}
// === end ===

// === case: hex_double_p_exponent_field ===
class InputRedundantSuffixHexDoublePExponentFieldSliceViolation {
	double x = 0x1.0p10d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: hex_long_field ===
class InputRedundantSuffixHexLongFieldSliceViolation {
	long x = 0xFFL; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: int_valued_float_double_boundaries ===
class InputRedundantSuffixIntValuedFloatDoubleBoundariesSliceViolation {
	float a = 16777216f; // violation: Remove the redundant 'f' suffix.
	float b = 16777218f; // violation: Remove the redundant 'f' suffix.
	float c = 2147483520f; // violation: Remove the redundant 'f' suffix.
	double e = 2147483647d; // violation: Remove the redundant 'd' suffix.
	float g = 00f; // violation: Remove the redundant 'f' suffix.
	float h = 0_0f; // violation: Remove the redundant 'f' suffix.
	float i = 1_000f; // violation: Remove the redundant 'f' suffix.
	double j = 1_0d; // violation: Remove the redundant 'd' suffix.
	double k = 1D; // violation: Remove the redundant 'D' suffix.
	float n = -1f; // violation: Remove the redundant 'f' suffix.
}
// === end ===

// === case: interface_constant_long ===
interface InputRedundantSuffixInterfaceConstantLongSliceViolation {
	long X = 1L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: long_int_range_every_radix ===
class InputRedundantSuffixLongIntRangeEveryRadixSliceViolation {
	long a = 0x7FFF_FFFFL; // violation: Remove the redundant 'L' suffix.
	long b = 017_777_777_777L; // violation: Remove the redundant 'L' suffix.
	long c = 0b111_1111_1111_1111_1111_1111_1111_1111L; // violation: Remove the redundant 'L' suffix.
	long e = -2_147_483_647L; // violation: Remove the redundant 'L' suffix.
	long f = -0L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: main ===
// multi-fix-expected
class InputRedundantSuffixViolation {
	long fieldLong = 0L; // violation: Remove the redundant 'L' suffix.
	float fieldFloat = 0f; // violation: Remove the redundant 'f' suffix.
	double fieldDouble = 0d; // violation: Remove the redundant 'd' suffix.
	double fieldDouble2 = 0.0d; // violation: Remove the redundant 'd' suffix.

	long[] longArray = {0L, 1L}; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	double[] doubleArray = {0.0d, 1.0D}; // violation: Remove the redundant 'd' suffix. // violation: Remove the redundant 'D' suffix.

	void localVariables() {
		final long a = 0L;
		final long b = 1_000L;
		final float c = 0f;
		final double d = 0.0d; // violation: Remove the redundant 'd' suffix.
	}

	void ternary(boolean flag) {
		final long x = flag ? 0L : 1L; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: method_arg_decimal_double ===
class InputRedundantSuffixMethodArgDecimalDoubleSliceViolation {
	void m() {
		takesDouble(0.0d); // violation: Remove the redundant 'd' suffix.
	}

	void takesDouble(double x) {}
}
// === end ===

// === case: multi_var_local_double_suffix ===
class InputRedundantSuffixMultiVarLocalDoubleSuffixSliceViolation {
	void m() {
		final double a = 0d, b = 1d; // violation: Remove the redundant 'd' suffix. // violation: Remove the redundant 'd' suffix.
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_float_suffix ===
class InputRedundantSuffixMultiVarLocalFloatSuffixSliceViolation {
	void m() {
		final float a = 0f, b = 1f; // violation: Remove the redundant 'f' suffix. // violation: Remove the redundant 'f' suffix.
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_long_out_of_range ===
class InputRedundantSuffixMultiVarLocalLongOutOfRangeSliceViolation {
	void m() {
		final long a = 2_147_483_648L, b = 0L; // violation: Remove the redundant 'L' suffix.
		System.out.println(a + b);
	}
}
// === end ===

// === case: multi_var_local_long_suffix ===
class InputRedundantSuffixMultiVarLocalLongSuffixSliceViolation {
	void m() {
		final long a = 0L, b = 1L; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
		System.out.println(a + b);
	}
}
// === end ===

// === case: negative_zero_double_field ===
class InputRedundantSuffixNegativeZeroDoubleFieldSliceViolation {
	double x = -0.0d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: new_array_long_zero_field ===
class InputRedundantSuffixNewArrayLongZeroFieldSliceViolation {
	long[] arr = new long[]{0L}; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: new_array_primitive_element ===
// imports: java.util.function.Supplier
class InputRedundantSuffixNewArrayPrimitiveElementSliceViolation {
	@interface A {
	}

	long[] annotated = new @A long[]{1L}; // violation: Remove the redundant 'L' suffix.
	long indexed = new long[]{1L}[0]; // violation: Remove the redundant 'L' suffix.
	double[] mixed = new double[]{1f, 1L, 1d}; // violation: Remove the redundant 'f' suffix. // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'd' suffix.
	long[][] nested = new long[][]{{1L}}; // violation: Remove the redundant 'L' suffix.
	Object object = new long[]{1L}; // violation: Remove the redundant 'L' suffix.
	Supplier<long[]> supplier = () -> new long[]{1L}; // violation: Remove the redundant 'L' suffix.

	void forEach() {
		for (long v : new long[]{1L}) // violation: Remove the redundant 'L' suffix.
			System.out.println(v);
	}

	void inArgument() {
		takes(new long[]{1L}); // violation: Remove the redundant 'L' suffix.
	}

	long[] returned() {
		return new long[]{1L}; // violation: Remove the redundant 'L' suffix.
	}

	void takes(long[] values) {
		System.out.println(values.length);
	}
}
// === end ===

// === case: octal_long_field ===
class InputRedundantSuffixOctalLongFieldSliceViolation {
	long x = 07L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: primitive_widening_targets ===
class InputRedundantSuffixPrimitiveWideningTargetsSliceViolation {
	double a = 1L; // violation: Remove the redundant 'L' suffix.
	float b = 1L; // violation: Remove the redundant 'L' suffix.
	double c = 1f; // violation: Remove the redundant 'f' suffix.
}
// === end ===

// === case: reassignment_long_zero ===
class InputRedundantSuffixReassignmentLongZeroSliceViolation {
	long x = 100;

	void m() {
		x = 0L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: remove_double_suffix ===
class InputRedundantSuffixRemoveDoubleSliceViolation {
	double x = 1.0d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: remove_float_suffix ===
class InputRedundantSuffixRemoveFloatSliceViolation {
	float x = 1f; // violation: Remove the redundant 'f' suffix.
}
// === end ===

// === case: remove_long_suffix ===
class InputRedundantSuffixRemoveLongSliceViolation {
	long m() {
		return 100L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: remove_lowercase_long_suffix ===
class InputRedundantSuffixRemoveLowercaseLongSliceViolation {
	long x = 100l; // violation: Remove the redundant 'l' suffix.
}
// === end ===

// === case: return_double_decimal_zero_lowercase ===
class InputRedundantSuffixReturnDoubleDecimalZeroLowercaseSliceViolation {
	double m() {
		return 0.0d; // violation: Remove the redundant 'd' suffix.
	}
}
// === end ===

// === case: return_double_int_zero_lowercase ===
class InputRedundantSuffixReturnDoubleIntZeroLowercaseSliceViolation {
	double m() {
		return 0d; // violation: Remove the redundant 'd' suffix.
	}
}
// === end ===

// === case: return_enum_constant_body ===
enum InputRedundantSuffixReturnEnumConstantBodySliceViolation {
	A {
		@Override
		long v() {
			return 1L; // violation: Remove the redundant 'L' suffix.
		}
	};

	long v() {
		return 2L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: return_float_int_zero_lowercase ===
class InputRedundantSuffixReturnFloatIntZeroLowercaseSliceViolation {
	float m() {
		return 0f; // violation: Remove the redundant 'f' suffix.
	}
}
// === end ===

// === case: return_interface_default_method ===
interface InputRedundantSuffixReturnInterfaceDefaultMethodSliceViolation {
	default long v() {
		return 1L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: return_long_int_zero_lowercase ===
class InputRedundantSuffixReturnLongIntZeroLowercaseSliceViolation {
	long m() {
		return 0L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: return_primitive_targets ===
class InputRedundantSuffixReturnPrimitiveTargetsSliceViolation {
	Object anonymous = new Object() {
		long v() {
			return 1L; // violation: Remove the redundant 'L' suffix.
		}
	};

	double a() {
		return 1L; // violation: Remove the redundant 'L' suffix.
	}

	double b() {
		return 1f; // violation: Remove the redundant 'f' suffix.
	}

	float c() {
		return 1L; // violation: Remove the redundant 'L' suffix.
	}

	<T> long generic(T t) {
		return 1L; // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: scientific_notation_double_suffix ===
class InputRedundantSuffixScientificNotationDoubleSliceViolation {
	double x = 1e10d; // violation: Remove the redundant 'd' suffix.
}
// === end ===

// === case: static_final_long_field ===
class InputRedundantSuffixStaticFinalLongFieldSliceViolation {
	static final long X = 100L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: static_long_field ===
class InputRedundantSuffixStaticLongFieldSliceViolation {
	static long x = 100L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: ternary_and_switch_arms ===
class InputRedundantSuffixTernaryAndSwitchArmsSliceViolation {
	boolean flag;
	int i;
	boolean other;
	long x;
	long[] array = {flag ? 1L : 2L}; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	long comparisonCondition = (flag ? 1L : 2L) > 0 ? 3L : 4L; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	long continued = flag
			? 1L // violation: Remove the redundant 'L' suffix.
			: 2L; // violation: Remove the redundant 'L' suffix.
	float floatArm = flag ? 1f : i; // violation: Remove the redundant 'f' suffix.
	float negatedTernary = -(flag ? 0f : 1f); // violation: Remove the redundant 'f' suffix.
	float negativeZeroArm = flag ? -0f : 1f; // violation: Remove the redundant 'f' suffix.
	long nested = flag ? other ? 1L : 2L : 3L; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.

	void assignSwitch(int k) {
		x = switch (k) {
			case 1 -> 1L; // violation: Remove the redundant 'L' suffix.
			default -> 2L; // violation: Remove the redundant 'L' suffix.
		};
	}

	long returnSwitch(int k) {
		return switch (k) {
			case 1 -> 1L; // violation: Remove the redundant 'L' suffix.
			default -> -2L; // violation: Remove the redundant 'L' suffix.
		};
	}

	long returnTernary(boolean condition) {
		return condition ? 1L : 2L; // violation: Remove the redundant 'L' suffix. // violation: Remove the redundant 'L' suffix.
	}
}
// === end ===

// === case: transparent_unary_and_parens ===
class InputRedundantSuffixTransparentUnaryAndParensSliceViolation {
	long a = ((1L)); // violation: Remove the redundant 'L' suffix.
	long b = +1L; // violation: Remove the redundant 'L' suffix.
	long c = ~1L; // violation: Remove the redundant 'L' suffix.
	long e = -~1L; // violation: Remove the redundant 'L' suffix.
}
// === end ===

// === case: var_local_decimal_double_zero ===
class InputRedundantSuffixVarLocalDecimalDoubleZeroSliceViolation {
	void m() {
		final var x = 0.0d; // violation: Remove the redundant 'd' suffix.
	}
}
// === end ===

// === case: zero_long_suffix ===
class InputRedundantSuffixZeroLongSliceViolation {
	long x = 0L; // violation: Remove the redundant 'L' suffix.
}
// === end ===