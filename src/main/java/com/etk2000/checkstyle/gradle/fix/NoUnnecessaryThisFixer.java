package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.NoUnnecessaryThisCheck;
import com.etk2000.checkstyle.NoUnnecessaryThisCheck.ThisTarget;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code NoUnnecessaryThisCheck}. Replaces the text from the {@code this} keyword up to
 * the member name it qualifies, which deletes the qualifier along with whatever was written between
 * the two: whitespace, a comment, or a line break.
 *
 * <p>A comment inside that text moves to the front of the member, the text it was written against
 * being what goes.
 */
class NoUnnecessaryThisFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull ThisTarget target) {
		final var start = AstSpan.spanStart(lines, target.literalThis());
		final var end = AstSpan.spanStart(lines, target.ident());
		if (start == null || end == null)
			return null;

		final var span = new TextSpan(start, end);
		final var emitted = SpanCarry.separated(lines, span, SpanCarry.carried(lines, span, List.of()));
		final var fix = SpanRewrite.replacing(lines, span, emitted);
		return fix != null ? fix : new SkipResult(SkipMessages.NO_UNNECESSARY_THIS_SKIP_UNSLICEABLE);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, NoUnnecessaryThisCheck::thisAt);
		return target == null ? null : rewrite(lines, target);
	}
}