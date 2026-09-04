package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.RedundantNumericSuffixCheck;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code RedundantNumericSuffixCheck}. Deletes the literal's last character, which the
 * check has already judged redundant.
 *
 * <p>Where the literal ends is the syntax tree's answer rather than a scan's, so a suffix behind a
 * signed exponent ({@code 1e+2d}, {@code 0x1p-3d}) needs no rule of its own: the token carries the
 * sign.
 */
class RedundantNumericSuffixFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var literal = FixerAst.locate(lines, lineIndex, column, RedundantNumericSuffixCheck::suffixedLiteralAt);
		if (literal == null)
			return null;

		final var end = AstSpan.tokenEnd(lines, literal);
		if (end == null)
			return null;

		final var line = lines.get(end.line());
		return new FixResult(
				end.line(),
				end.line(),
				List.of(line.substring(0, end.index() - 1) + line.substring(end.index()))
		);
	}
}