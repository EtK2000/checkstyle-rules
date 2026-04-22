package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ConstructorAssignmentOrderFixerTest {
	private final CheckstyleFixer fixer = new ConstructorAssignmentOrderFixer();

	@Test
	public void testAlphabeticalSimple() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha, int beta) {",
				"\t\tthis.beta = beta;",
				"\t\tthis.alpha = alpha;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tthis.alpha = alpha;", "\t\tthis.beta = beta;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAlphabeticalThreeFields() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int a, int b, int c) {",
				"\t\tthis.gamma = c;",
				"\t\tthis.alpha = a;",
				"\t\tthis.beta = b;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\t\tthis.alpha = a;", "\t\tthis.beta = b;", "\t\tthis.gamma = c;"),
				result.replacement()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAlreadySorted() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha, int beta) {",
				"\t\tthis.alpha = alpha;",
				"\t\tthis.beta = beta;",
				"\t}"
		));
		assertNull(fixer.fix(lines, 2, 0));
	}

	@Test
	public void testCircularDependencyDoesNotHang() {
		final var lines = new ArrayList<>(List.of(
				"\tT() {",
				"\t\tthis.alpha = this.beta + 1;",
				"\t\tthis.beta = this.alpha + 1;",
				"\t}"
		));
		fixer.fix(lines, 1, 0);
	}

	@Test
	public void testDependencyPreservesOrder() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha) {",
				"\t\tthis.alpha = alpha;",
				"\t\tthis.beta = this.alpha + 1;",
				"\t}"
		));
		assertNull(fixer.fix(lines, 2, 0));
	}

	@Test
	public void testDependencyViolationSwap() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha) {",
				"\t\tthis.beta = this.alpha + 1;",
				"\t\tthis.alpha = alpha;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(
				List.of("\t\tthis.alpha = alpha;", "\t\tthis.beta = this.alpha + 1;"),
				result.replacement()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLineIndexNegative() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tT() { this.a = 1; }")), -1, 0));
	}

	@Test
	public void testLineIndexOutOfBounds() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\tT() { this.a = 1; }")), 5, 0));
	}

	@Test
	public void testMultiLineAlphabetical() {
		final var lines = new ArrayList<>(List.of(
				"\tT(Object a, Object b) {",
				"\t\tthis.beta = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn b.toString();",
				"\t\t\t}",
				"\t\t};",
				"\t\tthis.alpha = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn a.toString();",
				"\t\t\t}",
				"\t\t};",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 7, 0));
		assertEquals(1, result.startLine());
		assertEquals(12, result.endLine());
		final var expected = List.of(
				"\t\tthis.alpha = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn a.toString();",
				"\t\t\t}",
				"\t\t};",
				"\t\tthis.beta = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn b.toString();",
				"\t\t\t}",
				"\t\t};"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineBeforeSimple() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha, Object beta) {",
				"\t\tthis.beta = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn beta.toString();",
				"\t\t\t}",
				"\t\t};",
				"\t\tthis.alpha = alpha;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 7, 0));
		assertEquals(1, result.startLine());
		assertEquals(7, result.endLine());
		final var expected = List.of(
				"\t\tthis.alpha = alpha;",
				"",
				"\t\tthis.beta = new Object() {",
				"\t\t\t@Override",
				"\t\t\tpublic String toString() {",
				"\t\t\t\treturn beta.toString();",
				"\t\t\t}",
				"\t\t};"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoBodyEnd() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int a, int b) {",
				"\t\tthis.beta = b;",
				"\t\tthis.alpha = a;"
		));
		assertNull(fixer.fix(lines, 2, 0));
	}

	@Test
	public void testNoBodyStart() {
		assertNull(fixer.fix(new ArrayList<>(List.of("\t\tthis.alpha = 1;")), 0, 0));
	}

	@Test
	public void testPartialVarNameNotMatched() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int x) {",
				"\t\tthis.beta = x;",
				"\t\tthis.alpha = x;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(List.of("\t\tthis.alpha = x;", "\t\tthis.beta = x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrimitiveArrayLocalVar() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int[] src) {",
				"\t\tint[] arr = src;",
				"\t\tthis.beta = arr;",
				"\t\tthis.alpha = 1;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		final var expected = List.of(
				"\t\tthis.alpha = 1;",
				"",
				"\t\tint[] arr = src;",
				"\t\tthis.beta = arr;"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrimitiveLocalVar() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int x) {",
				"\t\tint size = x + 1;",
				"\t\tthis.beta = size;",
				"\t\tthis.alpha = x;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		final var expected = List.of(
				"\t\tthis.alpha = x;",
				"",
				"\t\tint size = x + 1;",
				"\t\tthis.beta = size;"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSingleAssignment() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int alpha) {",
				"\t\tthis.alpha = alpha;",
				"\t}"
		));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testStringWithBraces() {
		final var lines = new ArrayList<>(List.of(
				"\tT(String a) {",
				"\t\tthis.beta = \"{}\";",
				"\t\tthis.alpha = a;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(List.of("\t\tthis.alpha = a;", "\t\tthis.beta = \"{}\";"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testVarAlphabeticalWithinSubGroup() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int x) {",
				"\t\tfinal var computed = x * 2;",
				"\t\tthis.beta = computed;",
				"\t\tthis.alpha = computed + 1;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		final var expected = List.of(
				"\t\tfinal var computed = x * 2;",
				"\t\tthis.alpha = computed + 1;",
				"\t\tthis.beta = computed;"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testVarBeforeSimple() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int x) {",
				"\t\tfinal var computed = x * 2;",
				"\t\tthis.alpha = computed;",
				"\t\tthis.beta = x;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		final var expected = List.of(
				"\t\tthis.beta = x;",
				"",
				"\t\tfinal var computed = x * 2;",
				"\t\tthis.alpha = computed;"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testVarGroupOrder() {
		final var lines = new ArrayList<>(List.of(
				"\tT(int x) {",
				"\t\tfinal var first = x + 1;",
				"\t\tfinal var second = x + 2;",
				"\t\tthis.beta = second;",
				"\t\tthis.alpha = first;",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		final var expected = List.of(
				"\t\tfinal var first = x + 1;",
				"\t\tthis.alpha = first;",
				"",
				"\t\tfinal var second = x + 2;",
				"\t\tthis.beta = second;"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}