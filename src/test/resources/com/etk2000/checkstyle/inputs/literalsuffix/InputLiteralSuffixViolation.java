package com.etk2000.checkstyle.inputs.literalsuffix;

class InputLiteralSuffixViolation {
	// cast to double with int literal
	void castToDoubleMultiply() {
		int x = 5;
		double d = (double) x * 100; // violation: use 100d
	}

	// cast to float with int literal
	void castToFloatMultiply() {
		int x = 5;
		float f = (float) x * 100; // violation: use 100f
	}

	// cast to long with int literal — addition
	void castToLongAdd() {
		int x = 5;
		long y = (long) x + 100; // violation: use 100L
	}

	// cast to long with int literal — bitwise AND
	void castToLongBitwiseAnd() {
		int x = 0xFF;
		long y = (long) x & 255; // violation: use 255L
	}

	// cast to long with int literal — bitwise OR
	void castToLongBitwiseOr() {
		int x = 0xFF;
		long y = (long) x | 255; // violation: use 255L
	}

	// cast to long with int literal — bitwise XOR
	void castToLongBitwiseXor() {
		int x = 0xFF;
		long y = (long) x ^ 255; // violation: use 255L
	}

	// cast to long with int literal — division
	void castToLongDivide() {
		int x = 100;
		long y = (long) x / 10; // violation: use 10L
	}

	// cast to long with int literal — comparison equals
	void castToLongEquals() {
		int x = 5;
		boolean b = (long) x == 100; // violation: use 100L
	}

	// cast to long with int literal — comparison greater than
	void castToLongGreaterThan() {
		int x = 5;
		boolean b = (long) x > 100; // violation: use 100L
	}

	// cast to long with int literal — modulo
	void castToLongModulo() {
		int x = 100;
		long y = (long) x % 7; // violation: use 7L
	}

	// cast to long with int literal — multiply
	void castToLongMultiply() {
		int x = 5;
		long y = (long) x * 100; // violation: use 100L
	}

	// cast to long with int literal — shift left
	void castToLongShiftLeft() {
		int x = 1;
		long y = (long) x << 32; // violation: use 32L
	}

	// cast to long with int literal — shift right
	void castToLongShiftRight() {
		int x = Integer.MIN_VALUE;
		long y = (long) x >> 1; // violation: use 1L
	}

	// cast to long with int literal — subtraction
	void castToLongSubtract() {
		int x = 100;
		long y = (long) x - 50; // violation: use 50L
	}

	// cast to long with int literal — unsigned shift right
	void castToLongUnsignedShiftRight() {
		int x = -1;
		long y = (long) x >>> 1; // violation: use 1L
	}

	// hex literal
	void hexLiteral() {
		int x = 5;
		long y = (long) x * 0xFF; // violation: use 0xFFL
	}

	// literal on the left, cast on the right — arithmetic
	void literalOnLeftArithmetic() {
		int x = 5;
		long y = 100 + (long) x; // violation: use 100L
	}

	// literal on the left, cast on the right — bitwise
	void literalOnLeftBitwise() {
		int x = 5;
		long y = 255 & (long) x; // violation: use 255L
	}

	// literal on the left, cast on the right — comparison
	void literalOnLeftComparison() {
		int x = 5;
		boolean b = 100 < (long) x; // violation: use 100L
	}

	// literal on the left, cast on the right — double
	void literalOnLeftDouble() {
		int x = 5;
		double d = 100 * (double) x; // violation: use 100d
	}

	// literal on the left, cast on the right — float
	void literalOnLeftFloat() {
		int x = 5;
		float f = 100 * (float) x; // violation: use 100f
	}

	// literal on the left, cast on the right — shift
	void literalOnLeftShift() {
		int x = 5;
		long y = 1 << (long) x; // violation: use 1L
	}

	// negative literal on left
	void negatedLiteralLeft() {
		int x = 5;
		long y = -100 + (long) x; // violation: use -100L
	}

	// negative literal on right
	void negatedLiteralRight() {
		int x = 5;
		long y = (long) x * -100; // violation: use -100L
	}

	// positive unary on right
	void positiveUnaryLiteralRight() {
		int x = 5;
		long y = (long) x * +100; // violation: use +100L
	}

	// ternary with double cast
	void ternaryCastDouble(boolean flag) {
		int x = 5;
		double d = flag ? (double) x : 0; // violation: use 0d
	}

	// ternary with float cast
	void ternaryCastFloat(boolean flag) {
		int x = 5;
		float f = flag ? (float) x : 0; // violation: use 0f
	}

	// ternary: cast is false branch, literal is true branch
	void ternaryCastOnFalse(boolean flag) {
		int x = 5;
		long y = flag ? 0 : (long) x; // violation: use 0L
	}

	// ternary: cast is false branch — double
	void ternaryCastOnFalseDouble(boolean flag) {
		int x = 5;
		double d = flag ? 0 : (double) x; // violation: use 0d
	}

	// ternary: cast is false branch — float
	void ternaryCastOnFalseFloat(boolean flag) {
		int x = 5;
		float f = flag ? 0 : (float) x; // violation: use 0f
	}

	// ternary: cast is true branch, literal is false branch
	void ternaryCastOnTrue(boolean flag) {
		int x = 5;
		long y = flag ? (long) x : 0; // violation: use 0L
	}

	// ternary: cast is true branch, negative literal is false branch
	void ternaryCastWithNegativeLiteral(boolean flag) {
		int x = 5;
		long y = flag ? (long) x : -1; // violation: use -1L
	}
}