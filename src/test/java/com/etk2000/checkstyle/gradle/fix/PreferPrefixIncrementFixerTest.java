package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferPrefixIncrementFixerTest {
	private static final String TOPIC = "preferprefixincrement";

	private final CheckstyleFixer fixer = new PreferPrefixIncrementFixer();

	@Test
	public void testUnparseableBufferRefused() throws Exception {
		assertSkip(fixer, TOPIC, "unparseable_buffer_refused");
	}
}