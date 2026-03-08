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
		assertEquals(2, violations.size());
		assertEquals(4, violations.getFirst().getLine());
		assertEquals(5, violations.get(1).getLine());
	}
}