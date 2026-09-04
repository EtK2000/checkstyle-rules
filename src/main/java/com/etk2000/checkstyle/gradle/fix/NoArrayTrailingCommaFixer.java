package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoArrayTrailingCommaFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int comma) {
		final var line = lines.get(lineIndex);
		if (comma < 0 || comma >= line.length() || line.charAt(comma) != ',')
			return null;
		final var fixed = (line.substring(0, comma) + line.substring(comma + 1)).stripTrailing();
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}