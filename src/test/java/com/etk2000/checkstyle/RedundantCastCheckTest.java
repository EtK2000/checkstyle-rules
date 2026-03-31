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

		// same-type casts on literals
		assertEquals(7, violations.get(0).getLine());
		assertEquals("Redundant cast to 'char', expression is already 'char'.", violations.get(0).getMessage());
		assertEquals(8, violations.get(1).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'double'.", violations.get(1).getMessage());
		assertEquals(9, violations.get(2).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'float'.", violations.get(2).getMessage());
		assertEquals(10, violations.get(3).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(3).getMessage());
		assertEquals(11, violations.get(4).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'long'.", violations.get(4).getMessage());
		assertEquals(12, violations.get(5).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(5).getMessage());

		// widening casts in compound assignments (&=, |=, >>>=, ^=, /=, -=, %=, +=, <<=, >>=, *=)
		for (var i = 6; i < 17; ++i) {
			assertEquals(18 + (i - 6), violations.get(i).getLine());
			assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(i).getMessage());
		}

		// null cast in assignment (different target)
		assertEquals(33, violations.get(17).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(17).getMessage());

		// null cast in assignment (matching target)
		assertEquals(38, violations.get(18).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(18).getMessage());

		// null cast in return (matching target)
		assertEquals(43, violations.get(19).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(19).getMessage());

		// null cast in return (different target)
		assertEquals(48, violations.get(20).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'null'.", violations.get(20).getMessage());

		// same-type cast on field
		assertEquals(53, violations.get(21).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(21).getMessage());

		// same-type cast on for-each variable
		assertEquals(59, violations.get(22).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(22).getMessage());

		// same-type cast on for-init variable
		assertEquals(65, violations.get(23).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(23).getMessage());

		// same-type cast on new
		assertEquals(70, violations.get(24).getLine());
		assertEquals("Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.", violations.get(24).getMessage());

		// same-type cast on parameter (reference type)
		assertEquals(75, violations.get(25).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(25).getMessage());

		// same-type cast on this
		assertEquals(80, violations.get(26).getLine());
		assertEquals("Redundant cast to 'InputRedundantCastViolation', expression is already 'InputRedundantCastViolation'.", violations.get(26).getMessage());

		// same-type cast on variable (primitive)
		assertEquals(86, violations.get(27).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'int'.", violations.get(27).getMessage());

		// same-type cast on variable (reference type)
		assertEquals(92, violations.get(28).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(28).getMessage());

		// same-type cast on nested cast
		assertEquals(97, violations.get(29).getLine());
		assertEquals("Redundant cast to 'String', expression is already 'String'.", violations.get(29).getMessage());

		// widening byte to int
		assertEquals(103, violations.get(30).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'byte'.", violations.get(30).getMessage());

		// widening byte to short
		assertEquals(109, violations.get(31).getLine());
		assertEquals("Redundant cast to 'short', expression is already 'byte'.", violations.get(31).getMessage());

		// widening char to int
		assertEquals(115, violations.get(32).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'char'.", violations.get(32).getMessage());

		// widening float to double
		assertEquals(121, violations.get(33).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'float'.", violations.get(33).getMessage());

		// widening in return
		assertEquals(127, violations.get(34).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(34).getMessage());

		// widening in return (method returns wider primitive)
		assertEquals(133, violations.get(35).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(35).getMessage());

		// widening in standalone reassignment
		assertEquals(140, violations.get(36).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(36).getMessage());

		// widening in ternary false branch (sibling is long)
		assertEquals(146, violations.get(37).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(37).getMessage());

		// widening in ternary return (sibling is long)
		assertEquals(152, violations.get(38).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(38).getMessage());

		// widening in ternary true branch (sibling is long)
		assertEquals(158, violations.get(39).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(39).getMessage());

		// widening in ternary with long reassignment target
		assertEquals(166, violations.get(40).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(40).getMessage());

		// widening in ternary with long assignment target
		assertEquals(173, violations.get(41).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(41).getMessage());

		// widening int to double
		assertEquals(179, violations.get(42).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'int'.", violations.get(42).getMessage());

		// widening int to float
		assertEquals(185, violations.get(43).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'int'.", violations.get(43).getMessage());

		// widening int to long
		assertEquals(191, violations.get(44).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(44).getMessage());

		// widening long to double
		assertEquals(197, violations.get(45).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'long'.", violations.get(45).getMessage());

		// widening long to float
		assertEquals(203, violations.get(46).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'long'.", violations.get(46).getMessage());

		// widening from parameter
		assertEquals(208, violations.get(47).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(47).getMessage());

		// widening short to int
		assertEquals(214, violations.get(48).getLine());
		assertEquals("Redundant cast to 'int', expression is already 'short'.", violations.get(48).getMessage());

		// widening short to long
		assertEquals(220, violations.get(49).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'short'.", violations.get(49).getMessage());

		// sibling already double (1.5 literal)
		assertEquals(226, violations.get(50).getLine());
		assertEquals("Redundant cast to 'double', expression is already 'int'.", violations.get(50).getMessage());

		// sibling already float (1.5f literal)
		assertEquals(232, violations.get(51).getLine());
		assertEquals("Redundant cast to 'float', expression is already 'int'.", violations.get(51).getMessage());

		// sibling already long (100L literal)
		assertEquals(238, violations.get(52).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(52).getMessage());

		// sibling already long (variable)
		assertEquals(245, violations.get(53).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(53).getMessage());

		// widening in comparison (sibling is wider)
		assertEquals(252, violations.get(54).getLine());
		assertEquals("Redundant cast to 'long', expression is already 'int'.", violations.get(54).getMessage());
	}
}