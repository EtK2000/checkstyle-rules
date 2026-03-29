package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ConstructorAssignmentOrderCheckTest {
	private static final String DIR = "constructorassign/";

	@Test
	public void testCleanAssignmentOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignViolation.java");
		assertEquals(2, violations.size());
		assertEquals(8, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("alpha"));
		assertEquals(23, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("alpha"));
	}
}