package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantModifierFixerTest {
	private static final String TOPIC = "redundantmodifier";

	private final CheckstyleFixer fixer = new RedundantModifierFixer();

	@Test
	public void testNotLetterAtColumn() throws Exception {
		assertSkip(fixer, TOPIC, "not_letter_at_column");
	}

	@Test
	public void testRemoveAtEndOfLineNoTrailingSpace() throws Exception {
		// can't migrate: snippet is a bare fragment that is not parseable as a compilation unit
		assertSimpleFix(fixer, TOPIC, "remove_at_end_of_line_no_trailing_space");
	}
}