package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class UpperEllFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static boolean isLiteralChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '.';
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		var end = column;
		while (end < line.length() && isLiteralChar(line.charAt(end)))
			++end;
		if (end <= column)
			return null;

		final var suffixPos = end - 1;
		if (line.charAt(suffixPos) != 'l')
			return null;

		final var fixed = line.substring(0, suffixPos) + 'L' + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}