package com.etk2000.checkstyle.inputs.preferexactassertion;

import static com.example.Assertions.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
class InputPreferExactAssertionLookalikeJunit4Clean {
	private static Object anyObject() {
		return "x";
	}

	void instanceofUnderJunit4NotReported() {
		final var o = anyObject();
		assertTrue(o instanceof String);
	}

	void plainBooleanClean() {
		final var flag = true;
		assertTrue(flag);
		assertEquals(1, 2);
	}
}