package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AvoidNoArgumentSuperCallFixerTest {
	private final CheckstyleFixer fixer = new AvoidNoArgumentSuperCallFixer();

	@Test
	public void testDeleteSuperCall() {
		final var lines = new ArrayList<>(List.of("\t\tsuper();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteSuperCallNoIndent() {
		final var lines = new ArrayList<>(List.of("super();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("super();"));
		assertNull(fixer.fix(lines, 5, 0));
	}

	@Test
	public void testNegativeLineIndex() {
		final var lines = new ArrayList<>(List.of("super();"));
		assertNull(fixer.fix(lines, -1, 0));
	}

	@Test
	public void testSkipSuperWithArguments() {
		final var lines = new ArrayList<>(List.of("\t\tsuper(arg);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSkipSuperWithComment() {
		final var lines = new ArrayList<>(List.of("\t\tsuper(); // needed"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSkipSuperWithExtraCode() {
		final var lines = new ArrayList<>(List.of("\t\tsuper(); x = 1;"));
		assertNull(fixer.fix(lines, 0, 0));
	}
}