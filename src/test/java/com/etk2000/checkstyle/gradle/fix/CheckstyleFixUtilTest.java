package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.PreferVarCheck;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
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

	@CheckReturnValue
	@Nonnull
	private static Map<String, String> regexpMessagesById(@Nonnull Configuration config) throws CheckstyleException {
		final var messages = new HashMap<String, String>();
		for (var child : config.getChildren()) {
			if (child.getName().startsWith("Regexp"))
				messages.put(child.getProperty("id"), child.getProperty("message"));
		}
		return messages;
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
	public void testApplyFixesSetsEachEventsViolationDuringItsFixerCall() {
		final var seen = new ArrayList<String>();
		CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b")),
				new ArrayList<>(List.of(
						new AuditEvent(new Object(), "Test.java", new Violation(1, 0, "", "", new Object[0], SeverityLevel.ERROR, null, Object.class, "first")),
						new AuditEvent(new Object(), "Test.java", new Violation(2, 0, "", "", new Object[0], SeverityLevel.ERROR, null, Object.class, "second"))
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							seen.add(FixContext.getViolationMessage());
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of("second", "first"), seen);
		assertNull(FixContext.getViolationMessage());
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
	public void testApplyFixesSetsTheBufferVersionPerCallAndClearsIt() {
		final var versions = new ArrayList<Integer>();
		CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b")),
				new ArrayList<>(List.of(createEvent(1, SeverityLevel.ERROR, null, Object.class), createEvent(2, SeverityLevel.ERROR, null, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							versions.add(FixContext.getBufferVersion());
							return new FixResult(i, i, List.of("x"));
						}
				),
				Map.of()
		);
		assertEquals(List.of(0, 1), versions);
		assertNull(FixContext.getBufferVersion());
	}

	@Test
	public void testCreateCheckerConfigReadsFilesAsUtf8() throws Exception {
		assertEquals(StandardCharsets.UTF_8.name(), CheckstyleFixAction.createCheckerConfig("21").getProperty("charset"));
	}

	@Test
	public void testCreateCheckerConfigRegexpMessagesMatchTheShippedConfig() throws Exception {
		final var shippedUrl = CheckstyleFixAction.class.getResource("/com/etk2000/checkstyle/checkstyle.xml");
		assertNotNull(shippedUrl);
		final var shipped = regexpMessagesById(ConfigurationLoader.loadConfiguration(
				new InputSource(shippedUrl.toExternalForm()),
				name -> "",
				ConfigurationLoader.IgnoredModulesOptions.EXECUTE
		));
		final var fixAction = regexpMessagesById(CheckstyleFixAction.createCheckerConfig("21"));
		assertEquals(
				Set.of(
						"BlankLineAfterBreak",
						"NoBlankLineAfterClassBrace",
						"NoBlankLineBeforeClosingBrace",
						"NoDoubleBlankLines",
						"NoTrailingNewline",
						"NoTrailingWhitespace"
				),
				fixAction.keySet()
		);

		final var expected = new HashMap<>(shipped);
		expected.keySet().retainAll(fixAction.keySet());
		assertEquals(expected, fixAction);
	}

	@Test
	public void testCreateCheckerConfigSetsTheCheckersTabWidth() throws Exception {
		assertEquals(String.valueOf(LineLength.TAB_WIDTH), CheckstyleFixAction.createCheckerConfig("21").getProperty("tabWidth"));
	}

	@Test
	public void testExpansionStalesItsLineForTheSameCheckToo() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "bbbbbbbbbb", "c")),
				new ArrayList<>(List.of(createEventWithColumn(2, 10, Object.class), createEventWithColumn(2, 5, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return new FixResult(1, 1, List.of("bbbbbbbbbx", "y", "z"));
						}
				),
				Map.of()
		);
		assertEquals(List.of(9), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertTrue(result.needsAnotherPass());
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
	public void testFixerReceivesCharIndexZeroForColumnZero() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("\tint a;")),
				new ArrayList<>(List.of(createEventWithColumn(1, 0, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(0), calls);
		assertEquals(0, result.fixCount());
	}

	@Test
	public void testFixerReceivesTheCharIndexPastASupplementaryCharacterAheadOfATab() throws Exception {
		final var line = "\tString s = \"" + new String(Character.toChars(0x1D465)) + "\";\tlong x = 0l;";
		final var violations = BaseCheckTest.runCheckInline(UpperEllCheck.class, "class T {\n" + line + "\n}");
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("class T {", line, "}")),
				new ArrayList<>(violations),
				Map.of(
						UpperEllCheck.class.getName(),
						(l, i, c) -> {
							calls.add(c);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(line.indexOf("0l")), calls);
		assertEquals(0, result.fixCount());
	}

	@Test
	public void testFixerReceivesTheCharIndexPastTabsAndSupplementaryCharacters() throws Exception {
		final var line = "\t\tString s = \"" + new String(Character.toChars(0x1D465)).repeat(3) + "\"; long x = 0l;";
		final var violations = BaseCheckTest.runCheckInline(UpperEllCheck.class, "class T {\n" + line + "\n}");
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("class T {", line, "}")),
				new ArrayList<>(violations),
				Map.of(
						UpperEllCheck.class.getName(),
						(l, i, c) -> {
							calls.add(c);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(line.indexOf("0l")), calls);
		assertEquals(0, result.fixCount());
	}

	@Test
	public void testInsertOnlyFixDefersLaterEventOnItsLine() {
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
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testIntactTextBeforeColumnDoesNotDeferUnusedImportsDelete() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "bbbbbbbbbb"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(2, 10, Object.class),
						createEventWithColumn(2, 5, Object.class),
						createEventWithColumn(1, 1, UnusedImportsCheck.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> c == 9 ? new FixResult(1, 1, List.of("bbbbbbbbbx")) : null,
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer()
				),
				Map.of()
		);
		assertEquals(2, result.fixCount());
		assertEquals(List.of("bbbbbbbbbx"), lines);
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testJoinWithLineBelowStalesItsLineForTheSameCheck() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "bbbbbbbbbb", "c")),
				new ArrayList<>(List.of(createEventWithColumn(2, 10, Object.class), createEventWithColumn(2, 5, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return new FixResult(1, 2, List.of("bbbbbbbbbb c"));
						}
				),
				Map.of()
		);
		assertEquals(List.of(9), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertTrue(result.needsAnotherPass());
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
	public void testParseBreakBothUnparseable() {
		assertNull(CheckstyleFixAction.parseBreak(List.of("class T {"), List.of("class T {", "int a")));
	}

	@Test
	public void testParseBreakByteOrderMarkLeftOut() {
		final var bom = String.valueOf((char) 0xFEFF);
		assertNull(CheckstyleFixAction.parseBreak(List.of(bom + "class T {", "int a;", "}"), List.of(bom + "class T {", "int a, b;", "}")));
		assertNotNull(CheckstyleFixAction.parseBreak(List.of(bom + "class T {", "int a;", "}"), List.of(bom + "class T {", "int a", "}")));
	}

	@Test
	public void testParseBreakFixedFileStillParses() {
		assertNull(CheckstyleFixAction.parseBreak(List.of("class T {", "int a;", "int b;", "}"), List.of("class T {", "int a, b;", "}")));
	}

	@Test
	public void testParseBreakNamesTheFirstError() {
		final var error = CheckstyleFixAction.parseBreak(List.of("class T {", "int a;", "}"), List.of("class T {", "int a", "}"));
		assertNotNull(error);
		assertTrue(error.startsWith("3:"), error);
	}

	@Test
	public void testParseBreakUnparseableInputLeftToTheFixers() {
		assertNull(CheckstyleFixAction.parseBreak(List.of("class T {", "int a"), List.of("class T {", "int a;", "}")));
	}

	@Test
	public void testPreferVarMustOnPinnedPrimitiveFixes() throws Exception {
		final var events = BaseCheckTest.runCheckInline(PreferVarCheck.class, "class T {\n\tvoid f(int x) {\n\t\tfinal long y = (long) x;\n\t}\n}");
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel(), "guard: the cast pins the type, so 'var' is a must");
		final var lines = new ArrayList<>(List.of("class T {", "\tvoid f(int x) {", "\t\tfinal long y = (long) x;", "\t}", "}"));
		final var result = CheckstyleFixAction.applyFixes(lines, new ArrayList<>(events), CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("class T {", "\tvoid f(int x) {", "\t\tfinal var y = (long) x;", "\t}", "}"), lines);
	}

	@Test
	public void testPreferVarStaleMustOnLiteralNoLongerPinningSkips() throws Exception {
		final var events = BaseCheckTest.runCheckInline(PreferVarCheck.class, "class T {\n\tvoid f() {\n\t\tfinal long y = 5L;\n\t}\n}");
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel(), "guard: the suffix pins the type, so 'var' is a must");
		final var lines = new ArrayList<>(List.of("class T {", "\tvoid f() {", "\t\tfinal long y = 5;", "\t}", "}"));
		final var result = CheckstyleFixAction.applyFixes(lines, new ArrayList<>(events), CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(0, result.fixCount());
		assertEquals(Map.of("PreferVarCheck", List.of(SkipMessages.PREFER_VAR_SKIP_TYPE_UNPINNED)), result.skippedReasons());
		assertEquals(List.of("class T {", "\tvoid f() {", "\t\tfinal long y = 5;", "\t}", "}"), lines);
	}

	@Test
	public void testPreferVarStaleMustOnUnpinnedPrimitiveSkips() throws Exception {
		final var events = BaseCheckTest.runCheckInline(PreferVarCheck.class, "class T {\n\tvoid f(int x) {\n\t\tfinal long y = (long) x;\n\t}\n}");
		final var lines = new ArrayList<>(List.of("class T {", "\tvoid f(int x) {", "\t\tfinal long y = x;", "\t}", "}"));
		final var result = CheckstyleFixAction.applyFixes(lines, new ArrayList<>(events), CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(0, result.fixCount());
		assertEquals(Map.of("PreferVarCheck", List.of(SkipMessages.PREFER_VAR_SKIP_TYPE_UNPINNED)), result.skippedReasons());
		assertEquals(List.of("class T {", "\tvoid f(int x) {", "\t\tfinal long y = x;", "\t}", "}"), lines);
	}

	@Test
	public void testRewrittenSpanChecksEverySpanStartingAbove() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "dddddddddd", "e")),
				new ArrayList<>(List.of(
						createEventWithColumn(5, 1, Number.class),
						createEventWithColumn(4, 10, Object.class),
						createEventWithColumn(3, 1, Integer.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(0, 3, List.of("w", "x", "y", "dddddddddz")),
						"java.lang.Number",
						(l, i, c) -> new FixResult(1, 1, List.of("v")),
						"java.lang.Integer",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(), calls);
		assertEquals(2, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Integer"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanDeferralLeavesOtherFixersAboveRunning() {
		final var lines = new ArrayList<>(List.of("top", "a", "b", "c"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(4, 1, Object.class),
						createEventWithColumn(3, 1, Number.class),
						createEventWithColumn(1, 1, Integer.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						"java.lang.Number", (l, i, c) -> null,
						"java.lang.Integer", (l, i, c) -> new FixResult(0, 0, List.of("TOP"))
				),
				Map.of()
		);
		assertEquals(2, result.fixCount());
		assertEquals(List.of("TOP", "x", "y", "z"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanDeferralSurvivesAlreadyPresentImport() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "a", "b", "c"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(3, 1, Number.class))),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 3, List.of("x", "y", "List"), Set.of("java.util.List")),
						"java.lang.Number", (l, i, c) -> null
				),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("import java.util.List;", "x", "y", "List"), lines);
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanDefersUnusedImportsDelete() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "a", "b", "c"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(4, 1, Object.class),
						createEventWithColumn(3, 1, Number.class),
						createEventWithColumn(1, 1, UnusedImportsCheck.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						"java.lang.Number", (l, i, c) -> null,
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer()
				),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertEquals(List.of("import java.util.List;", "x", "y", "z"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("UnusedImportsCheck"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanEndsAtTheFixEndLine() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "d")),
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(3, 1, Number.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(0, 0, List.of("top")),
						"java.lang.Number",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(2), calls);
		assertEquals(List.of(SkipMessages.FIX_NOT_FIXABLE), result.skippedReasons().get("Number"));
		assertFalse(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testRewrittenSpanKeepsTheWiderOfTwoSpansFromOneLine() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "d", "e", "ffffffffff")),
				new ArrayList<>(List.of(
						createEventWithColumn(6, 10, Object.class),
						createEventWithColumn(5, 1, Number.class),
						createEventWithColumn(3, 1, Integer.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(1, 2, List.of("x", "y")),
						"java.lang.Number",
						(l, i, c) -> new FixResult(1, 1, List.of("w")),
						"java.lang.Integer",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(), calls);
		assertEquals(2, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Integer"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanLeavesLineAboveOpen() {
		final var lines = new ArrayList<>(List.of("a", "b", "c", "d"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(1, 1, Number.class))),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						"java.lang.Number", (l, i, c) -> new FixResult(0, 0, List.of("top"))
				),
				Map.of()
		);
		assertEquals(2, result.fixCount());
		assertEquals(List.of("top", "x", "y", "z"), lines);
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanStalesEventsOwnLineEvenWithItsTextIntact() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "dddddddddd")),
				new ArrayList<>(List.of(createEventWithColumn(4, 10, Object.class), createEventWithColumn(4, 5, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return new FixResult(1, 3, List.of("x", "y", "dddddddddx"));
						}
				),
				Map.of()
		);
		assertEquals(List.of(9), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanSuppressesEventInside() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "d")),
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(3, 1, Number.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						"java.lang.Number",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanSuppressesEventOnItsFirstLine() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "d")),
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(2, 1, Number.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						"java.lang.Number",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testRewrittenSpanSuppressesEventsOnOwnLineWhoseTextChanged() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "b", "c", "dddddddddd")),
				new ArrayList<>(List.of(createEventWithColumn(4, 10, Object.class), createEventWithColumn(4, 5, Number.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(1, 3, List.of("x", "y", "ddddx")),
						"java.lang.Number",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(), calls);
		assertEquals(1, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
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
				CheckstyleFixAction.tabColumnToCodePointColumn(line, event.getColumn() - 1)
		);
	}

	@Test
	public void testSameLineEventAheadOfChangeAfterSupplementaryCharacterRuns() {
		final var wide = new String(Character.toChars(0x1D465));
		final var line = "String s = \"" + wide + "\"; int b = 0;";
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of(line)),
				new ArrayList<>(List.of(createEventWithColumn(1, 21, Object.class), createEventWithColumn(1, 19, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 21 ? new FixResult(0, 0, List.of("String s = \"" + wide + "\"; intb = 0;")) : null;
						}
				),
				Map.of()
		);
		// the fixer receives char indexes, one past the code-point columns once past the wide char
		assertEquals(List.of(21, 19), calls);
		assertEquals(1, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testSameLineEventAheadOfChangeRuns() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("this.a, this.b")),
				new ArrayList<>(List.of(createEventWithColumn(1, 13, Object.class), createEventWithColumn(1, 8, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 12 ? new FixResult(0, 0, List.of("this.a, b")) : null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(12, 7), calls);
		assertEquals(1, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testSameLineEventAtChangeIsStale() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("this.a, this.b")),
				new ArrayList<>(List.of(createEventWithColumn(1, 13, Object.class), createEventWithColumn(1, 9, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 12 ? new FixResult(0, 0, List.of("this.a, b")) : null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(12), calls);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSameLineEventColumnPastEndKeptOpenByAppend() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("int a;")),
				new ArrayList<>(List.of(createEventWithColumn(1, 50, Object.class), createEventWithColumn(1, 5, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c > 5 ? new FixResult(0, 0, List.of("int a; // x")) : null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(6, 4), calls);
		assertFalse(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSameLineEventColumnPastEndOnSupplementaryLineKeptOpen() {
		final var line = "int " + new String(Character.toChars(0x1D465)) + ";";
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of(line)),
				new ArrayList<>(List.of(createEventWithColumn(1, 50, Object.class), createEventWithColumn(1, 5, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c > 5 ? new FixResult(0, 0, List.of(line + " // x")) : null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(7, 4), calls);
		assertEquals(1, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testSameLineEventColumnPastEndStaleOnAnyEdit() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("int a;")),
				new ArrayList<>(List.of(createEventWithColumn(1, 50, Object.class), createEventWithColumn(1, 5, Number.class))),
				Map.of("java.lang.Object", (l, i, c) -> new FixResult(0, 0, List.of("int b;")), "java.lang.Number", (l, i, c) -> null),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSameLineEventRunsAfterInsertBelow() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "bbbbbbbbbb", "c")),
				new ArrayList<>(List.of(createEventWithColumn(2, 10, Object.class), createEventWithColumn(2, 5, Number.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(2, 1, List.of("inserted")),
						"java.lang.Number",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(1), calls);
		assertFalse(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSameLineEventStaleAfterChangeAheadOfSupplementaryCharacter() {
		final var wide = new String(Character.toChars(0x1D465));
		final var line = "String s = \"" + wide + "\"; int b = 0;";
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of(line)),
				new ArrayList<>(List.of(createEventWithColumn(1, 21, Object.class), createEventWithColumn(1, 20, Object.class))),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 21 ? new FixResult(0, 0, List.of("String s = \"" + wide + "\"; intb = 0;")) : null;
						}
				),
				Map.of()
		);
		// the space the fix removed is code point 19 but char 20, the first char that changed
		assertEquals(List.of(21), calls);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSameLineEventStaleWhenIndentChanges() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("\t\tint a = 0, b = 0;")),
				new ArrayList<>(List.of(createEventWithColumn(1, 20, Object.class), createEventWithColumn(1, 13, Number.class))),
				Map.of("java.lang.Object", (l, i, c) -> new FixResult(0, 0, List.of("\tint a = 0, b;")), "java.lang.Number", (l, i, c) -> null),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSameLineEventTabIndentedTextKept() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("\t\tint a = 0, b = 0;")),
				new ArrayList<>(List.of(createEventWithColumn(1, 20, Object.class), createEventWithColumn(1, 13, Object.class))),
				Map.of("java.lang.Object", (l, i, c) -> c == 13 ? new FixResult(0, 0, List.of("\t\tint a = 0, b;")) : null),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_NOT_FIXABLE), result.skippedReasons().get("Object"));
		assertFalse(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSameLineMultiLineFixAfterInPlaceOneStalesEveryLaterEvent() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("aaaaaaaaaaaa")),
				new ArrayList<>(List.of(
						createEventWithColumn(1, 12, Object.class),
						createEventWithColumn(1, 9, Object.class),
						createEventWithColumn(1, 3, Object.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 11
									? new FixResult(0, 0, List.of("aaaaaaaaaaaX"))
									: new FixResult(0, 0, List.of("aaaaaaaaaaaX", "next"));
						}
				),
				Map.of()
		);
		assertEquals(List.of(11, 8), calls);
		assertEquals(2, result.fixCount());
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSameLineOtherCheckEventAheadOfChangeIsStale() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("this.a, this.b")),
				new ArrayList<>(List.of(createEventWithColumn(1, 13, Object.class), createEventWithColumn(1, 8, Number.class))),
				Map.of("java.lang.Object", (l, i, c) -> new FixResult(0, 0, List.of("this.a, b")), "java.lang.Number", (l, i, c) -> null),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSameLineOtherCheckEventStaleAfterTwoInPlaceFixes() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("aaaaaaaaaaaa")),
				new ArrayList<>(List.of(
						createEventWithColumn(1, 12, Object.class),
						createEventWithColumn(1, 9, Object.class),
						createEventWithColumn(1, 3, Number.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> c == 11 ? new FixResult(0, 0, List.of("aaaaaaaaaaaY")) : new FixResult(0, 0, List.of("aaaaaaaaXaaY")),
						"java.lang.Number",
						(l, i, c) -> null
				),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertEquals(2, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSameLineSecondInPlaceFixMovesStaleFrom() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("aaaaaaaaaaaa")),
				new ArrayList<>(List.of(
						createEventWithColumn(1, 12, Object.class),
						createEventWithColumn(1, 9, Object.class),
						createEventWithColumn(1, 6, Object.class),
						createEventWithColumn(1, 3, Object.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return switch (c) {
								case 8 -> new FixResult(0, 0, List.of("aaaXaaaaaaaY"));
								case 11 -> new FixResult(0, 0, List.of("aaaaaaaaaaaY"));
								default -> null;
							};
						}
				),
				Map.of()
		);
		// the second fix changed char 3, so the event at char 5 points into new text while char 2 does not
		assertEquals(List.of(11, 8, 2), calls);
		assertEquals(2, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSkippedMiddleEventDoesNotStaleTheLine() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("bbbbbbbbbbbb")),
				new ArrayList<>(List.of(
						createEventWithColumn(1, 12, Object.class),
						createEventWithColumn(1, 8, Object.class),
						createEventWithColumn(1, 5, Object.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(c);
							return c == 11 ? new FixResult(0, 0, List.of("bbbbbbbbbbbx")) : null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(11, 7, 4), calls);
		assertEquals(1, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testStaleLineDoesNotLeakToLineAbove() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("a", "bbbbbbbbbb")),
				new ArrayList<>(List.of(
						createEventWithColumn(2, 10, Object.class),
						createEventWithColumn(2, 5, Number.class),
						createEventWithColumn(1, 1, Integer.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> new FixResult(1, 1, List.of("x")),
						"java.lang.Number",
						(l, i, c) -> null,
						"java.lang.Integer",
						(l, i, c) -> {
							calls.add(i);
							return null;
						}
				),
				Map.of()
		);
		assertEquals(List.of(0), calls);
		assertEquals(1, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testStaleLineMovesToTheNextFixedLineWithItsOwnCheck() {
		final var calls = new ArrayList<Integer>();
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("aaaaaaaaaa", "bbbbbbbbbb")),
				new ArrayList<>(List.of(
						createEventWithColumn(2, 10, Object.class),
						createEventWithColumn(1, 10, Number.class),
						createEventWithColumn(1, 5, Object.class)
				)),
				Map.of(
						"java.lang.Object",
						(l, i, c) -> {
							calls.add(i);
							return i == 1 ? new FixResult(1, 1, List.of("bbbbbbbbbX")) : null;
						},
						"java.lang.Number",
						(l, i, c) -> new FixResult(0, 0, List.of("aaaaaaaaaX"))
				),
				Map.of()
		);
		assertEquals(List.of(1), calls);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Object"));
		assertEquals(2, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testStaleLineSuppressesEveryLaterEventOnIt() {
		final var result = CheckstyleFixAction.applyFixes(
				new ArrayList<>(List.of("bbbbbbbbbbbb")),
				new ArrayList<>(List.of(
						createEventWithColumn(1, 12, Object.class),
						createEventWithColumn(1, 8, Number.class),
						createEventWithColumn(1, 5, Integer.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(0, 0, List.of("x")),
						"java.lang.Number", (l, i, c) -> null,
						"java.lang.Integer", (l, i, c) -> null
				),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Integer"));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testStaleSkipDefersUnusedImportsDelete() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "bbbbbbbbbb"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(2, 10, Object.class),
						createEventWithColumn(2, 5, Number.class),
						createEventWithColumn(1, 1, UnusedImportsCheck.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 1, List.of("x")),
						"java.lang.Number", (l, i, c) -> null,
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer()
				),
				Map.of()
		);
		assertEquals(List.of("import java.util.List;", "x"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("UnusedImportsCheck"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSuppressedLineDefersUnusedImportsDelete() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "a", "b", "c"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(4, 10, Object.class),
						createEventWithColumn(4, 5, Number.class),
						createEventWithColumn(1, 1, UnusedImportsCheck.class)
				)),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(3, 3, List.of()),
						"java.lang.Number", (l, i, c) -> null,
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer()
				),
				Map.of()
		);
		assertEquals(List.of("import java.util.List;", "a", "b"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("UnusedImportsCheck"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSuppressedLineDeleterOnShiftedCodeDefers() {
		final var lines = new ArrayList<>(List.of("import a.B;", "class T {}"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(1, 2, RedundantImportCheck.class), createEventWithColumn(1, 1, UnusedImportsCheck.class))),
				Map.of(RedundantImportCheck.class.getName(), new DeleteLineFixer(), UnusedImportsCheck.class.getName(), new UnusedImportsFixer()),
				Map.of()
		);
		assertEquals(List.of("class T {}"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("UnusedImportsCheck"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSuppressedLineDeleterOnShiftedWhitespaceLineDefers() {
		final var lines = new ArrayList<>(List.of("import a.B;", "\t", "class T {}"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(1, 2, RedundantImportCheck.class), createEventWithColumn(1, 1, UnusedImportsCheck.class))),
				Map.of(RedundantImportCheck.class.getName(), new DeleteLineFixer(), UnusedImportsCheck.class.getName(), new UnusedImportsFixer()),
				Map.of()
		);
		assertEquals(List.of("\t", "class T {}"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("UnusedImportsCheck"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSuppressedLineDoesNotFireOnSingleLineInPlaceRewrite() {
		final var event1 = createEventWithColumn(2, 10, Object.class);
		final var event2 = createEventWithColumn(2, 5, Object.class);
		final var lines = new ArrayList<>(List.of("a", "bbbbbbbbbb", "c"));
		final CheckstyleFixer rewriteFixer = (l, i, c) -> c == 9 ? new FixResult(1, 1, List.of("bbbbbbbbbreplaced")) : null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", rewriteFixer),
				Map.of()
		);
		assertEquals(1, result.fixCount());
		assertTrue(result.skippedReasons().getOrDefault("Object", List.of()).contains(SkipMessages.FIX_NOT_FIXABLE));
		assertFalse(result.needsAnotherPass());
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
		assertTrue(result.needsAnotherPass());
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
		assertTrue(result.needsAnotherPass());
	}

	@Test
	public void testSuppressedLineNonDeleterOnShiftedBlankDefers() {
		final var lines = new ArrayList<>(List.of("import a.B;", "", "class T {}"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(1, 2, RedundantImportCheck.class), createEventWithColumn(1, 1, Number.class))),
				Map.of(RedundantImportCheck.class.getName(), new DeleteLineFixer(), "java.lang.Number", (l, i, c) -> null),
				Map.of()
		);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Number"));
		assertTrue(result.needsAnotherPass());
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testSuppressedLinePassThroughDoesNotDefer() {
		final var lines = new ArrayList<>(List.of("import a.B;", "", "class T {}"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(1, 2, RedundantImportCheck.class), createEventWithColumn(1, 1, UnusedImportsCheck.class))),
				Map.of(RedundantImportCheck.class.getName(), new DeleteLineFixer(), UnusedImportsCheck.class.getName(), new UnusedImportsFixer()),
				Map.of()
		);
		assertEquals(List.of("class T {}"), lines);
		assertEquals(2, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testSuppressedLinePassThroughOnlyOnce() {
		final var lines = new ArrayList<>(List.of("import a.B;", "", "", "class T {}"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(
						createEventWithColumn(1, 3, RedundantImportCheck.class),
						createEventWithColumn(1, 2, UnusedImportsCheck.class),
						createEventWithColumn(1, 1, Integer.class)
				)),
				Map.of(
						RedundantImportCheck.class.getName(), new DeleteLineFixer(),
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer(),
						"java.lang.Integer", new DeleteLineFixer()
				),
				Map.of()
		);
		assertEquals(List.of("", "class T {}"), lines);
		assertEquals(List.of(SkipMessages.FIX_SUPPRESSED), result.skippedReasons().get("Integer"));
		assertTrue(result.needsAnotherPass());
		assertEquals(2, result.fixCount());
	}

	@Test
	public void testTabColumnConversion() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCodePointColumn("hello", 0));
		assertEquals(5, CheckstyleFixAction.tabColumnToCodePointColumn("hello", 5));
		assertEquals(1, CheckstyleFixAction.tabColumnToCodePointColumn("\thello", 4));
		assertEquals(2, CheckstyleFixAction.tabColumnToCodePointColumn("\t\thello", 8));
		assertEquals(6, CheckstyleFixAction.tabColumnToCodePointColumn("\thello world", 9));
	}

	@Test
	public void testTabColumnConversionBeyondLine() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCodePointColumn("hello", 10));
	}

	@Test
	public void testTabColumnConversionMidLineTab() {
		assertEquals(3, CheckstyleFixAction.tabColumnToCodePointColumn("ab\tcd", 4));
		assertEquals(4, CheckstyleFixAction.tabColumnToCodePointColumn("ab\tcd", 5));
	}

	@Test
	public void testTabColumnConversionNoTabs() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCodePointColumn("abcdef", 0));
		assertEquals(3, CheckstyleFixAction.tabColumnToCodePointColumn("abcdef", 3));
		assertEquals(6, CheckstyleFixAction.tabColumnToCodePointColumn("abcdef", 6));
	}

	@Test
	public void testTabColumnConversionTabAtStartColZero() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCodePointColumn("\thello", 0));
		assertEquals(0, CheckstyleFixAction.tabColumnToCodePointColumn("\t", 0));
	}

	@Test
	public void testTabColumnConversionTabOnAlignmentBoundary() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCodePointColumn("abcd\tef", 8));
		assertEquals(4, CheckstyleFixAction.tabColumnToCodePointColumn("abcd\tef", 4));
	}

	@Test
	public void testUnusedImportsDeleteRunsWithoutDeferredEvent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "a", "b", "c"));
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(createEventWithColumn(4, 1, Object.class), createEventWithColumn(1, 1, UnusedImportsCheck.class))),
				Map.of(
						"java.lang.Object", (l, i, c) -> new FixResult(1, 3, List.of("x", "y", "z")),
						UnusedImportsCheck.class.getName(), new UnusedImportsFixer()
				),
				Map.of()
		);
		assertEquals(2, result.fixCount());
		assertEquals(List.of("x", "y", "z"), lines);
		assertFalse(result.needsAnotherPass());
	}
}