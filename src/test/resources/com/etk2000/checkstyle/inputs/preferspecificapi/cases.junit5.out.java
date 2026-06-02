package com.etk2000.checkstyle.inputs.preferspecificapi;

// === case: assert_equals_false_junit5 ===
// imports: static org.junit.jupiter.api.Assertions.assertEquals
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class InputSpecificApiAssertAssertEqualsFalseJunit5SliceViolation {
	void assertEqualsFalseJunit5(boolean result) {
		assertFalse(result);
	}
}
// === end ===