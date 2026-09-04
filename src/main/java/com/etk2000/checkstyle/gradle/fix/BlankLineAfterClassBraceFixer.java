package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes blank lines after class/interface/enum/record opening braces.
 * The violation line points to the class declaration line (where the regex
 * match starts). The fixer scans forward to find the opening brace, then
 * deletes consecutive blank lines after it.
 *
 * <p>The brace search is textual, so it can land on a {@code &#123;} that is a text
 * block's string content; that span and any blank run inside a text block are
 * refused rather than deleted. See {@link TextBlockGuard}.
 */
class BlankLineAfterClassBraceFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		var braceLine = -1;
		for (var i = lineIndex; i < lines.size(); ++i) {
			if (lines.get(i).contains("{")) {
				braceLine = i;
				break;
			}
		}
		if (braceLine == -1)
			return null;

		final var blankStart = braceLine + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		if (TextBlockGuard.containsTextBlockContent(lines, braceLine, blankEnd))
			return new SkipResult(SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);

		return new FixResult(blankStart, blankEnd, List.of());
	}
}