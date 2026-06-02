package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class NoUnnecessaryThisFixerTest {
	private static final String TOPIC = "nounnecessarythis";

	private final CheckstyleFixer fixer = new NoUnnecessaryThisFixer();

	@Test
	public void testColumnEqualsLineLength() throws Exception {
		assertSkip(fixer, TOPIC, "column_equals_line_length");
	}

	@Test
	public void testThisStartMinusOne() throws Exception {
		assertSkip(fixer, TOPIC, "this_start_minus_one");
	}
}