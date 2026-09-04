package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantCastFixerTest {
	private static final String TOPIC = "redundantcast";

	private final CheckstyleFixer fixer = new RedundantCastFixer();

	@Test
	public void testFragmentBodyDoesNotParse_refuses() throws Exception {
		assertSkip(fixer, TOPIC, "malformed_no_expression");
	}
}