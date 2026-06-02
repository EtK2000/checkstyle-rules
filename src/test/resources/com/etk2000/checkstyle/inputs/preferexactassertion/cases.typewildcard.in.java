package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: typewildcard_comparison_fires_under_j5_static_import ===
// skip-reason: complex assertion form
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TypewildcardComparisonFiresUnderJ5StaticImportSlice {
	void m() {
		assertTrue(1 > 0); // violation: Use 'assertEquals' instead of 'assertTrue' with '>'.
	}
}
// === end ===

// === case: typewildcard_j4_does_not_count_unqualified_fires_via_j5_static ===
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class TypewildcardJ4DoesNotCountUnqualifiedFiresViaJ5StaticSlice {
	void m(Object o) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: typewildcard_qualified_assert_comparison_still_fires ===
// skip-reason: complex assertion form
// imports: org.junit.Assert
class TypewildcardQualifiedAssertComparisonStillFiresSlice {
	void m() {
		Assert.assertTrue(1 > 0); // violation: Use 'assertEquals' instead of 'assertTrue' with '>'.
	}
}
// === end ===