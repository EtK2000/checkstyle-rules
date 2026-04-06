package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnnotationSameLineCheckTest {
	private static final String DIR = "annotationsameline/";
	private static final String MSG_ORDER = "Annotation 'A' must appear before 'B' (alphabetical order).";
	private static final String MSG_PLACEMENT = "Annotation 'A' must be on the same line as the declaration.";
	private static final String OWN_DIR = "annotationownline/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, DIR + "InputAnnotationSameLineClean.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLineBlankViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLineBlankViolation.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLineClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLineClean.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLineOrderViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLineOrderViolation.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLinePackageClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLinePackageClean.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLinePackageViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLinePackageViolation.java").isEmpty());
	}

	@Test
	public void testDoesNotFireOnOwnLineViolation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationSameLineCheck.class, OWN_DIR + "InputAnnotationOwnLineViolation.java").isEmpty());
	}

	@Test
	public void testOrderViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationSameLineCheck.class, DIR + "InputAnnotationSameLineOrderViolation.java");
		assertEquals(7, violations.size());

		// record component: @B @A
		assertEquals(12, violations.get(0).getLine());
		assertEquals(MSG_ORDER, violations.get(0).getMessage());

		// constructor param: @B @A
		assertEquals(15, violations.get(1).getLine());
		assertEquals(MSG_ORDER, violations.get(1).getMessage());

		// catch param: @B @A
		assertEquals(22, violations.get(2).getLine());
		assertEquals(MSG_ORDER, violations.get(2).getMessage());

		// for-each: @B @A
		assertEquals(29, violations.get(3).getLine());
		assertEquals(MSG_ORDER, violations.get(3).getMessage());

		// lambda param: @B @A
		assertEquals(35, violations.get(4).getLine());
		assertEquals(MSG_ORDER, violations.get(4).getMessage());

		// method param: @B @A
		assertEquals(39, violations.get(5).getLine());
		assertEquals(MSG_ORDER, violations.get(5).getMessage());

		// multi-param: @C @A
		assertEquals(43, violations.get(6).getLine());
		assertEquals("Annotation 'A' must appear before 'C' (alphabetical order).", violations.get(6).getMessage());
	}

	@Test
	public void testPlacementViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationSameLineCheck.class, DIR + "InputAnnotationSameLineViolation.java");
		assertEquals(8, violations.size());

		assertEquals(12, violations.get(0).getLine());
		assertEquals(18, violations.get(1).getLine());
		assertEquals(28, violations.get(2).getLine());
		assertEquals(38, violations.get(3).getLine());
		assertEquals(47, violations.get(4).getLine());
		assertEquals(57, violations.get(5).getLine());
		assertEquals(64, violations.get(6).getLine());
		assertEquals(70, violations.get(7).getLine());

		for (final var violation : violations)
			assertEquals(MSG_PLACEMENT, violation.getMessage());
	}
}