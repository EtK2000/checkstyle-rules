package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class NoUnnecessaryThisFixerTest {
	private static final String TOPIC = "nounnecessarythis";

	private final CheckstyleFixer fixer = new NoUnnecessaryThisFixer();

	@Test
	public void testStaleColumnAfterSiblingEdit() throws Exception {
		assertSkip(fixer, TOPIC, "stale_column_after_sibling_edit");
	}

	@Test
	public void testUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "unparseable_buffer");
	}
}