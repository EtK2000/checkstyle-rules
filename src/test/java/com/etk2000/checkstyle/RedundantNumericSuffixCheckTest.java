package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

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

		assertEquals(6, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(MSG_L, violations.get(0).getMessage());
		assertEquals(7, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(MSG_L, violations.get(1).getMessage());
		assertEquals(8, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals(MSG_L, violations.get(2).getMessage());
		assertEquals(9, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals(MSG_f, violations.get(3).getMessage());
		assertEquals(10, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals(MSG_F, violations.get(4).getMessage());
		assertEquals(11, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals(MSG_d, violations.get(5).getMessage());
		assertEquals(12, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals(MSG_d, violations.get(6).getMessage());
		assertEquals(13, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals(MSG_D, violations.get(7).getMessage());

		assertEquals(15, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals(MSG_L, violations.get(8).getMessage());
		assertEquals(16, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals(MSG_L, violations.get(9).getMessage());
		assertEquals(17, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals(MSG_L, violations.get(10).getMessage());
		assertEquals(18, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals(MSG_d, violations.get(11).getMessage());

		assertEquals(20, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals(MSG_L, violations.get(12).getMessage());
		assertEquals(21, violations.get(13).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(13).getSeverityLevel());
		assertEquals(MSG_L, violations.get(13).getMessage());

		assertEquals(23, violations.get(14).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(14).getSeverityLevel());
		assertEquals(MSG_f, violations.get(14).getMessage());
		assertEquals(24, violations.get(15).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(15).getSeverityLevel());
		assertEquals(MSG_d, violations.get(15).getMessage());

		assertEquals(26, violations.get(16).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(16).getSeverityLevel());
		assertEquals(MSG_L, violations.get(16).getMessage());
		assertEquals(26, violations.get(17).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(17).getSeverityLevel());
		assertEquals(MSG_L, violations.get(17).getMessage());
		assertEquals(27, violations.get(18).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(18).getSeverityLevel());
		assertEquals(MSG_f, violations.get(18).getMessage());
		assertEquals(28, violations.get(19).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(19).getSeverityLevel());
		assertEquals(MSG_d, violations.get(19).getMessage());
		assertEquals(28, violations.get(20).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(20).getSeverityLevel());
		assertEquals(MSG_D, violations.get(20).getMessage());

		// int boundary (INT_MAX fits in int)
		assertEquals(31, violations.get(21).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(21).getSeverityLevel());
		assertEquals(MSG_L, violations.get(21).getMessage());

		// mixed array (only 0f flagged, not 1.5f)
		assertEquals(34, violations.get(22).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(22).getSeverityLevel());
		assertEquals(MSG_f, violations.get(22).getMessage());

		// d suffix on decimal is always redundant
		assertEquals(37, violations.get(23).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(23).getSeverityLevel());
		assertEquals(MSG_d, violations.get(23).getMessage());
		assertEquals(38, violations.get(24).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(24).getSeverityLevel());
		assertEquals(MSG_d, violations.get(24).getMessage());

		assertEquals(41, violations.get(25).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(25).getSeverityLevel());
		assertEquals(MSG_L, violations.get(25).getMessage());

		assertEquals(46, violations.get(26).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(26).getSeverityLevel());
		assertEquals(MSG_L, violations.get(26).getMessage());

		assertEquals(50, violations.get(27).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(27).getSeverityLevel());
		assertEquals(MSG_L, violations.get(27).getMessage());
		assertEquals(51, violations.get(28).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(28).getSeverityLevel());
		assertEquals(MSG_L, violations.get(28).getMessage());
		assertEquals(52, violations.get(29).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(29).getSeverityLevel());
		assertEquals(MSG_f, violations.get(29).getMessage());
		assertEquals(53, violations.get(30).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(30).getSeverityLevel());
		assertEquals(MSG_d, violations.get(30).getMessage());

		assertEquals(57, violations.get(31).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(31).getSeverityLevel());
		assertEquals(MSG_d, violations.get(31).getMessage());

		assertEquals(61, violations.get(32).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(32).getSeverityLevel());
		assertEquals(MSG_L, violations.get(32).getMessage());

		assertEquals(66, violations.get(33).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(33).getSeverityLevel());
		assertEquals(MSG_L, violations.get(33).getMessage());

		assertEquals(70, violations.get(34).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(34).getSeverityLevel());
		assertEquals(MSG_d, violations.get(34).getMessage());
		assertEquals(74, violations.get(35).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(35).getSeverityLevel());
		assertEquals(MSG_f, violations.get(35).getMessage());
		assertEquals(78, violations.get(36).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(36).getSeverityLevel());
		assertEquals(MSG_L, violations.get(36).getMessage());

		assertEquals(84, violations.get(37).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(37).getSeverityLevel());
		assertEquals(MSG_L, violations.get(37).getMessage());
		assertEquals(84, violations.get(38).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(38).getSeverityLevel());
		assertEquals(MSG_L, violations.get(38).getMessage());

		assertEquals(88, violations.get(39).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(39).getSeverityLevel());
		assertEquals(MSG_d, violations.get(39).getMessage());
	}
}