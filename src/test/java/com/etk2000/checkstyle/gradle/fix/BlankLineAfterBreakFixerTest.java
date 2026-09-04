package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class BlankLineAfterBreakFixerTest {
	private static final String TOPIC = "blanklineafterbreak";

	private final CheckstyleFixer fixer = new BlankLineAfterBreakFixer();

	@Test
	public void testAlreadyHasBlankLine() throws Exception {
		assertSkip(fixer, TOPIC, "already_has_blank_line");
	}

	@Test
	public void testInsertBlankBeforeCase() throws Exception {
		// can't migrate: BlankLineAfterBreak is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "insert_blank_before_case");
	}

	@Test
	public void testInsertBlankBeforeDefault() throws Exception {
		// can't migrate: BlankLineAfterBreak is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "insert_blank_before_default");
	}

	@Test
	public void testInsertBlankBelowTextBlock() throws Exception {
		// can't migrate: BlankLineAfterBreak is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "insert_blank_below_text_block");
	}

	@Test
	public void testNoNextLine() throws Exception {
		assertSkip(fixer, TOPIC, "no_next_line");
	}

	@Test
	public void testSkipBreakInsideTextBlock() throws Exception {
		assertSkipResult(fixer, TOPIC, "skip_break_inside_text_block", SkipMessages.BLANK_LINE_SKIP_TEXT_BLOCK);
	}
}