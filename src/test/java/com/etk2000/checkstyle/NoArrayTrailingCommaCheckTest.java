package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NoArrayTrailingCommaCheckTest {
	private static final String DIR = "arraycomma/";

	@Test
	public void testCleanNoTrailingComma() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoArrayTrailingCommaCheck.class, DIR + "InputArrayCommaClean.java").isEmpty());
	}

	@Test
	public void testTrailingCommaViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoArrayTrailingCommaCheck.class, DIR + "InputArrayCommaViolation.java");
		assertEquals(3, violations.size());
		assertEquals(4, violations.get(0).getLine());
		assertEquals("No trailing comma in array initializer.", violations.get(0).getMessage());
		assertEquals(5, violations.get(1).getLine());
		assertEquals("No trailing comma in array initializer.", violations.get(1).getMessage());
		assertEquals(7, violations.get(2).getLine());
		assertEquals("No trailing comma in array initializer.", violations.get(2).getMessage());
	}
}