package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
	public void testAnnotationWithNestedParens() {
		final var lines = new ArrayList<>(List.of("\t@A(v = (1 + 2)) @B void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A(v = (1 + 2))", "\t@B", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testAnnotationWithStringParams() {
		final var lines = new ArrayList<>(List.of("\t@SuppressWarnings(\"unchecked\") @Override void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@Override", "\t@SuppressWarnings(\"unchecked\")", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testBlankLineAfterBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/* block */", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineAfterJavadocBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/** Javadoc. */", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineAfterLineCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t// comment", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineAfterMultiLineBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/*", "\t * comment", "\t */", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineAfterMultiLineBlockCommentWithInternalBlankBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "\t/*", "\t * comment", "\t *", "\t * more", "\t */", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(6, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBeforeBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/* block */", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBeforeJavadocBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/** Javadoc. */", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBeforeLineCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t// comment", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBeforeMultiLineBlockCommentBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t/*", "\t * comment", "\t */", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "\t@B", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineBelowMultiLineAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@V({", "\t\t\"a\"", "\t})", "", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 2, 0);
		assertNotNull(result);
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineInsideAnnotation() {
		final var lines = new ArrayList<>(List.of("\t@V({", "", "\t\t\"a\"", "\t})", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testBlankLineInsideBlockCommentNoBlankAfter() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\t/*", "", "\t */", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
	}

	@Test
	public void testEscapedQuoteInStringParam() {
		final var lines = new ArrayList<>(List.of("\t@A(\"he said \\\"hi\\\"\") @B void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A(\"he said \\\"hi\\\"\")", "\t@B", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testMultipleBlankLinesBelow() {
		final var lines = new ArrayList<>(List.of("\t@A", "", "", "\t@B", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of(), result.replacement());
	}

	@Test
	public void testMultipleSpacesBetweenAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@A    @B void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A", "\t@B", "\tvoid f() {}"), result.replacement());
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
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@javax.annotation.Nonnull", "\t@Override", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testReorderBlock() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
	}

	@Test
	public void testReorderBlockCommentBelowNoBlank() {
		final var lines = new ArrayList<>(List.of("\t@B", "\t@A", "\t// comment", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
	}

	@Test
	public void testReorderThreeAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@C", "\t@A", "\t@B", "\tvoid f() {}"));
		final var result = fixer.fix(lines, 1, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t@A", "\t@B", "\t@C"), result.replacement());
	}

	@Test
	public void testSingleAnnotationAlone() {
		final var lines = new ArrayList<>(List.of("\t@A"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testSingleAnnotationWithDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@A void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testSortsAlphabetically() {
		final var lines = new ArrayList<>(List.of("\t@C @A @B void f() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A", "\t@B", "\t@C", "\tvoid f() {}"), result.replacement());
	}

	@Test
	public void testSplitAnnotationAndDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@Override void foo() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t@Override", "\tvoid foo() {}"), result.replacement());
	}

	@Test
	public void testSplitMultipleAnnotationsAndDeclaration() {
		final var lines = new ArrayList<>(List.of("\t@A @B void foo() {}"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A", "\t@B", "\tvoid foo() {}"), result.replacement());
	}

	@Test
	public void testSplitTwoAnnotations() {
		final var lines = new ArrayList<>(List.of("\t@A @B"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t@A", "\t@B"), result.replacement());
	}

	@Test
	public void testTabIndentPreserved() {
		final var lines = new ArrayList<>(List.of("\t\t@A @B"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals(List.of("\t\t@A", "\t\t@B"), result.replacement());
	}
}