package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

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

	@Test
	public void testDotWithNoMethodIdentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "dot_with_no_method_ident");
	}

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

	/**
	 * Reachable from ordinary source: a qualified call split so the {@code .} opens the next line.
	 */
	@Test
	public void testReceiverRunsToEndOfLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "receiver_runs_to_end_of_line");
	}

	@Test
	public void testUnknownSimpleClassSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "unknown_simple_class", SkipMessages.PREFER_STATIC_IMPORT_SKIP);
	}
}