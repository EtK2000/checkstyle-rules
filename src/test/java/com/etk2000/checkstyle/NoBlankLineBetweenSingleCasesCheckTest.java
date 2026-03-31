package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoBlankLineBetweenSingleCasesCheckTest {
	private static final String DIR = "singlecase/";

	@Test
	public void testBlankLineAfterBracedCaseClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputBracedCaseBlankLineClean.java").isEmpty());
	}

	@Test
	public void testBlankLineAfterBracedCaseViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputBracedCaseBlankLineViolation.java");
		assertEquals(2, violations.size());
		assertEquals(11, violations.get(0).getLine());
		assertEquals("No blank line after braced case, the closing brace provides separation.", violations.get(0).getMessage());
		assertEquals(16, violations.get(1).getLine());
		assertEquals("No blank line after braced case, the closing brace provides separation.", violations.get(1).getMessage());
	}

	@Test
	public void testBlankLineBetweenSingleCases() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertEquals("No blank line between single-line switch cases.", violations.getFirst().getMessage());
	}

	@Test
	public void testBlankLineBetweenThrowCases() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseThrowViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertEquals("No blank line between single-line switch cases.", violations.getFirst().getMessage());
	}

	@Test
	public void testBlankLineBetweenYieldCases() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseYieldViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertEquals("No blank line between single-line switch cases.", violations.getFirst().getMessage());
	}

	@Test
	public void testCleanNoBlankLines() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoBlankLineBetweenSingleCasesCheck.class, DIR + "InputSingleCaseClean.java").isEmpty());
	}
}