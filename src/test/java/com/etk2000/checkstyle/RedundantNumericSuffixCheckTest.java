package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RedundantNumericSuffixCheckTest {
	private static final String DIR = "redundantsuffix/";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RedundantNumericSuffixCheck.class, DIR + "InputRedundantSuffixClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(RedundantNumericSuffixCheck.class, DIR + "InputRedundantSuffixViolation.java");
		assertEquals(40, violations.size());

		// field declarations
		assertEquals(5, violations.get(0).getLine());
		assertEquals(6, violations.get(1).getLine());
		assertEquals(7, violations.get(2).getLine());
		assertEquals(8, violations.get(3).getLine());
		assertEquals(9, violations.get(4).getLine());
		assertEquals(10, violations.get(5).getLine());
		assertEquals(11, violations.get(6).getLine());
		assertEquals(12, violations.get(7).getLine());

		// hex, octal, binary
		assertEquals(15, violations.get(8).getLine());
		assertEquals(16, violations.get(9).getLine());
		assertEquals(17, violations.get(10).getLine());
		assertEquals(18, violations.get(11).getLine());

		// static and final fields
		assertEquals(21, violations.get(12).getLine());
		assertEquals(22, violations.get(13).getLine());

		// negative values
		assertEquals(25, violations.get(14).getLine());
		assertEquals(26, violations.get(15).getLine());

		// array initializers
		assertEquals(29, violations.get(16).getLine());
		assertEquals(29, violations.get(17).getLine());
		assertEquals(30, violations.get(18).getLine());
		assertEquals(31, violations.get(19).getLine());
		assertEquals(31, violations.get(20).getLine());

		// int boundary (INT_MAX fits in int)
		assertEquals(34, violations.get(21).getLine());

		// mixed array (only 0f flagged, not 1.5f)
		assertEquals(37, violations.get(22).getLine());

		// d suffix on decimal is always redundant
		assertEquals(40, violations.get(23).getLine());
		assertEquals(41, violations.get(24).getLine());

		// cast expression
		assertEquals(45, violations.get(25).getLine());

		// compound assignment
		assertEquals(51, violations.get(26).getLine());

		// local variable declarations
		assertEquals(56, violations.get(27).getLine());
		assertEquals(57, violations.get(28).getLine());
		assertEquals(58, violations.get(29).getLine());
		assertEquals(59, violations.get(30).getLine());

		// methodArgDecimalD
		assertEquals(63, violations.get(31).getLine());

		// new array expression
		assertEquals(68, violations.get(32).getLine());

		// reassignment
		assertEquals(74, violations.get(33).getLine());

		// return statements
		assertEquals(79, violations.get(34).getLine());
		assertEquals(83, violations.get(35).getLine());
		assertEquals(87, violations.get(36).getLine());

		// ternary in typed variable
		assertEquals(94, violations.get(37).getLine());
		assertEquals(94, violations.get(38).getLine());

		// varDecimalD
		assertEquals(98, violations.get(39).getLine());
	}
}