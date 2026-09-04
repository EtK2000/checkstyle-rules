package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferSpecificApiFixerTest {
	private static final String TOPIC = "preferspecificapi";

	private final CheckstyleFixer fixer = new PreferSpecificApiFixer();

	/**
	 * A bare statement beside an import is not a compilation unit.
	 */
	@Test
	public void testAssertImportBesideBareStatementRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "assert_import_beside_bare_statement_refused", "cannot rewrite: unrecognized API pattern");
	}

	/**
	 * An invalid escape is rejected by the <em>lexer</em>, so no parseable file can carry one and
	 * these two cases can only ever be fragments.
	 */
	@Test
	public void testIndexOfCharRefusesInvalidEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_escape", "cannot rewrite: unrecognized API pattern");
	}

	@Test
	public void testIndexOfCharRefusesInvalidUnicodeEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_unicode_escape", "cannot rewrite: unrecognized API pattern");
	}

	/** Unbalanced parens: the buffer does not parse, so there is no tree to locate a rule in. */
	@Test
	public void testUnmodifiableAsListUnbalanced() throws Exception {
		assertSkipResult(fixer, TOPIC, "unmodifiable_as_list_unbalanced", "cannot rewrite: unrecognized API pattern");
	}
}