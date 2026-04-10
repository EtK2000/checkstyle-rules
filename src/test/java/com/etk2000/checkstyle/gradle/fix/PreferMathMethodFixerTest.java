package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferMathMethodFixerTest {
	private final CheckstyleFixer fixer = new PreferMathMethodFixer();

	@Test
	public void testAbsGeZero() {
		final var lines = new ArrayList<>(List.of("\t\treturn a >= 0 ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsGtZero() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > 0 ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsLeZero() {
		final var lines = new ArrayList<>(List.of("\t\treturn a <= 0 ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsLtZero() {
		final var lines = new ArrayList<>(List.of("\t\treturn a < 0 ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsWithArrayAccess() {
		final var lines = new ArrayList<>(List.of("\t\treturn arr[0] < 0 ? -arr[0] : arr[0];"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(arr[0]);", result.replacement().getFirst());
	}

	@Test
	public void testAbsWithFieldAccess() {
		final var lines = new ArrayList<>(List.of("\t\treturn a.x < 0 ? -a.x : a.x;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a.x);", result.replacement().getFirst());
	}

	@Test
	public void testAbsZeroLeftGe() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 >= a ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsZeroLeftGt() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 > a ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsZeroLeftLe() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 <= a ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testAbsZeroLeftLt() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 < a ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.abs(a);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMin() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, foo(a, b)));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedOuterArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(bar(x, y), Math.min(hi, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(value, bar(x, y), hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, value), lo);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, foo(a, b)), lo);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMax() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, value));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, foo(a, b)));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, value), hi);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, foo(a, b)), hi);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testMaxGe() {
		final var lines = new ArrayList<>(List.of("\t\treturn a >= b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxGt() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxLe() {
		final var lines = new ArrayList<>(List.of("\t\treturn a <= b ? b : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxLt() {
		final var lines = new ArrayList<>(List.of("\t\treturn a < b ? b : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreDecrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn --a > b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(--a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreIncrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn ++a > b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.max(++a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMinGe() {
		final var lines = new ArrayList<>(List.of("\t\treturn a >= b ? b : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.min(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMinGt() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > b ? b : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.min(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMinLe() {
		final var lines = new ArrayList<>(List.of("\t\treturn a <= b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.min(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMinLt() {
		final var lines = new ArrayList<>(List.of("\t\treturn a < b ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\treturn Math.min(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testNoMatchBooleanCondition() {
		final var lines = new ArrayList<>(List.of("\t\treturn flag ? a : b;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchClampNoComma() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(a));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchClampUnbalancedParens() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.min(b, c);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchMismatchedOperands() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > b ? a : c;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchNestedSameMethod() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.max(b, c));"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranch() {
		final var lines = new ArrayList<>(List.of("\t\treturn a < 0 ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranchGe() {
		final var lines = new ArrayList<>(List.of("\t\treturn a >= 0 ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranchGt() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > 0 ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranchZeroLeft() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 > a ? a : -a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranchZeroLeftLe() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 <= a ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsBranchZeroLeftLt() {
		final var lines = new ArrayList<>(List.of("\t\treturn 0 < a ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}

	@Test
	public void testNoMatchWrongAbsNonZero() {
		final var lines = new ArrayList<>(List.of("\t\treturn a < 1 ? -a : a;"));
		final var result = fixer.fix(lines, 0, 0);
		assertNull(result);
	}
}