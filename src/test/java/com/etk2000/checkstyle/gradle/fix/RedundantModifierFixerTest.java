package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveAtEndOfLineNoTrailingSpace() {
		final var lines = new ArrayList<>(List.of("int abstract"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 4));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("int ", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinal() {
		final var lines = new ArrayList<>(List.of("\tpublic static final int X = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 15));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tpublic static int X = 1;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromCatch() {
		final var lines = new ArrayList<>(List.of("\t\tcatch (final Exception e) {"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tcatch (Exception e) {", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromCatchMulti() {
		final var lines = new ArrayList<>(List.of("\t\tcatch (final RuntimeException | Error e) {"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tcatch (RuntimeException | Error e) {", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromConstructor() {
		final var lines = new ArrayList<>(List.of("\tT(final int x) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 3));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tT(int x) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromForEach() {
		final var lines = new ArrayList<>(List.of("\t\tfor (final var item : list)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tfor (var item : list)", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromForEachAnnotated() {
		final var lines = new ArrayList<>(List.of("\t\tfor (@SuppressWarnings(\"unused\") final var item : list)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 35));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tfor (@SuppressWarnings(\"unused\") var item : list)", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromForInit() {
		final var lines = new ArrayList<>(List.of("\t\tfor (final var size = list.size(); size > 0;)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tfor (var size = list.size(); size > 0;)", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromForInitMulti() {
		final var lines = new ArrayList<>(List.of("\t\tfor (final int i = 0, size = 10; i < size;)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tfor (int i = 0, size = 10; i < size;)", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromLambda() {
		final var lines = new ArrayList<>(List.of("\t\tlist.sort((final String a, final String b) -> a.compareTo(b));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tlist.sort((String a, final String b) -> a.compareTo(b));", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromParameterAnnotatedAfter() {
		final var lines = new ArrayList<>(List.of("\tvoid method(final @SuppressWarnings(\"unused\") int x) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(@SuppressWarnings(\"unused\") int x) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromParameterAnnotatedBefore() {
		final var lines = new ArrayList<>(List.of("\tvoid method(@SuppressWarnings(\"unused\") final String s) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 41));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(@SuppressWarnings(\"unused\") String s) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromParameterFirst() {
		final var lines = new ArrayList<>(List.of("\tvoid method(final int x, String y) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(int x, String y) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromParameterSecond() {
		final var lines = new ArrayList<>(List.of("\tvoid method(int x, final String y) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 20));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(int x, String y) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromParameterSingle() {
		final var lines = new ArrayList<>(List.of("\tvoid method(final int x) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(int x) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFinalFromVarargs() {
		final var lines = new ArrayList<>(List.of("\tvoid method(final String... args) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method(String... args) {}", result.replacement().getFirst());
	}

	@Test
	public void testRemovePublicFromInterface() {
		final var lines = new ArrayList<>(List.of("\tpublic void method();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tvoid method();", result.replacement().getFirst());
	}

	@Test
	public void testRemoveStaticFromInterfaceField() {
		final var lines = new ArrayList<>(List.of("\tstatic final int X = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfinal int X = 1;", result.replacement().getFirst());
	}

	@Test
	public void testResultIsBlankDeletesLine() {
		final var lines = new ArrayList<>(List.of("public"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertTrue(result.replacement().isEmpty());
	}
}