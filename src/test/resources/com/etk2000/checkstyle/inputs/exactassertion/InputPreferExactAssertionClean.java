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
	private static class TestHelper {
		void assertTrue(boolean cond) {
		}
	}

	private static Object anyObject() {
		return "x";
	}

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

	void assertTrueInstanceOfPatternBinding() {
		final var o = anyObject();
		assertTrue(o instanceof String s && s.length() > 0);
	}

	void assertTrueInstanceOfPatternBindingJunit4MessageFirst() {
		final var o = anyObject();
		assertTrue("msg", o instanceof String s && s.length() > 0);
	}

	void assertTrueMethodCall() {
		assertTrue("hello".isEmpty());
	}

	void assertTrueNegatedNonInstanceOf() {
		final var o = anyObject();
		assertTrue(!o.toString().isEmpty());
		assertFalse(!"hello".isEmpty());
	}

	void assertTrueNoComparison() {
		assertTrue(true);
	}

	private void customAssertTrue(boolean flag) {
	}

	void junit4ChainedReceiverInstanceOfSuppressed() {
		final var o = anyObject();
		someHelper().assertTrue(o instanceof String);
	}

	void junit4FullyQualifiedTypeSuppressed() {
		final var o = anyObject();
		assertTrue(o instanceof java.lang.String);
	}

	void junit4MessageFirstInstanceOfSuppressed() {
		final var o = anyObject();
		assertTrue("should be a string", o instanceof String);
	}

	void junit4QualifiedAssertFalseInstanceOfSuppressed() {
		final var o = anyObject();
		org.junit.Assert.assertFalse(o instanceof Integer);
	}

	void junit4QualifiedAssertInstanceOfSuppressed() {
		final var o = anyObject();
		org.junit.Assert.assertTrue(o instanceof String);
	}

	void junit4UnqualifiedInstanceOfAssertFalseSuppressed() {
		final var o = anyObject();
		assertFalse(o instanceof Integer);
	}

	void junit4UnqualifiedInstanceOfDoubleNegatedSuppressed() {
		final var o = anyObject();
		assertTrue(!!(o instanceof String));
	}

	void junit4UnqualifiedInstanceOfNegatedSuppressed() {
		final var o = anyObject();
		assertTrue(!(o instanceof String));
	}

	void junit4UnqualifiedInstanceOfSuppressed() {
		final var o = anyObject();
		assertTrue(o instanceof String);
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

	private TestHelper someHelper() {
		return new TestHelper();
	}

	private void someMethod(boolean flag) {
	}
}