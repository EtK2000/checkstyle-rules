package com.etk2000.checkstyle.inputs.redundantcast;

import java.util.List;

class InputRedundantCastViolation {
	// same-type casts on literals
	char sameTypeChar = (char) 'a'; // violation: redundant cast to char
	double sameTypeDouble = (double) 5.0; // violation: redundant cast to double
	float sameTypeFloat = (float) 5.0f; // violation: redundant cast to float
	int sameTypeInt = (int) 5; // violation: redundant cast to int
	long sameTypeLong = (long) 5L; // violation: redundant cast to long
	String sameTypeString = (String) "hello"; // violation: redundant cast to String

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
		final Object o = (String) null; // violation: null assignable without cast
	}

	// null cast in assignment (target type matches cast type)
	void nullCastInAssignment() {
		final String s = (String) null; // violation: null assignable without cast
	}

	// null cast in return (return type matches cast type)
	String nullCastInReturn() {
		return (String) null; // violation: null assignable without cast
	}

	// null cast in return (cast type differs from return type)
	Object nullCastInReturnDifferent() {
		return (String) null; // violation: null assignable without cast
	}

	// same-type cast on field
	void sameTypeCastField() {
		final int x = (int) sameTypeInt; // violation: same type as field
	}

	// same-type cast on for-each variable
	void sameTypeCastForEach(List<String> list) {
		for (String s : list)
			System.out.println((String) s); // violation: same type
	}

	// same-type cast on for-init variable
	void sameTypeCastForInit() {
		for (int i = 0; i < 10; i++)
			System.out.println((int) i); // violation: same type
	}

	// same-type cast on new
	void sameTypeCastNew() {
		final Object x = (InputRedundantCastViolation) new InputRedundantCastViolation(); // violation: same type
	}

	// same-type cast on parameter (reference type)
	void sameTypeCastParameter(String s) {
		final String x = (String) s; // violation: same type
	}

	// same-type cast on this
	InputRedundantCastViolation sameTypeCastThis() {
		return (InputRedundantCastViolation) this; // violation: same type
	}

	// same-type cast on variable (primitive)
	void sameTypeCastVariable() {
		final int x = 5;
		final int y = (int) x; // violation: same type
	}

	// same-type cast on variable (reference type)
	void sameTypeCastVariableRef() {
		final String s = "hi";
		final String t = (String) s; // violation: same type
	}

	// same-type cast on nested cast
	void sameTypeNestedCast(Object obj) {
		final String s = (String) (String) obj; // violation: outer cast same as inner
	}

	// widening byte to int in assignment
	void wideningByteToInt() {
		final byte b = 5;
		final int x = (int) b; // violation: widening
	}

	// widening byte to short in assignment
	void wideningByteToShort() {
		final byte b = 5;
		final short x = (short) b; // violation: widening
	}

	// widening char to int in assignment
	void wideningCharToInt() {
		final char c = 'a';
		final int x = (int) c; // violation: widening
	}

	// widening float to double in assignment
	void wideningFloatToDouble() {
		final float f = 5.0f;
		final double d = (double) f; // violation: widening
	}

	// widening in return statement
	long wideningInReturn() {
		final int x = 5;
		return (long) x; // violation: widening
	}

	// widening in return to wider primitive than cast
	double wideningInReturnWider() {
		final int x = 5;
		return (long) x; // violation: widening int to long, return is double (primitive)
	}

	// widening in standalone reassignment
	void wideningInStandaloneAssign() {
		final int x = 5;
		long y = 0;
		y = (long) x; // violation: widening, target is long
	}

	// widening in ternary false branch with long true branch
	void wideningInTernaryFalseBranch(boolean flag) {
		final int x = 5;
		final long y = flag ? 0L : (long) x; // violation: 0L is already long
	}

	// widening in ternary return from primitive method
	long wideningInTernaryReturn(boolean flag) {
		final int x = 5;
		return flag ? (long) x : 0L; // violation: return type is long and sibling is long
	}

	// widening in ternary true branch with long false branch
	void wideningInTernaryTrueBranch(boolean flag) {
		final int x = 5;
		final long y = flag ? (long) x : 0L; // violation: 0L is already long
	}

	// widening in ternary with long reassignment target
	void wideningInTernaryWithLongReassignment(boolean flag) {
		final int x = 5;
		final int z = 10;
		long y = 0;
		y = flag ? (long) x : z; // violation: reassignment target is long
	}

	// widening in ternary with long assignment target
	void wideningInTernaryWithLongTarget(boolean flag) {
		final int x = 5;
		final int z = 10;
		final long y = flag ? (long) x : z; // violation: assignment target is long
	}

	// widening int to double in assignment
	void wideningIntToDouble() {
		final int x = 5;
		final double d = (double) x; // violation: widening
	}

	// widening int to float in assignment
	void wideningIntToFloat() {
		final int x = 5;
		final float f = (float) x; // violation: widening
	}

	// widening int to long in assignment
	void wideningIntToLong() {
		final int x = 5;
		final long y = (long) x; // violation: widening
	}

	// widening long to double in assignment
	void wideningLongToDouble() {
		final long x = 5;
		final double d = (double) x; // violation: widening
	}

	// widening long to float in assignment
	void wideningLongToFloat() {
		final long x = 5;
		final float f = (float) x; // violation: widening
	}

	// widening from parameter
	void wideningOnParameter(int x) {
		final long y = (long) x; // violation: widening
	}

	// widening short to int in assignment
	void wideningShortToInt() {
		final short s = 5;
		final int x = (int) s; // violation: widening
	}

	// widening short to long in assignment
	void wideningShortToLong() {
		final short s = 5;
		final long x = (long) s; // violation: widening
	}

	// sibling already has cast type (double literal)
	void wideningWithDoubleLiteralSibling() {
		final int x = 5;
		final double d = (double) x * 1.5; // violation: 1.5 is already double
	}

	// sibling already has cast type (float literal)
	void wideningWithFloatLiteralSibling() {
		final int x = 5;
		final float f = (float) x * 1.5f; // violation: 1.5f is already float
	}

	// sibling already has cast type (long literal)
	void wideningWithLongLiteralSibling() {
		final int x = 5;
		final long y = (long) x * 100L; // violation: 100L already makes it long
	}

	// sibling already has cast type (long variable)
	void wideningWithLongVariableSibling() {
		final int x = 5;
		final long y = 10;
		final long z = (long) x * y; // violation: y is already long
	}

	// widening in comparison (sibling is wider)
	void wideningWithWiderComparison() {
		final int x = 5;
		final long y = 10;
		final boolean b = (long) x == y; // violation: y is already long
	}
}