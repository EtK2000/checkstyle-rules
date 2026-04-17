package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes missing blank lines after {@code break;} before the next
 * {@code case}/{@code default}. The violation line is the {@code break;} line.
 * The fixer inserts an empty line after it.
 */
class BlankLineAfterBreakFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var nextLine = lineIndex + 1;
		if (nextLine >= lines.size())
			return null;

		// if the next line is already blank, nothing to fix
		if (lines.get(nextLine).isBlank())
			return null;

		// insert a blank line after the break line
		return new FixResult(nextLine, nextLine - 1, List.of(""));
	}
}