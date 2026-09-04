package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: wrapped_before_semicolon_import_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
import static org.junit.jupiter.api.Assertions.assertTrue
		;

class WrappedBeforeSemicolonImportResolvesSlice {
	void m(Object o, boolean flag) {
		assertInstanceOf(String.class, o);
		assertTrue(flag);
	}
}
// === end ===