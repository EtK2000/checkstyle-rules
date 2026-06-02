package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class ExplicitInitializationFixerTest {
	private static final String TOPIC = "explicitinitialization";

	private final CheckstyleFixer fixer = new ExplicitInitializationFixer();

	@Test
	public void testDotOnlyValueSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "dot_only_value_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testEmptyValueReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "empty_value_returns_null");
	}

	@Test
	public void testHexPrefixOnlyValueSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "hex_prefix_only_value_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testNonZeroExponentNoDigitsSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "non_zero_exponent_no_digits_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testNonZeroExponentNonDigitSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "non_zero_exponent_nondigit_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testNonZeroExponentSignNoDigitsSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "non_zero_exponent_sign_no_digits_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testNoSemicolon() throws Exception {
		assertSkip(fixer, TOPIC, "no_semicolon");
	}

	@Test
	public void testUnterminatedBlockCommentInValue() throws Exception {
		assertSkip(fixer, TOPIC, "unterminated_block_comment_in_value");
	}
}