package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;

@SuppressWarnings("unused")
class InputPreferExactAssertionNonStaticJ4WithJ5StaticViolation {
	private static Object anyObject() {
		return "x";
	}

	void unqualifiedInstanceofFiresIgnoringNonStaticJ4Import() {
		final var o = anyObject();
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
		Assert.class.getSimpleName();
	}
}