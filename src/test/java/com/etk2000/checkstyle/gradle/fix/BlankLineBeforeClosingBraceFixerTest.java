package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class BlankLineBeforeClosingBraceFixerTest {
	private static final String TOPIC = "blanklinebeforeclosingbrace";

	private final CheckstyleFixer fixer = new BlankLineBeforeClosingBraceFixer();

	@Test
	public void testDeleteMultipleBlanksBeforeCloseBrace() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_multiple_blanks_before_close_brace");
	}

	@Test
	public void testDeleteSingleBlankBeforeCloseBrace() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_single_blank_before_close_brace");
	}

	@Test
	public void testDeleteWhenLineIndexIsBlank() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_when_line_index_is_blank");
	}

	@Test
	public void testDeleteWhenLineIndexIsBlankTriple() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_when_line_index_is_blank_triple");
	}

	@Test
	public void testDeleteWhenLineIndexIsBlankTripleFromLast() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_when_line_index_is_blank_triple_from_last");
	}

	@Test
	public void testDeleteWhenLineIndexIsWhitespaceOnly() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_when_line_index_is_whitespace_only");
	}

	@Test
	public void testDeleteWhenLineIndexReachesZero() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_when_line_index_reaches_zero");
	}

	@Test
	public void testDeleteWhitespaceOnlyBeforeCloseBrace() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_whitespace_only_before_close_brace");
	}

	@Test
	public void testFixBlankAfterTextBlockClose() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "fix_blank_after_text_block_close");
	}

	@Test
	public void testFixBlankInsideBlockComment() throws Exception {
		// can't migrate: NoBlankLineBeforeClosingBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "fix_blank_inside_block_comment");
	}

	@Test
	public void testLastLine() throws Exception {
		assertSkip(fixer, TOPIC, "last_line");
	}

	@Test
	public void testNextLineNotBlank() throws Exception {
		assertSkip(fixer, TOPIC, "next_line_not_blank");
	}

	@Test
	public void testSkipBlankInsideTextBlock() throws Exception {
		assertSkipResult(fixer, TOPIC, "skip_blank_inside_text_block", SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);
	}
}