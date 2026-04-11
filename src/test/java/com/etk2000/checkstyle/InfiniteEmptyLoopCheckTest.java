package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class InfiniteEmptyLoopCheckTest {
	private static final String DIR = "emptybody/";

	@Test
	public void testCleanBodies() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyBodyClean.java").isEmpty());
	}

	@Test
	public void testInfiniteLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyInfiniteLoopViolation.java");
		assertEquals(8, violations.size());
		var i = 0;

		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(24, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(28, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(33, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(i++).getMessage());

		assertEquals(37, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(i++).getMessage());
	}

	@Test
	public void testNonInfiniteLoopsIgnored() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyLoopViolation.java").isEmpty());
	}
}