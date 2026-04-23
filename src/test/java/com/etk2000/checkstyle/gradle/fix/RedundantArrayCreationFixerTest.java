package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RedundantArrayCreationFixerTest {
	private final CheckstyleFixer fixer = new RedundantArrayCreationFixer();

	@Test
	public void testCharLiteralWithBraces() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{'}'});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList('}');"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmptyArrayOnlyArgument() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList();"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmptyArrayWithPrecedingArg() {
		final var lines = new ArrayList<>(List.of("\tCollections.addAll(list, new String[]{});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 26));
		assertEquals(List.of("\tCollections.addAll(list);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEscapedQuoteInString() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a\\\"b}c\"});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList(\"a\\\"b}c\");"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultilineReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\tArrays.asList(new Object[]{",
				"\t\t\"a\", \"b\"",
				"\t});"
		));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testMultipleElements() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a\", \"b\", \"c\"});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList(\"a\", \"b\", \"c\");"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNestedParensInElements() {
		final var lines = new ArrayList<>(List.of("\tString.format(\"%s\", new Object[]{foo(1, 2)});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 21));
		assertEquals(List.of("\tString.format(\"%s\", foo(1, 2));"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoBraceOnLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]"));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testOutOfBoundsColumn() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a\"});"));
		assertNull(fixer.fix(lines, 0, -1));
		assertNull(fixer.fix(lines, 0, 100));
	}

	@Test
	public void testOutOfBoundsLineIndex() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a\"});"));
		assertNull(fixer.fix(lines, -1, 0));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testSingleElement() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a\"});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList(\"a\");"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringLiteralsWithBraces() {
		final var lines = new ArrayList<>(List.of("\tArrays.asList(new Object[]{\"a{b}c\"});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(List.of("\tArrays.asList(\"a{b}c\");"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWithPrecedingArguments() {
		final var lines = new ArrayList<>(List.of("\tString.format(\"%s%s\", new Object[]{\"a\", \"b\"});"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 23));
		assertEquals(List.of("\tString.format(\"%s%s\", \"a\", \"b\");"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}