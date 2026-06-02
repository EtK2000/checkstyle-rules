package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.BaseCheckTest;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;

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

	@Nonnull
	private static AuditEvent createEventWithColumn(int line, int column, @Nonnull Class<?> sourceClass) {
		final var violation = new Violation(
				line, column, "", "", null, SeverityLevel.ERROR, null, sourceClass, "test"
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
	public void splitPreservingTrailingNewlineBom() {
		assertEquals(List.of("\uFEFFcode", ""), CheckstyleFixAction.splitPreservingTrailingNewline("\uFEFFcode\n"));
	}

	@Test
	public void splitPreservingTrailingNewlineCrlf() {
		assertEquals(List.of("code", ""), CheckstyleFixAction.splitPreservingTrailingNewline("code\r\n"));
	}

	@Test
	public void splitPreservingTrailingNewlineDouble() {
		final var content = "code\n\n";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("code", "", ""), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineEmpty() {
		assertEquals(List.of(), CheckstyleFixAction.splitPreservingTrailingNewline(""));
	}

	@Test
	public void splitPreservingTrailingNewlineLoneCr() {
		assertEquals(List.of("code", ""), CheckstyleFixAction.splitPreservingTrailingNewline("code\r"));
	}

	@Test
	public void splitPreservingTrailingNewlineMultiLineNoTrailing() {
		final var content = "a\nb\nc";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("a", "b", "c"), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineMultiLineTrailing() {
		final var content = "a\nb\nc\n";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("a", "b", "c", ""), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineNoTrailing() {
		final var content = "code";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("code"), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineOnlyNewline() {
		final var content = "\n";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("", ""), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineSingle() {
		final var content = "code\n";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("code", ""), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void splitPreservingTrailingNewlineTriple() {
		final var content = "code\n\n\n";
		final var lines = CheckstyleFixAction.splitPreservingTrailingNewline(content);
		assertEquals(List.of("code", "", "", ""), lines);
		assertEquals(content, String.join("\n", lines));
	}

	@Test
	public void testApplyFixesBoundsCheckAllowsStartLineEqualToSize() {
		final var event = createEventWithColumn(1, 0, Object.class);
		final var lines = new ArrayList<>(List.of("a"));
		final CheckstyleFixer appendFixer = (l, i, c) -> new FixResult(1, 0, List.of("appended"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", appendFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("a", "appended"), lines);
		assertFalse(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_BOUNDS));
	}

	@Test
	public void testApplyFixesBoundsCheckRejectsEndLineBeyondSize() {
		final var event = createEventWithColumn(1, 0, Object.class);
		final var lines = new ArrayList<>(List.of("a"));
		final CheckstyleFixer outOfBoundsFixer = (l, i, c) -> new FixResult(0, 100, List.of("x"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", outOfBoundsFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_BOUNDS));
	}

	@Test
	public void testApplyFixesBoundsCheckRejectsEventLineBeyondSize() {
		final var event = createEventWithColumn(5, 0, Object.class);
		final var lines = new ArrayList<>(List.of("a"));
		final CheckstyleFixer neverInvokedFixer = (l, i, c) -> {
			throw new AssertionError("fixer must not be invoked for an out-of-range line index");
		};
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", neverInvokedFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_BOUNDS));
	}

	@Test
	public void testApplyFixesBoundsCheckRejectsNegativeEventLine() {
		final var deletingEvent = createEventWithColumn(4, 0, Object.class);
		final var negativeEvent = createEventWithColumn(0, 0, String.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c", "d"));
		final CheckstyleFixer deletingFixer = (l, i, c) -> new FixResult(1, 2, List.of());
		final CheckstyleFixer neverInvokedFixer = (l, i, c) -> {
			throw new AssertionError("fixer must not be invoked for a negative line index");
		};
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(deletingEvent, negativeEvent)),
				Map.of("java.lang.Object", deletingFixer, "java.lang.String", neverInvokedFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("String", List.of()).contains(SkipMessages.FIX_BOUNDS));
		assertEquals(List.of("a", "d"), lines);
	}

	@Test
	public void testApplyFixesBoundsCheckRejectsNegativeStartLine() {
		final var event = createEventWithColumn(1, 0, Object.class);
		final var lines = new ArrayList<>(List.of("a"));
		final CheckstyleFixer outOfBoundsFixer = (l, i, c) -> new FixResult(-1, -1, List.of("x"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", outOfBoundsFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_BOUNDS));
	}

	@Test
	public void testApplyFixesBoundsCheckRejectsStartLineBeyondSize() {
		final var event = createEventWithColumn(1, 0, Object.class);
		final var lines = new ArrayList<>(List.of("a"));
		final CheckstyleFixer outOfBoundsFixer = (l, i, c) -> new FixResult(2, 0, List.of("x"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", outOfBoundsFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_BOUNDS));
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
	public void testApplyFixesContinuesAfterFixerThrows() {
		final var throwingEvent = createEventWithColumn(2, 0, Object.class);
		final var fixableEvent = createEventWithColumn(1, 0, String.class);
		final var lines = new ArrayList<>(List.of("a", "b"));
		final CheckstyleFixer throwingFixer = (l, i, c) -> {
			throw new IllegalStateException("boom");
		};
		final CheckstyleFixer replacingFixer = (l, i, c) -> new FixResult(0, 0, List.of("fixed"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(throwingEvent, fixableEvent)),
				Map.of("java.lang.Object", throwingFixer, "java.lang.String", replacingFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("fixed", "b"), lines);
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_ERROR));
	}

	@Test
	public void testApplyFixesContinuesAfterFixerThrowsError() {
		final var throwingEvent = createEventWithColumn(2, 0, Object.class);
		final var fixableEvent = createEventWithColumn(1, 0, String.class);
		final var lines = new ArrayList<>(List.of("a", "b"));
		final CheckstyleFixer throwingFixer = (l, i, c) -> {
			throw new StackOverflowError();
		};
		final CheckstyleFixer replacingFixer = (l, i, c) -> new FixResult(0, 0, List.of("fixed"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(throwingEvent, fixableEvent)),
				Map.of("java.lang.Object", throwingFixer, "java.lang.String", replacingFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("fixed", "b"), lines);
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_ERROR));
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
	public void testRoundTripThroughCheckstyleTabExpansion() throws Exception {
		final var source = "class Test {\n\t\tlong x = 0l;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(UpperEllCheck.class, source);
		assertEquals(1, violations.size());
		final var event = violations.getFirst();
		final var line = source.split("\n", -1)[event.getLine() - 1];
		assertEquals(
				line.indexOf("0l"),
				CheckstyleFixAction.tabColumnToCharIndex(line, event.getColumn() - 1)
		);
	}

	@Test
	public void testSuppressedLineDoesNotFireOnExpansion() {
		final var event1 = createEventWithColumn(2, 10, Object.class);
		final var event2 = createEventWithColumn(2, 5, Number.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c"));
		final CheckstyleFixer expandFixer = (l, i, c) -> new FixResult(1, 1, List.of("x", "y", "z"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", expandFixer, "java.lang.Number", nullFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Number", List.of()).contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testSuppressedLineDoesNotFireOnInsertOnly() {
		final var event1 = createEventWithColumn(2, 10, Object.class);
		final var event2 = createEventWithColumn(2, 5, Number.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c"));
		final CheckstyleFixer insertFixer = (l, i, c) -> new FixResult(1, 0, List.of("inserted"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", insertFixer, "java.lang.Number", nullFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Number", List.of()).contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testSuppressedLineDoesNotFireOnSingleLineInPlaceRewrite() {
		final var event1 = createEventWithColumn(2, 10, Object.class);
		final var event2 = createEventWithColumn(2, 5, Number.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c"));
		final CheckstyleFixer rewriteFixer = (l, i, c) -> new FixResult(1, 1, List.of("replaced"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", rewriteFixer, "java.lang.Number", nullFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Number", List.of()).contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testSuppressedLineFiresOnPureDeleteSameLine() {
		final var event1 = createEventWithColumn(3, 10, Object.class);
		final var event2 = createEventWithColumn(3, 5, Number.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c", "d"));
		final CheckstyleFixer deleteFixer = (l, i, c) -> new FixResult(2, 2, List.of());
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", deleteFixer, "java.lang.Number", nullFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Number", List.of()).contains(SkipMessages.FIX_SUPPRESSED));
	}

	@Test
	public void testSuppressedLineFiresWhenLineIndexPastReplacementRange() {
		final var event1 = createEventWithColumn(5, 10, Object.class);
		final var event2 = createEventWithColumn(5, 5, Number.class);
		final var lines = new ArrayList<>(List.of("a", "b", "c", "d", "e"));
		final CheckstyleFixer shrinkFixer = (l, i, c) -> new FixResult(1, 4, List.of("x", "y"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", shrinkFixer, "java.lang.Number", nullFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Number", List.of()).contains(SkipMessages.FIX_SUPPRESSED));
	}

	@Test
	public void testTabColumnConversion() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("hello", 0));
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 5));
		assertEquals(1, CheckstyleFixAction.tabColumnToCharIndex("\thello", 4));
		assertEquals(2, CheckstyleFixAction.tabColumnToCharIndex("\t\thello", 8));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("\thello world", 9));
	}

	@Test
	public void testTabColumnConversionBeyondLine() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 10));
	}

	@Test
	public void testTabColumnConversionMidLineTab() {
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 4));
		assertEquals(4, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 5));
	}

	@Test
	public void testTabColumnConversionNoTabs() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 0));
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 3));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 6));
	}

	@Test
	public void testTabColumnConversionTabAtStartColZero() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("\thello", 0));
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("\t", 0));
	}

	@Test
	public void testTabColumnConversionTabOnAlignmentBoundary() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("abcd\tef", 8));
		assertEquals(4, CheckstyleFixAction.tabColumnToCharIndex("abcd\tef", 4));
	}
}