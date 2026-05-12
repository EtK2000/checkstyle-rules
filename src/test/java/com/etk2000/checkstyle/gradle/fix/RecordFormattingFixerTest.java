package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class RecordFormattingFixerTest {
	private final CheckstyleFixer fixer = new RecordFormattingFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {}"));
		assertNull(fixer.fix(lines, 0, 100));
	}

	@Test
	public void testFixBraceNewlineEmpty() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a)", "\t{}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixBraceNewlineWithBody() {
		final var lines = new ArrayList<>(List.of(
				"\trecord R(int a)",
				"\t{",
				"\t\tvoid m() {}",
				"\t}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a) {"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentBitshiftInAnnotation() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(1 << 4) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(1 << 4) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentBlockCommentWithCommaInAnnotation() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(/* a, b */ 1) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(/* a, b */ 1) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentCollapseMixedToSingleLine() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentCommaInAnnotationParens() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(b = 1, c = 2) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(b = 1, c = 2) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentEmptyLeadingComponentReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(, int a,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentEmptyMiddleComponentReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a, ,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentExpandWideLineToStyleB() {
		final var sb = new StringBuilder("\trecord WideRecord(int aaaaaaaa,\n");
		sb.append("\t\t\tint bbbbbbbb, int cccccccc, int dddddddd, int eeeeeeee, int ffffffff, int gggggggg) {}");
		final var lines = new ArrayList<>(List.of(sb.toString().split("\n")));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 18));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		final var expected = List.of(
				"\trecord WideRecord(",
				"\t\t\tint aaaaaaaa,",
				"\t\t\tint bbbbbbbb,",
				"\t\t\tint cccccccc,",
				"\t\t\tint dddddddd,",
				"\t\t\tint eeeeeeee,",
				"\t\t\tint ffffffff,",
				"\t\t\tint gggggggg",
				"\t) {}"
		);
		assertEquals(expected, result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentGreaterEqualInAnnotation() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(X >= 1) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(X >= 1) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentGreaterThanInAnnotationInsideGenerics() {
		final var lines = new ArrayList<>(List.of("\trecord R(List<@A(x > 5) Integer> m,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(List<@A(x > 5) Integer> m, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentLessThanInAnnotation() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(x < 5) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(x < 5) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentLineCommentOnIntermediateLineReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\trecord R(",
				"\t\t\tint a, // note",
				"\t\t\tint b",
				"\t) {}"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testFixComponentLineCommentOnRparenLineReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\trecord R(",
				"\t\t\tint a,",
				"\t// note ) {}"
		));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testFixComponentMultiLineAlreadyCorrectNoOp() {
		final var lines = new ArrayList<>(List.of(
				"record R(",
				"\t\tint " + "x".repeat(50) + ",",
				"\t\tint " + "y".repeat(50),
				") {}"
		));
		assertNull(fixer.fix(lines, 1, 10));
	}

	@Test
	public void testFixComponentMultiPerLineCollapses() {
		final var lines = new ArrayList<>(List.of("\trecord R(", "\t\t\tint a, int b,", "\t\t\tint c", "\t) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 10));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\trecord R(int a, int b, int c) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentNestedGenerics() {
		final var lines = new ArrayList<>(List.of(
				"\trecord R(java.util.Map<String, java.util.List<Integer>> m,",
				"\t\tint x) {}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(
				List.of("\trecord R(java.util.Map<String, java.util.List<Integer>> m, int x) {}"),
				result.replacement()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentRecordKeywordAfterSupplementaryIdentCharReturnsNull() {
		// U+1D400 (MATHEMATICAL BOLD CAPITAL A) is a Java identifier part. The "record" text that
		// follows it is part of the identifier, not the keyword. The buggy form (using
		// `Character.isJavaIdentifierPart(char)`) would see the low surrogate alone, reject it as
		// an identifier part, and false-positive-match the keyword.
		final var lines = new ArrayList<>(List.of("\tfoo(x\uD835\uDC00record(a,", "\t\tb)) {}"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testFixComponentRightShiftInAnnotation() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(64 >> 2) int a,", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(@A(64 >> 2) int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentSingleLineAlreadyCorrectNoOp() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a, int b) {}"));
		assertNull(fixer.fix(lines, 0, 14));
	}

	@Test
	public void testFixComponentTrailingCommaReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a, int b,", "\t\tint c, ) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentUnbalancedAngleBracketsReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(List<String x, int y)", "\t{}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentUnterminatedBlockCommentInHeaderReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a, /* unterminated", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentUnterminatedCharInHeaderReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A('unterminated) int a,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentUnterminatedStringHidingRecordKeyword() {
		final var lines = new ArrayList<>(List.of("\tString s = \"unterminated record R(int a,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testFixComponentUnterminatedStringInHeaderReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R(@A(\"unterminated) int a,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testFixComponentUnterminatedTextBlockInHeaderReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"\trecord R(",
				"\t\t@A(\"\"\"abc",
				"\t\tcontent\"\"\") int a",
				"\t) {}"
		));
		assertNull(fixer.fix(lines, 1, 10));
	}

	@Test
	public void testFixComponentWithAnnotationComparisonInGenericBound() {
		final var lines = new ArrayList<>(List.of("\trecord R<T extends @A(1 < 2) Object>(T a,", "\t\tT b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 40));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R<T extends @A(1 < 2) Object>(T a, T b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentWithAnnotationGreaterThanInGenericBound() {
		final var lines = new ArrayList<>(List.of("\trecord R<T extends @A(x > 0) Object>(T a,", "\t\tT b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 40));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R<T extends @A(x > 0) Object>(T a, T b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentWithBlockCommentInHeader() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a,", "\t/* note */", "\t\tint b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\trecord R(int a, /* note */ int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentWithBoundedGeneric() {
		final var lines = new ArrayList<>(List.of("\trecord R<T extends java.util.Map<K, V>>(T a,", "\t\tT b) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 40));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R<T extends java.util.Map<K, V>>(T a, T b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentWithGenericTypeArg() {
		final var lines = new ArrayList<>(List.of("\trecord R(java.util.Map<String, Integer> m,", "\t\tint x) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(java.util.Map<String, Integer> m, int x) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixComponentWithLineCommentInHeaderReturnsNull() {
		final var lines = new ArrayList<>(List.of("\trecord R( // note", "\t\tint a,", "\t\tint b) {}"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testFixComponentWithStringContainingRecordOnPriorLine() {
		final var lines = new ArrayList<>(List.of(
				"\tString s = \"record FAKE(int x)\";",
				"\trecord Real(int a,",
				"\t\tint b) {}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 13));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\trecord Real(int a, int b) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixEmptyBodyBracesSplit() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {", "\t}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixEmptyBodyBracesSplitIntermediateContent() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {", "\t\tvoid m() {}", "\t}"));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testFixEmptyBodyBracesSplitOpenBraceTrailingContent() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { stmt;", "\t}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixEmptyBodyBracesSplitOpenLineHasBlockComment() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) /* { */ {", "\t}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a) /* { */ {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixEmptyBodyBracesSplitOpenLineHasLineComment() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { // note", "\t}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixEmptyBodyBracesSplitTrailingContent() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {", "\t} // note"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixEmptyBodyCloseOnLineAfterUnterminatedString() {
		final var lines = new ArrayList<>(List.of("\tString s = \"unterminated", "\t}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixImplementsMultiLine() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) implements", "\t\t\tFoo", "\t{}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(1, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\t\tFoo {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixImplementsNoSpace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) implements Foo{}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 31));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) implements Foo {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixNonEmptyBodyBlockCommentContainingBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { /* } { */ int x = 1; }"));
		final var line = lines.getFirst();
		final var lastBraceCol = line.lastIndexOf('}');
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, lastBraceCol));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {", "\t\t/* } { */ int x = 1;", "\t}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixNonEmptyBodyCharLiteralContainingBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { char c = '}'; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 33));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {", "\t\tchar c = '}';", "\t}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixNonEmptyBodySameLine() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { void m() {} }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 31));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {", "\t\tvoid m() {}", "\t}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixNonEmptyBodyStringContainingBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) { String s = \"{\"; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 35));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {", "\t\tString s = \"{\";", "\t}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixNoSpaceBeforeBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a){}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 16));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixOpenBraceAfterLineComment() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) // note", "\t{}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixOpenBraceAfterTerminatedBlockComment() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) /* note */", "\t{}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\trecord R(int a) /* note */ {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixOpenBraceAfterUnterminatedBlockComment() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) /* unterminated", "\t{}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixOpenBraceAfterUnterminatedCharLiteral() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) /* */ 'unterminated", "\t{}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixOpenBraceAfterUnterminatedStringLiteral() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) /* */ \"unterminated", "\t{}"));
		assertNull(fixer.fix(lines, 1, 1));
	}

	@Test
	public void testFixOpenBraceAlreadyCorrectNoOp() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {}"));
		assertNull(fixer.fix(lines, 0, 17));
	}

	@Test
	public void testFixOpenBraceNoAnchor() {
		final var lines = new ArrayList<>(List.of("", "{}"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testFixTabBeforeBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a)\t{}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 17));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFixTwoSpacesBeforeBrace() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a)  {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 18));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\trecord R(int a) {}"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLineIndexOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {}"));
		assertNull(fixer.fix(lines, 10, 0));
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {}"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNegativeLineIndex() {
		final var lines = new ArrayList<>(List.of("\trecord R(int a) {}"));
		assertNull(fixer.fix(lines, -1, 0));
	}
}