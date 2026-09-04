package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationSameLineCheck;
import com.etk2000.checkstyle.AnnotationSameLineCheck.AnnotationPlacementTarget;
import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Puts a declaration's annotations back on its own line, in alphabetical order. The group's span
 * runs from the first modifier to the declaration, so everything written inside it, comments and
 * other modifiers included, is re-emitted rather than dropped.
 */
class AnnotationSameLineFixer implements CheckstyleFixer {
	private record CarriedComments(@Nonnull List<String> above, @Nonnull List<String> inline) {}

	/**
	 * The comments written between the group's tokens, split by where they can go. A gap written on
	 * one line holds block comments only, since a {@code //} there would have taken the rest of that
	 * line with it and the gap would not have closed on it, so such a gap survives inline. A gap
	 * running across lines keeps its own lines, which is the only placement a {@code //} comment, or
	 * a block comment spanning lines, has once the rest of the span is joined.
	 */
	@CheckReturnValue
	@Nonnull
	private static CarriedComments carriedComments(@Nonnull List<String> lines, @Nonnull List<TextSpan> gaps) {
		final var above = new ArrayList<String>();
		final var inline = new ArrayList<String>();
		for (var gap : gaps) {
			for (var i = gap.start().line(); i <= gap.end().line(); ++i) {
				final var line = lines.get(i);
				final var from = i == gap.start().line() ? gap.start().index() : 0;
				final var to = i == gap.end().line() ? gap.end().index() : line.length();
				final var text = line.substring(from, to).strip();
				if (text.isEmpty())
					continue;
				if (gap.start().line() == gap.end().line())
					inline.add(text);
				else
					above.add(text);
			}
		}
		return new CarriedComments(above, inline);
	}

	/**
	 * The edit putting {@code target}'s group on the declaration's line in order, or the reason
	 * there is none.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt joined(@Nonnull List<String> lines, @Nonnull AnnotationPlacementTarget target) {
		final var start = AstSpan.spanStart(lines, target.owner());
		final var end = AstSpan.spanStart(lines, target.anchor());
		if (start == null || end == null || AstSpan.outOfBounds(lines, start, end))
			return null;

		final var covered = new ArrayList<TextSpan>();
		for (var child = target.owner().getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childStart = AstSpan.spanStart(lines, child);
			final var childEnd = AstSpan.spanEnd(lines, child);
			if (childStart == null || childEnd == null || AstSpan.outOfBounds(lines, childStart, childEnd))
				return null;
			covered.add(new TextSpan(childStart, childEnd));
		}

		// sorted on the name the check orders by, and each one sliced where it stands, so a name
		// spelled with a space or a comment after the `@`, with a package qualifier, or with an
		// argument list written across lines, is re-emitted as written rather than rebuilt
		final var sorted = new ArrayList<>(target.annotations());
		sorted.sort(Comparator.comparing(AstText::annotationName));
		final var parts = new ArrayList<String>();
		for (var annotation : sorted) {
			final var text = AstSpan.sliceSpan(
					lines, AstSpan.spanStart(lines, annotation), AstSpan.spanEnd(lines, annotation)
			);
			if (text == null)
				return new SkipResult(SkipMessages.ANNOTATION_SAME_LINE_SKIP_UNFOLDABLE);
			parts.add(text);
		}

		// the annotations lead, which is the only placement sorting leaves defined for a modifier
		// written among them; the modifiers keep their own order
		for (var child = target.owner().getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				parts.add(child.getText());
		}

		final var comments = carriedComments(lines, AstSpan.gaps(new TextSpan(start, end), covered));
		parts.addAll(comments.inline());

		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var joinedLine = edges.prefix() + String.join(" ", parts) + ' ' + edges.suffix();
		if (comments.above().isEmpty())
			return new FixResult(start.line(), end.line(), List.of(joinedLine));

		// the carried lines go ahead of the span's own first line, so when that line begins inside a
		// block comment they would land inside it and the first `*/` among them would close it early,
		// turning the rest of that comment back into code. Going above the line it opened on keeps
		// them out of it, and the lines in between are copied as they stand
		final var hoistLine = openLine(lines, start.line());
		final var indent = hoistLine == start.line()
				? edges.indent()
				: LineText.extractIndent(lines.get(hoistLine));
		final var replacement = new ArrayList<String>();
		for (var comment : comments.above())
			replacement.add(indent + comment);
		for (var i = hoistLine; i < start.line(); ++i)
			replacement.add(lines.get(i));
		replacement.add(joinedLine);
		return new FixResult(hoistLine, end.line(), replacement);
	}

	/**
	 * The first line at or before {@code line} that does not itself begin inside a block comment or a
	 * text block, which is the line whatever is open at {@code line}'s start was opened on.
	 *
	 * <p>Threaded forward once rather than asked per line walked back: the lexer state at a line is
	 * folded from the buffer's first line, so walking back costs the open literal's length times the
	 * distance to it, which is measurably quadratic on a long commented-out block.
	 */
	@CheckReturnValue
	private static int openLine(@Nonnull List<String> lines, int line) {
		var state = LexerState.NONE;
		var opened = 0;
		for (var i = 0; i < line && i < lines.size(); ++i) {
			if (!state.inMultilineLiteral())
				opened = i;
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		}
		return state.inMultilineLiteral() ? opened : line;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(
				lines, lineIndex, column, AnnotationSameLineCheck::placementAt
		);
		return target == null ? null : joined(lines, target);
	}
}