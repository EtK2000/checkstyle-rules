package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

// === case: annotation_default_primitive_elements ===
@interface InputRedundantSuffixAnnotationDefaultPrimitiveElementsSliceViolation {
	double d() default 1;

	float f() default 1;

	long n() default -1;

	long p() default (1);

	long v() default 1;

	long[] vs() default {1, 2};
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

	@Single(1)
	void a() {}

	@Element(v = 1)
	void b() {}

	@Array({1, 2})
	void c() {}

	@Element(vs = {1, 2})
	void e() {}

	@Single(-1)
	void f() {}

	@Nested(@Single(1))
	void g() {}

	@Element(d = 1)
	void h() {}
}
// === end ===

// === case: array_declaration_variants ===
class InputRedundantSuffixArrayDeclarationVariantsSliceViolation {
	@interface A {
	}

	long a[] = {1};
	long[] b[] = {{1}};
	long[][] f = {{1}, {2}};
	long @A [] g = {1};
	@Deprecated
	long h = 1;
}
// === end ===

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
		x = 3;
		System.out.println(x);
	};
	Runnable expression = () -> x = 4;

	static {
		s = 1;
	}

	{
		x = 1;
	}

	InputRedundantSuffixAssignPrimitiveTargetsSliceViolation() {
		x = 2;
	}

	void arrayElements() {
		a[0] = 1;
		longs()[0] = 1;
	}

	void chained() {
		o = l = 1;
	}

	void doubleTarget() {
		d = 1;
		d = 1;
	}

	void forEachVar(long[] values) {
		for (var v : values) {
			v = 1;
			System.out.println(v);
		}
	}

	void forInitExpression(long n) {
		long i;
		for (i = 1; i < n; ++i)
			System.out.println(i);
	}

	void inArgument() {
		System.out.println(x = 1);
	}

	void localAndParameter(long p) {
		var v = 0L;
		v = 1;
		p = 1;
		System.out.println(p + v);
	}

	long[] longs() {
		return a;
	}

	void qualifiedStatic() {
		Holder.count = 1;
	}

	void shadowingLongParameter(long o) {
		o = 1;
		System.out.println(o);
	}

	void thisField(long x) {
		this.x = 1;
		System.out.println(x);
	}
}
// === end ===

// === case: assign_record_compact_constructor ===
record InputRedundantSuffixAssignRecordCompactConstructorSliceViolation(long v) {
	InputRedundantSuffixAssignRecordCompactConstructorSliceViolation {
		v = 1;
	}
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

// === case: cast_primitive_type_by_suffix ===
class InputRedundantSuffixCastPrimitiveTypeBySuffixSliceViolation {
	byte byteD = (byte) 1;
	byte byteF = (byte) 1;
	byte byteL = (byte) 1;
	char charD = (char) 1;
	char charF = (char) 1;
	char charL = (char) 1;
	double doubleD = (double) 1;
	double doubleF = (double) 1;
	double doubleL = (double) 1;
	float floatD = (float) 1;
	float floatF = (float) 1;
	float floatL = (float) 1;
	int intD = (int) 1;
	int intF = (int) 1;
	int intL = (int) 1;
	long longD = (long) 1;
	long longF = (long) 1;
	long longL = (long) 1;
	short shortD = (short) 1;
	short shortF = (short) 1;
	short shortL = (short) 1;
}
// === end ===

// === case: cast_shapes ===
class InputRedundantSuffixCastShapesSliceViolation {
	@interface A {
	}

	long[] a = new long[2];
	long annotated = (@A long) 1;
	int doubleCast = (int) (long) 1;
	long negated = (long) -1;
	double negatedOutside = -(double) 0;
	Object object = (long) 1;

	void inArgument() {
		System.out.println((long) 1);
	}

	long indexed() {
		return a[(int) 1];
	}
}
// === end ===

// === case: compound_assignment_long_every_operator ===
class InputRedundantSuffixCompoundAssignmentLongEveryOperatorSliceViolation {
	long x;

	void m() {
		x += 1;
		x -= 1;
		x *= 2;
		x /= 2;
		x %= 2;
		x <<= 1;
		x >>= 1;
		x >>>= 1;
		x &= 1;
		x |= 1;
		x ^= 1;
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

// === case: compound_assignment_wider_left_side ===
class InputRedundantSuffixCompoundAssignmentWiderLeftSideSliceViolation {
	long[] a = new long[1];
	double d;
	float f;
	long l;
	Runnable r = () -> l += 1;

	void arrayAndThis(float f) {
		a[0] += 1;
		this.f -= 1;
		System.out.println(f);
	}

	void doubleLeft() {
		d += 1;
		d -= 1;
		d /= 2;
	}

	void floatLeft() {
		f += 1;
		f *= 2;
	}

	void forUpdate(long n) {
		for (long i = 0; i < n; i += 2)
			System.out.println(i);
	}

	void zeroDivisors() {
		d /= 0;
		l /= 0;
		l %= 0;
	}
}
// === end ===

// === case: decimal_double_spellings ===
class InputRedundantSuffixDecimalDoubleSpellingsSliceViolation {
	double a = 1.;
	double b = .5;
	double c = 0X1P3;
	double e = 1e+2;
	double f = 4.9e-324;
	double g = 010.5;
	double h = 09.0;
	double i = 010e1;
	double j = -0e0;
	double k = 0x1p-3;
	double n = 1E-2;
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

// === case: int_valued_float_double_boundaries ===
class InputRedundantSuffixIntValuedFloatDoubleBoundariesSliceViolation {
	float a = 16777216;
	float b = 16777218;
	float c = 2147483520;
	double e = 2147483647;
	float g = 00;
	float h = 0_0;
	float i = 1_000;
	double j = 1_0;
	double k = 1;
	float n = -1;
}
// === end ===

// === case: interface_constant_long ===
interface InputRedundantSuffixInterfaceConstantLongSliceViolation {
	long X = 1;
}
// === end ===

// === case: long_int_range_every_radix ===
class InputRedundantSuffixLongIntRangeEveryRadixSliceViolation {
	long a = 0x7FFF_FFFF;
	long b = 017_777_777_777;
	long c = 0b111_1111_1111_1111_1111_1111_1111_1111;
	long e = -2_147_483_647;
	long f = -0;
}
// === end ===

// === case: main ===
class InputRedundantSuffixViolation {
	long fieldLong = 0;
	float fieldFloat = 0;
	double fieldDouble = 0;
	double fieldDouble2 = 0.0;

	long[] longArray = {0, 1};
	double[] doubleArray = {0.0, 1.0};

	void localVariables() {
		final long a = 0L;
		final long b = 1_000L;
		final float c = 0f;
		final double d = 0.0;
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

// === case: new_array_long_zero_field ===
class InputRedundantSuffixNewArrayLongZeroFieldSliceViolation {
	long[] arr = new long[]{0};
}
// === end ===

// === case: new_array_primitive_element ===
// imports: java.util.function.Supplier
class InputRedundantSuffixNewArrayPrimitiveElementSliceViolation {
	@interface A {
	}

	long[] annotated = new @A long[]{1};
	long indexed = new long[]{1}[0];
	double[] mixed = new double[]{1, 1, 1};
	long[][] nested = new long[][]{{1}};
	Object object = new long[]{1};
	Supplier<long[]> supplier = () -> new long[]{1};

	void forEach() {
		for (long v : new long[]{1})
			System.out.println(v);
	}

	void inArgument() {
		takes(new long[]{1});
	}

	long[] returned() {
		return new long[]{1};
	}

	void takes(long[] values) {
		System.out.println(values.length);
	}
}
// === end ===

// === case: octal_long_field ===
class InputRedundantSuffixOctalLongFieldSliceViolation {
	long x = 07;
}
// === end ===

// === case: primitive_widening_targets ===
class InputRedundantSuffixPrimitiveWideningTargetsSliceViolation {
	double a = 1;
	float b = 1;
	double c = 1;
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

// === case: return_enum_constant_body ===
enum InputRedundantSuffixReturnEnumConstantBodySliceViolation {
	A {
		@Override
		long v() {
			return 1;
		}
	};

	long v() {
		return 2;
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

// === case: return_interface_default_method ===
interface InputRedundantSuffixReturnInterfaceDefaultMethodSliceViolation {
	default long v() {
		return 1;
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

// === case: return_primitive_targets ===
class InputRedundantSuffixReturnPrimitiveTargetsSliceViolation {
	Object anonymous = new Object() {
		long v() {
			return 1;
		}
	};

	double a() {
		return 1;
	}

	double b() {
		return 1;
	}

	float c() {
		return 1;
	}

	<T> long generic(T t) {
		return 1;
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

// === case: ternary_and_switch_arms ===
class InputRedundantSuffixTernaryAndSwitchArmsSliceViolation {
	boolean flag;
	int i;
	boolean other;
	long x;
	long[] array = {flag ? 1 : 2};
	long comparisonCondition = (flag ? 1L : 2L) > 0 ? 3 : 4;
	long continued = flag
			? 1
			: 2;
	float floatArm = flag ? 1 : i;
	float negatedTernary = -(flag ? 0f : 1);
	float negativeZeroArm = flag ? -0f : 1;
	long nested = flag ? other ? 1 : 2 : 3;

	void assignSwitch(int k) {
		x = switch (k) {
			case 1 -> 1;
			default -> 2;
		};
	}

	long returnSwitch(int k) {
		return switch (k) {
			case 1 -> 1;
			default -> -2;
		};
	}

	long returnTernary(boolean condition) {
		return condition ? 1 : 2;
	}
}
// === end ===

// === case: transparent_unary_and_parens ===
class InputRedundantSuffixTransparentUnaryAndParensSliceViolation {
	long a = ((1));
	long b = +1;
	long c = ~1;
	long e = -~1;
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