package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

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
		assertEquals(8, violations.size());
		var i = 0;

		// do; while(true);
		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(i++).getMessage());

		// do {} while(true);
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite do-while loop, this will hang.", violations.get(i++).getMessage());

		// for(;;);
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		// for(;;) {}
		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		// for(;true;);
		assertEquals(24, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		// for(;true;) {}
		assertEquals(28, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite for loop, this will hang.", violations.get(i++).getMessage());

		// while(true);
		assertEquals(33, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(i++).getMessage());

		// while(true) {}
		assertEquals(37, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty infinite while loop, this will hang.", violations.get(i++).getMessage());
	}

	@Test
	public void testNonInfiniteLoopsIgnored() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InfiniteEmptyLoopCheck.class, DIR + "InputEmptyLoopViolation.java").isEmpty());
	}
}