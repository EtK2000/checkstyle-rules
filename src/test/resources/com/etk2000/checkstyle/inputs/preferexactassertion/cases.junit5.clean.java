package com.etk2000.checkstyle.inputs.preferexactassertion;

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

	void patternBindingSuppressed() {
		final var o = anyObject();
		assertTrue(o instanceof String s);
		assertTrue(o instanceof String s2, "msg");
	}

	private TestHelper someHelper() {
		return new TestHelper();
	}

	void unusedAssertTrueToConsumeImport() {
		assertTrue(true);
	}
}