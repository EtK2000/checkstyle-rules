package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

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
		assertEquals(8, violations.size());

		assertEquals(11, violations.get(0).getLine());
		assertEquals(MSG_ORDER, violations.get(0).getMessage());

		assertEquals(13, violations.get(1).getLine());
		assertEquals(MSG_ORDER, violations.get(1).getMessage());

		assertEquals(19, violations.get(2).getLine());
		assertEquals(MSG_ORDER, violations.get(2).getMessage());

		assertEquals(25, violations.get(3).getLine());
		assertEquals(MSG_ORDER, violations.get(3).getMessage());

		assertEquals(30, violations.get(4).getLine());
		assertEquals(MSG_ORDER, violations.get(4).getMessage());

		assertEquals(35, violations.get(5).getLine());
		assertEquals(MSG_ORDER, violations.get(5).getMessage());

		assertEquals(38, violations.get(6).getLine());
		assertEquals(MSG_ORDER, violations.get(6).getMessage());

		assertEquals(41, violations.get(7).getLine());
		assertEquals("Annotation 'A' must appear before 'C' (alphabetical order).", violations.get(7).getMessage());
	}

	@Test
	public void testPlacementViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationSameLineCheck.class, DIR + "InputAnnotationSameLineViolation.java");
		assertEquals(10, violations.size());

		assertEquals(11, violations.get(0).getLine());
		assertEquals(16, violations.get(1).getLine());
		assertEquals(25, violations.get(2).getLine());
		assertEquals(34, violations.get(3).getLine());
		assertEquals(42, violations.get(4).getLine());
		assertEquals(51, violations.get(5).getLine());
		assertEquals(59, violations.get(6).getLine());
		assertEquals(65, violations.get(7).getLine());
		assertEquals(70, violations.get(8).getLine());

		for (var i = 0; i < 9; ++i)
			assertEquals(MSG_PLACEMENT, violations.get(i).getMessage());

		// placement wrong AND order wrong: only placement reported (early return)
		assertEquals(76, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Annotation 'B' must be on the same line as the declaration.", violations.get(9).getMessage());
	}
}