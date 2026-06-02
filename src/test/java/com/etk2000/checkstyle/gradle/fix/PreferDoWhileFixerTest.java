package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferDoWhileFixerTest {
	private static final String TOPIC = "preferdowhile";

	private final CheckstyleFixer fixer = new PreferDoWhileFixer();

	@Test
	public void testBracedBodyMissingClosingLine() throws Exception {
		assertSkip(fixer, TOPIC, "braced_body_missing_closing_line");
	}

	@Test
	public void testPreStmtBlankLineSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "pre_stmt_blank_line_skipped", "pre-statement formatting");
	}

	@Test
	public void testWhileIsLastLine() throws Exception {
		assertSkip(fixer, TOPIC, "while_is_last_line");
	}

	@Test
	public void testWhileLineFormatNotMatched() throws Exception {
		assertSkipResult(fixer, TOPIC, "while_line_format_not_matched", "while line not in expected format (multi-line cond, trailing content, or comment)");
	}
}