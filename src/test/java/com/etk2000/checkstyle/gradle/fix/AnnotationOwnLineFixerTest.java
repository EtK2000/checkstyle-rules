package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AnnotationOwnLineFixerTest {
	private final CheckstyleFixer fixer = new AnnotationOwnLineFixer();

	@Test
	public void testAlreadySortedReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t@B", "\tvoid f() {}"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testAnnotationWithCharLiteral() {
		final var lines = new ArrayList<>(List.of("\t@A('x') @B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A('x')", "\t@B", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationWithNestedAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@A(@B) @C void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(@B)", "\t@C", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationWithNestedParens() {
		final var lines = new ArrayList<>(List.of("\t@A(v = (1 + 2)) @B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(v = (1 + 2))", "\t@B", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationWithStringParams() {
		final var lines = new ArrayList<>(List.of("\t@SuppressWarnings(\"unchecked\") @Override void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@Override", "\t@SuppressWarnings(\"unchecked\")", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineAfterBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/* block */", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineAfterJavadocBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/** Javadoc. */", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineAfterLineCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t// comment", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineAfterMultiLineBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/*", "\t * comment", "\t */", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineAfterMultiLineBlockCommentWithInternalBlankBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/*", "\t * comment", "\t *", "\t * more", "\t */", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(6, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBeforeBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/* block */", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBeforeJavadocBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/** Javadoc. */", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBeforeLineCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t// comment", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBeforeMultiLineBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/*", "\t * comment", "\t */", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t@B", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineBelowMultiLineAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@V({", "\t\t\"a\"", "\t})", "", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 0));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineInsideAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@V({", "", "\t\t\"a\"", "\t})", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBlankLineInsideBlockCommentNoBlankAfter() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\t/*", "", "\t */", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationAfterMultipleModifiers() {
		final var lines = new ArrayList<>(List.of("\tprivate final @A String field;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tprivate final String field;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationAfterStaticFinal() {
		final var lines = new ArrayList<>(List.of("\tstatic final @A int CONST = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tstatic final int CONST = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationBetweenModifiers() {
		final var lines = new ArrayList<>(List.of("\tprivate @A final var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tprivate final var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationsWithEmptyRemaining() {
		final var lines = new ArrayList<>(List.of("\t@B final @A"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tfinal"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithArrayOfAnnotations() {
		final var lines = new ArrayList<>(List.of("\tfinal @A({@B, @C}) var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A({@B, @C})", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithDeepNestedAnnotation() {
		final var lines = new ArrayList<>(List.of("\tfinal @A(value = @B(\"test\")) var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(value = @B(\"test\"))", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithEscapedQuotes() {
		final var lines = new ArrayList<>(List.of("\tfinal @A(\"he said \\\"hi\\\"\") var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(\"he said \\\"hi\\\"\")", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithNestedAnnotation() {
		final var lines = new ArrayList<>(List.of("\tfinal @A(@B) var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(@B)", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithNestedParens() {
		final var lines = new ArrayList<>(List.of("\tfinal @A(v = (1 + 2)) var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(v = (1 + 2))", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithTabSeparator() {
		final var lines = new ArrayList<>(List.of("\tfinal\t@A var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedAnnotationWithValue() {
		final var lines = new ArrayList<>(List.of("\tfinal @SuppressWarnings(\"unused\") var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@SuppressWarnings(\"unused\")", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedMultipleAnnotationsAfterFinal() {
		final var lines = new ArrayList<>(List.of("\tfinal @A @B var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedQualifiedAnnotation() {
		final var lines = new ArrayList<>(List.of("\tfinal @javax.annotation.Nonnull var x = \"\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@javax.annotation.Nonnull", "\tfinal var x = \"\";"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedSingleAnnotationAfterFinal() {
		final var lines = new ArrayList<>(List.of("\tfinal @A var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEscapedQuoteInStringParam() {
		final var lines = new ArrayList<>(List.of("\t@A(\"he said \\\"hi\\\"\") @B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A(\"he said \\\"hi\\\"\")", "\t@B", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLeadingAndEmbeddedAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@C final @A var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@C", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLeadingAndEmbeddedAnnotationsWithMultipleModifiers() {
		final var lines = new ArrayList<>(List.of("\t@B private final @A String x;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tprivate final String x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleBlankLinesBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "", "\t@B", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleLeadingAndEmbeddedAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@D @C final @B @A var x = 1;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\t@C", "\t@D", "\tfinal var x = 1;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiplePositionsAllThree() {
		final var lines = new ArrayList<>(List.of("\t@X static @Y final @Z int v;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@X", "\t@Y", "\t@Z", "\tstatic final int v;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiplePositionsBetweenAndAfter() {
		final var lines = new ArrayList<>(List.of("\tstatic @X final @Y int v;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@X", "\t@Y", "\tstatic final int v;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiplePositionsBetweenOnly() {
		final var lines = new ArrayList<>(List.of("\tstatic @X final int v;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@X", "\tstatic final int v;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiplePositionsLeadingAndAfter() {
		final var lines = new ArrayList<>(List.of("\t@X static final @Y int v;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@X", "\t@Y", "\tstatic final int v;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiplePositionsLeadingAndBetween() {
		final var lines = new ArrayList<>(List.of("\t@X static @Y final int v;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@X", "\t@Y", "\tstatic final int v;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleSpacesBetweenAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@A    @B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\t@A void f() {}"));
		assertNull(fixer.fix(lines, -1, 0));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testQualifiedAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@javax.annotation.Nonnull @Override void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@javax.annotation.Nonnull", "\t@Override", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testReorderBlock() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testReorderBlockCommentBelowNoBlank() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\t// comment", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testReorderThreeAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@C", "\t@A", "\t@B", "\tvoid f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t@A", "\t@B", "\t@C"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSingleAnnotationAlone() {
		final var lines = new ArrayList<>(List.of("\t@A"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSingleAnnotationWithDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@A void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSortsAlphabetically() {
		final var lines = new ArrayList<>(List.of("\t@C @A @B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\t@C", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSplitAnnotationAndDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@Override void foo() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t@Override", "\tvoid foo() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSplitMultipleAnnotationsAndDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@A @B void foo() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tvoid foo() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSplitTwoAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@A @B"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTabBetweenLeadingAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@A\t@B void f() {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t@A", "\t@B", "\tvoid f() {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTabIndentPreserved() {
		final var lines = new ArrayList<>(List.of("\t\t@A @B"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(List.of("\t\t@A", "\t\t@B"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}