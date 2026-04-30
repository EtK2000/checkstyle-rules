package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RedundantEqualityBranchFixerTest {
	private final CheckstyleFixer fixer = new RedundantEqualityBranchFixer();

	@Test
	public void testAssignBareCollapse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignBareCollapseIfOnFirstLineWithTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignBareCollapseNotEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\tr = a;"), result.replacement());
	}

	@Test
	public void testAssignBareCollapseNotEqualHintNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\tr = c;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAssignDeclAndReturnCollapse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\treturn b;"), result.replacement());
	}

	@Test
	public void testAssignDeclAndReturnCollapseNotEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a != b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\treturn a;"), result.replacement());
	}

	@Test
	public void testAssignDeclLineDoesNotMatchDecl() {
		final var lines = new ArrayList<>(List.of(
				"\t\tx.something();",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignDeclTargetMismatch() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int s;",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignDeclWithoutTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignDifferentTargets() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\ts = b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAssignDifferentTargetsTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn s;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignElseBodyNotAssign() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tthrow new RuntimeException();"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAssignNoElseLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\tr = b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAssignNoElseLineFourLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\tr = b;",
				"\t\tr = c;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAssignTrailingLineNotReturnVar() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r + 1;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\t\tr = b;"), result.replacement());
	}

	@Test
	public void testAssignTruncatedAfterIf() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = a;",
				"\t\telse"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchHintNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tr = c;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchIfIsLastLine() {
		final var lines = new ArrayList<>(List.of("\t\tif (a == b)"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchNonEqualityCondition() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchNonIfLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\treturn a;",
				"\t\treturn b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchThenIsThrow() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\tthrow new RuntimeException();",
				"\t\treturn b;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnFourLinesNoElseUsesTrailingArm() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;",
				"\t\treturn b;",
				"\t\t// extra line"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn b;"), result.replacement());
	}

	@Test
	public void testReturnIfElseBodyNotReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\tthrow new RuntimeException();"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnIfElseEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn b;"), result.replacement());
	}

	@Test
	public void testReturnIfElseHintNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn 42;",
				"\t\telse",
				"\t\t\treturn 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnIfElseHintNullNotEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\treturn 42;",
				"\t\telse",
				"\t\t\treturn 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnIfElseNotEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn a;"), result.replacement());
	}

	@Test
	public void testReturnNoTrailingStatement() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnTrailingEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;",
				"\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn b;"), result.replacement());
	}

	@Test
	public void testReturnTrailingHintNull() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\treturn 42;",
				"\t\treturn 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnTrailingHintNullEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn 42;",
				"\t\treturn 0;"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testReturnTrailingNotEqual() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a != b)",
				"\t\t\treturn a;",
				"\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn a;"), result.replacement());
	}

	@Test
	public void testReturnTrailingNotReturnStatement() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a == b)",
				"\t\t\treturn a;",
				"\t\tx.something();"
		));
		assertNull(fixer.fix(lines, 0, 0));
	}
}