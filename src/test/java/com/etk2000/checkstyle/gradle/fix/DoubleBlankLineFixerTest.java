package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class DoubleBlankLineFixerTest {
	private static final String TOPIC = "doubleblankline";

	private final CheckstyleFixer fixer = new DoubleBlankLineFixer();

	@Test
	public void testCollapseBlankInsideBlockComment() throws Exception {
		// can't migrate: NoDoubleBlankLines is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "collapse_blank_inside_block_comment");
	}

	@Test
	public void testCollapseDoubleBlankToSingle() throws Exception {
		// can't migrate: NoDoubleBlankLines is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "collapse_double_blank_to_single");
	}

	@Test
	public void testCollapseTripleBlankToSingle() throws Exception {
		// can't migrate: NoDoubleBlankLines is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "collapse_triple_blank_to_single");
	}

	@Test
	public void testLastLineNoBlankAfter() throws Exception {
		assertSkip(fixer, TOPIC, "last_line_no_blank_after");
	}

	@Test
	public void testNextLineNotBlank() throws Exception {
		assertSkip(fixer, TOPIC, "next_line_not_blank");
	}

	@Test
	public void testSingleBlankNotFixed() throws Exception {
		assertSkip(fixer, TOPIC, "single_blank_not_fixed");
	}

	@Test
	public void testSkipBlankInsideTextBlock() throws Exception {
		assertSkipResult(fixer, TOPIC, "skip_blank_inside_text_block", SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);
	}

	@Test
	public void testWhitespaceOnlyLinesCollapsed() throws Exception {
		// can't migrate: NoDoubleBlankLines is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "whitespace_only_lines_collapsed");
	}
}