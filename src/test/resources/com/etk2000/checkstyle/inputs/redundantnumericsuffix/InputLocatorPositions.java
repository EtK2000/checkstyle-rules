package com.etk2000.checkstyle.inputs.redundantnumericsuffix;

/**
 * Positions for {@code RedundantNumericSuffixLocatorTest}, which addresses them directly rather
 * than through a reported violation. Nothing here is a slice: the file carries no markers and is
 * never fix-run. The fixer deletes the literal's last character, so the rows that matter most are
 * the ones asserting a position does <em>not</em> classify, and the unsuffixed spellings the lexer
 * still hands the check: stripping {@code 1.} would retype it and stripping {@code 1e+2} would not
 * even parse.
 */
public class InputLocatorPositions {
	long decimalUpperL = 1L;
	long decimalLowerL = 1l;
	long hexUpperL = 0xFFL;
	long hexLowerL = 0xFFl;
	long binaryUpperL = 0b1010L;
	long binaryUpperPrefixL = 0B1010L;
	long octalUpperL = 0777L;
	long underscoreL = 1_000L;
	long hexUnderscoreL = 0xFF_FFL;

	float noFractionF = 5f;
	float decimalLowerF = 1.5f;
	float decimalUpperF = 1.5F;
	float trailingDotF = 1.f;
	float leadingDotF = .5f;
	float exponentF = 1e2f;
	float exponentPlusF = 1e+2f;
	float exponentMinusUpperF = 1E-2F;
	float hexExponentF = 0x1p3f;

	double noFractionD = 5d;
	double decimalLowerD = 1.5d;
	double decimalUpperD = 1.5D;
	double trailingDotD = 1.d;
	double leadingDotD = .5d;
	double exponentPlusD = 1e+2d;
	double exponentMinusUpperD = 1E-2D;
	double hexExponentUpperP = 0X1P3D;
	double hexExponentMinusP = 0x1p-3d;
	double hexExponentPlusP = 0x1p+3d;
	double underscoreExponentD = 1_0.0_1e+1_0d;

	int plainInt = 5;
	long hexIntBeforeOperator = 0x1E + 2L;
	long intBeforeOperator = 5 + 2L;
	double unsuffixedTrailingDot = 1.;
	double unsuffixedLeadingDot = .5;
	double unsuffixedSignedExponent = 1e+2;
	double unsuffixedDecimal = 1.5;

	float negatedFloat = -1f;
	float spacedNegatedFloat = - 1f;

	String stringHoldingSuffix = "100L";
	String textBlockHoldingSuffix = """
			100L
			""";
	long trailingCommentSuffix = 1L; // 2L
	long afterBlockComment = /* note */ 1L;

	long[] twoOnOneLine = {3L, 4L};
	long[] supplementaryAhead = {"𝒜".length(), 5L};
	long[] literalEndingTheLine = {
		6L
	};

	long methodArgument = Math.abs(7L);
}