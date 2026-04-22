package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteSingleBlankBeforeCloseBrace() {
		final var lines = new ArrayList<>(List.of("\tint x;", "", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhenLineIndexIsBlank() {
		final var lines = new ArrayList<>(List.of("\tint x;", "", "", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhenLineIndexIsBlankTriple() {
		final var lines = new ArrayList<>(List.of("\tint x;", "", "", "", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhenLineIndexIsBlankTripleFromLast() {
		final var lines = new ArrayList<>(List.of("content", "", "", "", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhenLineIndexIsWhitespaceOnly() {
		final var lines = new ArrayList<>(List.of("content", "\t", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhenLineIndexReachesZero() {
		final var lines = new ArrayList<>(List.of("", "", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteWhitespaceOnlyBeforeCloseBrace() {
		final var lines = new ArrayList<>(List.of("\tint x;", "\t", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
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