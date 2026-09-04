package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantArrayCreationFixerTest {
	private static final String TOPIC = "redundantarraycreation";

	private final CheckstyleFixer fixer = new RedundantArrayCreationFixer();

	/**
	 * A stale column in a buffer that <em>does</em> parse is the same mechanism one step on, and is
	 * pinned by {@code RedundantArrayCreationLocatorTest}'s neighbouring-column rows rather than by
	 * a second fragment here.
	 */
	@Test
	public void testUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "unparseable_buffer");
	}
}