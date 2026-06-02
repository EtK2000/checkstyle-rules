package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: mixedimports_comparison_assert_false_still_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertFalse
class MixedimportsComparisonAssertFalseStillFiresSlice {
	void m() {
		assertFalse(1 == 1);
	}
}
// === end ===

// === case: mixedimports_comparison_still_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class MixedimportsComparisonStillFiresSlice {
	void m() {
		assertTrue(1 > 0);
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_fqn_instanceof_fires ===
class MixedimportsQualifiedAssertionsFqnInstanceofFiresSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_simple_name_assert_false_instanceof_fires ===
// imports: org.junit.jupiter.api.Assertions
class MixedimportsQualifiedAssertionsSimpleNameAssertFalseInstanceofFiresSlice {
	void m(Object o) {
		Assertions.assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: mixedimports_qualified_assertions_simple_name_instanceof_fires ===
// imports: org.junit.jupiter.api.Assertions
class MixedimportsQualifiedAssertionsSimpleNameInstanceofFiresSlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===