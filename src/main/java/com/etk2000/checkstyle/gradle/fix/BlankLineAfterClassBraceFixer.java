package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes blank lines after class/interface/enum/record opening braces.
 * The violation line points to the class declaration line (where the regex
 * match starts). The fixer scans forward to find the opening brace, then
 * deletes consecutive blank lines after it.
 */
class BlankLineAfterClassBraceFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// find the line containing the opening brace
		var braceLine = -1;
		for (var i = lineIndex; i < lines.size(); ++i) {
			if (lines.get(i).contains("{")) {
				braceLine = i;
				break;
			}
		}
		if (braceLine == -1)
			return null;

		// find consecutive blank lines after the brace line
		final var blankStart = braceLine + 1;
		if (blankStart >= lines.size() || !lines.get(blankStart).isBlank())
			return null;

		var blankEnd = blankStart;
		while (blankEnd + 1 < lines.size() && lines.get(blankEnd + 1).isBlank())
			++blankEnd;

		return new FixResult(blankStart, blankEnd, List.of());
	}
}