package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Direct tests for the two {@link SpanReformat} primitives that the rest of the project shares but
 * never drove directly. {@code joinsTight} decides where a space goes when a multi-line construct is
 * rejoined, and its character sets read like omissions ({@code &#123;} and a leading {@code (} are
 * loose) that a future tidy-up would "fix"; {@code lexerStateAt} is the one place the carried
 * comment/text-block state is folded, and its out-of-range behaviour is load-bearing for callers
 * that ask about the position one past a span's last line.
 */
public class SpanReformatTest {
	@Test
	public void joinsLooseOnCharLiteralComma() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), "','"));
	}

	@Test
	public void joinsLooseOnEmptyContinuation() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), ""));
	}

	@Test
	public void joinsLooseOnEmptyJoined() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder(), "x"));
	}

	@Test
	public void joinsLooseOnLeadingCloseBrace() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), "}"));
	}

	@Test
	public void joinsLooseOnLeadingColon() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), ":b"));
	}

	/** The character the class javadoc names as the one a tidy-up would wrongly add to the tight set. */
	@Test
	public void joinsLooseOnLeadingOpenBrace() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), "{"));
	}

	@Test
	public void joinsLooseOnLeadingOpenBracket() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), "[i]"));
	}

	/**
	 * The asymmetry worth pinning: a trailing {@code (} is tight but a leading one is loose, so
	 * {@code f} + {@code (a)} rejoins as {@code f (a)}. Making the two symmetric is the plausible
	 * "fix" that would silently reflow every rejoined call.
	 */
	@Test
	public void joinsLooseOnLeadingOpenParen() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("f"), "(a)"));
	}

	@Test
	public void joinsLooseOnLeadingPlus() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a"), "+ b"));
	}

	@Test
	public void joinsLooseOnTrailingCloseBracket() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("a]"), "i"));
	}

	@Test
	public void joinsLooseOnTrailingCloseParen() {
		assertFalse(SpanReformat.joinsTight(new StringBuilder("f)"), "b"));
	}

	@Test
	public void joinsTightOnLeadingCloseBracket() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a"), "]"));
	}

	@Test
	public void joinsTightOnLeadingCloseParen() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a"), ")"));
	}

	@Test
	public void joinsTightOnLeadingComma() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a"), ", b"));
	}

	@Test
	public void joinsTightOnLeadingDot() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a"), ".b"));
	}

	@Test
	public void joinsTightOnLeadingSemicolon() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a"), ";"));
	}

	@Test
	public void joinsTightOnTrailingOpenBracket() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("a["), "i"));
	}

	@Test
	public void joinsTightOnTrailingOpenParen() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("f("), "b"));
	}

	/** Both arms answer tight here; the arms are pinned apart by the two single-arm rows above. */
	@Test
	public void joinsTightPrefersContinuationArm() {
		assertTrue(SpanReformat.joinsTight(new StringBuilder("f("), ".b"));
	}

	@Test
	public void lexerStateAtBlockMarkerInsideTextBlockDoesNotOpen() {
		assertEquals(new LexerState(false, true), SpanReformat.lexerStateAt(List.of("\"\"\" /* a", "b"), 1));
	}

	@Test
	public void lexerStateAtClosedBlockComment() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("x; /* a */", "b"), 1));
	}

	@Test
	public void lexerStateAtEmptyListIsNone() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of(), 0));
	}

	/** Degenerate form of {@link #lexerStateAtIndexPastSizeIsTotal}: the bound with no line to fold. */
	@Test
	public void lexerStateAtEmptyListPastSizeIsTotal() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of(), 3));
	}

	@Test
	public void lexerStateAtFirstLineIsNone() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("/* a", "b"), 0));
	}

	/**
	 * The boundary the callers asking about the line after a span actually hit, since
	 * {@code endLineIdx + 1} reaches exactly {@code size}. Fold depth at this bound is pinned by
	 * {@link #lexerStateAtIndexEqualToSizeFoldsLastLine}, whose last line is the one that opens.
	 */
	@Test
	public void lexerStateAtIndexEqualToSizeFoldsEveryLine() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("/* a", "b"), 2));
	}

	/** Only the last line opens anything, so an early stop at {@code size - 1} would answer NONE. */
	@Test
	public void lexerStateAtIndexEqualToSizeFoldsLastLine() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("f();", "x; /* a"), 2));
	}

	@Test
	public void lexerStateAtIndexEqualToSizeOnCleanBuffer() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("f();", "g();"), 2));
	}

	/** Past the end answers rather than throwing, which is what lets the three {@code +1} callers be total. */
	@Test
	public void lexerStateAtIndexPastSizeIsTotal() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("/* a", "b"), 5));
	}

	/**
	 * Read cold, a line that is only a text-block closer looks like an opener. Threading from the top
	 * of the buffer is the whole reason this primitive exists rather than a per-line scan; the pair
	 * with {@link #lexerStateAtThreadedTextBlockCloses} is what distinguishes the two readings.
	 */
	@Test
	public void lexerStateAtIsolatedTripleQuoteSemicolonReadsAsOpener() {
		assertEquals(new LexerState(false, true), SpanReformat.lexerStateAt(List.of("\"\"\";", "f();"), 1));
	}

	@Test
	public void lexerStateAtLineCommentDoesNotCarry() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("x; // /* y", "z"), 1));
	}

	@Test
	public void lexerStateAtNegativeIndexIsNone() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("/* a", "b"), -1));
	}

	@Test
	public void lexerStateAtOpenBlockComment() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("x; /* a", "b"), 1));
	}

	@Test
	public void lexerStateAtOpenTextBlock() {
		assertEquals(new LexerState(false, true), SpanReformat.lexerStateAt(List.of("var s = \"\"\"", "x"), 1));
	}

	@Test
	public void lexerStateAtThreadedTextBlockCloses() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("var s = \"\"\"", "txt", "\"\"\";", "f();"), 3));
	}

	@Test
	public void lexerStateAtTripleQuoteInsideBlockCommentDoesNotOpen() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("/* a \"\"\"", "b"), 1));
	}

	/** A string literal cannot span lines in valid Java, so an unterminated one carries nothing. */
	@Test
	public void lexerStateAtUnterminatedStringDoesNotCarry() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("s = \"abc", "x"), 1));
	}
}