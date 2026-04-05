package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class TrailingWhitespaceFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var stripped = line.stripTrailing();
		if (stripped.equals(line))
			return null;
		return new FixResult(lineIndex, lineIndex, List.of(stripped));
	}
}