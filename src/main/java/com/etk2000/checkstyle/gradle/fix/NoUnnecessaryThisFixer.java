package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoUnnecessaryThisFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int dot) {
		final var line = lines.get(lineIndex);
		final var thisStart = dot - 4;
		if (thisStart < 0 || dot >= line.length())
			return null;
		if (!line.startsWith("this.", thisStart))
			return null;
		final var fixed = line.substring(0, thisStart) + line.substring(dot + 1);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}