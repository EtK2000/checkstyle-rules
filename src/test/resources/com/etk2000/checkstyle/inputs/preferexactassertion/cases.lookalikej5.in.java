package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: lookalike_assert_exact_with_real_junit5_negation_fixed ===
// imports: static com.foo.Assert.assertNotNull
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertExactWithRealJunit5NegationFixedSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
	}
}
// === end ===

// === case: lookalike_assert_wildcard_with_real_junit5_instanceof_fixed ===
// imports: static com.foo.Assert.*
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertWildcardWithRealJunit5InstanceofFixedSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: lookalike_assert_wildcard_with_real_junit5_negation_fixed ===
// imports: static com.foo.Assert.*
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertWildcardWithRealJunit5NegationFixedSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
	}
}
// === end ===

// === case: lookalike_assertions_exact_after_real_instanceof_fixed ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
// imports: static zz.example.Assertions.assertEquals
class LookalikeAssertionsExactAfterRealInstanceofFixedSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: lookalike_assertions_exact_before_real_instanceof_fixed ===
// imports: static com.example.Assertions.assertEquals
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertionsExactBeforeRealInstanceofFixedSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: lookalike_assertions_exact_before_real_negation_fixed ===
// imports: static com.example.Assertions.assertEquals
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertionsExactBeforeRealNegationFixedSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
	}
}
// === end ===

// === case: lookalike_assertions_unknown_member_before_real_instanceof_fixed ===
// imports: static com.example.Assertions.assertThrowsExactly
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertionsUnknownMemberBeforeRealInstanceofFixedSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: lookalike_assertions_wildcard_before_real_instanceof_fixed ===
// imports: static com.example.Assertions.*
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertionsWildcardBeforeRealInstanceofFixedSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: lookalike_assertions_wildcard_before_real_negation_fixed ===
// imports: static com.example.Assertions.*
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class LookalikeAssertionsWildcardBeforeRealNegationFixedSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
	}
}
// === end ===