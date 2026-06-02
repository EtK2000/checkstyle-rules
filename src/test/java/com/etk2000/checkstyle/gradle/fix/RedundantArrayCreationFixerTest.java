package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class RedundantArrayCreationFixerTest {
	private static final String TOPIC = "redundantarraycreation";

	private final CheckstyleFixer fixer = new RedundantArrayCreationFixer();

	@Test
	public void testNoBraceOnLineSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_brace_on_line_skipped", "multi-line array initializer");
	}
}