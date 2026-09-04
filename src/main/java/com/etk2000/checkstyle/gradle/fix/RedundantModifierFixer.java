package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

class RedundantModifierFixer implements CheckstyleFixer {
	private static final Set<String> MODIFIER_KEYWORDS = Set.of(
			"abstract", "default", "final", "native", "private", "protected",
			"public", "static", "strictfp", "synchronized", "transient", "volatile"
	);

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int start) {
		final var line = lines.get(lineIndex);
		if (start < 0 || start >= line.length())
			return new SkipResult(SkipMessages.FIX_POSITION_BOUNDS);

		var end = LineText.identEnd(line, start);

		// guard against stale columns: if a prior same-line fixer rewrote text to the
		// left of this violation column, the word here may be a non-modifier identifier
		// (e.g. `compareTo`, `final_count`) that we must not silently truncate
		if (LineText.identStart(line, start) != start || !MODIFIER_KEYWORDS.contains(line.substring(start, end)))
			return new SkipResult(SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);

		while (end < line.length() && (line.charAt(end) == ' ' || line.charAt(end) == '\t'))
			++end;

		final var fixed = line.substring(0, start) + line.substring(end);
		if (fixed.isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}