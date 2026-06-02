package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferStandardCharsetsFixer implements CheckstyleFixer {
	private static final Map<String, String> CHARSET_MAP = buildCharsetMap();

	@CheckReturnValue
	@Nonnull
	private static Map<String, String> buildCharsetMap() {
		final var map = new HashMap<String, String>();
		for (var field : StandardCharsets.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == Charset.class) {
				try {
					final var charset = (Charset) field.get(null);
					final var fieldName = field.getName();
					map.put(charset.name().toLowerCase(Locale.ROOT), fieldName);
					for (var alias : charset.aliases())
						map.put(alias.toLowerCase(Locale.ROOT), fieldName);
				}
				catch (IllegalAccessException ignored) {
				}
			}
		}
		return Map.copyOf(map);
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// the reported column counts code points, so it has to be converted before it can
		// index the line: one supplementary character earlier on the line shifts every
		// char index right of it
		final var charColumn = LineText.charIndexOfColumn(line, column);
		if (charColumn < 0 || charColumn >= line.length())
			return null;

		// the converted column points at the opening " of the charset literal; the
		// String-variable form of this violation names an identifier instead, so there is no
		// literal on the line to rewrite
		if (line.charAt(charColumn) != '"')
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_VARIABLE);
		final var closeQuote = line.indexOf('"', charColumn + 1);
		if (closeQuote < 0)
			return null;

		final var charsetName = line.substring(charColumn + 1, closeQuote);
		final var constant = CHARSET_MAP.get(charsetName.toLowerCase(Locale.ROOT));
		if (constant == null)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP);

		final var newLine = line.substring(0, charColumn) + "StandardCharsets." + constant
				+ line.substring(closeQuote + 1);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(newLine),
				Set.of("java.nio.charset.StandardCharsets")
		);
	}
}