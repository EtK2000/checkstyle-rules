package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class RedundantArrayCreationCheckTest {
	private static final String DIR = "redundantarraycreation/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RedundantArrayCreationCheck.class, DIR + "InputRedundantArrayCreationClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(RedundantArrayCreationCheck.class, DIR + "InputRedundantArrayCreationViolation.java");
		assertEquals(5, violations.size());
		var i = 0;

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant array creation for varargs parameter of 'asList'.", violations.get(i++).getMessage());

		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant array creation for varargs parameter of 'addAll'.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant array creation for varargs parameter of 'ProcessBuilder'.", violations.get(i++).getMessage());

		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant array creation for varargs parameter of 'format'.", violations.get(i++).getMessage());

		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Remove redundant array creation for varargs parameter of 'join'.", violations.get(i++).getMessage());
	}
}