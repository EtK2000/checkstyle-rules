package com.etk2000.checkstyle;

import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Works out which comments a rewrite cannot leave where they are, and what single trailing comment
 * they become. Shared by the fixers that re-emit a multi-line span on fewer lines than it occupied:
 * every comment inside such a span either travels with text the rewrite copies verbatim, or has to
 * be moved, because a {@code //} comment on a joined line would comment out the code after it.
 *
 * <p>Public because {@code com.etk2000.checkstyle.gradle.fix} consumes the result, and sited beside
 * {@link AstSpan} and {@link JavaLineScanner} because it needs both.
 */
public final class SpanComments {
	/**
	 * The index just past the comment starting at {@code at} in {@code text}, or {@code text.length()}
	 * when it runs to the end of the line (a {@code //} comment, or a block comment still open there).
	 */
	@CheckReturnValue
	private static int commentEnd(@Nonnull String text, int at, @Nonnull LexerState state) {
		if (state.inBlockComment()) {
			final var close = JavaLineScanner.multilineLiteralCloseIndex(text, state);
			return close < 0 ? text.length() : close;
		}
		if (text.startsWith("//", at))
			return text.length();
		final var close = text.indexOf("*/", at + 2);
		return close < 0 ? text.length() : close + 2;
	}

	@CheckReturnValue
	private static boolean insideAny(@Nonnull List<TextSpan> regions, int line, int index) {
		for (var region : regions) {
			final var afterStart = line > region.start().line()
					|| (line == region.start().line() && index >= region.start().index());
			final var beforeEnd = line < region.end().line()
					|| (line == region.end().line() && index < region.end().index());
			if (afterStart && beforeEnd)
				return true;
		}
		return false;
	}

	/**
	 * Joins the comments relocated out of a rewritten span into a single trailing comment. Each
	 * comment's delimiters and surrounding whitespace are stripped and the non-empty remainders joined
	 * with a single space under one leading {@code // }. Content-less comments are dropped; an empty
	 * string is returned when nothing survives.
	 */
	@CheckReturnValue
	@Nonnull
	private static String merge(@Nonnull List<String> comments) {
		final var texts = new ArrayList<String>();
		for (var comment : comments) {
			var text = comment.strip();
			if (text.startsWith("//"))
				text = text.substring(2);
			else {
				if (text.startsWith("/*"))
					text = text.substring(2);
				if (text.endsWith("*/"))
					text = text.substring(0, text.length() - 2);
				while (text.startsWith("*"))
					text = text.substring(1);
			}
			text = text.strip();
			if (!text.isEmpty())
				texts.add(text);
		}
		return texts.isEmpty() ? "" : "// " + String.join(" ", texts);
	}

	/**
	 * The one trailing comment carrying everything inside {@code span} that the rewrite cannot leave in
	 * place, in source order, or an empty string when nothing has to move. Every comment inside the
	 * span moves, except a block comment written inside one of the {@code kept} regions, which travels
	 * with the text copied from it. A {@code //} comment always moves, even out of a kept region, since
	 * anything re-emitted after it on the same line would be commented out.
	 */
	@CheckReturnValue
	@Nonnull
	public static String relocate(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull List<TextSpan> kept
	) {
		final var found = new ArrayList<String>();
		var state = LexerState.NONE;
		for (var i = span.start().line(); i <= span.end().line(); ++i) {
			final var line = lines.get(i);
			final var from = i == span.start().line() ? span.start().index() : 0;
			final var to = i == span.end().line() ? span.end().index() : line.length();
			final var text = line.substring(from, to);
			var scanState = state;
			var scan = 0;
			while (scan < text.length()) {
				final var marker = JavaLineScanner.firstCommentMarker(text.substring(scan), scanState);
				if (marker < 0)
					break;
				final var at = scan + marker;
				final var close = commentEnd(text, at, scanState);
				final var comment = text.substring(at, close);
				if (comment.startsWith("//") || !insideAny(kept, i, from + at))
					found.add(comment);
				if (close >= text.length())
					break;
				// a closed comment leaves the lexer where it started, and `scan` never lands in a literal
				scanState = LexerState.NONE;
				scan = Math.max(close, scan + 1);
			}
			state = JavaLineScanner.stateAfter(text, state);
		}
		return merge(found);
	}

	private SpanComments() {
	}
}