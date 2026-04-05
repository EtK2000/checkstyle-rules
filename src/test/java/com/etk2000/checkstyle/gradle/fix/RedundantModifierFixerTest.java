package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class RedundantModifierFixerTest {
	private final CheckstyleFixer fixer = new RedundantModifierFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\tpublic void method();"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\tpublic void method();"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNotLetterAtColumn() {
		final var lines = new ArrayList<>(List.of("\t123 method();"));
		assertNull(fixer.fix(lines, 0, 1));
	}

	@Test
	public void testRemoveAbstract() {
		final var lines = new ArrayList<>(List.of("\tabstract void method();"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals("\tvoid method();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveAtEndOfLineNoTrailingSpace() {
		final var lines = new ArrayList<>(List.of("int abstract"));
		final var result = fixer.fix(lines, 0, 4);
		assertNotNull(result);
		assertEquals("int ", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinal() {
		final var lines = new ArrayList<>(List.of("\tpublic static final int X = 1;"));
		final var result = fixer.fix(lines, 0, 15);
		assertNotNull(result);
		assertEquals("\tpublic static int X = 1;", result.replacement().getFirst());
	}

	@Test
	public void testRemovePublicFromInterface() {
		final var lines = new ArrayList<>(List.of("\tpublic void method();"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals("\tvoid method();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveStaticFromInterfaceField() {
		final var lines = new ArrayList<>(List.of("\tstatic final int X = 1;"));
		final var result = fixer.fix(lines, 0, 1);
		assertNotNull(result);
		assertEquals("\tfinal int X = 1;", result.replacement().getFirst());
	}

	@Test
	public void testResultIsBlankDeletesLine() {
		final var lines = new ArrayList<>(List.of("public"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertTrue(result.replacement().isEmpty());
	}
}