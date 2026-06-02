package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for the package-private helpers in {@link FixerTestUtil} that
 * enforce the imports-directive contract (additive-only diffs, top-of-slice
 * placement, no empty FQCNs). The public {@code assertCaseFix} path is
 * exercised end-to-end by every {@code *FixerTest} class; these tests cover
 * the throw branches that the happy-path callers can't reach.
 */
public class FixerTestUtilTest {
	@Test
	public void testAssertAdditiveImportsAcceptsAdditiveDiff() {
		FixerTestUtil.assertAdditiveImports(
				"topic",
				"case",
				Set.of("a.b.C"),
				Set.of("a.b.C", "a.b.D")
		);
	}

	@Test
	public void testAssertAdditiveImportsRejectsRemovedImport() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.assertAdditiveImports(
						"topic",
						"case",
						Set.of("a.b.C", "a.b.Removed"),
						Set.of("a.b.C")
				)
		);
		assertTrue(ex.getMessage().contains("additive diffs only"));
		assertTrue(ex.getMessage().contains("a.b.Removed"));
		assertTrue(ex.getMessage().contains("topic/case"));
	}

	@Test
	public void testCollectImportFqcnsAcceptsStaticImport() {
		assertEquals(
				Set.of("static foo.Bar.X"),
				FixerTestUtil.collectImportFqcns(List.of("import static foo.Bar.X;"))
		);
	}

	@Test
	public void testCollectImportFqcnsAcceptsWildcard() {
		assertEquals(
				Set.of("java.util.*"),
				FixerTestUtil.collectImportFqcns(List.of("import java.util.*;"))
		);
	}

	@Test
	public void testCollectImportFqcnsEmptyFqcnThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.collectImportFqcns(List.of("import ;"))
		);
		assertTrue(ex.getMessage().contains("empty FQCN"));
	}

	@Test
	public void testCollectImportFqcnsIgnoresImportWithoutSemicolon() {
		assertEquals(Set.of(), FixerTestUtil.collectImportFqcns(List.of("import a.b.C")));
	}

	@Test
	public void testCollectImportFqcnsIgnoresNonImportLines() {
		assertEquals(
				Set.of("a.b.C"),
				FixerTestUtil.collectImportFqcns(List.of(
						"package x;",
						"// comment with import in it",
						"import a.b.C;",
						"class Foo {}"
				))
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsBodyLocatedImportThrows() {
		final var lines = new ArrayList<>(List.of("class X {}"));
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.insertAddedImportsAtFixedPositions(
						lines,
						List.of("class X {}", "import a.b.C;"),
						Set.of()
				)
		);
		assertTrue(ex.getMessage().contains("below body content"));
		assertTrue(ex.getMessage().contains("top of the slice"));
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsHandlesParenInStringInsideMultiLineAnnotation() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(",
				"\t\"(stray open paren in string\"",
				")",
				"package x;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"@SuppressWarnings(",
						"\t\"(stray open paren in string\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"@SuppressWarnings(",
						"\t\"(stray open paren in string\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsInsertsAtEndBoundary() {
		final var lines = new ArrayList<>(List.of("package x;"));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of("package x;", "import a.b.New;"),
				Set.of()
		);
		assertEquals(List.of("package x;", "import a.b.New;"), lines);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsInsertsAtFixedIndex() {
		final var lines = new ArrayList<>(List.of(
				"import a.b.Existing;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"import a.b.Existing;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of("a.b.Existing")
		);
		assertEquals(
				List.of("import a.b.Existing;", "import a.b.New;", "class X {}"),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsPastEndOfLinesThrows() {
		final var lines = new ArrayList<>(List.of("x"));
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.insertAddedImportsAtFixedPositions(
						lines,
						List.of("// h", "// h", "// h", "import a.b.C;"),
						Set.of()
				)
		);
		assertTrue(ex.getMessage().contains("past the end of post-fix lines"));
		assertTrue(ex.getMessage().contains("size=1"));
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsAtSymbolInsideBlockCommentAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"/*",
				"@Override // commented-out annotation",
				"*/",
				"package x;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"/*",
						"@Override // commented-out annotation",
						"*/",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"/*",
						"@Override // commented-out annotation",
						"*/",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsBlankLineAsHeader() {
		final var lines = new ArrayList<>(List.of("package x;", "", "class X {}"));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of("package x;", "", "import a.b.New;", "class X {}"),
				Set.of()
		);
		assertEquals(
				List.of("package x;", "", "import a.b.New;", "class X {}"),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsHeaderLinesAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"// line comment",
				"/**",
				" * Javadoc",
				" */",
				"@Annotation",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"package x;",
						"// line comment",
						"/**",
						" * Javadoc",
						" */",
						"@Annotation",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"package x;",
						"// line comment",
						"/**",
						" * Javadoc",
						" */",
						"@Annotation",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsImportInsideBlockCommentAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"/*",
				"import a.b.Commented;",
				"*/",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"package x;",
						"/*",
						"import a.b.Commented;",
						"*/",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"package x;",
						"/*",
						"import a.b.Commented;",
						"*/",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsImportInsideMultiLineAnnotationAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(",
				"\t\"import a.b.Decoy; (looks like an import but is a string arg)\"",
				")",
				"package x;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"@SuppressWarnings(",
						"\t\"import a.b.Decoy; (looks like an import but is a string arg)\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"@SuppressWarnings(",
						"\t\"import a.b.Decoy; (looks like an import but is a string arg)\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsImportShapedLineInsideMultiLineAnnotationAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings({",
				"import a.b.Decoy;",
				"})",
				"package x;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"@SuppressWarnings({",
						"import a.b.Decoy;",
						"})",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"@SuppressWarnings({",
						"import a.b.Decoy;",
						"})",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsTreatsMultiLineAnnotationAsHeader() {
		final var lines = new ArrayList<>(List.of(
				"@SuppressWarnings(",
				"\t\"unchecked\"",
				")",
				"package x;",
				"class X {}"
		));
		FixerTestUtil.insertAddedImportsAtFixedPositions(
				lines,
				List.of(
						"@SuppressWarnings(",
						"\t\"unchecked\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				Set.of()
		);
		assertEquals(
				List.of(
						"@SuppressWarnings(",
						"\t\"unchecked\"",
						")",
						"package x;",
						"import a.b.New;",
						"class X {}"
				),
				lines
		);
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsUnclosedAnnotationAndBlockCommentCombinedMessage() {
		final var lines = new ArrayList<>(List.of("@Foo(/* unterminated"));
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.insertAddedImportsAtFixedPositions(
						lines,
						List.of("@Foo(/* unterminated"),
						Set.of()
				)
		);
		assertTrue(ex.getMessage().contains("unclosed block comment, unclosed annotation (annotationDepth=1)"));
		assertTrue(ex.getMessage().contains("in fixed slice (at end of slice)."));
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsUnclosedAnnotationThrows() {
		final var lines = new ArrayList<>(List.of("@SuppressWarnings(", "class X {}"));
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.insertAddedImportsAtFixedPositions(
						lines,
						List.of("@SuppressWarnings(", "class X {}"),
						Set.of()
				)
		);
		assertTrue(ex.getMessage().contains("unclosed annotation"));
		assertTrue(ex.getMessage().contains("annotationDepth=1"));
		assertTrue(ex.getMessage().contains("in fixed slice (at end of slice)."));
	}

	@Test
	public void testInsertAddedImportsAtFixedPositionsUnclosedBlockCommentThrows() {
		final var lines = new ArrayList<>(List.of("package x;", "/* unclosed comment with no terminator"));
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> FixerTestUtil.insertAddedImportsAtFixedPositions(
						lines,
						List.of("package x;", "/* unclosed comment with no terminator"),
						Set.of()
				)
		);
		assertTrue(ex.getMessage().contains("unclosed block comment"));
		assertTrue(ex.getMessage().contains("in fixed slice (at end of slice)."));
	}

	@Test
	public void testScanAnnotationLineBareSlashIsNotCommentStart() {
		final var scan = FixerTestUtil.scanAnnotationLine("@A(4/2)", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineBlockCommentTrailingStar() {
		final var scan = FixerTestUtil.scanAnnotationLine(" * comment *", 0, true);
		assertEquals(0, scan.depth());
		assertTrue(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineBlockCommentWithStarNotFollowedBySlash() {
		final var scan = FixerTestUtil.scanAnnotationLine("@A(/* * x */)", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineClampsCloseParenAtZeroDepth() {
		final var scan = FixerTestUtil.scanAnnotationLine("@A())", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineIncrementsOnContinuationOpen() {
		final var scan = FixerTestUtil.scanAnnotationLine("nested(", 1, false);
		assertEquals(2, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSingleLineAnnotationReturnsZeroDepth() {
		final var scan = FixerTestUtil.scanAnnotationLine("@SuppressWarnings(\"x\")", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsEscapedApostropheInsideCharLiteral() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo('\\'')", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsEscapedQuoteInsideString() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo(\"\\\")\")", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsParenInsideBlockComment() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo(/* ) */)", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsParenInsideCharLiteral() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo('(')", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsParenInsideLineComment() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo( // close )", 0, false);
		assertEquals(1, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineSkipsParenInsideString() {
		final var scan = FixerTestUtil.scanAnnotationLine("@Foo(\"(\")", 0, false);
		assertEquals(0, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineTracksBlockCommentAcrossLines() {
		final var first = FixerTestUtil.scanAnnotationLine("@Foo(/* open", 0, false);
		assertEquals(1, first.depth());
		assertTrue(first.inBlockComment());
		final var second = FixerTestUtil.scanAnnotationLine("closed */)", first.depth(), first.inBlockComment());
		assertEquals(0, second.depth());
		assertFalse(second.inBlockComment());
	}

	@Test
	public void testScanAnnotationLineTrailingSlashHasNoLookahead() {
		final var scan = FixerTestUtil.scanAnnotationLine("@A(x/", 0, false);
		assertEquals(1, scan.depth());
		assertFalse(scan.inBlockComment());
	}

	@Test
	public void testWildcardSubsumptionHintMismatchWithoutWildcardReturnsEmpty() {
		assertEquals(
				"",
				FixerTestUtil.wildcardSubsumptionHint(
						Set.of(),
						Set.of("a.b.C"),
						Set.of("a.b.Other")
				)
		);
	}

	@Test
	public void testWildcardSubsumptionHintMultipleUnexpectedActualsAppendsAllHints() {
		final var hint = FixerTestUtil.wildcardSubsumptionHint(
				Set.of(),
				Set.of("java.util.List", "java.util.Map"),
				Set.of("java.util.*")
		);
		assertTrue(hint.contains("java.util.List"));
		assertTrue(hint.contains("java.util.Map"));
		assertTrue(hint.contains("java.util.*"));
	}

	@Test
	public void testWildcardSubsumptionHintMultipleWildcardsBreaksOnFirstMatch() {
		final var hint = FixerTestUtil.wildcardSubsumptionHint(
				Set.of(),
				Set.of("java.util.List"),
				Set.of("java.io.*", "java.util.*")
		);
		assertTrue(hint.contains("java.util.*"));
		assertTrue(hint.contains("java.util.List"));
		final var firstHint = hint.indexOf("Hint:");
		assertEquals(firstHint, hint.lastIndexOf("Hint:"));
	}

	@Test
	public void testWildcardSubsumptionHintNoMismatchReturnsEmpty() {
		assertEquals(
				"",
				FixerTestUtil.wildcardSubsumptionHint(
						Set.of("a.b.C"),
						Set.of("a.b.C"),
						Set.of("java.util.*")
				)
		);
	}

	@Test
	public void testWildcardSubsumptionHintWildcardEqualsActualReturnsEmpty() {
		assertEquals(
				"",
				FixerTestUtil.wildcardSubsumptionHint(
						Set.of(),
						Set.of("java.util.*"),
						Set.of("java.util.*")
				)
		);
	}

	@Test
	public void testWildcardSubsumptionHintWildcardMismatchPrefixNotMatchReturnsEmpty() {
		assertEquals(
				"",
				FixerTestUtil.wildcardSubsumptionHint(
						Set.of(),
						Set.of("a.b.C"),
						Set.of("x.y.*")
				)
		);
	}

	@Test
	public void testWildcardSubsumptionHintWildcardSubsumesActualAppendsHint() {
		final var hint = FixerTestUtil.wildcardSubsumptionHint(
				Set.of(),
				Set.of("java.util.List"),
				Set.of("java.util.*")
		);
		assertTrue(hint.contains("subsumed by wildcard 'java.util.*'"));
		assertTrue(hint.contains("java.util.List"));
	}
}