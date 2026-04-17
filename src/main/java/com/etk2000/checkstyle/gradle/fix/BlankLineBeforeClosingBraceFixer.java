package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes blank lines before closing braces.
 * The violation line points to the last content line before the blank lines
 * (where the regex match starts). The fixer deletes all consecutive blank
 * lines between the content and the closing brace.
 */
class BlankLineBeforeClosingBraceFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var blankStart = lineIndex + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		return new FixResult(blankStart, blankEnd, List.of());
	}
}