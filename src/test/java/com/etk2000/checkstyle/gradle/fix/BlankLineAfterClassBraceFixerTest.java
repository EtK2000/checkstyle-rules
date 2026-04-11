package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class BlankLineAfterClassBraceFixerTest {
	private final CheckstyleFixer fixer = new BlankLineAfterClassBraceFixer();

	@Test
	public void testDeleteMixedWhitespaceBlanks() {
		final var lines = new ArrayList<>(List.of("class T {", "", "\t", "  ", "\tint x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteMultipleBlanksAfterClassBrace() {
		final var lines = new ArrayList<>(List.of("class T {", "", "", "\tint x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteSingleBlankAfterClassBrace() {
		final var lines = new ArrayList<>(List.of("class T {", "", "\tint x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteWhitespaceOnlyBlank() {
		final var lines = new ArrayList<>(List.of("class T {", "\t", "\tint x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testEnumKeyword() {
		final var lines = new ArrayList<>(List.of("enum E {", "", "\tA"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testInterfaceKeyword() {
		final var lines = new ArrayList<>(List.of("interface I {", "", "\tvoid f();"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testMultiLineDeclaration() {
		final var lines = new ArrayList<>(List.of("class T", "\textends Base {", "", "\tint x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testNoBlankAfterBrace() {
		final var lines = new ArrayList<>(List.of("class T {", "\tint x;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoBraceFound() {
		final var lines = new ArrayList<>(List.of("class T"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testRecordKeyword() {
		final var lines = new ArrayList<>(List.of("record R(int x) {", "", "}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}
}