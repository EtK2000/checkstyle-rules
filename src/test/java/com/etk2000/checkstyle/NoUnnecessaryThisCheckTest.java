package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoUnnecessaryThisCheckTest {
	private static final String DIR = "unnecessarythis/";

	@Test
	public void testCleanAssignmentAndShadowing() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, DIR + "InputThisClean.java").isEmpty());
	}

	@Test
	public void testInstanceInitViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, DIR + "InputThisInstanceInitViolation.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Unnecessary 'this.field', only use when shadowing or in field assignment.", violations.getFirst().getMessage());
	}

	@Test
	public void testLambdaViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, DIR + "InputThisLambdaViolation.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Unnecessary 'this.field', only use when shadowing or in field assignment.", violations.getFirst().getMessage());
	}

	@Test
	public void testMethodCallViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, DIR + "InputThisMethodCall.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Unnecessary 'this.doSomething', only use when shadowing or in field assignment.", violations.getFirst().getMessage());
	}

	@Test
	public void testUnnecessaryFieldAccess() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, DIR + "InputThisViolation.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Unnecessary 'this.field', only use when shadowing or in field assignment.", violations.getFirst().getMessage());
	}
}