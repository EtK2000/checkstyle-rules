package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantEqualityBranchFixerTest {
	private static final String TOPIC = "redundantequalitybranch";

	private final CheckstyleFixer fixer = new RedundantEqualityBranchFixer();

	@Test
	public void testAssignTruncatedAfterIf() throws Exception {
		assertSkip(fixer, TOPIC, "assign_truncated_after_if");
	}

	@Test
	public void testNoMatchIfIsLastLine() throws Exception {
		assertSkip(fixer, TOPIC, "no_match_if_is_last_line");
	}
}