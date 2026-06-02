package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SuppressWarnings("unused")
class InputPreferExactAssertionUnknownReceiverClean {
	private static class Helper {
		void assertFalse(boolean cond) {
		}

		void assertTrue(boolean cond) {
		}
	}

	private static Object anyObject() {
		return "x";
	}

	private final Helper helper = new Helper();

	void chainedReceiverClean() {
		final var flag = true;
		final var a = 1;
		final var b = 2;
		final var o = anyObject();
		getHelper().assertTrue(!flag);
		getHelper().assertTrue(a == b);
		getHelper().assertTrue(!(o instanceof String));
		getHelper().assertTrue(o instanceof String);
	}

	private Helper getHelper() {
		return helper;
	}

	void helperReceiverComparisonClean() {
		final var a = 1;
		final var b = 2;
		helper.assertTrue(a == b);
		helper.assertFalse(a != b);
		helper.assertTrue(a < b);
		helper.assertFalse(a >= b);
	}

	void helperReceiverInstanceofClean() {
		final var o = anyObject();
		helper.assertTrue(o instanceof String);
		helper.assertFalse(o instanceof Integer);
		helper.assertTrue(!(o instanceof String));
		helper.assertFalse(!(o instanceof Integer));
	}

	void helperReceiverNegationClean() {
		final var flag = true;
		helper.assertTrue(!flag);
		helper.assertFalse(!flag);
	}

	void parenReceiverClean() {
		final var flag = true;
		final var a = 1;
		final var b = 2;
		final var o = anyObject();
		(helper).assertTrue(!flag);
		(helper).assertTrue(a == b);
		(helper).assertTrue(!(o instanceof String));
	}

	void unqualifiedJunit5StillFiresElsewhere() {
		final var o = anyObject();
		assertInstanceOf(String.class, o);
	}
}