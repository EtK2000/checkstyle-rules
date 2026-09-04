package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class RedundantModifierFixerTest {
	private static final String TOPIC = "redundantmodifier";

	private final CheckstyleFixer fixer = new RedundantModifierFixer();

	@Test
	public void testKeywordFollowedByDigitIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "keyword_followed_by_digit_is_stale", SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);
	}

	@Test
	public void testKeywordInsideIdentifierIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "keyword_inside_identifier_is_stale", SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);
	}

	@Test
	public void testKeywordPrefixOfIdentifierIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "keyword_prefix_of_identifier_is_stale", SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);
	}

	@Test
	public void testNotLetterAtColumn() throws Exception {
		assertSkipResult(fixer, TOPIC, "not_letter_at_column", SkipMessages.REDUNDANT_MODIFIER_STALE_COLUMN);
	}

	@Test
	public void testRemoveAtEndOfLineNoTrailingSpace() throws Exception {
		// can't migrate: snippet is a bare fragment that is not parseable as a compilation unit
		assertSimpleFix(fixer, TOPIC, "remove_at_end_of_line_no_trailing_space");
	}
}