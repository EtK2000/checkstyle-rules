package com.etk2000.checkstyle.gradle.fix;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
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
					map.put(charset.name().toLowerCase(), fieldName);
					for (var alias : charset.aliases())
						map.put(alias.toLowerCase(), fieldName);
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

		// column points to the opening " of the charset string literal
		if (column >= line.length() || line.charAt(column) != '"')
			return null;
		final var closeQuote = line.indexOf('"', column + 1);
		if (closeQuote < 0)
			return null;

		final var charsetName = line.substring(column + 1, closeQuote);
		final var constant = CHARSET_MAP.get(charsetName.toLowerCase());
		if (constant == null)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP);

		final var newLine = line.substring(0, column) + "StandardCharsets." + constant
				+ line.substring(closeQuote + 1);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(newLine),
				Set.of("java.nio.charset.StandardCharsets")
		);
	}
}