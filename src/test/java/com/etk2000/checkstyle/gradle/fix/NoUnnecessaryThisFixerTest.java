package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NoUnnecessaryThisFixerTest {
	private final CheckstyleFixer fixer = new NoUnnecessaryThisFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("this.x;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testColumnTooSmallForThis() {
		final var lines = new ArrayList<>(List.of("x.y;"));
		assertNull(fixer.fix(lines, 0, 1));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("this.x;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNotThis() {
		final var lines = new ArrayList<>(List.of("\t\treturn that.field;"));
		assertNull(fixer.fix(lines, 0, 13));
	}

	@Test
	public void testRemoveThis() {
		final var lines = new ArrayList<>(List.of("\t\treturn this.field;"));
		final var result = fixer.fix(lines, 0, 13);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn field;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveThisAtStartOfLine() {
		final var lines = new ArrayList<>(List.of("this.method();"));
		final var result = fixer.fix(lines, 0, 4);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("method();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveThisInExpression() {
		final var lines = new ArrayList<>(List.of("\t\tint x = this.value + 1;"));
		final var result = fixer.fix(lines, 0, 14);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tint x = value + 1;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveThisInMethodCall() {
		final var lines = new ArrayList<>(List.of("\t\tthis.doSomething();"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tdoSomething();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveThisInParens() {
		final var lines = new ArrayList<>(List.of("foo(this.bar);"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("foo(bar);", result.replacement().getFirst());
	}
}