package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AnnotationFixerUtilTest {
	@Test
	public void testAnnotationSortKeyQualified() {
		assertEquals("Nonnull", AnnotationFixerUtil.annotationSortKey("@javax.annotation.Nonnull"));
	}

	@Test
	public void testAnnotationSortKeySimple() {
		assertEquals("A", AnnotationFixerUtil.annotationSortKey("@A"));
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
	public void testParseAnnotationsTabSeparated() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A\t@B void f()");
		assertEquals(List.of("@A", "@B"), result.annotations());
		assertEquals("void f()", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedCharLiteral() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A('\\");
		assertEquals(List.of("@A('\\"), result.annotations());
		assertEquals("", result.remaining());
	}

	@Test
	public void testParseAnnotationsUnterminatedString() {
		final var result = AnnotationFixerUtil.parseAnnotations("@A(\"unterminated");
		assertEquals(List.of("@A(\"unterminated"), result.annotations());
		assertEquals("", result.remaining());
	}
}