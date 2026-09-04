package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class RedundantNumericSuffixCheckTest {
	@ParameterizedTest
	@ValueSource(strings = {
			"class T { long x = 1L; }",
			"class T { java.lang.Object x = (long) 1L; }",
			"class T { java.lang.Long x = (long) 1L; }",
			"class T { java.lang.Long[] x = new java.lang.Long[]{(long) 1L}; }",
			"class T { java.lang.Object m() { return new long[]{1L}; } }",
			"class T { long x; java.lang.Object y = x = 1L; }"
	})
	public void testPrimitiveTargetReported(String source) throws Exception {
		final var events = BaseCheckTest.runCheckInline(RedundantNumericSuffixCheck.class, source);
		assertEquals(1, events.size(), events::toString);
		assertEquals(1, events.getFirst().getLine());
		assertEquals(source.indexOf("1L") + 1, events.getFirst().getColumn());
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel());
		assertEquals("Remove the redundant 'L' suffix.", events.getFirst().getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"class T { java.lang.Long x = 1L; }",
			"class T { java.lang.Float x = 1f; }",
			"class T { java.lang.Double x = 1d; }",
			"class T { @interface TypeUse {} java.lang.@TypeUse Long x = 1L; }",
			"class T { java.lang.Long[] x = {1L}; }",
			"class T { java.lang.Long x[] = {1L}; }",
			"class T { java.lang.Long[][] x = {{1L}}; }",
			"class T { java.lang.Object x = 1L; }",
			"class T { java.lang.Number x = 1L; }",
			"class T { java.lang.Object[] x = {1L}; }",
			"class T { java.io.Serializable x = 1L; }",
			"class T { java.lang.Comparable<Long> x = 1L; }",
			"class T { java.lang.Comparable<java.lang.Long> x = 1L; }",
			"class T { boolean flag; java.lang.Object x = flag ? 1L : 2; }",
			"interface T { java.lang.Long X = 1L; }",
			"class T { Object x = (java.lang.Long) 1L; }",
			"class T { java.lang.Long m() { return 1L; } }",
			"class T { java.lang.Object m() { return 1f; } }",
			"class T { java.lang.Long[] m() { return new java.lang.Long[]{1L}; } }",
			"class T { Object[] x = new java.lang.Long[]{1L}; }",
			"class T { java.lang.Object[] x = new java.lang.Object[]{1f}; }",
			"class T { java.lang.Long x; void m() { x = 1L; } }",
			"class T { java.lang.Object x; void m() { x = 1L; } }",
			"class T { void m() { for (Object o : new java.lang.Long[]{1L}) System.out.println(o); } }",
			"class T { void m() { final java.lang.Long x = 1L; } }"
	})
	public void testQualifiedReferenceTargetNotReported(String source) throws Exception {
		assertEquals(List.of(), BaseCheckTest.runCheckInline(RedundantNumericSuffixCheck.class, source));
	}
}