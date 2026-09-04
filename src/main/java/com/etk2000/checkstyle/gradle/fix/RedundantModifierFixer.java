package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantModifierFixer implements CheckstyleFixer {
	private static final Set<String> MODIFIER_KEYWORDS = Set.of(
			"abstract", "default", "final", "native", "private", "protected",
			"public", "static", "strictfp", "synchronized", "transient", "volatile"
	);

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int start) {
		final var line = lines.get(lineIndex);
		if (start < 0 || start >= line.length())
			return null;

		var end = start;
		while (end < line.length() && Character.isLetter(line.charAt(end)))
			++end;
		if (end <= start)
			return null;

		// guard against stale columns: if a prior same-line fixer rewrote text to the
		// left of this violation column, the letter run here may be a non-modifier word
		// (e.g. `compareTo`) that we must not silently truncate
		if (!MODIFIER_KEYWORDS.contains(line.substring(start, end)))
			return new SkipResult(SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);

		while (end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t'))
			++end;

		final var fixed = line.substring(0, start) + line.substring(end);
		if (fixed.isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}