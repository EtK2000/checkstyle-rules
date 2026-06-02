package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;

import org.junit.jupiter.api.Test;

public class NoArrayTrailingCommaFixerTest {
	private static final String TOPIC = "noarraytrailingcomma";

	private final CheckstyleFixer fixer = new NoArrayTrailingCommaFixer();

	@Test
	public void testRemoveTrailingCommaWithTrailingWhitespace() throws Exception {
		// can't migrate: violation-comment stripping eats the trailing whitespace this case is meant to exercise
		assertSimpleFix(fixer, TOPIC, "remove_trailing_comma_with_trailing_whitespace");
	}
}