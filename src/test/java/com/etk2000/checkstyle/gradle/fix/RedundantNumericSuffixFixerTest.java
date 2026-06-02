package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantNumericSuffixFixerTest {
	private static final String TOPIC = "redundantnumericsuffix";

	private final CheckstyleFixer fixer = new RedundantNumericSuffixFixer();

	@Test
	public void testColumnAtNonLiteralChar() throws Exception {
		assertSkip(fixer, TOPIC, "column_at_non_literal_char");
	}
}