package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Assertions;

@SuppressWarnings("unused")
class InputPreferExactAssertionMixedImportsViolation {
	private static Object anyObject() {
		return "x";
	}

	void comparisonAssertFalseStillFires() {
		assertFalse(1 == 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '=='.
	}

	void comparisonStillFires() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void qualifiedAssertionsFqnInstanceofFires() {
		final var o = anyObject();
		org.junit.jupiter.api.Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void qualifiedAssertionsSimpleNameAssertFalseInstanceofFires() {
		final var o = anyObject();
		Assertions.assertFalse(o instanceof Integer); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void qualifiedAssertionsSimpleNameInstanceofFires() {
		final var o = anyObject();
		Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}