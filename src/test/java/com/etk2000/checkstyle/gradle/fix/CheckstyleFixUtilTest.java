package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckstyleFixUtilTest {
	@Nonnull
	private static AuditEvent createEvent(
			int line,
			@Nonnull SeverityLevel severity,
			@Nullable String moduleId,
			@Nonnull Class<?> sourceClass
	) {
		final var violation = new Violation(
				line, 0, "", "", null, severity, moduleId, sourceClass, "test"
		);
		return new AuditEvent(new Object(), "Test.java", violation);
	}

	@Test
	public void computeHintClampsStaleTotalUpToFixable() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 5 violations.",
				CheckstyleFixAction.computeHint(5, 3, "checkstyleFix")
		);
	}

	@Test
	public void computeHintEqualTotalNoClamp() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 4 violations.",
				CheckstyleFixAction.computeHint(4, 4, "checkstyleFix")
		);
	}

	@Test
	public void computeHintLargerTotalNoClamp() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix 2 of 7 violations.",
				CheckstyleFixAction.computeHint(2, 7, "checkstyleFix")
		);
	}

	@Test
	public void computeHintNegativeDryRunTotalClamps() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 3 violations.",
				CheckstyleFixAction.computeHint(3, -1, "checkstyleFix")
		);
	}

	@Test
	public void computeHintNegativeFixableReturnsNull() {
		assertNull(CheckstyleFixAction.computeHint(-1, 5, "checkstyleFix"));
	}

	@Test
	public void computeHintSingleFixable() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 1 violations.",
				CheckstyleFixAction.computeHint(1, 0, "checkstyleFix")
		);
	}

	@Test
	public void computeHintZeroFixableReturnsNull() {
		assertNull(CheckstyleFixAction.computeHint(0, 5, "checkstyleFix"));
	}

	@Test
	public void formatHintMessageAllFixable() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 5 violations.",
				CheckstyleFixAction.formatHintMessage(5, 5, "checkstyleFix")
		);
	}

	@Test
	public void formatHintMessageNegativeFixable() {
		assertNull(CheckstyleFixAction.formatHintMessage(-1, 5, "checkstyleFix"));
	}

	@Test
	public void formatHintMessagePartiallyFixable() {
		assertEquals(
				"Run ./gradlew checkstyleFixAll to auto-fix 3 of 10 violations.",
				CheckstyleFixAction.formatHintMessage(3, 10, "checkstyleFixAll")
		);
	}

	@Test
	public void formatHintMessageSingleFixable() {
		assertEquals(
				"Run ./gradlew checkstyleFix to auto-fix all 1 violations.",
				CheckstyleFixAction.formatHintMessage(1, 1, "checkstyleFix")
		);
	}

	@Test
	public void formatHintMessageZeroFixable() {
		assertNull(CheckstyleFixAction.formatHintMessage(0, 5, "checkstyleFix"));
	}

	@Test
	public void formatHintMessageZeroTotal() {
		assertNull(CheckstyleFixAction.formatHintMessage(0, 0, "checkstyleFix"));
	}

	@Test
	public void testApplyFixesClearsFilePathEvenWhenFixerThrows() {
		FixContext.clearFilePath();
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("content"));
		final CheckstyleFixer throwingFixer = (l, i, c) -> {
			throw new RuntimeException("boom");
		};
		try {
			CheckstyleFixAction.applyFixes(
					lines, new ArrayList<>(List.of(event)), Map.of("java.lang.Object", throwingFixer), Map.of()
			);
		}
		catch (RuntimeException ignored) {
		}
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testApplyFixesEmptyViolationsLeavesFilePathUnchanged() {
		FixContext.clearFilePath();
		final var lines = new ArrayList<>(List.of("content"));
		CheckstyleFixAction.applyFixes(lines, new ArrayList<>(), Map.of(), Map.of());
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testApplyFixesFilePathStaysSetAcrossMultipleViolations() {
		FixContext.clearFilePath();
		final var event1 = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var event2 = createEvent(2, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("a", "b"));
		final var seen = new ArrayList<String>();
		final CheckstyleFixer recordingFixer = (l, i, c) -> {
			seen.add(FixContext.getFilePath());
			return null;
		};
		CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event1, event2)), Map.of("java.lang.Object", recordingFixer), Map.of()
		);
		assertEquals(List.of("Test.java", "Test.java"), seen);
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testApplyFixesLeavesFilePathUnsetWhenFirstFileNameIsNull() {
		FixContext.clearFilePath();
		final var violation = new Violation(
				1, 0, "", "", null, SeverityLevel.ERROR, null, Object.class, "test"
		);
		final var event = new AuditEvent(new Object(), null, violation);
		final var lines = new ArrayList<>(List.of("content"));
		final String[] recorded = {"sentinel"};
		final CheckstyleFixer recordingFixer = (l, i, c) -> {
			recorded[0] = FixContext.getFilePath();
			return null;
		};
		CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of("java.lang.Object", recordingFixer), Map.of()
		);
		assertNull(recorded[0]);
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testApplyFixesSetsFilePathDuringFixerCallAndClearsAfter() {
		FixContext.clearFilePath();
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("content"));
		final String[] recorded = {null};
		final CheckstyleFixer recordingFixer = (l, i, c) -> {
			recorded[0] = FixContext.getFilePath();
			return null;
		};
		CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of("java.lang.Object", recordingFixer), Map.of()
		);
		assertEquals("Test.java", recorded[0]);
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testExtractCheckShortNameModuleIdPreferred() {
		final var event = createEvent(1, SeverityLevel.ERROR, "NoDoubleBlankLines", Object.class);
		final var lines = new ArrayList<>(List.of("line1"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of(), Map.of("NoDoubleBlankLines", nullFixer)
		);
		assertTrue(result.skippedReasons().containsKey("NoDoubleBlankLines"));
		assertTrue(result.skippedReasons().get("NoDoubleBlankLines").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testExtractCheckShortNameSourceNameFallback() {
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("line1"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", nullFixer),
				Map.of()
		);
		assertTrue(result.skippedReasons().containsKey("Object"));
		assertTrue(result.skippedReasons().get("Object").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testMultipleSkipReasonsPerCheckAccumulated() {
		final var event1 = createEvent(1, SeverityLevel.WARNING, null, Object.class);
		final var event2 = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("only line"));
		final CheckstyleFixer dummyFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", dummyFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().containsKey("Object"));
		final var reasons = result.skippedReasons().get("Object");
		assertEquals(2, reasons.size());
		assertTrue(reasons.contains(SkipMessages.FIX_SEVERITY));
		assertTrue(reasons.contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testNullFixerReturnTracksNotFixable() {
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("content"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of("java.lang.Object", nullFixer), Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().containsKey("Object"));
		assertTrue(result.skippedReasons().get("Object").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testTabColumnConversion() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("hello", 0));
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 5));
		assertEquals(1, CheckstyleFixAction.tabColumnToCharIndex("\thello", 8));
		assertEquals(2, CheckstyleFixAction.tabColumnToCharIndex("\t\thello", 16));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("\thello world", 13));
	}

	@Test
	public void testTabColumnConversionBeyondLine() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 10));
	}

	@Test
	public void testTabColumnConversionMidLineTab() {
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 8));
		assertEquals(4, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 9));
	}

	@Test
	public void testTabColumnConversionNoTabs() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 0));
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 3));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 6));
	}
}