package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

public class LineDeletionTest {
	@Test
	public void testBlankAboveBlankBelowCollapses() {
		final var lines = List.of("", "alias", "");
		final var result = LineDeletion.deleteRange(lines, 1, 1);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankAboveContentBelowDeletesLineOnly() {
		final var lines = List.of("", "alias", "code");
		final var result = LineDeletion.deleteRange(lines, 1, 1);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContentAboveBlankBelowDeletesLineOnly() {
		final var lines = List.of("code", "alias", "");
		final var result = LineDeletion.deleteRange(lines, 1, 1);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFirstLineWithBlankBelowDeletesLineOnly() {
		final var lines = List.of("alias", "");
		final var result = LineDeletion.deleteRange(lines, 0, 0);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testImportsToAddPropagated() {
		final var lines = List.of("code", "alias", "code");
		final var result = LineDeletion.deleteRange(lines, 1, 1, Set.of("static foo.Bar.X"));
		assertEquals(Set.of("static foo.Bar.X"), result.importsToAdd());
	}

	@Test
	public void testInvalidRangeReturnsNull() {
		final var lines = List.of("a", "b", "c");
		assertNull(LineDeletion.deleteRange(lines, -1, 0));
		assertNull(LineDeletion.deleteRange(lines, 0, -1));
		assertNull(LineDeletion.deleteRange(lines, 5, 6));
		assertNull(LineDeletion.deleteRange(lines, 2, 1));
	}

	@Test
	public void testLastLineWithBlankAboveDeletesLineOnly() {
		final var lines = List.of("", "alias");
		final var result = LineDeletion.deleteRange(lines, 1, 1);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiLineRangeCollapsesAdjacentBlanks() {
		final var lines = List.of("code", "", "line1", "line2", "", "code");
		final var result = LineDeletion.deleteRange(lines, 2, 3);
		assertEquals(2, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoBlanksDeletesLineOnly() {
		final var lines = List.of("a", "alias", "c");
		final var result = LineDeletion.deleteRange(lines, 1, 1);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRangeEndingAtLastLineDoesNotCollapse() {
		final var lines = List.of("a", "", "x", "y");
		final var result = LineDeletion.deleteRange(lines, 2, 3);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
	}

	@Test
	public void testRangeStartingAtFirstLineDoesNotCollapse() {
		final var lines = List.of("a", "b", "c", "", "d");
		final var result = LineDeletion.deleteRange(lines, 0, 2);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void testWhitespaceOnlyLinesAreTreatedAsBlank() {
		// LineDeletion uses isBlank() not isEmpty(), so tab/space-only lines collapse too
		final var lines = List.of("code", "\t\t", "alias", "    ", "code");
		final var result = LineDeletion.deleteRange(lines, 2, 2);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
	}
}