package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class UpperEllFixerTest {
	private final CheckstyleFixer fixer = new UpperEllFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("long x = 100l;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testHexLiteral() {
		final var lines = new ArrayList<>(List.of("long x = 0xFFl;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 0xFFL;", result.replacement().getFirst());
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("long x = 100l;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNoLiteralCharsAtColumn() {
		final var lines = new ArrayList<>(List.of("long x = ;"));
		assertNull(fixer.fix(lines, 0, 9));
	}

	@Test
	public void testNotLowercaseL() {
		final var lines = new ArrayList<>(List.of("long x = 100L;"));
		assertNull(fixer.fix(lines, 0, 9));
	}

	@Test
	public void testReplaceLowercaseL() {
		final var lines = new ArrayList<>(List.of("long x = 100l;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 100L;", result.replacement().getFirst());
	}

	@Test
	public void testUnderscoreLiteral() {
		final var lines = new ArrayList<>(List.of("long x = 1_000_000l;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 1_000_000L;", result.replacement().getFirst());
	}
}