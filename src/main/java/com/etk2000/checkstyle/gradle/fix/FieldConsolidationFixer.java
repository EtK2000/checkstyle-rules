package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FieldConsolidationFixer implements CheckstyleFixer {
	private static final int MAX_LINE_LENGTH = 120;
	private static final int WRAP_TAB_WIDTH = 4;

	@CheckReturnValue
	@Nonnull
	private static List<String> buildWrappedLines(
			@Nonnull String prefix,
			@Nonnull List<String> names,
			@Nonnull String suffix,
			@Nonnull String contIndent
	) {
		final var result = new ArrayList<String>();
		var line = new StringBuilder(prefix);

		for (var i = 0; i < names.size(); ++i) {
			final var name = names.get(i);
			final var isLast = i == names.size() - 1;

			if (i == 0)
				line.append(name);
			else {
				final var withName = line + ", " + name;
				if (tabExpandedLength(withName + (isLast ? suffix : ",")) > MAX_LINE_LENGTH) {
					result.add(line + ",");
					line = new StringBuilder(contIndent + name);
				}
				else
					line = new StringBuilder(withName);
			}

			if (isLast)
				result.add(line + suffix);
		}

		return result;
	}

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
				do ++pos;
				while (pos < line.length() && Character.isWhitespace(line.charAt(pos)));
			}
			else
				break;
		}
		var result = sb.toString();
		if (result.endsWith(", "))
			result = result.substring(0, result.length() - 2);
		return result.isEmpty() ? null : result;
	}

	@CheckReturnValue
	@Nonnull
	private static String extractIndent(@Nonnull String line) {
		var i = 0;
		while (i < line.length() && (line.charAt(i) == '\t' || line.charAt(i) == ' '))
			++i;
		return line.substring(0, i);
	}

	@CheckReturnValue
	private static int findFieldNamesStart(@Nonnull String line, int semiIdx) {
		var pos = semiIdx - 1;
		while (true) {
			while (pos >= 0 && Character.isWhitespace(line.charAt(pos)))
				--pos;
			while (pos >= 0 && (line.charAt(pos) == '[' || line.charAt(pos) == ']'))
				--pos;
			if (pos < 0 || !Character.isJavaIdentifierPart(line.charAt(pos)))
				break;
			while (pos >= 0 && Character.isJavaIdentifierPart(line.charAt(pos)))
				--pos;
			var tempPos = pos;
			while (tempPos >= 0 && Character.isWhitespace(line.charAt(tempPos)))
				--tempPos;
			if (tempPos >= 0 && line.charAt(tempPos) == ',') {
				pos = tempPos - 1;
				continue;
			}
			break;
		}
		return pos + 1;
	}

	@CheckReturnValue
	private static int findFieldSemicolon(@Nonnull String line) {
		var inBlockComment = false;
		var inChar = false;
		var inString = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
			}
			else if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
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
			else if (c == '\'')
				inChar = true;
			else if (c == '"')
				inString = true;
			else if (c == ';')
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findFirstIdentStart(@Nonnull String line) {
		for (var i = 0; i < line.length(); ++i) {
			if (Character.isJavaIdentifierStart(line.charAt(i)))
				return i;
		}
		return -1;
	}

	private static int findPrevFieldLine(@Nonnull List<String> lines, int lineIndex) {
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var line = lines.get(i);
			if (findFieldSemicolon(line) >= 0 || findTrailingComma(line) >= 0)
				return i;
			final var trimmed = line.trim();
			if (!trimmed.startsWith("@") && !trimmed.isEmpty())
				return -1;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findTrailingComma(@Nonnull String line) {
		var lastComma = -1;
		var inBlockComment = false;
		var inChar = false;
		var inString = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
			}
			else if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
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
					break;
			}
			else if (c == '\'')
				inChar = true;
			else if (c == '"')
				inString = true;
			else if (c == ',')
				lastComma = i;
		}
		return lastComma;
	}

	@CheckReturnValue
	private static boolean hasBlockCommentBefore(@Nonnull String line, int end) {
		var inChar = false;
		var inString = false;
		for (var i = 0; i < end; ++i) {
			final var c = line.charAt(i);
			if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
			}
			else if (inString) {
				if (c == '\\')
					++i;
				else if (c == '"')
					inString = false;
			}
			else if (c == '/' && i + 1 < end && line.charAt(i + 1) == '*')
				return true;
			else if (c == '\'')
				inChar = true;
			else if (c == '"')
				inString = true;
		}
		return false;
	}

	private static boolean hasCStyleArrayBrackets(@Nonnull String line, int semiIdx) {
		var i = semiIdx - 1;
		while (i >= 0 && Character.isWhitespace(line.charAt(i)))
			--i;
		return i >= 0 && line.charAt(i) == ']';
	}

	private static boolean isCommentLine(@Nonnull String line) {
		final var trimmed = line.trim();
		return trimmed.startsWith("//") || trimmed.startsWith("/*");
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> parseFieldNames(@Nonnull String names) {
		final var result = new ArrayList<String>();
		var pos = 0;
		while (pos < names.length()) {
			while (pos < names.length() && Character.isWhitespace(names.charAt(pos)))
				++pos;
			if (pos >= names.length() || !Character.isJavaIdentifierStart(names.charAt(pos)))
				break;
			final var start = pos;
			while (pos < names.length() && Character.isJavaIdentifierPart(names.charAt(pos)))
				++pos;
			while (pos < names.length() && (names.charAt(pos) == '[' || names.charAt(pos) == ']'))
				++pos;
			result.add(names.substring(start, pos));
			while (pos < names.length() && (names.charAt(pos) == ',' || Character.isWhitespace(names.charAt(pos))))
				++pos;
		}
		return result;
	}

	@CheckReturnValue
	private static int tabExpandedLength(@Nonnull String line) {
		var len = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (line.charAt(i) == '\t')
				len += WRAP_TAB_WIDTH - (len % WRAP_TAB_WIDTH);
			else
				++len;
		}
		return len;
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
		var terminatorIdx = findFieldSemicolon(prevLine);
		final var prevEndsWithComma = terminatorIdx < 0;
		if (prevEndsWithComma)
			terminatorIdx = findTrailingComma(prevLine);
		if (terminatorIdx < 0)
			return null;

		final var prevCStyle = hasCStyleArrayBrackets(prevLine, terminatorIdx);
		final var violationLine = lines.get(lineIndex);
		final var violationSemiIdx = findFieldSemicolon(violationLine);
		final var currCStyle = violationSemiIdx > column && hasCStyleArrayBrackets(violationLine, violationSemiIdx);

		if (prevCStyle && !currCStyle)
			return null;

		final var names = extractFieldNames(violationLine, column, prevCStyle);
		if (names == null || names.isEmpty())
			return null;
		if (hasBlockCommentBefore(violationLine, column))
			return null;
		final var violationEnd = violationSemiIdx >= 0 ? violationSemiIdx : violationLine.length();
		if (hasBlockCommentBefore(violationLine.substring(column), violationEnd - column))
			return null;

		var endLine = lineIndex;
		var allNames = names;
		var stoppedAtComment = false;
		if (violationSemiIdx < 0) {
			final var violationIndentWidth = tabExpandedLength(extractIndent(violationLine));
			for (var i = lineIndex + 1; i < lines.size(); ++i) {
				final var contLine = lines.get(i);
				if (tabExpandedLength(extractIndent(contLine)) <= violationIndentWidth)
					break;
				if (isCommentLine(contLine)) {
					stoppedAtComment = true;
					break;
				}
				final var contIdentStart = findFirstIdentStart(contLine);
				if (contIdentStart < 0)
					break;
				final var contNames = extractFieldNames(contLine, contIdentStart, prevCStyle);
				if (contNames != null && !contNames.isEmpty())
					allNames = allNames + ", " + contNames;
				endLine = i;
				if (findFieldSemicolon(contLine) >= 0)
					break;
			}
		}

		final String suffix;
		if (prevEndsWithComma) {
			final var lastLine = lines.get(endLine);
			final var lastSemiIdx = findFieldSemicolon(lastLine);
			if (lastSemiIdx >= 0)
				suffix = lastLine.substring(lastSemiIdx);
			else {
				final var lastCommaIdx = findTrailingComma(lastLine);
				suffix = lastCommaIdx >= 0 ? lastLine.substring(lastCommaIdx) : ",";
			}
		}
		else if (stoppedAtComment) {
			final var lastLine = lines.get(endLine);
			final var lastCommaIdx = findTrailingComma(lastLine);
			suffix = lastCommaIdx >= 0 ? lastLine.substring(lastCommaIdx) : prevLine.substring(terminatorIdx);
		}
		else if (endLine > lineIndex) {
			final var lastLine = lines.get(endLine);
			final var lastSemiIdx = findFieldSemicolon(lastLine);
			suffix = lastSemiIdx >= 0 ? lastLine.substring(lastSemiIdx) : prevLine.substring(terminatorIdx);
		}
		else
			suffix = prevLine.substring(terminatorIdx);

		final var merged = prevLine.substring(0, terminatorIdx) + ", " + allNames + suffix;

		if (tabExpandedLength(merged) <= MAX_LINE_LENGTH)
			return new FixResult(prev, endLine, List.of(merged));

		final var newTerminatorIdx = terminatorIdx + 2 + allNames.length();
		final var fieldNamesStart = findFieldNamesStart(merged, newTerminatorIdx);
		final var prefix = merged.substring(0, fieldNamesStart);
		final var namesRegion = merged.substring(fieldNamesStart, newTerminatorIdx);
		final var wrapSuffix = merged.substring(newTerminatorIdx);

		final var namesList = parseFieldNames(namesRegion);
		if (namesList.size() < 2)
			return new FixResult(prev, endLine, List.of(merged));

		final var baseIndent = extractIndent(prevLine);
		final var contIndent = baseIndent + "\t\t";
		return new FixResult(prev, endLine, buildWrappedLines(prefix, namesList, wrapSuffix, contIndent));
	}
}