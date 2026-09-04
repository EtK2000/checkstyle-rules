package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.PreferStandardCharsetsCheck;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferStandardCharsetsFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var line = lines.get(lineIndex);
		if (charColumn < 0 || charColumn >= line.length())
			return null;

		// the column points at the opening " of the charset literal; the
		// String-variable form of this violation names an identifier instead, so there is no
		// literal on the line to rewrite
		if (line.charAt(charColumn) != '"')
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_VARIABLE);
		final var closeQuote = line.indexOf('"', charColumn + 1);
		if (closeQuote < 0)
			return null;

		final var charsetName = line.substring(charColumn + 1, closeQuote);
		final var constant = PreferStandardCharsetsCheck.standardCharsetConstant(charsetName);
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