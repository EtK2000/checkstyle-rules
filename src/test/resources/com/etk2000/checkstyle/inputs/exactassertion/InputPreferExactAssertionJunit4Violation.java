package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Assertions;

@SuppressWarnings("unused")
class InputPreferExactAssertionJunit4Violation {
	private static Object anyObject() {
		return "x";
	}

	void assertFalseEqual() {
		assertFalse(1 == 2); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '=='.
	}

	void assertFalseGt() {
		assertFalse(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>'.
	}

	void assertTrueEqual() {
		assertTrue(1 == 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '=='.
	}

	void assertTrueGt() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void assertTrueLt() {
		assertTrue(0 < 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '<'.
	}

	void assertTrueNotEqual() {
		assertTrue(1 != 2); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '!='.
	}

	void junit4MessageFirstAssertFalseComparisonFires() {
		assertFalse("should be negative", 1 >= 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>='.
	}

	void junit4MessageFirstComparisonFires() {
		assertTrue("should be positive", 1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void qualifiedJunit5AssertionsAssertFalseInstanceofStillFires() {
		final var o = anyObject();
		org.junit.jupiter.api.Assertions.assertFalse(o instanceof Integer); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void qualifiedJunit5AssertionsInstanceofStillFiresUnderJ4Imports() {
		final var o = anyObject();
		org.junit.jupiter.api.Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void qualifiedJunit5AssertionsSimpleNameInstanceofStillFires() {
		final var o = anyObject();
		Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}