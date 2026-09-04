package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class PreferSpecificApiFixerTest {
	private static final String TOPIC = "preferspecificapi";

	private final CheckstyleFixer fixer = new PreferSpecificApiFixer();

	/**
	 * A bare statement beside an import is not a compilation unit, so the AST locator has nothing
	 * to resolve and the fixer refuses rather than guessing. The degenerate-whitespace parsing this
	 * case used to reach through {@code addAssertImport} is covered directly by
	 * {@code ImportLineTest}, whose parameters include the space-separated static form.
	 */
	@Test
	public void testAssertImportWhitespaceTolerant() throws Exception {
		assertSkipResult(fixer, TOPIC, "assert_import_whitespace_tolerant", "unrecognized API pattern");
	}

	/**
	 * An invalid escape is rejected by the <em>lexer</em>, so no parseable file can carry one and
	 * these two cases can only ever be fragments. The fixer refuses for want of a tree, which
	 * leaves the check's own invalid-escape arms ({@code decodedJavaStringLength}) reachable only
	 * from a buffer nothing can parse.
	 */
	@Test
	public void testIndexOfCharRefusesInvalidEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_escape", "unrecognized API pattern");
	}

	@Test
	public void testIndexOfCharRefusesInvalidUnicodeEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_unicode_escape", "unrecognized API pattern");
	}

	/**
	 * Unbalanced parens: the buffer does not parse, so there is no tree to locate a rule in. The
	 * old text fixer spliced one anyway and emitted {@code List.copyOf(Arrays.asList(list);},
	 * which does not compile either; refusing is the better answer.
	 */
	@Test
	public void testUnmodifiableAsListUnbalanced() throws Exception {
		assertSkipResult(fixer, TOPIC, "unmodifiable_as_list_unbalanced", "unrecognized API pattern");
	}
}