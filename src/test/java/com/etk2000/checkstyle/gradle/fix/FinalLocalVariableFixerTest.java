package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class FinalLocalVariableFixerTest {
	private static final String TOPIC = "finallocalvariable";

	private final CheckstyleFixer fixer = new FinalLocalVariableFixer();

	@Test
	public void testAlreadyFinalDeclPrecededByStatementSkips() {
		final var line = "\t\tX = Foo.X; final int y = 0;";
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(SkipMessages.FINAL_LOCAL_ALREADY_FINAL, result.reason());
	}

	@Test
	public void testBlankLineOutOfBoundsColumn() {
		assertEquals(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE, assertInstanceOf(SkipResult.class, fixer.fix(List.of(""), 0, 5)).reason());
	}

	@Test
	public void testBoundaryCharInBlockCommentNotTreatedAsBoundary() {
		final var line = "\t\t/* } */ foo(); int y = 0;";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\t\t/* } */ foo(); final int y = 0;"), result.replacement());
	}

	@Test
	public void testDeclAfterCloseBraceOnSameLineInsertsFinalBeforeType() {
		final var line = "\t\tif (b) {} int y = 0;";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\t\tif (b) {} final int y = 0;"), result.replacement());
	}

	@Test
	public void testDeclAfterOpenBraceOnSameLineInsertsFinalBeforeType() {
		final var line = "\t\tif (b) { int y = 0; }";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\t\tif (b) { final int y = 0; }"), result.replacement());
	}

	@Test
	public void testDeclPrecededByStatementOnSameLineInsertsFinalBeforeType() {
		final var line = "\t\tX = Foo.X; int y = 0;";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\t\tX = Foo.X; final int y = 0;"), result.replacement());
	}

	@Test
	public void testInvalidColumnOnNonBlankLineSkips() {
		final var line = "\tint y = 0;";
		assertEquals(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE, assertInstanceOf(SkipResult.class, fixer.fix(List.of(line), 0, -1)).reason());
		assertEquals(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE, assertInstanceOf(SkipResult.class, fixer.fix(List.of(line), 0, line.length() + 5)).reason());
	}

	@Test
	public void testLineStartDeclInsertsFinalAfterIndent() {
		final var line = "\tint y = 0;";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\tfinal int y = 0;"), result.replacement());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"break", "case", "catch", "continue", "default", "do", "else", "finally",
			"for", "if", "return", "switch", "throw", "try", "while", "yield"
	})
	public void testNonDeclKeywordLineRejected(String keyword) {
		final var line = "\t" + keyword + " r;";
		final var result = assertInstanceOf(
				SkipResult.class,
				fixer.fix(List.of(line), 0, line.length() - 2)
		);
		assertEquals("no declaration type line precedes the variable name", result.reason());
	}

	@Test
	public void testSemicolonInStringLiteralNotTreatedAsBoundary() {
		final var line = "\t\tString s = \";\"; int y = 0;";
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(line), 0, line.indexOf('y')));
		assertEquals(List.of("\t\tString s = \";\"; final int y = 0;"), result.replacement());
	}

	@Test
	public void testSpaceIndentation() throws Exception {
		// can't migrate: probes fixer leading-whitespace handling with space-indented input; class wrapping is tab-indented, so space-only indentation is unreachable when the check fires
		assertSimpleFix(fixer, TOPIC, "space_indentation");
	}

	@Test
	public void testSplitFixResultTargetsTypeLine() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of("X", "\tY"), 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
	}

	@Test
	public void testSplitNameOnFirstLineWithNoTypeLineSkips() {
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(List.of("\t\tr = x;"), 0, 2));
		assertEquals(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE, result.reason());
	}
}