package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferDirectBooleanReturnFixer implements CheckstyleFixer {
	private record BodyInfo(int startLine, int endLine, boolean value) {}

	@CheckReturnValue
	private static int findMatchingClosingParen(@Nonnull String line, int openParenIndex) {
		var depth = 1;
		var inString = false;
		var inChar = false;
		for (var i = openParenIndex + 1; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return -1;
			if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return -1;
				++i;
				continue;
			}
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(')
				++depth;
			else if (c == ')') {
				if (--depth == 0)
					return i;
			}
		}
		return -1;
	}

	@CheckReturnValue
	private static boolean isAtomicCond(@Nonnull String s) {
		var depth = 0;
		final var len = s.length();
		var i = 0;
		while (i < len) {
			final var cp = s.codePointAt(i);
			if (cp == '(')
				++depth;
			else if (cp == ')')
				--depth;
			else if (depth == 0
					&& !Character.isLetterOrDigit(cp)
					&& cp != '_'
					&& cp != '.'
					&& cp != '$'
					&& !Character.isWhitespace(cp))
				return false;
			i += Character.charCount(cp);
		}
		return depth == 0;
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseBody(
			@Nonnull List<String> lines,
			int ifLineIndex,
			@Nonnull String indent,
			@Nonnull String afterCond
	) {
		return switch (afterCond) {
			case "" ->
					parseSingleReturnLine(lines, ifLineIndex + 1, indent + "\t", ifLineIndex + 1);
			case "return false;" -> new BodyInfo(ifLineIndex, ifLineIndex, false);
			case "return true;" -> new BodyInfo(ifLineIndex, ifLineIndex, true);
			case "{" -> {
				if (ifLineIndex + 2 >= lines.size())
					yield null;
				if (!(indent + "}").equals(lines.get(ifLineIndex + 2)))
					yield null;
				yield parseSingleReturnLine(lines, ifLineIndex + 1, indent + "\t", ifLineIndex + 2);
			}
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseElse(@Nonnull List<String> lines, int candidateLineIndex, @Nonnull String indent) {
		if (candidateLineIndex >= lines.size())
			return null;
		final var firstLine = lines.get(candidateLineIndex);
		if ((indent + "else").equals(firstLine))
			return parseSingleReturnLine(lines, candidateLineIndex + 1, indent + "\t", candidateLineIndex + 1);
		if ((indent + "else {").equals(firstLine)) {
			if (candidateLineIndex + 2 >= lines.size())
				return null;
			if (!(indent + "}").equals(lines.get(candidateLineIndex + 2)))
				return null;
			return parseSingleReturnLine(lines, candidateLineIndex + 1, indent + "\t", candidateLineIndex + 2);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseSingleReturnLine(
			@Nonnull List<String> lines,
			int returnLineIndex,
			@Nonnull String returnIndent,
			int rangeEndLineIndex
	) {
		if (returnLineIndex >= lines.size())
			return null;
		final var line = lines.get(returnLineIndex);
		if ((returnIndent + "return true;").equals(line))
			return new BodyInfo(returnLineIndex, rangeEndLineIndex, true);
		if ((returnIndent + "return false;").equals(line))
			return new BodyInfo(returnLineIndex, rangeEndLineIndex, false);
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static String simplifyNegation(@Nonnull String cond) {
		final var stripped = cond.strip();
		if (stripped.startsWith("!") && !stripped.startsWith("!=")) {
			final var rest = stripped.substring(1).strip();
			if (!rest.isEmpty() && isAtomicCond(rest))
				return rest;
		}
		if (isAtomicCond(stripped))
			return "!" + stripped;
		return "!(" + stripped + ")";
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var ifLine = lines.get(lineIndex);
		if (column < 0 || column + 4 > ifLine.length())
			return null;
		if (!ifLine.startsWith("if ", column))
			return null;
		final var openParen = column + 3;
		if (openParen >= ifLine.length() || ifLine.charAt(openParen) != '(')
			return null;

		final var closeParen = findMatchingClosingParen(ifLine, openParen);
		if (closeParen == -1)
			return new SkipResult("multi-line if condition");

		final var indent = ifLine.substring(0, column);
		final var cond = ifLine.substring(openParen + 1, closeParen).strip();
		if (cond.contains("\\u"))
			return new SkipResult("Unicode escape in condition");
		final var afterCond = ifLine.substring(closeParen + 1).stripLeading();

		final var thenInfo = parseBody(lines, lineIndex, indent, afterCond);
		if (thenInfo == null)
			return null;

		final var afterThen = thenInfo.endLine() + 1;
		var elseInfo = parseElse(lines, afterThen, indent);
		if (elseInfo == null) {
			if (afterThen >= lines.size())
				return null;
			final var trailing = lines.get(afterThen);
			if ((indent + "return true;").equals(trailing))
				elseInfo = new BodyInfo(afterThen, afterThen, true);
			else if ((indent + "return false;").equals(trailing))
				elseInfo = new BodyInfo(afterThen, afterThen, false);
			else
				return null;
		}

		if (elseInfo.value() == thenInfo.value())
			return null;

		final var hint = thenInfo.value() ? cond : simplifyNegation(cond);
		return new FixResult(lineIndex, elseInfo.endLine(), List.of(indent + "return " + hint + ";"));
	}
}