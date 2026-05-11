package com.etk2000.checkstyle.inputs.exactassertion;

@SuppressWarnings("unused")
class InputPreferExactAssertionNoStaticImportClean extends InputPreferExactAssertionNoStaticImportClean.AssertBase {
	private static class AssertBase {
		static void assertFalse(boolean cond) {
		}

		static void assertTrue(boolean cond) {
		}
	}

	private static Object anyObject() {
		return "x";
	}

	void inheritedAssertInstanceofClean() {
		final var o = anyObject();
		assertTrue(o instanceof String);
		assertFalse(o instanceof Integer);
	}
}