package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes double blank lines by collapsing consecutive blank lines to a single one.
 * The violation line points to the line before the blank line group.
 *
 * <p>A run that is text-block content is refused rather than collapsed; see
 * {@link TextBlockGuard}.
 */
class DoubleBlankLineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var blankStart = lineIndex + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		if (blankEnd - blankStart < 1)
			return null;

		if (TextBlockGuard.containsTextBlockContent(lines, blankStart, blankEnd))
			return new SkipResult(SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);

		return new FixResult(blankStart + 1, blankEnd, List.of());
	}
}