// === case: array_declaration_variants ===
class InputRedundantSuffixArrayDeclarationVariantsSliceViolation {
	@interface A {
	}

	@Deprecated
	long h = 1;
	long[] a = {1};
	long @A [] g = {1};
	long[][] b = {{1}}, f = {{1}, {2}};
}
// === end ===

// === case: assign_primitive_targets ===
class InputRedundantSuffixAssignPrimitiveTargetsSliceViolation {
	static class Holder {
		static long count;
	}

	static long s;

	double d;
	long l, x;
	long[] a = new long[1];
	Object o;
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

// === case: cast_long_zero_return ===
class InputRedundantSuffixCastLongZeroReturnSliceViolation {
	long m() {
		return 0;
	}
}
// === end ===

// === case: cast_primitive_type_by_suffix ===
class InputRedundantSuffixCastPrimitiveTypeBySuffixSliceViolation {
	byte byteD = (byte) 1, byteF = (byte) 1, byteL = (byte) 1;
	char charD = (char) 1, charF = (char) 1, charL = (char) 1;
	double doubleD = 1, doubleF = 1, doubleL = 1;
	float floatD = 1, floatF = 1, floatL = 1;
	int intD = 1, intF = 1, intL = 1;
	long longD = 1, longF = 1, longL = 1;
	short shortD = (short) 1, shortF = (short) 1, shortL = (short) 1;
}
// === end ===

// === case: cast_shapes ===
class InputRedundantSuffixCastShapesSliceViolation {
	@interface A {
	}

	double negatedOutside = -(double) 0;
	int doubleCast = (int) (long) 1;
	long annotated = (@A long) 1, negated = (long) -1;
	long[] a = new long[2];
	Object object = (long) 1;

	void inArgument() {
		System.out.println((long) 1);
	}

	long indexed() {
		return a[1];
	}
}
// === end ===

// === case: compound_assignment_wider_left_side ===
class InputRedundantSuffixCompoundAssignmentWiderLeftSideSliceViolation {
	double d;
	float f;
	long l;
	long[] a = new long[1];
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
	double a = 1., b = .5, c = 0X1P3, e = 1e+2, f = 4.9e-324, g = 010.5, h = 09.0, i = 010e1, j = -0e0, k = 0x1p-3,
			n = 1E-2;
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

// === case: int_valued_float_double_boundaries ===
class InputRedundantSuffixIntValuedFloatDoubleBoundariesSliceViolation {
	double e = 2147483647, j = 1_0, k = 1;
	float a = 16777216, b = 16777218, c = 2147483520, g, h, i = 1_000, n = -1;
}
// === end ===

// === case: long_int_range_every_radix ===
class InputRedundantSuffixLongIntRangeEveryRadixSliceViolation {
	long a = 0x7FFF_FFFF, b = 017_777_777_777, e = -2_147_483_647, f = -0;
	long c = 0b111_1111_1111_1111_1111_1111_1111_1111;
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

// === case: primitive_widening_targets ===
class InputRedundantSuffixPrimitiveWideningTargetsSliceViolation {
	double a = 1, c = 1;
	float b = 1;
}
// === end ===

// === case: ternary_and_switch_arms ===
class InputRedundantSuffixTernaryAndSwitchArmsSliceViolation {
	boolean flag, other;
	int i;
	long x;
	long[] array = {flag ? 1 : 2};
	long comparisonCondition = (flag ? 1L : 2L) > 0 ? 3 : 4;
	long continued = flag
			? 1
			: 2;
	float floatArm = flag ? 1 : i, negatedTernary = -(flag ? 0f : 1), negativeZeroArm = flag ? -0f : 1;
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
	long a = ((1)), b = +1, c = ~1, e = -~1;
}
// === end ===

// === case: zero_long_suffix ===
class InputRedundantSuffixZeroLongSliceViolation {
	long x;
}
// === end ===