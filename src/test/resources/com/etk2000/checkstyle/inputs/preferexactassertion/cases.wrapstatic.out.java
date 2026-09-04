package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: wrapped_after_static_import_resolves_negation ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
import static
		org.junit.jupiter.api.Assertions.assertTrue;

class WrappedAfterStaticImportResolvesNegationSlice {
	void m(boolean flag) {
		assertFalse(flag);
		assertTrue(flag);
	}
}
// === end ===