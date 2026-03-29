package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferVarCheckTest {
	private static final String DIR = "prefervar/";

	@Test
	public void testCleanVarUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarClean.java").isEmpty());
	}

	@Test
	public void testExplicitTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarViolation.java");
		assertEquals(6, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals(12, violations.get(1).getLine());
		assertEquals(17, violations.get(2).getLine());
		assertEquals(18, violations.get(3).getLine());
		assertEquals(19, violations.get(4).getLine());
		assertEquals(23, violations.get(5).getLine());
	}
}