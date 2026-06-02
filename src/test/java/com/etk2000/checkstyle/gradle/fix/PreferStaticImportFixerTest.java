package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferStaticImportFixerTest {
	private static final String TOPIC = "preferstaticimport";

	private final CheckstyleFixer fixer = new PreferStaticImportFixer();

	@Test
	public void testDotWithNoMethodIdentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "dot_with_no_method_ident");
	}
}