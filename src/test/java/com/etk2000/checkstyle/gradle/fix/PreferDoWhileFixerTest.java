package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferDoWhileFixerTest {
	private final CheckstyleFixer fixer = new PreferDoWhileFixer();

	@Test
	public void testAssignmentBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\ti = i + 1;",
				"\t\twhile (i < 10)",
				"\t\t\ti = i + 1;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tdo i = i + 1;", "\t\twhile (i < 10);"), result.replacement());
	}

	@Test
	public void testBracedBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10) {",
				"\t\t\t++i;",
				"\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tdo ++i;", "\t\twhile (i < 10);"), result.replacement());
	}

	@Test
	public void testBracedBodyMissingClosingLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10) {",
				"\t\t\t++i;"
		));
		assertNull(fixer.fix(lines, 1, 16));
	}

	@Test
	public void testBracedClosingIndentMismatchSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10) {",
				"\t\t\t++i;",
				"\t}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testChainedAssignmentBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\tnode = node.next();",
				"\t\twhile (node != null)",
				"\t\t\tnode = node.next();"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(List.of("\t\tdo node = node.next();", "\t\twhile (node != null);"), result.replacement());
	}

	@Test
	public void testCommentOnBodyLineSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10)",
				"\t\t\t++i; // comment"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testCommentOnPreLineSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i; // comment",
				"\t\twhile (i < 10)",
				"\t\t\t++i;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testCompoundAssignBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\ti += 2;",
				"\t\twhile (i < 100)",
				"\t\t\ti += 2;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(List.of("\t\tdo i += 2;", "\t\twhile (i < 100);"), result.replacement());
	}

	@Test
	public void testConditionWithNestedParens() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile ((i < 10) && check(i))",
				"\t\t\t++i;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(List.of("\t\tdo ++i;", "\t\twhile ((i < 10) && check(i));"), result.replacement());
	}

	@Test
	public void testIndentMismatchSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t++i;",
				"\t\twhile (i < 10)",
				"\t\t\t++i;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testMethodCallBody() {
		final var lines = new ArrayList<>(List.of(
				"\t\tlist.add(1);",
				"\t\twhile (list.size() < 10)",
				"\t\t\tlist.add(1);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(List.of("\t\tdo list.add(1);", "\t\twhile (list.size() < 10);"), result.replacement());
	}

	@Test
	public void testPrefixDecrement() {
		final var lines = new ArrayList<>(List.of(
				"\t\t--i;",
				"\t\twhile (i > 0)",
				"\t\t\t--i;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(List.of("\t\tdo --i;", "\t\twhile (i > 0);"), result.replacement());
	}

	@Test
	public void testPrefixIncrement() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10)",
				"\t\t\t++i;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 16));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tdo ++i;", "\t\twhile (i < 10);"), result.replacement());
	}

	@Test
	public void testPreStmtAtFileStart() {
		final var lines = new ArrayList<>(List.of(
				"\t\twhile (i < 10)",
				"\t\t\t++i;"
		));
		assertNull(fixer.fix(lines, 0, 16));
	}

	@Test
	public void testPreStmtExtraWhitespaceAfterIndentSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t \t++i;",
				"\t\twhile (i < 10)",
				"\t\t\t++i;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testTextualMismatchSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10)",
				"\t\t\t++j;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testUnexpectedBracedClosingSkipped() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10) {",
				"\t\t\t++i;",
				"\t\t\t++i;",
				"\t\t}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testWhileIsLastLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10)"
		));
		assertNull(fixer.fix(lines, 1, 16));
	}

	@Test
	public void testWhileLineFormatNotMatched() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\tnotAWhile (i < 10)",
				"\t\t\t++i;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}

	@Test
	public void testWhileLineHasTrailingComment() {
		final var lines = new ArrayList<>(List.of(
				"\t\t++i;",
				"\t\twhile (i < 10) // loop",
				"\t\t\t++i;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 1, 16));
	}
}