package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TrailingNewlineFixerTest {
	private final CheckstyleFixer fixer = new TrailingNewlineFixer();

	@Test
	public void testAllBlankLines() {
		final var lines = new ArrayList<>(List.of("", ""));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteMultipleTrailingEmptyLines() {
		final var lines = new ArrayList<>(List.of("class T {}", "", ""));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteSingleTrailingEmptyLine() {
		final var lines = new ArrayList<>(List.of("class T {}", ""));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteTrailingWhitespaceOnlyLine() {
		final var lines = new ArrayList<>(List.of("class T {}", "\t"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLastLineHasContent() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		assertNull(fixer.fix(lines, 0, 0));
	}
}