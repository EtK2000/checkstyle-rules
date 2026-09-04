package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

/**
 * The positions no located cast can be classified at. The first three buffers are bare statements
 * that do not parse as a compilation unit, so the AST prologue answers null before any of the shapes
 * their names describe is reached; they pin that one refusal rather than three.
 */
public class PreferLiteralSuffixFixerTest {
	private static final String TOPIC = "preferliteralsuffix";

	private final CheckstyleFixer fixer = new PreferLiteralSuffixFixer();

	@Test
	public void testColumnNotOpenParen() throws Exception {
		assertSkip(fixer, TOPIC, "column_not_open_paren");
	}

	@Test
	public void testForwardSubjectAtEol() throws Exception {
		assertSkip(fixer, TOPIC, "forward_subject_at_eol");
	}

	@Test
	public void testMalformedCastNoExpression() throws Exception {
		assertSkip(fixer, TOPIC, "malformed_cast_no_expression");
	}

	@Test
	public void testStaleColumnAfterSiblingFix() throws Exception {
		assertSkip(fixer, TOPIC, "stale_column_after_sibling_fix");
	}
}