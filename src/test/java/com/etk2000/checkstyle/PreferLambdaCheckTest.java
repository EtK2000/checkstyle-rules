package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferLambdaCheckTest {
	private static final String DIR = "preferlambda/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferLambdaCheck.class, DIR + "InputPreferLambdaClean.java").isEmpty());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferLambdaCheck.class, DIR + "InputPreferLambdaViolation.java");
		assertEquals(3, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("Runnable"));
		assertEquals(13, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("Supplier"));

		// anonymous class as method argument
		assertEquals(19, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("Runnable"));
	}
}