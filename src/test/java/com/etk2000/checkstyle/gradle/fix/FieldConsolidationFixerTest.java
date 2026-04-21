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
	public void testBackwardScanHitsBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\t/* separator */",
				"\tint beta;"
		));
		assertNull(fixer.fix(lines, 2, 5));
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
	public void testBackwardScanHitsJavadoc() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\t/** Javadoc for beta */",
				"\tint beta;"
		));
		assertNull(fixer.fix(lines, 2, 5));
	}

	@Test
	public void testBackwardScanHitsMultiLineJavadoc() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\t/**",
				"\t * Javadoc for beta.",
				"\t */",
				"\tint beta;"
		));
		assertNull(fixer.fix(lines, 4, 5));
	}

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("int alpha;", "int beta;"));
		assertNull(fixer.fix(lines, 1, 50));
	}

	@Test
	public void testCommaMergeCharLiteralWithEscapedQuote() {
		final var lines = new ArrayList<>(List.of(
				"@Ann('\\'') int alpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann('\\'') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeContinuationCollected() {
		final var lines = new ArrayList<>(List.of(
				"\tboolean alpha,",
				"\t\t\tbeta,",
				"\t\t\t\tgamma,",
				"\t\t\t\tdelta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tboolean alpha, beta, gamma, delta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeCStyleArrays() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha[],",
				"\t\t\tbeta[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha[], beta[];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeIntermediate() {
		final var lines = new ArrayList<>(List.of(
				"\tboolean alpha,",
				"\t\t\tbeta,",
				"\t\t\tgamma;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tboolean alpha, beta,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeLastField() {
		final var lines = new ArrayList<>(List.of(
				"\t\t\talpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\t\t\talpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeNoTerminatorOnViolation() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha,",
				"\t\t\tbeta"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeThroughAnnotation() {
		final var lines = new ArrayList<>(List.of(
				"\tboolean alpha,",
				"\t@Deprecated",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 3));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tboolean alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWithBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint /* , */ alpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint /* , */ alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWithLineComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha, // trailing,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWithStringLiteral() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a,b\") int alpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a,b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWithStringLiteralEscapedQuote() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a\\\"b\") int alpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a\\\"b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWithTypePrefix() {
		final var lines = new ArrayList<>(List.of(
				"\tprivate boolean areInvestmentFundsTreatedAsPensionLiquidity,",
				"\t\t\tarePensionsTreatedAsSeparateLiquidity,",
				"\t\t\tareUnvestedRsusExcludedFromSum;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals(
				"\tprivate boolean areInvestmentFundsTreatedAsPensionLiquidity, arePensionsTreatedAsSeparateLiquidity,",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeWraps() {
		final var name1 = "a".repeat(55);
		final var name2 = "b".repeat(55);
		final var lines = new ArrayList<>(List.of(
				"\tint " + name1 + ",",
				"\t\t\t" + name2 + ";"
		));
		// tab(4) + "int "(4) + 55 + ", "(2) + 55 + ";"(1) = 121
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tint " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + name2 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationLoopExhaustsLines() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\tbeta,",
				"\t\t\tgamma,"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha, beta, gamma;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\t/* single-line block comment */",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtCollectedThenComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\tbeta,",
				"\t\t\t// comment about gamma",
				"\t\t\tgamma;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha, beta,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\t// comment about beta",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtCommentNoCommaOnViolation() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha",
				"\t\t\t// comment",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtJavadoc() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\t/** Javadoc for beta */",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testContinuationStopsAtMultiLineBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\t/*",
				"\t\t\t * multi-line comment",
				"\t\t\t */",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha,", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
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
	public void testMergeIgnoresTrailingCommaBeforeSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"int alpha;",
				"int beta, ;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 4));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
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
	public void testPrevLineAdjacentBlockComments() {
		final var lines = new ArrayList<>(List.of(
				"\tint /* ; *//* x */ alpha;",
				"\tint beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint /* ; *//* x */ alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineAllCommasInStrings() {
		final var lines = new ArrayList<>(List.of(
				"@Ann(\"a,b\") // no semicolon or real comma",
				"int beta;"
		));
		assertNull(fixer.fix(lines, 1, 4));
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
	public void testPrevLineBlockCommentSpanningMultipleFields() {
		final var lines = new ArrayList<>(List.of(
				"\tint /* comment */ alpha;",
				"\tint /* comment */ beta;"
		));
		assertNull(fixer.fix(lines, 1, 19));
	}

	@Test
	public void testPrevLineBlockCommentUnclosed() {
		final var lines = new ArrayList<>(List.of(
				"\tint /* unclosed alpha;",
				"\tint beta;"
		));
		assertNull(fixer.fix(lines, 1, 5));
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
	public void testPrevLineCharLiteralWithComma() {
		final var lines = new ArrayList<>(List.of(
				"@Ann(',') int alpha,",
				"\t\t\tbeta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 3));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann(',') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineCharLiteralWithEscapedBackslash() {
		final var lines = new ArrayList<>(List.of(
				"@Ann('\\\\') int alpha;",
				"@Ann('\\\\') int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 15));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann('\\\\') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineCharLiteralWithEscapedQuote() {
		final var lines = new ArrayList<>(List.of(
				"@Ann('\\'') int alpha;",
				"@Ann('\\'') int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 15));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann('\\'') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineCharLiteralWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"@Ann(';') int alpha;",
				"@Ann(';') int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 14));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann(';') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testPrevLineTrailingCommentNotEndingWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha; // field comment",
				"\tint beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta; // field comment", result.replacement().getFirst());
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
	public void testViolationLineBlockCommentAfterFieldNameProceeds() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta; /* note */"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testViolationLineBlockCommentInsideCharLiteralProceeds() {
		final var lines = new ArrayList<>(List.of(
				"@Ann('/') int alpha;",
				"@Ann('/') int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 14));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@Ann('/') int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testViolationLineBlockCommentInsideEscapedStringProceeds() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a\\\"/*b\") int alpha;",
				"@SuppressWarnings(\"a\\\"/*b\") int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 32));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a\\\"/*b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testViolationLineBlockCommentInsideStringProceeds() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(\"a/*b\") int alpha;",
				"@SuppressWarnings(\"a/*b\") int beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 30));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("@SuppressWarnings(\"a/*b\") int alpha, beta;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testViolationLineBlockCommentPostName() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta /* doc */;"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testViolationLineBlockCommentPostNameNoSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta /* doc */"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testViolationLineBlockCommentUnclosed() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint beta /* unclosed"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testViolationLineBlockCommentWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"\tint alpha;",
				"\tint /* ; */ beta;"
		));
		assertNull(fixer.fix(lines, 1, 13));
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

	@Test
	public void testWrapBoundary121Wraps() {
		// tab(4) + "int "(4) + 55 + ", "(2) + 55 + ";"(1) = 121
		final var name1 = "a".repeat(55);
		final var name2 = "b".repeat(55);
		final var lines = new ArrayList<>(List.of(
				"\tint " + name1 + ";",
				"\tint " + name2 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tint " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + name2 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapBoundaryExactly120NoWrap() {
		// tab(4) + "int "(4) + 55 + ", "(2) + 54 + ";"(1) = 120
		final var name1 = "a".repeat(55);
		final var name2 = "b".repeat(54);
		final var lines = new ArrayList<>(List.of(
				"\tint " + name1 + ";",
				"\tint " + name2 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint " + name1 + ", " + name2 + ";", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationBreaksAtNoIdentLine() {
		final var name1 = "a".repeat(40);
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint " + name1 + ",",
				"\t\t\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, " + name1 + ";", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationBreaksAtSameIndent() {
		final var name1 = "a".repeat(40);
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint " + name1 + ",",
				"\tint anotherField;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, " + name1 + ";", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationBreaksAtSameIndentMixedTabsSpaces() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"    beta;"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationCommentPreserved() {
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint alpha,",
				"\t\t\tbeta; // important"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(1, result.replacement().size());
		assertEquals("\tint prevName, alpha, beta; // important", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationFromPreviousWrap() {
		final var name1 = "a".repeat(40);
		final var name2 = "b".repeat(40);
		final var name3 = "c".repeat(40);
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint " + name1 + ", " + name2 + ",",
				"\t\t\t" + name3 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tint prevName, " + name1 + ", " + name2 + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + name3 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapContinuationMultipleLines() {
		final var name1 = "a".repeat(30);
		final var name2 = "b".repeat(30);
		final var name3 = "c".repeat(30);
		final var name4 = "d".repeat(30);
		final var lines = new ArrayList<>(List.of(
				"\tint prevName;",
				"\tint " + name1 + ",",
				"\t\t\t" + name2 + ",",
				"\t\t\t" + name3 + ",",
				"\t\t\t" + name4 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals(
				"\tint prevName, " + name1 + ", " + name2 + ", " + name3 + ",",
				result.replacement().get(0)
		);
		assertEquals("\t\t\t" + name4 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapCStyleArrays() {
		// tab(4) + "int "(4) + 53 + "[], "(4) + 53 + "[];"(3) = 121
		final var name1 = "a".repeat(53);
		final var name2 = "b".repeat(53);
		final var lines = new ArrayList<>(List.of(
				"\tint " + name1 + "[];",
				"\tint " + name2 + "[];"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tint " + name1 + "[],", result.replacement().get(0));
		assertEquals("\t\t\t" + name2 + "[];", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapDeepIndent() {
		// tabs(8) + "int "(4) + 53 + ", "(2) + 53 + ";"(1) = 121
		final var name1 = "a".repeat(53);
		final var name2 = "b".repeat(53);
		final var lines = new ArrayList<>(List.of(
				"\t\tint " + name1 + ";",
				"\t\tint " + name2 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 6));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\t\tint " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t\t" + name2 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapEachNameOwnLine() {
		final var name1 = "a".repeat(51);
		final var name2 = "b".repeat(51);
		final var name3 = "c".repeat(51);
		final var lines = new ArrayList<>(List.of(
				"\t\t\tint " + name1 + ";",
				"\t\t\tint " + name2 + ", " + name3 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 7));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(3, result.replacement().size());
		assertEquals("\t\t\tint " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t\t\t" + name2 + ",", result.replacement().get(1));
		assertEquals("\t\t\t\t\t" + name3 + ";", result.replacement().get(2));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapFourFieldsTwoPerLine() {
		final var a = "a".repeat(35);
		final var b = "b".repeat(35);
		final var c = "c".repeat(35);
		final var d = "d".repeat(35);
		final var lines = new ArrayList<>(List.of(
				"\tboolean " + a + ";",
				"\tboolean " + b + ", " + c + ", " + d + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 9));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tboolean " + a + ", " + b + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + c + ", " + d + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapThreeFieldsOnePlusTwo() {
		final var a = "a".repeat(55);
		final var b = "b".repeat(55);
		final var c = "c".repeat(30);
		final var lines = new ArrayList<>(List.of(
				"\tboolean " + a + ";",
				"\tboolean " + b + ", " + c + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 9));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tboolean " + a + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + b + ", " + c + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapThreeFieldsTwoPlusOne() {
		final var a = "a".repeat(35);
		final var b = "b".repeat(35);
		final var c = "c".repeat(35);
		final var lines = new ArrayList<>(List.of(
				"\tboolean " + a + ";",
				"\tboolean " + b + ", " + c + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 9));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tboolean " + a + ", " + b + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + c + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapWithModifiers() {
		// tab(4) + "private static int "(19) + 48 + ", "(2) + 48 + ";"(1) = 122
		final var name1 = "a".repeat(48);
		final var name2 = "b".repeat(48);
		final var lines = new ArrayList<>(List.of(
				"\tprivate static int " + name1 + ";",
				"\tprivate static int " + name2 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 20));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tprivate static int " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + name2 + ";", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWrapWithTrailingComment() {
		// tab(4) + "int "(4) + 48 + ", "(2) + 48 + "; // see init();"(16) = 122
		final var name1 = "a".repeat(48);
		final var name2 = "b".repeat(48);
		final var lines = new ArrayList<>(List.of(
				"\tint " + name1 + "; // see init();",
				"\tint " + name2 + ";"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 5));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(2, result.replacement().size());
		assertEquals("\tint " + name1 + ",", result.replacement().get(0));
		assertEquals("\t\t\t" + name2 + "; // see init();", result.replacement().get(1));
		assertTrue(result.importsToAdd().isEmpty());
	}
}