package com.etk2000.checkstyle.inputs.literalsuffix;

class InputLiteralSuffixViolation {
	// cast to double with int literal
	void castToDoubleMultiply() {
		final int x = 5;
		final double d = (double) x * 100; // violation: Use 'd' suffix on '100' instead of a cast.
	}

	// cast to float with int literal
	void castToFloatMultiply() {
		final int x = 5;
		final float f = (float) x * 100; // violation: Use 'f' suffix on '100' instead of a cast.
	}

	// cast to long with int literal — addition
	void castToLongAdd() {
		final int x = 5;
		final long y = (long) x + 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// cast to long with int literal — bitwise AND
	void castToLongBitwiseAnd() {
		final int x = 0xFF;
		final long y = (long) x & 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}

	// cast to long with int literal — bitwise OR
	void castToLongBitwiseOr() {
		final int x = 0xFF;
		final long y = (long) x | 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}

	// cast to long with int literal — bitwise XOR
	void castToLongBitwiseXor() {
		final int x = 0xFF;
		final long y = (long) x ^ 255; // violation: Use 'L' suffix on '255' instead of a cast.
	}

	// cast to long with int literal — division
	void castToLongDivide() {
		final int x = 100;
		final long y = (long) x / 10; // violation: Use 'L' suffix on '10' instead of a cast.
	}

	// cast to long with int literal — comparison equals
	void castToLongEquals() {
		final int x = 5;
		final boolean b = (long) x == 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// cast to long with int literal — comparison greater than
	void castToLongGreaterThan() {
		final int x = 5;
		final boolean b = (long) x > 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// cast to long with int literal — modulo
	void castToLongModulo() {
		final int x = 100;
		final long y = (long) x % 7; // violation: Use 'L' suffix on '7' instead of a cast.
	}

	// cast to long with int literal — multiply
	void castToLongMultiply() {
		final int x = 5;
		final long y = (long) x * 100; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// cast to long with int literal — shift left
	void castToLongShiftLeft() {
		final int x = 1;
		final long y = (long) x << 32; // violation: Use 'L' suffix on '32' instead of a cast.
	}

	// cast to long with int literal — shift right
	void castToLongShiftRight() {
		final int x = Integer.MIN_VALUE;
		final long y = (long) x >> 1; // violation: Use 'L' suffix on '1' instead of a cast.
	}

	// cast to long with int literal — subtraction
	void castToLongSubtract() {
		final int x = 100;
		final long y = (long) x - 50; // violation: Use 'L' suffix on '50' instead of a cast.
	}

	// cast to long with int literal — unsigned shift right
	void castToLongUnsignedShiftRight() {
		final int x = -1;
		final long y = (long) x >>> 1; // violation: Use 'L' suffix on '1' instead of a cast.
	}

	// hex literal
	void hexLiteral() {
		final int x = 5;
		final long y = (long) x * 0xFF; // violation: Use 'L' suffix on '0xFF' instead of a cast.
	}

	// literal on the left, cast on the right — arithmetic
	void literalOnLeftArithmetic() {
		final int x = 5;
		final long y = 100 + (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// literal on the left, cast on the right — bitwise
	void literalOnLeftBitwise() {
		final int x = 5;
		final long y = 255 & (long) x; // violation: Use 'L' suffix on '255' instead of a cast.
	}

	// literal on the left, cast on the right — comparison
	void literalOnLeftComparison() {
		final int x = 5;
		final boolean b = 100 < (long) x; // violation: Use 'L' suffix on '100' instead of a cast.
	}

	// literal on the left, cast on the right — double
	void literalOnLeftDouble() {
		final int x = 5;
		final double d = 100 * (double) x; // violation: Use 'd' suffix on '100' instead of a cast.
	}

	// literal on the left, cast on the right — float
	void literalOnLeftFloat() {
		final int x = 5;
		final float f = 100 * (float) x; // violation: Use 'f' suffix on '100' instead of a cast.
	}

	// literal on the left, cast on the right — shift
	void literalOnLeftShift() {
		final int x = 5;
		final long y = 1 << (long) x; // violation: Use 'L' suffix on '1' instead of a cast.
	}

	// negative literal on left
	void negatedLiteralLeft() {
		final int x = 5;
		final long y = -100 + (long) x; // violation: Use 'L' suffix on '-100' instead of a cast.
	}

	// negative literal on right
	void negatedLiteralRight() {
		final int x = 5;
		final long y = (long) x * -100; // violation: Use 'L' suffix on '-100' instead of a cast.
	}

	// positive unary on right
	void positiveUnaryLiteralRight() {
		final int x = 5;
		final long y = (long) x * +100; // violation: Use 'L' suffix on '+100' instead of a cast.
	}

	// ternary with double cast
	void ternaryCastDouble(boolean flag) {
		final int x = 5;
		final double d = flag ? (double) x : 0; // violation: Use 'd' suffix on '0' instead of a cast.
	}

	// ternary with float cast
	void ternaryCastFloat(boolean flag) {
		final int x = 5;
		final float f = flag ? (float) x : 0; // violation: Use 'f' suffix on '0' instead of a cast.
	}

	// ternary: cast is false branch, literal is true branch
	void ternaryCastOnFalse(boolean flag) {
		final int x = 5;
		final long y = flag ? 0 : (long) x; // violation: Use 'L' suffix on '0' instead of a cast.
	}

	// ternary: cast is false branch — double
	void ternaryCastOnFalseDouble(boolean flag) {
		final int x = 5;
		final double d = flag ? 0 : (double) x; // violation: Use 'd' suffix on '0' instead of a cast.
	}

	// ternary: cast is false branch — float
	void ternaryCastOnFalseFloat(boolean flag) {
		final int x = 5;
		final float f = flag ? 0 : (float) x; // violation: Use 'f' suffix on '0' instead of a cast.
	}

	// ternary: cast is true branch, literal is false branch
	void ternaryCastOnTrue(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : 0; // violation: Use 'L' suffix on '0' instead of a cast.
	}

	// ternary: cast is true branch, negative literal is false branch
	void ternaryCastWithNegativeLiteral(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : -1; // violation: Use 'L' suffix on '-1' instead of a cast.
	}
}