package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class FieldSortingFixerTest {
	private final CheckstyleFixer fixer = new FieldSortingFixer();

	private void assertFix(
			FixResult result,
			int expectedStartLine,
			int expectedEndLine,
			List<String> expectedReplacement
	) {
		assertNotNull(result);
		assertEquals(expectedStartLine, result.startLine());
		assertEquals(expectedEndLine, result.endLine());
		assertEquals(expectedReplacement, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAlreadySortedOwnLines() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tALPHA,",
				"\tBETA,",
				"\tGAMMA",
				"}"
		));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testAlreadySortedWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tALPHA,",
				"\tBETA;",
				"\tint x;",
				"}"
		));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testCaseInsensitiveSort() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tbeta,",
				"\tAlpha",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tAlpha,", "\tbeta"));
	}

	@Test
	public void testEmptyEnumBody() {
		final var lines = new ArrayList<>(List.of("enum T {", "}"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testFieldLineNotEnum() {
		final var lines = new ArrayList<>(List.of(
				"class T {",
				"\tstatic final int Z = 1;",
				"\tstatic final int A = 0;",
				"}"
		));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testIdempotenceAfterFix() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result1 = fixer.fix(lines, 2, 1);
		assertNotNull(result1);

		// apply the fix
		lines.subList(result1.startLine(), result1.endLine() + 1).clear();
		lines.addAll(result1.startLine(), result1.replacement());

		// second call on the fixed content should return null
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testInnerEnum() {
		final var lines = new ArrayList<>(List.of(
				"class Outer {",
				"\tenum Inner {",
				"\t\tBETA,",
				"\t\tALPHA",
				"\t}",
				"}"
		));
		final var result = fixer.fix(lines, 3, 2);
		assertFix(result, 2, 3, List.of("\t\tALPHA,", "\t\tBETA"));
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("enum T {", "\tALPHA", "}"));
		assertNull(fixer.fix(lines, 5, 0));
		assertNull(fixer.fix(lines, -1, 0));
	}

	@Test
	public void testNestedParensInArgs() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tB(foo(1, 2)),",
				"\tA(3)",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tA(3),", "\tB(foo(1, 2))"));
	}

	@Test
	public void testReorderThreeConstantsReverse() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tCHARLIE,",
				"\tBRAVO,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 3, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\tBRAVO,", "\tCHARLIE"));
	}

	@Test
	public void testReorderTwoConstants() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tALPHA,", "\tBETA"));
	}

	@Test
	public void testReorderWithAnnotationBlockCommentParens() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@Anno(/* ( */ \"x\")",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\t@Anno(/* ( */ \"x\")", "\tBETA"));
	}

	@Test
	public void testReorderWithAnnotationCharLiteralParens() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@Anno('(')",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\t@Anno('(')", "\tBETA"));
	}

	@Test
	public void testReorderWithAnnotationCommentParens() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@Deprecated // has ( here",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\t@Deprecated // has ( here", "\tBETA"));
	}

	@Test
	public void testReorderWithAnnotationMultiLine() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@SuppressWarnings(",
				"\t\t\"unchecked\"",
				"\t)",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 6, 1);
		final var expected = List.of(
				"\tALPHA,",
				"\t@SuppressWarnings(",
				"\t\t\"unchecked\"",
				"\t)",
				"\tBETA"
		);
		assertFix(result, 1, 5, expected);
	}

	@Test
	public void testReorderWithAnnotations() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@Deprecated",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 3, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\t@Deprecated", "\tBETA"));
	}

	@Test
	public void testReorderWithAnnotationStringParens() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t@SuppressWarnings(\"(((\")",
				"\tBETA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		assertFix(result, 1, 3, List.of("\tALPHA,", "\t@SuppressWarnings(\"(((\")", "\tBETA"));
	}

	@Test
	public void testReorderWithArguments() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tCHERRY(\"red\"),",
				"\tAPPLE(\"green\")",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tAPPLE(\"green\"),", "\tCHERRY(\"red\")"));
	}

	@Test
	public void testReorderWithBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t/*",
				"\t * ZEBRA docs",
				"\t */",
				"\tZEBRA,",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 5, 1);
		final var expected = List.of(
				"\tALPHA,",
				"\t/*",
				"\t * ZEBRA docs",
				"\t */",
				"\tZEBRA"
		);
		assertFix(result, 1, 5, expected);
	}

	@Test
	public void testReorderWithBodies() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tSUBTRACT {",
				"\t\t@Override",
				"\t\tint apply(int a, int b) {",
				"\t\t\treturn a - b;",
				"\t\t}",
				"\t},",
				"\tADD {",
				"\t\t@Override",
				"\t\tint apply(int a, int b) {",
				"\t\t\treturn a + b;",
				"\t\t}",
				"\t};",
				"\tabstract int apply(int a, int b);",
				"}"
		));
		final var result = fixer.fix(lines, 7, 1);
		final var expected = List.of(
				"\tADD {",
				"\t\t@Override",
				"\t\tint apply(int a, int b) {",
				"\t\t\treturn a + b;",
				"\t\t}",
				"\t},",
				"\tSUBTRACT {",
				"\t\t@Override",
				"\t\tint apply(int a, int b) {",
				"\t\t\treturn a - b;",
				"\t\t}",
				"\t};"
		);
		assertFix(result, 1, 12, expected);
	}

	@Test
	public void testReorderWithComments() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t// z",
				"\tZEBRA,",
				"\t// a",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		assertFix(result, 1, 4, List.of("\t// a", "\tALPHA,", "\t// z", "\tZEBRA"));
	}

	@Test
	public void testReorderWithJavadoc() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\t/** ZEBRA constant. */",
				"\tZEBRA,",
				"\t/** ALPHA constant. */",
				"\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 4, 1);
		final var expected = List.of(
				"\t/** ALPHA constant. */",
				"\tALPHA,",
				"\t/** ZEBRA constant. */",
				"\tZEBRA"
		);
		assertFix(result, 1, 4, expected);
	}

	@Test
	public void testReorderWithSemicolonAndTrailingComments() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tBETA, // b",
				"\tALPHA; // a",
				"\tint x;",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tALPHA, // a", "\tBETA; // b"));
	}

	@Test
	public void testReorderWithSemicolonOnLast() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tBETA,",
				"\tALPHA;",
				"\tint x;",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tALPHA,", "\tBETA;"));
	}

	@Test
	public void testReorderWithTrailingCommentEscapedQuote() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tB(\"test\\\\\"), // note",
				"\tA(\"x\")",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tA(\"x\"),", "\tB(\"test\\\\\") // note"));
	}

	@Test
	public void testReorderWithTrailingComments() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tCHERRY, // fruit",
				"\tAPPLE, // fruit",
				"\tBANANA // fruit",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		final var expected = List.of("\tAPPLE, // fruit", "\tBANANA, // fruit", "\tCHERRY // fruit");
		assertFix(result, 1, 3, expected);
	}

	@Test
	public void testReorderWithUrlInStringArg() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tB(\"http://example.com\"),",
				"\tA(\"y\")",
				"}"
		));
		final var result = fixer.fix(lines, 2, 1);
		assertFix(result, 1, 2, List.of("\tA(\"y\"),", "\tB(\"http://example.com\")"));
	}

	@Test
	public void testSameLineAndReorder() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tZEBRA, ALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 1, 8);
		assertFix(result, 1, 1, List.of("\tALPHA,", "\tZEBRA"));
	}

	@Test
	public void testSameLineTabSeparated() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tBETA,\tALPHA",
				"}"
		));
		final var result = fixer.fix(lines, 1, 6);
		assertFix(result, 1, 1, List.of("\tALPHA,", "\tBETA"));
	}

	@Test
	public void testSameLineThreeConstants() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tALPHA, BETA, GAMMA",
				"}"
		));
		final var result = fixer.fix(lines, 1, 8);
		assertFix(result, 1, 1, List.of("\tALPHA,", "\tBETA,", "\tGAMMA"));
	}

	@Test
	public void testSameLineTwoConstants() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tALPHA, BETA",
				"}"
		));
		final var result = fixer.fix(lines, 1, 8);
		assertFix(result, 1, 1, List.of("\tALPHA,", "\tBETA"));
	}

	@Test
	public void testSameLineTwoWithArgs() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tAPPLE(\"red\"), BANANA(\"yellow\")",
				"}"
		));
		final var result = fixer.fix(lines, 1, 16);
		assertFix(result, 1, 1, List.of("\tAPPLE(\"red\"),", "\tBANANA(\"yellow\")"));
	}

	@Test
	public void testSameLineTwoWithCommaInArgs() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tB(1, 2), A(3)",
				"}"
		));
		final var result = fixer.fix(lines, 1, 10);
		assertFix(result, 1, 1, List.of("\tA(3),", "\tB(1, 2)"));
	}

	@Test
	public void testSameLineWithSemicolon() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tALPHA, BETA;",
				"\tint x;",
				"}"
		));
		final var result = fixer.fix(lines, 1, 8);
		assertFix(result, 1, 1, List.of("\tALPHA,", "\tBETA;"));
	}

	@Test
	public void testSingleConstant() {
		final var lines = new ArrayList<>(List.of(
				"enum T {",
				"\tONLY_ONE",
				"}"
		));
		assertNull(fixer.fix(lines, 1, 1));
	}
}