package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: nonstaticj4_with_j5static_unqualified_instanceof_fires_ignoring_nonstatic_j4_import ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: static org.junit.jupiter.api.Assertions.assertTrue
// imports: org.junit.Assert
class NonstaticJ4WithJ5StaticUnqualifiedInstanceofFiresIgnoringNonstaticJ4ImportSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
		Assert.class.getSimpleName();
	}
}
// === end ===