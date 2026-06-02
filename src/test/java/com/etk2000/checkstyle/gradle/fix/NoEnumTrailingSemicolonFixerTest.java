package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;

import org.junit.jupiter.api.Test;

public class NoEnumTrailingSemicolonFixerTest {
	private static final String TOPIC = "noenumtrailingsemicolon";

	private final CheckstyleFixer fixer = new NoEnumTrailingSemicolonFixer();

	@Test
	public void testRemoveSemicolonWithTrailingWhitespace() throws Exception {
		assertSimpleFix(fixer, TOPIC, "remove_semicolon_with_trailing_whitespace");
	}
}