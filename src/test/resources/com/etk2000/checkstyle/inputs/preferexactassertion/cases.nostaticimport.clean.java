package com.etk2000.checkstyle.inputs.preferexactassertion;

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

	void inheritedAssertComparisonClean() {
		final var a = 1;
		final var b = 2;
		assertTrue(a == b);
		assertFalse(a != b);
		assertTrue(a < b);
		assertFalse(a >= b);
	}

	void inheritedAssertInstanceofClean() {
		final var o = anyObject();
		assertTrue(o instanceof String);
		assertFalse(o instanceof Integer);
	}

	void inheritedAssertNegationClean() {
		final var flag = true;
		final var o = anyObject();
		assertTrue(!flag);
		assertFalse(!flag);
		assertTrue(!(o instanceof String));
		assertFalse(!(o instanceof Integer));
	}
}