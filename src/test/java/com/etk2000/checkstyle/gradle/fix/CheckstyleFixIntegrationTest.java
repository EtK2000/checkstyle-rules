package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction.ApplyFixesResult;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class CheckstyleFixIntegrationTest {
	record FixOutput(@Nonnull String content, @Nonnull ApplyFixesResult result) {}

	record MultiPassOutput(
			@Nonnull String content,
			int pass1FixCount,
			boolean pass1NeedsSecondPass,
			int pass2FixCount,
			boolean pass2NeedsSecondPass
	) {}

	private int verifyCleanCallCount;

	@TempDir
	Path tempDir;

	@Nonnull
	private FixOutput assertFullFix(@Nonnull String caseName, int expectedFixCount, boolean expectedNeedsSecondPass) throws Exception {
		return assertFullFix(caseName, expectedFixCount, expectedNeedsSecondPass, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private FixOutput assertFullFix(@Nonnull String caseName, int expectedFixCount, boolean expectedNeedsSecondPass, @Nonnull String minSdk) throws Exception {
		return assertFullFixTopic("integration", caseName, expectedFixCount, expectedNeedsSecondPass, minSdk);
	}

	@Nonnull
	private FixOutput assertFullFixTopic(@Nonnull String topic, @Nonnull String caseName, int expectedFixCount, boolean expectedNeedsSecondPass, @Nonnull String minSdk) throws Exception {
		final var fx = TestResources.loadCase(topic, caseName);
		final var file = tempDir.resolve(caseName + ".java").toFile();
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));
		final var output = runFixAndGetResult(file, minSdk);
		assertAll(
				() -> assertEquals(String.join("\n", fx.fixedLines()), output.content(), "content"),
				() -> assertEquals(expectedFixCount, output.result().fixCount(), "fixCount"),
				() -> assertEquals(expectedNeedsSecondPass, output.result().needsSecondPass(), "needsSecondPass")
		);
		return output;
	}

	private void assertMinSdkGate(@Nonnull String caseName, @Nonnull String belowSdk, @Nonnull String atSdk) throws Exception {
		final var fx = TestResources.loadCase("integration", caseName);
		final var input = String.join("\n", fx.inputLines());
		final var file = tempDir.resolve(caseName + ".java").toFile();
		Files.writeString(file.toPath(), input);
		assertEquals(input, runFixAndGetResult(file, belowSdk).content());
		assertEquals(String.join("\n", fx.fixedLines()), runFixAndGetResult(file, atSdk).content());
	}

	@Test
	public void doExecuteNormalModePrintsSummary() throws Exception {
		final var file = tempDir.resolve("NormalSummary.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", TestResources.loadCase("integration", "array_trailing_comma").inputLines()));

		final var origOut = System.out;
		final var captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured));
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			CheckstyleFixAction.doExecute(config, false, List.of(file));
		}
		finally {
			System.setOut(origOut);
		}
		assertTrue(captured.toString().contains("Fixed"));
		verifyFixedOutputClean(file, Files.readString(file.toPath()), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void doExecuteNormalModeSecondPassFlag() throws Exception {
		final var file = tempDir.resolve("SecondPass.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", TestResources.loadCase("integration", "do_execute_charset_for_name").inputLines()));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, false, List.of(file));
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
		verifyFixedOutputClean(file, Files.readString(file.toPath()), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void doExecuteNormalModeWritesFile() throws Exception {
		final var file = tempDir.resolve("NormalExec.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", TestResources.loadCase("integration", "array_trailing_comma").inputLines()));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, false, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		final var written = Files.readString(file.toPath());
		assertFalse(written.contains(",}"));
		assertFalse(written.endsWith("\n"));
		verifyFixedOutputClean(file, written, String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void e2eFixableCountMatchesBetweenDryRunAndNormalRun() throws Exception {
		final var content = String.join("\n", TestResources.loadCase("integration", "e2e_dry_vs_normal_run_fixable_count").inputLines());
		final var file1 = tempDir.resolve("E2E1.java").toFile();
		Files.writeString(file1.toPath(), content);

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var dryResult = CheckstyleFixAction.doExecute(config, true, List.of(file1));

		Files.writeString(file1.toPath(), content);
		final var normalResult = CheckstyleFixAction.doExecute(config, false, List.of(file1));

		assertEquals(3, dryResult[1]);
		assertEquals(dryResult[1], normalResult[1]);
		verifyFixedOutputClean(file1, Files.readString(file1.toPath()), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file) throws Exception {
		return FullPipelineRunner.runChecks(file, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		return FullPipelineRunner.runChecks(file, minSdk);
	}

	@Nonnull
	private FixOutput runFixAndGetResult(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		final var violations = runChecks(file, minSdk);
		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		final var content = String.join("\n", lines);
		if (result.fixCount() > 0)
			verifyFixedOutputClean(file, content, minSdk, result.needsSecondPass());
		return new FixOutput(content, result);
	}

	@Nonnull
	private MultiPassOutput runFixMultiPass(@Nonnull File file) throws Exception {
		return runFixMultiPass(file, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private MultiPassOutput runFixMultiPass(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		final var config = CheckstyleFixAction.createCheckerConfig(minSdk);
		final var pass1Result = CheckstyleFixAction.doExecute(config, false, List.of(file));
		final var pass1FixCount = pass1Result[1];
		final var pass1NeedsSecondPass = pass1Result[0] != 0;
		var pass2FixCount = 0;
		var pass2NeedsSecondPass = false;
		if (pass1NeedsSecondPass) {
			final var pass2Result = CheckstyleFixAction.doExecute(config, false, List.of(file));
			pass2FixCount = pass2Result[1];
			pass2NeedsSecondPass = pass2Result[0] != 0;
		}
		final var content = Files.readString(file.toPath());
		verifyFixedOutputClean(file, content, minSdk, pass2NeedsSecondPass);
		return new MultiPassOutput(content, pass1FixCount, pass1NeedsSecondPass, pass2FixCount, pass2NeedsSecondPass);
	}

	@BeforeEach
	void setUp() {
		verifyCleanCallCount = 0;
	}

	@AfterEach
	void tearDown() {
		assertEquals(1, verifyCleanCallCount, "verifyFixedOutputClean must be called exactly once per test");
	}

	@Test
	public void testAllFixedNoSkipReasons() throws Exception {
		final var output = assertFullFix("all_fixed_no_skip_reasons", 2, false);
		assertTrue(output.result().skippedReasons().isEmpty());
	}

	@Test
	public void testFieldConsolidationWrappingFourLongFields() throws Exception {
		final var fx = TestResources.loadCase("integration", "field_consolidation_wrapping_four_long_fields");
		final var file = tempDir.resolve("FieldConsWrap4.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));
		assertEquals(String.join("\n", fx.fixedLines()), runFixMultiPass(file).content());
	}

	@Test
	public void testJitInefficiencyToArraySized() throws Exception {
		assertFullFix("jit_inefficiency_to_array_sized", 1, false, "29");
	}

	@Test
	public void testMinSdkGatesCollectionsSort() throws Exception {
		assertMinSdkGate("prefer_specific_api_collections_sort_no_comparator", "23", "24");
	}

	@Test
	public void testMinSdkGatesStringFormat() throws Exception {
		assertMinSdkGate("prefer_specific_api_string_format", "33", "34");
	}

	@Test
	public void testMinSdkGatesToArray() throws Exception {
		assertMinSdkGate("prefer_specific_api_to_array_new_zero", "32", "33");
	}

	@Test
	public void testMixedFixAndSkipFromSameCheck() throws Exception {
		final var output = assertFullFix("mixed_fix_and_skip_from_same_check", 2, false);
		assertTrue(
				output.result().skippedReasons().getOrDefault("FieldSortingCheck", List.of())
						.contains(SkipMessages.FIX_NOT_FIXABLE)
		);
	}

	@Test
	public void testMultipleChecksSkipReasons() throws Exception {
		final var output = assertFullFix("multiple_checks_skip_reasons", 1, false);
		assertTrue(output.result().skippedReasons().containsKey("ControlFlowBracesCheck"));
		assertEquals(
				SkipMessages.CONTROL_FLOW_SKIP_MULTILINE_HEADER,
				output.result().skippedReasons().get("ControlFlowBracesCheck").getFirst()
		);
	}

	@Test
	public void testPreferExactAssertionHelperQualifierUnchanged() throws Exception {
		final var input = String.join("\n", TestResources.loadCase("integration", "prefer_exact_assertion_helper_qualifier_unchanged").inputLines());
		final var file = tempDir.resolve("ExactAssertHelperQualifier.java").toFile();
		Files.writeString(file.toPath(), input);
		assertEquals(input, runFixMultiPass(file).content());
	}

	@Test
	public void testPreferExactAssertionQualifiedJunit4NegationFallback() throws Exception {
		final var output = assertFullFix("prefer_exact_assertion_qualified_junit4_negation_fallback", 1, false);
		assertTrue(output.result().skippedReasons().isEmpty());
	}

	@Test
	public void testPreferSpecificApiImportLineWithTrailingWhitespaceStillDetected() throws Exception {
		assertFullFixTopic("integrationtrailingws", "import_line_with_trailing_whitespace_still_detected", 2, true, String.valueOf(Integer.MAX_VALUE));
	}

	@Test
	public void testPreferSpecificApiZeroFixesNoTrigger() throws Exception {
		final var file = tempDir.resolve("ZeroFixes.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", TestResources.loadCase("integration", "prefer_specific_api_zero_fixes_no_trigger").inputLines()));

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(0, result.fixCount());
		assertFalse(result.needsSecondPass());
		verifyFixedOutputClean(file, String.join("\n", lines), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testRedundantImportContiguousSuppressesDuplicate() throws Exception {
		final var output = assertFullFix("redundant_import_contiguous_suppresses_duplicate", 1, false);
		assertFalse(output.result().skippedReasons().isEmpty());
		assertTrue(
				output.result().skippedReasons().values().stream()
						.anyMatch(reasons -> reasons.contains(SkipMessages.FIX_SUPPRESSED))
		);
	}

	@Test
	public void testTrailingNewlineDryRunHintCountsSingleNewline() throws Exception {
		final var fx = TestResources.loadCase("integration", "trailing_newline_single");
		final var input = String.join("\n", fx.inputLines());
		final var file = tempDir.resolve("TrailingNewlineDryRun.java").toFile();
		Files.writeString(file.toPath(), input);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(1, result[1]);
		assertEquals(input, Files.readString(file.toPath()));
		verifyFixedOutputClean(file, String.join("\n", fx.fixedLines()), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testTrailingNewlinePlusWhitespaceDryRunCountsBoth() throws Exception {
		final var fx = TestResources.loadCase("integration", "trailing_newline_plus_whitespace");
		final var input = String.join("\n", fx.inputLines());
		final var file = tempDir.resolve("TrailingNewlinePlusWsDryRun.java").toFile();
		Files.writeString(file.toPath(), input);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(2, result[1]);
		assertEquals(input, Files.readString(file.toPath()));
		verifyFixedOutputClean(file, String.join("\n", fx.fixedLines()), String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testTrailingNewlineSingleWrittenByDoExecute() throws Exception {
		final var fx = TestResources.loadCase("integration", "trailing_newline_single");
		final var file = tempDir.resolve("TrailingNewlineDoExecute.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, false, List.of(file));
		final var written = Files.readString(file.toPath());
		assertEquals(String.join("\n", fx.fixedLines()), written);
		assertFalse(written.endsWith("\n"));
		assertEquals(1, result[1]);
		verifyFixedOutputClean(file, written, String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testVerifyCleanAcceptsCleanOutput() throws Exception {
		final var content = String.join("\n", TestResources.loadCase("integration", "verify_clean_accepts_clean_output").inputLines());
		final var file = tempDir.resolve("VerClean.java").toFile();
		Files.writeString(file.toPath(), content);
		verifyFixedOutputClean(file, content, String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testVerifyCleanAcceptsUnfixableViolations() throws Exception {
		final var content = String.join("\n", TestResources.loadCase("integration", "verify_clean_accepts_unfixable_violations").inputLines());
		final var file = tempDir.resolve("VerUnfixable.java").toFile();
		Files.writeString(file.toPath(), content);
		verifyFixedOutputClean(file, content, String.valueOf(Integer.MAX_VALUE), false);
	}

	@Test
	public void testVerifyCleanHandlesMultiPassStabilization() throws Exception {
		final var content = String.join("\n", TestResources.loadCase("integration", "verify_clean_handles_multi_pass_stabilization").inputLines());
		final var file = tempDir.resolve("VerMulti.java").toFile();
		Files.writeString(file.toPath(), content);
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		verifyFixedOutputClean(file, String.join("\n", lines), String.valueOf(Integer.MAX_VALUE), result.needsSecondPass());
	}

	private void verifyFixedOutputClean(@Nonnull File file, @Nonnull String content, @Nonnull String minSdk, boolean needsSecondPass) throws Exception {
		++verifyCleanCallCount;
		Files.writeString(file.toPath(), content);
		if (needsSecondPass) {
			final var violations = runChecks(file, minSdk);
			final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
			final var secondPassResult = CheckstyleFixAction.applyFixes(
					lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS
			);
			assertFalse(secondPassResult.needsSecondPass(), "Second pass should not require a third pass");
			Files.writeString(file.toPath(), String.join("\n", lines));
		}
		final var finalViolations = runChecks(file, minSdk);
		final var finalLines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var finalResult = CheckstyleFixAction.applyFixes(
				finalLines, finalViolations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS
		);
		assertEquals(0, finalResult.fixCount(), "Fixed output still has fixable violations");
	}
}