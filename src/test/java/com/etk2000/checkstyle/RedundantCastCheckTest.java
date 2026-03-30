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
		assertEquals(38, violations.size());

		// same-type casts on literals
		assertEquals(7, violations.get(0).getLine());
		assertEquals(8, violations.get(1).getLine());
		assertEquals(9, violations.get(2).getLine());
		assertEquals(10, violations.get(3).getLine());
		assertEquals(11, violations.get(4).getLine());
		assertEquals(12, violations.get(5).getLine());

		// null cast in assignment (different target)
		assertEquals(16, violations.get(6).getLine());

		// null cast in assignment (matching target)
		assertEquals(21, violations.get(7).getLine());

		// null cast in return (matching target)
		assertEquals(26, violations.get(8).getLine());

		// null cast in return (different target)
		assertEquals(31, violations.get(9).getLine());

		// same-type cast on field
		assertEquals(36, violations.get(10).getLine());

		// same-type cast on for-each variable
		assertEquals(42, violations.get(11).getLine());

		// same-type cast on for-init variable
		assertEquals(48, violations.get(12).getLine());

		// same-type cast on new
		assertEquals(53, violations.get(13).getLine());

		// same-type cast on parameter (reference type)
		assertEquals(58, violations.get(14).getLine());

		// same-type cast on this
		assertEquals(63, violations.get(15).getLine());

		// same-type cast on variable (primitive)
		assertEquals(69, violations.get(16).getLine());

		// same-type cast on variable (reference type)
		assertEquals(75, violations.get(17).getLine());

		// same-type cast on nested cast
		assertEquals(80, violations.get(18).getLine());

		// widening byte to int
		assertEquals(86, violations.get(19).getLine());

		// widening byte to short
		assertEquals(92, violations.get(20).getLine());

		// widening char to int
		assertEquals(98, violations.get(21).getLine());

		// widening float to double
		assertEquals(104, violations.get(22).getLine());

		// widening in return
		assertEquals(110, violations.get(23).getLine());

		// widening in return (method returns wider primitive)
		assertEquals(116, violations.get(24).getLine());

		// widening int to double
		assertEquals(122, violations.get(25).getLine());

		// widening int to float
		assertEquals(128, violations.get(26).getLine());

		// widening int to long
		assertEquals(134, violations.get(27).getLine());

		// widening long to double
		assertEquals(140, violations.get(28).getLine());

		// widening long to float
		assertEquals(146, violations.get(29).getLine());

		// widening from parameter
		assertEquals(151, violations.get(30).getLine());

		// widening short to int
		assertEquals(157, violations.get(31).getLine());

		// widening short to long
		assertEquals(163, violations.get(32).getLine());

		// sibling already double (1.5 literal)
		assertEquals(169, violations.get(33).getLine());

		// sibling already float (1.5f literal)
		assertEquals(175, violations.get(34).getLine());

		// sibling already long (100L literal)
		assertEquals(181, violations.get(35).getLine());

		// sibling already long (variable)
		assertEquals(188, violations.get(36).getLine());

		// widening in comparison (sibling is wider)
		assertEquals(195, violations.get(37).getLine());
	}
}