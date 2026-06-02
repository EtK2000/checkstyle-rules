package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class TrailingWhitespaceFixerTest {
	private static final String TOPIC = "trailingwhitespace";

	private final CheckstyleFixer fixer = new TrailingWhitespaceFixer();

	@Test
	public void testBlankLineWithSpaces() throws Exception {
		// can't migrate: backing check is RegexpSingleline (AbstractFileSetCheck), not an AbstractCheck, assertCaseFix requires Class<? extends AbstractCheck>
		assertSimpleFix(fixer, TOPIC, "blank_line_with_spaces");
	}

	@Test
	public void testBlankLineWithTab() throws Exception {
		// can't migrate: backing check is RegexpSingleline (AbstractFileSetCheck), not an AbstractCheck, assertCaseFix requires Class<? extends AbstractCheck>
		assertSimpleFix(fixer, TOPIC, "blank_line_with_tab");
	}

	@Test
	public void testNoTrailingWhitespace() throws Exception {
		assertSkip(fixer, TOPIC, "no_trailing_whitespace");
	}

	@Test
	public void testTrailingSpaces() throws Exception {
		// can't migrate: backing check is RegexpSingleline (AbstractFileSetCheck), not an AbstractCheck, assertCaseFix requires Class<? extends AbstractCheck>
		assertSimpleFix(fixer, TOPIC, "trailing_spaces");
	}

	@Test
	public void testTrailingTab() throws Exception {
		// can't migrate: backing check is RegexpSingleline (AbstractFileSetCheck), not an AbstractCheck, assertCaseFix requires Class<? extends AbstractCheck>
		assertSimpleFix(fixer, TOPIC, "trailing_tab");
	}
}