package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: wrapped_mid_fqn_import_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.
		Assertions.assertTrue;

class WrappedMidFqnImportResolvesSlice {
	void m(Object o, boolean flag) {
		assertInstanceOf(String.class, o);
		assertTrue(flag);
	}
}
// === end ===