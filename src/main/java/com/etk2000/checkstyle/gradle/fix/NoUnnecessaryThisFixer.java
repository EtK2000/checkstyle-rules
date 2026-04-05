package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoUnnecessaryThisFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var thisStart = column - 4;
		if (thisStart < 0 || column >= line.length())
			return null;
		if (!line.startsWith("this.", thisStart))
			return null;
		final var fixed = line.substring(0, thisStart) + line.substring(column + 1);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}