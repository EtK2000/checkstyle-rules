package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
class InputPreferExactAssertionJunit4WildcardClean {
	private static Object anyObject() {
		return "x";
	}

	void unqualifiedInstanceofSuppressedUnderWildcardJ4() {
		final var o = anyObject();
		assertTrue(o instanceof String);
	}
}