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
		assertEquals(10, violations.size());
	}

	@Test
	public void testEmptyLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyLoopViolation.java");
		assertEquals(6, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(10, violations.get(1).getLine());
		assertEquals(15, violations.get(2).getLine());
		assertEquals(20, violations.get(3).getLine());
		assertEquals(24, violations.get(4).getLine());
		assertEquals(29, violations.get(5).getLine());
	}
}