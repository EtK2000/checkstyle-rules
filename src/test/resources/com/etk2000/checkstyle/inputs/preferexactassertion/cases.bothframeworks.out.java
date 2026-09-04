package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: negation_both_frameworks_import_ambiguous ===
// skip-reason: cannot rewrite the assertion: both JUnit 4 and JUnit 5 assertions are statically imported
// imports: static org.junit.Assert.assertTrue
// imports: static org.junit.jupiter.api.Assertions.assertFalse
class NegationBothFrameworksImportAmbiguousSlice {
	void m(boolean flag) {
		assertTrue(!flag);
		// keeps the JUnit 5 import in use, so the unused-import fixer cannot resolve the ambiguity first
		assertFalse(flag);
	}
}
// === end ===