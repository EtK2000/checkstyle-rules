package com.etk2000.checkstyle;

import com.etk2000.checkstyle.format.SpanReformat;
import com.etk2000.checkstyle.gradle.fix.LineLength;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Asks whether a span of source lines would still fit on one line once joined, measured against
 * {@link LineLength#MAX_LINE_LENGTH}. The width is taken from the raw text, because that is what the
 * joiners emit: masking comments made a comment inside the span cost nothing here and full width in
 * the output, so a span the gate accepted could come back over the limit. Line numbers are 1-based,
 * as they come off the AST.
 */
final class LineCollapse {
	@CheckReturnValue
	static boolean fitsOnOneLine(@Nonnull String[] lines, int fromLine, int toLine) {
		return fitsOnOneLine(lines, fromLine, toLine, false);
	}

	@CheckReturnValue
	static boolean fitsOnOneLine(@Nonnull String[] lines, int fromLine, int toLine, boolean tight) {
		if (fromLine < 1 || toLine > lines.length || fromLine > toLine)
			return false;
		var state = JavaLineScanner.LexerState.NONE;
		final var collapsed = new StringBuilder();
		for (var line = fromLine; line <= toLine; ++line) {
			final var raw = lines[line - 1];
			state = JavaLineScanner.stateAfter(raw, state);
			// a span crossing a text block can never become one physical line, and masking hides that
			// from the width measure, so every joiner refuses it and this gate must agree
			if (line < toLine && state.inTextBlock())
				return false;
			if (line == fromLine) {
				collapsed.append(raw.stripTrailing());
				continue;
			}
			final var piece = raw.strip();
			// tight joins around brackets/punctuation exactly as the fixer's collapse does, so the measured
			// width equals the fixer's one-line output at the max-width boundary; loose keeps the historical
			// space-join the postDelayed / collapsible-put gates were tuned against
			if (tight) {
				if (piece.isEmpty())
					continue;
				if (!collapsed.isEmpty() && !SpanReformat.joinsTight(collapsed, piece))
					collapsed.append(' ');
				collapsed.append(piece);
			}
			else {
				collapsed.append(' ');
				collapsed.append(piece);
			}
		}
		return LineLength.tabExpandedLength(collapsed.toString()) <= LineLength.MAX_LINE_LENGTH;
	}

	private LineCollapse() {
	}
}