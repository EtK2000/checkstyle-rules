package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionJunit5WildcardViolation {
	private static Object anyObject() {
		return "x";
	}

	void unqualifiedComparisonFiresUnderWildcardJ5() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void unqualifiedInstanceofFiresUnderWildcardJ5() {
		final var o = anyObject();
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}