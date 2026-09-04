package com.etk2000.checkstyle.inputs.preferliteralsuffix;

/**
 * Positions for {@code PreferLiteralSuffixLocatorTest}, which addresses them directly rather than
 * through a reported violation. Nothing here is a slice: the file carries no markers and is never
 * fix-run.
 */
public class InputLocatorPositions {
	long bitThirtyOneLiteral(int i) {
		return (long) i | 0x80000000;
	}

	long castAtColumnZero(int i) {
		return
(long) i * 100;
	}

	long castInsideACommentAndAString(int i) {
		final var text = "(long) k * 100";
		return (long) i * 100 + text.length(); // (long) j * 100
	}

	long castToInt(int i) {
		return (int) i * 100;
	}

	long castWithLiteralSibling(int i) {
		return (long) i * 100;
	}

	long castWithNegatedLiteral(int i) {
		return (long) i * -100;
	}

	long castWithNonLiteralSibling(int i, int j) {
		return (long) i * j;
	}

	long commentBeforeLiteralOperand(int i) {
		return /* c */ 100 + (long) i;
	}

	long commentBeforeTernaryColon(boolean flag, int i) {
		return flag ? 0 /* c */ : (long) i;
	}

	long commentBeforeTernaryFalseBranch(boolean flag, int i) {
		return flag ? 0 : /* c */ (long) i;
	}

	long commentBeforeTernaryTrueBranchLiteral(boolean flag, int i) {
		return flag ? (long) i : /* c */ 0;
	}

	long nestedCastPair(long i) {
		return (long) (int) i * 100;
	}

	long sameColumnCastPair(int i, int j) {
		final long a = (long) i * 100;
		final long b = (long) j * 200;
		return a + b;
	}

	long shiftCast(int i) {
		return (long) i << 32;
	}

	long supplementaryBeforeCast(int i) {
		return /* 𝕏 */ (long) i * 100;
	}

	long twoCastsOnOneLine(int a, int b) {
		return (long) a * 100 + (long) b * 200;
	}
}