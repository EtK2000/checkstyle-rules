package com.etk2000.checkstyle.inputs.explicitinitialization;

class InputExplicitInitializationClean {
	boolean flagNonDefault = true;
	char nonNullChar = 'a';
	char unicodeNullChar = '\u0000';
	double negativeZeroDouble = -0.0;
	double nonZeroDouble = 1.0;
	double nonZeroDoubleExponent = 1.0e1;
	double nonZeroDoubleLowercaseSuffix = 1.0d;
	double nonZeroDoubleSmallNegativeExponent = 1.0e-300;
	double nonZeroDoubleUppercaseSuffix = 1.0D;
	double nonZeroLeadingDot = .1;
	float nonZeroFloat = 1.0f;
	float nonZeroFloatWithoutDecimal = 1F;
	float nonZeroHexFloat = 0x1.0p0f;
	int countNonDefault = 5;
	int nonZeroBinary = 0b1;
	int nonZeroBinaryUppercasePrefix = 0B1;
	int nonZeroHex = 0x1;
	int nonZeroHexDDigit = 0x0D;
	int nonZeroHexFDigit = 0x0F;
	int nonZeroHexUppercaseEDigit = 0x0E1;
	int nonZeroHexUppercasePrefix = 0X1;
	int nonZeroOctal = 01;
	long nonZeroLong = 1L;
	long nonZeroLongLowercaseSuffix = 1l;
	String name;
	String stringValue = "hello";

	void forInitLocalNotFlagged() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}

	void methodLocalNotFlagged() {
		final var local = 0;
		System.out.println(local);
	}
}