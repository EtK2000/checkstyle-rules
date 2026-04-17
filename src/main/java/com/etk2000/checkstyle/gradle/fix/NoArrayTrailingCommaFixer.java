package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoArrayTrailingCommaFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length() || line.charAt(column) != ',')
			return null;
		final var fixed = (line.substring(0, column) + line.substring(column + 1)).stripTrailing();
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}