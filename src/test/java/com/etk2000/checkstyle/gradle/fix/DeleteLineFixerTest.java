package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
	}

	@Test
	public void testDeleteLastLine() {
		final var lines = new ArrayList<>(List.of("class Foo {}", "import java.util.List;"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
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