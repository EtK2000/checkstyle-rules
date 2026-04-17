package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AnnotationSameLineFixerTest {
	private final CheckstyleFixer fixer = new AnnotationSameLineFixer();

	@Test
	public void testAnnotationWithParams() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@SuppressWarnings(\"unchecked\")",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t@SuppressWarnings(\"unchecked\") String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testInlineReorderAlreadySorted() {
		final var lines = new ArrayList<>(List.of("\tvoid foo(@A @B String param) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testInlineReorderThreeAnnotations() {
		final var lines = new ArrayList<>(List.of("\tvoid foo(@C @A @B String param) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(List.of("\tvoid foo(@A @B @C String param) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testInlineReorderTwoAnnotations() {
		// @B @A on a parameter line, column points to @A (the out-of-order annotation)
		final var lines = new ArrayList<>(List.of("\tvoid foo(@B @A String param) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(List.of("\tvoid foo(@A @B String param) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testInlineSingleAnnotationReturnsNull() {
		final var lines = new ArrayList<>(List.of("\tvoid foo(@A String param) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testMergeMultipleAnnotationLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A",
				"\t\t@B",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\t@A @B String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMergeSingleAnnotationLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\t@A String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMergeThreeAnnotationLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A",
				"\t\t@B",
				"\t\t@C",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\t@A @B @C String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleAnnotationLinesAtEofReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\t@A", "\t\t@B"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoDeclarationLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\t@A"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\t\t@A", "\t\tString param"));
		assertNull(fixer.fix(lines, -1, 0));
		assertNull(fixer.fix(lines, 2, 0));
	}

	@Test
	public void testPreservesDeclarationIndentation() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\t@A",
				"\t\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t\t@A String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSortsAlphabetically() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@C",
				"\t\t@A",
				"\t\t@B",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t@A @B @C String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTwoAnnotationsOnSameLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A @B",
				"\t\tString param"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t@A @B String param"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}