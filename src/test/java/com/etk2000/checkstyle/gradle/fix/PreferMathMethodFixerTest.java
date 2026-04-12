package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

public class PreferMathMethodFixerTest {
	private final CheckstyleFixer fixer = new PreferMathMethodFixer();

	@CsvSource({
			"'a >= 0 ? a : -a', 'Math.abs(a)'",
			"'a > 0 ? a : -a', 'Math.abs(a)'",
			"'a <= 0 ? -a : a', 'Math.abs(a)'",
			"'a < 0 ? -a : a', 'Math.abs(a)'",
			"'0 >= a ? -a : a', 'Math.abs(a)'",
			"'0 > a ? -a : a', 'Math.abs(a)'",
			"'0 <= a ? a : -a', 'Math.abs(a)'",
			"'0 < a ? a : -a', 'Math.abs(a)'",
			"'arr[0] < 0 ? -arr[0] : arr[0]', 'Math.abs(arr[0])'",
			"'a.x < 0 ? -a.x : a.x', 'Math.abs(a.x)'"
	})
	@ParameterizedTest
	void testAbsFix(String ternary, String expected) {
		final var lines = new ArrayList<>(List.of("\t\treturn " + ternary + ";"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn " + expected + ";", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMin() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, foo(a, b)));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedOuterArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(bar(x, y), Math.min(hi, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, bar(x, y), hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, value), lo);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, foo(a, b)), lo);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMax() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, foo(a, b)));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, value), hi);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, foo(a, b)), hi);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@CsvSource({
			"'a >= b ? a : b', 'Math.max(a, b)'",
			"'a > b ? a : b', 'Math.max(a, b)'",
			"'a <= b ? b : a', 'Math.max(a, b)'",
			"'a < b ? b : a', 'Math.max(a, b)'",
			"'a >= b ? b : a', 'Math.min(a, b)'",
			"'a > b ? b : a', 'Math.min(a, b)'",
			"'a <= b ? a : b', 'Math.min(a, b)'",
			"'a < b ? a : b', 'Math.min(a, b)'"
	})
	@ParameterizedTest
	void testMaxMinFix(String ternary, String expected) {
		final var lines = new ArrayList<>(List.of("\t\treturn " + ternary + ";"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn " + expected + ";", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreDecrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn --a > b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.max(--a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreIncrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn ++a > b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.max(++a, b);", result.replacement().getFirst());
	}

	@Test
	public void testNoMatchBooleanCondition() {
		final var lines = new ArrayList<>(List.of("\t\treturn flag ? a : b;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchClampNoComma() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(a));"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchClampUnbalancedParens() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.min(b, c);"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchMismatchedOperands() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > b ? a : c;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testNoMatchNestedSameMethod() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.max(b, c));"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@ParameterizedTest
	@ValueSource(strings = {"a < 0 ? a : -a", "a >= 0 ? -a : a", "a > 0 ? -a : a",
			"0 > a ? a : -a", "0 <= a ? -a : a", "0 < a ? -a : a", "a < 1 ? -a : a"})
	void testNoMatchWrongAbsBranch(String ternary) {
		final var lines = new ArrayList<>(List.of("\t\treturn " + ternary + ";"));
		assertNull(fixer.fix(lines, 0, 0));
	}
}