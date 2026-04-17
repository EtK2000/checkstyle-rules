package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoEnumTrailingSemicolonFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length() || line.charAt(column) != ';')
			return null;
		final var before = line.substring(0, column);
		final var after = line.substring(column + 1);
		final var joined = before.endsWith(" ") && after.startsWith(" ")
				? before + after.substring(1)
				: before + after;
		final var fixed = joined.stripTrailing();
		if (fixed.isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}