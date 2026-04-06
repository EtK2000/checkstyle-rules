package com.etk2000.checkstyle.inputs.literalsuffix;

class InputLiteralSuffixClean {
	// both operands are variables, cast on left
	void bothVariablesCastOnLeft() {
		final int x = 5;
		final int y = 10;
		final long z = (long) x * y;
	}

	// both operands are variables, cast on right
	void bothVariablesCastOnRight() {
		final int x = 5;
		final int y = 10;
		final long z = y * (long) x;
	}

	// cast on whole expression, not an operand
	void castOnWholeExpression() {
		final int x = 5;
		final long y = (long) (x * 100);
	}

	// cast to byte (no suffix exists)
	void castToByte() {
		final int x = 5;
		final byte y = (byte) x;
	}

	// cast to int (no suffix exists)
	void castToInt() {
		final byte x = 5;
		final int y = (int) x * 100;
	}

	// cast to short (no suffix exists)
	void castToShort() {
		final byte x = 5;
		final short y = (short) x;
	}

	// literal already has the right suffix (no cast needed)
	void literalAlreadyHasSuffix() {
		final int x = 5;
		final long y = x * 100L;
	}

	// literal is a double (not NUM_INT, no cast needed)
	void literalIsDouble() {
		final int x = 5;
		final double d = x * 1.5;
	}

	// literal is a float (not NUM_INT, no cast needed)
	void literalIsFloat() {
		final int x = 5;
		final float f = x * 1.5f;
	}

	// not in arithmetic context (method arguments)
	void notArithmeticArgs() {
		final int x = 5;
		takesLong((long) x);
	}

	// sibling is a complex expression, cast on left
	void siblingIsExpressionCastOnLeft() {
		final int x = 5;
		final int y = 10;
		final long z = (long) x * (y + 100);
	}

	// sibling is a complex expression, cast on right
	void siblingIsExpressionCastOnRight() {
		final int x = 5;
		final int y = 10;
		final long z = (y + 100) * (long) x;
	}

	// sibling is NUM_LONG with different cast type
	void siblingIsLongLiteralDifferentCast() {
		final int x = 5;
		final double d = (double) x * 100L;
	}

	void takesLong(long x) {}

	// ternary: other branch is not a literal
	void ternaryOtherBranchNotLiteral(boolean flag) {
		final int x = 5;
		final int y = 10;
		final long z = flag ? (long) x : y;
	}
}