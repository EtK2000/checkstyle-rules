package com.etk2000.checkstyle.inputs.preferexactassertion;

@SuppressWarnings("unused")
class InputPreferExactAssertionLookalikeFqnClean {
	private static Object anyObject() {
		return "x";
	}

	void qualifiedLookalikeComparisonClean() {
		final var a = 1;
		final var b = 2;
		com.example.Assertions.assertTrue(a == b);
		com.foo.Assert.assertFalse(a != b);
	}

	void qualifiedLookalikeInstanceofClean() {
		final var o = anyObject();
		com.example.Assertions.assertTrue(o instanceof String);
		com.foo.Assert.assertFalse(o instanceof Integer);
	}

	void qualifiedLookalikeNegationClean() {
		final var flag = true;
		com.example.Assertions.assertTrue(!flag);
		com.foo.Assert.assertFalse(!flag);
	}
}