package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InfiniteEmptyLoopCheckTest {
	private static final String DIR = "emptybody/";

	@Test
	public void testCleanBodies() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyBodyClean.java").isEmpty());
	}

	@Test
	public void testInfiniteLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyInfiniteLoopViolation.java");
		assertEquals(6, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(10, violations.get(1).getLine());
		assertEquals(15, violations.get(2).getLine());
		assertEquals(19, violations.get(3).getLine());
		assertEquals(24, violations.get(4).getLine());
		assertEquals(28, violations.get(5).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("infinite"));
	}

	@Test
	public void testNonInfiniteLoopsIgnored() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyLoopViolation.java").isEmpty());
	}
}