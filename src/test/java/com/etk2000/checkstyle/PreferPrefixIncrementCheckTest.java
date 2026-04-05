package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferPrefixIncrementCheckTest {
	private static final String DIR = "prefix/";

	@Test
	public void testCleanPrefixUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixClean.java").isEmpty());
	}

	@Test
	public void testPostfixViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixViolation.java");
		assertEquals(6, violations.size());

		// braceless else
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(0).getMessage());

		// braceless if
		assertEquals(15, violations.get(1).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(1).getMessage());

		// braceless while (decrement)
		assertEquals(21, violations.get(2).getLine());
		assertEquals("Use prefix decrement (--x) instead of postfix (x--).", violations.get(2).getMessage());

		// for-loop update
		assertEquals(25, violations.get(3).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(3).getMessage());

		// standalone statement
		assertEquals(32, violations.get(4).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(4).getMessage());
		assertEquals(33, violations.get(5).getLine());
		assertEquals("Use prefix decrement (--x) instead of postfix (x--).", violations.get(5).getMessage());
	}
}