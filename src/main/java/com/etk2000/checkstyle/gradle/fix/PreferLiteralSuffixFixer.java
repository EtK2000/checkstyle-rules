package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.PreferLiteralSuffixCheck;
import com.etk2000.checkstyle.PreferLiteralSuffixCheck.LiteralSuffixTarget;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.format.SpanEdges;
import com.etk2000.checkstyle.format.SpanReformat;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Deletes a widening cast ({@code (long)}, {@code (float)}, {@code (double)}) and appends the
 * matching suffix ({@code L}, {@code f}, {@code d}) to the integer literal the check paired it with.
 */
class PreferLiteralSuffixFixer implements CheckstyleFixer {
	/**
	 * Appends {@code piece} to {@code text}, separating the two where the junction would otherwise
	 * run one token into the next. A piece that was folded arrives stripped, so the whitespace that
	 * stood between it and its neighbour went with the line break; a piece taken from a single line
	 * still carries its own, which is why an existing space on either side settles it first.
	 */
	private static void appendPiece(@Nonnull StringBuilder text, @Nonnull String piece) {
		if (!text.isEmpty() && !piece.isEmpty()
				&& !Character.isWhitespace(text.charAt(text.length() - 1))
				&& !Character.isWhitespace(piece.charAt(0))
				&& !SpanReformat.joinsTight(text, piece))
			text.append(' ');
		text.append(piece);
	}

	/**
	 * The text replacing the region: its pieces joined in source order with {@code suffix} written
	 * straight after piece {@code suffixAfter}, or null when a piece cannot be folded onto one line.
	 */
	@CheckReturnValue
	@Nullable
	private static String emitted(
			@Nonnull List<String> lines, @Nonnull List<TextSpan> copied, int suffixAfter, @Nonnull String suffix
	) {
		final var text = new StringBuilder();
		for (var i = 0; i < copied.size(); ++i) {
			final var piece = copied.get(i);
			final var slice = AstSpan.sliceSpan(lines, piece.start(), piece.end());
			if (slice == null)
				return null;
			appendPiece(text, slice);
			if (i == suffixAfter)
				text.append(suffix);
		}
		return text.toString();
	}

	@CheckReturnValue
	private static boolean isBefore(@Nonnull TextPos a, @Nonnull TextPos b) {
		return a.line() != b.line() ? a.line() < b.line() : a.index() < b.index();
	}

	/**
	 * The edit removing {@code target}'s cast, or a skip when the rewrite would lose text or overrun
	 * the line budget.
	 *
	 * <p>The deleted range ends at the subject's first token rather than at the cast's own closing
	 * parenthesis, so parentheses around the subject survive and so does a comment written between
	 * the two under a parse that carries comment nodes.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull LiteralSuffixTarget target) {
		// the operator or ternary the cast is an operand of: the expression being rewritten, and so
		// the region emitted as one line. Widening no further leaves a wrap elsewhere in the
		// statement alone, which is what keeps this out of MultilineCallFormatting's way
		final var start = AstSpan.spanStart(lines, target.typecast().getParent());
		final var end = AstSpan.spanEnd(lines, target.typecast().getParent());
		final var castStart = AstSpan.spanStart(lines, target.typecast());
		final var literalEnd = AstSpan.spanEnd(lines, target.literal());
		final var subjectStart = AstSpan.spanStart(lines, target.subject());
		if (start == null || end == null || castStart == null || literalEnd == null || subjectStart == null)
			return null;
		if (AstSpan.outOfBounds(lines, start, end))
			return null;
		if (SpanRewrite.leavesALiteralOpen(lines, start, end))
			return new SkipResult(SkipMessages.PREFER_LITERAL_SUFFIX_SKIP_UNSLICEABLE);

		// the literal is the cast's sibling, so it lies wholly on one side of it: the region is the
		// text either side of the cast, cut again where the suffix goes. The piece past the literal
		// is empty whenever the literal is the expression's last operand, which is every shape the
		// check reports today; it is written out so the cut does not depend on that staying true
		final var literalFirst = isBefore(literalEnd, castStart);
		final var copied = literalFirst
				? List.of(
						new TextSpan(start, literalEnd),
						new TextSpan(literalEnd, castStart),
						new TextSpan(subjectStart, end)
				)
				: List.of(
						new TextSpan(start, castStart),
						new TextSpan(subjectStart, literalEnd),
						new TextSpan(literalEnd, end)
				);
		final var text = emitted(lines, copied, literalFirst ? 0 : 1, target.suffix());
		if (text == null)
			return new SkipResult(SkipMessages.PREFER_LITERAL_SUFFIX_SKIP_UNSLICEABLE);

		final var span = new TextSpan(start, end);
		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var relocated = SpanComments.relocate(lines, span, copied);
		final var comment = edges.appendableComment(relocated);
		// the suffix opens a literal nothing may follow, so the comment the rewrite displaced has
		// nowhere to go; dropping it is the one way this rewrite could lose text silently
		if (comment.isEmpty() && !relocated.isEmpty())
			return new SkipResult(SkipMessages.PREFER_LITERAL_SUFFIX_SKIP_UNSLICEABLE);

		final var rewritten = edges.prefix() + text + edges.suffix()
				+ (comment.isEmpty() ? "" : ' ' + comment);
		return LineLength.tabExpandedLength(rewritten) > LineLength.MAX_LINE_LENGTH
				? new SkipResult(SkipMessages.PREFER_LITERAL_SUFFIX_SKIP_LINE_BUDGET)
				: new FixResult(start.line(), end.line(), List.of(rewritten));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, PreferLiteralSuffixCheck::targetAt);
		return target == null ? null : rewrite(lines, target);
	}
}