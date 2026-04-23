package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
class InputPreferExactAssertionClean {
	void assertEqualsNotFlagged() {
		assertEquals(5, 3 + 2);
	}

	void assertFalseNoComparison() {
		assertFalse(false);
	}

	void assertTrueCompoundCondition() {
		final var a = 1;
		final var b = 2;
		assertTrue(a > 0 && b > 0);
		assertTrue(a > 0 || b < 5);
		assertFalse(a >= 0 && b <= 10);
		assertFalse(a == 1 || b != 2);
	}

	void assertTrueMethodCall() {
		assertTrue("hello".isEmpty());
	}

	void assertTrueNoComparison() {
		assertTrue(true);
	}

	private void customAssertTrue(boolean flag) {
	}

	void noFireOnParenthesizedComparison() {
		assertTrue((1 > 0));
	}

	void noFireOnSimilarMethodName() {
		customAssertTrue(1 > 0);
	}

	void noFireOnTwoArgNoComparison() {
		assertTrue("message", true);
		assertFalse("message", false);
	}

	void otherAssertMethodsNotFlagged() {
		final var a = 1;
		final var b = 2;
		assertEquals(a, b);
		assertNotEquals(a, b);
		assertNull(null);
		assertNotNull(a);
		assertSame(a, b);
		assertNotSame(a, b);
	}

	void otherMethodWithComparison() {
		final var a = 1;
		someMethod(a > 0);
	}

	private void someMethod(boolean flag) {
	}
}