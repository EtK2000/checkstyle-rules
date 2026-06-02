package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class UpperEllFixerTest {
	private static final String TOPIC = "upperell";

	private final CheckstyleFixer fixer = new UpperEllFixer();

	@Test
	public void testNoLiteralCharsAtColumn() throws Exception {
		assertSkip(fixer, TOPIC, "no_literal_chars_at_column");
	}
}