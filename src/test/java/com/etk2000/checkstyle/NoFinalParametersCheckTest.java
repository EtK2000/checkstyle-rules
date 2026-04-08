package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.Test;

public class NoFinalParametersCheckTest {
	private static final String DIR = "nofinalparameters/";
	private static final String MSG_FOR_INIT = "For-loop variable '%s' must not be final, move it before the loop.";
	private static final String MSG_FOREACH = "For-each variable '%s' must not be final.";
	private static final String MSG_PARAM = "Parameter '%s' must not be final.";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoFinalParametersCheck.class, DIR + "InputNoFinalParametersClean.java").isEmpty());
	}

	@Test
	public void testCleanForInit() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoFinalParametersCheck.class, DIR + "InputNoFinalParametersClean.java", "tokens", "FOR_INIT").isEmpty());
	}

	@Test
	public void testForInitViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoFinalParametersCheck.class, DIR + "InputNoFinalParametersViolation.java", "tokens", "FOR_INIT");
		assertEquals(3, violations.size());
		var i = 0;

		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_FOR_INIT, "i"), violations.get(i++).getMessage());
		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_FOR_INIT, "size"), violations.get(i++).getMessage());

		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_FOR_INIT, "size"), violations.get(i++).getMessage());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoFinalParametersCheck.class, DIR + "InputNoFinalParametersViolation.java");
		assertEquals(15, violations.size());
		var i = 0;

		assertEquals(6, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "x"), violations.get(i++).getMessage());

		assertEquals(8, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "s"), violations.get(i++).getMessage());

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "x"), violations.get(i++).getMessage());
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "y"), violations.get(i++).getMessage());

		assertEquals(16, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "e"), violations.get(i++).getMessage());

		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "e"), violations.get(i++).getMessage());

		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "x"), violations.get(i++).getMessage());

		assertEquals(32, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "x"), violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_FOREACH, "item"), violations.get(i++).getMessage());

		assertEquals(40, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_FOREACH, "item"), violations.get(i++).getMessage());

		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "a"), violations.get(i++).getMessage());
		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "b"), violations.get(i++).getMessage());

		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "y"), violations.get(i++).getMessage());

		assertEquals(60, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "x"), violations.get(i++).getMessage());

		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(String.format(MSG_PARAM, "args"), violations.get(i++).getMessage());
	}
}