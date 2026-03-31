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
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(1).getMessage());
		assertEquals(15, violations.get(2).getLine());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(2).getMessage());
		assertEquals(19, violations.get(3).getLine());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(3).getMessage());
		assertEquals(24, violations.get(4).getLine());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(4).getMessage());
		assertEquals(28, violations.get(5).getLine());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(5).getMessage());
	}

	@Test
	public void testNonInfiniteLoopsIgnored() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyLoopViolation.java").isEmpty());
	}
}