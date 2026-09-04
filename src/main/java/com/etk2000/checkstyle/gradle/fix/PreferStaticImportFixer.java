package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.PreferStaticImportCheck;
import com.etk2000.checkstyle.PreferStaticImportCheck.StaticCallTarget;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code PreferStaticImportCheck}. Replaces the text from the receiver naming the class up
 * to the method name it qualifies, which deletes the qualifier along with whatever was written
 * between the two: whitespace, a comment, or a line break.
 *
 * <p>A comment inside that text moves to the front of the method name, the text it was written
 * against being what goes.
 */
class PreferStaticImportFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull StaticCallTarget target) {
		final var start = AstSpan.spanStart(lines, target.receiver());
		final var end = AstSpan.spanStart(lines, target.methodIdent());
		if (start == null || end == null)
			return null;

		final var span = new TextSpan(start, end);
		final var emitted = SpanCarry.separated(lines, span, SpanCarry.carried(lines, span, List.of()));
		final var fix = SpanRewrite.replacing(lines, span, emitted);
		if (fix == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_SKIP_UNSLICEABLE);

		// folding a multi-line span can join two lines each inside the budget into one that is not,
		// and no fixer breaks a line to satisfy the limit, so the fix would trade this violation for
		// one that never clears. A single-line span is not asked, because refusing on length alone
		// would decline to shorten a line that is already too long. That leaves a hole: a single-line
		// span re-emits every comment it holds with a space around it, so enough of them grow the
		// line instead of shrinking it, measured at nine for `Objects`. See docs/gaps/preferstaticimport.md
		if (start.line() != end.line()
				&& LineLength.tabExpandedLength(fix.replacement().getFirst()) > LineLength.MAX_LINE_LENGTH)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_SKIP_LINE_BUDGET);

		return new FixResult(
				fix.startLine(),
				fix.endLine(),
				fix.replacement(),
				Set.of("static " + target.fqcn() + '.' + target.simpleMethod())
		);
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, PreferStaticImportCheck::staticCallAt);
		return target == null ? null : rewrite(lines, target);
	}
}