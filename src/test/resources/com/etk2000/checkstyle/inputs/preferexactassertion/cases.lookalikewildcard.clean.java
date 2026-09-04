package com.etk2000.checkstyle.inputs.preferexactassertion;

import static com.foo.Assert.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionLookalikeWildcardClean {
	private static Object anyObject() {
		return "x";
	}

	void lookalikeWildcardComparisonClean() {
		final var a = 1;
		final var b = 2;
		assertTrue(a == b);
		assertFalse(a != b);
	}

	void lookalikeWildcardInstanceofClean() {
		final var o = anyObject();
		assertTrue(o instanceof String);
		assertFalse(o instanceof Integer);
	}

	void lookalikeWildcardNegationClean() {
		final var flag = true;
		assertTrue(!flag);
		assertFalse(!flag);
	}
}