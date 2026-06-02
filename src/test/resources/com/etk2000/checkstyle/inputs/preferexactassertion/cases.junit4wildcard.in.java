package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: junit4_wildcard_unqualified_comparison_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.*
class Junit4WildcardUnqualifiedComparisonFiresSlice {
	void m() {
		assertTrue(1 > 0); // violation: Use 'assertEquals' instead of 'assertTrue' with '>'.
	}
}
// === end ===

// === case: junit4_wildcard_unqualified_negation_fires ===
// imports: static org.junit.Assert.*
class Junit4WildcardUnqualifiedNegationFiresSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
	}
}
// === end ===