package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FinalLocalVariableFixerTest {
	private final CheckstyleFixer fixer = new FinalLocalVariableFixer();

	@Test
	public void testAddFinalAfterSingleTab() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfinal int x = 5;", result.replacement().getFirst());
	}

	@Test
	public void testAddFinalAfterTwoTabs() {
		final var lines = new ArrayList<>(List.of("\t\tvar x = getSomething();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tfinal var x = getSomething();", result.replacement().getFirst());
	}

	@Test
	public void testAddFinalNoIndentation() {
		final var lines = new ArrayList<>(List.of("String s = \"hello\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
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
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("    final int x = 5;", result.replacement().getFirst());
	}
}