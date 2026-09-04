package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Text-block guard shared by the blank-line fixers. Their checks are
 * {@code RegexpMultiline} modules attached directly to {@code Checker}, outside
 * {@code TreeWalker}, so they cannot be made lexically aware on the report side
 * and do fire on blank lines that are a text block's string content. Deleting or
 * inserting such a line changes a string value, so the fixers ask here before
 * touching a span and refuse when any of it is text-block content.
 *
 * <p>Only text blocks are guarded. A blank line inside a block comment carries no
 * value, and a string or char literal cannot span physical lines, so a text block
 * is the only construct whose value a whole-line edit can alter.
 */
final class TextBlockGuard {
	/**
	 * Whether any line in {@code [fromLine, toLine]} begins inside a text block.
	 * The {@link JavaLineScanner.LexerState} is threaded from the first line of
	 * {@code lines}, so the answer accounts for text blocks opened anywhere above
	 * the span. Indices outside the buffer are ignored; an empty or inverted range
	 * answers {@code false}.
	 */
	@CheckReturnValue
	static boolean containsTextBlockContent(@Nonnull List<String> lines, int fromLine, int toLine) {
		final var last = Math.min(toLine, lines.size() - 1);
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i <= last; ++i) {
			if (i >= fromLine && state.inTextBlock())
				return true;
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		}
		return false;
	}

	private TextBlockGuard() {
	}
}