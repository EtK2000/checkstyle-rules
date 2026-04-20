package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferLambdaCheckTest {
	private static final String DIR = "preferlambda/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferLambdaCheck.class, DIR + "InputPreferLambdaClean.java").isEmpty());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferLambdaCheck.class, DIR + "InputPreferLambdaViolation.java");
		assertEquals(7, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Runnable'.", violations.get(0).getMessage());

		assertEquals(16, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Supplier'.", violations.get(1).getMessage());

		assertEquals(25, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Runnable'.", violations.get(2).getMessage());

		assertEquals(31, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Supplier'.", violations.get(3).getMessage());

		assertEquals(37, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Runnable'.", violations.get(4).getMessage());

		assertEquals(46, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'java.lang.Runnable'.", violations.get(5).getMessage());

		assertEquals(55, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Use a lambda expression instead of anonymous 'Runnable'.", violations.get(6).getMessage());
	}
}