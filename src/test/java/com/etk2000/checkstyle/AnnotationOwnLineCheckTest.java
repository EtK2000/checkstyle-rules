package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnnotationOwnLineCheckTest {
	private static final String DIR = "annotationownline/";
	private static final String SAME_DIR = "annotationsameline/";

	@Test
	public void testBlankLineViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLineBlankViolation.java");
		assertEquals(3, violations.size());

		// blank line between annotation and field
		assertEquals(8, violations.get(0).getLine());
		assertEquals("No blank line after annotation 'A'.", violations.get(0).getMessage());

		// blank line between two annotations
		assertEquals(13, violations.get(1).getLine());
		assertEquals("No blank line after annotation 'A'.", violations.get(1).getMessage());

		// blank line between annotation and method
		assertEquals(19, violations.get(2).getLine());
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLineClean.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnSameLineClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "InputAnnotationSameLineClean.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnSameLineOrderViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "InputAnnotationSameLineOrderViolation.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnSameLineViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, SAME_DIR + "InputAnnotationSameLineViolation.java").isEmpty());
	}

	@Test
	public void testOrderViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLineOrderViolation.java");
		assertEquals(4, violations.size());

		// @B before @A
		assertEquals(10, violations.get(0).getLine());
		assertEquals("Annotation 'A' must appear before 'B' (alphabetical order).", violations.get(0).getMessage());

		// @C before @A
		assertEquals(15, violations.get(1).getLine());
		assertEquals("Annotation 'A' must appear before 'C' (alphabetical order).", violations.get(1).getMessage());

		// @C @B @A reverse order: two violations
		assertEquals(21, violations.get(2).getLine());
		assertEquals("Annotation 'B' must appear before 'C' (alphabetical order).", violations.get(2).getMessage());
		assertEquals(22, violations.get(3).getLine());
		assertEquals("Annotation 'A' must appear before 'B' (alphabetical order).", violations.get(3).getMessage());
	}

	@Test
	public void testPackageAnnotationClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLinePackageClean.java").isEmpty());
	}

	@Test
	public void testPackageAnnotationViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLinePackageViolation.java");
		assertEquals(1, violations.size());
		assertEquals(1, violations.getFirst().getLine());
		assertEquals("Annotation 'Deprecated' must be on its own line.", violations.getFirst().getMessage());
	}

	@Test
	public void testSameLineViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLineViolation.java");
		assertEquals(17, violations.size());

		// class: @A @B
		assertEquals(8, violations.get(0).getLine());
		// enum declaration: @A enum
		assertEquals(11, violations.get(1).getLine());
		// enum constant: @A ACTIVE
		assertEquals(15, violations.get(2).getLine());
		// enum constant: @A @B INACTIVE
		assertEquals(17, violations.get(3).getLine());
		// interface: @A interface
		assertEquals(21, violations.get(4).getLine());
		// annotation type: @A @interface
		assertEquals(24, violations.get(5).getLine());
		// annotation field: @A String value()
		assertEquals(25, violations.get(6).getLine());
		// record: @A @B record
		assertEquals(29, violations.get(7).getLine());
		// compact ctor: @A InlineCompact
		assertEquals(33, violations.get(8).getLine());
		// field: @A int
		assertEquals(37, violations.get(9).getLine());
		// field: @A @B
		assertEquals(40, violations.get(10).getLine());
		// field: @A @B @C int (deduplication)
		assertEquals(44, violations.get(11).getLine());
		// ctor: @A Ctor
		assertEquals(47, violations.get(12).getLine());
		// method: @A void
		assertEquals(50, violations.get(13).getLine());
		// local: @A final
		assertEquals(54, violations.get(14).getLine());
		// local: @A @B final
		assertEquals(56, violations.get(15).getLine());
		// method: @A @B
		assertEquals(60, violations.get(16).getLine());

		for (final var violation : violations)
			assertEquals("Annotation 'A' must be on its own line.", violation.getMessage());
	}
}