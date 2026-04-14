package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class DeleteLineFixerTest {
	private final CheckstyleFixer fixer = new DeleteLineFixer();

	@Test
	public void testDeleteFirstLine() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "class Foo {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteFirstLineBlankBelow() {
		final var lines = new ArrayList<>(List.of("import A;", "", "class T {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteImportBlankAboveOnly() {
		final var lines = new ArrayList<>(List.of("import A;", "", "import B;", "import C;"));
		final var result = fixer.fix(lines, 2, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteImportBlankBelowOnly() {
		final var lines = new ArrayList<>(List.of("import A;", "import B;", "", "import C;"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteLastLine() {
		final var lines = new ArrayList<>(List.of("class Foo {}", "import java.util.List;"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteLastLineBlankAbove() {
		final var lines = new ArrayList<>(List.of("import A;", "", "import B;"));
		final var result = fixer.fix(lines, 2, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
	}

	@Test
	public void testDeleteMiddleLine() {
		final var lines = new ArrayList<>(List.of("line1", "line2", "line3"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDeleteOrphanedImportBlankAboveAndBelow() {
		final var lines = new ArrayList<>(List.of("import A;", "", "import B;", "", "import C;"));
		final var result = fixer.fix(lines, 2, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("single line"));
		assertNull(fixer.fix(lines, 5, 0));
	}

	@Test
	public void testNegativeLineIndex() {
		final var lines = new ArrayList<>(List.of("single line"));
		assertNull(fixer.fix(lines, -1, 0));
	}
}