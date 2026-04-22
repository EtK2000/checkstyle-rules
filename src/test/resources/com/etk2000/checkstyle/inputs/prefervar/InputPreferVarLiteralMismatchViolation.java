package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarLiteralMismatchViolation {
	void allPrimitiveSameTypeLiterals() {
		// same-type primitive literals — var infers the correct type, safe to flag
		final boolean b = true; // violation: Local variable must use 'var' instead of an explicit type.
		final char c = 'a'; // violation: Local variable must use 'var' instead of an explicit type.
		final double d = 5.0; // violation: Local variable must use 'var' instead of an explicit type.
		final float f = 5.0f; // violation: Local variable must use 'var' instead of an explicit type.
		final int i = 5; // violation: Local variable must use 'var' instead of an explicit type.
		final long l = 5L; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void castToMatchingType(Object obj, int x) {
		// cast type matches declared type — var infers the cast type, safe
		final float cf = (float) x; // violation: Local variable must use 'var' instead of an explicit type.
		final long cl = (long) x; // violation: Local variable must use 'var' instead of an explicit type.
		final String cs = (String) obj; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void doubleFromFloatLiteral() {
		final double d = 5.0f; // violation: Local variable must use 'var' instead of an explicit type.
		final double dExp = 5e2f; // violation: Local variable must use 'var' instead of an explicit type.
		final double dNeg = -5.0f; // violation: Local variable must use 'var' instead of an explicit type.
		final double dSep = 1_000.0f; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void doubleFromIntLiteral() {
		final double d = 5; // violation: Local variable must use 'var' instead of an explicit type.
		final double dBin = 0b101; // violation: Local variable must use 'var' instead of an explicit type.
		final double dHex = 0xFF; // violation: Local variable must use 'var' instead of an explicit type.
		final double dNeg = -5; // violation: Local variable must use 'var' instead of an explicit type.
		final double dOct = 077; // violation: Local variable must use 'var' instead of an explicit type.
		final double dSep = 1_000; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void doubleFromLongLiteral() {
		final double d = 5L; // violation: Local variable must use 'var' instead of an explicit type.
		final double dNeg = -5L; // violation: Local variable must use 'var' instead of an explicit type.
		final double dSep = 1_000L; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void floatFromDoubleLiteral() {
		final float f = 5.0; // violation: Local variable must use 'var' instead of an explicit type.
		final float fExp = 5e2; // violation: Local variable must use 'var' instead of an explicit type.
		final float fNeg = -5.0; // violation: Local variable must use 'var' instead of an explicit type.
		final float fSep = 1_000.0; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void floatFromIntLiteral() {
		final float f = 5; // violation: Local variable must use 'var' instead of an explicit type.
		final float fBin = 0b101; // violation: Local variable must use 'var' instead of an explicit type.
		final float fHex = 0xFF; // violation: Local variable must use 'var' instead of an explicit type.
		final float fNeg = -5; // violation: Local variable must use 'var' instead of an explicit type.
		final float fOct = 077; // violation: Local variable must use 'var' instead of an explicit type.
		final float fSep = 1_000; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void longFromIntLiteral() {
		final long l = 5; // violation: Local variable must use 'var' instead of an explicit type.
		final long lBin = 0b101; // violation: Local variable must use 'var' instead of an explicit type.
		final long lHex = 0xFF; // violation: Local variable must use 'var' instead of an explicit type.
		final long lNeg = -5; // violation: Local variable must use 'var' instead of an explicit type.
		final long lOct = 077; // violation: Local variable must use 'var' instead of an explicit type.
		final long lSep = 1_000; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void parseBoolean() {
		final boolean b = Boolean.parseBoolean("true"); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void parseByte() {
		// matching return type (error)
		final byte b = Byte.parseByte("5"); // violation: Local variable must use 'var' instead of an explicit type.
		// widening to other primitives (warning — var would infer byte)
		final double bd = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float bf = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int bi = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long bl = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final short bs = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void parseDouble() {
		final double d = Double.parseDouble("5.0"); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void parseFloat() {
		// matching return type (error)
		final float f = Float.parseFloat("5.0"); // violation: Local variable must use 'var' instead of an explicit type.
		// widening (warning — var would infer float)
		final double fd = Float.parseFloat("5.0"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void parseInt() {
		// matching return type (error)
		final int i = Integer.parseInt("5"); // violation: Local variable must use 'var' instead of an explicit type.
		// widening (warning — var would infer int)
		final double id = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float ifl = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long il = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void parseLong() {
		// matching return type (error)
		final long l = Long.parseLong("5"); // violation: Local variable must use 'var' instead of an explicit type.
		// widening (warning — var would infer long)
		final double ld = Long.parseLong("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float lf = Long.parseLong("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void parseShort() {
		// matching return type (error)
		final short s = Short.parseShort("5"); // violation: Local variable must use 'var' instead of an explicit type.
		// widening (warning — var would infer short)
		final double sd = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float sf = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int si = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long sl = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void primitiveWithNonLiteralExpression(int a, byte b, boolean flag) {
		// non-literal expression on a primitive — can't verify inferred type (warning)
		final float fAdd = a + b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int fCast = (byte) a; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long lMul = a * b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long lShift = a << b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long tern = flag ? 1L : 2L; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}