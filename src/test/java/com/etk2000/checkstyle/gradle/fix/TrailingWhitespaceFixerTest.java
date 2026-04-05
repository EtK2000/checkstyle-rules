package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TrailingWhitespaceFixerTest {
	private final CheckstyleFixer fixer = new TrailingWhitespaceFixer();

	@Test
	public void testBlankLineWithSpaces() {
		final var lines = new ArrayList<>(List.of("   "));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("", result.replacement().getFirst());
	}

	@Test
	public void testBlankLineWithTab() {
		final var lines = new ArrayList<>(List.of("\t"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\tint x = 5;", result.replacement().getFirst());
	}

	@Test
	public void testTrailingTab() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;\t"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\tint x = 5;", result.replacement().getFirst());
	}
}