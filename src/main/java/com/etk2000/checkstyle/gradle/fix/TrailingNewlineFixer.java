package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes trailing newlines at end of file by deleting trailing blank lines.
 * For a single trailing newline with no blank lines in the lines list, the
 * read/write cycle in {@link CheckstyleFixTask} handles it automatically
 * (readAllLines strips the trailing newline, writeString doesn't add one).
 */
class TrailingNewlineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// scan backward from the end to find trailing blank lines
		var lastBlank = lines.size();
		while (lastBlank > 0 && lines.get(lastBlank - 1).isBlank())
			--lastBlank;

		// no trailing blank lines: the read/write cycle handles the single trailing newline
		if (lastBlank == lines.size())
			return null;

		return new FixResult(lastBlank, lines.size() - 1, List.of());
	}
}