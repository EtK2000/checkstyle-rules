package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class RedundantAnnotationSyntaxFixerTest {
	private static final String TOPIC = "redundantannotationsyntax";

	private final CheckstyleFixer fixer = new RedundantAnnotationSyntaxFixer();

	@Test
	public void testExtractIndentSpaceValueLine() throws Exception {
		assertSimpleFix(fixer, TOPIC, "extract_indent_space_value_line");
	}

	@Test
	public void testMultiLineEmptyParensEofNoClose() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_empty_parens_eof_no_close", SkipMessages.ANNOTATION_SYNTAX_SKIP);
	}

	@Test
	public void testRule1MultilineNoAtSign() throws Exception {
		assertSkipResult(fixer, TOPIC, "rule1_multiline_no_at_sign", SkipMessages.ANNOTATION_SYNTAX_SKIP);
	}
}