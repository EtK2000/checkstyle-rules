package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantNumericSuffixFixer implements CheckstyleFixer {
	/**
	 * Whether the {@code +}/{@code -} at {@code sign} is the sign of the exponent of the literal
	 * starting at {@code start}, rather than an operator after it: it follows {@code e}/{@code E}
	 * in a decimal literal, or {@code p}/{@code P} in a hex one, where {@code E} is a digit.
	 */
	@CheckReturnValue
	private static boolean isExponentSign(@Nonnull String line, int start, int sign) {
		if (sign >= line.length() || (line.charAt(sign) != '+' && line.charAt(sign) != '-'))
			return false;

		final var marker = Character.toLowerCase(line.charAt(sign - 1));
		final var hex = line.regionMatches(true, start, "0x", 0, 2);
		return hex ? marker == 'p' : marker == 'e';
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int start) {
		final var line = lines.get(lineIndex);
		if (start < 0 || start >= line.length())
			return null;

		var end = LineText.literalTokenEnd(line, start);
		if (end <= start)
			return null;
		if (isExponentSign(line, start, end))
			end = LineText.literalTokenEnd(line, end + 1);

		final var suffixPos = end - 1;
		final var ch = line.charAt(suffixPos);
		if (ch != 'D' && ch != 'F' && ch != 'L' && ch != 'd' && ch != 'f' && ch != 'l')
			return null;

		final var fixed = line.substring(0, suffixPos) + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}