package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: wrapped_after_static_import_resolves_negation ===
import static
		org.junit.jupiter.api.Assertions.assertTrue;

class WrappedAfterStaticImportResolvesNegationSlice {
	void m(boolean flag) {
		assertTrue(!flag); // violation: Use 'assertFalse' instead of 'assertTrue' with a negated argument.
		assertTrue(flag);
	}
}
// === end ===