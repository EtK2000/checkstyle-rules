package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * What a fixer emits in place of a span it deletes: the comments written inside it, kept in source
 * order, spaced away from whatever the edit leaves standing on either side.
 *
 * <p>Shared by the fixers whose whole edit is "take this region out and keep what was written in
 * it", where the region is the construct being removed rather than a value being rewritten.
 */
final class SpanCarry {
	/**
	 * The comments inside {@code span} as written, in source order and followed by a space, or empty
	 * when there are none. {@code kept} names the regions whose text the rewrite copies verbatim, so
	 * a comment written there travels with that copy and must not be written out a second time.
	 *
	 * <p>A {@code //} comment never survives into the result: it runs to the end of its line, so the
	 * span holding it reaches a later one, and a span crossing lines is refused by
	 * {@link SpanRewrite#replacing} before any of this is re-emitted.
	 */
	@CheckReturnValue
	@Nonnull
	static String carried(@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull List<TextSpan> kept) {
		final var comments = SpanComments.inside(lines, span, kept);
		return comments.isEmpty() ? "" : String.join(" ", comments) + ' ';
	}

	/**
	 * Whether the end of {@code before} and the start of {@code after} would read as one token. A
	 * slash ending {@code before} counts against a comment opener starting {@code after}: a relocated
	 * comment is written in front of what follows the span, so a division written tight against the
	 * removed construct would otherwise put {@code /*} straight after the slash, where the pair reads
	 * as a line comment and takes the rest of the line with it.
	 */
	@CheckReturnValue
	static boolean fuses(@Nonnull String before, @Nonnull String after) {
		if (before.isEmpty() || after.isEmpty())
			return false;
		final var last = before.charAt(before.length() - 1);
		final var first = after.charAt(0);
		return (LineText.isIdentifierPart(last) && LineText.isIdentifierPart(first))
				|| (last == '/' && (first == '/' || first == '*'));
	}

	/**
	 * {@code body} spaced away from whichever edge of {@code span} it would otherwise run into.
	 * {@code return((String) s).length();} has to come out as {@code return s.length();} rather than
	 * {@code returns.length();}, and {@code ((String) s)instanceof Object} as {@code s instanceof
	 * Object} rather than {@code sinstanceof Object}.
	 */
	@CheckReturnValue
	@Nonnull
	static String separated(@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull String body) {
		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		// with nothing emitted between them the two edges meet each other, so one test settles both
		final var following = body.isEmpty() ? edges.suffix() : body;
		return (fuses(edges.prefix(), following) ? " " : "")
				+ body
				+ (!body.isEmpty() && fuses(body, edges.suffix()) ? " " : "");
	}

	private SpanCarry() {}
}