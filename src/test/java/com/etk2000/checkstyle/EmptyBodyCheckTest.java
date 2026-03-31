package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EmptyBodyCheckTest {
	private static final String DIR = "emptybody/";

	@Test
	public void testCleanBodies() throws Exception {
		assertTrue(BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyClean.java").isEmpty());
	}

	@Test
	public void testEmptyBodyViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyViolation.java");
		assertEquals(10, violations.size());

		// empty else
		assertEquals(7, violations.get(0).getLine());
		assertEquals("Empty else body, remove it.", violations.get(0).getMessage());
		assertEquals(14, violations.get(1).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(1).getMessage());
		assertEquals(21, violations.get(2).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(2).getMessage());
		assertEquals(27, violations.get(3).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(3).getMessage());
		assertEquals(34, violations.get(4).getLine());
		assertEquals("Empty else body, remove it.", violations.get(4).getMessage());
		assertEquals(40, violations.get(5).getLine());
		assertEquals("Empty else body, remove it.", violations.get(5).getMessage());

		// empty if
		assertEquals(45, violations.get(6).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(6).getMessage());
		assertEquals(50, violations.get(7).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(7).getMessage());
		assertEquals(54, violations.get(8).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(8).getMessage());
		assertEquals(59, violations.get(9).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(9).getMessage());
	}

	@Test
	public void testEmptyLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyLoopViolation.java");
		assertEquals(6, violations.size());

		// empty do-while
		assertEquals(5, violations.get(0).getLine());
		assertEquals("Empty do-while body, remove it (preserve any side effects in the condition).", violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals("Empty do-while body, remove it (preserve any side effects in the condition).", violations.get(1).getMessage());

		// empty for
		assertEquals(15, violations.get(2).getLine());
		assertEquals("Empty for body, remove it (preserve any side effects in the condition/update).", violations.get(2).getMessage());
		assertEquals(20, violations.get(3).getLine());
		assertEquals("Empty for body, remove it (preserve any side effects in the condition/update).", violations.get(3).getMessage());

		// empty while
		assertEquals(24, violations.get(4).getLine());
		assertEquals("Empty while body, remove it (preserve any side effects in the condition).", violations.get(4).getMessage());
		assertEquals(29, violations.get(5).getLine());
		assertEquals("Empty while body, remove it (preserve any side effects in the condition).", violations.get(5).getMessage());
	}
}