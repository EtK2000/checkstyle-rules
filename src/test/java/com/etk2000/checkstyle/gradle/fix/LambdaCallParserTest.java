package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

public class LambdaCallParserTest {
	@Test
	public void testExtractReceiverStartAtPositionZero() {
		assertEquals(0, LambdaCallParser.extractReceiverStart("anything", 0));
	}

	@Test
	public void testExtractReceiverStartChainedIdentifier() {
		final var text = "this.target.forEach(";
		final var forEachCol = text.indexOf(".forEach");
		assertEquals(0, LambdaCallParser.extractReceiverStart(text, forEachCol));
	}

	@Test
	public void testExtractReceiverStartEmptyText() {
		assertEquals(0, LambdaCallParser.extractReceiverStart("", 0));
	}

	@Test
	public void testExtractReceiverStartIdentifier() {
		final var text = "source.forEach(";
		final var forEachCol = text.indexOf(".forEach");
		assertEquals(0, LambdaCallParser.extractReceiverStart(text, forEachCol));
	}

	@Test
	public void testExtractReceiverStartJustDots() {
		final var text = "....";
		assertEquals(0, LambdaCallParser.extractReceiverStart(text, text.length()));
	}

	@Test
	public void testExtractReceiverStartNoIdentifier() {
		final var text = "    ";
		assertEquals(text.length(), LambdaCallParser.extractReceiverStart(text, text.length()));
	}

	@Test
	public void testExtractReceiverStartStopsAtNonIdent() {
		final var text = "foo(bar.baz";
		assertEquals(4, LambdaCallParser.extractReceiverStart(text, text.length()));
	}

	@Test
	public void testExtractReceiverStartStopsAtWhitespace() {
		final var text = "return source.forEach";
		final var forEachCol = text.indexOf(".forEach");
		assertEquals(7, LambdaCallParser.extractReceiverStart(text, forEachCol));
	}

	@Test
	public void testFindClosingBraceLineMultiLineBalanced() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add(item);",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineMultiLineBlockCommentWithFakeBrace() {
		final var lines = List.of(
				"for (var e : source.entrySet()) {",
				"\t/* block comment",
				"\twith } brace */",
				"\ttarget.put(e.getKey(), e.getValue());",
				"}"
		);
		assertEquals(4, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsCharLiteral() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add('}');",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsEscapedQuoteInStringWithBrace() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add(\"\\\"}\");",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsLineComment() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add(item); // fake }",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsStringLiteral() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add(\"}\");",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsTextBlock() {
		final var lines = List.of(
				"for (var i = 0; i < n; ++i) {",
				"\tvar s = \"\"\"",
				"\t    fake } brace",
				"\t    \"\"\";",
				"}"
		);
		assertEquals(4, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsTextBlockAdjacentOpenAndClose() {
		// An empty text block `""""""` is open + close adjacent on the same line; the
		// scanner must correctly exit text-block state so the trailing `}` is counted.
		final var lines = List.of(
				"for (var i = 0; i < n; ++i) {",
				"\tvar s = \"\"\"\"\"\";",
				"}"
		);
		assertEquals(2, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineRespectsTextBlockClosingAtEndOfLine() {
		// Closing `"""` at the very end of a line (no trailing content) must still
		// exit text-block state so the next line's brace is counted structurally.
		final var lines = List.of(
				"for (var i = 0; i < n; ++i) {",
				"\tvar s = \"\"\"",
				"\tcontent",
				"\t\"\"\"",
				"\t;",
				"}"
		);
		assertEquals(5, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineReturnsMinus1WhenNoOpenBrace() {
		final var lines = List.of(
				"\t}"
		);
		assertEquals(-1, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineSingleLine() {
		final var lines = List.of(
				"for (var item : list) { target.add(item); }"
		);
		assertEquals(0, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingBraceLineUnclosedReturnsMinus1() {
		final var lines = List.of(
				"for (var item : list) {",
				"\ttarget.add(item);"
		);
		assertEquals(-1, LambdaCallParser.findClosingBraceLine(lines, 0));
	}

	@Test
	public void testFindClosingParenInLineBalanced() {
		assertEquals(4, LambdaCallParser.findClosingParenInLine("foo()", 3));
	}

	@Test
	public void testFindClosingParenInLineBlockComment() {
		final var text = "foo(/* ) */)";
		assertEquals(text.length() - 1, LambdaCallParser.findClosingParenInLine(text, 3));
	}

	@Test
	public void testFindClosingParenInLineCharLiteral() {
		final var text = "foo(')')";
		assertEquals(text.length() - 1, LambdaCallParser.findClosingParenInLine(text, 3));
	}

	@Test
	public void testFindClosingParenInLineEscapedQuoteInString() {
		final var text = "foo(\"a\\\")\")";
		assertEquals(text.length() - 1, LambdaCallParser.findClosingParenInLine(text, 3));
	}

	@Test
	public void testFindClosingParenInLineLineComment() {
		// A `//` line comment terminates the scan; a `)` after `//` is not matched.
		assertEquals(-1, LambdaCallParser.findClosingParenInLine("foo( // )", 3));
	}

	@Test
	public void testFindClosingParenInLineNegativeOpenIdx() {
		assertEquals(-1, LambdaCallParser.findClosingParenInLine("foo()", -1));
	}

	@Test
	public void testFindClosingParenInLineNested() {
		assertEquals(12, LambdaCallParser.findClosingParenInLine("foo(bar(baz))", 3));
	}

	@Test
	public void testFindClosingParenInLineStringLiteral() {
		final var text = "foo(\")\")";
		assertEquals(text.length() - 1, LambdaCallParser.findClosingParenInLine(text, 3));
	}

	@Test
	public void testFindClosingParenInLineTextBlock() {
		final var text = "foo(\"\"\")\"\"\")";
		assertEquals(text.length() - 1, LambdaCallParser.findClosingParenInLine(text, 3));
	}

	@Test
	public void testFindClosingParenInLineUnbalanced() {
		assertEquals(-1, LambdaCallParser.findClosingParenInLine("foo(", 3));
	}

	@Test
	public void testFindClosingParenMultiLineBalanced() {
		final var lines = List.of(
				"source.forEach((k, v) -> {",
				"    target.put(k, v);",
				"});"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(2, loc.line());
		assertEquals(1, loc.col());
	}

	@Test
	public void testFindClosingParenMultiLineBlockComment() {
		final var lines = List.of(
				"source.forEach(/* start",
				"    fake ) text",
				"    end */ x -> foo(x));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(2, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsBlockCommentSameLine() {
		final var lines = List.of(
				"source.forEach(x -> foo(/* ) */ x));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsCharLiterals() {
		final var lines = List.of(
				"source.forEach(x -> foo(')'));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsEscapedCharLiteral() {
		// `'\''` contains a backslash-escaped single quote; the escape skip must advance
		// past it so the closing `'` does not prematurely end the char literal.
		final var lines = List.of(
				"source.forEach(x -> foo('\\''));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsEscapedQuote() {
		final var lines = List.of(
				"source.forEach(x -> foo(\"a\\\"b)c\"));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsLineComment() {
		final var lines = List.of(
				"source.forEach( // fake )",
				"\tx -> foo(x)",
				");"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(2, loc.line());
	}

	@Test
	public void testFindClosingParenRespectsNestedCalls() {
		final var lines = List.of(
				"source.forEach(x -> foo(a));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
		assertEquals(26, loc.col());
	}

	@Test
	public void testFindClosingParenRespectsStringLiterals() {
		final var lines = List.of(
				"source.forEach(x -> foo(\"a)b\"));"
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenReturnsLocationColumn() {
		final var lines = List.of("source.forEach(x);");
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
		assertEquals(16, loc.col());
	}

	@Test
	public void testFindClosingParenSingleLine() {
		final var lines = List.of("source.forEach(x);");
		final var openParenCol = lines.getFirst().indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 0, openParenCol);
		assertNotNull(loc);
		assertEquals(0, loc.line());
	}

	@Test
	public void testFindClosingParenStartingFromMiddle() {
		final var lines = List.of(
				"// first line ignored (fake (",
				"source.forEach(x);"
		);
		final var openParenCol = lines.get(1).indexOf('(');
		final var loc = LambdaCallParser.findClosingParen(lines, 1, openParenCol);
		assertNotNull(loc);
		assertEquals(1, loc.line());
	}

	@Test
	public void testFindClosingParenUnclosed() {
		final var lines = List.of(
				"source.forEach(x -> foo("
		);
		final var openParenCol = lines.getFirst().indexOf('(');
		assertNull(LambdaCallParser.findClosingParen(lines, 0, openParenCol));
	}

	@Test
	public void testFindEndOfBracelessStatementBasic() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(item);"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementBlockCommentStartsOnStartLine() {
		final var lines = List.of(
				"for (var item : list) /* comment starts",
				"\tcontinuation */",
				"\ttarget.add(item);"
		);
		assertEquals(2, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementBodyWithMultiLineArgList() {
		// Braceless body whose method call spans multiple lines: paren depth rises on
		// line 1 (`add(`), stays >0 on lines 2-3 (arg expressions), and returns to 0 on
		// line 4 (`)`), where the final `;` is structural. Depth must carry across lines.
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(method(",
				"\t\targ1,",
				"\t\targ2",
				"\t));"
		);
		assertEquals(4, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementLineCommentOnStartLine() {
		// A trailing `//` comment on the for-header line must not leave the scanner
		// in an inconsistent state for the following body line.
		final var lines = List.of(
				"for (var item : list) // header comment",
				"\ttarget.add(item);"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementMalformedHeaderReturnsMinus1() {
		// Malformed input: for-header's `(` is never closed. Depth carries across lines
		// stuck at 1, so `;` on subsequent lines are never at paren-depth 0. Function
		// defensively returns -1 (can't determine terminator).
		final var lines = List.of(
				"for (var i = 0;",
				"\ttarget.add(item);"
		);
		assertEquals(-1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementMultiLineBlockComment() {
		// A multi-line block comment between the for header and the body must not be
		// treated as code. The `;` inside the comment (e.g. on the `comment */;` line)
		// is not a statement terminator, but the `;` on the real body line is.
		final var lines = List.of(
				"for (var item : list)",
				"\t/* multi-line",
				"\t   comment */",
				"\ttarget.add(item);"
		);
		assertEquals(3, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementMultiLineForHeader() {
		// For-header spans 3 lines (init, condition, iterator each on their own line).
		// Paren depth must carry across lines; only the final `)` closes the header. The
		// body `;` is on line 3, at depth 0.
		final var lines = List.of(
				"for (var i = 0;",
				"     i < arr.length;",
				"     ++i)",
				"\tarr[i] = 0;"
		);
		assertEquals(3, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementMultipleStatementsOnSameLineSkipped() {
		// Line 2 has no structural `;`, so overall the method returns -1.
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(item); x = 5;",
				"\tbody()"
		);
		assertEquals(-1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementNestedParensInHeader() {
		// For-header with nested parens (`for (var i = 0; i < getMax(a, b); ++i)`).
		// `headerCloseIdx` must track the OUTER `)`, not the inner one after `getMax(a, b)`.
		final var lines = List.of(
				"for (var i = 0; i < getMax(a, b); ++i)",
				"\tarr[i] = 0;"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementNoSemicolonReturnsMinus1() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(item)"
		);
		assertEquals(-1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementRespectsTextBlock() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(\"\"\"",
				"\tfake ; inside",
				"\t\"\"\");"
		);
		assertEquals(3, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementRespectsTextBlockLineEndingInSemi() {
		final var lines = List.of(
				"for (var i = 0; i < arr.length; ++i)",
				"\tarr[i] = \"\"\"",
				"\tSELECT * FROM t;",
				"\tWHERE x = 1",
				"\t\"\"\";"
		);
		assertEquals(4, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSemicolonInCharIgnored() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(';');"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSemicolonInStringIgnored() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(\";\");"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSemicolonNotLastStructuralCharSkipsLine() {
		// A line with `;` followed by more structural content (e.g. `foo(); bar()`) must
		// not be claimed as the terminator; the next line's `;` is the real terminator.
		final var lines = List.of(
				"for (var item : list)",
				"\tfoo(); bar()",
				"\ttarget.add(item);"
		);
		assertEquals(2, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSingleLineBody() {
		final var lines = List.of(
				"for (var item : list) target.add(item);"
		);
		assertEquals(0, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSingleLineEmptyBodyIsNotClaimed() {
		final var lines = List.of(
				"for (var item : list)",
				"\ttarget.add(item);"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSingleLineIndexedForBody() {
		final var lines = List.of(
				"for (var i = 0; i < arr.length; ++i) arr[i] = 0;"
		);
		assertEquals(0, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testFindEndOfBracelessStatementSingleLineTrailingWhitespaceIsNotClaimed() {
		final var lines = List.of(
				"for (var item : list)   ",
				"\ttarget.add(item);"
		);
		assertEquals(1, LambdaCallParser.findEndOfBracelessStatement(lines, 0));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenBraceInBlockComment() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("for (var item : list) /* { */"));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenBraceInCharLiteral() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("for (var i = 0; i < arr.length; ++i) arr[i] = '{';"));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenBraceInLineComment() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("for (var item : list) // {"));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenBraceInString() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("for (var item : getList(\"{\"))"));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenBraceInTextBlock() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("var s = \"\"\"{\"\"\";"));
	}

	@Test
	public void testHasStructuralOpenBraceFalseWhenNone() {
		assertFalse(LambdaCallParser.hasStructuralOpenBrace("for (var item : list)"));
	}

	@Test
	public void testHasStructuralOpenBraceTrueWhenStructural() {
		assertTrue(LambdaCallParser.hasStructuralOpenBrace("for (var item : list) {"));
	}

	@Test
	public void testHasUnclosedBraceBalanced() {
		assertFalse(LambdaCallParser.hasUnclosedBrace("class T { void f() { } }"));
	}

	@Test
	public void testHasUnclosedBraceEmpty() {
		assertFalse(LambdaCallParser.hasUnclosedBrace(""));
	}

	@Test
	public void testHasUnclosedBraceIgnoresBlockCommentContent() {
		assertFalse(LambdaCallParser.hasUnclosedBrace("foo(); /* { */"));
	}

	@Test
	public void testHasUnclosedBraceIgnoresCharLiteralContent() {
		assertFalse(LambdaCallParser.hasUnclosedBrace("char c = '{'; "));
	}

	@Test
	public void testHasUnclosedBraceIgnoresEscapedCharLiteral() {
		// `'\''` has an escaped quote that must not terminate the char literal early,
		// so the `{` after it stays outside the literal and gets counted. Here `{` is
		// balanced with `}`, asserting the scan completes without breaking the literal.
		assertFalse(LambdaCallParser.hasUnclosedBrace("foo('\\'') { }"));
	}

	@Test
	public void testHasUnclosedBraceIgnoresEscapedQuoteInString() {
		// `"\"{"` contains an escaped quote followed by `{`; the escape must ensure the
		// string doesn't terminate early, so the `{` stays inside the literal
		assertFalse(LambdaCallParser.hasUnclosedBrace("foo(\"a\\\"{\")"));
	}

	@Test
	public void testHasUnclosedBraceIgnoresLineCommentContent() {
		assertFalse(LambdaCallParser.hasUnclosedBrace("foo() // hidden {"));
	}

	@Test
	public void testHasUnclosedBraceIgnoresStringLiteralContent() {
		assertFalse(LambdaCallParser.hasUnclosedBrace("var s = \"{\"; "));
	}

	@Test
	public void testHasUnclosedBraceOpen() {
		assertTrue(LambdaCallParser.hasUnclosedBrace("synchronized (lock) { "));
	}

	@Test
	public void testHasUnclosedBraceOverclosed() {
		assertTrue(LambdaCallParser.hasUnclosedBrace("} foo()"));
	}

	@Test
	public void testHasUnclosedParenBalanced() {
		assertFalse(LambdaCallParser.hasUnclosedParen("if (flag) "));
	}

	@Test
	public void testHasUnclosedParenEmpty() {
		assertFalse(LambdaCallParser.hasUnclosedParen(""));
	}

	@Test
	public void testHasUnclosedParenIgnoresBlockCommentContent() {
		assertFalse(LambdaCallParser.hasUnclosedParen("foo /* ( */ "));
	}

	@Test
	public void testHasUnclosedParenIgnoresCharLiteralContent() {
		assertFalse(LambdaCallParser.hasUnclosedParen("char c = '('; "));
	}

	@Test
	public void testHasUnclosedParenIgnoresCharLiteralParen() {
		assertFalse(LambdaCallParser.hasUnclosedParen("foo('(') "));
	}

	@Test
	public void testHasUnclosedParenIgnoresEscapedCharLiteral() {
		assertFalse(LambdaCallParser.hasUnclosedParen("foo('\\'') "));
	}

	@Test
	public void testHasUnclosedParenIgnoresEscapedQuoteInString() {
		// `"\"("` contains an escaped quote followed by `(`; the escape must ensure the
		// string doesn't terminate at the escaped quote, so the `(` stays inside
		assertFalse(LambdaCallParser.hasUnclosedParen("foo(\"a\\\"(\") "));
	}

	@Test
	public void testHasUnclosedParenIgnoresLineCommentContent() {
		assertFalse(LambdaCallParser.hasUnclosedParen("foo() // hidden ("));
	}

	@Test
	public void testHasUnclosedParenIgnoresStringLiteralContent() {
		assertFalse(LambdaCallParser.hasUnclosedParen("print(\"(\") "));
	}

	@Test
	public void testHasUnclosedParenOpen() {
		assertTrue(LambdaCallParser.hasUnclosedParen("map.forEach((k, v) -> "));
	}

	@Test
	public void testHasUnclosedParenOverclosed() {
		assertTrue(LambdaCallParser.hasUnclosedParen("foo())"));
	}

	@Test
	public void testIndexOfStructuralCharFindsStructural() {
		assertEquals(1, LambdaCallParser.indexOfStructuralChar("a;b", 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexAtTextBlockOpener() {
		// fromIndex=0 at the first `"` of `"""`: the scanner must enter text-block mode
		// and skip `;` inside the block; the structural `;` is after the closing `"""`.
		final var text = "\"\"\"abc;def\"\"\";";
		assertEquals(text.length() - 1, LambdaCallParser.indexOfStructuralChar(text, 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexInsideOpenStringState() {
		// String is STILL OPEN at fromIndex; the `;` inside the string must not match.
		// Essential proof that state accumulated before fromIndex affects matching after.
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("\"a;b", 3, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexLandsOnEscapeSecondByteReturnsMinus1() {
		// fromIndex=3 lands on the second byte of `\"` inside a string. Single-pass scan
		// ensures the escape is processed as a unit (not double-advanced), so the `;`
		// inside the string is correctly reported as non-structural.
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("\"a\\\"b;c\"", 3, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexSeedingAdvancesStringState() {
		// Advances scanner state through a string literal (`"a;b"`) so by fromIndex=5
		// `inString` is false and the structural `;` at position 5 is matched correctly.
		assertEquals(5, LambdaCallParser.indexOfStructuralChar("\"a;b\";c", 5, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexSeedingLineCommentShortCircuits() {
		// If the seeding loop hits a `//` line comment, the whole search returns -1,
		// even if fromIndex is past the `//`.
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("// ;", 3, ';'));
	}

	@Test
	public void testIndexOfStructuralCharFromIndexSkipsEarlier() {
		assertEquals(3, LambdaCallParser.indexOfStructuralChar("a;b;c", 2, ';'));
	}

	@Test
	public void testIndexOfStructuralCharIgnoresInsideBlockComment() {
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("/* ; */", 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharIgnoresInsideCharLiteral() {
		final var text = "char c = ';';";
		assertEquals(text.lastIndexOf(';'), LambdaCallParser.indexOfStructuralChar(text, 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharIgnoresInsideLineComment() {
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("// ; comment", 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharIgnoresInsideString() {
		final var text = "\"a;b\";";
		assertEquals(text.lastIndexOf(';'), LambdaCallParser.indexOfStructuralChar(text, 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharIgnoresInsideTextBlock() {
		final var text = "\"\"\"a;b\"\"\";";
		assertEquals(text.lastIndexOf(';'), LambdaCallParser.indexOfStructuralChar(text, 0, ';'));
	}

	@Test
	public void testIndexOfStructuralCharReturnsMinus1WhenNotPresent() {
		assertEquals(-1, LambdaCallParser.indexOfStructuralChar("abc", 0, ';'));
	}

	@Test
	public void testIndexOfStructuralEmptyNeedleReturnsZero() {
		assertEquals(0, LambdaCallParser.indexOfStructural("anything", ""));
	}

	@Test
	public void testIndexOfStructuralFindsNeedleAtStart() {
		assertEquals(0, LambdaCallParser.indexOfStructural(": rest of text", ": "));
	}

	@Test
	public void testIndexOfStructuralFindsStructural() {
		assertEquals(11, LambdaCallParser.indexOfStructural("for (var i : list)", ": "));
	}

	@Test
	public void testIndexOfStructuralIgnoresInsideBlockComment() {
		assertEquals(-1, LambdaCallParser.indexOfStructural("x /* key: value */ = 1;", ": "));
	}

	@Test
	public void testIndexOfStructuralIgnoresInsideCharLiteral() {
		// `: ` cannot fit in a single char literal, but a `:` or ` ` inside a char
		// literal must still be respected; the combined needle must not be found.
		assertEquals(-1, LambdaCallParser.indexOfStructural("char c = ':'; ", ": "));
	}

	@Test
	public void testIndexOfStructuralIgnoresInsideLineComment() {
		// After `//`, the scanner returns -1 immediately; needle after `//` is ignored.
		assertEquals(-1, LambdaCallParser.indexOfStructural("x = 1; // key: value", ": "));
	}

	@Test
	public void testIndexOfStructuralIgnoresInsideString() {
		final var text = "x = \"key: value\"; for (var i : list)";
		final var expected = text.lastIndexOf(": ");
		assertEquals(expected, LambdaCallParser.indexOfStructural(text, ": "));
	}

	@Test
	public void testIndexOfStructuralIgnoresInsideTextBlock() {
		assertEquals(-1, LambdaCallParser.indexOfStructural("var s = \"\"\"key: value\"\"\";", ": "));
	}

	@Test
	public void testIndexOfStructuralReturnsMinus1WhenOnlyInString() {
		assertEquals(-1, LambdaCallParser.indexOfStructural("var s = \"x: y\";", ": "));
	}

	@Test
	public void testStripCommentBlockCommentSameLine() {
		assertEquals("foo  bar", LambdaCallParser.stripComment("foo /* remove */ bar"));
	}

	@Test
	public void testStripCommentBlockCommentStartsButNotEnds() {
		assertEquals("foo ", LambdaCallParser.stripComment("foo /* unterminated"));
	}

	@Test
	public void testStripCommentEmpty() {
		assertEquals("", LambdaCallParser.stripComment(""));
	}

	@Test
	public void testStripCommentIgnoresSlashInCharLiteral() {
		assertEquals("char c = '/';", LambdaCallParser.stripComment("char c = '/';"));
	}

	@Test
	public void testStripCommentIgnoresSlashInStringLiteral() {
		assertEquals(
				"var url = \"http://x\";",
				LambdaCallParser.stripComment("var url = \"http://x\";")
		);
	}

	@Test
	public void testStripCommentLineAfterBlockComment() {
		assertEquals("foo  bar ", LambdaCallParser.stripComment("foo /* x */ bar // comment"));
	}

	@Test
	public void testStripCommentNoComment() {
		assertEquals("foo();", LambdaCallParser.stripComment("foo();"));
	}

	@Test
	public void testStripCommentRemovesLineComment() {
		assertEquals("foo(); ", LambdaCallParser.stripComment("foo(); // a comment"));
	}

	@Test
	public void testStripCommentRespectsEscapedCharLiteral() {
		// `'\''` is a char literal containing an escaped single quote; the `//` after it
		// must still be stripped, and the escape inside the literal must be preserved.
		assertEquals(
				"char c = '\\''; ",
				LambdaCallParser.stripComment("char c = '\\''; // comment")
		);
	}

	@Test
	public void testStripCommentRespectsEscapedQuote() {
		assertEquals(
				"var s = \"\\\"\"; ",
				LambdaCallParser.stripComment("var s = \"\\\"\"; // comment")
		);
	}

	@Test
	public void testStripCommentsJoinedBlockCommentNotEndingOnLine() {
		// `/*` starts but no `*/` on that line: the rest of the line is stripped
		// and the block-comment state carries forward. The next line (still inside
		// the comment) is entirely skipped. The output has the leading `code ` kept,
		// then join-space after line 0, then nothing from line 1, then join-space
		// after line 1, then ` end` (leading space + content) from line 2 after `*/`.
		final var lines = List.of(
				"code /* starts here",
				"still inside",
				"closed here */ end"
		);
		assertEquals("code    end", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 2));
	}

	@Test
	public void testStripCommentsJoinedCharLiteralPreserved() {
		final var lines = List.of("char c = '/';");
		assertEquals("char c = '/';", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 0));
	}

	@Test
	public void testStripCommentsJoinedEndLineEqualsStartLine() {
		final var lines = List.of("prefix source.forEach(body);");
		assertEquals("source.forEach(body);", LambdaCallParser.stripCommentsJoined(lines, 0, "prefix ".length(), 0));
	}

	@Test
	public void testStripCommentsJoinedEscapedCharLiteralPreserved() {
		// `'\''` is a char literal containing an escaped single quote; the `//` after
		// it must still be stripped, and the escape inside the literal must be preserved.
		final var lines = List.of("char c = '\\''; // comment");
		assertEquals("char c = '\\''; ", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 0));
	}

	@Test
	public void testStripCommentsJoinedEscapedQuoteInString() {
		final var lines = List.of("var s = \"a\\\"b\";");
		assertEquals("var s = \"a\\\"b\";", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 0));
	}

	@Test
	public void testStripCommentsJoinedLineCommentTerminatesEarly() {
		final var lines = List.of(
				"first line",
				"middle // ignore this",
				"last line"
		);
		assertEquals("first line middle  last line", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 2));
	}

	@Test
	public void testStripCommentsJoinedMultiLine() {
		final var lines = List.of(
				"alpha",
				"beta",
				"gamma"
		);
		assertEquals("alpha beta gamma", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 2));
	}

	@Test
	public void testStripCommentsJoinedMultiLineBlockComment() {
		final var lines = List.of(
				"code /* start",
				"middle",
				"end */ tail"
		);
		assertEquals("code    tail", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 2));
	}

	@Test
	public void testStripCommentsJoinedMultiLineStartColOffset() {
		final var lines = List.of(
				"prefix body_start",
				"\tbody_end"
		);
		assertEquals("body_start \tbody_end", LambdaCallParser.stripCommentsJoined(lines, 0, "prefix ".length(), 1));
	}

	@Test
	public void testStripCommentsJoinedSingleLine() {
		final var lines = List.of("foo(); // comment");
		assertEquals("foo(); ", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 0));
	}

	@Test
	public void testStripCommentsJoinedStringLiteralPreserved() {
		final var lines = List.of("var s = \"//\";");
		assertEquals("var s = \"//\";", LambdaCallParser.stripCommentsJoined(lines, 0, 0, 0));
	}

	@Test
	public void testStripCommentSlashAtEnd() {
		assertEquals("foo / ", LambdaCallParser.stripComment("foo / "));
	}
}