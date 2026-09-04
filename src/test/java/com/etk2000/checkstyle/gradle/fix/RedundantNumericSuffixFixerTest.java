package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class RedundantNumericSuffixFixerTest {
	private static final String TOPIC = "redundantnumericsuffix";

	private final CheckstyleFixer fixer = new RedundantNumericSuffixFixer();

	@Test
	public void testColumnAtNonLiteralChar() throws Exception {
		assertSkip(fixer, TOPIC, "column_at_non_literal_char");
	}

	/** In a hex literal {@code E} is a digit, so the {@code +} after {@code 0x1E} is an operator, not an exponent sign. */
	@Test
	public void testHexDigitEBeforeSign() throws Exception {
		assertSkip(fixer, TOPIC, "hex_digit_e_before_sign");
	}

	@Test
	public void testSignAfterNonExponentDigit() throws Exception {
		assertSkip(fixer, TOPIC, "sign_after_non_exponent_digit");
	}
}