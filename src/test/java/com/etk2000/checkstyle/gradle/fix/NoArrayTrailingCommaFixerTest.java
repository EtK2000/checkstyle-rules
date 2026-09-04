package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class NoArrayTrailingCommaFixerTest {
	private static final String TOPIC = "noarraytrailingcomma";

	private final CheckstyleFixer fixer = new NoArrayTrailingCommaFixer();

	@Test
	public void testColumnNotOnCommaIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "column_not_on_comma_is_stale", SkipMessages.NO_ARRAY_TRAILING_COMMA_SKIP_STALE);
	}

	@Test
	public void testRemoveTrailingCommaWithTrailingWhitespace() throws Exception {
		// can't migrate: violation-comment stripping eats the trailing whitespace this case is meant to exercise
		assertSimpleFix(fixer, TOPIC, "remove_trailing_comma_with_trailing_whitespace");
	}
}