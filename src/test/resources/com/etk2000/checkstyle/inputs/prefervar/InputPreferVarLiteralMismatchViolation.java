package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarLiteralMismatchViolation {
	void allPrimitiveSameTypeLiterals() {
		// same-type primitive literals — var infers the correct type, safe to flag
		final boolean b = true; // violation: local must use var
		final char c = 'a'; // violation: local must use var
		final double d = 5.0; // violation: local must use var
		final float f = 5.0f; // violation: local must use var
		final int i = 5; // violation: local must use var
		final long l = 5L; // violation: local must use var
	}

	void castToMatchingType(Object obj, int x) {
		// cast type matches declared type — var infers the cast type, safe
		final float cf = (float) x; // violation: local must use var
		final long cl = (long) x; // violation: local must use var
		final String cs = (String) obj; // violation: local must use var
	}

	void doubleFromFloatLiteral() {
		final double d = 5.0f; // violation: local must use var
		final double dExp = 5e2f; // violation: local must use var
		final double dNeg = -5.0f; // violation: local must use var
		final double dSep = 1_000.0f; // violation: local must use var
	}

	void doubleFromIntLiteral() {
		final double d = 5; // violation: local must use var
		final double dBin = 0b101; // violation: local must use var
		final double dHex = 0xFF; // violation: local must use var
		final double dNeg = -5; // violation: local must use var
		final double dOct = 077; // violation: local must use var
		final double dSep = 1_000; // violation: local must use var
	}

	void doubleFromLongLiteral() {
		final double d = 5L; // violation: local must use var
		final double dNeg = -5L; // violation: local must use var
		final double dSep = 1_000L; // violation: local must use var
	}

	void floatFromDoubleLiteral() {
		final float f = 5.0; // violation: local must use var
		final float fExp = 5e2; // violation: local must use var
		final float fNeg = -5.0; // violation: local must use var
		final float fSep = 1_000.0; // violation: local must use var
	}

	void floatFromIntLiteral() {
		final float f = 5; // violation: local must use var
		final float fBin = 0b101; // violation: local must use var
		final float fHex = 0xFF; // violation: local must use var
		final float fNeg = -5; // violation: local must use var
		final float fOct = 077; // violation: local must use var
		final float fSep = 1_000; // violation: local must use var
	}

	void longFromIntLiteral() {
		final long l = 5; // violation: local must use var
		final long lBin = 0b101; // violation: local must use var
		final long lHex = 0xFF; // violation: local must use var
		final long lNeg = -5; // violation: local must use var
		final long lOct = 077; // violation: local must use var
		final long lSep = 1_000; // violation: local must use var
	}

	void parseBoolean() {
		final boolean b = Boolean.parseBoolean("true"); // violation: matching return type
	}

	void parseByte() {
		// matching return type (error)
		final byte b = Byte.parseByte("5"); // violation: matching return type
		// widening to other primitives (warning — var would infer byte)
		final double bd = Byte.parseByte("5"); // violation (warning): returns byte
		final float bf = Byte.parseByte("5"); // violation (warning): returns byte
		final int bi = Byte.parseByte("5"); // violation (warning): returns byte
		final long bl = Byte.parseByte("5"); // violation (warning): returns byte
		final short bs = Byte.parseByte("5"); // violation (warning): returns byte
	}

	void parseDouble() {
		final double d = Double.parseDouble("5.0"); // violation: matching return type
	}

	void parseFloat() {
		// matching return type (error)
		final float f = Float.parseFloat("5.0"); // violation: matching return type
		// widening (warning — var would infer float)
		final double fd = Float.parseFloat("5.0"); // violation (warning): returns float
	}

	void parseInt() {
		// matching return type (error)
		final int i = Integer.parseInt("5"); // violation: matching return type
		// widening (warning — var would infer int)
		final double id = Integer.parseInt("5"); // violation (warning): returns int
		final float ifl = Integer.parseInt("5"); // violation (warning): returns int
		final long il = Integer.parseInt("5"); // violation (warning): returns int
	}

	void parseLong() {
		// matching return type (error)
		final long l = Long.parseLong("5"); // violation: matching return type
		// widening (warning — var would infer long)
		final double ld = Long.parseLong("5"); // violation (warning): returns long
		final float lf = Long.parseLong("5"); // violation (warning): returns long
	}

	void parseShort() {
		// matching return type (error)
		final short s = Short.parseShort("5"); // violation: matching return type
		// widening (warning — var would infer short)
		final double sd = Short.parseShort("5"); // violation (warning): returns short
		final float sf = Short.parseShort("5"); // violation (warning): returns short
		final int si = Short.parseShort("5"); // violation (warning): returns short
		final long sl = Short.parseShort("5"); // violation (warning): returns short
	}

	void primitiveWithNonLiteralExpression(int a, byte b, boolean flag) {
		// non-literal expression on a primitive — can't verify inferred type (warning)
		final float fAdd = a + b; // violation (warning): binary op
		final int fCast = (byte) a; // violation (warning): cast to different primitive
		final long lMul = a * b; // violation (warning): binary op
		final long lShift = a << b; // violation (warning): shift op
		final long tern = flag ? 1L : 2L; // violation (warning): ternary
	}
}