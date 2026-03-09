package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferEnhancedSwitchCheckTest {
	private static final String DIR = "enhancedswitch/";

	@Test
	public void testCleanNotConvertible() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferEnhancedSwitchCheck.class, DIR + "InputEnhancedSwitchClean.java").isEmpty());
	}

	@Test
	public void testConvertibleSwitches() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferEnhancedSwitchCheck.class, DIR + "InputEnhancedSwitchViolation.java");
		assertEquals(6, violations.size());
	}
}