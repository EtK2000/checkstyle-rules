package com.etk2000.checkstyle.inputs.literalsuffix;

class InputLiteralSuffixClean {
	// both operands are variables, cast on left
	void bothVariablesCastOnLeft() {
		int x = 5;
		int y = 10;
		long z = (long) x * y;
	}

	// both operands are variables, cast on right
	void bothVariablesCastOnRight() {
		int x = 5;
		int y = 10;
		long z = y * (long) x;
	}

	// cast on whole expression, not an operand
	void castOnWholeExpression() {
		int x = 5;
		long y = (long) (x * 100);
	}

	// cast to byte (no suffix exists)
	void castToByte() {
		int x = 5;
		byte y = (byte) x;
	}

	// cast to int (no suffix exists)
	void castToInt() {
		byte x = 5;
		int y = (int) x * 100;
	}

	// cast to short (no suffix exists)
	void castToShort() {
		byte x = 5;
		short y = (short) x;
	}

	// literal already has the right suffix (no cast needed)
	void literalAlreadyHasSuffix() {
		int x = 5;
		long y = x * 100L;
	}

	// literal is a double (not NUM_INT, no cast needed)
	void literalIsDouble() {
		int x = 5;
		double d = x * 1.5;
	}

	// literal is a float (not NUM_INT, no cast needed)
	void literalIsFloat() {
		int x = 5;
		float f = x * 1.5f;
	}

	// not in arithmetic context (method arguments)
	void notArithmeticArgs() {
		int x = 5;
		takesLong((long) x);
	}

	// sibling is a complex expression, cast on left
	void siblingIsExpressionCastOnLeft() {
		int x = 5;
		int y = 10;
		long z = (long) x * (y + 100);
	}

	// sibling is a complex expression, cast on right
	void siblingIsExpressionCastOnRight() {
		int x = 5;
		int y = 10;
		long z = (y + 100) * (long) x;
	}

	// sibling is NUM_LONG with different cast type
	void siblingIsLongLiteralDifferentCast() {
		int x = 5;
		double d = (double) x * 100L;
	}

	void takesLong(long x) {}

	// ternary: other branch is not a literal
	void ternaryOtherBranchNotLiteral(boolean flag) {
		int x = 5;
		int y = 10;
		long z = flag ? (long) x : y;
	}
}