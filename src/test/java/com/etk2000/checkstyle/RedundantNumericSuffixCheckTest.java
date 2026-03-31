package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RedundantNumericSuffixCheckTest {
	private static final String DIR = "redundantsuffix/";
	private static final String MSG_D = "Redundant 'D' suffix, remove it.";
	private static final String MSG_d = "Redundant 'd' suffix, remove it.";
	private static final String MSG_F = "Redundant 'F' suffix, remove it.";
	private static final String MSG_f = "Redundant 'f' suffix, remove it.";
	private static final String MSG_L = "Redundant 'L' suffix, remove it.";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RedundantNumericSuffixCheck.class, DIR + "InputRedundantSuffixClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(RedundantNumericSuffixCheck.class, DIR + "InputRedundantSuffixViolation.java");
		assertEquals(40, violations.size());

		// field declarations
		assertEquals(7, violations.get(0).getLine());
		assertEquals(MSG_L, violations.get(0).getMessage());
		assertEquals(8, violations.get(1).getLine());
		assertEquals(MSG_L, violations.get(1).getMessage());
		assertEquals(9, violations.get(2).getLine());
		assertEquals(MSG_L, violations.get(2).getMessage());
		assertEquals(10, violations.get(3).getLine());
		assertEquals(MSG_f, violations.get(3).getMessage());
		assertEquals(11, violations.get(4).getLine());
		assertEquals(MSG_F, violations.get(4).getMessage());
		assertEquals(12, violations.get(5).getLine());
		assertEquals(MSG_d, violations.get(5).getMessage());
		assertEquals(13, violations.get(6).getLine());
		assertEquals(MSG_d, violations.get(6).getMessage());
		assertEquals(14, violations.get(7).getLine());
		assertEquals(MSG_D, violations.get(7).getMessage());

		// hex, octal, binary
		assertEquals(17, violations.get(8).getLine());
		assertEquals(MSG_L, violations.get(8).getMessage());
		assertEquals(18, violations.get(9).getLine());
		assertEquals(MSG_L, violations.get(9).getMessage());
		assertEquals(19, violations.get(10).getLine());
		assertEquals(MSG_L, violations.get(10).getMessage());
		assertEquals(20, violations.get(11).getLine());
		assertEquals(MSG_d, violations.get(11).getMessage());

		// static and final fields
		assertEquals(23, violations.get(12).getLine());
		assertEquals(MSG_L, violations.get(12).getMessage());
		assertEquals(24, violations.get(13).getLine());
		assertEquals(MSG_L, violations.get(13).getMessage());

		// negative values
		assertEquals(27, violations.get(14).getLine());
		assertEquals(MSG_f, violations.get(14).getMessage());
		assertEquals(28, violations.get(15).getLine());
		assertEquals(MSG_d, violations.get(15).getMessage());

		// array initializers
		assertEquals(31, violations.get(16).getLine());
		assertEquals(MSG_L, violations.get(16).getMessage());
		assertEquals(31, violations.get(17).getLine());
		assertEquals(MSG_L, violations.get(17).getMessage());
		assertEquals(32, violations.get(18).getLine());
		assertEquals(MSG_f, violations.get(18).getMessage());
		assertEquals(33, violations.get(19).getLine());
		assertEquals(MSG_d, violations.get(19).getMessage());
		assertEquals(33, violations.get(20).getLine());
		assertEquals(MSG_D, violations.get(20).getMessage());

		// int boundary (INT_MAX fits in int)
		assertEquals(36, violations.get(21).getLine());
		assertEquals(MSG_L, violations.get(21).getMessage());

		// mixed array (only 0f flagged, not 1.5f)
		assertEquals(39, violations.get(22).getLine());
		assertEquals(MSG_f, violations.get(22).getMessage());

		// d suffix on decimal is always redundant
		assertEquals(42, violations.get(23).getLine());
		assertEquals(MSG_d, violations.get(23).getMessage());
		assertEquals(43, violations.get(24).getLine());
		assertEquals(MSG_d, violations.get(24).getMessage());

		// cast expression
		assertEquals(47, violations.get(25).getLine());
		assertEquals(MSG_L, violations.get(25).getMessage());

		// compound assignment
		assertEquals(53, violations.get(26).getLine());
		assertEquals(MSG_L, violations.get(26).getMessage());

		// local variable declarations
		assertEquals(58, violations.get(27).getLine());
		assertEquals(MSG_L, violations.get(27).getMessage());
		assertEquals(59, violations.get(28).getLine());
		assertEquals(MSG_L, violations.get(28).getMessage());
		assertEquals(60, violations.get(29).getLine());
		assertEquals(MSG_f, violations.get(29).getMessage());
		assertEquals(61, violations.get(30).getLine());
		assertEquals(MSG_d, violations.get(30).getMessage());

		// methodArgDecimalD
		assertEquals(65, violations.get(31).getLine());
		assertEquals(MSG_d, violations.get(31).getMessage());

		// new array expression
		assertEquals(70, violations.get(32).getLine());
		assertEquals(MSG_L, violations.get(32).getMessage());

		// reassignment
		assertEquals(76, violations.get(33).getLine());
		assertEquals(MSG_L, violations.get(33).getMessage());

		// return statements
		assertEquals(81, violations.get(34).getLine());
		assertEquals(MSG_d, violations.get(34).getMessage());
		assertEquals(85, violations.get(35).getLine());
		assertEquals(MSG_f, violations.get(35).getMessage());
		assertEquals(89, violations.get(36).getLine());
		assertEquals(MSG_L, violations.get(36).getMessage());

		// ternary in typed variable
		assertEquals(96, violations.get(37).getLine());
		assertEquals(MSG_L, violations.get(37).getMessage());
		assertEquals(96, violations.get(38).getLine());
		assertEquals(MSG_L, violations.get(38).getMessage());

		// varDecimalD
		assertEquals(100, violations.get(39).getLine());
		assertEquals(MSG_d, violations.get(39).getMessage());
	}
}