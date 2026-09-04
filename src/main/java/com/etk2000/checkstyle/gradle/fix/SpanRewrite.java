package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Turns a span a check located into the single line that replaces it, keeping whatever the span's
 * own first and last lines held outside it.
 *
 * <p>The counterpart to {@link AstSplice} for a fixer working from a {@link TextSpan} rather than
 * from a node, and for one whose span may run across lines: {@code AstSplice} refuses a cross-line
 * span by design, because its callers rewrite a token in place rather than fold a construct.
 */
final class SpanRewrite {
	/**
	 * Whether the text of {@code [start, end)} ends inside a block comment or a text block it opened
	 * itself. Such a span cannot be replaced at all: the lines carrying the rest of the literal sit
	 * below {@code end} and are left behind, orphaned, once its opener goes. That is true whether or
	 * not the span is folded, so this applies to a single-line span as much as to a multi-line one.
	 *
	 * <p>Package-visible because a fixer that assembles its own replacement rather than going through
	 * {@link #replacing} owes the same guard, and a second copy of it would drift from this one.
	 */
	@CheckReturnValue
	static boolean leavesALiteralOpen(
			@Nonnull List<String> lines, @Nonnull TextPos start, @Nonnull TextPos end
	) {
		var state = LexerState.NONE;
		for (var i = start.line(); i <= end.line(); ++i) {
			final var line = lines.get(i);
			state = JavaLineScanner.stateAfter(
					line.substring(i == start.line() ? start.index() : 0, i == end.line() ? end.index() : line.length()),
					state
			);
		}
		return state.inMultilineLiteral();
	}

	/**
	 * The edit replacing {@code span} with {@code emitted}, or null when that replacement is unsafe
	 * or would change nothing.
	 *
	 * <p>Null covers four cases, all of which a caller treats the same way (leave the violation
	 * alone): the span does not describe a sliceable region; {@code emitted} carries a line
	 * terminator; the span cannot be replaced, either because folding it onto one line would lose a
	 * comment or because its own text leaves a literal open; the result is the source text.
	 *
	 * <p>Note what is <em>not</em> refused. A comment written inside a <strong>single-line</strong>
	 * span is discarded along with the rest of the span's text, because nothing is being folded and
	 * the comment sits in the region the caller asked to replace. That is deliberate and is what the
	 * one current caller wants (see {@code docs/coverage/lambdaparametertype.md}: a comment inside a
	 * parameter goes with the type). A caller that must preserve interior comments has to gate on
	 * that itself, the way {@link com.etk2000.checkstyle.LambdaParameterTypeCheck#collapsible} gates
	 * the multi-line case by comparing the lines against their mask.
	 *
	 * <p>A multi-line span whose text strips to nothing is refused rather than folded, which is a
	 * stricter answer than the fold's safety needs; it falls out of using {@code sliceSpan} as the
	 * gate. No caller folds whitespace, so it has not been worth a separate predicate.
	 */
	@CheckReturnValue
	@Nullable
	static FixResult replacing(@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull String emitted) {
		final var start = span.start();
		final var end = span.end();
		if (AstSpan.outOfBounds(lines, start, end))
			return null;
		// a terminator inside the emitted text would go into one element of the replacement, where
		// it desynchronises every later line index: a FixResult counts its lines by element
		if (emitted.indexOf('\n') >= 0 || emitted.indexOf('\r') >= 0)
			return null;
		// folding a multi-line span onto one line is only safe when nothing inside it needs a line
		// of its own: a `//` comment would comment out everything appended after it, and a text
		// block's opening delimiter must be the last thing on its line. sliceSpan refuses a comment
		// or a literal carried INTO a line, which is why the gate is its result rather than a scan
		// written again here; what it does not refuse is a span whose own last line LEAVES one open,
		// because it tests the carried-in state at the top of each line and discards the state the
		// final line produces. Measured: a span ending in `"""` sliced to `foo("""` and folded,
		// deleting the opener and orphaning the block's content on the lines below.
		// leavesALiteralOpen applies to a single-line span too: nothing is folded there, but
		// replacing text that opened a literal still orphans the lines carrying the rest of it
		if ((start.line() != end.line() && AstSpan.sliceSpan(lines, start, end) == null)
				|| leavesALiteralOpen(lines, start, end))
			return null;

		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var rewritten = edges.prefix() + emitted + edges.suffix();
		// a position that resolved to a construct already in this form would otherwise count as a
		// fix and send the file round the pipeline again, up to the pass limit, without ever
		// changing anything. Only a single-line span can come back unchanged, since folding several
		// lines into one always sheds lines
		if (start.line() == end.line() && rewritten.equals(lines.get(start.line())))
			return null;
		return new FixResult(start.line(), end.line(), List.of(rewritten));
	}

	private SpanRewrite() {
	}
}