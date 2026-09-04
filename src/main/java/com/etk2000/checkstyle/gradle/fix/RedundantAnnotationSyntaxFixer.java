package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck.AnnotationSyntaxTarget;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Deletes the redundant part of an annotation: the empty parentheses, or the {@code value =} key in
 * front of the one value they hold.
 */
class RedundantAnnotationSyntaxFixer implements CheckstyleFixer {
	/**
	 * Whether a block comment inside {@code span} runs across one of its line breaks. Such a comment
	 * cannot be written on a single line, so no fold joining those lines is safe.
	 */
	@CheckReturnValue
	private static boolean commentCrossesALine(@Nonnull List<String> lines, @Nonnull TextSpan span) {
		var state = LexerState.NONE;
		for (var i = span.start().line(); i < span.end().line(); ++i) {
			final var line = lines.get(i);
			state = JavaLineScanner.stateAfter(
					i == span.start().line() ? line.substring(span.start().index()) : line, state
			);
			if (state.inBlockComment())
				return true;
		}
		return false;
	}

	/**
	 * The edit taking {@code target}'s empty parentheses out, or the reason there is none. The span
	 * runs from the end of the name to past the closing parenthesis, so whatever was written between
	 * the name and the {@code (} goes with it, and every comment the span held is re-emitted after
	 * the name.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt emptyParensFix(@Nonnull List<String> lines, @Nonnull AnnotationSyntaxTarget target) {
		final var start = AstSpan.spanEnd(lines, target.name());
		final var end = AstSpan.tokenEnd(lines, target.rparen());
		if (start == null || end == null || AstSpan.outOfBounds(lines, start, end))
			return null;

		final var span = new TextSpan(start, end);
		final var comments = SpanComments.inside(lines, span, List.of());
		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var suffix = edges.suffix();
		final var refusal = foldRefusal(lines, span, comments, suffix);
		if (refusal != null)
			return refusal;

		// taking the parentheses out puts the name, or the last comment carried after it, straight
		// against whatever followed them: an identifier or another annotation there needs a space of
		// its own (@Override()void -> @Override void, @A()@B -> @A @B), while a tail already opening
		// with whitespace, or with punctuation that cannot read as one token with a name, does not.
		// LineText's test rather than Character's, because the first char of a type name spelled with
		// a supplementary letter is a lone high surrogate, which the char-wise test rejects: measured,
		// omitting the space there fused the names and the result did not parse
		final var separated = !suffix.isEmpty()
				&& (LineText.isIdentifierPart(suffix.charAt(0)) || suffix.charAt(0) == '@');
		final var body = (comments.isEmpty() ? "" : ' ' + String.join(" ", comments)) + (separated ? " " : "");
		return rewritten(lines, span, edges, body);
	}

	/**
	 * The edit taking {@code target}'s {@code value =} out, or the reason there is none. The whole
	 * annotation is folded onto one line when it was written across several and folding is safe;
	 * otherwise only the key and the {@code =} go, which leaves the annotation spread as written.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt explicitValueFix(@Nonnull List<String> lines, @Nonnull AnnotationSyntaxTarget target) {
		final var keyStart = AstSpan.tokenStart(lines, target.key());
		final var valueStart = AstSpan.spanStart(lines, target.value());
		if (keyStart == null || valueStart == null || AstSpan.outOfBounds(lines, keyStart, valueStart))
			return null;

		final var folded = foldedAnnotation(lines, target, valueStart);
		if (folded != null)
			return folded;

		final var span = new TextSpan(keyStart, valueStart);
		final var edges = SpanEdges.of(
				lines, keyStart.line(), keyStart.index(), valueStart.line(), valueStart.index()
		);
		final var comments = SpanComments.inside(lines, span, List.of());
		final var refusal = foldRefusal(lines, span, comments, edges.suffix());
		if (refusal != null)
			return refusal;

		// the value follows immediately, so a carried comment is spaced off it rather than off the
		// `(` in front, which cannot read as one token with anything
		final var body = comments.isEmpty() ? "" : String.join(" ", comments) + ' ';
		return rewritten(lines, span, edges, body);
	}

	/**
	 * {@code target}'s whole annotation on one line without the {@code value =}, or null when it is
	 * already on one line, when folding it would lose something, or when the result would not fit.
	 * Null here is a fall-back rather than a refusal: the key still goes, the annotation just keeps
	 * the lines it was written on.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult foldedAnnotation(
			@Nonnull List<String> lines, @Nonnull AnnotationSyntaxTarget target, @Nonnull TextPos valueStart
	) {
		final var start = AstSpan.spanStart(lines, target.annotation());
		final var valueEnd = AstSpan.spanEnd(lines, target.value());
		final var end = AstSpan.tokenEnd(lines, target.rparen());
		if (start == null || valueEnd == null || end == null
				|| start.line() == end.line() || AstSpan.outOfBounds(lines, start, end))
			return null;

		// the name is sliced rather than spelled from the reported one, which is the unqualified
		// form: rebuilding `@com.example.A` from it would drop the package
		final var nameEnd = AstSpan.spanEnd(lines, target.name());
		final var head = AstSpan.sliceSpan(lines, start, nameEnd);
		final var value = AstSpan.sliceSpan(lines, valueStart, valueEnd);
		final var span = new TextSpan(start, end);
		if (head == null || value == null || AstSpan.outOfBounds(lines, valueEnd, end)
				|| commentCrossesALine(lines, span))
			return null;

		// the head is copied as written, so a comment written between the `@` and the name
		// travels with it and must not be carried a second time
		final var leading = SpanComments.inside(
				lines, new TextSpan(start, valueStart), List.of(new TextSpan(start, nameEnd))
		);
		final var trailing = SpanComments.inside(lines, new TextSpan(valueEnd, end), List.of());
		for (var comment : leading) {
			if (comment.startsWith("//"))
				return null;
		}
		for (var comment : trailing) {
			if (comment.startsWith("//"))
				return null;
		}

		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var collapsed = edges.prefix()
				+ head + '('
				+ (leading.isEmpty() ? "" : String.join(" ", leading) + ' ')
				+ value
				+ (trailing.isEmpty() ? "" : ' ' + String.join(" ", trailing))
				+ ')'
				+ edges.suffix();
		// no exemption for a span already over the budget, which is the rule the other emission site
		// carries: joining can make a line far longer than any it replaces, so the exemption would
		// buy nothing and cost that
		return overBudget(collapsed) ? null : new FixResult(start.line(), end.line(), List.of(collapsed));
	}

	/**
	 * Why {@code span} will not fold onto one line, or null when it will. Only a span that really
	 * does cross lines can refuse: nothing is joined inside one line, so a comment written there
	 * travels with the text around it.
	 *
	 * <p>A {@code //} comment survives a fold only as the last thing on the joined line, since it
	 * takes the rest of that line with it. Anything emitted after it, another comment or the text
	 * standing past the span, would be commented out.
	 */
	@CheckReturnValue
	@Nullable
	private static SkipResult foldRefusal(
			@Nonnull List<String> lines,
			@Nonnull TextSpan span,
			@Nonnull List<String> comments,
			@Nonnull String suffix
	) {
		// nothing below can answer true for a single-line span: the line-boundary scan has no
		// boundary to cross, and a `//` inside the span would have commented out the token the span
		// ends at. Stated here rather than left to fall out of both
		if (span.start().line() == span.end().line())
			return null;
		if (commentCrossesALine(lines, span))
			return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP_COMMENT);

		for (var i = 0; i < comments.size(); ++i) {
			if (comments.get(i).startsWith("//") && (i < comments.size() - 1 || !suffix.isBlank()))
				return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP_LINE_COMMENT);
		}
		return null;
	}

	/**
	 * Whether {@code emitted} pushes a line past the budget that was not already past it. A span
	 * whose own source is already over the limit cannot be brought under it by taking an annotation
	 * apart, and refusing those would keep the redundant syntax for good.
	 */
	@CheckReturnValue
	private static boolean lengthensPastBudget(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull String emitted
	) {
		if (!overBudget(emitted))
			return false;

		for (var line = span.start().line(); line <= span.end().line(); ++line) {
			if (overBudget(lines.get(line)))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean overBudget(@Nonnull String line) {
		return LineLength.tabExpandedLength(line) > LineLength.MAX_LINE_LENGTH;
	}

	/** The edit replacing {@code span} with {@code body}, or the reason there is none. */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt rewritten(
			@Nonnull List<String> lines,
			@Nonnull TextSpan span,
			@Nonnull SpanEdges edges,
			@Nonnull String body
	) {
		final var rewritten = edges.prefix() + body + edges.suffix();
		return lengthensPastBudget(lines, span, rewritten)
				? new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP_LINE_BUDGET)
				: new FixResult(span.start().line(), span.end().line(), List.of(rewritten));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(
				lines, lineIndex, column, RedundantAnnotationSyntaxCheck::redundantSyntaxAt
		);
		if (target == null)
			return null;

		return switch (target.redundancy()) {
			case EMPTY_PARENS -> emptyParensFix(lines, target);
			case EXPLICIT_VALUE -> explicitValueFix(lines, target);
		};
	}
}