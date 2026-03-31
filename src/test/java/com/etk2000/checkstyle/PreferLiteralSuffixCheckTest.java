package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

		// cast on left, literal on right — various operators and types
		assertEquals(7, violations.get(0).getLine());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(19, violations.get(2).getLine());
		assertEquals(25, violations.get(3).getLine());
		assertEquals(31, violations.get(4).getLine());
		assertEquals(37, violations.get(5).getLine());
		assertEquals(43, violations.get(6).getLine());
		assertEquals(49, violations.get(7).getLine());
		assertEquals(55, violations.get(8).getLine());
		assertEquals(61, violations.get(9).getLine());
		assertEquals(67, violations.get(10).getLine());
		assertEquals(73, violations.get(11).getLine());
		assertEquals(79, violations.get(12).getLine());
		assertEquals(85, violations.get(13).getLine());
		assertEquals(91, violations.get(14).getLine());
		assertEquals(97, violations.get(15).getLine());

		// literal on left, cast on right — arithmetic, bitwise, comparison, shift, double, float
		assertEquals(103, violations.get(16).getLine());
		assertEquals(109, violations.get(17).getLine());
		assertEquals(115, violations.get(18).getLine());
		assertEquals(121, violations.get(19).getLine());
		assertEquals(127, violations.get(20).getLine());
		assertEquals(133, violations.get(21).getLine());

		// negative/positive unary literals (left, right, positive)
		assertEquals(139, violations.get(22).getLine());
		assertEquals(145, violations.get(23).getLine());
		assertEquals(151, violations.get(24).getLine());
		assertTrue(violations.get(22).getMessage().contains("-100"));
		assertTrue(violations.get(23).getMessage().contains("-100"));
		assertTrue(violations.get(24).getMessage().contains("+100"));

		// ternary — long/double/float, both branches
		assertEquals(157, violations.get(25).getLine());
		assertEquals(163, violations.get(26).getLine());
		assertEquals(169, violations.get(27).getLine());
		assertEquals(175, violations.get(28).getLine());
		assertEquals(181, violations.get(29).getLine());
		assertEquals(187, violations.get(30).getLine());

		// ternary with negative literal
		assertEquals(193, violations.get(31).getLine());
		assertTrue(violations.get(31).getMessage().contains("-1"));
	}
}