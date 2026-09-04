package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;

import org.junit.jupiter.api.Test;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Direct tests for {@link SpanRewrite}, whose span arrives from a check's locator in production and
 * so is always well formed there. The refusals below are reachable only from a caller that builds
 * one by hand, which is what a fixer driven past the pipeline's own bounds check does.
 *
 * <p>Spans are located by searching the line for the text they cover rather than by counting
 * characters, so a case reads as the construct it means rather than as a pair of indices.
 */
public class SpanRewriteTest {
	/** The span of the first occurrence of {@code text} in the single-line buffer {@code line}. */
	@Nonnull
	private static TextSpan spanOf(@Nonnull String line, @Nonnull String text) {
		final var start = line.indexOf(text);
		return new TextSpan(new TextPos(0, start), new TextPos(0, start + text.length()));
	}

	/** The idempotence path: a second violation on a line the first fix already collapsed. */
	@Test
	public void alreadyCollapsedListIsANoOp() {
		final var line = "\tf((x, y) -> x);";
		assertNull(SpanRewrite.replacing(List.of(line), spanOf(line, "(x, y)"), "(x, y)"));
	}

	/** The pair with {@link #supplementaryCharAheadOfTheSpanDoesNotShiftIt}. */
	@Test
	public void asciiTwinOfTheSupplementaryCase() {
		final var line = "x one two";
		assertEquals(
				new FixResult(0, 0, List.of("x one TWO")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "two"), "TWO")
		);
	}

	@Test
	public void backwardsSpanAcrossLinesReturnsNull() {
		final var span = new TextSpan(new TextPos(1, 0), new TextPos(0, 0));
		assertNull(SpanRewrite.replacing(List.of("ab", "cd"), span, "x"));
	}

	/** Not merely refused but refused rather than built from an overlapping prefix and suffix. */
	@Test
	public void backwardsSpanOnOneLineReturnsNull() {
		final var span = new TextSpan(new TextPos(0, 5), new TextPos(0, 2));
		assertNull(SpanRewrite.replacing(List.of("abcdef"), span, "x"));
	}

	/** Folding the comment's line away would comment out everything emitted after it. */
	@Test
	public void commentInsideAMultiLineSpanReturnsNull() {
		final var lines = List.of("\tf((String x,", "\t// note", "\tString y) -> x);");
		final var span = new TextSpan(new TextPos(0, 3), new TextPos(2, 10));
		assertNull(SpanRewrite.replacing(lines, span, "(x, y)"));
	}

	/** A single-line span needs no folding, so a comment inside it is replaced like any other text. */
	@Test
	public void commentInsideASingleLineSpanIsReplaced() {
		final var line = "\tf((/*c*/ String x) -> x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tf(x -> x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "(/*c*/ String x)"), "x")
		);
	}

	@Test
	public void emittedCarriageReturnReturnsNull() {
		final var line = "\tf(x -> x);";
		assertNull(SpanRewrite.replacing(List.of(line), spanOf(line, "x"), "a\rb"));
	}

	@Test
	public void emittedNewlineReturnsNull() {
		final var line = "\tf(x -> x);";
		assertNull(SpanRewrite.replacing(List.of(line), spanOf(line, "x"), "a\nb"));
	}

	@Test
	public void emptySpanWithEmptyEmitReturnsNull() {
		final var span = new TextSpan(new TextPos(0, 1), new TextPos(0, 1));
		assertNull(SpanRewrite.replacing(List.of("\tint x;"), span, ""));
	}

	@Test
	public void emptySpanWithNonEmptyEmitInserts() {
		final var span = new TextSpan(new TextPos(0, 1), new TextPos(0, 1));
		assertEquals(
				new FixResult(0, 0, List.of("\tfinal int x;")),
				SpanRewrite.replacing(List.of("\tint x;"), span, "final ")
		);
	}

	@Test
	public void multiLineSpanCollapsesToOneLine() {
		final var lines = List.of("\tf((String x,", "\tString y) -> x);");
		final var span = new TextSpan(new TextPos(0, 3), new TextPos(1, 10));
		assertEquals(
				new FixResult(0, 1, List.of("\tf((x, y) -> x);")),
				SpanRewrite.replacing(lines, span, "(x, y)")
		);
	}

	/** Folding several lines into one always sheds lines, so the no-op guard cannot fire there. */
	@Test
	public void multiLineSpanIsNeverANoOp() {
		final var lines = List.of("\tf((String x,", "\tString y) -> x);");
		final var span = new TextSpan(new TextPos(0, 3), new TextPos(1, 10));
		assertNotNull(SpanRewrite.replacing(lines, span, "(String x, String y)"));
	}

	@Test
	public void noOpEmitReturnsNull() {
		final var line = "\tf(x -> x);";
		assertNull(SpanRewrite.replacing(List.of(line), spanOf(line, "x"), "x"));
	}

	@Test
	public void oneCharacterDifferenceReturnsFixResult() {
		final var line = "\tf(x -> x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tf(y -> x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "x"), "y")
		);
	}

	/**
	 * Nothing is folded for a single-line span, but replacing text that opened a text block still
	 * orphans the lines carrying the rest of it, so the same refusal applies.
	 */
	@Test
	public void singleLineSpanOpeningATextBlockReturnsNull() {
		final var lines = List.of("\tfoo(\"\"\"", "\tbody", "\t\"\"\");");
		final var span = new TextSpan(new TextPos(0, 4), new TextPos(0, 8));
		assertNull(SpanRewrite.replacing(lines, span, "()"));
	}

	/** The pair with {@link #tabIndentedPrefix}: both are char arithmetic and must agree. */
	@Test
	public void spaceIndentedTwin() {
		final var line = "        f((String x) -> x);";
		assertEquals(
				new FixResult(0, 0, List.of("        f(x -> x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "(String x)"), "x")
		);
	}

	@Test
	public void spanEndingAtEndOfLineHasEmptySuffix() {
		final var line = "\tint a = f(x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tint a = g();")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "f(x);"), "g();")
		);
	}

	@Test
	public void spanEndLinePastBufferReturnsNull() {
		final var span = new TextSpan(new TextPos(0, 0), new TextPos(1, 0));
		assertNull(SpanRewrite.replacing(List.of("ab"), span, "x"));
	}

	/** The boundary partner of {@link #spanIndexPastLineEndReturnsNull}. */
	@Test
	public void spanIndexAtLineEndReturnsFixResult() {
		assertEquals(
				new FixResult(0, 0, List.of("cd")),
				SpanRewrite.replacing(List.of("ab"), spanOf("ab", "ab"), "cd")
		);
	}

	@Test
	public void spanIndexPastLineEndReturnsNull() {
		final var span = new TextSpan(new TextPos(0, 0), new TextPos(0, 3));
		assertNull(SpanRewrite.replacing(List.of("ab"), span, "x"));
	}

	@Test
	public void spanOpeningItsLineKeepsTheIndentAsPrefix() {
		final var line = "\tf(x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tg(x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "f"), "g")
		);
	}

	@Test
	public void spanStartIndexNegativeReturnsNull() {
		final var span = new TextSpan(new TextPos(0, -1), new TextPos(0, 1));
		assertNull(SpanRewrite.replacing(List.of("ab"), span, "x"));
	}

	@Test
	public void spanStartLineNegativeReturnsNull() {
		final var span = new TextSpan(new TextPos(-1, 0), new TextPos(0, 1));
		assertNull(SpanRewrite.replacing(List.of("ab"), span, "x"));
	}

	@Test
	public void spanWithTextAheadKeepsThatTextVerbatim() {
		final var line = "\tint a = f(x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tint a = g();")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "f(x)"), "g()")
		);
	}

	/** A span is located by char index, so a surrogate pair ahead of it must not shift it. */
	@Test
	public void supplementaryCharAheadOfTheSpanDoesNotShiftIt() {
		final var pair = new String(Character.toChars(0x1D465));
		final var line = pair + " one two";
		assertEquals(
				new FixResult(0, 0, List.of(pair + " one TWO")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "two"), "TWO")
		);
	}

	@Test
	public void tabIndentedPrefix() {
		final var line = "\tf((String x) -> x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tf(x -> x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "(String x)"), "x")
		);
	}

	/**
	 * The companion to {@link #textBlockOpenedButNotClosedByTheSpanReturnsNull}: when the literal is
	 * carried INTO the span's last line the slice refuses it on its own, so the two text-block
	 * shapes are stopped by two different mechanisms and both need their own case.
	 */
	@Test
	public void textBlockCarriedIntoTheSpansLastLineIsRefusedByTheSlice() {
		final var lines = List.of("foo(\"\"\"", "body\"\"\");");
		final var span = new TextSpan(new TextPos(0, 0), new TextPos(1, 9));
		assertNull(AstSpan.sliceSpan(lines, span.start(), span.end()), "the slice refuses this one itself");
		assertNull(SpanRewrite.replacing(lines, span, "X"));
	}

	/** Folding strips the line terminator a text block's opening delimiter requires. */
	@Test
	public void textBlockInsideAMultiLineSpanReturnsNull() {
		final var lines = List.of("\tf(\"\"\"", "\tbody\"\"\", x);");
		final var span = new TextSpan(new TextPos(0, 3), new TextPos(1, 12));
		assertNull(SpanRewrite.replacing(lines, span, "\"body\""));
	}

	/**
	 * A span that opens a text block and ends before closing it. The lines carrying the rest of the
	 * literal sit below the span, so folding its opener onto one line orphans them. The joined slice
	 * alone does not catch this: it tests the state carried INTO each line and discards the state the
	 * last line produces, so it answers {@code foo("""} rather than refusing.
	 */
	@Test
	public void textBlockOpenedButNotClosedByTheSpanReturnsNull() {
		final var lines = List.of("foo(", "\"\"\"", "content", "\"\"\";");
		final var span = new TextSpan(new TextPos(0, 0), new TextPos(1, 3));
		assertNotNull(AstSpan.sliceSpan(lines, span.start(), span.end()), "the slice itself does not refuse it");
		assertNull(SpanRewrite.replacing(lines, span, "X"));
	}

	@Test
	public void trailingCommentAfterTheSpanSurvives() {
		final var line = "\tf((String x) -> x); // note";
		assertEquals(
				new FixResult(0, 0, List.of("\tf(x -> x); // note")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "(String x)"), "x")
		);
	}

	/** The no-op guard is a byte comparison, not a normalisation. */
	@Test
	public void trailingSpaceCountsAsADifference() {
		final var line = "\tf(x -> x);";
		assertEquals(
				new FixResult(0, 0, List.of("\tf(x  -> x);")),
				SpanRewrite.replacing(List.of(line), spanOf(line, "x"), "x ")
		);
	}
}