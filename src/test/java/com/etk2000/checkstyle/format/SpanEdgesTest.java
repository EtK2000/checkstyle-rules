package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Direct tests for {@link SpanEdges}, which four call sites across two checks and their two fixers
 * share. All four reach it through a parsed tree, so its edges are otherwise only ever asserted
 * through rewritten fixture text: the {@code prefix}/{@code indent} split shows up there only when a
 * fixture happens to carry leading text, and the {@link SpanEdges#appendableComment} guard only when
 * one happens to carry both a relocated comment and an unterminated literal after the span. Neither
 * arm of that guard was distinguished by any fixture in either unit.
 *
 * <p>The {@link SpanEdges#of} cases use plain words indented with spaces rather than Java indented
 * with tabs, which is the point: the edges are char arithmetic over whatever {@code lines} holds, and
 * neither the content nor the indent character may change the answer. Only
 * {@link SpanEdges#appendableComment} reads its input as Java, and those cases spell real suffixes.
 */
public class SpanEdgesTest {
	@Test
	public void appendableCommentKeptAfterAClosedBlockComment() {
		assertEquals("// note", new SpanEdges("", "", "; /* done */").appendableComment("// note"));
	}

	@Test
	public void appendableCommentKeptAfterPlainCode() {
		assertEquals("// note", new SpanEdges("", "", "; next();").appendableComment("// note"));
	}

	@Test
	public void appendableCommentSuppressedAfterAnOpenBlockComment() {
		assertEquals("", new SpanEdges("", "", "; /* opens").appendableComment("// note"));
	}

	@Test
	public void appendableCommentSuppressedAfterAnOpenTextBlock() {
		assertEquals("", new SpanEdges("", "", "; var s = \"\"\"").appendableComment("// note"));
	}

	@Test
	public void indentIsTheLinesOwnWhenTextPrecedesTheSpan() {
		final var edges = SpanEdges.of(List.of("  one two three"), 0, 6, 0, 9);
		assertEquals("  one ", edges.prefix());
		assertEquals("  ", edges.indent());
		assertEquals(" three", edges.suffix());
	}

	@Test
	public void indentIsThePrefixWhenTheSpanOpensItsLine() {
		final var edges = SpanEdges.of(List.of("  one two"), 0, 2, 0, 5);
		assertEquals("  ", edges.prefix());
		assertEquals("  ", edges.indent());
		assertEquals(" two", edges.suffix());
	}

	@Test
	public void theSuffixComesFromTheEndLine() {
		final var edges = SpanEdges.of(List.of(" one", "  two three"), 0, 1, 1, 5);
		assertEquals(" ", edges.prefix());
		assertEquals(" ", edges.indent());
		assertEquals(" three", edges.suffix());
	}
}