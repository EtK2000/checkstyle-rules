package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InstanceofBeforeCastCheckTest {
	private static final String DIR = "instanceofbeforecast/";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InstanceofBeforeCastCheck.class, DIR + "InputInstanceofBeforeCastClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(InstanceofBeforeCastCheck.class, DIR + "InputInstanceofBeforeCastViolation.java");
		assertEquals(3, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals(12, violations.get(1).getLine());
		assertEquals(18, violations.get(2).getLine());
	}
}