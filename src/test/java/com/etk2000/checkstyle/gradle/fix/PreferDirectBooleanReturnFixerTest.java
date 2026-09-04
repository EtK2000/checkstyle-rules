package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferDirectBooleanReturnFixerTest {
	private static final String TOPIC = "preferdirectbooleanreturn";

	private final CheckstyleFixer fixer = new PreferDirectBooleanReturnFixer();

	@Test
	public void testBracedBodyTruncated() throws Exception {
		assertSkip(fixer, TOPIC, "braced_body_truncated");
	}

	@Test
	public void testBracedElseTruncated() throws Exception {
		assertSkip(fixer, TOPIC, "braced_else_truncated");
	}

	@Test
	public void testEmptyConditionSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "empty_condition_skipped");
	}

	@Test
	public void testIfWithoutOpenParen() throws Exception {
		assertSkip(fixer, TOPIC, "if_without_open_paren");
	}

	@Test
	public void testMultilineCloseParenNeverFound() throws Exception {
		assertSkip(fixer, TOPIC, "multiline_close_paren_never_found");
	}

	@Test
	public void testMultilineUnicodeEscapeInCondSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "multiline_unicode_escape_in_cond_skipped");
	}

	@Test
	public void testMultilineUnterminatedBlockCommentSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "multiline_unterminated_block_comment_skipped");
	}

	@Test
	public void testNextLineBodyNoBodyLineSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "next_line_body_no_body_line_skipped");
	}

	@Test
	public void testUnicodeEscapeInCondSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "unicode_escape_in_cond_skipped");
	}

	@Test
	public void testUnterminatedBlockCommentInCondSkipped() throws Exception {
		assertSkip(fixer, TOPIC, "unterminated_block_comment_in_cond_skipped");
	}
}