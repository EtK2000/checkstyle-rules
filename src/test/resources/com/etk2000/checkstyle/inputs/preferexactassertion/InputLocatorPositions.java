package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Positions for {@code PreferExactAssertionLocatorTest}, which addresses them directly rather than
 * through a reported violation. One assertion shape per method so a test names a method instead of a
 * line, and nothing here is a slice: the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	private static class Settings {
		Object currentValue() {
			return null;
		}

		Object currentValueOf() {
			return null;
		}

		Settings getNestedSettings() {
			return this;
		}

		Settings resolveDefaultValueProvider() {
			return this;
		}
	}

	void comparisonCall(int a, int b) {
		assertTrue(a == b);
	}

	void instanceofCall(Object o) {
		assertTrue(o instanceof String);
	}

	void lambdaBodyCall(Object o) {
		runLater(() -> assertTrue(o instanceof String));
	}

	void lengthRefused(Settings configuration) {
		assertTrue(configuration.getNestedSettings().resolveDefaultValueProvider().currentValueOf() instanceof String);
	}

	void lengthRefusedNegatedInstanceof(Settings configuration) {
		assertTrue(!(configuration.getNestedSettings().resolveDefaultValueProvider().currentValue() instanceof String));
	}

	void negationCall(boolean flag) {
		assertTrue(!flag);
	}

	void oneLineBodyCall(Object o) { assertTrue(o instanceof String); }

	void qualifiedCall(Object o) {
		org.junit.jupiter.api.Assertions.assertTrue(o instanceof String);
	}

	private void runLater(Runnable task) {
	}

	void splitNameAndParen(Object o) {
		assertTrue
		(o instanceof String);
	}

	void supplementaryBeforeCall(Object o) {
		/* 𝕏 */ assertTrue(o instanceof String);
	}

	void tabIndentedCall(Object o, boolean other) {
		while (other)
			assertTrue(o instanceof String);
	}
}