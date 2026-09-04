package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Direct tests for the {@link SpanReformat} primitives the rest of the project shares.
 * {@code joinsTight} decides where a space goes when a multi-line construct is rejoined, and its
 * character sets read like omissions ({@code &#123;} and a leading {@code (} are loose) that a future
 * tidy-up would "fix"; {@code lexerStateAt} is the one place the carried comment/text-block state is
 * folded, and its out-of-range behaviour is load-bearing for callers that ask about the position one
 * past a span's last line.
 */
public class SpanReformatTest {
	private static final List<String> SPAN_OPENING_ON_TEXT_BLOCK_CLOSE = List.of("String s = \"\"\"", "text", "\"\"\".formatted(a,", "b);");
	private static final String ESCAPED_QUOTE = "\\" + "u0022";

	@Test
	public void beginsInMultilineLiteralByLineFlagsEveryLineWithEscapedDelimiter() {
		assertArrayEquals(
				new boolean[] {true, true},
				SpanReformat.beginsInMultilineLiteralByLine(List.of("x(" + ESCAPED_QUOTE + ",", "y);"), 0, 1)
		);
	}

	@Test
	public void beginsInMultilineLiteralByLineSeedsFromSpanEntryState() {
		assertArrayEquals(new boolean[] {true, false}, SpanReformat.beginsInMultilineLiteralByLine(SPAN_OPENING_ON_TEXT_BLOCK_CLOSE, 2, 3));
	}

	@Test
	public void beginsInMultilineLiteralEscapedDelimiter() {
		assertTrue(SpanReformat.beginsInMultilineLiteral(List.of("x(" + ESCAPED_QUOTE + ",", "y);"), 0, 1));
	}

	@Test
	public void beginsInMultilineLiteralSeedsFromSpanEntryState() {
		assertFalse(SpanReformat.beginsInMultilineLiteral(SPAN_OPENING_ON_TEXT_BLOCK_CLOSE, 2, 3));
	}

	@Test
	public void beginsInTextBlockEscapedDelimiter() {
		assertTrue(SpanReformat.beginsInTextBlock(List.of("x(" + ESCAPED_QUOTE + ",", "y);"), 0, 1));
	}

	@Test
	public void beginsInTextBlockSeedsFromSpanEntryState() {
		assertFalse(SpanReformat.beginsInTextBlock(SPAN_OPENING_ON_TEXT_BLOCK_CLOSE, 2, 3));
	}

	@Test
	public void firstLineIndentInsideBlockCommentKeepsRawWhitespace() {
		assertEquals("\t\t   ", SpanReformat.firstLineIndent(List.of("\t\t/* off", "\t\t   it was flaky */ run(a,"), 1));
	}

	@Test
	public void firstLineIndentInsideTextBlockKeepsRawWhitespace() {
		assertEquals("\t\t  ", SpanReformat.firstLineIndent(List.of("s = \"\"\"", "text", "\t\t  \"\"\".formatted(a,"), 2));
	}

	@Test
	public void firstLineIndentOutsideLiteralKeepsOnlyTabs() {
		assertEquals("\t\t", SpanReformat.firstLineIndent(List.of("x();", "\t\t  run(a,"), 1));
	}

	@Test
	public void hasEscapedDelimiterClampsRangeToBuffer() {
		assertTrue(SpanReformat.hasEscapedDelimiter(List.of("a", ESCAPED_QUOTE), -5, 99));
	}

	@Test
	public void hasEscapedDelimiterEvenBackslashRunIsNotAnEscape() {
		assertFalse(SpanReformat.hasEscapedDelimiter(List.of("Pattern.compile(\"\\" + "\\" + "u0022\")"), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterIgnoresLinesOutsideRange() {
		assertFalse(SpanReformat.hasEscapedDelimiter(List.of(ESCAPED_QUOTE, "a", ESCAPED_QUOTE), 1, 1));
	}

	@Test
	public void hasEscapedDelimiterIgnoresNeighborsOfEachDelimiter() {
		for (var hex : List.of("0009", "000B", "000C", "000E", "0021", "0023", "0026", "0028", "0029", "002B", "002E", "0030", "005B", "005D"))
			assertFalse(SpanReformat.hasEscapedDelimiter(List.of("\\" + "u" + hex), 0, 0), hex);
	}

	@Test
	public void hasEscapedDelimiterIgnoresOtherEscapes() {
		assertFalse(SpanReformat.hasEscapedDelimiter(List.of("\"" + "\\" + "u0041" + "\""), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterMultipleUs() {
		assertTrue(SpanReformat.hasEscapedDelimiter(List.of("\\" + "uuu002F"), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterOddBackslashRunIsAnEscape() {
		assertTrue(SpanReformat.hasEscapedDelimiter(List.of("\"\\" + "\\" + "\\" + "u0022\""), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterOddRunOfFiveIsAnEscape() {
		assertTrue(SpanReformat.hasEscapedDelimiter(List.of("\"" + "\\".repeat(5) + "u0022\""), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterQuote() {
		assertTrue(SpanReformat.hasEscapedDelimiter(List.of(ESCAPED_QUOTE), 0, 0));
	}

	@Test
	public void hasEscapedDelimiterSlashAndStarEitherCase() {
		for (var hex : List.of("002A", "002a", "002F", "002f"))
			assertTrue(SpanReformat.hasEscapedDelimiter(List.of("\\" + "u" + hex), 0, 0), hex);
	}

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

	@Test
	public void lexerStateAtEmptyListPastSizeIsTotal() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of(), 3));
	}

	@Test
	public void lexerStateAtFirstLineIsNone() {
		assertEquals(LexerState.NONE, SpanReformat.lexerStateAt(List.of("/* a", "b"), 0));
	}

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

	@Test
	public void lexerStateAtIndexPastSizeIsTotal() {
		assertEquals(new LexerState(true, false), SpanReformat.lexerStateAt(List.of("/* a", "b"), 5));
	}

	/**
	 * Read cold, a line that is only a text-block closer looks like an opener; the pair with
	 * {@link #lexerStateAtThreadedTextBlockCloses} is what distinguishes the two readings.
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