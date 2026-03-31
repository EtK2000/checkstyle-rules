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
	public void testDependencyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignDependencyViolation.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Assignment 'this.beta' references 'this.alpha' which should be assigned before it.", violations.getFirst().getMessage());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignViolation.java");
		assertEquals(2, violations.size());
		assertEquals(8, violations.get(0).getLine());
		assertEquals("Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).", violations.get(0).getMessage());
		assertEquals(23, violations.get(1).getLine());
		assertEquals("Simple assignment 'this.alpha' must appear before multi-line assignments.", violations.get(1).getMessage());
	}
}