package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RedundantAnnotationSyntaxFixerTest {
	private final CheckstyleFixer fixer = new RedundantAnnotationSyntaxFixer();

	@Test
	public void testEmptyParens() {
		final var lines = new ArrayList<>(List.of("\t@Override()"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@Override"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmptyParensBeforeDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@Override() void f() {}"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@Override void f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmptyParensQualified() {
		final var lines = new ArrayList<>(List.of("\t@javax.annotation.Nonnull()"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@javax.annotation.Nonnull"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitValue() {
		final var lines = new ArrayList<>(List.of("\t@A(value = \"x\")"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@A(\"x\")"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitValueArray() {
		final var lines = new ArrayList<>(List.of("\t@A(value = {\"x\", \"y\"})"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@A({\"x\", \"y\"})"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitValueNestedAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@A(value = @B)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@A(@B)"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitValueNoSpaces() {
		final var lines = new ArrayList<>(List.of("\t@A(value=\"x\")"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@A(\"x\")"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitValueNoSpacesQualified() {
		final var lines = new ArrayList<>(List.of("\t@com.example.A(value=\"x\")"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(List.of("\t@com.example.A(\"x\")"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineEmptyParens() {
		final var lines = new ArrayList<>(List.of("\t@B(", "\t)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineEmptyParensBlankBetween() {
		final var lines = new ArrayList<>(List.of("\t@B(", "", "\t)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineEmptyParensContentAfterClose() {
		final var lines = new ArrayList<>(List.of("\t@B(", "\t) // comment"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@B // comment"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineExplicitValue() {
		final var lines = new ArrayList<>(List.of("\t@A(", "\t\tvalue = \"x\"", "\t)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\t\"x\""), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineExplicitValueBlankBefore() {
		final var lines = new ArrayList<>(List.of("\t@A(", "", "\t\tvalue = \"x\"", "\t)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\t\"x\""), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineExplicitValueNoSpaces() {
		final var lines = new ArrayList<>(List.of("\t@A(", "\t\tvalue=\"x\"", "\t)"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\t\"x\""), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineNotEmptyParensReturnsNull() {
		// line ends with ( but next line has content (not just )), so not empty parens
		// falls through to explicit value scan, which also doesn't match
		final var lines = new ArrayList<>(List.of("\t@A(", "\t\t\"x\"", "\t)"));
		assertNull(fixer.fix(lines, 0, 1));
	}

	@Test
	public void testNoMatchNonAnnotationLine() {
		final var lines = new ArrayList<>(List.of("\tvoid f() {}"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\t@A()"));
		assertNull(fixer.fix(lines, -1, 0));
		assertNull(fixer.fix(lines, 1, 0));
	}
}