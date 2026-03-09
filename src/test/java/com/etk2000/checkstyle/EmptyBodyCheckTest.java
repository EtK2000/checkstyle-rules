package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmptyBodyCheckTest {
	private static final String DIR = "emptybody/";

	@Test
	public void testCleanBodies() throws Exception {
		assertTrue(BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyClean.java").isEmpty());
	}

	@Test
	public void testEmptyBodyViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyViolation.java");
		assertEquals(5, violations.size());
	}
}