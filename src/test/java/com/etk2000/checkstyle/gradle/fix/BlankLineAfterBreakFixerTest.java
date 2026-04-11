package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BlankLineAfterBreakFixerTest {
	private final CheckstyleFixer fixer = new BlankLineAfterBreakFixer();

	@Test
	public void testAlreadyHasBlankLine() {
		final var lines = new ArrayList<>(List.of("\t\tbreak;", "", "\t\tcase 2:"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testInsertBlankBeforeCase() {
		final var lines = new ArrayList<>(List.of("\t\tbreak;", "\t\tcase 2:"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("", result.replacement().getFirst());
	}

	@Test
	public void testInsertBlankBeforeDefault() {
		final var lines = new ArrayList<>(List.of("\t\tbreak;", "\t\tdefault:"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("", result.replacement().getFirst());
	}

	@Test
	public void testNoNextLine() {
		final var lines = new ArrayList<>(List.of("\t\tbreak;"));
		assertNull(fixer.fix(lines, 0, 0));
	}
}