package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.RedundantCastCheck;
import com.etk2000.checkstyle.RedundantCastCheck.RedundantCastTarget;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code RedundantCastCheck}. Replaces the text from the cast's opening parenthesis up to
 * the expression it applies to, which deletes the cast and anything written between it and its
 * subject.
 *
 * <p>When the check pairs the cast with grouping parentheses, those go in the same edit where they
 * can, so {@code return ((String) s).length();} becomes {@code return s.length();} rather than
 * {@code return (s).length();}.
 *
 * <p>A comment inside the replaced text moves to the front of the subject, the text it was written
 * against being what goes.
 */
class RedundantCastFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull RedundantCastTarget target) {
		final var castStart = AstSpan.spanStart(lines, target.typecast());
		final var subjectStart = AstSpan.spanStart(lines, target.subject());
		if (castStart == null || subjectStart == null)
			return null;

		final var stripped = target.wrapOpen() != null && target.wrapClose() != null
				? stripping(lines, target.wrapOpen(), target.wrapClose(), target.subject(), subjectStart)
				: null;
		if (stripped != null)
			return stripped;

		final var span = new TextSpan(castStart, subjectStart);
		final var emitted = SpanCarry.separated(lines, span, SpanCarry.carried(lines, span, List.of()));
		final var fix = SpanRewrite.replacing(lines, span, emitted);
		return fix != null ? fix : new SkipResult(SkipMessages.REDUNDANT_CAST_SKIP_UNSLICEABLE);
	}

	/**
	 * The edit taking the grouping parentheses out along with the cast, or null when that rewrite
	 * cannot be made and the cast has to come out on its own. The parentheses sit either side of the
	 * subject, so the region spans all three and the subject is written back into it.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult stripping(
			@Nonnull List<String> lines,
			@Nonnull DetailAST wrapOpen,
			@Nonnull DetailAST wrapClose,
			@Nonnull DetailAST subject,
			@Nonnull TextPos subjectStart
	) {
		final var start = AstSpan.spanStart(lines, wrapOpen);
		final var end = AstSpan.spanEnd(lines, wrapClose);
		final var subjectEnd = AstSpan.spanEnd(lines, subject);
		if (start == null || end == null || subjectEnd == null || AstSpan.outOfBounds(lines, start, end))
			return null;

		// the subject is written back into the region, so a subject of its own across lines would be
		// folded onto one: an anonymous class body or a wrapped argument list keeps its layout, and
		// the cast still comes out on its own through the caller's plain path
		final var subjectText = subjectStart.line() == subjectEnd.line()
				? AstSpan.sliceSpan(lines, subjectStart, subjectEnd)
				: null;
		if (subjectText == null)
			return null;

		// the subject's own text is copied verbatim, so only the parentheses, the cast and whatever
		// stands on either side of the subject are being replaced. A comment written in any of that
		// has to come back, including one past the subject, which the region also swallows
		final var span = new TextSpan(start, end);
		final var emitted = SpanCarry.carried(lines, span, List.of(new TextSpan(subjectStart, subjectEnd))) + subjectText;
		return SpanRewrite.replacing(lines, span, SpanCarry.separated(lines, span, emitted));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, RedundantCastCheck::targetAt);
		return target == null ? null : rewrite(lines, target);
	}
}