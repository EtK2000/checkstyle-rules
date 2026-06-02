package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AnnotationFixerUtilTest {
	@Test
	public void testAnnotationSortKeyBareAt() {
		assertEquals("", AnnotationFixerUtil.annotationSortKey("@"));
	}

	@Test
	public void testAnnotationSortKeyBareAtWithParen() {
		assertEquals("", AnnotationFixerUtil.annotationSortKey("@("));
	}

	@Test
	public void testAnnotationSortKeyEmpty() {
		assertEquals("", AnnotationFixerUtil.annotationSortKey(""));
	}

	@Test
	public void testAnnotationSortKeyParenFirst() {
		assertEquals("", AnnotationFixerUtil.annotationSortKey("("));
	}

	@Test
	public void testAnnotationSortKeyQualified() {
		assertEquals("Nonnull", AnnotationFixerUtil.annotationSortKey("@javax.annotation.Nonnull"));
	}

	@Test
	public void testAnnotationSortKeyQualifiedArgumentConstant() {
		// the last '.' sits in the argument, not the name; keying off it put start past end
		assertEquals("Target", AnnotationFixerUtil.annotationSortKey("@Target(ElementType.TYPE)"));
	}

	@Test
	public void testAnnotationSortKeyQualifiedNameAndArgumentConstant() {
		assertEquals("C", AnnotationFixerUtil.annotationSortKey("@a.b.C(x.y)"));
	}

	@Test
	public void testAnnotationSortKeySimple() {
		assertEquals("A", AnnotationFixerUtil.annotationSortKey("@A"));
	}

	@Test
	public void testAnnotationSortKeyStringArgumentContainingDot() {
		assertEquals("Foo", AnnotationFixerUtil.annotationSortKey("@Foo(\"a.b\")"));
	}

	@Test
	public void testAnnotationSortKeyWithParams() {
		assertEquals("SuppressWarnings", AnnotationFixerUtil.annotationSortKey("@SuppressWarnings(\"unused\")"));
	}

	@Test
	public void testIsAnnotationOnlyLineAnnotationWithParams() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A(\"value\")"));
	}

	@Test
	public void testIsAnnotationOnlyLineBareAt() {
		// deliberately diverges from parseAnnotations, which refuses a nameless '@'
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@"));
	}

	@Test
	public void testIsAnnotationOnlyLineBareAtWithParen() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@("));
	}

	@Test
	public void testIsAnnotationOnlyLineEmpty() {
		assertFalse(AnnotationFixerUtil.isAnnotationOnlyLine(""));
	}

	@Test
	public void testIsAnnotationOnlyLineMultiple() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A @B"));
	}

	@Test
	public void testIsAnnotationOnlyLineMultipleTabSeparated() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A\t@B"));
	}

	@Test
	public void testIsAnnotationOnlyLineNotAnnotation() {
		assertFalse(AnnotationFixerUtil.isAnnotationOnlyLine("void f()"));
	}

	@Test
	public void testIsAnnotationOnlyLineSingle() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A"));
	}

	@Test
	public void testIsAnnotationOnlyLineSupplementaryInName() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A\uD835\uDC00b"));
	}

	@Test
	public void testIsAnnotationOnlyLineUnterminatedBareParen() {
		// deliberately diverges from parseAnnotations, which bails on an unterminated '('
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A("));
	}

	@Test
	public void testIsAnnotationOnlyLineUnterminatedCharLiteral() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A('\\"));
	}

	@Test
	public void testIsAnnotationOnlyLineUnterminatedString() {
		assertTrue(AnnotationFixerUtil.isAnnotationOnlyLine("@A(\"unterminated"));
	}

	@Test
	public void testIsAnnotationOnlyLineWithTrailingContent() {
		assertFalse(AnnotationFixerUtil.isAnnotationOnlyLine("@A void f()"));
	}

	@Test
	public void testParseAnnotationsBareAt() {
		final var result = AnnotationFixerUtil.parseAnnotations("@");
		assertEquals(List.of(), result.annotations());
		assertEquals("@", result.remaining());
	}

	@Test
	public void testParseAnnotationsBareAtWithParen() {
		final var result = AnnotationFixerUtil.parseAnnotations("@(");
		assertEquals(List.of(), result.annotations());
		assertEquals("@(", result.remaining());
	}

	@Test
	public void testParseAnnotationsCharLiteral() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A('\\'') @B void f()");
		assertEquals(List.of("@A('\\'')", "@B"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsEmpty() {
		final var result = AnnotationFixerUtil.parseAnnotations("");
		assertTrue(result.annotations().isEmpty());
		assertEquals("", result.remaining());
	}

	@Test
	public void testParseAnnotationsEscapedString() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(\"he said \\\"hi\\\"\") @B void f()");
		assertEquals(List.of("@A(\"he said \\\"hi\\\"\")", "@B"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsMultiple() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A @B @C void f()");
		assertEquals(List.of("@A", "@B", "@C"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsNestedAnnotation() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(@B) void f()");
		assertEquals(List.of("@A(@B)"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsNestedParens() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(v = (1 + 2)) void f()");
		assertEquals(List.of("@A(v = (1 + 2))"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsNoAnnotation() {
		final var result = AnnotationFixerUtil.parseAnnotations("void f()");
		assertTrue(result.annotations().isEmpty());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsQualified() {
		final var result = AnnotationFixerUtil.parseAnnotations("@javax.annotation.Nonnull void f()");
		assertEquals(List.of("@javax.annotation.Nonnull"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsSingleMarker() {
		final var result = AnnotationFixerUtil.parseAnnotations("@Override void f()");
		assertEquals(List.of("@Override"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsSingleWithValue() {
		final var result = AnnotationFixerUtil.parseAnnotations("@SuppressWarnings(\"unused\") void f()");
		assertEquals(List.of("@SuppressWarnings(\"unused\")"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsSpaceAfterAtIsNotAnAnnotation() {
		// `@ Deprecated` is legal Java; emitting a bare "@" splits it onto its own line
		final var result = AnnotationFixerUtil.parseAnnotations("@ Deprecated void f()");
		assertEquals(List.of(), result.annotations());
		assertEquals("@ Deprecated void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsSupplementaryInName() {
		// a char-wise name scan stops inside the surrogate pair and splits it across the
		// annotation and the remaining content
		final var result = AnnotationFixerUtil.parseAnnotations("@A\uD835\uDC00b @B void f()");
		assertEquals(List.of("@A\uD835\uDC00b", "@B"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsTabSeparated() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A\t@B void f()");
		assertEquals(List.of("@A", "@B"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedBareParen() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(");
		assertEquals(List.of(), result.annotations());
		assertEquals("@A(", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedCharLiteral() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A('\\");
		assertEquals(List.of(), result.annotations());
		assertEquals("@A('\\", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedNestedAnnotation() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(@B");
		assertEquals(List.of(), result.annotations());
		assertEquals("@A(@B", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedString() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(\"unterminated");
		assertEquals(List.of(), result.annotations());
		assertEquals("@A(\"unterminated", result.remaining());
	}

	@Test
	public void testParseAnnotationsValidThenSpaceAfterAt() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A @ Deprecated void f()");
		assertEquals(List.of("@A"), result.annotations());
		assertEquals("@ Deprecated void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsValidThenUnterminatedParen() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A @B(");
		assertEquals(List.of("@A"), result.annotations());
		assertEquals("@B(", result.remaining());
	}
}