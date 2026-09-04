package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class UpperEllFixerTest {
	private static final String TOPIC = "upperell";

	private final CheckstyleFixer fixer = new UpperEllFixer();

	@Test
	public void testDigitInsideIdentifierIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "digit_inside_identifier_is_stale", SkipMessages.UPPER_ELL_SKIP_STALE);
	}

	@Test
	public void testIdentifierEndingInLIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "identifier_ending_in_l_is_stale", SkipMessages.UPPER_ELL_SKIP_STALE);
	}

	@Test
	public void testLiteralAtLineStartIsFixed() {
		assertEquals(new FixResult(0, 0, List.of("3000000000L;")), fixer.fix(List.of("3000000000l;"), 0, 0));
	}

	@Test
	public void testNoLiteralCharsAtColumn() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_literal_chars_at_column", SkipMessages.UPPER_ELL_SKIP_STALE);
	}

	@Test
	public void testSupplementaryIdentifierEndingInDigitLIsStale() {
		final var line = "long " + Character.toString(0x1D400) + "1l = 3000000000l;";
		assertEquals(new SkipResult(SkipMessages.UPPER_ELL_SKIP_STALE), fixer.fix(List.of(line), 0, 7));
	}

	@Test
	public void testUnsuffixedLiteralAtColumnIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "unsuffixed_literal_at_column_is_stale", SkipMessages.UPPER_ELL_SKIP_STALE);
	}

	@Test
	public void testUppercaseSuffixAtColumnIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "uppercase_suffix_at_column_is_stale", SkipMessages.UPPER_ELL_SKIP_STALE);
	}
}