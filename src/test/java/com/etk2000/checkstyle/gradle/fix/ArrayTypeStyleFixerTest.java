package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ArrayTypeStyleFixerTest {
	private final CheckstyleFixer fixer = new ArrayTypeStyleFixer();

	@Test
	public void testAnnotationArgumentParensNotTreatedAsMethodParens() {
		final var lines = new ArrayList<>(List.of("@Ann(\"x\") int x[], y;"));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testBlockCommentBeforeModifier() {
		final var lines = new ArrayList<>(List.of("/* doc */ final int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 21));
		assertEquals(List.of("/* doc */ final int[] x;"), result.replacement());
	}

	@Test
	public void testBlockCommentContainingOpenParenBeforeBracket() {
		final var lines = new ArrayList<>(List.of("/* ( */ int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 13));
		assertEquals(List.of("/* ( */ int[] x;"), result.replacement());
	}

	@Test
	public void testBoundedWildcardCStyle() {
		final var lines = new ArrayList<>(List.of("List<? super Integer> x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 23));
		assertEquals(List.of("List<? super Integer>[] x;"), result.replacement());
	}

	@Test
	public void testBracketAfterClosingBracketReturnsNull() {
		final var lines = new ArrayList<>(List.of("int[] [] x;"));
		assertNull(fixer.fix(lines, 0, 6));
	}

	@Test
	public void testBracketAtLineStartReturnsNull() {
		final var lines = new ArrayList<>(List.of("[] x;"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testCatchKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("catch (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testCharLiteralCommaInMultiVarInitReturnsNull() {
		final var lines = new ArrayList<>(List.of("char x[] = (c == ',' ? a : b), y = 0;"));
		assertNull(fixer.fix(lines, 0, 6));
	}

	@Test
	public void testCharLiteralWithParenInAnnotationParam() {
		final var input = "int m(@A('(') int x)[] { return null; }";
		final var expected = "int[] m(@A('(') int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testCloseParenInCharLiteralOnMethodReturn() {
		final var input = "int m(@A(')') int x)[] { return null; }";
		final var expected = "int[] m(@A(')') int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testCloseParenInStringLiteralOnMethodReturn() {
		final var input = "int m(@A(\")\") int x)[] { return null; }";
		final var expected = "int[] m(@A(\")\") int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testCommaAfterBracketsInVariableDeclReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[], y;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testCommaInTrailingLineCommentDoesNotBlock() {
		final var lines = new ArrayList<>(List.of("int x[] = a; // hello, world"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[] x = a; // hello, world"), result.replacement());
	}

	@Test
	public void testCommentAfterBracketBeforeAssignment() {
		final var lines = new ArrayList<>(List.of("int x[] /* note */ = a;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[] x /* note */ = a;"), result.replacement());
	}

	@Test
	public void testCommentBetweenCommaAndIdentMultiVarReturnsNull() {
		final var lines = new ArrayList<>(List.of("int i = 0, /* note */ x[] = a;"));
		assertNull(fixer.fix(lines, 0, 23));
	}

	@Test
	public void testCommentBetweenIdentAndBracketReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x /* */ [];"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testCompoundCStyle() {
		final var lines = new ArrayList<>(List.of("int x[][];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[][] x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDoKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("do (int x[] = a, b);"));
		assertNull(fixer.fix(lines, 0, 9));
	}

	@Test
	public void testEscapedApostropheInCharLiteral() {
		final var input = "int m(char c, @A('\\'') int x)[] { return null; }";
		final var expected = "int[] m(char c, @A('\\'') int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testEscapedBackslashAtEolInString() {
		final var lines = new ArrayList<>(List.of("String s = \"abc\\", "more\";", "int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 5));
		assertEquals(List.of("int[] x;"), result.replacement());
	}

	@Test
	public void testEscapedBackslashInAnnotationStringScansCorrectly() {
		final var input = "int m(@A(\"\\\\\") int x)[] { return null; }";
		final var expected = "int[] m(@A(\"\\\\\") int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testEscapedBackslashInStringSuccessSingleLine() {
		final var lines = new ArrayList<>(List.of("String s[] = \"a\\\\b\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(List.of("String[] s = \"a\\\\b\";"), result.replacement());
	}

	@Test
	public void testEscapedQuoteInsideAnnotationStringScansCorrectly() {
		final var input = "int m(@A(\"\\\"\") int x)[] { return null; }";
		final var expected = "int[] m(@A(\"\\\"\") int x) { return null; }";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of(expected), result.replacement());
	}

	@Test
	public void testExpressionContextNotMethodReturnReturnsNull() {
		final var lines = new ArrayList<>(List.of("x = bar()[]"));
		assertNull(fixer.fix(lines, 0, 9));
	}

	@Test
	public void testExtendsKeywordSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("class C extends X[] {}"));
		assertNull(fixer.fix(lines, 0, 17));
	}

	@Test
	public void testFakeIdentAfterMethodReturnBracketsReturnsNull() {
		final var lines = new ArrayList<>(List.of("int m()[] foo;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testFieldAccessNotMethodReturnReturnsNull() {
		final var lines = new ArrayList<>(List.of("obj.bar()[]"));
		assertNull(fixer.fix(lines, 0, 9));
	}

	@Test
	public void testFieldInitCallEndsInParenMultiVarReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x = compute()", "[], y = 1;"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testFinalModifier() {
		final var lines = new ArrayList<>(List.of("final int x[] = {1};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 11));
		assertEquals(List.of("final int[] x = {1};"), result.replacement());
	}

	@Test
	public void testForEachMethodNotTreatedAsForKeyword() {
		final var lines = new ArrayList<>(List.of("void forEach(int x[]) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 18));
		assertEquals(List.of("void forEach(int[] x) {}"), result.replacement());
	}

	@Test
	public void testForLoopMultiVarReturnsNull() {
		final var lines = new ArrayList<>(List.of("for (int x[] = a, y = 1; cond; step) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testForLoopSingleVarFixed() {
		final var lines = new ArrayList<>(List.of("for (int x[] = a; cond; step) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 10));
		assertEquals(List.of("for (int[] x = a; cond; step) {}"), result.replacement());
	}

	@Test
	public void testGenericMethodMultiParamLambda() {
		final var input = "BiConsumer<X, Y> b = (int a[], int y) -> {};";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("BiConsumer<X, Y> b = (int[] a, int y) -> {};"), result.replacement());
	}

	@Test
	public void testGenericMethodMultiParamLambdaCStyleOnLast() {
		final var input = "BiConsumer<X, Y> b = (int a, int y[]) -> {};";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("BiConsumer<X, Y> b = (int a, int[] y) -> {};"), result.replacement());
	}

	@Test
	public void testGenericMethodMultiParamLambdaCStyleOnMiddle() {
		final var input = "TriConsumer<X, Y, Z> t = (int a, int b[], int c) -> {};";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("TriConsumer<X, Y, Z> t = (int a, int[] b, int c) -> {};"), result.replacement());
	}

	@Test
	public void testGenericMethodReturnType() {
		final var lines = new ArrayList<>(List.of("List<String> m()[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 16));
		assertEquals(List.of("List<String>[] m() { return null; }"), result.replacement());
	}

	@Test
	public void testGenericRecordMultiComponent() {
		final var input = "record R<T>(int x[], String s) {}";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("record R<T>(int[] x, String s) {}"), result.replacement());
	}

	@Test
	public void testGenericRecordMultiTypeParam() {
		final var input = "record R<K, V>(int x[], V v) {}";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("record R<K, V>(int[] x, V v) {}"), result.replacement());
	}

	@Test
	public void testGenericRecordNestedTypeBounds() {
		final var input = "record R<T extends List<String>>(int x[], int y) {}";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("record R<T extends List<String>>(int[] x, int y) {}"), result.replacement());
	}

	@Test
	public void testGenericReturnNestedTypeArgs() {
		final var lines = new ArrayList<>(List.of("Map<K, List<V>> m()[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 19));
		assertEquals(List.of("Map<K, List<V>>[] m() { return null; }"), result.replacement());
	}

	@Test
	public void testGenericTypeCStyle() {
		final var lines = new ArrayList<>(List.of("List<String> x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 14));
		assertEquals(List.of("List<String>[] x;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIdentAtLineStartReturnsNull() {
		final var lines = new ArrayList<>(List.of("foo[];"));
		assertNull(fixer.fix(lines, 0, 3));
	}

	@Test
	public void testIfKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("if (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testImplementsKeywordSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("class C implements X[] {}"));
		assertNull(fixer.fix(lines, 0, 20));
	}

	@Test
	public void testMethodIdentAtLineStartReturnsNull() {
		final var lines = new ArrayList<>(List.of("m()[];"));
		assertNull(fixer.fix(lines, 0, 3));
	}

	@Test
	public void testMethodMultiParamCommaSeparated() {
		final var lines = new ArrayList<>(List.of("void m(int a[], int b)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals(List.of("void m(int[] a, int b)"), result.replacement());
	}

	@Test
	public void testMethodMultiParamCStyleOnLast() {
		final var lines = new ArrayList<>(List.of("void m(int x, int y[])"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 19));
		assertEquals(List.of("void m(int x, int[] y)"), result.replacement());
	}

	@Test
	public void testMethodMultiParamCStyleOnMiddle() {
		final var lines = new ArrayList<>(List.of("void m(int a, int b[], int c)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 19));
		assertEquals(List.of("void m(int a, int[] b, int c)"), result.replacement());
	}

	@Test
	public void testMethodReturnFollowedByPartialThrowsReturnsNull() {
		final var lines = new ArrayList<>(List.of("int m()[] thrown;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testMethodReturnFollowedByThrowsLikeIdentReturnsNull() {
		final var lines = new ArrayList<>(List.of("int m()[] throwsException;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testMethodReturnTypeAbstractSemicolon() {
		final var lines = new ArrayList<>(List.of("int m()[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(List.of("int[] m();"), result.replacement());
	}

	@Test
	public void testMethodReturnTypeArrayPrefixed() {
		final var lines = new ArrayList<>(List.of("int[] m()[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(List.of("int[][] m() { return null; }"), result.replacement());
	}

	@Test
	public void testMethodReturnTypeBraceBody() {
		final var lines = new ArrayList<>(List.of("int method()[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals(List.of("int[] method() { return null; }"), result.replacement());
	}

	@Test
	public void testMethodReturnTypeCompound() {
		final var lines = new ArrayList<>(List.of("int method()[][] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals(List.of("int[][] method() { return null; }"), result.replacement());
	}

	@Test
	public void testMethodReturnTypeWithThrows() {
		final var lines = new ArrayList<>(List.of("int method()[] throws Exception { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals(List.of("int[] method() throws Exception { return null; }"), result.replacement());
	}

	@Test
	public void testMethodReturnWithTypeUseAnnotationReturnsNull() {
		final var lines = new ArrayList<>(List.of("int m() @A []"));
		assertNull(fixer.fix(lines, 0, 11));
	}

	@Test
	public void testMixedJavaAndCStyle() {
		final var lines = new ArrayList<>(List.of("int[] x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(List.of("int[][] x;"), result.replacement());
	}

	@Test
	public void testMultiLineAbstractMethodReturn() {
		final var lines = new ArrayList<>(List.of("int method()", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] method()", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineBlockCommentSpansAcrossLinesFollowedByCommaReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] /*", "fake */, y;"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineBlockCommentSpansAcrossLinesWithFakeComma() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] /*", "fake , */;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x", "\t\t /*"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineBlockCommentSpansLinesContainingComma() {
		final var lines = new ArrayList<>(List.of("int x[] /*", "fake , inside", "*/ , y;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testMultiLineBracketLineHasInitializer() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] = {1};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x", "\t\t = {1};"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineBracketLineHasMethodBody() {
		final var lines = new ArrayList<>(List.of("int method()", "\t\t[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] method()", "\t\t { return null; }"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineBracketsUnclosedReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "[abc];"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testMultiLineDeclEndingAtClosingBraceTreatedAsTerminator() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[]", "}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineEofWithoutTerminator() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[]"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineFinalModifier() {
		final var lines = new ArrayList<>(List.of("final int x", "\t\t[] = {1};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("final int[] x", "\t\t = {1};"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineFinalWithAnnotation() {
		final var lines = new ArrayList<>(List.of("final @Deprecated int x", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("final @Deprecated int[] x", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineFirstLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\t[];"));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testMultiLineGenericMethodReturn() {
		final var lines = new ArrayList<>(List.of("<T> T method()", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("<T> T[] method()", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineLineCommentBeforeCommaOnNextLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "[] = a // comment", ", y = 0;"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testMultiLineMethodReturn() {
		final var lines = new ArrayList<>(List.of("int method()", "\t\t[]", "\t\t{ return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] method()"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineMethodReturnWithCommentOnPrevLine() {
		final var lines = new ArrayList<>(List.of("int method() // comment", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] method() // comment", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineMethodReturnWithMultiThrows() {
		final var lines = new ArrayList<>(List.of("int method()", "\t\t[] throws E1, E2 { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] method()", "\t\t throws E1, E2 { return null; }"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineMultiVarOnBracketLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "[], y;"));
		assertNull(fixer.fix(lines, 1, 0));
	}

	@Test
	public void testMultiLineMultiVarOnFollowingLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[]", ", y;"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineMultiVarReturnsNull() {
		final var lines = new ArrayList<>(List.of("int alpha, beta", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineMultiVarSpanningParenInitReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] = foo(a,", "b), y = 0;"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInBraceReturnsNull() {
		final var lines = new ArrayList<>(List.of("class C {", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInExtendsReturnsNull() {
		final var lines = new ArrayList<>(List.of("class C extends X", "\t\t[] {}"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInImplementsReturnsNull() {
		final var lines = new ArrayList<>(List.of("class C implements X", "\t\t[] {}"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInPermitsReturnsNull() {
		final var lines = new ArrayList<>(List.of("sealed class C permits X", "\t\t[] {}"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInSemicolonReturnsNull() {
		final var lines = new ArrayList<>(List.of("int y = 0;", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineEndsInThrowsIdent() {
		final var lines = new ArrayList<>(List.of("int m() throws E", "\t\t[] { return null; }"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLinePrevLineSingleIdentReturnsNull() {
		final var lines = new ArrayList<>(List.of("x", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineSimpleVar() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineStringLiteralInitWithCommaReturnsNull() {
		final var lines = new ArrayList<>(List.of("String x", "\t\t[] = \",\", y = \"\";"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineSuperDenylistReturnsNull() {
		final var lines = new ArrayList<>(List.of("super X", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineTextBlockOnBracketLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("String x", "\t\t[] = \"\"\""));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineTextBlockOnLaterLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] = a", "\"\"\""));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineTypeUseAnnotationOnPrevLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x @Anno", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineUnterminatedStringDoesNotEatCommaOnNextLine() {
		final var lines = new ArrayList<>(List.of("int x", "\t\t[] = \"abc", ", y = 0;"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineWithAnnotation() {
		final var lines = new ArrayList<>(List.of("@Deprecated int x", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("@Deprecated int[] x", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithBlockCommentOnPrevLine() {
		final var lines = new ArrayList<>(List.of("int x /* note */", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[] x /* note */", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithEmptyPreviousLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultiLineWithGenericPrevReturn() {
		final var lines = new ArrayList<>(List.of("List<String> method()", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("List<String>[] method()", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithGenericPrevVar() {
		final var lines = new ArrayList<>(List.of("List<String> x", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("List<String>[] x", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithJavaStyleArrayPrevReturn() {
		final var lines = new ArrayList<>(List.of("int[] method()", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[][] method()", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithJavaStyleArrayPrevVar() {
		final var lines = new ArrayList<>(List.of("int[] x", "\t\t[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("int[][] x", "\t\t;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithStringLiteralInitializer() {
		final var lines = new ArrayList<>(List.of("String x", "\t\t[] = \"abc\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 2));
		assertEquals(List.of("String[] x", "\t\t = \"abc\";"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
	}

	@Test
	public void testMultiLineWithWhitespaceOnlyPrevLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\t", "\t\t[];"));
		assertNull(fixer.fix(lines, 1, 2));
	}

	@Test
	public void testMultipleModifiers() {
		final var lines = new ArrayList<>(List.of("public static final int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 25));
		assertEquals(List.of("public static final int[] x;"), result.replacement());
	}

	@Test
	public void testMultipleTypeArgsCStyle() {
		final var lines = new ArrayList<>(List.of("Map<K, V> x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 11));
		assertEquals(List.of("Map<K, V>[] x;"), result.replacement());
	}

	@Test
	public void testMultiVarBraceInitializerReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[] = {1, 2}, y = 0;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testMultiVarBracketIndexInitReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[] = arr[i], y = 0;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testMultiVarFunctionCallInitReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[] = foo(a, b), y = 0;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testMultiVarPrevReturnsNull() {
		final var lines = new ArrayList<>(List.of("int alpha, beta[];"));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testMultiVarSpanningParenInitOnSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[] = foo(a,", "b), y = 0;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testMultiVarStringLiteralInitReturnsNull() {
		final var lines = new ArrayList<>(List.of("String x[] = \",\", y = \"\";"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testMultiVarWithInitializerReturnsNull() {
		final var lines = new ArrayList<>(List.of("final int gamma[] = {1}, delta = 0;"));
		assertNull(fixer.fix(lines, 0, 15));
	}

	@Test
	public void testNestedGenericCStyle() {
		final var lines = new ArrayList<>(List.of("Map<String, List<Integer>> x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 28));
		assertEquals(List.of("Map<String, List<Integer>>[] x;"), result.replacement());
	}

	@Test
	public void testNoSuffixReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[]"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNotABracketAtColumn() {
		final var lines = new ArrayList<>(List.of("int x = 5;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNoTypeBeforeIdentReturnsNull() {
		final var lines = new ArrayList<>(List.of("x[];"));
		assertNull(fixer.fix(lines, 0, 1));
	}

	@Test
	public void testOpenParenAtLineStartTreatedAsParens() {
		final var lines = new ArrayList<>(List.of("(int x[])"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(List.of("(int[] x)"), result.replacement());
	}

	@Test
	public void testOrphanCloseParenIgnored() {
		final var lines = new ArrayList<>(List.of(") int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(List.of(") int[] x;"), result.replacement());
	}

	@Test
	public void testOrphanCloseParenWithoutMatchReturnsNull() {
		final var lines = new ArrayList<>(List.of("})[];"));
		assertNull(fixer.fix(lines, 0, 2));
	}

	@Test
	public void testOutOfBoundsColumn() {
		final var lines = new ArrayList<>(List.of("int x[];"));
		assertNull(fixer.fix(lines, 0, -1));
		assertNull(fixer.fix(lines, 0, 100));
	}

	@Test
	public void testOutOfBoundsLineIndex() {
		final var lines = new ArrayList<>(List.of("int x[];"));
		assertNull(fixer.fix(lines, -1, 5));
		assertNull(fixer.fix(lines, 1, 5));
	}

	@Test
	public void testParameterCStyle() {
		final var lines = new ArrayList<>(List.of("void m(int x[])"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 12));
		assertEquals(List.of("void m(int[] x)"), result.replacement());
	}

	@Test
	public void testPermitsKeywordSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("sealed class C permits X[] {}"));
		assertNull(fixer.fix(lines, 0, 24));
	}

	@Test
	public void testRecordComponentCStyle() {
		final var lines = new ArrayList<>(List.of("record R(int x[]) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 14));
		assertEquals(List.of("record R(int[] x) {}"), result.replacement());
	}

	@Test
	public void testRecordMultiComponentCommaSeparated() {
		final var lines = new ArrayList<>(List.of("record R(int a[], String s) {}"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 14));
		assertEquals(List.of("record R(int[] a, String s) {}"), result.replacement());
	}

	@Test
	public void testSimpleCStyle() {
		final var lines = new ArrayList<>(List.of("int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[] x;"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringLiteralWithParenBeforeBracketReturnsNull() {
		final var lines = new ArrayList<>(List.of("String s = \"(\", x[];"));
		assertNull(fixer.fix(lines, 0, 17));
	}

	@Test
	public void testSuperKeywordSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("super[] x;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testSwitchKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("switch (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 13));
	}

	@Test
	public void testSynchronizedKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("synchronized (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 19));
	}

	@Test
	public void testTabBetweenTypeAndIdent() {
		final var lines = new ArrayList<>(List.of("int\tx[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[]\tx;"), result.replacement());
	}

	@Test
	public void testTabIndented() {
		final var lines = new ArrayList<>(List.of("\t\tint x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(List.of("\t\tint[] x;"), result.replacement());
	}

	@Test
	public void testThrowsKeywordSingleLineReturnsNull() {
		final var lines = new ArrayList<>(List.of("int m() throws E[];"));
		assertNull(fixer.fix(lines, 0, 16));
	}

	@Test
	public void testTripleQuoteInLineCommentBailsConservatively() {
		final var lines = new ArrayList<>(List.of("int x[] = a; // \"\"\" in comment"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testTryKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("try (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 10));
	}

	@Test
	public void testTypeParameterMethodReturn() {
		final var lines = new ArrayList<>(List.of("<T> T m()[] { return null; }"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(List.of("<T> T[] m() { return null; }"), result.replacement());
	}

	@Test
	public void testTypeUseAnnotationInParameterReturnsNull() {
		final var lines = new ArrayList<>(List.of("void m(int x @A [])"));
		assertNull(fixer.fix(lines, 0, 16));
	}

	@Test
	public void testTypeUseAnnotationInRecordReturnsNull() {
		final var lines = new ArrayList<>(List.of("record R(int x @A [])"));
		assertNull(fixer.fix(lines, 0, 18));
	}

	@Test
	public void testTypeUseAnnotationInTypeArgsCStyle() {
		final var input = "List<@A String> x[];";
		final var lines = new ArrayList<>(List.of(input));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, input.indexOf("[]")));
		assertEquals(List.of("List<@A String>[] x;"), result.replacement());
	}

	@Test
	public void testTypeUseAnnotationOnBracketReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x @Anno [];"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testUnclosedBracketReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[abc];"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testWhileKeywordNotTreatedAsParamList() {
		final var lines = new ArrayList<>(List.of("while (int x[] = a, b) {}"));
		assertNull(fixer.fix(lines, 0, 12));
	}

	@Test
	public void testWhitespaceBeforeBracket() {
		final var lines = new ArrayList<>(List.of("int x [];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(List.of("int[] x;"), result.replacement());
	}

	@Test
	public void testWhitespaceInsideBrackets() {
		final var lines = new ArrayList<>(List.of("int x[ ];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[ ] x;"), result.replacement());
	}

	@Test
	public void testWildcardGenericCStyle() {
		final var lines = new ArrayList<>(List.of("List<? extends Number> x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 24));
		assertEquals(List.of("List<? extends Number>[] x;"), result.replacement());
	}

	@Test
	public void testWithAnnotation() {
		final var lines = new ArrayList<>(List.of("@Deprecated int x[];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 17));
		assertEquals(List.of("@Deprecated int[] x;"), result.replacement());
	}

	@Test
	public void testWithInitializer() {
		final var lines = new ArrayList<>(List.of("int x[] = {1};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(List.of("int[] x = {1};"), result.replacement());
	}

	@Test
	public void testWithInvalidSuffixReturnsNull() {
		final var lines = new ArrayList<>(List.of("int x[]:"));
		assertNull(fixer.fix(lines, 0, 5));
	}
}