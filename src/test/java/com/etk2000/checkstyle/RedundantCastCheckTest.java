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
		assertEquals(49, violations.size());

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

		// widening int to double
		assertEquals(139, violations.get(36).getLine());

		// widening int to float
		assertEquals(145, violations.get(37).getLine());

		// widening int to long
		assertEquals(151, violations.get(38).getLine());

		// widening long to double
		assertEquals(157, violations.get(39).getLine());

		// widening long to float
		assertEquals(163, violations.get(40).getLine());

		// widening from parameter
		assertEquals(168, violations.get(41).getLine());

		// widening short to int
		assertEquals(174, violations.get(42).getLine());

		// widening short to long
		assertEquals(180, violations.get(43).getLine());

		// sibling already double (1.5 literal)
		assertEquals(186, violations.get(44).getLine());

		// sibling already float (1.5f literal)
		assertEquals(192, violations.get(45).getLine());

		// sibling already long (100L literal)
		assertEquals(198, violations.get(46).getLine());

		// sibling already long (variable)
		assertEquals(205, violations.get(47).getLine());

		// widening in comparison (sibling is wider)
		assertEquals(212, violations.get(48).getLine());
	}
}