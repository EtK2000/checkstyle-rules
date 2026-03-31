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
		assertEquals(8, violations.get(1).getLine());
		assertEquals(9, violations.get(2).getLine());
		assertEquals(10, violations.get(3).getLine());
		assertEquals(11, violations.get(4).getLine());
		assertEquals(12, violations.get(5).getLine());

		// widening casts in compound assignments (&=, |=, >>>=, ^=, /=, -=, %=, +=, <<=, >>=, *=)
		for (var i = 6; i < 17; ++i)
			assertEquals(18 + (i - 6), violations.get(i).getLine());

		// null cast in assignment (different target)
		assertEquals(33, violations.get(17).getLine());

		// null cast in assignment (matching target)
		assertEquals(38, violations.get(18).getLine());

		// null cast in return (matching target)
		assertEquals(43, violations.get(19).getLine());

		// null cast in return (different target)
		assertEquals(48, violations.get(20).getLine());

		// same-type cast on field
		assertEquals(53, violations.get(21).getLine());

		// same-type cast on for-each variable
		assertEquals(59, violations.get(22).getLine());

		// same-type cast on for-init variable
		assertEquals(65, violations.get(23).getLine());

		// same-type cast on new
		assertEquals(70, violations.get(24).getLine());

		// same-type cast on parameter (reference type)
		assertEquals(75, violations.get(25).getLine());

		// same-type cast on this
		assertEquals(80, violations.get(26).getLine());

		// same-type cast on variable (primitive)
		assertEquals(86, violations.get(27).getLine());

		// same-type cast on variable (reference type)
		assertEquals(92, violations.get(28).getLine());

		// same-type cast on nested cast
		assertEquals(97, violations.get(29).getLine());

		// widening byte to int
		assertEquals(103, violations.get(30).getLine());

		// widening byte to short
		assertEquals(109, violations.get(31).getLine());

		// widening char to int
		assertEquals(115, violations.get(32).getLine());

		// widening float to double
		assertEquals(121, violations.get(33).getLine());

		// widening in return
		assertEquals(127, violations.get(34).getLine());

		// widening in return (method returns wider primitive)
		assertEquals(133, violations.get(35).getLine());

		// widening in standalone reassignment
		assertEquals(140, violations.get(36).getLine());

		// widening in ternary false branch (sibling is long)
		assertEquals(146, violations.get(37).getLine());

		// widening in ternary return (sibling is long)
		assertEquals(152, violations.get(38).getLine());

		// widening in ternary true branch (sibling is long)
		assertEquals(158, violations.get(39).getLine());

		// widening in ternary with long reassignment target
		assertEquals(166, violations.get(40).getLine());

		// widening in ternary with long assignment target
		assertEquals(173, violations.get(41).getLine());

		// widening int to double
		assertEquals(179, violations.get(42).getLine());

		// widening int to float
		assertEquals(185, violations.get(43).getLine());

		// widening int to long
		assertEquals(191, violations.get(44).getLine());

		// widening long to double
		assertEquals(197, violations.get(45).getLine());

		// widening long to float
		assertEquals(203, violations.get(46).getLine());

		// widening from parameter
		assertEquals(208, violations.get(47).getLine());

		// widening short to int
		assertEquals(214, violations.get(48).getLine());

		// widening short to long
		assertEquals(220, violations.get(49).getLine());

		// sibling already double (1.5 literal)
		assertEquals(226, violations.get(50).getLine());

		// sibling already float (1.5f literal)
		assertEquals(232, violations.get(51).getLine());

		// sibling already long (100L literal)
		assertEquals(238, violations.get(52).getLine());

		// sibling already long (variable)
		assertEquals(245, violations.get(53).getLine());

		// widening in comparison (sibling is wider)
		assertEquals(252, violations.get(54).getLine());
	}
}