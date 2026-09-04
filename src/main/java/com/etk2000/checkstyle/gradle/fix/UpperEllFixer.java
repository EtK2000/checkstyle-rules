package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.util.List;

import javax.annotation.Nonnull;

class UpperEllFixer implements CheckstyleFixer {
	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int start) {
		final var line = lines.get(lineIndex);
		if (start < 0 || start >= line.length())
			return new SkipResult(SkipMessages.FIX_POSITION_BOUNDS);

		// the check reported a literal ending in 'l' here, so anything else means a sibling fix on
		// the line (RedundantNumericSuffix dropping the suffix) moved or rewrote it. The token scan
		// also runs over letters, so a literal is told from an identifier like `val` or `x1l` by its
		// leading digit and the absence of an identifier char before it
		final var end = LineText.literalTokenEnd(line, start);
		final var suffixPos = end - 1;
		if (end <= start || line.charAt(suffixPos) != 'l' || !Character.isDigit(line.charAt(start))
				|| (start > 0 && LineText.isIdentifierPart(line.charAt(start - 1))))
			return new SkipResult(SkipMessages.UPPER_ELL_SKIP_STALE);

		final var fixed = line.substring(0, suffixPos) + 'L' + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}