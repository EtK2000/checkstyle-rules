package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;

@SuppressWarnings("unused")
class InputPreferExactAssertionJunit5Clean {
	private static class TestHelper {
		void assertTrue(boolean cond) {
		}
	}

	private static Object anyObject() {
		return "x";
	}

	void chainedReceiverInstanceOfSuppressed() {
		final var o = anyObject();
		someHelper().assertTrue(o instanceof String);
	}

	void junit5SimpleNameAssertSuppressed() {
		final var o = anyObject();
		Assert.assertTrue(o instanceof String);
	}

	private TestHelper someHelper() {
		return new TestHelper();
	}

	void unusedAssertTrueToConsumeImport() {
		assertTrue(true);
	}
}