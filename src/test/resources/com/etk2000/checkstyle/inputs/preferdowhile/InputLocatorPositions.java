package com.etk2000.checkstyle.inputs.preferdowhile;

/**
 * Positions for {@code PreferDoWhileLocatorTest}, which addresses them directly rather than through
 * a reported violation. Nothing here is a slice: the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	int doWhileTerminator(int i) {
		++i;
		do ++i;
		while (i < 10);
		return i;
	}

	int labelledWhile(int i) {
		++i;
		lbl:
		while (i < 10)
			++i;
		return i;
	}

	int nestedWhilePair(boolean cond, int i) {
		while (cond) {
			++i;
			while (i < 10)
				++i;
		}
		return i;
	}

	int sameColumnWhilePair(int i, int j) {
		++i;
		while (i < 10)
			++i;
		--j;
		while (j > 0)
			--j;
		return i + j;
	}

	int supplementaryBeforeWhile(int i) {
		++i;
		/* 𝕏 */ while (i < 10)
			++i;
		return i;
	}

	int whileAtColumnZero(int i) {
		++i;
while (i < 10)
			++i;
		return i;
	}

	int whileInsideACommentAndAString(int i) {
		++i;
		while (i < 10) // while (j < 10)
			++i;
		final var text = "while (k < 10)";
		return i + text.length();
	}

	int whileNoDuplicate(int i) {
		++i;
		while (i < 10)
			--i;
		return i;
	}

	int whileWithDuplicate(int i) {
		++i;
		while (i < 10)
			++i;
		return i;
	}
}