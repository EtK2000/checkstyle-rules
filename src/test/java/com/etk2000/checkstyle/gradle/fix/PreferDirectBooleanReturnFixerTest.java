package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferDirectBooleanReturnFixerTest {
	private final CheckstyleFixer fixer = new PreferDirectBooleanReturnFixer();

	@Test
	public void testAtomicDollarSign() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (foo$bar) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !foo$bar;"), result.replacement());
	}

	@Test
	public void testAtomicMethodCallNegated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (s.isEmpty()) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !s.isEmpty();"), result.replacement());
	}

	@Test
	public void testAtomicSupplementaryCodepoint() {
		// U+1D400 is a supplementary codepoint requiring Character.charCount(cp) == 2;
		// Character.isLetter is true for the codepoint but false for either surrogate
		// half alone. A char-based scan would reject it; the codepoint walk accepts it.
		final var supp = new StringBuilder().appendCodePoint(0x1D400).toString();
		final var lines = new ArrayList<>(List.of(
				"\t\tif (" + supp + ") return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !" + supp + ";"), result.replacement());
	}

	@Test
	public void testAtomicWhitespace() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (s . isEmpty()) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !s . isEmpty();"), result.replacement());
	}

	@Test
	public void testBlankLineBetweenBodyAndTrailingSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testBlockCommentWithParenInCond() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (/* ) */ flag) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn /* ) */ flag;"), result.replacement());
	}

	@Test
	public void testBlockCommentWithStarMidBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (/* a * b ) */ flag) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn /* a * b ) */ flag;"), result.replacement());
	}

	@Test
	public void testBracedBodyCloseBraceIndentMismatch() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t}",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testBracedBodyTruncated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testBracedBothBranches() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\telse {",
				"\t\t\treturn false;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testBracedElseCloseBraceIndentMismatch() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\telse {",
				"\t\t\treturn false;",
				"\t}"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testBracedElseTruncated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\telse {",
				"\t\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testBracedIfBodyTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testBracedIfBodyTrailingReturnNegated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn false;",
				"\t\t}",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn !flag;"), result.replacement());
	}

	@Test
	public void testBracedThenUnbracedElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\telse",
				"\t\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testCharLiteralEscape() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (c == '\\'') return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn c == '\\'';"), result.replacement());
	}

	@Test
	public void testCharLiteralWithParen() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (c == ')') return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn c == ')';"), result.replacement());
	}

	@Test
	public void testColumnBeyondLineSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 1000));
	}

	@Test
	public void testCommentBetweenBracesSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) // comment",
				"\t\t\treturn true;",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testComparisonNegatedAddsParens() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (x > 0) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !(x > 0);"), result.replacement());
	}

	@Test
	public void testCondInQuotesIgnored() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (s.equals(\")\")) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn s.equals(\")\");"), result.replacement());
	}

	@Test
	public void testCondWithStringEscape() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (s.equals(\"\\\"\")) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn s.equals(\"\\\"\");"), result.replacement());
	}

	@Test
	public void testCondWithStringLiteralIsNotAtomic() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (\"x\".equals(s)) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !(\"x\".equals(s));"), result.replacement());
	}

	@Test
	public void testDoubleNegationSimplifies() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (!flag) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testForwardInlineFalseTrue() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !flag;"), result.replacement());
	}

	@Test
	public void testForwardInlineTrueFalse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testIfWithoutOpenParen() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif x return true;",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testIndentationPreserved() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\t\tif (flag) return true;",
				"\t\t\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 4));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\t\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testInlineThenBracedElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\telse {",
				"\t\t\treturn false;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testLandConditionNegatedAddsParens() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a && b) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !(a && b);"), result.replacement());
	}

	@Test
	public void testLandConditionNotNegatedNoParens() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a && b) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn a && b;"), result.replacement());
	}

	@Test
	public void testLineCommentInCondSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag // comment",
				"\t\t\t) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 2));
		assertEquals("multi-line if condition", result.reason());
	}

	@Test
	public void testMultiLineCondSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a",
				"\t\t\t\t&& b)",
				"\t\t\treturn true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 2));
		assertEquals("multi-line if condition", result.reason());
	}

	@Test
	public void testMultiStatementIfBodySkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) {",
				"\t\t\tcompute();",
				"\t\t\treturn true;",
				"\t\t}",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testNegativeColumnSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNestedParensInCondition() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif ((a || b) && c) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn (a || b) && c;"), result.replacement());
	}

	@Test
	public void testNestedParensInConditionNegated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif ((a || b) && c) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !((a || b) && c);"), result.replacement());
	}

	@Test
	public void testNextLineBodyNoBodyLineSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testNextLineBodyTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)",
				"\t\t\treturn true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testNextLineBodyTrailingReturnNegated() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)",
				"\t\t\treturn false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn !flag;"), result.replacement());
	}

	@Test
	public void testNextLineThenBracedElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)",
				"\t\t\treturn true;",
				"\t\telse {",
				"\t\t\treturn false;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}

	@Test
	public void testNoIfPrefixSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\twhile (flag) return true;",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testNotIdentInPrecedence() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (!a && b) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !(!a && b);"), result.replacement());
	}

	@Test
	public void testNotIdentNotNegatedKeptAsIs() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (!flag) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn !flag;"), result.replacement());
	}

	@Test
	public void testNotParenthesizedSimplifies() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (!(flag)) return false;",
				"\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\treturn (flag);"), result.replacement());
	}

	@Test
	public void testNoTrailingLineSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testSameLiteralBothSidesSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\treturn true;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testTrailingCommentOnReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true; // important",
				"\t\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testTrailingReturnIndentMismatchSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\treturn false;"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testTrailingReturnNonLiteralSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag) return true;",
				"\t\treturn computeY();"
		));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testUnicodeEscapeInCondSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (foo\\u0029) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 2));
		assertEquals("Unicode escape in condition", result.reason());
	}

	@Test
	public void testUnterminatedBlockCommentInCondSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (/* unterm flag) return true;",
				"\t\treturn false;"
		));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 2));
		assertEquals("multi-line if condition", result.reason());
	}

	@Test
	public void testWithElseFalseTrue() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)",
				"\t\t\treturn false;",
				"\t\telse",
				"\t\t\treturn true;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn !flag;"), result.replacement());
	}

	@Test
	public void testWithElseTrueFalse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (flag)",
				"\t\t\treturn true;",
				"\t\telse",
				"\t\t\treturn false;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn flag;"), result.replacement());
	}
}