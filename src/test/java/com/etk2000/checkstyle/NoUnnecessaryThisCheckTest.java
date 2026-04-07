package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

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
		assertEquals(2, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Unnecessary 'this.field', only use when shadowing or in field assignment.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Unnecessary 'this.field', only use when shadowing or in field assignment.", violations.get(1).getMessage());
	}
}