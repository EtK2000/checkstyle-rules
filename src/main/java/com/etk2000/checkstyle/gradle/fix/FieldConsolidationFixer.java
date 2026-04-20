package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FieldConsolidationFixer implements CheckstyleFixer {
	@Nullable
	private static String extractFieldNames(@Nonnull String line, int column, boolean keepBrackets) {
		if (column < 0 || column >= line.length())
			return null;
		if (!Character.isJavaIdentifierStart(line.charAt(column)))
			return null;

		final var sb = new StringBuilder();
		var pos = column;
		while (pos < line.length()) {
			if (!Character.isJavaIdentifierStart(line.charAt(pos)))
				break;
			final var nameStart = pos;
			while (pos < line.length() && Character.isJavaIdentifierPart(line.charAt(pos)))
				++pos;
			sb.append(line, nameStart, pos);

			final var bracketStart = pos;
			while (pos < line.length() && (line.charAt(pos) == '[' || line.charAt(pos) == ']'))
				++pos;
			if (keepBrackets && pos > bracketStart)
				sb.append(line, bracketStart, pos);

			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;

			if (pos < line.length() && line.charAt(pos) == ',') {
				sb.append(", ");
				++pos;
				while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
					++pos;
			}
			else
				break;
		}
		return sb.isEmpty() ? null : sb.toString();
	}

	@CheckReturnValue
	private static int findFieldSemicolon(@Nonnull String line) {
		var inBlockComment = false;
		var inString = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/')
					inBlockComment = false;
			}
			else if (inString) {
				if (c == '\\')
					++i;
				else if (c == '"')
					inString = false;
			}
			else if (c == '/' && i + 1 < line.length()) {
				if (line.charAt(i + 1) == '*')
					inBlockComment = true;
				else if (line.charAt(i + 1) == '/')
					return -1;
			}
			else if (c == '"')
				inString = true;
			else if (c == ';')
				return i;
		}
		return -1;
	}

	private static int findPrevFieldLine(@Nonnull List<String> lines, int lineIndex) {
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var trimmed = lines.get(i).trim();
			if (trimmed.endsWith(";"))
				return i;
			if (!trimmed.startsWith("@") && !trimmed.isEmpty())
				return -1;
		}
		return -1;
	}

	private static boolean hasCStyleArrayBrackets(@Nonnull String line, int semiIdx) {
		var i = semiIdx - 1;
		while (i >= 0 && Character.isWhitespace(line.charAt(i)))
			--i;
		return i >= 0 && line.charAt(i) == ']';
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 1 || lineIndex >= lines.size())
			return null;

		final var prev = findPrevFieldLine(lines, lineIndex);
		if (prev < 0)
			return null;

		final var prevLine = lines.get(prev);
		final var semiIdx = findFieldSemicolon(prevLine);
		if (semiIdx < 0)
			return null;

		final var prevCStyle = hasCStyleArrayBrackets(prevLine, semiIdx);
		final var violationLine = lines.get(lineIndex);
		final var violationSemiIdx = violationLine.indexOf(';', column);
		final var currCStyle = violationSemiIdx > column && hasCStyleArrayBrackets(violationLine, violationSemiIdx);

		if (prevCStyle && !currCStyle)
			return null;

		final var names = extractFieldNames(violationLine, column, prevCStyle && currCStyle);
		if (names == null || names.isEmpty())
			return null;

		final var merged = prevLine.substring(0, semiIdx) + ", " + names + prevLine.substring(semiIdx);
		return new FixResult(prev, lineIndex, List.of(merged));
	}
}