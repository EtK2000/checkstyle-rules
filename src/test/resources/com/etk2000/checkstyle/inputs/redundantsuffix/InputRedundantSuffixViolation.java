package com.etk2000.checkstyle.inputs.redundantsuffix;

import java.util.function.DoubleSupplier;

class InputRedundantSuffixViolation {
	// field declarations
	long fieldLong = 0L; // violation: redundant L suffix
	long fieldLong2 = 100L; // violation: redundant L suffix
	long fieldLong3 = -1L; // violation: redundant L suffix
	float fieldFloat = 0f; // violation: redundant f suffix
	float fieldFloat2 = 100F; // violation: redundant F suffix
	double fieldDouble = 0d; // violation: redundant d suffix
	double fieldDouble2 = 0.0d; // violation: redundant d suffix
	double fieldDouble3 = 3.14D; // violation: redundant D suffix

	// hex, octal, binary with redundant suffix
	long hexLong = 0xFFL; // violation: redundant L suffix
	long octalLong = 07L; // violation: redundant L suffix
	long binaryLong = 0b1010L; // violation: redundant L suffix
	double hexDouble = 0x1.0p10d; // violation: redundant d suffix

	// static and final fields
	static long staticLong = 0L; // violation: redundant L suffix
	static final long CONSTANT = 0L; // violation: redundant L suffix

	// negative values
	float negativeFloat = -0f; // violation: redundant f suffix
	double negativeDouble = -0.0d; // violation: redundant d suffix

	// array initializers
	long[] longArray = {0L, 1L}; // violation x2: redundant L suffix
	float[] floatArray = {0f}; // violation: redundant f suffix
	double[] doubleArray = {0.0d, 1.0D}; // violation x2: redundant d suffix

	// int boundary: INT_MAX fits in int, so L is redundant
	long intMax = 2_147_483_647L; // violation: redundant L suffix

	// mixed array: only integer-valued suffix is flagged
	float[] mixedArray = {0f, 1.5f}; // violation: only 0f flagged

	// d suffix on decimal is always redundant, even in:
	double arithmeticDouble = 1.0 + 0.0d; // violation: redundant d suffix
	DoubleSupplier lambdaDecimalD = () -> 0.0d; // violation: redundant d suffix

	// cast expression
	void castExpression() {
		long x = (long) 0L; // violation: redundant L suffix
	}

	// compound assignment
	void compoundAssignment() {
		long x = 0;
		x += 0L; // violation: redundant L suffix
	}

	// local variable declarations
	void localVariables() {
		long a = 0L; // violation: redundant L suffix
		long b = 1_000L; // violation: redundant L suffix
		float c = 0f; // violation: redundant f suffix
		double d = 0.0d; // violation: redundant d suffix
	}

	void methodArgDecimalD() {
		takesDouble(0.0d); // violation: redundant d suffix
	}

	// new array expression
	void newArrayExpression() {
		long[] arr = new long[]{0L}; // violation: redundant L suffix
	}

	// reassignment
	void reassignment() {
		long x = 0;
		x = 0L; // violation: redundant L suffix
	}

	// return statements
	double returnDouble() {
		return 0.0d; // violation: redundant d suffix
	}

	float returnFloat() {
		return 0f; // violation: redundant f suffix
	}

	long returnLong() {
		return 0L; // violation: redundant L suffix
	}

	void takesDouble(double x) {}

	// ternary in typed variable
	void ternary(boolean flag) {
		long x = flag ? 0L : 1L; // violation x2: redundant L suffix
	}

	void varDecimalD() {
		var x = 0.0d; // violation: redundant d suffix (0.0 is already double)
	}
}