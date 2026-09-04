package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

import java.io.Serializable;
import java.util.List;
import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;

class InputRedundantSuffixClean {
	// L suffix needed when value exceeds int range
	long bigValue = 3_000_000_000L;
	long maxLong = 9_223_372_036_854_775_807L;
	long minInt = -2_147_483_648L;
	long bigHex = 0x1_0000_0000L;
	long bigBinary = 0b1_0000_0000_0000_0000_0000_0000_0000_0000L;

	// float with decimal: f suffix is needed to scope down from double
	float decimalFloat = 1.5f;
	float piFloat = 3.14f;
	float scientificFloat = 1e2f;
	float hexFloat = 0x1.0p10f;

	// double without d suffix: already double, nothing to flag
	double implicitDouble = 0.0;
	double anotherDouble = 3.14;
	double hexDouble = 0x1.0p10;

	// no suffix at all: nothing to flag
	long widened = 0;
	float widenedFloat = 0;
	double widenedDouble = 0;

	// wrapper types: suffix needed for autoboxing
	Long boxedLong = 0L;
	Float boxedFloat = 0f;
	Double boxedDouble = 0d;

	// lambda: functional interface type inference
	LongSupplier lambdaLong = () -> 0L;

	// wrapper array init: autoboxing needs exact type
	Long[] boxedArray = {0L, 1L};

	// arithmetic: suffix promotes the operation's type
	long promoted = Integer.MAX_VALUE + 1L;
	long multiplication = 1_000_000 * 1_000_000L;
	long bitwise = 0xFFFF & 0xFFL;
	long shifted = 1L << 32;

	// constructor arguments: can't verify overloads, so never flagged
	InputRedundantSuffixClean(long x) {}

	// constructor arguments: can't verify overloads, so never flagged
	void constructorArgs() {
		new InputRedundantSuffixClean(0L);
	}

	// a primitive-typed local becomes `var`, so a suffix the unsuffixed literal would not
	// imply is load-bearing; only `d` on a decimal is strippable, and `0d` is not decimal
	void doubleAndNegatedLocals() {
		final double intValued = 0d;
		final long negated = -1L;
		final long complemented = ~1L;
		final long positive = +1L;
		final long parenWrapped = (0L);
		final float decimalPair = 1.5f, decimalPairTwo = 2.5f;
	}

	void forInitLocals(long total) {
		// for-init locals become `var` too, so the suffix must survive or the counter
		// silently becomes an int
		for (long i = 0L; i < total; ++i)
			System.out.println(i);
		for (float f = 1f; f < total; ++f)
			System.out.println(f);
		for (double d = 0d; d < total; ++d)
			System.out.println(d);
	}

	// method arguments: can't verify overloads, so never flagged
	void methodArgs() {
		takesLong(0L);
		takesFloat(0f);
		takesDouble(0d);
		takesFloat(1.5f);
	}

	// return in wrapper-typed methods: autoboxing needs exact type
	Double returnsBoxedDouble() {
		return 0d;
	}

	Float returnsBoxedFloat() {
		return 0f;
	}

	Long returnsBoxedLong() {
		return 0L;
	}

	int returnsInt() {
		return 0;
	}

	// method arguments: can't verify overloads, so never flagged
	void takesDouble(double x) {}
	void takesFloat(float x) {}
	void takesLong(long x) {}

	// ternary in var: suffix determines inferred type
	void ternaryInVar(boolean flag) {
		final var x = flag ? 0L : 1;
	}

	// var declarations: suffix determines the inferred type
	void varDeclarations() {
		final var a = 0L;
		final var b = 0f;
		final var c = 0d;
		final var d = 1.5f;
	}
}

class InputRedundantSuffixCleanValueGates {
	// an int literal cannot spell these values, or spells a different one
	float notExactlyRepresentable = 16777217f;
	double notExactIntoDouble = 16777217f;
	double[] notExactNewArray = new double[]{16777217f};
	float intMaxRoundsInFloat = 2147483647f;
	float pastIntRangeFloat = 2147483648f;
	float farPastIntRangeFloat = 99999999999f;
	double pastIntRangeDouble = 2147483648d;
	double farPastIntRangeDouble = 99999999999d;
	double octalLookingDouble = 010d;
	float octalLookingFloat = 010F;
	float invalidOctalFloat = 09f;
	double invalidOctalDouble = 08D;
	float leadingZeroUnderscoreFloat = 0_1f;

	long hexPastInt = 0x8000_0000L;
	long hexAllOnes32 = 0xFFFF_FFFFL;
	long octalPastInt = 020_000_000_000L;
	long binaryPastInt = 0b1000_0000_0000_0000_0000_0000_0000_0000L;
	long minLong = -9_223_372_036_854_775_808L;
	long hexAllOnes64 = 0xFFFF_FFFF_FFFF_FFFFL;

	// a minus keeps a floating-point zero negative, but the int zero has no sign
	float negativeZeroFloat = -0f;
	double negativeZeroDouble = -0d;
	float negativeZeroUppercase = -0F;
	double negativeZeroOctalSpelling = -00d;
	float negatedParenthesizedZero = -(0f);
	float[] negativeZeroNewArray = new float[]{-0f};

	// checkstyle lexes an unsuffixed decimal as NUM_FLOAT, but there is no suffix to remove
	double trailingDot = 1.;
	double trailingDotPair = 1., trailingDotPairTwo = 2.;
	double leadingDot = .5;
	double signedExponent = 1e+2;
}

class InputRedundantSuffixCleanTargets {
	static class Base {
		Base(long size) {}
	}

	static class Derived extends Base {
		Derived() {
			super(1L);
		}
	}

	static class GenericBox<T> {
		T value;
	}

	@interface GatedDefaults {
		float negativeZero() default -0f;

		double notExact() default 16777217f;

		long shifted() default 1L << 2;

		String text() default "a" + 1L;

		double value() default 0;
	}

	record ObjectComponent(Object value) {
		ObjectComponent {
			value = 1L;
		}
	}

	enum Sized {
		SMALL(1L);

		Sized(long size) {}
	}

	static final boolean NEVER = false;

	static Object staticTarget;

	boolean flag;
	GenericBox<Long> box = new GenericBox<>();
	Long[] boxedArrayTarget;
	Double boxedDoubleTarget;
	Object[] boxedNewArray = new Long[]{1L};
	Long cStyleBoxedArray[] = {1L};
	Long boxedTarget;
	char charTarget;
	Comparable<Long> comparableLong = 1L;
	String concatenatedFloat = "a" + 1f;
	double doubleTarget;
	float floatTarget;
	int intTarget;
	double floatArmIntoDouble = flag ? 1f : intTarget;
	long longTarget;
	Object lowercaseIntoObject = 100l;
	Object[][] nestedObjectArray = {{1L}};
	Number numberLong = 1L;
	Object[] objectArray = {1L};
	Object objectDouble = 1d;
	Object objectFloat = 1f;
	Object objectLong = 1L;
	Object[] objectNewArray = new Object[]{1L};
	long primitiveField;
	LongUnaryOperator reassignImplicit = v -> {
		v = 1L;
		return v;
	};
	LongUnaryOperator reassignVarParam = (var v) -> {
		v = 1L;
		return v;
	};
	Serializable serializableLong = 1L;
	String stringTarget = "";
	Object target;
	Object[] targets = new Object[1];
	Object ternaryIntoObject = flag ? 1L : 2L;

	static {
		staticTarget = 1L;
	}

	{
		target = 1f;
	}

	InputRedundantSuffixCleanTargets() {
		target = 1d;
	}

	@GatedDefaults(1L + 1)
	void annotatedArithmetic() {}

	@GatedDefaults(NEVER ? 1f : 16777217)
	void annotatedFloatArm() {}

	@GatedDefaults(negativeZero = -0f)
	void annotatedNegativeZero() {}

	@GatedDefaults(16777217f)
	void annotatedNotExact() {}

	void arithmetic(long value) {
		final long minus = value - 1L;
		final long div = value / 2L;
		final long mod = value % 2L;
		final long or = value | 1L;
		final long xor = value ^ 1L;
		final long sr = value >> 1L;
		final long bsr = value >>> 1L;
		System.out.println(minus + div + mod + or + xor + sr + bsr);
	}

	void assertMessage() {
		assert flag : 1f;
	}

	void assignBoxedNewArray() {
		boxedArrayTarget = new Long[]{1L};
	}

	void assignObjectTargets() {
		target = 1L;
		targets[0] = 1L;
		target = (-1L);
		target = ~1L;
		target = flag ? 1L : 2L;
		boxedTarget = 1L;
	}

	void assignSwitchIntoObject(int k) {
		target = switch (k) {
			case 1 -> 2L;
			default -> 1L;
		};
	}

	void assignThisObjectFieldShadowedByLongParam(long target) {
		this.target = 1L;
		System.out.println(target);
	}

	void assignThroughTypeArgument() {
		box.value = 1L;
	}

	void callArguments() {
		takesOverloaded(1L);
		takesGeneric(1L);
		takesVarargs(1L, 2L);
		takesOverloaded(flag ? 1L : 2L);
	}

	void castReceivers() {
		final boolean same = ((Object) 1L).equals(1L);
		final long hash = ((Object) 1f).hashCode();
		final boolean identical = target == (Object) 1L;
		System.out.println(same || identical || hash > 0);
	}

	void comparisons(long value) {
		final boolean eq = value == 1L;
		final boolean ne = value != 1L;
		final boolean lt = value < 1L;
		final boolean gt = value > 1L;
		final boolean le = value <= 1L;
		final boolean ge = value >= 1L;
		final boolean reversed = 1L == value;
		final boolean intVsFloat = (int) value == 16777216f;
		System.out.println(eq || ne || lt || gt || le || ge || reversed || intVsFloat);
	}

	void compoundAssignmentsNotReported() {
		intTarget += 1L;
		intTarget += 1f;
		intTarget += 1d;
		intTarget <<= 1L;
		charTarget += 1L;
		charTarget += 1f;
		charTarget += 1d;
		longTarget += 1f;
		longTarget += 1d;
		floatTarget += 1d;
		boxedTarget += 1L;
		boxedDoubleTarget += 1L;
		boxedDoubleTarget += 1f;
		boxedDoubleTarget += 1d;
		stringTarget += 1L;
		stringTarget += 1f;
		stringTarget += 1d;
		floatTarget += -0f;
		doubleTarget += -0d;
		doubleTarget += flag ? 1f : intTarget;
	}

	void floatArmsIntoDouble(int k) {
		final double cast = (double) (flag ? 1f : intTarget);
		final double switched = switch (k) {
			case 1 -> 1f;
			default -> intTarget;
		};
		System.out.println(cast + switched);
	}

	void forEachBoxed(List<Long> values) {
		for (var v : values) {
			v = 1L;
			System.out.println(v);
		}
	}

	void forEachBoxedNewArray() {
		for (Object o : new Long[]{1L})
			System.out.println(o);
	}

	<T> T generic() {
		return null;
	}

	void localShadowsLongField() {
		final Object primitiveField;
		primitiveField = 1L;
		System.out.println(primitiveField);
	}

	void referenceAndGatedCasts() {
		final Object boxedCast = (Long) 1L;
		final Object objectCast = (Object) 1L;
		final Object numberCast = (Number) 1f;
		final Object intersectionCast = (Comparable<Long> & Serializable) 1L;
		final double notExactCast = (double) 16777217f;
		final double negativeZeroCast = (double) -0f;
		final long arithmeticCast = (long) (1L + 2);
		System.out.println(List.of(boxedCast, objectCast, numberCast, intersectionCast));
		System.out.println(notExactCast + negativeZeroCast + arithmeticCast);
	}

	long returnsAfterLambda() {
		final LongSupplier inner = () -> {
			return 1L;
		};
		return inner.getAsLong();
	}

	Comparable<Long> returnsComparable() {
		return 1L;
	}

	double returnsFloatArmIntoDouble() {
		return flag ? 1f : intTarget;
	}

	float returnsNegativeZero() {
		return -0f;
	}

	double returnsNotExactFloat() {
		return 16777217f;
	}

	Number returnsNumber() {
		return 1d;
	}

	Object returnsObjectLong() {
		return 1L;
	}

	Object returnsTernaryIntoObject() {
		return flag ? 1L : 2L;
	}

	long returnsYield(int k) {
		return switch (k) {
			case 1 -> 2;
			default -> {
				System.out.println(k);
				yield 1L;
			}
		};
	}

	<T> void takesGeneric(T value) {}

	void takesOverloaded(long x) {}

	void takesOverloaded(Long x) {}

	void takesVarargs(long... values) {}

	void varFromGenericCall() {
		var v = generic();
		v = 1L;
		System.out.println(v);
	}
}