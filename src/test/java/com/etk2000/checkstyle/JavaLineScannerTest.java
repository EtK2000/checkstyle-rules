package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.List;

import org.junit.jupiter.api.Test;

public class JavaLineScannerTest {
	@Test
	public void testFirstCommentMarkerBlockAfterCharLiteral() {
		final var line = "c = '/'; /* z */";
		assertEquals(line.indexOf("/*"), JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerBlockBeforeLineFirstWins() {
		final var line = "/* a */ // b";
		assertEquals(0, JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerBlockMidLine() {
		final var line = "a /* x */ b";
		assertEquals(2, JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerBlockStart() {
		assertEquals(0, JavaLineScanner.firstCommentMarker("/* x */ a", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerContinuedBlockCommentClosesThenCodeReturnsZero() {
		assertEquals(0, JavaLineScanner.firstCommentMarker("*/ x // y", new LexerState(true, false)));
	}

	@Test
	public void testFirstCommentMarkerContinuedBlockCommentReturnsZero() {
		assertEquals(0, JavaLineScanner.firstCommentMarker("still comment", new LexerState(true, false)));
	}

	@Test
	public void testFirstCommentMarkerDivisionOperatorNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = a / b;", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerEmptyLineNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerEscapedQuoteThenLineComment() {
		final var line = "s = \"a\\\"b\"; // c";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerLineBeforeBlockFirstWins() {
		final var line = "a // b /* c";
		assertEquals(2, JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerLineCommentMidLine() {
		assertEquals(3, JavaLineScanner.firstCommentMarker("a; // x", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerLineCommentStart() {
		assertEquals(0, JavaLineScanner.firstCommentMarker("// hi", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerMarkerInsideStringBlockCommentIgnored() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = \"a/*b*/c\";", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerMarkerInsideStringIgnored() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = \"a//b\";", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerPlainCodeNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("int x = a + b;", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerTextBlockClosesAtEolNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("end\"\"\"", new LexerState(false, true)));
	}

	@Test
	public void testFirstCommentMarkerTextBlockClosesThenLineComment() {
		final var line = "end\"\"\"; // c";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstCommentMarker(line, new LexerState(false, true)));
	}

	@Test
	public void testFirstCommentMarkerTextBlockClosesThenNoComment() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("end\"\"\"; x", new LexerState(false, true)));
	}

	@Test
	public void testFirstCommentMarkerTextBlockContinuationNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("still in block", new LexerState(false, true)));
	}

	@Test
	public void testFirstCommentMarkerTextBlockEscapedQuoteDoesNotClose() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("\\\"\"\" // x", new LexerState(false, true)));
	}

	@Test
	public void testFirstCommentMarkerTextBlockOpenedMarkerInsideIgnored() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = \"\"\"a//b\"\"\"", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerTextBlockOpenedThenLineComment() {
		final var line = "x = \"\"\"abc\"\"\"; // c";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstCommentMarker(line, LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerTextBlockOpenedUnterminatedMarkerIgnored() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = \"\"\"a//b", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerTrailingLoneSlashNoMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = a /", LexerState.NONE));
	}

	@Test
	public void testFirstCommentMarkerUnterminatedStringHidesMarker() {
		assertEquals(-1, JavaLineScanner.firstCommentMarker("x = \"a//b", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentAfterCompleteBlockComment() {
		final var line = "a /* x */ // y";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstLineComment(line, LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentBlockCommentOnlyNoLineComment() {
		assertEquals(-1, JavaLineScanner.firstLineComment("a /* x */ b", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentContinuedBlockCommentClosesThenLineComment() {
		final var line = "*/ x // y";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstLineComment(line, new LexerState(true, false)));
	}

	@Test
	public void testFirstLineCommentContinuedBlockCommentNeverCloses() {
		assertEquals(-1, JavaLineScanner.firstLineComment("still comment // x", new LexerState(true, false)));
	}

	@Test
	public void testFirstLineCommentDivisionOperatorNoMarker() {
		assertEquals(-1, JavaLineScanner.firstLineComment("x = a / b;", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentEmptyLineNoMarker() {
		assertEquals(-1, JavaLineScanner.firstLineComment("", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentEscapedCharLiteralThenLineComment() {
		final var line = "c = '\\''; // x";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstLineComment(line, LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentEscapedQuoteThenLineComment() {
		final var line = "s = \"a\\\"b\"; // c";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstLineComment(line, LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentInsideCharIgnored() {
		assertEquals(-1, JavaLineScanner.firstLineComment("c = '/';", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentInsideStringIgnored() {
		assertEquals(-1, JavaLineScanner.firstLineComment("x = \"a//b\";", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentInsideStringUnterminatedHidesMarker() {
		assertEquals(-1, JavaLineScanner.firstLineComment("x = \"a//b", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentMidLine() {
		assertEquals(3, JavaLineScanner.firstLineComment("a; // x", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentPlainCodeNoMarker() {
		assertEquals(-1, JavaLineScanner.firstLineComment("int x = a + b;", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentStart() {
		assertEquals(0, JavaLineScanner.firstLineComment("// hi", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentTextBlockClosesThenLineComment() {
		final var line = "end\"\"\"; // c";
		assertEquals(line.indexOf("//"), JavaLineScanner.firstLineComment(line, new LexerState(false, true)));
	}

	@Test
	public void testFirstLineCommentTextBlockClosesThenNoComment() {
		assertEquals(-1, JavaLineScanner.firstLineComment("end\"\"\"; x", new LexerState(false, true)));
	}

	@Test
	public void testFirstLineCommentTextBlockContentSlashesIgnored() {
		assertEquals(-1, JavaLineScanner.firstLineComment("x = \"\"\"a//b\"\"\"", LexerState.NONE));
	}

	@Test
	public void testFirstLineCommentTextBlockContinuationNeverCloses() {
		assertEquals(-1, JavaLineScanner.firstLineComment("still in block // x", new LexerState(false, true)));
	}

	@Test
	public void testFirstLineCommentTextBlockEscapedQuoteDoesNotClose() {
		assertEquals(-1, JavaLineScanner.firstLineComment("\\\"\"\" // x", new LexerState(false, true)));
	}

	@Test
	public void testFirstLineCommentTextBlockOpenedUnterminatedMarkerIgnored() {
		assertEquals(-1, JavaLineScanner.firstLineComment("x = \"\"\"a//b", LexerState.NONE));
	}

	@Test
	public void testInMultilineLiteralBlockCommentOpen() {
		assertTrue(new LexerState(true, false).inMultilineLiteral());
	}

	@Test
	public void testInMultilineLiteralNoneIsFalse() {
		assertFalse(LexerState.NONE.inMultilineLiteral());
	}

	@Test
	public void testInMultilineLiteralTextBlockOpen() {
		assertTrue(new LexerState(false, true).inMultilineLiteral());
	}

	@Test
	public void testMaskAllBlockCommentSpansLines() {
		final var masked = JavaLineScanner.maskAll(List.of("/* c", "still", "*/ x"));
		assertEquals(List.of("    ", "     ", "   x"), masked);
	}

	@Test
	public void testMaskAllEmptyListReturnsEmpty() {
		assertEquals(List.of(), JavaLineScanner.maskAll(List.of()));
	}

	@Test
	public void testMaskAllPlainLinesMatchPerLineStrip() {
		final var lines = List.of("int x;", "int y;");
		assertEquals(lines, JavaLineScanner.maskAll(lines));
	}

	@Test
	public void testMaskAllSingleLineUnchanged() {
		assertEquals(List.of("a + b;"), JavaLineScanner.maskAll(List.of("a + b;")));
	}

	@Test
	public void testMaskAllStringSlashesNotTreatedAsComment() {
		assertEquals(List.of("s=\"    \";"), JavaLineScanner.maskAll(List.of("s=\"a//b\";")));
	}

	@Test
	public void testMaskAllTextBlockNeverClosedBlanksTrailing() {
		final var masked = JavaLineScanner.maskAll(List.of("x=\"\"\"", "body"));
		assertEquals(List.of("x=   ", "    "), masked);
	}

	@Test
	public void testMaskAllTextBlockSpansLines() {
		final var masked = JavaLineScanner.maskAll(List.of("x=\"\"\"", "secret", "\"\"\";"));
		assertEquals(List.of("x=   ", "      ", "   ;"), masked);
	}

	@Test
	public void testMaskAllUnterminatedBlockCommentBlanksToEol() {
		assertEquals(List.of("    "), JavaLineScanner.maskAll(List.of("/* c")));
	}

	@Test
	public void testMatchingCloseAngleBracketsNotCounted() {
		final var line = "[a<b>c]";
		assertEquals(line.lastIndexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseBalancedOtherFamilyAgrees() {
		final var line = "[(a)]";
		assertEquals(line.lastIndexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseBraceNested() {
		final var line = "{a{b}}";
		assertEquals(line.lastIndexOf('}'), JavaLineScanner.matchingClose(line, line.indexOf('{')));
	}

	@Test
	public void testMatchingCloseBraceNestedInner() {
		final var line = "{{a}}";
		assertEquals(line.indexOf('}'), JavaLineScanner.matchingClose(line, line.indexOf('{', 1)));
	}

	@Test
	public void testMatchingCloseBraceSimple() {
		final var line = "x{y}";
		assertEquals(line.indexOf('}'), JavaLineScanner.matchingClose(line, line.indexOf('{')));
	}

	@Test
	public void testMatchingCloseBracketInStringLiteralIgnored() {
		final var line = "a[\"]\"]";
		assertEquals(line.lastIndexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseBracketNestedInner() {
		final var line = "[[a]]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[', 1)));
	}

	@Test
	public void testMatchingCloseBracketNestedOuter() {
		final var line = "[[a]]";
		assertEquals(line.lastIndexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseBracketSimple() {
		final var line = "a[b]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseComposesWithBlockCommentMask() {
		final var line = "a[/*]*/b]";
		assertEquals(line.lastIndexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseCrossedFamiliesSameFamilyWins() {
		final var line = "[(]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseEmptyGroup() {
		final var line = "[]";
		assertEquals(1, JavaLineScanner.matchingClose(line, 0));
	}

	@Test
	public void testMatchingCloseExtraTrailingCloseIgnored() {
		final var line = "[a]]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseNoCloseReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("[a", 0));
	}

	@Test
	public void testMatchingCloseOpenIndexIsCloseCharReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("a}", 1));
	}

	@Test
	public void testMatchingCloseOpenIndexNotOpenerReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("a{b}", 0));
	}

	@Test
	public void testMatchingCloseOpenIndexOutOfBoundsReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("[a]", -1));
		assertEquals(-1, JavaLineScanner.matchingClose("[a]", 3));
	}

	@Test
	public void testMatchingCloseOpenIsLastCharReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("x[", 1));
	}

	@Test
	public void testMatchingCloseParenAfterBlockCommentCloseOnLine() {
		final var line = "*/ f(a)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenAfterTextBlockCloseOnLine() {
		final var line = "\"\"\"; f(a)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenClosedBlockCommentBeforeParen() {
		final var line = "/* x */ f(a)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenEscapedQuoteInString() {
		final var line = "(\"\\\")\")";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenNestedInner() {
		final var line = "((a))";
		assertEquals(line.indexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(', 1)));
	}

	@Test
	public void testMatchingCloseParenNestedOuter() {
		final var line = "((a))";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenNoCloseReturnsMinusOne() {
		final var line = "(a";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenOpenIndexOutOfBoundsReturnsMinusOne() {
		final var line = "f(a)";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.length()));
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, -1));
	}

	@Test
	public void testMatchingCloseParenOpenParenIsLastCharReturnsMinusOne() {
		final var line = "f(";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.length() - 1));
	}

	@Test
	public void testMatchingCloseParenParenInBlockCommentIgnored() {
		final var line = "(a /* ) */ b)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenParenInCharIgnored() {
		final var line = "(')')";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenParenInLineCommentReturnsMinusOne() {
		final var line = "(a // )";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenParenInStringIgnored() {
		final var line = "(\"x)y\")";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenPriorClosedGroupIgnored() {
		final var line = "g() && f(a)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(', 3)));
	}

	@Test
	public void testMatchingCloseParenSimple() {
		final var line = "f(a)";
		assertEquals(line.lastIndexOf(')'), JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenTextBlockOpenerReturnsMinusOne() {
		final var line = "(x = \"\"\"";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseParenUnbalancedExtraOpenReturnsMinusOne() {
		final var line = "((a)";
		assertEquals(-1, JavaLineScanner.matchingCloseParen(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseRoundBracketSimple() {
		final var line = "f(a)";
		assertEquals(line.indexOf(')'), JavaLineScanner.matchingClose(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseSameFamilyIgnoresStrayCloseBrace() {
		final var line = "[}]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseSameFamilyIgnoresStrayCloseBracketInBrace() {
		final var line = "{]}";
		assertEquals(line.indexOf('}'), JavaLineScanner.matchingClose(line, line.indexOf('{')));
	}

	@Test
	public void testMatchingCloseSameFamilyIgnoresStrayCloseBracketInParen() {
		final var line = "(]a)";
		assertEquals(line.indexOf(')'), JavaLineScanner.matchingClose(line, line.indexOf('(')));
	}

	@Test
	public void testMatchingCloseSameFamilyIgnoresStrayCloseParen() {
		final var line = "[)]";
		assertEquals(line.indexOf(']'), JavaLineScanner.matchingClose(line, line.indexOf('[')));
	}

	@Test
	public void testMatchingCloseSameFamilyIgnoresStrayParenInBrace() {
		final var line = "{)}";
		assertEquals(line.indexOf('}'), JavaLineScanner.matchingClose(line, line.indexOf('{')));
	}

	@Test
	public void testMatchingCloseUnbalancedExtraOpenReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.matchingClose("[[a]", 0));
	}

	@Test
	public void testMultilineLiteralCloseIndex() {
		assertEquals(7, JavaLineScanner.multilineLiteralCloseIndex("text\"\"\"; });", new LexerState(false, true)));
		assertEquals(-1, JavaLineScanner.multilineLiteralCloseIndex("still in text block", new LexerState(false, true)));
		assertEquals(8, JavaLineScanner.multilineLiteralCloseIndex("line2 */ });", new LexerState(true, false)));
		assertEquals(-1, JavaLineScanner.multilineLiteralCloseIndex("int x;", LexerState.NONE));
		// a `"""` occupying the final three characters IS detected (i + 2 == length - 1, not off-by-one)
		assertEquals(6, JavaLineScanner.multilineLiteralCloseIndex("abc\"\"\"", new LexerState(false, true)));
		// an escaped quote is content, not the first character of a closing delimiter
		assertEquals(-1, JavaLineScanner.multilineLiteralCloseIndex("\\\"\"\" more", new LexerState(false, true)));
	}

	@Test
	public void testNextCodeLineEmptyListReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.nextCodeLine(List.of(), 0));
	}

	@Test
	public void testNextCodeLineFromPastEndReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.nextCodeLine(JavaLineScanner.maskAll(List.of("int x;")), 5));
	}

	@Test
	public void testNextCodeLineNegativeFromClampsToZero() {
		assertEquals(0, JavaLineScanner.nextCodeLine(JavaLineScanner.maskAll(List.of("int x;")), -1));
	}

	@Test
	public void testNextCodeLineNoCodeReturnsMinusOne() {
		assertEquals(-1, JavaLineScanner.nextCodeLine(JavaLineScanner.maskAll(List.of("// a", "\t", "")), 0));
	}

	@Test
	public void testNextCodeLineSkipsBlankAndComment() {
		assertEquals(3, JavaLineScanner.nextCodeLine(JavaLineScanner.maskAll(List.of("", "// c", "\t", "int x;")), 0));
	}

	@Test
	public void testNextCodeLineSkipsBlockCommentContinuation() {
		assertEquals(2, JavaLineScanner.nextCodeLine(JavaLineScanner.maskAll(List.of("/* c", "still", "*/ x")), 0));
	}

	@Test
	public void testOpensWithCommentAheadOfBrace() {
		final var raw = "\t/* n */ {";
		assertFalse(JavaLineScanner.opensWith(raw, JavaLineScanner.stripCommentsAndStrings(raw, LexerState.NONE), '{'));
	}

	@Test
	public void testOpensWithDifferentTokenReturnsFalse() {
		assertFalse(JavaLineScanner.opensWith("\t}", "\t}", '{'));
	}

	@Test
	public void testOpensWithMaskedBlankReturnsFalse() {
		final var raw = "// c";
		assertFalse(JavaLineScanner.opensWith(raw, JavaLineScanner.stripCommentsAndStrings(raw, LexerState.NONE), '{'));
	}

	@Test
	public void testOpensWithPlainBrace() {
		assertTrue(JavaLineScanner.opensWith("\t{", "\t{", '{'));
	}

	@Test
	public void testOpensWithTrailingCommentAfterBrace() {
		final var raw = "\t{ // n";
		assertTrue(JavaLineScanner.opensWith(raw, JavaLineScanner.stripCommentsAndStrings(raw, LexerState.NONE), '{'));
	}

	@Test
	public void testStateAfterBlockCommentClose() {
		assertFalse(JavaLineScanner.stateAfter("*/ x", new LexerState(true, false)).inBlockComment());
	}

	@Test
	public void testStateAfterBlockCommentCloseThenTextBlockOpen() {
		final var state = JavaLineScanner.stateAfter("*/ String s = \"\"\"", new LexerState(true, false));
		assertFalse(state.inBlockComment());
		assertTrue(state.inTextBlock());
	}

	@Test
	public void testStateAfterBlockCommentOpen() {
		assertTrue(JavaLineScanner.stateAfter("a /* b", LexerState.NONE).inBlockComment());
	}

	@Test
	public void testStateAfterBlockCommentStaysOpen() {
		assertTrue(JavaLineScanner.stateAfter("still in comment", new LexerState(true, false)).inBlockComment());
	}

	@Test
	public void testStateAfterCharLiteralStaysNone() {
		assertEquals(LexerState.NONE, JavaLineScanner.stateAfter("c = '\"';", LexerState.NONE));
	}

	@Test
	public void testStateAfterCompleteStringStaysNone() {
		assertEquals(LexerState.NONE, JavaLineScanner.stateAfter("x = \"a//b/*c\";", LexerState.NONE));
	}

	@Test
	public void testStateAfterEmptyLinePreservesBlockComment() {
		assertTrue(JavaLineScanner.stateAfter("", new LexerState(true, false)).inBlockComment());
	}

	@Test
	public void testStateAfterEmptyStringStaysNone() {
		assertEquals(LexerState.NONE, JavaLineScanner.stateAfter("s = \"\";", LexerState.NONE));
	}

	@Test
	public void testStateAfterLineCommentStopsScan() {
		assertEquals(LexerState.NONE, JavaLineScanner.stateAfter("x; // /* \"\"\"", LexerState.NONE));
	}

	@Test
	public void testStateAfterPlainCode() {
		assertEquals(LexerState.NONE, JavaLineScanner.stateAfter("int x;", LexerState.NONE));
	}

	@Test
	public void testStateAfterSlashStarSlashStaysOpen() {
		assertTrue(JavaLineScanner.stateAfter("/*/", LexerState.NONE).inBlockComment());
	}

	@Test
	public void testStateAfterTextBlockClose() {
		assertFalse(JavaLineScanner.stateAfter("x\"\"\";", new LexerState(false, true)).inTextBlock());
	}

	@Test
	public void testStateAfterTextBlockCloseBareDelimiterAtEol() {
		assertFalse(JavaLineScanner.stateAfter("\t \"\"\"", new LexerState(false, true)).inTextBlock());
	}

	@Test
	public void testStateAfterTextBlockClosesWithTrailingContentQuote() {
		final var s = JavaLineScanner.stateAfter("x\"\"\"\"", new LexerState(false, true));
		assertFalse(s.inTextBlock());
		assertFalse(s.inBlockComment());
	}

	@Test
	public void testStateAfterTextBlockCloseThenBlockCommentOpen() {
		final var state = JavaLineScanner.stateAfter("x\"\"\"; /* c", new LexerState(false, true));
		assertTrue(state.inBlockComment());
		assertFalse(state.inTextBlock());
	}

	@Test
	public void testStateAfterTextBlockOpen() {
		assertTrue(JavaLineScanner.stateAfter("x = \"\"\"", LexerState.NONE).inTextBlock());
	}

	@Test
	public void testStateAfterTextBlockStaysOpen() {
		assertTrue(JavaLineScanner.stateAfter("still in text block", new LexerState(false, true)).inTextBlock());
	}

	@Test
	public void testStripCharLiteralContentBlanked() {
		assertEquals("c=' ';", JavaLineScanner.stripCommentsAndStrings("c=',';", LexerState.NONE));
	}

	@Test
	public void testStripCleanCodeUnchanged() {
		assertEquals("int x = a + b;", JavaLineScanner.stripCommentsAndStrings("int x = a + b;", LexerState.NONE));
	}

	@Test
	public void testStripCommaInsideBlockComment() {
		assertEquals("f(a     b)", JavaLineScanner.stripCommentsAndStrings("f(a/*,*/b)", LexerState.NONE));
	}

	@Test
	public void testStripContinuedBlockComment() {
		assertEquals(" ".repeat(10), JavaLineScanner.stripCommentsAndStrings("still here", new LexerState(true, false)));
	}

	@Test
	public void testStripEmptyLineUnchanged() {
		assertEquals("", JavaLineScanner.stripCommentsAndStrings("", LexerState.NONE));
	}

	@Test
	public void testStripEscapedQuoteInChar() {
		assertEquals("c='  ';", JavaLineScanner.stripCommentsAndStrings("c='\\'';", LexerState.NONE));
	}

	@Test
	public void testStripEscapedQuoteInString() {
		assertEquals("\"    \"", JavaLineScanner.stripCommentsAndStrings("\"a\\\"b\"", LexerState.NONE));
	}

	@Test
	public void testStripLineComment() {
		assertEquals("a;   ", JavaLineScanner.stripCommentsAndStrings("a;//b", LexerState.NONE));
	}

	@Test
	public void testStripLineCommentInsideString() {
		assertEquals("\"    \"", JavaLineScanner.stripCommentsAndStrings("\"a//b\"", LexerState.NONE));
	}

	@Test
	public void testStripOuterParensCharInteriorParen() {
		assertEquals("c == ')'", JavaLineScanner.stripOuterParens("(c == ')')"));
	}

	@Test
	public void testStripOuterParensNested() {
		assertEquals("a", JavaLineScanner.stripOuterParens("(((a)))"));
	}

	@Test
	public void testStripOuterParensNonWrappingPair() {
		assertEquals("(a) && (b)", JavaLineScanner.stripOuterParens("(a) && (b)"));
	}

	@Test
	public void testStripOuterParensNoParens() {
		assertEquals("a + b", JavaLineScanner.stripOuterParens("a + b"));
	}

	@Test
	public void testStripOuterParensSimple() {
		assertEquals("a", JavaLineScanner.stripOuterParens("(a)"));
	}

	@Test
	public void testStripOuterParensStringInteriorParen() {
		assertEquals("f(\")\")", JavaLineScanner.stripOuterParens("(f(\")\"))"));
	}

	@Test
	public void testStripSingleLineBlockComment() {
		assertEquals("a     c", JavaLineScanner.stripCommentsAndStrings("a/*b*/c", LexerState.NONE));
	}

	@Test
	public void testStripSlashStarSlashStaysOpen() {
		assertEquals("    ", JavaLineScanner.stripCommentsAndStrings("/*/x", LexerState.NONE));
	}

	@Test
	public void testStripStringAndComment() {
		assertEquals("f(\" \");    ", JavaLineScanner.stripCommentsAndStrings("f(\"x\"); //y", LexerState.NONE));
	}

	@Test
	public void testStripStringContentBlanked() {
		assertEquals("x=\"   \";", JavaLineScanner.stripCommentsAndStrings("x=\"a,b\";", LexerState.NONE));
	}

	@Test
	public void testStripTextBlockEscapedQuoteDoesNotClose() {
		assertEquals(" ".repeat(7), JavaLineScanner.stripCommentsAndStrings("\\\"\"\"abc", new LexerState(false, true)));
	}

	@Test
	public void testStripTextBlockOpenMidContent() {
		assertEquals("x = " + " ".repeat(6), JavaLineScanner.stripCommentsAndStrings("x = \"\"\"abc", LexerState.NONE));
	}

	@Test
	public void testStripUnterminatedStringBlanksToEol() {
		assertEquals("x=\"   ", JavaLineScanner.stripCommentsAndStrings("x=\"abc", LexerState.NONE));
	}
}