package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * The only inputs this fixer still needs as fragments: buffers that do not parse as a compilation
 * unit. An AST-driven fixer declines those with no reason to name, and that is the one path a slice
 * cannot drive, because the check never reports inside a file that does not parse.
 */
public class PreferExactAssertionFixerTest {
	private static final String TOPIC = "preferexactassertion";

	private final CheckstyleFixer fixer = new PreferExactAssertionFixer();

	@Test
	public void lineIndexPastEndReturnsNull() {
		final var lines = List.of("x");
		assertNull(fixer.fix(lines, lines.size(), 0));
		assertNull(fixer.fix(lines, 7, 0));
		assertNull(fixer.fix(lines, -1, 0));
	}

	@Test
	public void methodAtFileStartWithLeadingDotUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "method_at_file_start_with_leading_dot_unparseable");
	}

	@Test
	public void multiLineCallNoCloseParenUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_call_no_close_paren_unparseable");
	}

	@Test
	public void multiLineCallNoSemicolonUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_call_no_semicolon_unparseable");
	}

	@Test
	public void negationEmptyInnerUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "negation_empty_inner_unparseable");
	}

	@Test
	public void negationNotEqualMalformedUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "negation_not_equal_malformed_unparseable");
	}

	@Test
	public void unicodeEscapeInStringArgUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "unicode_escape_in_string_arg_unparseable");
	}

	@Test
	public void unicodeEscapeOutsideLiteralUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "unicode_escape_outside_literal_unparseable");
	}

	@Test
	public void walkbackLineWithUnicodeEscapeUnparseable() throws Exception {
		assertSkip(fixer, TOPIC, "walkback_line_with_unicode_escape_unparseable");
	}
}