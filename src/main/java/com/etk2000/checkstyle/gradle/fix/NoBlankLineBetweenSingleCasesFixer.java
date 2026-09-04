package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Deletes the blank lines separating the reported case from the one above it.
 * The gap may also hold comments, which the check counts as separation but the
 * rule does not ask to remove, so they are re-emitted and only the blank lines
 * go.
 */
class NoBlankLineBetweenSingleCasesFixer implements CheckstyleFixer {
	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return new SkipResult(SkipMessages.FIX_BOUNDS);
		// a comment line masks to blank, so one walk covers both kinds of gap line
		// and stops at the previous case's code
		final var masked = JavaLineScanner.maskAll(lines);
		var start = lineIndex - 1;
		while (start >= 0 && masked.get(start).isBlank())
			--start;
		++start;

		final var kept = new ArrayList<String>();
		var sawBlank = false;
		for (var i = start; i < lineIndex; ++i) {
			if (lines.get(i).isBlank())
				sawBlank = true;
			else
				kept.add(lines.get(i));
		}
		// the check reports only a gap holding a blank line, so an empty or comment-only one means a
		// sibling fix in the pass rewrote the lines above the case
		if (!sawBlank)
			return new SkipResult(SkipMessages.NO_BLANK_CASES_SKIP_STALE);
		return new FixResult(start, lineIndex - 1, kept);
	}
}