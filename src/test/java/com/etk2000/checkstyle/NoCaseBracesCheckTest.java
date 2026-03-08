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
	public void testUnnecessaryBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoCaseBracesCheck.class, DIR + "InputCaseBracesViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
	}

	@Test
	public void testMissingRequiredBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoCaseBracesCheck.class, DIR + "InputCaseBracesMissingViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals(12, violations.get(1).getLine());
		for (final var v : violations)
			assertTrue(v.getMessage().contains("variable"));
	}
}