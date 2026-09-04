package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferDoWhileFixerTest {
	private static final String TOPIC = "preferdowhile";

	private final CheckstyleFixer fixer = new PreferDoWhileFixer();

	@Test
	public void testBracedBodyMissingClosingLine() throws Exception {
		assertSkip(fixer, TOPIC, "braced_body_missing_closing_line");
	}

	@Test
	public void testWhileIsLastLine() throws Exception {
		assertSkip(fixer, TOPIC, "while_is_last_line");
	}

	@Test
	public void testWhileLineFormatNotMatched() throws Exception {
		assertSkip(fixer, TOPIC, "while_line_format_not_matched");
	}
}