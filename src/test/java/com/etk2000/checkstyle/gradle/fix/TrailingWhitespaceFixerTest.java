package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TrailingWhitespaceFixerTest {
	private final CheckstyleFixer fixer = new TrailingWhitespaceFixer();

	@Test
	public void testBlankLineWithSpaces() {
		final var lines = new ArrayList<>(List.of("   "));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("", result.replacement().getFirst());
	}

	@Test
	public void testBlankLineWithTab() {
		final var lines = new ArrayList<>(List.of("\t"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("", result.replacement().getFirst());
	}

	@Test
	public void testNoTrailingWhitespace() {
		final var lines = new ArrayList<>(List.of("int x = 5;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testTrailingSpaces() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;   "));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x = 5;", result.replacement().getFirst());
	}

	@Test
	public void testTrailingTab() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;\t"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x = 5;", result.replacement().getFirst());
	}
}