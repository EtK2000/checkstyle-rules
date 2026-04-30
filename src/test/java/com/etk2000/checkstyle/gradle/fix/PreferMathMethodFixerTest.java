package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn " + expected + ";", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMin() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, value));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(lo, Math.min(hi, foo(a, b)));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinNestedOuterArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(bar(x, y), Math.min(hi, value));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, bar(x, y), hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, value), lo);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMaxMinReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(hi, foo(a, b)), lo);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMax() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, value));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxNestedInnerArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(hi, Math.max(lo, foo(a, b)));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversed() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, value), hi);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(value, lo, hi);", result.replacement().getFirst());
	}

	@Test
	public void testClampMinMaxReversedNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.min(Math.max(lo, foo(a, b)), hi);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.clamp(foo(a, b), lo, hi);", result.replacement().getFirst());
	}

	@ParameterizedTest
	@ValueSource(strings = {"+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", ">>>="})
	void testIfCompoundAssignAllOperators(String compoundOp) {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr " + compoundOp + " a;",
				"\t\telse",
				"\t\t\tr " + compoundOp + " b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals("\t\tr " + compoundOp + " Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfCompoundAssignDifferentOpsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += a;",
				"\t\telse",
				"\t\t\tr -= b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfCompoundAssignDifferentTargetsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += a;",
				"\t\telse",
				"\t\t\ts += b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfCompoundAssignElseIsPlainReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfCompoundAssignMismatchedOperandsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += c;",
				"\t\telse",
				"\t\t\tr += d;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfCompoundAssignNoElseReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += a;",
				"\t\tfoo();",
				"\t\tr += b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfCompoundAssignTruncatedReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr += a;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfElseReturnMismatchedOperandsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\treturn c;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfInitOverwriteDeclVarNameMismatchReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint s = 0;",
				"\t\tif (a > b)",
				"\t\t\tr = a;"
		));
		final var attempt = fixer.fix(lines, 1, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfInitOverwriteMergeIntoDecl() {
		final var lines = new ArrayList<>(List.of(
				"\t\tvar r = b;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\tSystem.out.println(r);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals("\t\tvar r = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfInitOverwriteMismatchedOperandsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tvar r = c;",
				"\t\tif (a > b)",
				"\t\t\tr = d;",
				"\t\treturn r;"
		));
		final var attempt = fixer.fix(lines, 1, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfInitOverwriteNonDeclAboveReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfoo();",
				"\t\tif (a > b)",
				"\t\t\tr = a;"
		));
		final var attempt = fixer.fix(lines, 1, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfInitOverwriteWithTrailingReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tvar r = b;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfMultiDeclAboveRejected() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint s = 0, r = 0;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\treturn r;"
		));
		final var attempt = fixer.fix(lines, 1, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfNoBodyLineReturnsSkip() {
		final var lines = new ArrayList<>(List.of("\t\tif (a > b)"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfPlainAssignAtFileStartFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\tSystem.out.println(r);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignDeclClauseFalseFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tSystem.out.println(\"hi\");",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignDeclReturn() {
		final var lines = new ArrayList<>(List.of(
				"\t\tfinal int r;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignDeclVarMismatchFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint s;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignDeclWithoutTrailingReturnFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint r;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignDifferentTargetsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\ts = b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfPlainAssignElseIsCompoundReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr += b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfPlainAssignMismatchedOperandsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = c;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfPlainAssignNoElseReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\tr = b;",
				"\t\tr = c;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfPlainAssignReturnClauseFalseFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint r;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\tSystem.out.println(r);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignReturnVarMismatchFallsBackToBare() {
		final var lines = new ArrayList<>(List.of(
				"\t\tint r;",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn s;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals("\t\tr = Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfPlainAssignTruncatedReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tr = a;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfReturnElseBodyNotReturnReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\tr = b;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfReturnElseClauseFalseFallsToTrailing() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\treturn b;",
				"\t\t// dummy"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfReturnFollowedByNonReturnNonElseSkips() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\tSystem.out.println(\"x\");"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfReturnTrailing() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfReturnTruncatedReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfReturnWithElse() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\telse",
				"\t\t\treturn b;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals("\t\treturn Math.max(a, b);", result.replacement().getFirst());
	}

	@Test
	public void testIfThenLineUnrecognizedReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\tfoo();"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
	}

	@Test
	public void testIfTrailingReturnMismatchedOperandsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"\t\tif (a > b)",
				"\t\t\treturn a;",
				"\t\treturn c;"
		));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, ((SkipResult) attempt).reason());
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
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn " + expected + ";", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreDecrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn --a > b ? a : b;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.max(--a, b);", result.replacement().getFirst());
	}

	@Test
	public void testMaxPreIncrement() {
		final var lines = new ArrayList<>(List.of("\t\treturn ++a > b ? a : b;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\t\treturn Math.max(++a, b);", result.replacement().getFirst());
	}

	@Test
	public void testNoMatchBooleanCondition() {
		final var lines = new ArrayList<>(List.of("\t\treturn flag ? a : b;"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNoMatchClampNoComma() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(Math.min(a));"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNoMatchClampUnbalancedParens() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.min(b, c);"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNoMatchMismatchedOperands() {
		final var lines = new ArrayList<>(List.of("\t\treturn a > b ? a : c;"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNoMatchNestedSameMethod() {
		final var lines = new ArrayList<>(List.of("\t\treturn Math.max(a, Math.max(b, c));"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}

	@ParameterizedTest
	@ValueSource(strings = {"a < 0 ? a : -a", "a >= 0 ? -a : a", "a > 0 ? -a : a",
			"0 > a ? a : -a", "0 <= a ? -a : a", "0 < a ? -a : a", "a < 1 ? -a : a"})
	void testNoMatchWrongAbsBranch(String ternary) {
		final var lines = new ArrayList<>(List.of("\t\treturn " + ternary + ";"));
		final var attempt = fixer.fix(lines, 0, 0);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, ((SkipResult) attempt).reason());
	}
}