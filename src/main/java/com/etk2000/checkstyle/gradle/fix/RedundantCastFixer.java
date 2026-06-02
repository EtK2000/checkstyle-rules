package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code RedundantCastCheck}. Deletes the cast token
 * {@code (Type)} and the whitespace immediately following it, anchored at
 * the column the check reports (always the cast's open paren).
 *
 * <p>When the cast is the sole content of a parenthesized receiver such
 * as {@code ((String) obj).method()}, the surrounding parens become
 * syntactically redundant after the cast is removed; this fixer strips
 * them in the same pass to avoid leaving {@code (obj).method()}. Two
 * strip paths exist:
 * <ul>
 *   <li><b>receiver wrap</b>: outer close is followed by {@code .} (the
 *       receiver-of-something pattern), e.g.
 *       {@code return ((String) s).length();} becomes
 *       {@code return s.length();}.
 *   <li><b>bare cast wrap</b>: outer paren is immediately adjacent to
 *       the cast paren (no chars between) and wraps only {@code (Cast)
 *       atom} where atom is a single Java identifier, e.g.
 *       {@code return ((String) s);} becomes {@code return s;}.
 * </ul>
 * Both paths require the outer paren be preceded only by whitespace or
 * by a non-identifier non-{@code )}/{@code ]} character (otherwise it's
 * a method-call argument paren). When the outer paren is the first
 * non-whitespace on its own line, the prior-context scan walks back
 * across blank lines and prior-line trailing whitespace to find the
 * last non-whitespace char on the most recent non-blank prior line.
 * Source like {@code return\n((String) s).length();} is handled this
 * way. When no prior content exists at all (snippet starts at file
 * head), the strip proceeds. When the outer paren is directly adjacent
 * to a keyword (no whitespace), a space is inserted before the receiver
 * to keep the keyword tokenized.
 *
 * <p>Comments between the cast's close paren and the expression are
 * preserved: {@code (String) /* note *\/ null} becomes
 * {@code /* note *\/ null}.
 *
 * <p>Paren matching is literal- and comment-aware: parens inside string
 * literals, text blocks, char literals, line comments, and block comments
 * do not affect the depth count. This prevents source corruption on
 * inputs like {@code ((String) "x)y").length()}.
 *
 * <p>Bails (returns {@link SkipResult}) when the cast spans multiple
 * lines or has no expression on the same line after the close paren.
 */
class RedundantCastFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static boolean isBareCastWrap(
			@Nonnull String line,
			int outerOpen,
			int castOpen,
			int exprStart,
			int outerClose
	) {
		return outerOpen + 1 == castOpen && isBareIdentifierInner(line, exprStart, outerClose);
	}

	@CheckReturnValue
	private static boolean isBareIdentifierInner(@Nonnull String line, int exprStart, int outerClose) {
		var i = exprStart;
		while (i < outerClose && Character.isWhitespace(line.charAt(i)))
			++i;
		if (i >= outerClose)
			return false;
		if (!Character.isJavaIdentifierStart(line.charAt(i)) && !Character.isHighSurrogate(line.charAt(i)))
			return false;
		do ++i;
		while (i < outerClose && isIdentifierPart(line.charAt(i)));
		while (i < outerClose) {
			if (!Character.isWhitespace(line.charAt(i)))
				return false;
			++i;
		}
		return true;
	}

	/**
	 * Returns {@code true} when {@code c} is part of a Java identifier OR a
	 * surrogate half of a supplementary code point. The surrogate-half check
	 * is conservative defense against supplementary identifier characters
	 * (U+10000+, e.g. CJK Extension B): treating them as identifier-part
	 * keeps the receiver-wrap detection from misclassifying a method-call
	 * paren as a receiver-wrap paren.
	 */
	@CheckReturnValue
	private static boolean isIdentifierPart(char c) {
		return Character.isJavaIdentifierPart(c)
				|| Character.isHighSurrogate(c)
				|| Character.isLowSurrogate(c);
	}

	@CheckReturnValue
	private static boolean isPriorCharAllowed(@Nonnull String line, int pos) {
		final var c = line.charAt(pos);
		if (c == ')' || c == ']')
			return false;
		if (!isIdentifierPart(c))
			return true;
		var wordStart = pos;
		while (wordStart > 0 && isIdentifierPart(line.charAt(wordStart - 1)))
			--wordStart;
		final var word = line.substring(wordStart, pos + 1);
		return switch (word) {
			case "return", "throw", "yield" -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isPriorContextAllowed(
			@Nonnull List<String> lines,
			int lineIndex,
			int outerOpen
	) {
		final var strippedLines = JavaLineScanner.maskAll(lines.subList(0, lineIndex + 1));
		final var strippedCurrent = strippedLines.get(lineIndex);
		if (outerOpen >= strippedCurrent.length() || strippedCurrent.charAt(outerOpen) != '(')
			return false;
		var beforeOuter = Math.min(outerOpen, strippedCurrent.length()) - 1;
		while (beforeOuter >= 0 && Character.isWhitespace(strippedCurrent.charAt(beforeOuter)))
			--beforeOuter;
		if (beforeOuter >= 0)
			return isPriorCharAllowed(strippedCurrent, beforeOuter);
		for (var li = lineIndex - 1; li >= 0; --li) {
			final var stripped = strippedLines.get(li);
			var pos = stripped.length() - 1;
			while (pos >= 0 && Character.isWhitespace(stripped.charAt(pos)))
				--pos;
			if (pos >= 0)
				return isPriorCharAllowed(stripped, pos);
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isReceiverWrap(@Nonnull String line, int outerClose) {
		if (outerClose >= line.length() - 1)
			return false;
		var afterOuter = outerClose + 1;
		while (afterOuter < line.length() && Character.isWhitespace(line.charAt(afterOuter)))
			++afterOuter;
		return afterOuter < line.length() && line.charAt(afterOuter) == '.';
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;
		if (line.charAt(column) != '(')
			return null;

		final var castClose = JavaLineScanner.matchingCloseParen(line, column);
		if (castClose < 0)
			return new SkipResult("multi-line-cast");

		var exprStart = castClose + 1;
		while (exprStart < line.length() && Character.isWhitespace(line.charAt(exprStart)))
			++exprStart;
		if (exprStart >= line.length())
			return new SkipResult("malformed-cast-no-expression");

		var outerOpen = -1;
		for (var i = column - 1; i >= 0; --i) {
			final var c = line.charAt(i);
			if (c == '(') {
				outerOpen = i;
				break;
			}
			if (!Character.isWhitespace(c))
				break;
		}

		if (outerOpen >= 0) {
			final var outerClose = JavaLineScanner.matchingCloseParen(line, outerOpen);
			if (outerClose >= 0
					&& isPriorContextAllowed(lines, lineIndex, outerOpen)
					&& (isReceiverWrap(line, outerClose)
					|| isBareCastWrap(line, outerOpen, column, exprStart, outerClose))) {
				final var prefix = line.substring(0, outerOpen);
				final var needsSpace = !prefix.isEmpty()
						&& isIdentifierPart(prefix.charAt(prefix.length() - 1));
				final var fixed = prefix
						+ (needsSpace ? " " : "")
						+ line.substring(exprStart, outerClose)
						+ line.substring(outerClose + 1);
				return new FixResult(lineIndex, lineIndex, List.of(fixed));
			}
		}

		final var fixed = line.substring(0, column) + line.substring(exprStart);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}