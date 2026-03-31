package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferPatternMatchingInstanceofCheckTest {
	private static final String DIR = "patterninstanceof/";

	@Test
	public void testCastAfterInstanceofViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferPatternMatchingInstanceofCheck.class, DIR + "InputPatternInstanceofViolation.java");
		assertEquals(8, violations.size());

		assertEquals(6, violations.get(0).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(0).getMessage());

		assertEquals(13, violations.get(1).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(1).getMessage());

		assertEquals(18, violations.get(2).getLine());
		assertEquals("Use pattern matching 'instanceof Number name' instead of casting after instanceof check.", violations.get(2).getMessage());

		assertEquals(26, violations.get(3).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(3).getMessage());

		assertEquals(32, violations.get(4).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(4).getMessage());

		assertEquals(39, violations.get(5).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(5).getMessage());

		assertEquals(46, violations.get(6).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(6).getMessage());

		assertEquals(51, violations.get(7).getLine());
		assertEquals("Use pattern matching 'instanceof String name' instead of casting after instanceof check.", violations.get(7).getMessage());
	}

	@Test
	public void testCleanPatternMatching() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferPatternMatchingInstanceofCheck.class, DIR + "InputPatternInstanceofClean.java").isEmpty());
	}
}