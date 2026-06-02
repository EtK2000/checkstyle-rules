package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: mixedimports_comparison_assert_false_still_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertFalse
class MixedimportsComparisonAssertFalseStillFiresSlice {
	void m() {
		assertFalse(1 == 1); // violation: Use 'assertNotEquals' instead of 'assertFalse' with '=='.
	}
}
// === end ===

// === case: mixedimports_comparison_still_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class MixedimportsComparisonStillFiresSlice {
	void m() {
		assertTrue(1 > 0); // violation: Use 'assertEquals' instead of 'assertTrue' with '>'.
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_fqn_instanceof_fires ===
class MixedimportsQualifiedAssertionsFqnInstanceofFiresSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_simple_name_assert_false_instanceof_fires ===
// imports: org.junit.jupiter.api.Assertions
class MixedimportsQualifiedAssertionsSimpleNameAssertFalseInstanceofFiresSlice {
	void m(Object o) {
		Assertions.assertFalse(o instanceof Integer); // violation: Use 'assertNotInstanceOf' instead of 'assertFalse' with 'instanceof'.
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_simple_name_instanceof_fires ===
// imports: org.junit.jupiter.api.Assertions
class MixedimportsQualifiedAssertionsSimpleNameInstanceofFiresSlice {
	void m(Object o) {
		Assertions.assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
	}
}
// === end ===