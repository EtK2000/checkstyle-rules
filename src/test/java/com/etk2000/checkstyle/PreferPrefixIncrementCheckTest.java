package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferPrefixIncrementCheckTest {
	private static final String DIR = "prefix/";

	@Test
	public void testCleanPrefixUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixClean.java").isEmpty());
	}

	@Test
	public void testPostfixViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferPrefixIncrementCheck.class, DIR + "InputPrefixViolation.java");
		assertEquals(8, violations.size());
		var i = 0;

		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());

		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());

		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());

		assertEquals(29, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix decrement (--x) instead of postfix (x--).", violations.get(i++).getMessage());

		assertEquals(39, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());

		assertEquals(46, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix increment (++x) instead of postfix (x++).", violations.get(i++).getMessage());
		assertEquals(47, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use prefix decrement (--x) instead of postfix (x--).", violations.get(i++).getMessage());
	}
}