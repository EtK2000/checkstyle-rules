package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BlankLineBeforeClosingBraceFixerTest {
	private final CheckstyleFixer fixer = new BlankLineBeforeClosingBraceFixer();

	@Test
	public void testDeleteMultipleBlanksBeforeCloseBrace() {
		final var lines = new ArrayList<>(List.of("\tint x;", "", "", "}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteSingleBlankBeforeCloseBrace() {
		final var lines = new ArrayList<>(List.of("\tint x;", "", "}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteWhitespaceOnlyBeforeCloseBrace() {
		final var lines = new ArrayList<>(List.of("\tint x;", "\t", "}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testLastLine() {
		final var lines = new ArrayList<>(List.of("\tint x;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNextLineNotBlank() {
		final var lines = new ArrayList<>(List.of("\tint x;", "\tint y;"));
		assertNull(fixer.fix(lines, 0, 0));
	}
}