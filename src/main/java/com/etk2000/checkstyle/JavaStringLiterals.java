package com.etk2000.checkstyle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Decodes the content of a string literal into the characters it stands for. Checkstyle's lexer
 * leaves both unicode escapes and escape sequences as written, so a rule that asks about a
 * literal's characters has to undo both, in the order javac does.
 */
final class JavaStringLiterals {
	/** One character of a literal's value and the span of the literal's content that spells it. */
	record Unit(char value, int start, int end) {}

	private static final int OCTAL_MAX_DIGITS = 3;
	private static final int UNICODE_HEX_DIGITS = 4;

	/**
	 * The characters {@code content} (the text between a string literal's quotes) stands for, each
	 * with the span of {@code content} that spells it, or null when it is not well formed. Unicode
	 * escapes are translated first, as JLS 3.3 does ahead of tokenizing, and the escape sequences
	 * of JLS 3.10.7 are decoded over the result, so a backslash written as a unicode escape still
	 * begins one. A unicode escape that javac would translate into a quote or a line terminator ends or
	 * breaks the literal there, so it is not well formed either.
	 */
	@CheckReturnValue
	@Nullable
	static List<Unit> decode(@Nonnull String content) {
		final var translated = translateUnicodeEscapes(content);
		return translated == null ? null : decodeEscapes(translated);
	}

	@CheckReturnValue
	@Nullable
	private static List<Unit> decodeEscapes(@Nonnull List<Unit> chars) {
		final var units = new ArrayList<Unit>();
		var i = 0;
		while (i < chars.size()) {
			final var current = chars.get(i);
			if (current.value() != '\\') {
				units.add(current);
				++i;
				continue;
			}
			if (i + 1 >= chars.size())
				return null;

			final var next = chars.get(i + 1).value();
			if (isOctalDigit(next)) {
				// JLS 3.10.7: three digits only when the first is 0-3, so the value stays within \377
				final var maxDigits = next <= '3' ? OCTAL_MAX_DIGITS : OCTAL_MAX_DIGITS - 1;
				var j = i + 1;
				var code = 0;
				while (j < chars.size() && j <= i + maxDigits && isOctalDigit(chars.get(j).value())) {
					code = code * 8 + chars.get(j).value() - '0';
					++j;
				}
				units.add(new Unit((char) code, current.start(), chars.get(j - 1).end()));
				i = j;
				continue;
			}

			final var simple = switch (next) {
				case '"' -> '"';
				case '\'' -> '\'';
				case '\\' -> '\\';
				case 'b' -> '\b';
				case 'f' -> '\f';
				case 'n' -> '\n';
				case 'r' -> '\r';
				case 's' -> ' ';
				case 't' -> '\t';
				default -> -1;
			};
			if (simple < 0)
				return null;
			units.add(new Unit((char) simple, current.start(), chars.get(i + 1).end()));
			i += 2;
		}
		return units;
	}

	@CheckReturnValue
	private static int hexValue(char c) {
		if (c >= '0' && c <= '9')
			return c - '0';
		if (c >= 'a' && c <= 'f')
			return c - 'a' + 10;
		if (c >= 'A' && c <= 'F')
			return c - 'A' + 10;
		return -1;
	}

	@CheckReturnValue
	private static boolean isOctalDigit(char c) {
		return c >= '0' && c <= '7';
	}

	/**
	 * The characters of {@code content} once its unicode escapes are translated, or null when one
	 * is malformed. A backslash begins one only when an even number of raw backslashes precede it
	 * (JLS 3.3), and one an escape produced does not count toward that run.
	 */
	@CheckReturnValue
	@Nullable
	static List<Unit> translateUnicodeEscapes(@Nonnull String content) {
		final var chars = new ArrayList<Unit>();
		var backslashRun = 0;
		var i = 0;
		while (i < content.length()) {
			final var c = content.charAt(i);
			if (c != '\\' || backslashRun % 2 != 0 || i + 1 >= content.length() || content.charAt(i + 1) != 'u') {
				backslashRun = c == '\\' ? backslashRun + 1 : 0;
				chars.add(new Unit(c, i, i + 1));
				++i;
				continue;
			}

			var digits = i + 1;
			while (digits < content.length() && content.charAt(digits) == 'u')
				++digits;
			if (digits + UNICODE_HEX_DIGITS > content.length())
				return null;

			var code = 0;
			for (var j = digits; j < digits + UNICODE_HEX_DIGITS; ++j) {
				final var digit = hexValue(content.charAt(j));
				if (digit < 0)
					return null;
				code = code * 16 + digit;
			}
			if (code == '"' || code == '\n' || code == '\r')
				return null;

			chars.add(new Unit((char) code, i, digits + UNICODE_HEX_DIGITS));
			backslashRun = 0;
			i = digits + UNICODE_HEX_DIGITS;
		}
		return chars;
	}

	/**
	 * The value {@code literalText} (a string literal with its quotes) stands for, or null when it
	 * is not a well-formed string literal.
	 */
	@CheckReturnValue
	@Nullable
	static String value(@Nonnull String literalText) {
		if (literalText.length() < 2 || literalText.charAt(0) != '"' || literalText.charAt(literalText.length() - 1) != '"')
			return null;

		final var units = decode(literalText.substring(1, literalText.length() - 1));
		if (units == null)
			return null;

		final var value = new StringBuilder();
		for (var unit : units)
			value.append(unit.value());
		return value.toString();
	}

	private JavaStringLiterals() {
	}
}