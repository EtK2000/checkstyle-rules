package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferLiteralSuffixFixerTest {
	private static final String TOPIC = "preferliteralsuffix";

	private final CheckstyleFixer fixer = new PreferLiteralSuffixFixer();

	@Test
	public void testColumnNotOpenParen() throws Exception {
		assertSkip(fixer, TOPIC, "column_not_open_paren");
	}

	@Test
	public void testForwardSubjectAtEol() throws Exception {
		assertSkipResult(fixer, TOPIC, "forward_subject_at_eol", "complex-cast-subject");
	}

	@Test
	public void testMalformedCastNoExpression() throws Exception {
		assertSkipResult(fixer, TOPIC, "malformed_cast_no_expression", "malformed-cast-no-expression");
	}
}