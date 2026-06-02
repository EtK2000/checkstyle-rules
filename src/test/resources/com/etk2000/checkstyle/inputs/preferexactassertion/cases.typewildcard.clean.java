package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionTypeWildcardClean {
	private static Object anyObject() {
		return "x";
	}

	void qualifiedAssertViaTypeWildcardStillSuppressed() {
		final var o = anyObject();
		Assert.assertTrue(o instanceof String);
	}

	void unusedAssertTrueToConsumeImport() {
		assertTrue(true);
	}
}