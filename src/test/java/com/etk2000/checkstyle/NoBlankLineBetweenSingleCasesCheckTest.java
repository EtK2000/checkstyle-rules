package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoBlankLineBetweenSingleCasesCheckTest {
	private static final String DIR = "singlecase/";

	@Test
	public void testBlankLineBetweenSingleCases() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
	}

	@Test
	public void testBlankLineBetweenThrowCases() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseThrowViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
	}

	@Test
	public void testCleanNoBlankLines() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseClean.java").isEmpty());
	}
}