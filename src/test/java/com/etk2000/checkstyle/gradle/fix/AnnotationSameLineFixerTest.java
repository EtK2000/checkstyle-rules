package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t@SuppressWarnings(\"unchecked\") String param"), result.replacement());
	}

	@Test
	public void testInlineReorderAlreadySorted() {
		final var lines = new ArrayList<>(List.of("\tvoid foo(@A @B String param) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testInlineReorderThreeAnnotations() {
		final var lines = new ArrayList<>(List.of("\tvoid foo(@C @A @B String param) {}"));
		final var result = fixer.fix(lines, 0, 13);
		assertNotNull(result);
		assertEquals(List.of("\tvoid foo(@A @B @C String param) {}"), result.replacement());
	}

	@Test
	public void testInlineReorderTwoAnnotations() {
		// @B @A on a parameter line, column points to @A (the out-of-order annotation)
		final var lines = new ArrayList<>(List.of("\tvoid foo(@B @A String param) {}"));
		final var result = fixer.fix(lines, 0, 13);
		assertNotNull(result);
		assertEquals(List.of("\tvoid foo(@A @B String param) {}"), result.replacement());
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\t@A @B String param"), result.replacement());
	}

	@Test
	public void testMergeSingleAnnotationLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A",
				"\t\tString param"
		));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t\t@A String param"), result.replacement());
	}

	@Test
	public void testMergeThreeAnnotationLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A",
				"\t\t@B",
				"\t\t@C",
				"\t\tString param"
		));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\t@A @B @C String param"), result.replacement());
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t\t@A String param"), result.replacement());
	}

	@Test
	public void testSortsAlphabetically() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@C",
				"\t\t@A",
				"\t\t@B",
				"\t\tString param"
		));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t@A @B @C String param"), result.replacement());
	}

	@Test
	public void testTwoAnnotationsOnSameLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t@A @B",
				"\t\tString param"
		));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t@A @B String param"), result.replacement());
	}
}