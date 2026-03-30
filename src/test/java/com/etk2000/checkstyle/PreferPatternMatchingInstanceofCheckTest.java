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
		assertEquals(13, violations.get(1).getLine());
		assertEquals(18, violations.get(2).getLine());
		assertEquals(26, violations.get(3).getLine());
		assertEquals(32, violations.get(4).getLine());
		assertEquals(39, violations.get(5).getLine());
		assertEquals(46, violations.get(6).getLine());
		assertEquals(51, violations.get(7).getLine());
	}

	@Test
	public void testCleanPatternMatching() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferPatternMatchingInstanceofCheck.class, DIR + "InputPatternInstanceofClean.java").isEmpty());
	}
}