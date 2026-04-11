package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class RedundantAnnotationSyntaxCheckTest {
	private static final String DIR = "annotationsyntax/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RedundantAnnotationSyntaxCheck.class, DIR + "InputRedundantAnnotationSyntaxClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(RedundantAnnotationSyntaxCheck.class, DIR + "InputRedundantAnnotationSyntaxViolation.java");
		assertEquals(11, violations.size());
		var i = 0;

		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove empty parentheses from annotation '@A'.", violations.get(i++).getMessage());

		assertEquals(16, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove empty parentheses from annotation '@B'.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@D'.", violations.get(i++).getMessage());

		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@A'.", violations.get(i++).getMessage());

		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@A'.", violations.get(i++).getMessage());

		assertEquals(28, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@C'.", violations.get(i++).getMessage());

		assertEquals(31, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove empty parentheses from annotation '@B'.", violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@A'.", violations.get(i++).getMessage());

		assertEquals(40, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove empty parentheses from annotation '@B'.", violations.get(i++).getMessage());

		assertEquals(42, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant 'value =' from annotation '@A'.", violations.get(i++).getMessage());

		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove empty parentheses from annotation '@B'.", violations.get(i++).getMessage());
	}
}