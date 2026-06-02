package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

import java.util.function.LongSupplier;

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