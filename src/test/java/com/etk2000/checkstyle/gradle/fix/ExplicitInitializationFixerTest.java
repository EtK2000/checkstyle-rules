package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.BaseCheckTest;
import com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import javax.annotation.Nonnull;

public class ExplicitInitializationFixerTest {
	private static final String TOPIC = "explicitinitialization";

	/** Sets the violation the check reports on {@code declaration} as the one being fixed. */
	private static void reportOn(@Nonnull String declaration) throws Exception {
		final var events = BaseCheckTest.runCheckInline(ExplicitInitializationCheck.class, "class T {\n" + declaration + "\n}");
		assertEquals(1, events.size(), "guard: the check reports the declaration once");
		FixContext.setViolation(events.getFirst().getViolation());
	}

	private final CheckstyleFixer fixer = new ExplicitInitializationFixer();

	@AfterEach
	public void clearViolation() {
		FixContext.clearViolation();
	}

	@Test
	public void testCharColumnPastSupplementaryCharacters() {
		final var wide = new String(Character.toChars(0x1D465)).repeat(3);
		final var line = "\t/* " + wide + " */ Integer a=1,b = null;";
		assertEquals(new FixResult(0, 0, List.of("\t/* " + wide + " */ Integer a=1,b;")), fixer.fix(List.of(line), 0, line.indexOf("b =")));
	}

	@Test
	public void testCommaAfterSemicolonEndsAtSemicolon() {
		assertEquals(new FixResult(0, 0, List.of("\tint x; int y, z;")), fixer.fix(List.of("\tint x = 0; int y, z;"), 0, 5));
	}

	@Test
	public void testDotOnlyValueSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "dot_only_value_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testEmptyValueReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "empty_value_returns_null");
	}

	@Test
	public void testEqualsBeforeColumnReturnsNull() {
		assertNull(fixer.fix(List.of("\tint x = 0;"), 0, 8));
	}

	@Test
	public void testHexPrefixOnlyValueSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "hex_prefix_only_value_skipped", SkipMessages.EXPLICIT_INIT_SKIP);
	}

	@Test
	public void testNameExtendingReportedNameSkipsAsStale() throws Exception {
		reportOn("\tInteger a = 1, b = null;");
		final var line = "\tInteger a = 1, bb = null;";
		assertEquals(new SkipResult(SkipMessages.EXPLICIT_INIT_SKIP_STALE), fixer.fix(List.of(line), 0, line.indexOf("bb")));
	}

	@Test
	public void testNamePrefixOfReportedNameSkipsAsStale() throws Exception {
		reportOn("\tInteger a = 1, bb = null;");
		final var line = "\tInteger a = 1, b = null;";
		assertEquals(new SkipResult(SkipMessages.EXPLICIT_INIT_SKIP_STALE), fixer.fix(List.of(line), 0, line.indexOf("b =")));
	}

	@Test
	public void testNoNameAtColumnSkipsAsStale() throws Exception {
		reportOn("\tInteger a = 1, b = null;");
		final var line = "\tInteger a = 1, b = null;";
		assertEquals(new SkipResult(SkipMessages.EXPLICIT_INIT_SKIP_STALE), fixer.fix(List.of(line), 0, line.indexOf(" b")));
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
	public void testOtherNameAtColumnSkipsAsStale() throws Exception {
		reportOn("\tInteger timeout = null;");
		final var line = "\tInteger retries = 0, timeout = null;";
		assertEquals(new SkipResult(SkipMessages.EXPLICIT_INIT_SKIP_STALE), fixer.fix(List.of(line), 0, line.indexOf("retries")));
	}

	@Test
	public void testReportedNameAtColumnFixes() throws Exception {
		reportOn("\tInteger a = 1, b = null;");
		final var line = "\tInteger a = 1, b = null;";
		assertEquals(new FixResult(0, 0, List.of("\tInteger a = 1, b;")), fixer.fix(List.of(line), 0, line.indexOf("b =")));
	}

	@Test
	public void testReportedSupplementaryNameAtCharColumnFixes() throws Exception {
		final var name = new String(Character.toChars(0x1D465)).repeat(2);
		reportOn("\tInteger " + name + " = null;");
		final var line = "\t/* " + name + " */ Integer a = 1, " + name + " = null;";
		assertEquals(new FixResult(0, 0, List.of("\t/* " + name + " */ Integer a = 1, " + name + ";")), fixer.fix(List.of(line), 0, line.lastIndexOf(name)));
	}

	@Test
	public void testUnterminatedBlockCommentInValue() throws Exception {
		assertSkip(fixer, TOPIC, "unterminated_block_comment_in_value");
	}
}