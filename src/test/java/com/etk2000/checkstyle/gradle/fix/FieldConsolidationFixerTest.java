package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FieldConsolidationFixerTest {
	private final CheckstyleFixer fixer = new FieldConsolidationFixer();

	@Test
	public void testAnnotatedFieldsBothAnnotated() {
		final var lines = new ArrayList<>(List.of(
				"@Deprecated",
				"int alpha;",
				"@Deprecated",
				"int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 4));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationsWithBlankLineBetween() {
		final var lines = new ArrayList<>(List.of(
				"int alpha;",
				"",
				"@NonNull",
				"int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 4));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeBothCStyle() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[];",
				"\tint beta[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha[], beta[];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeBothCStyleMergedThreeFields() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[];",
				"\tint beta[], gamma[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha[], beta[], gamma[];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeBothCStyleMultidimensional() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[][];",
				"\tint beta[][];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha[][], beta[][];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeBothCStyleNoSemicolonOnViolation() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[];",
				"\tint beta[]"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testArrayTypeCStyleCurrOnly() {
		final var lines = new ArrayList<>(List.of(
				"\tint[] alpha;",
				"\tint beta[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint[] alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeCStyleCurrOnlyMultidimensional() {
		final var lines = new ArrayList<>(List.of(
				"\tint[][] alpha;",
				"\tint beta[][];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint[][] alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayTypeCStylePrevJavaStyleCurrBailsOut() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[];",
				"\tint[] beta;"
		));
		assertNull(fixer.fix(lines, 1, 6));
	}

	@Test
	public void testArrayTypeCStylePrevWithTabBeforeSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[]\t;",
				"\tint beta[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha[]\t, beta[];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBackwardScanExhausted() {
		final var lines = new ArrayList<>(List.of(
				"@Foo",
				"",
				"int beta;"
		));
		assertNull(fixer.fix(lines, 2, 4));
	}

	@Test
	public void testBackwardScanHitsCommentLine() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\t// separator comment",
				"\tint beta;"
		));
		assertNull(fixer.fix(lines, 2, 5));
	}

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("int alpha;", "int beta;"));
		assertNull(fixer.fix(lines, 1, 50));
	}

	@Test
	public void testFinalFields() {
		final var lines = new ArrayList<>(List.of(
				"\tfinal int alpha;",
				"\tfinal int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 11));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tfinal int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testGenericType() {
		final var lines = new ArrayList<>(List.of(
				"\tList<String> names;",
				"\tList<String> words;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 14));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tList<String> names, words;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("int alpha;"));
		assertNull(fixer.fix(lines, 5, 0));
	}

	@Test
	public void testLineIndexZero() {
		final var lines = new ArrayList<>(List.of("int beta;"));
		assertNull(fixer.fix(lines, 0, 4));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("int alpha;", "int beta;"));
		assertNull(fixer.fix(lines, 1, -1));
	}

	@Test
	public void testNonIdentifierAtColumn() {
		final var lines = new ArrayList<>(List.of("int alpha;", "int ;"));
		assertNull(fixer.fix(lines, 1, 4));
	}

	@Test
	public void testNoPreviousSemicolon() {
		final var lines = new ArrayList<>(List.of("class Foo {", "\tint beta;"));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testPrevLineAllSemicolonsInComments() {
		final var lines = new ArrayList<>(List.of(
				"// foo;",
				"int beta;"
		));
		assertNull(fixer.fix(lines, 1, 4));
	}

	@Test
	public void testPrevLineAnnotationWithSemicolonInString() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a;b\") int alpha;",
				"@SuppressWarnings(\"a;b\") int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 29));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a;b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineBlockCommentWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint /* ; */ alpha;",
				"\tint beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint /* ; */ alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineTrailingCommentWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha; // see init();",
				"\tint beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta; // see init();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testProtectedFields() {
		final var lines = new ArrayList<>(List.of(
				"\tprotected Button nextButton;",
				"\tprotected Button presetsButton;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 18));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tprotected Button nextButton, presetsButton;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSimplePrimitiveFields() {
		final var lines = new ArrayList<>(List.of("int alpha;", "int beta;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 4));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSimpleReferenceFields() {
		final var lines = new ArrayList<>(List.of("\tString first;", "\tString second;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 8));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tString first, second;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStaticFields() {
		final var lines = new ArrayList<>(List.of(
				"\tstatic int global;",
				"\tstatic int shared;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 12));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tstatic int global, shared;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTabSeparatedMultiVarOnViolationLine() {
		final var lines = new ArrayList<>(List.of(
				"\tint a;",
				"\tint b,\tc;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint a, b, c;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testThreeFieldsBottomUpFirstPass() {
		final var lines = new ArrayList<>(List.of(
				"\tint a;",
				"\tint b;",
				"\tint c;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 5));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint b, c;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testThreeFieldsBottomUpSecondPass() {
		final var lines = new ArrayList<>(List.of(
				"\tint a;",
				"\tint b, c;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint a, b, c;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testViolationLineWithoutSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWithAnnotationsOnOwnLine() {
		final var lines = new ArrayList<>(List.of(
				"\t@NonNull",
				"\tprotected Button nextButton;",
				"\t@NonNull",
				"\tprotected Button presetsButton;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 18));
		assertEquals(1, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tprotected Button nextButton, presetsButton;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWithEscapedQuoteInAnnotationString() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a\\\"b\") int alpha;",
				"@SuppressWarnings(\"a\\\"b\") int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 30));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a\\\"b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWithMultipleAnnotations() {
		final var lines = new ArrayList<>(List.of(
				"\t@CheckResult",
				"\t@NonNull",
				"\tString alpha;",
				"\t@CheckResult",
				"\t@NonNull",
				"\tString beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 8));
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tString alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWithViolationComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line."
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}
}