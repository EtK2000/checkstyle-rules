package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferStaticImportFixerTest {
	private static final String TOPIC = "preferstaticimport";

	private final CheckstyleFixer fixer = new PreferStaticImportFixer();

	/**
	 * A column equal to the line's length is the one out-of-range value a {@code // target:} can
	 * express: the fragment guard rejects anything strictly past the end as a redundant target.
	 */
	@Test
	public void testColumnAtLineEndReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "column_at_line_end_returns_null");
	}

	/** The only fragment here whose buffer does not parse, which is a different null from the rest. */
	@Test
	public void testDotWithNoMethodIdentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "dot_with_no_method_ident");
	}

	/**
	 * The case name predates the AST migration: the column names the candidate inside an
	 * {@code import}, where it is the tail of a dotted name, and no {@code .} follows it only
	 * incidentally.
	 */
	@Test
	public void testNoDotAfterCandidateClassReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "no_dot_after_candidate_class");
	}

	/**
	 * A column landing inside the receiver rather than at its first character means a sibling fix
	 * rewrote this line earlier in the pass.
	 */
	@Test
	public void testReceiverColumnInsideIdentifierReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "receiver_column_inside_identifier");
	}
}