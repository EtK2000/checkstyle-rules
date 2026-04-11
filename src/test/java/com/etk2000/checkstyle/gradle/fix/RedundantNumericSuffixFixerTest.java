package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RedundantNumericSuffixFixerTest {
	private final CheckstyleFixer fixer = new RedundantNumericSuffixFixer();

	@Test
	public void testBinaryLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 0b1010L;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 0b1010;", result.replacement().getFirst());
	}

	@Test
	public void testColumnAtNonLiteralChar() {
		final var lines = new ArrayList<>(List.of("int x = ;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testHexLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 0xFFL;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 0xFF;", result.replacement().getFirst());
	}

	@Test
	public void testInvalidColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("int x = 1;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testLiteralEndsWithNonSuffixLetter() {
		final var lines = new ArrayList<>(List.of("int x = 0xAB;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testLiteralInExpression() {
		final var lines = new ArrayList<>(List.of("\tint x = 10L + 5;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("\tint x = 10 + 5;", result.replacement().getFirst());
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("long x = 100L;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNoSuffixCharacter() {
		final var lines = new ArrayList<>(List.of("int x = 100;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testRemoveDoubleSuffix() {
		final var lines = new ArrayList<>(List.of("double x = 1.0d;"));
		final var result = fixer.fix(lines, 0, 11);
		assertNotNull(result);
		assertEquals("double x = 1.0;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatSuffix() {
		final var lines = new ArrayList<>(List.of("float x = 1f;"));
		final var result = fixer.fix(lines, 0, 10);
		assertNotNull(result);
		assertEquals("float x = 1;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 100L;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 100;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLowercaseLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 100l;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 100;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveUppercaseDoubleSuffix() {
		final var lines = new ArrayList<>(List.of("double x = 2.5D;"));
		final var result = fixer.fix(lines, 0, 11);
		assertNotNull(result);
		assertEquals("double x = 2.5;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveUppercaseFloatSuffix() {
		final var lines = new ArrayList<>(List.of("float x = 3F;"));
		final var result = fixer.fix(lines, 0, 10);
		assertNotNull(result);
		assertEquals("float x = 3;", result.replacement().getFirst());
	}

	@Test
	public void testScientificNotationDoubleSuffix() {
		final var lines = new ArrayList<>(List.of("double x = 1e10d;"));
		final var result = fixer.fix(lines, 0, 11);
		assertNotNull(result);
		assertEquals("double x = 1e10;", result.replacement().getFirst());
	}

	@Test
	public void testUnderscoreLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 1_000L;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 1_000;", result.replacement().getFirst());
	}

	@Test
	public void testZeroLongSuffix() {
		final var lines = new ArrayList<>(List.of("long x = 0L;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("long x = 0;", result.replacement().getFirst());
	}
}