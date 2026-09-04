package com.etk2000.checkstyle.inputs.prefermathmethod;

/**
 * Positions for {@code PreferMathMethodLocatorTest} and {@code PreferMathMethodIfLocatorTest},
 * which address them directly rather than through a reported violation. Nothing here is a slice:
 * the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	static class MyMath {
		static int max(int a, int b) {
			return a > b ? a : b;
		}

		static int min(int a, int b) {
			return a < b ? a : b;
		}
	}

	int absWithElse(int a, int r) {
		if (a < 0)
			r = -a;
		else
			r = a;
		return r;
	}

	int assignWithElse(int a, int b, int r) {
		if (a > b)
			r = a;
		else
			r = b;
		return r;
	}

	int chainedOnOneLine(int a, int b, int c) {
		return a > b ? a : b > c ? b : c;
	}

	int clampAndTernaryOnOneLine(int a, int b, int lo, int hi, int v) {
		return (a > b ? a : b) + Math.max(lo, Math.min(hi, v));
	}

	int declWithInitializer(int a, int b) {
		var r = 7;
		if (a > b)
			r = a;
		else
			r = b;
		return r;
	}

	int elseIfChain(boolean cond, int a, int b) {
		if (cond)
			return 0;
		else if (a > b)
			return a;
		else
			return b;
	}

	int foreignReceiver(int lo, int hi, int v) {
		return MyMath.max(lo, Math.min(hi, v));
	}

	int ifAtColumnZero(int a, int b, int r) {
if (a > b)
			r = a;
		else
			r = b;
		return r;
	}

	int ifInsideACommentAndAString(int a, int b, int r) {
		if (a > b) // if (a <= b) r = b; else r = a;
			r = a;
		else
			r = b;
		final var text = "if (a < b)";
		return r + text.length();
	}

	int initOverwrite(int a, int b) {
		var r = b;
		if (a > b)
			r = a;
		return r;
	}

	int nestedIfPair(boolean cond, int a, int b, int r) {
		if (cond)
			if (a > b)
				r = a;
			else
				r = b;
		return r;
	}

	int returnWithElse(int a, int b) {
		if (a > b)
			return a;
		else
			return b;
	}

	int sameColumnIfPair(int a, int b, int c, int d, int r, int s) {
		if (a > b)
			r = a;
		else
			r = b;
		if (c < d)
			s = c;
		else
			s = d;
		return r + s;
	}

	int[] sameColumnPair(int a, int b, int c, int d) {
		return new int[]{
				a > b ? a : b,
				c < d ? c : d
		};
	}

	int ternaryAtColumnZero(int a, int b) {
		return a > b
? a : b;
	}

	int trailingReturn(int a, int b) {
		if (a > b)
			return a;
		return b;
	}

	int twoClampsOnOneLine(int lo, int hi, int v, int lo2, int hi2, int w) {
		return Math.max(lo, Math.min(hi, v)) + Math.max(lo2, Math.min(hi2, w));
	}
}