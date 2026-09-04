package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Deletes the blank lines separating the reported case from the one above it.
 * The gap may also hold comments, which the check counts as separation but the
 * rule does not ask to remove, so they are re-emitted and only the blank lines
 * go.
 */
class NoBlankLineBetweenSingleCasesFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// a comment line masks to blank, so one walk covers both kinds of gap line
		// and stops at the previous case's code
		final var masked = JavaLineScanner.maskAll(lines);
		var start = lineIndex - 1;
		while (start >= 0 && masked.get(start).isBlank())
			--start;
		++start;
		if (start >= lineIndex)
			return null;

		final var kept = new ArrayList<String>();
		var sawBlank = false;
		for (var i = start; i < lineIndex; ++i) {
			if (lines.get(i).isBlank())
				sawBlank = true;
			else
				kept.add(lines.get(i));
		}
		if (!sawBlank)
			return null;
		return new FixResult(start, lineIndex - 1, kept);
	}
}