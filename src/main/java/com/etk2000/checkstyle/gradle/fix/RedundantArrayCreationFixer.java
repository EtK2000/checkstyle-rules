package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantArrayCreationFixer implements CheckstyleFixer {
	private static final String SKIP_MULTILINE_ARRAY = "multi-line array initializer";
	private static final String SKIP_NESTED_ARRAY_INIT = "nested array initializer";

	private static int findPrecedingComma(@Nonnull String beforeNew) {
		for (var i = beforeNew.length() - 1; i >= 0; --i) {
			final var c = beforeNew.charAt(i);
			if (c == ',')
				return i;
			if (!Character.isWhitespace(c))
				return -1;
		}
		return -1;
	}

	/**
	 * True when any top-level element of a masked array-initializer body starts
	 * with {@code &#123;}, i.e. the array is multi-dimensional with nested
	 * brace-initializer elements ({@code &#123;&#123;"a"&#125;, &#123;"b"&#125;&#125;}).
	 * Splicing those bare {@code &#123;...&#125;} into an argument list produces
	 * invalid Java, so the caller bails. A leading {@code &#123;} at depth zero is
	 * what distinguishes a brace-initializer element from a lambda body (whose
	 * {@code &#123;} follows {@code ->}).
	 */
	private static boolean hasBraceInitElement(@Nonnull String maskedElements) {
		var depth = 0;
		var atElementStart = true;
		for (var i = 0; i < maskedElements.length(); ++i) {
			final var c = maskedElements.charAt(i);
			if (Character.isWhitespace(c))
				continue;
			if (atElementStart && c == '{')
				return true;
			switch (c) {
				case '(', '[', '{' -> ++depth;
				case ')', ']', '}' -> --depth;
			}
			atElementStart = depth == 0 && c == ',';
		}
		return false;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		// Mask string/char/comment content (positions preserved) so braces inside
		// literals or comments aren't counted. The incoming lexer state is folded
		// from preceding lines so a line continuing a multi-line block comment or
		// text block is masked correctly rather than assumed to start in code.
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, state);

		final var openBrace = scan.indexOf('{', column);
		if (openBrace < 0)
			return new SkipResult(SKIP_MULTILINE_ARRAY);

		final var closeBrace = JavaLineScanner.matchingClose(scan, openBrace);
		if (closeBrace < 0)
			return new SkipResult(SKIP_MULTILINE_ARRAY);

		final var elements = line.substring(openBrace + 1, closeBrace).strip();
		final var beforeNew = line.substring(0, column);
		final var afterBrace = line.substring(closeBrace + 1);

		if (elements.isEmpty()) {
			final var commaIdx = findPrecedingComma(scan.substring(0, column));
			if (commaIdx >= 0) {
				final var fixed = line.substring(0, commaIdx) + afterBrace;
				return new FixResult(lineIndex, lineIndex, List.of(fixed));
			}
			final var fixed = beforeNew + afterBrace;
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}

		if (hasBraceInitElement(scan.substring(openBrace + 1, closeBrace)))
			return new SkipResult(SKIP_NESTED_ARRAY_INIT);

		final var packed = elements.endsWith(",")
				? elements.substring(0, elements.length() - 1).strip()
				: elements;
		final var fixed = beforeNew + packed + afterBrace;
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}