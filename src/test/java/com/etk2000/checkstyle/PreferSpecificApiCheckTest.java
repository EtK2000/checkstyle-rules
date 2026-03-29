package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferSpecificApiCheckTest {
	private static final String DIR = "specificapi/";

	@Test
	public void testCleanApiUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiClean.java").isEmpty());
	}

	@Test
	public void testGetFirstAndGetLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("getLast"));
		assertEquals(11, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("getFirst"));
	}
}