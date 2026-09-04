package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: wrapped_mid_fqn_import_resolves ===
import static org.junit.jupiter.api.
		Assertions.assertTrue;

class WrappedMidFqnImportResolvesSlice {
	void m(Object o, boolean flag) {
		assertTrue(o instanceof String); // violation: Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.
		assertTrue(flag);
	}
}
// === end ===