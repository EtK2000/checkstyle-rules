package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LineTextTest {
	@Test
	public void testCharIndexOfColumnBeyondEnd() {
		assertEquals(-1, LineText.charIndexOfColumn("ab", 3));
	}

	@Test
	public void testCharIndexOfColumnLineEnd() {
		assertEquals(2, LineText.charIndexOfColumn("ab", 2));
	}

	@Test
	public void testCharIndexOfColumnNegative() {
		assertEquals(-1, LineText.charIndexOfColumn("ab", -1));
	}

	/**
	 * A column past a supplementary character is a code-point index, so it must map
	 * two chars further along than its own value.
	 */
	@Test
	public void testCharIndexOfColumnPastSupplementary() {
		assertEquals(3, LineText.charIndexOfColumn("x𝐀c", 2));
	}

	@Test
	public void testCharIndexOfColumnPlainAscii() {
		assertEquals(2, LineText.charIndexOfColumn("abc", 2));
	}

	@Test
	public void testCharIndexOfColumnZero() {
		assertEquals(0, LineText.charIndexOfColumn("ab", 0));
	}

	@Test
	public void testExtractIndentEmpty() {
		assertEquals("", LineText.extractIndent(""));
	}

	@Test
	public void testExtractIndentMixedTabSpace() {
		assertEquals("\t  ", LineText.extractIndent("\t  foo"));
	}

	@Test
	public void testExtractIndentNoIndent() {
		assertEquals("", LineText.extractIndent("foo"));
	}

	@Test
	public void testExtractIndentNonTabSpaceWhitespace() {
		assertEquals("", LineText.extractIndent("\ffoo"));
	}

	@Test
	public void testExtractIndentSpaces() {
		assertEquals("    ", LineText.extractIndent("    foo"));
	}

	@Test
	public void testExtractIndentTab() {
		assertEquals("\t", LineText.extractIndent("\tfoo"));
	}

	@Test
	public void testExtractIndentWholeLineIndent() {
		assertEquals("\t ", LineText.extractIndent("\t "));
	}

	@Test
	public void testIdentEndBasicRunStopsAtTerminator() {
		assertEquals(2, LineText.identEnd("ab;", 0));
	}

	@Test
	public void testIdentEndDigitInRun() {
		assertEquals(3, LineText.identEnd("a1b;", 0));
	}

	@Test
	public void testIdentEndDollarInRun() {
		assertEquals(3, LineText.identEnd("a$b.", 0));
	}

	@Test
	public void testIdentEndFloatDotTerminates() {
		assertEquals(1, LineText.identEnd("3.14;", 0));
	}

	@Test
	public void testIdentEndLeadingDigitNoStartValidation() {
		assertEquals(3, LineText.identEnd("1ab;", 0));
	}

	@Test
	public void testIdentEndMatchesFinalLocalVariableTokenEnd() {
		assertEquals(3, LineText.identEnd("int x;", 0));
	}

	@Test
	public void testIdentEndMatchesPreferCollectionInterfaceCallSite() {
		assertEquals(9, LineText.identEnd("ArrayList<String> x;", 0));
	}

	@Test
	public void testIdentEndMatchesPreferPrefixIncrementCase1() {
		assertEquals(1, LineText.identEnd("i++;", 0));
	}

	@Test
	public void testIdentEndQuoteTerminatesNoLiteralAwareness() {
		assertEquals(2, LineText.identEnd("ab\"cd", 0));
	}

	@Test
	public void testIdentEndRunReachesEol() {
		assertEquals(2, LineText.identEnd("ab", 0));
	}

	@Test
	public void testIdentEndStartAtNonPartReturnsStart() {
		assertEquals(0, LineText.identEnd(";ab", 0));
	}

	@Test
	public void testIdentEndStartEqualsLength() {
		assertEquals(2, LineText.identEnd("ab", 2));
	}

	@Test
	public void testIdentEndStartMidIdentifier() {
		assertEquals(3, LineText.identEnd("foo.bar", 1));
	}

	@Test
	public void testIdentEndSupplementaryFollowedByColon() {
		assertEquals(6, LineText.identEnd("loop\uD835\uDC00:", 0));
	}

	@Test
	public void testIdentEndSupplementaryRunWalksWholePair() {
		assertEquals(3, LineText.identEnd("\uD835\uDC00a;", 0));
	}

	@Test
	public void testIdentEndUnderscoreRun() {
		assertEquals(2, LineText.identEnd("_x ", 0));
	}

	@Test
	public void testIdentEndUnicodeIdentifierPart() {
		assertEquals(4, LineText.identEnd("café;", 0));
	}

	@Test
	public void testIdentStartBasicRunFromTerminator() {
		assertEquals(1, LineText.identStart(".ab", 3));
	}

	@Test
	public void testIdentStartDigitInRun() {
		assertEquals(1, LineText.identStart(".a1", 3));
	}

	@Test
	public void testIdentStartDollarBeforeDotNotInRun() {
		assertEquals(2, LineText.identStart("$.a", 3));
	}

	@Test
	public void testIdentStartDollarInRun() {
		assertEquals(0, LineText.identStart("a$b", 3));
	}

	@Test
	public void testIdentStartLeadingDigitNoStartValidation() {
		assertEquals(1, LineText.identStart(".1a", 3));
	}

	@Test
	public void testIdentStartMatchesPreferPrefixIncrementCase2() {
		assertEquals(0, LineText.identStart("count++;", 5));
	}

	@Test
	public void testIdentStartPosEqualsLength() {
		assertEquals(2, LineText.identStart("a.foo", 5));
	}

	@Test
	public void testIdentStartPosOneCharIsPart() {
		assertEquals(0, LineText.identStart("ab", 1));
	}

	@Test
	public void testIdentStartPosOneCharNotPart() {
		assertEquals(1, LineText.identStart(".b", 1));
	}

	@Test
	public void testIdentStartPosZeroReturnsZero() {
		assertEquals(0, LineText.identStart("ab", 0));
	}

	@Test
	public void testIdentStartPrevCharNotPartReturnsPos() {
		assertEquals(3, LineText.identStart("ab.", 3));
	}

	@Test
	public void testIdentStartRunHitsIndexZero() {
		assertEquals(0, LineText.identStart("ab", 2));
	}

	@Test
	public void testIdentStartSupplementaryRunWalksWholePair() {
		assertEquals(0, LineText.identStart("a\uD835\uDC00", 3));
	}

	@Test
	public void testIdentStartSupplementaryTerminatorStopsRun() {
		assertEquals(1, LineText.identStart(".\uD835\uDC00x", 4));
	}

	@Test
	public void testIdentStartUnderscoreInRun() {
		assertEquals(1, LineText.identStart("._a", 3));
	}

	@Test
	public void testIdentStartUnicodeIdentifierPart() {
		assertEquals(1, LineText.identStart(".café", 5));
	}

	@Test
	public void testIndexOfWordAbsent() {
		assertEquals(-1, LineText.indexOfWord("} foo();", "while"));
	}

	@Test
	public void testIndexOfWordFound() {
		assertEquals(2, LineText.indexOfWord("} while (x);", "while"));
	}

	/**
	 * The first textual hit is inside a longer identifier, so the scan has to keep
	 * looking rather than reporting it.
	 */
	@Test
	public void testIndexOfWordSkipsLongerIdentifier() {
		assertEquals(15, LineText.indexOfWord("} while_loop() while (x);", "while"));
	}

	@Test
	public void testIndexOfWordSkipsPrefixedIdentifier() {
		assertEquals(-1, LineText.indexOfWord("} dowhile();", "while"));
	}

	@Test
	public void testIndexOfWordSkipsSuffixedIdentifier() {
		assertEquals(-1, LineText.indexOfWord("} while_loop();", "while"));
	}

	@Test
	public void testIsEscapedEvenRun() {
		assertFalse(LineText.isEscaped("\\\\\"", 2));
	}

	@Test
	public void testIsEscapedNoBackslash() {
		assertFalse(LineText.isEscaped("a\"b", 1));
	}

	@Test
	public void testIsEscapedOddRun() {
		assertTrue(LineText.isEscaped("\\\"", 1));
	}

	@Test
	public void testIsEscapedPosBeyondLength() {
		assertFalse(LineText.isEscaped("ab", 5));
	}

	@Test
	public void testIsEscapedPosZero() {
		assertFalse(LineText.isEscaped("\\\"", 0));
	}

	@Test
	public void testIsEscapedThreeBackslashes() {
		assertTrue(LineText.isEscaped("\\\\\\\"", 3));
	}

	@Test
	public void testIsWordAtFollowedByIdentifierChar() {
		assertFalse(LineText.isWordAt("doWork();", 0, "do"));
	}

	@Test
	public void testIsWordAtFollowedBySupplementaryChar() {
		assertFalse(LineText.isWordAt("do\uD835\uDC00();", 0, "do"));
	}

	@Test
	public void testIsWordAtIndexPastEndReturnsFalse() {
		assertFalse(LineText.isWordAt("do", 5, "do"));
	}

	@Test
	public void testIsWordAtPrecededByIdentifierChar() {
		assertFalse(LineText.isWordAt("undo();", 2, "do"));
	}

	@Test
	public void testIsWordAtPrecededByPunctuation() {
		assertTrue(LineText.isWordAt("x=do;", 2, "do"));
	}

	@Test
	public void testIsWordAtPrecededBySupplementaryChar() {
		assertFalse(LineText.isWordAt("\uD835\uDC00do();", 2, "do"));
	}

	@Test
	public void testIsWordAtWholeToken() {
		assertTrue(LineText.isWordAt("do x;", 0, "do"));
	}

	@Test
	public void testLiteralTokenEndCloseParenTerminates() {
		assertEquals(1, LineText.literalTokenEnd("5)", 0));
	}

	@Test
	public void testLiteralTokenEndDecimalPointConsumed() {
		assertEquals(4, LineText.literalTokenEnd("3.14;", 0));
	}

	@Test
	public void testLiteralTokenEndExponentSignTerminates() {
		assertEquals(4, LineText.literalTokenEnd("1.0E-30f", 0));
	}

	@Test
	public void testLiteralTokenEndHexLettersConsumed() {
		assertEquals(5, LineText.literalTokenEnd("0xFFL,", 0));
	}

	@Test
	public void testLiteralTokenEndMatchesRedundantNumericSuffixCallSite() {
		assertEquals(4, LineText.literalTokenEnd("100L", 0));
	}

	@Test
	public void testLiteralTokenEndMatchesUpperEllCallSite() {
		assertEquals(2, LineText.literalTokenEnd("5l;", 0));
	}

	@Test
	public void testLiteralTokenEndQuoteTerminatesNoLiteralAwareness() {
		assertEquals(1, LineText.literalTokenEnd("5\"x", 0));
	}

	@Test
	public void testLiteralTokenEndRunReachesEol() {
		assertEquals(3, LineText.literalTokenEnd("100", 0));
	}

	@Test
	public void testLiteralTokenEndSpaceTerminates() {
		assertEquals(1, LineText.literalTokenEnd("5 ", 0));
	}

	@Test
	public void testLiteralTokenEndStartAtNonLiteralReturnsStart() {
		assertEquals(0, LineText.literalTokenEnd(";5", 0));
	}

	@Test
	public void testLiteralTokenEndStartEqualsLength() {
		assertEquals(3, LineText.literalTokenEnd("100", 3));
	}

	@Test
	public void testLiteralTokenEndStartMidToken() {
		assertEquals(4, LineText.literalTokenEnd("3.14;", 2));
	}

	@Test
	public void testLiteralTokenEndStopsAtTerminator() {
		assertEquals(1, LineText.literalTokenEnd("5;", 0));
	}

	@Test
	public void testLiteralTokenEndUnderscoreConsumed() {
		assertEquals(5, LineText.literalTokenEnd("1_000;", 0));
	}

	@Test
	public void testLiteralTokenEndUnicodeLetterConsumed() {
		assertEquals(4, LineText.literalTokenEnd("café;", 0));
	}

	@Test
	public void testQualifiedNameEndDotContinuesRun() {
		assertEquals(14, LineText.qualifiedNameEnd("java.util.List<String>", 0));
	}

	@Test
	public void testQualifiedNameEndMatchesPreferVarNewTypeCallSite() {
		assertEquals(3, LineText.qualifiedNameEnd("Foo()", 0));
	}

	@Test
	public void testQualifiedNameEndRunReachesEol() {
		assertEquals(3, LineText.qualifiedNameEnd("a.b", 0));
	}

	@Test
	public void testQualifiedNameEndStartAtNonPartReturnsStart() {
		assertEquals(0, LineText.qualifiedNameEnd("<ab", 0));
	}

	@Test
	public void testQualifiedNameEndStartEqualsLength() {
		assertEquals(2, LineText.qualifiedNameEnd("ab", 2));
	}

	@Test
	public void testQualifiedNameEndSupplementaryRunWalksWholePair() {
		assertEquals(5, LineText.qualifiedNameEnd("Fo\uD835\uDC00o<", 0));
	}

	@Test
	public void testQualifiedNameEndTrailingDotConsumed() {
		assertEquals(2, LineText.qualifiedNameEnd("a. b", 0));
	}

	@Test
	public void testQualifiedNameStartDotContinuesRun() {
		assertEquals(4, LineText.qualifiedNameStart("new java.util.List", 18));
	}

	@Test
	public void testQualifiedNameStartPosZeroReturnsZero() {
		assertEquals(0, LineText.qualifiedNameStart("ab", 0));
	}

	@Test
	public void testQualifiedNameStartPrevCharNotPartReturnsPos() {
		assertEquals(3, LineText.qualifiedNameStart("ab<", 3));
	}

	@Test
	public void testQualifiedNameStartRunHitsIndexZero() {
		assertEquals(0, LineText.qualifiedNameStart("a.b", 3));
	}

	@Test
	public void testQualifiedNameStartStopsAtOpenParen() {
		assertEquals(1, LineText.qualifiedNameStart("(a.b", 4));
	}

	@Test
	public void testQualifiedNameStartSupplementaryRunWalksWholePair() {
		assertEquals(1, LineText.qualifiedNameStart("(\uD835\uDC00.x", 5));
	}

	@Test
	public void testStartsWithSeparatedWordFollowedByOperator() {
		assertFalse(LineText.startsWithSeparatedWord("var++;", "var"));
	}

	@Test
	public void testStartsWithSeparatedWordSpace() {
		assertTrue(LineText.startsWithSeparatedWord("var x = 5;", "var"));
	}

	@Test
	public void testStartsWithSeparatedWordWordOnly() {
		assertFalse(LineText.startsWithSeparatedWord("var", "var"));
	}

	@Test
	public void testStartsWithWordExact() {
		assertTrue(LineText.startsWithWord("do", "do"));
	}

	@Test
	public void testStartsWithWordFollowedBySupplementaryChar() {
		assertFalse(LineText.startsWithWord("do\uD835\uDC00", "do"));
	}

	@Test
	public void testStartsWithWordPrefixOfLongerIdentifier() {
		assertFalse(LineText.startsWithWord("doWork();", "do"));
	}
}