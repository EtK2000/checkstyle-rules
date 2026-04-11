package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PreferLiteralSuffixCheckTest {
	private static final String DIR = "literalsuffix/";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferLiteralSuffixCheck.class, DIR + "InputLiteralSuffixClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferLiteralSuffixCheck.class, DIR + "InputLiteralSuffixViolation.java");
		assertEquals(32, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals("Use 'd' suffix on '100' instead of a cast.", violations.get(0).getMessage());

		assertEquals(13, violations.get(1).getLine());
		assertEquals("Use 'f' suffix on '100' instead of a cast.", violations.get(1).getMessage());

		assertEquals(19, violations.get(2).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(2).getMessage());

		assertEquals(25, violations.get(3).getLine());
		assertEquals("Use 'L' suffix on '255' instead of a cast.", violations.get(3).getMessage());

		assertEquals(31, violations.get(4).getLine());
		assertEquals("Use 'L' suffix on '255' instead of a cast.", violations.get(4).getMessage());

		assertEquals(37, violations.get(5).getLine());
		assertEquals("Use 'L' suffix on '255' instead of a cast.", violations.get(5).getMessage());

		assertEquals(43, violations.get(6).getLine());
		assertEquals("Use 'L' suffix on '10' instead of a cast.", violations.get(6).getMessage());

		assertEquals(49, violations.get(7).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(7).getMessage());

		assertEquals(55, violations.get(8).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(8).getMessage());

		assertEquals(61, violations.get(9).getLine());
		assertEquals("Use 'L' suffix on '7' instead of a cast.", violations.get(9).getMessage());

		assertEquals(67, violations.get(10).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(10).getMessage());

		assertEquals(73, violations.get(11).getLine());
		assertEquals("Use 'L' suffix on '32' instead of a cast.", violations.get(11).getMessage());

		assertEquals(79, violations.get(12).getLine());
		assertEquals("Use 'L' suffix on '1' instead of a cast.", violations.get(12).getMessage());

		assertEquals(85, violations.get(13).getLine());
		assertEquals("Use 'L' suffix on '50' instead of a cast.", violations.get(13).getMessage());

		assertEquals(91, violations.get(14).getLine());
		assertEquals("Use 'L' suffix on '1' instead of a cast.", violations.get(14).getMessage());

		assertEquals(97, violations.get(15).getLine());
		assertEquals("Use 'L' suffix on '0xFF' instead of a cast.", violations.get(15).getMessage());

		assertEquals(103, violations.get(16).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(16).getMessage());

		assertEquals(109, violations.get(17).getLine());
		assertEquals("Use 'L' suffix on '255' instead of a cast.", violations.get(17).getMessage());

		assertEquals(115, violations.get(18).getLine());
		assertEquals("Use 'L' suffix on '100' instead of a cast.", violations.get(18).getMessage());

		assertEquals(121, violations.get(19).getLine());
		assertEquals("Use 'd' suffix on '100' instead of a cast.", violations.get(19).getMessage());

		assertEquals(127, violations.get(20).getLine());
		assertEquals("Use 'f' suffix on '100' instead of a cast.", violations.get(20).getMessage());

		assertEquals(133, violations.get(21).getLine());
		assertEquals("Use 'L' suffix on '1' instead of a cast.", violations.get(21).getMessage());

		assertEquals(139, violations.get(22).getLine());
		assertEquals("Use 'L' suffix on '-100' instead of a cast.", violations.get(22).getMessage());

		assertEquals(145, violations.get(23).getLine());
		assertEquals("Use 'L' suffix on '-100' instead of a cast.", violations.get(23).getMessage());

		assertEquals(151, violations.get(24).getLine());
		assertEquals("Use 'L' suffix on '+100' instead of a cast.", violations.get(24).getMessage());

		assertEquals(157, violations.get(25).getLine());
		assertEquals("Use 'd' suffix on '0' instead of a cast.", violations.get(25).getMessage());

		assertEquals(163, violations.get(26).getLine());
		assertEquals("Use 'f' suffix on '0' instead of a cast.", violations.get(26).getMessage());

		assertEquals(169, violations.get(27).getLine());
		assertEquals("Use 'L' suffix on '0' instead of a cast.", violations.get(27).getMessage());

		assertEquals(175, violations.get(28).getLine());
		assertEquals("Use 'd' suffix on '0' instead of a cast.", violations.get(28).getMessage());

		assertEquals(181, violations.get(29).getLine());
		assertEquals("Use 'f' suffix on '0' instead of a cast.", violations.get(29).getMessage());

		assertEquals(187, violations.get(30).getLine());
		assertEquals("Use 'L' suffix on '0' instead of a cast.", violations.get(30).getMessage());

		assertEquals(193, violations.get(31).getLine());
		assertEquals("Use 'L' suffix on '-1' instead of a cast.", violations.get(31).getMessage());
	}
}