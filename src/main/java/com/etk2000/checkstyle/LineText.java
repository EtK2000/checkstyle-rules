package com.etk2000.checkstyle;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Stateless helpers for manipulating a single line of Java source text, shared
 * across the text-based fixers.
 */
public final class LineText {
	/**
	 * Converts a zero-based column to an index into {@code line}'s chars, or
	 * {@code -1} when it does not name a position on the line.
	 *
	 * <p>Columns count code points, not chars: checkstyle parses the file as a
	 * code-point stream, so {@code DetailAST.getColumnNo()} is a code-point index.
	 * A column may therefore be compared against an AST position directly, but must
	 * be converted here before it indexes a line, which the two units disagree on
	 * from the first supplementary character onward.
	 */
	@CheckReturnValue
	public static int charIndexOfColumn(@Nonnull String line, int column) {
		return column < 0 || column > line.codePointCount(0, line.length())
				? -1
				: line.offsetByCodePoints(0, column);
	}

	/**
	 * Returns the leading indentation (the run of tabs and spaces at the start)
	 * of {@code line}. This codebase indents with tabs/spaces only, so other
	 * whitespace forms are deliberately not treated as indentation.
	 */
	@CheckReturnValue
	@Nonnull
	public static String extractIndent(@Nonnull String line) {
		var i = 0;
		while (i < line.length() && (line.charAt(i) == '\t' || line.charAt(i) == ' '))
			++i;
		return line.substring(0, i);
	}

	/**
	 * Returns the index of the first character at or after {@code start} that is
	 * not a Java identifier part ({@link Character#isJavaIdentifierPart}), or
	 * {@code line.length()} when the run reaches the end of the line. Returns
	 * {@code start} when the character there is already not an identifier part.
	 * Does not validate that {@code start} is an identifier start.
	 *
	 * <p>Walks whole code points: a supplementary identifier character is a
	 * surrogate pair, and neither half is an identifier part on its own, so a
	 * char-wise walk would stop inside the pair and report a word boundary that is
	 * not there.
	 */
	@CheckReturnValue
	public static int identEnd(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length()) {
			final var cp = line.codePointAt(i);
			if (!Character.isJavaIdentifierPart(cp))
				break;
			i += Character.charCount(cp);
		}
		return i;
	}

	/**
	 * Returns the start index of the Java identifier-part run ending just before
	 * {@code pos} (treated as an exclusive end). Returns {@code pos} when the
	 * character at {@code pos - 1} is not a Java identifier part or when
	 * {@code pos == 0}. Walks whole code points, for the reason given on
	 * {@link #identEnd}.
	 */
	@CheckReturnValue
	public static int identStart(@Nonnull String line, int pos) {
		var i = pos;
		while (i > 0) {
			final var cp = line.codePointBefore(i);
			if (!Character.isJavaIdentifierPart(cp))
				break;
			i -= Character.charCount(cp);
		}
		return i;
	}

	/**
	 * Index of the first occurrence of {@code word} in {@code s} as a whole token,
	 * or {@code -1} when there is none. Unlike {@link String#indexOf(String)} this
	 * will not match inside a longer identifier, so {@code while} is not found in
	 * {@code while_loop}.
	 */
	@CheckReturnValue
	public static int indexOfWord(@Nonnull String s, @Nonnull String word) {
		for (var i = s.indexOf(word); i >= 0; i = s.indexOf(word, i + 1)) {
			if (isWordAt(s, i, word))
				return i;
		}
		return -1;
	}

	/**
	 * Returns true when the character at {@code pos} is escaped, i.e. preceded by
	 * an odd number of consecutive backslashes.
	 */
	@CheckReturnValue
	public static boolean isEscaped(@Nonnull String line, int pos) {
		var backslashes = 0;
		for (var i = Math.min(pos, line.length()) - 1; i >= 0 && line.charAt(i) == '\\'; --i)
			++backslashes;
		return backslashes % 2 != 0;
	}

	/**
	 * Returns true when {@code c} can appear inside a numeric literal token
	 * (digit, letter for a hex/suffix character, {@code _} separator, or the
	 * decimal point).
	 */
	@CheckReturnValue
	public static boolean isLiteralChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_' || c == '.';
	}

	/**
	 * Whether {@code word} sits at {@code index} as a whole token, i.e. no
	 * identifier character abuts it on either side.
	 */
	@CheckReturnValue
	public static boolean isWordAt(@Nonnull String s, int index, @Nonnull String word) {
		final var end = index + word.length();
		return s.startsWith(word, index)
				&& identStart(s, index) == index
				&& identEnd(s, end) == end;
	}

	/**
	 * Returns the index of the first character at or after {@code start} that is
	 * not a literal char (see {@link #isLiteralChar}), or {@code line.length()}
	 * when the run reaches the end of the line. Returns {@code start} when the
	 * character there is already not a literal char. Does not validate that
	 * {@code start} is a literal-token start. Note {@code .} is a literal char,
	 * so {@code 3.14} scans as one token (unlike {@link #identEnd}).
	 */
	@CheckReturnValue
	public static int literalTokenEnd(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length() && isLiteralChar(line.charAt(i)))
			++i;
		return i;
	}

	/**
	 * Like {@link #identEnd}, but {@code .} also continues the run, so a qualified
	 * name ({@code java.util.List}) is spanned in one call.
	 */
	@CheckReturnValue
	public static int qualifiedNameEnd(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length()) {
			final var cp = line.codePointAt(i);
			if (!Character.isJavaIdentifierPart(cp) && cp != '.')
				break;
			i += Character.charCount(cp);
		}
		return i;
	}

	/**
	 * Like {@link #identStart}, but {@code .} also continues the run, so a qualified
	 * name ({@code java.util.List}) is spanned in one call.
	 */
	@CheckReturnValue
	public static int qualifiedNameStart(@Nonnull String line, int pos) {
		var i = pos;
		while (i > 0) {
			final var cp = line.codePointBefore(i);
			if (!Character.isJavaIdentifierPart(cp) && cp != '.')
				break;
			i -= Character.charCount(cp);
		}
		return i;
	}

	/**
	 * Whether {@code s} opens with {@code word} as a whole token that introduces
	 * something else, i.e. whitespace and more text follow it. Unlike
	 * {@link #startsWithWord} this rejects a bare {@code var++}, where the word is
	 * the subject rather than a declaration's leading keyword.
	 */
	@CheckReturnValue
	public static boolean startsWithSeparatedWord(@Nonnull String s, @Nonnull String word) {
		return s.startsWith(word) && s.length() > word.length() && Character.isWhitespace(s.charAt(word.length()));
	}

	/**
	 * Whether {@code s} opens with {@code word} as a whole token, i.e. what follows
	 * it is not an identifier character.
	 */
	@CheckReturnValue
	public static boolean startsWithWord(@Nonnull String s, @Nonnull String word) {
		return s.startsWith(word) && identEnd(s, word.length()) == word.length();
	}

	private LineText() {
	}
}