package com.etk2000.checkstyle.inputs.preferspecificapi;

// === case: assert_equals_false_junit5 ===
// imports: static org.junit.jupiter.api.Assertions.assertEquals
class InputSpecificApiAssertAssertEqualsFalseJunit5SliceViolation {
	void assertEqualsFalseJunit5(boolean result) {
		assertEquals(false, result); // violation [minSdk>=35]: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===