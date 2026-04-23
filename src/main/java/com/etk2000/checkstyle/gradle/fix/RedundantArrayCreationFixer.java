package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantArrayCreationFixer implements CheckstyleFixer {
	private static int findMatchingBrace(@Nonnull String line, int openBrace) {
		var depth = 0;
		var inChar = false;
		var inString = false;
		for (var i = openBrace; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '\\')
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}') {
				--depth;
				if (depth == 0)
					return i;
			}
		}
		return -1;
	}

	private static int findPrecedingComma(@Nonnull String beforeNew) {
		for (var i = beforeNew.length() - 1; i >= 0; --i) {
			final var c = beforeNew.charAt(i);
			if (c == ',')
				return i;
			if (!Character.isWhitespace(c))
				return -1;
		}
		return -1;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var openBrace = line.indexOf('{', column);
		if (openBrace < 0)
			return null;

		final var closeBrace = findMatchingBrace(line, openBrace);
		if (closeBrace < 0)
			return null;

		final var elements = line.substring(openBrace + 1, closeBrace).strip();
		final var beforeNew = line.substring(0, column);
		final var afterBrace = line.substring(closeBrace + 1);

		if (elements.isEmpty()) {
			final var commaIdx = findPrecedingComma(beforeNew);
			if (commaIdx >= 0) {
				final var fixed = line.substring(0, commaIdx) + afterBrace;
				return new FixResult(lineIndex, lineIndex, List.of(fixed));
			}
			final var fixed = beforeNew + afterBrace;
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}

		final var fixed = beforeNew + elements + afterBrace;
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}