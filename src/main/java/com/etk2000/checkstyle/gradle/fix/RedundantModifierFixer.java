package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantModifierFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		var end = column;
		while (end < line.length() && Character.isLetter(line.charAt(end)))
			++end;
		if (end <= column)
			return null;

		// also remove trailing whitespace after the keyword
		while (end < line.length() && line.charAt(end) == ' ')
			++end;

		final var fixed = line.substring(0, column) + line.substring(end);
		if (fixed.isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}