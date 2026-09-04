package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;

class NoArrayTrailingCommaFixer implements CheckstyleFixer {
	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int comma) {
		final var line = lines.get(lineIndex);
		if (comma < 0 || comma >= line.length())
			return new SkipResult(SkipMessages.FIX_POSITION_BOUNDS);
		if (line.charAt(comma) != ',')
			return new SkipResult(SkipMessages.NO_ARRAY_TRAILING_COMMA_SKIP_STALE);
		final var fixed = (line.substring(0, comma) + line.substring(comma + 1)).stripTrailing();
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}