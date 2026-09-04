package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes missing blank lines after {@code break;} before the next
 * {@code case}/{@code default}. The violation line is the {@code break;} line.
 * The fixer inserts an empty line after it.
 *
 * <p>An insertion point that is text-block content is refused; see
 * {@link TextBlockGuard}.
 */
class BlankLineAfterBreakFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var nextLine = lineIndex + 1;
		if (nextLine >= lines.size())
			return null;

		if (lines.get(nextLine).isBlank())
			return null;

		if (TextBlockGuard.containsTextBlockContent(lines, lineIndex, nextLine))
			return new SkipResult(SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);

		return new FixResult(nextLine, nextLine - 1, List.of(""));
	}
}