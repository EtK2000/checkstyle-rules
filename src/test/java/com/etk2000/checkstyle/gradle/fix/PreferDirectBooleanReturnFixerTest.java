package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferDirectBooleanReturnFixerTest {
	private static final String ELSE_BODY_REASON = "no simple collapsible else or trailing return";
	private static final String MULTI_LINE_REASON = "multi-line if condition";
	private static final String THEN_BODY_REASON = "if body is not a simple collapsible return";
	private static final String TOPIC = "preferdirectbooleanreturn";
	private static final String UNICODE_REASON = "Unicode escape in condition";

	private final CheckstyleFixer fixer = new PreferDirectBooleanReturnFixer();

	@Test
	public void testBracedBodyTruncated() throws Exception {
		assertSkipResult(fixer, TOPIC, "braced_body_truncated", THEN_BODY_REASON);
	}

	@Test
	public void testBracedElseTruncated() throws Exception {
		assertSkipResult(fixer, TOPIC, "braced_else_truncated", ELSE_BODY_REASON);
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
		assertSkipResult(fixer, TOPIC, "multiline_close_paren_never_found", MULTI_LINE_REASON);
	}

	@Test
	public void testMultilineUnicodeEscapeInCondSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "multiline_unicode_escape_in_cond_skipped", UNICODE_REASON);
	}

	@Test
	public void testMultilineUnterminatedBlockCommentSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "multiline_unterminated_block_comment_skipped", MULTI_LINE_REASON);
	}

	@Test
	public void testNextLineBodyNoBodyLineSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "next_line_body_no_body_line_skipped", THEN_BODY_REASON);
	}

	@Test
	public void testUnicodeEscapeInCondSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "unicode_escape_in_cond_skipped", UNICODE_REASON);
	}

	@Test
	public void testUnterminatedBlockCommentInCondSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "unterminated_block_comment_in_cond_skipped", MULTI_LINE_REASON);
	}
}