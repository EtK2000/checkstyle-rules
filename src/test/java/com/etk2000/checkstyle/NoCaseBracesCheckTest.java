package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoCaseBracesCheckTest {
	private static final String DIR = "casebraces/";

	@Test
	public void testCleanNoBracesAndJustifiedBraces() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoCaseBracesCheck.class, DIR + "InputCaseBracesClean.java").isEmpty());
	}

	@Test
	public void testMissingRequiredBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoCaseBracesCheck.class, DIR + "InputCaseBracesMissingViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals("Case block defines a variable, add braces to limit scope.", violations.get(0).getMessage());
		assertEquals(12, violations.get(1).getLine());
		assertEquals("Case block defines a variable, add braces to limit scope.", violations.get(1).getMessage());
	}

	@Test
	public void testUnnecessaryBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoCaseBracesCheck.class, DIR + "InputCaseBracesViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("Unnecessary braces in case block, only use braces when a variable is defined in the case's scope.", violations.getFirst().getMessage());
	}
}