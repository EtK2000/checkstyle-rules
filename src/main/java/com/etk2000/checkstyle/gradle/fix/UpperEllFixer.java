package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class UpperEllFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var end = LineText.literalTokenEnd(line, column);
		if (end <= column)
			return null;

		final var suffixPos = end - 1;
		if (line.charAt(suffixPos) != 'l')
			return null;

		final var fixed = line.substring(0, suffixPos) + 'L' + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}