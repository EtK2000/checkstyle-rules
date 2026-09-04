package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationOwnLineCheck;
import com.etk2000.checkstyle.AnnotationOwnLineCheck.AnnotationLayoutTarget;
import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Puts a declaration's annotations one to a line, in alphabetical order, above the declaration, and
 * deletes the blank lines the check reports. The group's span runs from its first modifier to the
 * declaration, so everything written inside it, comments and other modifiers included, is re-emitted
 * rather than dropped.
 */
class AnnotationOwnLineFixer implements CheckstyleFixer {
	/**
	 * What a gap between two of the group's tokens holds, split by where it goes. Text on the gap's
	 * first line trails the token before the gap and stays on that token's line. Whole lines between
	 * go above the token after it, copied as they stand. Text on the gap's last line leads that token
	 * and travels with it.
	 */
	private record GapText(@Nonnull String trailing, @Nonnull List<String> above, @Nonnull String leading) {}

	/**
	 * One of the group's tokens as it will be written, with the comment lines that go above it.
	 *
	 * @param sortKey the name the check orders by, or empty for a modifier, which keeps source order
	 */
	private record Piece(@Nonnull List<String> above, @Nonnull String sortKey, @Nonnull String text) {}

	/**
	 * Adds {@code text} to {@code out} at the start line's own indentation, which is where every line
	 * of the group goes: whatever shared that line ahead of the group has already been emitted above.
	 * Text carrying line breaks comes from an annotation kept as written, whose later lines are
	 * already indented as they stand.
	 */
	private static void append(@Nonnull List<String> out, @Nonnull SpanEdges edges, @Nonnull String text) {
		final var parts = text.split("\n", -1);
		out.add(edges.indent() + parts[0]);
		for (var i = 1; i < parts.length; ++i)
			out.add(parts[i]);
	}

	/** Deletes the run of blank lines starting at {@code from}. */
	@CheckReturnValue
	@Nonnull
	private static FixResult blankRun(@Nonnull List<String> lines, int from) {
		var last = from;
		while (last + 1 < lines.size() && lines.get(last + 1).isBlank())
			++last;
		return new FixResult(from, last, List.of());
	}

	@CheckReturnValue
	@Nonnull
	private static GapText gapText(
			@Nonnull List<String> lines, @Nonnull TextSpan gap, @Nonnull String indent
	) {
		final var first = lines.get(gap.start().line());
		// each side keeps the whitespace that separated it from its own token, so a junction the
		// author wrote without a space stays without one. A gap written on one line cannot leave a
		// comment open, since the token that ends it would be inside one
		if (gap.start().line() == gap.end().line()) {
			return new GapText(
					"", List.of(), first.substring(gap.start().index(), gap.end().index()).stripLeading()
			);
		}

		// the lines between keep their own line, so they are copied as written: a block comment's
		// interior belongs to whoever wrote it, and stripping it or dropping a blank line inside it
		// rewrites the comment. A blank line outside one is the whitespace this fix is removing.
		// The gap opens just past a token, so the lexer state there is known without a fold
		final var trailing = first.substring(gap.start().index()).stripTrailing();
		final var leading = lines.get(gap.end().line()).substring(0, gap.end().index()).stripLeading();
		var state = JavaLineScanner.stateAfter(trailing, LexerState.NONE);
		final var opened = state.inMultilineLiteral();

		final var above = new ArrayList<String>();
		// a trailing part that leaves a block comment or a text block open is kept with the rest of
		// the gap instead of riding on the token before it: sorting moves that token, which would
		// strand the opener from its closing delimiter and swallow whatever ended up between them
		if (opened)
			above.add(indent + trailing.strip());

		for (var i = gap.start().line() + 1; i < gap.end().line(); ++i) {
			final var line = lines.get(i);
			if (!line.isBlank() || state.inMultilineLiteral())
				above.add(line);
			state = JavaLineScanner.stateAfter(line, state);
		}
		// a leading part closing a construct the gap's interior left open travels with those lines as
		// well. The token after the gap is written on the declaration's own line, so leaving the
		// closing delimiter on it would put it below its opener and comment out whatever joins it
		final var keepsLeading = opened || state.inMultilineLiteral();
		if (keepsLeading && !leading.isEmpty())
			above.add((indent + leading).stripTrailing());
		return new GapText(opened ? "" : trailing, above, keepsLeading ? "" : leading);
	}

	/**
	 * Whether {@code line} begins inside a block comment or a text block opened above it.
	 *
	 * <p>Folded forward from the buffer's first line, which is the only anchor whose state is known:
	 * the group's own first line cannot serve, since that is the line in question.
	 */
	@CheckReturnValue
	private static boolean opensInsideLiteral(@Nonnull List<String> lines, int line) {
		var state = LexerState.NONE;
		for (var i = 0; i < line && i < lines.size(); ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		return state.inMultilineLiteral();
	}

	/**
	 * The edit putting {@code target}'s group one annotation to a line above the declaration, or null
	 * when the span does not resolve.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt reemitted(@Nonnull List<String> lines, @Nonnull AnnotationLayoutTarget target) {
		final var holder = target.holder();
		final var declaration = holder.getNextSibling();
		if (declaration == null)
			return null;

		final var start = AstSpan.spanStart(lines, AstSpan.startToken(holder));
		final var end = AstSpan.spanStart(lines, AstSpan.startToken(declaration));
		if (AstSpan.outOfBounds(lines, start, end))
			return null;

		final var children = new ArrayList<DetailAST>();
		final var covered = new ArrayList<TextSpan>();
		for (var child = holder.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childStart = AstSpan.spanStart(lines, child);
			final var childEnd = AstSpan.spanEnd(lines, child);
			if (AstSpan.outOfBounds(lines, childStart, childEnd))
				return null;
			children.add(child);
			covered.add(new TextSpan(childStart, childEnd));
		}

		final var edges = SpanEdges.of(lines, start.line(), start.index(), end.line(), end.index());
		final var gaps = new ArrayList<GapText>();
		for (var gap : AstSpan.gaps(new TextSpan(start, end), covered))
			gaps.add(gapText(lines, gap, edges.indent()));

		// a comment written immediately ahead of the group describes what follows it, so it travels
		// down with the group rather than staying on the line the group is lifted off. Masking keeps
		// the prefix's own indices, so the last code character locates the run without re-lexing it.
		// It reads the prefix as code, which a line opening inside a block comment or a text block is
		// not: there the prefix holds that construct's closing delimiter, and carrying it below the
		// annotations would put them inside the construct, so the whole prefix keeps its own line
		final var masked = JavaLineScanner.stripCommentsAndStrings(edges.prefix(), LexerState.NONE);
		final var head = opensInsideLiteral(lines, start.line())
				? edges.prefix()
				: edges.prefix().substring(0, masked.stripTrailing().length());
		final var carried = edges.prefix().substring(head.length()).strip();

		final var annotations = new ArrayList<Piece>();
		final var modifiers = new ArrayList<Piece>();
		final var aboveDeclaration = new ArrayList<String>();
		for (var i = 0; i < children.size(); ++i) {
			final var child = children.get(i);
			final var written = tokenText(lines, child);
			if (written == null)
				return null;

			final var lead = i == 0 && !carried.isEmpty()
					? carried + ' ' + gaps.getFirst().leading()
					: gaps.get(i).leading();
			var trailing = gaps.get(i + 1).trailing();
			if (child.getType() == TokenTypes.ANNOTATION) {
				annotations.add(new Piece(gaps.get(i).above(), AstText.annotationName(child), lead + written + trailing));
				continue;
			}

			// an annotation keeps a line to itself, so a comment trailing one is safe there, but the
			// modifiers are written onto the declaration's own line and a line comment joined onto it
			// would swallow the declaration. The comment keeps its text, on the line above instead
			final var comment = JavaLineScanner.firstLineComment(trailing, LexerState.NONE);
			if (comment >= 0) {
				aboveDeclaration.add(edges.indent() + trailing.substring(comment).strip());
				trailing = trailing.substring(0, comment).stripTrailing();
			}
			modifiers.add(new Piece(gaps.get(i).above(), "", lead + written + trailing));
		}
		annotations.sort(Comparator.comparing(Piece::sortKey));

		final var replacement = new ArrayList<String>();
		final var tail = new ArrayList<String>();
		// whatever shares the group's line ahead of it keeps that line, and the group starts on the
		// next one, which is what putting an annotation on its own line means when something else is
		// written before it. The group opens on a token, so the break is always between two of them
		if (!head.isBlank())
			replacement.add(head.stripTrailing());

		// a carried line is written at its own indentation, and a group that has any of them spans
		// lines, so its first token sits at its line's indentation and the prefix is that indentation
		for (var piece : annotations) {
			replacement.addAll(piece.above());
			append(replacement, edges, piece.text());
		}

		// the annotations lead, which is the only placement sorting leaves defined for a modifier
		// written among them; the modifiers keep their own order and stay with the declaration
		for (var piece : modifiers) {
			replacement.addAll(piece.above());
			tail.add(piece.text());
		}

		final var last = gaps.getLast();
		final var modifierText = tail.isEmpty() ? "" : String.join(" ", tail) + ' ';
		// a comment pulled out of the last modifier's own line was written before that gap's interior
		// lines, so it is emitted before them
		replacement.addAll(aboveDeclaration);
		replacement.addAll(last.above());
		append(replacement, edges, modifierText + last.leading() + edges.suffix().stripTrailing());
		return new FixResult(start.line(), end.line(), replacement);
	}

	/**
	 * How {@code child} is written on the line it ends up on: a modifier as its keyword, an annotation
	 * as the source it was sliced from. An annotation written across lines is folded onto one where
	 * that is safe, and kept as written where a comment or a text block inside it makes folding lose
	 * the comment or the line break the literal depends on.
	 */
	@CheckReturnValue
	@Nullable
	private static String tokenText(@Nonnull List<String> lines, @Nonnull DetailAST child) {
		if (child.getType() != TokenTypes.ANNOTATION)
			return child.getText();

		final var start = AstSpan.spanStart(lines, child);
		final var end = AstSpan.spanEnd(lines, child);
		final var folded = AstSpan.sliceSpan(lines, start, end);
		return folded != null ? folded : AstSpan.sliceVerbatim(lines, start, end);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(
				lines,
				lineIndex,
				column,
				(root, line, col) -> AnnotationOwnLineCheck.layoutAt(root, lines, line, col)
		);
		if (target == null)
			return null;

		return switch (target.layout()) {
			case ALPHABETICAL_ORDER, OWN_LINE -> reemitted(lines, target);
			case BLANK_AFTER, BLANK_INTERNAL -> blankRun(lines, target.blankLine() - 1);
		};
	}
}