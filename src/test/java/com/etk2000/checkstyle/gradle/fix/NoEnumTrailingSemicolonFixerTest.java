package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NoEnumTrailingSemicolonFixerTest {
	private final CheckstyleFixer fixer = new NoEnumTrailingSemicolonFixer();

	@Test
	public void testInvalidColumnNegative() {
		final var lines = new ArrayList<>(List.of("\tX;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testInvalidColumnNotSemicolon() {
		final var lines = new ArrayList<>(List.of("\tX;"));
		assertNull(fixer.fix(lines, 0, 1));
	}

	@Test
	public void testInvalidColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\tX;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testRemoveSemicolonAfterBrace() {
		final var lines = new ArrayList<>(List.of("\t};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\t}", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonBeforeComment() {
		final var lines = new ArrayList<>(List.of("\tX; // some comment"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tX // some comment", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonCollapsesDoubleSpace() {
		final var lines = new ArrayList<>(List.of("enum A { ; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("enum A { }", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonInline() {
		final var lines = new ArrayList<>(List.of("enum A { X; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("enum A { X }", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonOnConstantLine() {
		final var lines = new ArrayList<>(List.of("\tGAMMA;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tGAMMA", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonOnlyOnLine() {
		final var lines = new ArrayList<>(List.of("\t;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.replacement().isEmpty(), "line should be deleted when only semicolon on line");
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveSemicolonWithTrailingWhitespace() {
		final var lines = new ArrayList<>(List.of("\tX;  "));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tX", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}
}