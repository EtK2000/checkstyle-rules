package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MethodAlphabeticalOrderCheckTest {
	private static final String DIR = "methodorder/";

	@Test
	public void testCleanOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderClean.java").isEmpty());
	}

	@Test
	public void testInstanceMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderViolation.java");
		assertEquals(2, violations.size());
	}

	@Test
	public void testOverloadsSkipped() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderOverloads.java").isEmpty());
	}

	@Test
	public void testStaticMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderStaticViolation.java");
		assertEquals(1, violations.size());
	}
}