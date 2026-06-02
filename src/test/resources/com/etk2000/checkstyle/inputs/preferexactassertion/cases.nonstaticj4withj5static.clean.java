package com.etk2000.checkstyle.inputs.preferexactassertion;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;

@SuppressWarnings("unused")
class InputPreferExactAssertionNonStaticJ4WithJ5StaticClean {
	private static Object anyObject() {
		return "x";
	}

	void qualifiedAssertViaNonStaticImportSuppressed() {
		final var o = anyObject();
		Assert.assertTrue(o instanceof String);
	}

	void unusedAssertTrueToConsumeImport() {
		assertTrue(true);
	}
}