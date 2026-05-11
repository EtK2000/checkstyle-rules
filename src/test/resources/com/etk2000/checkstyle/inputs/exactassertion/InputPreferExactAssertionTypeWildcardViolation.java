package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionTypeWildcardViolation {
	private static Object anyObject() {
		return "x";
	}

	void comparisonFiresUnderJ5StaticImport() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void qualifiedAssertComparisonStillFires() {
		Assert.assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void typeWildcardJ4DoesNotCountUnqualifiedFiresViaJ5Static() {
		final var o = anyObject();
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}