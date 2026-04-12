package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DoubleBlankLineFixerTest {
	private final CheckstyleFixer fixer = new DoubleBlankLineFixer();

	@Test
	public void testCollapseDoubleBlankToSingle() {
		final var lines = new ArrayList<>(List.of("int x;", "", "", "int y;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollapseTripleBlankToSingle() {
		final var lines = new ArrayList<>(List.of("int x;", "", "", "", "int y;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLastLineNoBlankAfter() {
		final var lines = new ArrayList<>(List.of("int x;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNextLineNotBlank() {
		final var lines = new ArrayList<>(List.of("int x;", "int y;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSingleBlankNotFixed() {
		final var lines = new ArrayList<>(List.of("int x;", "", "int y;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testWhitespaceOnlyLinesCollapsed() {
		final var lines = new ArrayList<>(List.of("int x;", "\t", "   ", "int y;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}
}