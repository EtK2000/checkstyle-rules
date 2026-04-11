package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FinalLocalVariableFixerTest {
	private final CheckstyleFixer fixer = new FinalLocalVariableFixer();

	@Test
	public void testAddFinalAfterSingleTab() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tfinal int x = 5;", result.replacement().getFirst());
	}

	@Test
	public void testAddFinalAfterTwoTabs() {
		final var lines = new ArrayList<>(List.of("\t\tvar x = getSomething();"));
		final var result = fixer.fix(lines, 0, 10);
		assertNotNull(result);
		assertEquals("\t\tfinal var x = getSomething();", result.replacement().getFirst());
	}

	@Test
	public void testAddFinalNoIndentation() {
		final var lines = new ArrayList<>(List.of("String s = \"hello\";"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("final String s = \"hello\";", result.replacement().getFirst());
	}

	@Test
	public void testAlreadyFinalSkipped() {
		final var lines = new ArrayList<>(List.of("\t\tfinal int x = 5;"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testMultiDeclarationSecondViolationSkipped() {
		final var lines = new ArrayList<>(List.of("\t\tfinal int x, y;"));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testSpaceIndentation() {
		final var lines = new ArrayList<>(List.of("    int x = 5;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("    final int x = 5;", result.replacement().getFirst());
	}
}