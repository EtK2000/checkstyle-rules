package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes a trailing newline at end of file by deleting the trailing blank
 * line(s). The file is read via
 * {@link CheckstyleFixAction#splitPreservingTrailingNewline}, which represents
 * an end-of-file newline as a trailing blank line, so a lone trailing newline
 * is deleted the same way as multiple trailing blank lines.
 */
class TrailingNewlineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		var lastBlank = lines.size();
		while (lastBlank > 0 && lines.get(lastBlank - 1).isBlank())
			--lastBlank;

		// unreachable for a real violation: a NoTrailingNewline file always ends
		// with a terminator, which the read step materializes as a trailing blank
		if (lastBlank == lines.size())
			return null;

		return new FixResult(lastBlank, lines.size() - 1, List.of());
	}
}