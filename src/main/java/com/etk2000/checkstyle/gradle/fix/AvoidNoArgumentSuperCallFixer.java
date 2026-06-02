package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

class AvoidNoArgumentSuperCallFixer implements CheckstyleFixer {
	private static final Pattern BARE_SUPER_CALL = Pattern.compile("\\bsuper\\s*\\(\\s*\\)\\s*;");

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		final var matcher = BARE_SUPER_CALL.matcher(JavaLineScanner.stripCommentsAndStrings(line, state));
		if (!matcher.find())
			return new SkipResult(SkipMessages.AVOID_SUPER_SKIP);
		final var before = line.substring(0, matcher.start());
		final var after = line.substring(matcher.end());
		if ((before + after).isBlank())
			return new FixResult(lineIndex, lineIndex, List.of());
		final var indent = before.substring(0, before.length() - before.stripLeading().length());
		final var rejoined = (before.strip() + " " + after.strip()).strip();
		return new FixResult(lineIndex, lineIndex, List.of(indent + rejoined));
	}
}