package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes double blank lines by collapsing consecutive blank lines to a single one.
 * The violation line points to the line before the blank line group.
 */
class DoubleBlankLineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var blankStart = lineIndex + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		// find end of consecutive blank lines
		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		// only fix if there are 2+ consecutive blank lines
		if (blankEnd - blankStart < 1)
			return null;

		// delete all but the first blank line
		return new FixResult(blankStart + 1, blankEnd, List.of());
	}
}