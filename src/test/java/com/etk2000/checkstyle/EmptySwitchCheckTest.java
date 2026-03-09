package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmptySwitchCheckTest {
	private static final String DIR = "emptyswitch/";

	@Test
	public void testCleanSwitchesWithCases() throws Exception {
		assertTrue(BaseCheckTest.runCheck(EmptySwitchCheck.class, DIR + "InputEmptySwitchClean.java").isEmpty());
	}

	@Test
	public void testEmptySwitchViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptySwitchCheck.class, DIR + "InputEmptySwitchViolation.java");
		assertEquals(2, violations.size());
	}
}