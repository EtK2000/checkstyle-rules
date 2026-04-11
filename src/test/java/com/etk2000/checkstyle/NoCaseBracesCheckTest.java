package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
		final var msg = "Unnecessary braces in case block, only use braces when a variable is defined in the case's scope.";
		assertEquals(2, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals(msg, violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals(msg, violations.get(1).getMessage());
	}
}