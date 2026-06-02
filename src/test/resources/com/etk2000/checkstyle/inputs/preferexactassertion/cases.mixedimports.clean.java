package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Assertions;

@SuppressWarnings("unused")
class InputPreferExactAssertionMixedImportsClean {
	private static Object anyObject() {
		return "x";
	}

	void noOpToConsumeNonNullImport() {
		assertNotNull(anyObject());
	}

	void qualifiedAssertInstanceofSuppressed() {
		final var o = anyObject();
		org.junit.Assert.assertTrue(o instanceof String);
	}

	void unqualifiedInstanceofSuppressedByJ4Import() {
		final var o = anyObject();
		assertTrue(o instanceof String);
	}

	void unusedAssertFalseToConsumeImport() {
		assertFalse(false);
	}

	void unusedAssertionsToConsumeImport() {
		Assertions.assertTrue(true);
	}
}