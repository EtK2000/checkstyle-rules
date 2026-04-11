package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
		assertEquals("Empty switch statement, remove it (preserve any side effects in the expression).", violations.get(0).getMessage());
		assertEquals("Empty switch statement, remove it (preserve any side effects in the expression).", violations.get(1).getMessage());
	}
}