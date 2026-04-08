package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class LambdaParameterTypeFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static int findArrow(@Nonnull String line, int fromIndex) {
		final var idx = line.indexOf("->", fromIndex);
		if (idx < 0)
			return -1;
		// verify it's a lambda arrow, not in a comment or string
		return idx;
	}

	/**
	 * Finds the opening paren that matches the closing paren before the arrow.
	 * Tracks paren depth to handle nested expressions.
	 */
	@CheckReturnValue
	private static int findLambdaOpenParen(@Nonnull String line, int arrowStart) {
		// search backwards from arrow for the closing paren of the parameter list
		var closeParenIdx = -1;
		for (var i = arrowStart - 1; i >= 0; --i) {
			final var ch = line.charAt(i);
			if (ch == ')') {
				closeParenIdx = i;
				break;
			}
			if (!Character.isWhitespace(ch))
				return -1; // no paren before arrow, naked param
		}
		if (closeParenIdx < 0)
			return -1;

		// find matching open paren
		var depth = 1;
		for (var i = closeParenIdx - 1; i >= 0; --i) {
			if (line.charAt(i) == ')')
				++depth;
			else if (line.charAt(i) == '(') {
				--depth;
				if (depth == 0)
					return i;
			}
		}
		return -1;
	}

	@Nonnull
	private static FixResult fixRemoveParens(
			@Nonnull String line,
			int lineIndex,
			int openParen,
			int closeParen
	) {
		final var inside = line.substring(openParen + 1, closeParen).trim();
		final var fixed = line.substring(0, openParen) + inside + line.substring(closeParen + 1);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	@Nonnull
	private static FixResult fixTypes(
			@Nonnull String line,
			int lineIndex,
			int openParen,
			int closeParen
	) {
		final var paramSection = line.substring(openParen + 1, closeParen);
		final var params = paramSection.split(",");

		// if any param has annotations, all must use var (Java requires uniform form)
		var anyAnnotated = false;
		for (var param : params) {
			if (param.contains("@")) {
				anyAnnotated = true;
				break;
			}
		}

		final var sb = new StringBuilder();
		for (var j = 0; j < params.length; ++j) {
			if (j > 0)
				sb.append(", ");
			final var param = params[j].trim();
			if (anyAnnotated) {
				if (param.contains("@"))
					sb.append(replaceTypeWithVar(param));
				else {
					// non-annotated param in annotated context: replace type with var
					final var lastSpace = param.lastIndexOf(' ');
					if (lastSpace >= 0)
						sb.append("var ").append(param.substring(lastSpace + 1));
					else
						sb.append(param);
				}
			}
			else {
				// remove type: keep only the param name (last word)
				final var lastSpace = param.lastIndexOf(' ');
				if (lastSpace >= 0)
					sb.append(param.substring(lastSpace + 1));
				else
					sb.append(param);
			}
		}

		// single non-annotated param: go straight to naked form (no parens)
		if (params.length == 1 && !anyAnnotated) {
			final var fixed = line.substring(0, openParen) + sb + line.substring(closeParen + 1);
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}
		final var fixed = line.substring(0, openParen + 1) + sb + line.substring(closeParen);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	@Nonnull
	private static String replaceTypeWithVar(@Nonnull String param) {
		// param looks like "@A @B String name" or "@A String[] name"
		// find the end of annotations, then replace the type with var
		var lastAnnotationEnd = 0;
		var i = 0;
		while (i < param.length()) {
			if (param.charAt(i) == '@') {
				do ++i;
				while (i < param.length()
						&& (Character.isLetterOrDigit(param.charAt(i)) || param.charAt(i) == '.'));

				if (i < param.length() && param.charAt(i) == '(') {
					var depth = 1;
					++i;
					while (i < param.length() && depth > 0) {
						if (param.charAt(i) == '(')
							++depth;
						else if (param.charAt(i) == ')')
							--depth;
						++i;
					}
				}
				while (i < param.length() && Character.isWhitespace(param.charAt(i)))
					++i;
				lastAnnotationEnd = i;
			}
			else
				break;
		}
		final var afterAnnotations = param.substring(lastAnnotationEnd);
		final var lastSpace = afterAnnotations.lastIndexOf(' ');
		if (lastSpace < 0)
			return param;
		final var paramName = afterAnnotations.substring(lastSpace + 1);
		return param.substring(0, lastAnnotationEnd) + "var " + paramName;
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var arrowIdx = findArrow(line, column);
		if (arrowIdx < 0)
			return null;

		final var openParen = findLambdaOpenParen(line, arrowIdx);
		if (openParen < 0)
			return null;

		// find the closing paren
		var closeParen = -1;
		for (var i = arrowIdx - 1; i > openParen; --i) {
			if (line.charAt(i) == ')') {
				closeParen = i;
				break;
			}
		}
		if (closeParen < 0)
			return null;

		final var paramSection = line.substring(openParen + 1, closeParen).trim();

		// check if this is a single implicit param with unnecessary parens
		if (!paramSection.isEmpty() && !paramSection.contains(" ") && !paramSection.contains(","))
			return fixRemoveParens(line, lineIndex, openParen, closeParen);

		return fixTypes(line, lineIndex, openParen, closeParen);
	}
}