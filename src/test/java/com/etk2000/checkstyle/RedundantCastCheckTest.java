package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RedundantCastCheckTest {
	private static final String DIR = "redundantcast/";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RedundantCastCheck.class, DIR + "InputRedundantCastClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(RedundantCastCheck.class, DIR + "InputRedundantCastViolation.java");
		assertEquals(55, violations.size());

		assertEquals(6, violations.get(0).getLine());
		assertEquals("Redundant cast to 'char', expression is already 'char'.", violations.get(0).getMessage());
		assertEquals(7, violations.get(1).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'double'.", violations.get(1).getMessage());
		assertEquals(8, violations.get(2).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'float'.", violations.get(2).getMessage());
		assertEquals(9, violations.get(3).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(3).getMessage());
		assertEquals(10, violations.get(4).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'long'.", violations.get(4).getMessage());
		assertEquals(11, violations.get(5).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(5).getMessage());

		for (var i = 6; i < 17; ++i) {
			assertEquals(17 + (i - 6), violations.get(i).getLine());
			assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(i).getMessage());
		}

		assertEquals(32, violations.get(17).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(17).getMessage());

		assertEquals(36, violations.get(18).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(18).getMessage());

		assertEquals(40, violations.get(19).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(19).getMessage());

		assertEquals(44, violations.get(20).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(20).getMessage());

		assertEquals(48, violations.get(21).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(21).getMessage());

		assertEquals(53, violations.get(22).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(22).getMessage());

		assertEquals(58, violations.get(23).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(23).getMessage());

		assertEquals(62, violations.get(24).getLine());
		assertEquals("Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.", violations.get(24).getMessage());

		assertEquals(66, violations.get(25).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(25).getMessage());

		assertEquals(70, violations.get(26).getLine());
		assertEquals("Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.", violations.get(26).getMessage());

		assertEquals(75, violations.get(27).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(27).getMessage());

		assertEquals(80, violations.get(28).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(28).getMessage());

		assertEquals(84, violations.get(29).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(29).getMessage());

		assertEquals(89, violations.get(30).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'byte'.", violations.get(30).getMessage());

		assertEquals(94, violations.get(31).getLine());
		assertEquals("Redundant cast to 'short', expression is already 'byte'.", violations.get(31).getMessage());

		assertEquals(99, violations.get(32).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'char'.", violations.get(32).getMessage());

		assertEquals(104, violations.get(33).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'float'.", violations.get(33).getMessage());

		assertEquals(109, violations.get(34).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(34).getMessage());

		assertEquals(114, violations.get(35).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(35).getMessage());

		assertEquals(120, violations.get(36).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(36).getMessage());

		assertEquals(125, violations.get(37).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(37).getMessage());

		assertEquals(130, violations.get(38).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(38).getMessage());

		assertEquals(135, violations.get(39).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(39).getMessage());

		assertEquals(142, violations.get(40).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(40).getMessage());

		assertEquals(148, violations.get(41).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(41).getMessage());

		assertEquals(153, violations.get(42).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'int'.", violations.get(42).getMessage());

		assertEquals(158, violations.get(43).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'int'.", violations.get(43).getMessage());

		assertEquals(163, violations.get(44).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(44).getMessage());

		assertEquals(168, violations.get(45).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'long'.", violations.get(45).getMessage());

		assertEquals(173, violations.get(46).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'long'.", violations.get(46).getMessage());

		assertEquals(177, violations.get(47).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(47).getMessage());

		assertEquals(182, violations.get(48).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'short'.", violations.get(48).getMessage());

		assertEquals(187, violations.get(49).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'short'.", violations.get(49).getMessage());

		assertEquals(192, violations.get(50).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'int'.", violations.get(50).getMessage());

		assertEquals(197, violations.get(51).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'int'.", violations.get(51).getMessage());

		assertEquals(202, violations.get(52).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(52).getMessage());

		assertEquals(208, violations.get(53).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(53).getMessage());

		assertEquals(214, violations.get(54).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(54).getMessage());
	}
}