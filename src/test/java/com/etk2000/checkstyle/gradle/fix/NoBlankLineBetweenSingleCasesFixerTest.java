package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NoBlankLineBetweenSingleCasesFixerTest {
	private final CheckstyleFixer fixer = new NoBlankLineBetweenSingleCasesFixer();

	@Test
	public void testBlankLinesWithWhitespace() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\treturn 1;",
				"\t",
				"\t\tcase B:"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFirstLine() {
		final var lines = new ArrayList<>(List.of("\t\tcase A:"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testMixedBlankAndWhitespaceLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\treturn 1;",
				"",
				"   ",
				"\t",
				"\t\tcase B:"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoBlankLinesAbove() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\treturn 1;",
				"\t\tcase B:"
		));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testRemoveMultipleBlankLines() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\treturn 1;",
				"",
				"",
				"",
				"\t\tcase B:"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSingleBlankLine() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\treturn 1;",
				"",
				"\t\tcase B:"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}