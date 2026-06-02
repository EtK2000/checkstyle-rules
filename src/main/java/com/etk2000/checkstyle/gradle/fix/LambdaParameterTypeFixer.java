package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class LambdaParameterTypeFixer implements CheckstyleFixer {
	/**
	 * Finds the opening paren that matches the closing paren before the arrow.
	 * Tracks paren depth to handle nested expressions. Operates on the masked
	 * line so parens inside string/char literals or comments are ignored.
	 */
	@CheckReturnValue
	private static int findLambdaOpenParen(@Nonnull String masked, int arrowStart) {
		var closeParenIdx = -1;
		for (var i = arrowStart - 1; i >= 0; --i) {
			final var ch = masked.charAt(i);
			if (ch == ')') {
				closeParenIdx = i;
				break;
			}
			if (!Character.isWhitespace(ch))
				return -1; // no paren before arrow, naked param
		}
		if (closeParenIdx < 0)
			return -1;

		var depth = 1;
		for (var i = closeParenIdx - 1; i >= 0; --i) {
			if (masked.charAt(i) == ')')
				++depth;
			else if (masked.charAt(i) == '(') {
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
			@Nonnull String mask,
			int lineIndex,
			int openParen,
			int closeParen
	) {
		// Split the parameter list on top-level commas located on the MASK, so a
		// comma inside a string/char literal in an annotation argument is not a
		// split point. Each param is carried as both its original text (for the
		// output) and its masked text (for structural scanning).
		final var origParams = new ArrayList<String>();
		final var maskParams = new ArrayList<String>();
		var start = openParen + 1;
		for (var i = openParen + 1; i < closeParen; ++i) {
			if (mask.charAt(i) == ',') {
				origParams.add(line.substring(start, i));
				maskParams.add(mask.substring(start, i));
				start = i + 1;
			}
		}
		origParams.add(line.substring(start, closeParen));
		maskParams.add(mask.substring(start, closeParen));

		// if any param has annotations, all must use var (Java requires uniform form)
		var anyAnnotated = false;
		for (var maskParam : maskParams) {
			if (maskParam.contains("@")) {
				anyAnnotated = true;
				break;
			}
		}

		final var sb = new StringBuilder();
		for (var j = 0; j < origParams.size(); ++j) {
			if (j > 0)
				sb.append(", ");
			final var param = origParams.get(j).trim();
			final var maskParam = maskParams.get(j).trim();
			if (anyAnnotated) {
				if (maskParam.contains("@"))
					sb.append(replaceTypeWithVar(param, maskParam));
				else {
					final var stripped = stripTrailingArrayBrackets(param);
					final var lastSpace = lastWhitespaceIndex(stripped);
					if (lastSpace >= 0)
						sb.append("var ").append(stripped.substring(lastSpace + 1));
					else
						sb.append(param);
				}
			}
			else {
				final var stripped = stripTrailingArrayBrackets(param);
				if (stripped.isEmpty()) {
					sb.append(param);
					continue;
				}
				final var lastSpace = lastWhitespaceIndex(stripped);
				if (lastSpace >= 0)
					sb.append(stripped.substring(lastSpace + 1));
				else
					sb.append(stripped);
			}
		}

		if (origParams.size() == 1 && !anyAnnotated) {
			final var fixed = line.substring(0, openParen) + sb + line.substring(closeParen + 1);
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}
		final var fixed = line.substring(0, openParen + 1) + sb + line.substring(closeParen);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	/**
	 * Index of the last whitespace character in {@code s}, or -1 if none. Used
	 * instead of {@code lastIndexOf(' ')} so a tab between a parameter's type
	 * and name is handled.
	 */
	@CheckReturnValue
	private static int lastWhitespaceIndex(@Nonnull String s) {
		for (var i = s.length() - 1; i >= 0; --i) {
			if (Character.isWhitespace(s.charAt(i)))
				return i;
		}
		return -1;
	}

	/**
	 * Replaces the type of an annotated lambda parameter with {@code var},
	 * keeping the annotations. {@code param} is the original text (spliced into
	 * the output); {@code mask} is the same text with string/char/comment
	 * content blanked, so the annotation-argument paren match and the type/name
	 * boundary are located on structural characters only (an argument like
	 * {@code @A(")")} no longer mis-terminates the paren scan).
	 */
	@Nonnull
	private static String replaceTypeWithVar(@Nonnull String param, @Nonnull String mask) {
		var lastAnnotationEnd = 0;
		var i = 0;
		while (i < mask.length()) {
			if (mask.charAt(i) == '@') {
				do ++i;
				while (i < mask.length()
						&& (Character.isLetterOrDigit(mask.charAt(i)) || mask.charAt(i) == '.'));

				if (i < mask.length() && mask.charAt(i) == '(') {
					var depth = 1;
					++i;
					while (i < mask.length() && depth > 0) {
						if (mask.charAt(i) == '(')
							++depth;
						else if (mask.charAt(i) == ')')
							--depth;
						++i;
					}
				}
				while (i < mask.length() && Character.isWhitespace(mask.charAt(i)))
					++i;
				lastAnnotationEnd = i;
			}
			else
				break;
		}
		final var afterAnnotations = stripTrailingArrayBrackets(mask.substring(lastAnnotationEnd));
		final var lastSpace = lastWhitespaceIndex(afterAnnotations);
		if (lastSpace < 0)
			return param;
		final var paramName = param.substring(lastAnnotationEnd + lastSpace + 1, lastAnnotationEnd + afterAnnotations.length());
		return param.substring(0, lastAnnotationEnd) + "var " + paramName;
	}

	/**
	 * Removes trailing C-style array brackets (and any surrounding whitespace)
	 * from a {@code "type name[]"} fragment, e.g. {@code int a[]},
	 * {@code int a [][]}, {@code int a [ ]}. Removing the explicit type from a
	 * lambda parameter drops the array-ness too, so the brackets must not cling
	 * to the extracted name. Brackets on the type ({@code int[] a}) are left
	 * untouched since they precede the name. Bails (returns the input unchanged)
	 * on unbalanced brackets.
	 */
	@CheckReturnValue
	@Nonnull
	private static String stripTrailingArrayBrackets(@Nonnull String s) {
		var end = s.length();
		while (true) {
			var i = end;
			while (i > 0 && Character.isWhitespace(s.charAt(i - 1)))
				--i;
			if (i <= 0 || s.charAt(i - 1) != ']')
				break;
			var depth = 0;
			var j = i - 1;
			for (; j >= 0; --j) {
				final var ch = s.charAt(j);
				if (ch == ']')
					++depth;
				else if (ch == '[' && --depth == 0)
					break;
			}
			if (j < 0)
				break;
			end = j;
		}
		return s.substring(0, end).stripTrailing();
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;
		// Mask string/char/comment content so a `->`, `(`, or `)` inside a
		// literal or comment on the lambda line can't misdirect the searches;
		// all output is spliced from the original line.
		final var mask = JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE);

		final var arrowIdx = mask.indexOf("->", column);
		if (arrowIdx < 0)
			return new SkipResult(SkipMessages.LAMBDA_PARAM_SKIP);

		final var openParen = findLambdaOpenParen(mask, arrowIdx);
		if (openParen < 0)
			return new SkipResult(SkipMessages.LAMBDA_PARAM_SKIP);

		var closeParen = -1;
		for (var i = arrowIdx - 1; i > openParen; --i) {
			if (mask.charAt(i) == ')') {
				closeParen = i;
				break;
			}
		}
		if (closeParen < 0)
			return null;

		final var paramSection = mask.substring(openParen + 1, closeParen).trim();

		if (!paramSection.isEmpty() && lastWhitespaceIndex(paramSection) < 0 && !paramSection.contains(","))
			return fixRemoveParens(line, lineIndex, openParen, closeParen);

		return fixTypes(line, mask, lineIndex, openParen, closeParen);
	}
}