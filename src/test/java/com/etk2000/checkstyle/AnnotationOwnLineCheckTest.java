package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class AnnotationOwnLineCheckTest {
	private static final String DIR = "annotationownline/";
	private static final String SAME_DIR = "annotationsameline/";

	@Test
	public void testBlankLineViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationOwnLineCheck.class, DIR + "InputAnnotationOwnLineBlankViolation.java");
		assertEquals(6, violations.size());

		assertEquals(10, violations.get(0).getLine());
		assertEquals("No blank line after annotation 'A'.", violations.get(0).getMessage());

		assertEquals(14, violations.get(1).getLine());
		assertEquals("No blank line after annotation 'A'.", violations.get(1).getMessage());

		assertEquals(19, violations.get(2).getLine());
		assertEquals("No blank line after annotation 'A'.", violations.get(2).getMessage());

		assertEquals(25, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("No blank line after annotation 'V'.", violations.get(3).getMessage());

		assertEquals(29, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("No blank line after annotation 'A'.", violations.get(4).getMessage());

		assertEquals(37, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("No blank line inside annotation 'V'.", violations.get(5).getMessage());
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

		assertEquals(9, violations.get(0).getLine());
		assertEquals("Annotation 'A' must appear before 'B' (alphabetical order).", violations.get(0).getMessage());

		assertEquals(13, violations.get(1).getLine());
		assertEquals("Annotation 'A' must appear before 'C' (alphabetical order).", violations.get(1).getMessage());

		assertEquals(18, violations.get(2).getLine());
		assertEquals("Annotation 'B' must appear before 'C' (alphabetical order).", violations.get(2).getMessage());
		assertEquals(19, violations.get(3).getLine());
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

		assertEquals(7, violations.get(0).getLine());
		assertEquals(9, violations.get(1).getLine());
		assertEquals(12, violations.get(2).getLine());
		assertEquals(14, violations.get(3).getLine());
		assertEquals(17, violations.get(4).getLine());
		assertEquals(19, violations.get(5).getLine());
		assertEquals(20, violations.get(6).getLine());
		assertEquals(23, violations.get(7).getLine());
		assertEquals(26, violations.get(8).getLine());
		assertEquals(29, violations.get(9).getLine());
		assertEquals(31, violations.get(10).getLine());
		assertEquals(34, violations.get(11).getLine());
		assertEquals(36, violations.get(12).getLine());
		assertEquals(38, violations.get(13).getLine());
		assertEquals(41, violations.get(14).getLine());
		assertEquals(43, violations.get(15).getLine());
		assertEquals(46, violations.get(16).getLine());

		for (var violation : violations)
			assertEquals("Annotation 'A' must be on its own line.", violation.getMessage());
	}
}