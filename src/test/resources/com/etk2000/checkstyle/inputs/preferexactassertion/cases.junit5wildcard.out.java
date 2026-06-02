package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: junit5_wildcard_unqualified_comparison_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.jupiter.api.Assertions.*
class Junit5WildcardUnqualifiedComparisonFiresSlice {
	void m() {
		assertTrue(1 > 0);
	}
}
// === end ===

// === case: junit5_wildcard_unqualified_instanceof_fires ===
// imports: static org.junit.jupiter.api.Assertions.*
class Junit5WildcardUnqualifiedInstanceofFiresSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===