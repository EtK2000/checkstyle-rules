package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: junit4_wildcard_unqualified_comparison_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.*
class Junit4WildcardUnqualifiedComparisonFiresSlice {
	void m() {
		assertTrue(1 > 0);
	}
}
// === end ===

// === case: junit4_wildcard_unqualified_negation_fires ===
// imports: static org.junit.Assert.*
class Junit4WildcardUnqualifiedNegationFiresSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===