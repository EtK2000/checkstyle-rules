package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;

class NoEnumTrailingSemicolonFixer implements CheckstyleFixer {
	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int semi) {
		final var line = lines.get(lineIndex);
		if (semi < 0 || semi >= line.length())
			return new SkipResult(SkipMessages.FIX_POSITION_BOUNDS);
		if (line.charAt(semi) != ';')
			return new SkipResult(SkipMessages.NO_ENUM_TRAILING_SEMICOLON_SKIP_STALE);
		final var before = line.substring(0, semi);
		final var after = line.substring(semi + 1);
		final var joined = before.endsWith(" ") && after.startsWith(" ")
				? before + after.substring(1)
				: before + after;
		final var fixed = joined.stripTrailing();
		if (fixed.isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}