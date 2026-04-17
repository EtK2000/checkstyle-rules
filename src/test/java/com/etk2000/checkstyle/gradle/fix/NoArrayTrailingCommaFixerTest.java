package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class NoArrayTrailingCommaFixerTest {
	private final CheckstyleFixer fixer = new NoArrayTrailingCommaFixer();

	@Test
	public void testCommaAtStartOfLine() {
		final var lines = new ArrayList<>(List.of(",}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("}", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaBeforeClosingBrace() {
		final var lines = new ArrayList<>(List.of("\tint[] a = {1, 2,}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 16));
		assertEquals("\tint[] a = {1, 2}", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testInvalidColumnNotComma() {
		final var lines = new ArrayList<>(List.of("int[] a = {1, 2};"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testInvalidColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("int[] a = {};"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("int[] a = {1,};"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testRemoveTrailingCommaInline() {
		final var lines = new ArrayList<>(List.of("int[] a = {1, 2,};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals("int[] a = {1, 2};", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveTrailingCommaMultiline() {
		final var lines = new ArrayList<>(List.of("\t\t2,"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 3));
		assertEquals("\t\t2", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveTrailingCommaWithSpaceBefore() {
		final var lines = new ArrayList<>(List.of("\t1 ,"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 3));
		assertEquals("\t1", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRemoveTrailingCommaWithTrailingWhitespace() {
		final var lines = new ArrayList<>(List.of("\t1,  "));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals("\t1", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSingleElementArray() {
		final var lines = new ArrayList<>(List.of("int[] a = {1,};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals("int[] a = {1};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}
}