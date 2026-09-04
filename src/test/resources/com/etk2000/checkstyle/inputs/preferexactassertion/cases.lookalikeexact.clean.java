package com.etk2000.checkstyle.inputs.preferexactassertion;

import static com.example.Assertions.assertFalse;
import static com.example.Assertions.assertTrue;

@SuppressWarnings("unused")
class InputPreferExactAssertionLookalikeExactClean {
	private static Object anyObject() {
		return "x";
	}

	void lookalikeComparisonClean() {
		final var a = 1;
		final var b = 2;
		assertTrue(a == b);
		assertFalse(a != b);
	}

	void lookalikeInstanceofClean() {
		final var o = anyObject();
		assertTrue(o instanceof String);
		assertFalse(o instanceof Integer);
	}

	void lookalikeNegationClean() {
		final var flag = true;
		assertTrue(!flag);
		assertFalse(!flag);
	}
}