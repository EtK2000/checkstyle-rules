package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * What a replaced span leaves behind on its own first and last lines: the text before it there, the
 * indentation that line is written at, and the text after it.
 *
 * <p>Pure text, like the rest of this package: positions arrive as char indices into {@code lines},
 * the same shape {@link SpanReformat#slice} takes, so nothing here needs a parsed tree. That is also
 * what keeps this out of a dependency cycle with the span types in {@code com.etk2000.checkstyle},
 * which already reach into this package.
 */
public record SpanEdges(@Nonnull String prefix, @Nonnull String indent, @Nonnull String suffix) {
	/**
	 * {@code comment} when a fixer may append it after {@code following}, else empty. Text leaving a
	 * block comment open swallows whatever follows, so the comment would be lost and a terminator
	 * inside it would close that comment early and turn the prose after it back into code. Text opening
	 * a text block is worse: only whitespace may follow the opening delimiter, so appending would not
	 * compile. Both are {@link LexerState#inMultilineLiteral}.
	 *
	 * <p>{@code following} is whatever the fixer will actually re-emit on the comment's line, which is
	 * not always {@link #suffix}.
	 */
	@CheckReturnValue
	@Nonnull
	public static String appendableAfter(@Nonnull String comment, @Nonnull String following) {
		return JavaLineScanner.stateAfter(following, LexerState.NONE).inMultilineLiteral() ? "" : comment;
	}

	/**
	 * The edges of the span running from {@code startCol} on {@code startIdx} to {@code endCol} on
	 * {@code endIdx}, both char indices into their own line.
	 */
	@CheckReturnValue
	@Nonnull
	public static SpanEdges of(
			@Nonnull List<String> lines, int startIdx, int startCol, int endIdx, int endCol
	) {
		final var startLine = lines.get(startIdx);
		final var prefix = startLine.substring(0, startCol);
		return new SpanEdges(
				prefix,
				// a span opening at its line's own indentation has nothing ahead of it to keep, so the
				// prefix is the indent; otherwise the emitted lines line up under the line's indent
				prefix.isBlank() ? prefix : LineText.extractIndent(startLine),
				lines.get(endIdx).substring(endCol)
		);
	}

	/** {@link #appendableAfter} for a fixer that re-emits the whole {@link #suffix} after the comment. */
	@CheckReturnValue
	@Nonnull
	public String appendableComment(@Nonnull String comment) {
		return appendableAfter(comment, suffix);
	}
}