package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes blank lines before closing braces. For a single blank line, the
 * violation points to the content line before the blank. For 2+ blank
 * lines, the regex match starts on a blank line within the group. The
 * fixer scans both directions from {@code lineIndex} to find and delete
 * all consecutive blank lines in the group.
 */
class BlankLineBeforeClosingBraceFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		var blankStart = lines.get(lineIndex).isBlank() ? lineIndex : lineIndex + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		// scan backward to find the first blank in the group
		while (blankStart > 0 && lines.get(blankStart - 1).isBlank())
			--blankStart;

		// scan forward to find the last blank in the group
		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		return new FixResult(blankStart, blankEnd, List.of());
	}
}