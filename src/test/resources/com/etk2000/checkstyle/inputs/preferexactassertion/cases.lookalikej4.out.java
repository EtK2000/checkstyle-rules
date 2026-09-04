package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: lookalike_assert_exact_with_real_junit4_negation_fixed ===
// imports: static com.foo.Assert.assertNotNull
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class LookalikeAssertExactWithRealJunit4NegationFixedSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: lookalike_assertions_wildcard_with_real_junit4_negation_fixed ===
// imports: static com.example.Assertions.*
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class LookalikeAssertionsWildcardWithRealJunit4NegationFixedSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: lookalike_assertions_with_real_junit4_comparison_still_fires ===
// skip-reason: cannot rewrite the assertion: a comparison needs the operand types to rewrite
// imports: static com.example.Assertions.assertEquals
// imports: static org.junit.Assert.assertTrue
class LookalikeAssertionsWithRealJunit4ComparisonStillFiresSlice {
	void m(int a, int b) {
		assertTrue(a == b);
	}
}
// === end ===

// === case: lookalike_assertions_with_real_junit4_instanceof_negation_fallback_fixed ===
// imports: static com.example.Assertions.assertEquals
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class LookalikeAssertionsWithRealJunit4InstanceofNegationFallbackFixedSlice {
	void m(Object o) {
		assertFalse(o instanceof String);
	}
}
// === end ===

// === case: lookalike_assertions_with_real_junit4_negation_fixed ===
// imports: static com.example.Assertions.assertEquals
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class LookalikeAssertionsWithRealJunit4NegationFixedSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===