package com.etk2000.checkstyle.inputs.redundantcast;

import java.util.List;

class InputRedundantCastViolation {
	char sameTypeChar = (char) 'a'; // violation: Redundant cast to 'char', expression is already 'char'.
	double sameTypeDouble = (double) 5.0; // violation: Redundant cast to 'double', expression is already 'double'.
	float sameTypeFloat = (float) 5.0f; // violation: Redundant cast to 'float', expression is already 'float'.
	int sameTypeInt = (int) 5; // violation: Redundant cast to 'int', expression is already 'int'.
	long sameTypeLong = (long) 5L; // violation: Redundant cast to 'long', expression is already 'long'.
	String sameTypeString = (String) "hello"; // violation: Redundant cast to 'String', expression is already 'String'.

	// widening cast in compound assignments (always redundant)
	void compoundAssignments() {
		long x = 5;
		final int y = 10;
		x &= (long) y; // violation
		x |= (long) y; // violation
		x >>>= (long) y; // violation
		x ^= (long) y; // violation
		x /= (long) y; // violation
		x -= (long) y; // violation
		x %= (long) y; // violation
		x += (long) y; // violation
		x <<= (long) y; // violation
		x >>= (long) y; // violation
		x *= (long) y; // violation
	}

	// null cast in assignment (cast type differs from target)
	void nullCastDifferentTarget() {
		final Object o = (String) null; // violation: Redundant cast to 'String', expression is already 'null'.
	}

	void nullCastInAssignment() {
		final String s = (String) null; // violation: Redundant cast to 'String', expression is already 'null'.
	}

	String nullCastInReturn() {
		return (String) null; // violation: Redundant cast to 'String', expression is already 'null'.
	}

	Object nullCastInReturnDifferent() {
		return (String) null; // violation: Redundant cast to 'String', expression is already 'null'.
	}

	void sameTypeCastField() {
		final int x = (int) sameTypeInt; // violation: Redundant cast to 'int', expression is already 'int'.
	}

	void sameTypeCastForEach(List<String> list) {
		for (String s : list)
			System.out.println((String) s); // violation: Redundant cast to 'String', expression is already 'String'.
	}

	void sameTypeCastForInit() {
		for (int i = 0; i < 10; i++)
			System.out.println((int) i); // violation: Redundant cast to 'int', expression is already 'int'.
	}

	void sameTypeCastNew() {
		final Object x = (InputRedundantCastViolation) new InputRedundantCastViolation(); // violation: Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.
	}

	void sameTypeCastParameter(String s) {
		final String x = (String) s; // violation: Redundant cast to 'String', expression is already 'String'.
	}

	InputRedundantCastViolation sameTypeCastThis() {
		return (InputRedundantCastViolation) this; // violation: Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.
	}

	void sameTypeCastVariable() {
		final int x = 5;
		final int y = (int) x; // violation: Redundant cast to 'int', expression is already 'int'.
	}

	void sameTypeCastVariableRef() {
		final String s = "hi";
		final String t = (String) s; // violation: Redundant cast to 'String', expression is already 'String'.
	}

	void sameTypeNestedCast(Object obj) {
		final String s = (String) (String) obj; // violation: Redundant cast to 'String', expression is already 'String'.
	}

	void wideningByteToInt() {
		final byte b = 5;
		final int x = (int) b; // violation: Redundant cast to 'int', expression is already 'byte'.
	}

	void wideningByteToShort() {
		final byte b = 5;
		final short x = (short) b; // violation: Redundant cast to 'short', expression is already 'byte'.
	}

	void wideningCharToInt() {
		final char c = 'a';
		final int x = (int) c; // violation: Redundant cast to 'int', expression is already 'char'.
	}

	void wideningFloatToDouble() {
		final float f = 5.0f;
		final double d = (double) f; // violation: Redundant cast to 'double', expression is already 'float'.
	}

	long wideningInReturn() {
		final int x = 5;
		return (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	double wideningInReturnWider() {
		final int x = 5;
		return (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningInStandaloneAssign() {
		final int x = 5;
		long y = 0;
		y = (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningInTernaryFalseBranch(boolean flag) {
		final int x = 5;
		final long y = flag ? 0L : (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	long wideningInTernaryReturn(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0L; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningInTernaryTrueBranch(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : 0L; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningInTernaryWithLongReassignment(boolean flag) {
		final int x = 5;
		final int z = 10;
		long y = 0;
		y = flag ? (long) x : z; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningInTernaryWithLongTarget(boolean flag) {
		final int x = 5;
		final int z = 10;
		final long y = flag ? (long) x : z; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningIntToDouble() {
		final int x = 5;
		final double d = (double) x; // violation: Redundant cast to 'double', expression is already 'int'.
	}

	void wideningIntToFloat() {
		final int x = 5;
		final float f = (float) x; // violation: Redundant cast to 'float', expression is already 'int'.
	}

	void wideningIntToLong() {
		final int x = 5;
		final long y = (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningLongToDouble() {
		final long x = 5;
		final double d = (double) x; // violation: Redundant cast to 'double', expression is already 'long'.
	}

	void wideningLongToFloat() {
		final long x = 5;
		final float f = (float) x; // violation: Redundant cast to 'float', expression is already 'long'.
	}

	void wideningOnParameter(int x) {
		final long y = (long) x; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningShortToInt() {
		final short s = 5;
		final int x = (int) s; // violation: Redundant cast to 'int', expression is already 'short'.
	}

	void wideningShortToLong() {
		final short s = 5;
		final long x = (long) s; // violation: Redundant cast to 'long', expression is already 'short'.
	}

	void wideningWithDoubleLiteralSibling() {
		final int x = 5;
		final double d = (double) x * 1.5; // violation: Redundant cast to 'double', expression is already 'int'.
	}

	void wideningWithFloatLiteralSibling() {
		final int x = 5;
		final float f = (float) x * 1.5f; // violation: Redundant cast to 'float', expression is already 'int'.
	}

	void wideningWithLongLiteralSibling() {
		final int x = 5;
		final long y = (long) x * 100L; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningWithLongVariableSibling() {
		final int x = 5;
		final long y = 10;
		final long z = (long) x * y; // violation: Redundant cast to 'long', expression is already 'int'.
	}

	void wideningWithWiderComparison() {
		final int x = 5;
		final long y = 10;
		final boolean b = (long) x == y; // violation: Redundant cast to 'long', expression is already 'int'.
	}
}