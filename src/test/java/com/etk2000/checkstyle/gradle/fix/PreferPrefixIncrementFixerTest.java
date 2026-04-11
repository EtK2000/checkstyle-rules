package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferPrefixIncrementFixerTest {
	private final CheckstyleFixer fixer = new PreferPrefixIncrementFixer();

	@Test
	public void testCase2ColumnAtEndOfLine() {
		final var lines = new ArrayList<>(List.of("+"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testCase2NonIdentStartBeforeOperator() {
		final var lines = new ArrayList<>(List.of("\t\t1++;"));
		assertNull(fixer.fix(lines, 0, 3));
	}

	@Test
	public void testColumnAtOperatorDecrement() {
		final var lines = new ArrayList<>(List.of("\t\ti--;"));
		final var result = fixer.fix(lines, 0, 3);
		assertNotNull(result);
		assertEquals("\t\t--i;", result.replacement().getFirst());
	}

	@Test
	public void testColumnAtOperatorIncrement() {
		final var lines = new ArrayList<>(List.of("\t\ti++;"));
		final var result = fixer.fix(lines, 0, 3);
		assertNotNull(result);
		assertEquals("\t\t++i;", result.replacement().getFirst());
	}

	@Test
	public void testColumnAtOperatorMultiCharIdent() {
		final var lines = new ArrayList<>(List.of("\t\tcount--;"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("\t\t--count;", result.replacement().getFirst());
	}

	@Test
	public void testInvalidColumn() {
		final var lines = new ArrayList<>(List.of("\t\ti++;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testMultiCharIdentifier() {
		final var lines = new ArrayList<>(List.of("\t\tcount++;"));
		final var result = fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals("\t\t++count;", result.replacement().getFirst());
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\t\ti++;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNoOperatorAfterIdent() {
		final var lines = new ArrayList<>(List.of("\t\ti = 1;"));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testOperatorAtEndOfLine() {
		final var lines = new ArrayList<>(List.of("\t\ti++"));
		final var result = fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals("\t\t++i", result.replacement().getFirst());
	}

	@Test
	public void testOperatorAtStartNoIdent() {
		final var lines = new ArrayList<>(List.of("++x;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testPostfixDecrementToPrefix() {
		final var lines = new ArrayList<>(List.of("\t\ti--;"));
		final var result = fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals("\t\t--i;", result.replacement().getFirst());
	}

	@Test
	public void testPostfixIncrementToPrefix() {
		final var lines = new ArrayList<>(List.of("\t\ti++;"));
		final var result = fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals("\t\t++i;", result.replacement().getFirst());
	}

	@Test
	public void testUnderscoreInIdent() {
		final var lines = new ArrayList<>(List.of("\t\tmy_var++;"));
		final var result = fixer.fix(lines, 0, 2);
		assertNotNull(result);
		assertEquals("\t\t++my_var;", result.replacement().getFirst());
	}
}