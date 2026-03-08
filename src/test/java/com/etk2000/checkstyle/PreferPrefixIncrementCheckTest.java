package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferPrefixIncrementCheckTest {
	private static final String DIR = "prefix/";

	@Test
	public void testCleanPrefixUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixClean.java").isEmpty());
	}

	@Test
	public void testPostfixViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixViolation.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals(7, violations.get(1).getLine());
	}
}