package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantAnnotationSyntaxFixerTest {
	private static final String TOPIC = "redundantannotationsyntax";

	private final CheckstyleFixer fixer = new RedundantAnnotationSyntaxFixer();

	@Test
	public void testUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "unparseable_buffer");
	}
}