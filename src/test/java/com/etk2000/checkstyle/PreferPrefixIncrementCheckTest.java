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
		assertEquals(5, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(0).getMessage());
		assertEquals(15, violations.get(1).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(1).getMessage());
		assertEquals(19, violations.get(2).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(2).getMessage());
		assertEquals(26, violations.get(3).getLine());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(3).getMessage());

		// postfix decrement
		assertEquals(27, violations.get(4).getLine());
		assertEquals("Use prefix decrement (--x) instead of postfix (x--).", violations.get(4).getMessage());
	}
}