package com.etk2000.checkstyle.inputs.exactassertion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
class InputPreferExactAssertionViolation {
	private static Object anyObject() {
		return "x";
	}

	private static Throwable anyThrowable() {
		return new RuntimeException();
	}

	void assertFalseEqual() {
		assertFalse(1 == 2); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '=='.
	}

	void assertFalseGe() {
		assertFalse(1 >= 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>='.
	}

	void assertFalseGt() {
		assertFalse(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>'.
	}

	void assertFalseInstanceOf() {
		final var o = anyObject();
		assertFalse(o instanceof Integer); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void assertFalseInstanceOfFullyQualified() {
		final var o = anyObject();
		assertFalse(o instanceof java.lang.Integer); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void assertFalseInstanceOfNegated() {
		final var o = anyObject();
		assertFalse(!(o instanceof Integer)); // violation: Use 'assertInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void assertFalseInstanceOfNegatedTwice() {
		final var o = anyObject();
		assertFalse(!!(o instanceof Integer)); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void assertFalseInstanceOfNested() {
		final var o = anyObject();
		assertFalse(o instanceof java.util.Map.Entry); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}

	void assertFalseLe() {
		assertFalse(0 <= 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '<='.
	}

	void assertFalseLt() {
		assertFalse(0 < 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '<'.
	}

	void assertFalseNotEqual() {
		assertFalse(1 != 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '!='.
	}

	void assertTrueComplexLhsInstanceOf() {
		final var ex = anyThrowable();
		assertTrue(ex.getCause() instanceof RuntimeException); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueEqual() {
		assertTrue(1 == 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '=='.
	}

	void assertTrueGe() {
		assertTrue(2 >= 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>='.
	}

	void assertTrueGt() {
		assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void assertTrueInstanceOf() {
		final var o = anyObject();
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueInstanceOfFullyQualified() {
		final var o = anyObject();
		assertTrue(o instanceof java.lang.String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueInstanceOfNegated() {
		final var o = anyObject();
		assertTrue(!(o instanceof String)); // violation: Use 'assertNotInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueInstanceOfNegatedTwice() {
		final var o = anyObject();
		assertTrue(!!(o instanceof String)); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueInstanceOfNested() {
		final var o = anyObject();
		assertTrue(o instanceof java.util.Map.Entry); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void assertTrueLe() {
		assertTrue(0 <= 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '<='.
	}

	void assertTrueLt() {
		assertTrue(0 < 1); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '<'.
	}

	void assertTrueNotEqual() {
		assertTrue(1 != 2); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '!='.
	}

	void qualifiedAssertFalseCall() {
		org.junit.Assert.assertFalse(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>'.
	}

	void qualifiedAssertTrueCall() {
		org.junit.Assert.assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void qualifiedJunit5AssertFalse() {
		org.junit.jupiter.api.Assertions.assertFalse(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>'.
	}

	void qualifiedJunit5AssertTrue() {
		org.junit.jupiter.api.Assertions.assertTrue(1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void twoArgJunit4AssertFalse() {
		assertFalse("should be negative", 1 >= 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>='.
	}

	void twoArgJunit4AssertTrue() {
		assertTrue("should be positive", 1 > 0); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void twoArgJunit4AssertTrueInstanceOf() {
		final var o = anyObject();
		assertTrue("should be a string", o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}

	void twoArgJunit5AssertFalse() {
		assertFalse(1 >= 0, "should be negative"); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertFalse' with '>='.
	}

	void twoArgJunit5AssertTrue() {
		assertTrue(1 > 0, "should be positive"); // violation: Use a dedicated assertion (e.g. 'assertEquals') instead of 'assertTrue' with '>'.
	}

	void twoArgJunit5AssertTrueInstanceOf() {
		final var o = anyObject();
		assertTrue(o instanceof String, "should be a string"); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}