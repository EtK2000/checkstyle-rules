package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.NoArrayTrailingCommaCheck;
import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction.ApplyFixesResult;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

@Isolated
public class CheckstyleFixNoFixTest {
	record FixOutput(@Nonnull String content, @Nonnull ApplyFixesResult result) {}

	private static final String TOPIC = "nofix";

	@TempDir
	Path tempDir;

	@Test
	public void collectJavaFilesEmptyDir() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("empty"));
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertTrue(files.isEmpty());
	}

	@Test
	public void collectJavaFilesFiltersNonJava() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("src"));
		Files.writeString(dir.resolve("A.java"), TestResources.loadCaseSource(TOPIC, "collect_java_files_filters_non_java_a"));
		Files.writeString(dir.resolve("B.txt"), "not java");
		Files.writeString(dir.resolve("C.java"), TestResources.loadCaseSource(TOPIC, "collect_java_files_filters_non_java_c"));
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertEquals(2, files.size());
		assertTrue(files.stream().allMatch(f -> f.getName().endsWith(".java")));
	}

	@Test
	public void collectJavaFilesNonExistentDir() throws Exception {
		final var dir = tempDir.resolve("does-not-exist");
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertTrue(files.isEmpty());
	}

	@Test
	public void collectJavaFilesRecursive() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("src"));
		final var sub = Files.createDirectory(dir.resolve("sub"));
		Files.writeString(dir.resolve("A.java"), TestResources.loadCaseSource(TOPIC, "collect_java_files_recursive_a"));
		Files.writeString(sub.resolve("B.java"), TestResources.loadCaseSource(TOPIC, "collect_java_files_recursive_b"));
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertEquals(2, files.size());
	}

	@Test
	public void doExecuteDryRunAnotherPassFlag() throws Exception {
		final var file = tempDir.resolve("DryPass.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_second_pass_flag"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
	}

	@Test
	public void doExecuteDryRunDoesNotModifyFile() throws Exception {
		final var file = tempDir.resolve("DryExec.java").toFile();
		final var original = TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_does_not_modify_file");
		Files.writeString(file.toPath(), original);

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		assertEquals(original, Files.readString(file.toPath()));
	}

	@Test
	public void doExecuteDryRunFixableButAllSkipped() throws Exception {
		final var file = tempDir.resolve("DrySkip.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_fixable_but_all_skipped"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
	}

	@Test
	public void doExecuteDryRunFixableCountMatchesViolations() throws Exception {
		final var file = tempDir.resolve("DryAnnot.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_fixable_count_matches_violations"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(2, result[1]);
	}

	@Test
	public void doExecuteDryRunReturnsCorrectCount() throws Exception {
		final var file = tempDir.resolve("DryCount.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_returns_correct_count"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(2, result[1]);
	}

	@Test
	public void doExecuteDryRunSuppressesSummaryOutput() throws Exception {
		final var file = tempDir.resolve("DrySilent.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_suppresses_summary_output"));

		final var origOut = System.out;
		final var captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured));
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			CheckstyleFixAction.doExecute(config, true, List.of(file));
		}
		finally {
			System.setOut(origOut);
		}
		assertFalse(captured.toString().contains("Fixed"));
	}

	@Test
	public void doExecuteKeepsAFileItsFixesLeaveUnparseable() throws Exception {
		final var file = tempDir.resolve("BrokenFix.java").toFile();
		final var input = String.join("\n", TestResources.loadCase("integration", "array_trailing_comma").inputLines());
		Files.writeString(file.toPath(), input);
		final CheckstyleFixer breaksTheParse = (lines, lineIndex, column) -> new FixResult(lineIndex, lineIndex, List.of("}{"));
		final var changed = new ArrayList<File>();
		final var origErr = System.err;
		final var captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured));
		final int[] result;
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			result = CheckstyleFixAction.doExecute(config, false, List.of(file), changed, Map.of(NoArrayTrailingCommaCheck.class.getName(), breaksTheParse), Map.of());
		}
		finally {
			System.setErr(origErr);
		}
		assertEquals(input, Files.readString(file.toPath()));
		assertEquals(List.of(), changed);
		assertEquals(0, result[1]);
		assertTrue(captured.toString().startsWith("Not writing " + file + ": its fixes would leave it unparseable ("), captured.toString());
	}

	@Test
	public void doExecuteWritesAFileItsFixesLeaveParseable() throws Exception {
		final var file = tempDir.resolve("SoundFix.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", TestResources.loadCase("integration", "array_trailing_comma").inputLines()));
		final var changed = new ArrayList<File>();
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, false, List.of(file), changed, Map.of(NoArrayTrailingCommaCheck.class.getName(), new NoArrayTrailingCommaFixer()), Map.of());
		assertEquals(List.of(file), changed);
		assertEquals(1, result[1]);
		assertFalse(Files.readString(file.toPath()).contains(",}"));
	}

	@Test
	public void doExecuteZeroViolationsReturnsZeros() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_zero_violations_returns_zeros"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
	}

	@Test
	public void e2eMixedFixableAndUnfixableViolations() throws Exception {
		final var file = tempDir.resolve("E2EMixed.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "e2e_mixed_fixable_and_unfixable_violations"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));

		assertEquals(2, result[1]);
	}

	@Test
	public void e2eMultipleFilesAggregatesCount() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("multi"));
		final var f1 = dir.resolve("A.java").toFile();
		final var f2 = dir.resolve("B.java").toFile();
		Files.writeString(f1.toPath(), TestResources.loadCaseSource(TOPIC, "e2e_multiple_files_aggregates_count_a"));
		Files.writeString(f2.toPath(), TestResources.loadCaseSource(TOPIC, "e2e_multiple_files_aggregates_count_b"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(f1, f2));
		assertEquals(2, result[1]);
	}

	@Test
	public void fixUntilSettledCleanFileRunsOnePass() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		final var source = TestResources.loadCaseSource(TOPIC, "do_execute_zero_violations_returns_zeros");
		Files.writeString(file.toPath(), source);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		assertEquals(1, CheckstyleFixAction.fixUntilSettled(config, List.of(file), CheckstyleFixAction.MAX_FIX_PASSES));
		assertEquals(source, Files.readString(file.toPath()));
	}

	@Test
	public void fixUntilSettledDoesNotRecheckUnwritableFile() throws Exception {
		final var file = tempDir.resolve("ReadOnly.java").toFile();
		final var source = TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_returns_correct_count");
		Files.writeString(file.toPath(), source);
		assertTrue(file.setWritable(false));
		final var origErr = System.err;
		final var captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured));
		final int passes;
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			passes = CheckstyleFixAction.fixUntilSettled(config, List.of(file), CheckstyleFixAction.MAX_FIX_PASSES);
		}
		finally {
			file.setWritable(true);
			System.setErr(origErr);
		}
		assertEquals(1, passes);
		assertEquals(source, Files.readString(file.toPath()));
		assertTrue(captured.toString().contains("Could not write " + file.getAbsolutePath() + ": " + AccessDeniedException.class.getName()), captured::toString);
	}

	@Test
	public void fixUntilSettledMaxPassesZeroStillRunsOnce() throws Exception {
		final var file = tempDir.resolve("Stats.java").toFile();
		final var fx = TestResources.loadCase(TOPIC, "fix_until_settled_one_pass_merge_suppressed_line");
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		assertEquals(1, CheckstyleFixAction.fixUntilSettled(config, List.of(file), 0));
		assertEquals(String.join("\n", fx.fixedLines()), Files.readString(file.toPath()));
	}

	@Test
	public void fixUntilSettledRechecksOnlyRewrittenFiles() throws Exception {
		final var fixed = tempDir.resolve("Fixed.java").toFile();
		final var skipped = tempDir.resolve("Skipped.java").toFile();
		Files.writeString(fixed.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_returns_correct_count"));
		Files.writeString(skipped.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_fixable_but_all_skipped"));

		final var origOut = System.out;
		final var captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured));
		final int passes;
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			passes = CheckstyleFixAction.fixUntilSettled(config, List.of(fixed, skipped), CheckstyleFixAction.MAX_FIX_PASSES);
		}
		finally {
			System.setOut(origOut);
		}
		// a pass that re-checked the skip-only file would print its own "Fixed 0 violations" summary
		assertEquals(2, passes);
		assertEquals(1, captured.toString().lines().filter(line -> line.startsWith("Fixed ")).count(), captured::toString);
		assertEquals(TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_fixable_but_all_skipped"), Files.readString(skipped.toPath()));
	}

	@Test
	public void fixUntilSettledSettlesEveryFile() throws Exception {
		final var stats = tempDir.resolve("Stats.java").toFile();
		final var clean = tempDir.resolve("Clean.java").toFile();
		final var statsCase = TestResources.loadCase("integration", "field_consolidation_explicit_init_shrunk_line");
		final var cleanSource = TestResources.loadCaseSource(TOPIC, "do_execute_zero_violations_returns_zeros");
		Files.writeString(stats.toPath(), String.join("\n", statsCase.inputLines()));
		Files.writeString(clean.toPath(), cleanSource);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var origErr = System.err;
		final var captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured));
		try {
			assertEquals(3, CheckstyleFixAction.fixUntilSettled(config, List.of(stats, clean), CheckstyleFixAction.MAX_FIX_PASSES));
		}
		finally {
			System.setErr(origErr);
		}
		assertEquals(String.join("\n", statsCase.fixedLines()), Files.readString(stats.toPath()));
		assertEquals(cleanSource, Files.readString(clean.toPath()));
		assertFalse(captured.toString().contains("Stopped after"), captured::toString);
	}

	@Test
	public void fixUntilSettledStopsAtMaxPasses() throws Exception {
		final var file = tempDir.resolve("Player.java").toFile();
		final var fx = TestResources.loadCase(TOPIC, "fix_until_settled_one_pass_merge_stale_line");
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var origErr = System.err;
		final var captured = new ByteArrayOutputStream();
		System.setErr(new PrintStream(captured));
		try {
			assertEquals(1, CheckstyleFixAction.fixUntilSettled(config, List.of(file), 1));
		}
		finally {
			System.setErr(origErr);
		}
		assertEquals(String.join("\n", fx.fixedLines()), Files.readString(file.toPath()));
		assertEquals(
				"Stopped after 1 fix passes with files still changing, run the task again to finish them: [" + file.getAbsoluteFile() + "]" + System.lineSeparator(),
				captured.toString()
		);
	}

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file) throws Exception {
		return FullPipelineRunner.runChecks(file, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private FixOutput runFixAndGetResult(@Nonnull File file) throws Exception {
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		return new FixOutput(String.join("\n", lines), result);
	}

	@Test
	public void testAllSkippedHasReasons() throws Exception {
		final var file = tempDir.resolve("AllSkipped.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_all_skipped_has_reasons"));

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("ControlFlowBracesCheck"));
		final var reasons = output.result().skippedReasons().get("ControlFlowBracesCheck");
		assertFalse(reasons.isEmpty());
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP_MULTILINE_HEADER, reasons.getFirst());
	}

	@Test
	public void testApplyFixesSkipsUnknownViolations() throws Exception {
		final var file = tempDir.resolve("Unknown.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_apply_fixes_skips_unknown_violations"));

		final var violations = runChecks(file);
		assertFalse(violations.isEmpty());

		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, Map.of(), Map.of());
		assertEquals(0, result.fixCount());
		assertFalse(result.skippedReasons().isEmpty());
		assertTrue(
				result.skippedReasons().values().stream()
						.anyMatch(reasons -> reasons.contains(SkipMessages.FIX_NO_FIXER))
		);
	}

	@Test
	public void testArrayTypeStyleMultiVarWithInitializerSkipped() throws Exception {
		final var file = tempDir.resolve("ArrTypeStyleMultiVarInit.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_array_type_style_multi_var_with_initializer_skipped");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
	}

	@Test
	public void testCleanFileNoViolationsNoReasons() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_clean_file_no_violations_no_reasons"));

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().isEmpty());
	}

	@Test
	public void testFieldConsolidationWrappingPreExistingMultiLineNotFlagged() throws Exception {
		final var file = tempDir.resolve("FieldConsPreWrap.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_field_consolidation_wrapping_pre_existing_multi_line_not_flagged");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
	}

	@Test
	public void testFieldSortingEnumAlreadySorted() throws Exception {
		final var file = tempDir.resolve("SortedEnum.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_field_sorting_enum_already_sorted");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsAnotherPass());
	}

	@Test
	public void testFieldSortingEnumOrderWarningNotFixed() throws Exception {
		final var file = tempDir.resolve("EnumOrder.java").toFile();
		final var content = TestResources.loadCaseSource("integration", "field_sorting_enum_reorder");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
		assertEquals(Map.of("FieldSortingCheck", List.of(SkipMessages.FIX_SEVERITY)), output.result().skippedReasons());
	}

	@Test
	public void testFieldSortingFieldViolationNowFixed() throws Exception {
		final var file = tempDir.resolve("FieldOrder.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_field_sorting_field_violation_now_fixed"));

		final var output = runFixAndGetResult(file);
		assertEquals(TestResources.loadCaseExpected(TOPIC, "test_field_sorting_field_violation_now_fixed"), output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsAnotherPass());
	}

	@Test
	public void testMultilineCloseCoOccurringInlineBlockNowFixed() throws Exception {
		final var file = tempDir.resolve("MultilineCloseCoOccurringInlineBlock.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_multiline_close_co_occurring_inline_block_now_fixed"));

		final var output = runFixAndGetResult(file);
		assertEquals(TestResources.loadCaseExpected(TOPIC, "test_multiline_close_co_occurring_inline_block_now_fixed"), output.content());
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsAnotherPass());
	}

	@Test
	public void testMultilineCloseCoOccurringSkip() throws Exception {
		final var file = tempDir.resolve("MultilineCloseCoOccurring.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_multiline_close_co_occurring_skip");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("MultilineCallFormattingCheck"));
		assertTrue(
				output.result().skippedReasons().get("MultilineCallFormattingCheck")
						.contains(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED)
		);
	}

	@Test
	public void testMultilineClosePullupCommentSkip() throws Exception {
		final var file = tempDir.resolve("MultilineClosePullupComment.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_multiline_close_pullup_comment_skip");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(
				output.result().skippedReasons().getOrDefault("MultilineCallFormattingCheck", List.of())
						.contains(SkipMessages.MULTILINE_PUT_SKIP_COMMENT)
		);
	}

	@Test
	public void testMultilinePullUpTailCommentSkip() throws Exception {
		final var file = tempDir.resolve("MultilinePullUpTailComment.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_multiline_pull_up_tail_comment_skip");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(
				output.result().skippedReasons().getOrDefault("MultilineCallFormattingCheck", List.of())
						.contains(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN)
		);
	}

	@Test
	public void testMultilinePutCommentSkip() throws Exception {
		final var file = tempDir.resolve("MultilinePutComment.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_multiline_put_comment_skip");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("MultilineCallFormattingCheck"));
		assertTrue(
				output.result().skippedReasons().get("MultilineCallFormattingCheck")
						.contains(SkipMessages.MULTILINE_PUT_SKIP_COMMENT)
		);
	}

	@Test
	public void testMultilinePutUnsupportedShapeSkip() throws Exception {
		final var file = tempDir.resolve("MultilinePutUnsupported.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_multiline_put_unsupported_shape_skip");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("MultilineCallFormattingCheck"));
		assertTrue(
				output.result().skippedReasons().get("MultilineCallFormattingCheck")
						.contains(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED)
		);
	}

	@Test
	public void testNoViolations() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_no_violations"));

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(0, result.fixCount());
		assertFalse(result.needsAnotherPass());
	}

	@Test
	public void testPreferMathMethodSkipsMultilineTernary() throws Exception {
		final var file = tempDir.resolve("MathMulti.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_prefer_math_method_skips_multiline_ternary"));

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("PreferMathMethodCheck"));
		assertTrue(
				output.result().skippedReasons().get("PreferMathMethodCheck")
						.contains(SkipMessages.MATH_METHOD_SKIP)
		);
	}

	@Test
	public void testPreferVarTryResourceWarningNotFixed() throws Exception {
		final var file = tempDir.resolve("TryResourceWarn.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_prefer_var_try_resource_warning_not_fixed");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsAnotherPass());
		assertTrue(
				output.result().skippedReasons().get("PreferVarCheck").contains(SkipMessages.FIX_SEVERITY)
		);
	}

	@Test
	public void testPreferVarWarningNotFixed() throws Exception {
		final var file = tempDir.resolve("VarWarn.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_prefer_var_warning_not_fixed");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsAnotherPass());
		assertTrue(output.result().skippedReasons().containsKey("PreferVarCheck"));
		assertTrue(
				output.result().skippedReasons().get("PreferVarCheck").contains(SkipMessages.FIX_SEVERITY)
		);
	}

	@Test
	public void testUnusedImportUnterminatedBlockCommentSkipped() throws Exception {
		final var file = tempDir.resolve("UnusedImportUnterminatedBlockComment.java").toFile();
		final var input = TestResources.loadCaseSource(TOPIC, "test_unused_import_unterminated_block_comment_skipped");
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
		assertTrue(
				output.result().skippedReasons().getOrDefault("UnusedImportsCheck", List.of())
						.contains(SkipMessages.UNUSED_IMPORTS_MALFORMED)
		);
	}

	@Test
	public void writeAtomicallyFailedMoveLeavesNoTemporaryFile() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("Dir.java"));
		Files.writeString(dir.resolve("child"), "kept");
		assertThrows(IOException.class, () -> CheckstyleFixAction.writeAtomically(dir, "new"));
		try (var listing = Files.list(tempDir)) {
			assertEquals(List.of(dir), listing.toList());
		}
		assertEquals("kept", Files.readString(dir.resolve("child")));
	}

	@Test
	public void writeAtomicallyKeepsPermissions() throws Exception {
		final var file = tempDir.resolve("Perms.java");
		Files.writeString(file, "old");
		final var permissions = PosixFilePermissions.fromString("rw-r-----");
		Files.setPosixFilePermissions(file, permissions);
		CheckstyleFixAction.writeAtomically(file, "new");
		assertEquals("new", Files.readString(file));
		assertEquals(permissions, Files.getPosixFilePermissions(file));
		try (var listing = Files.list(tempDir)) {
			assertEquals(List.of(file), listing.toList());
		}
	}

	@Test
	public void writeAtomicallyRefusesUnwritableFile() throws Exception {
		final var file = tempDir.resolve("ReadOnly.java");
		Files.writeString(file, "old");
		assertTrue(file.toFile().setWritable(false));
		try {
			assertThrows(AccessDeniedException.class, () -> CheckstyleFixAction.writeAtomically(file, "new"));
		}
		finally {
			file.toFile().setWritable(true);
		}
		assertEquals("old", Files.readString(file));
		try (var listing = Files.list(tempDir)) {
			assertEquals(List.of(file), listing.toList());
		}
	}

	@Test
	public void writeAtomicallyWritesInPlaceInDirectoryTakingNoNewFiles() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("locked"));
		final var file = Files.writeString(dir.resolve("Locked.java"), "old");
		assertTrue(dir.toFile().setWritable(false));
		try {
			CheckstyleFixAction.writeAtomically(file, "new");
		}
		finally {
			dir.toFile().setWritable(true);
		}
		assertEquals("new", Files.readString(file));
		try (var listing = Files.list(dir)) {
			assertEquals(List.of(file), listing.toList());
		}
	}

	@Test
	public void writeAtomicallyWritesThroughSymlink() throws Exception {
		final var real = Files.writeString(tempDir.resolve("Real.java"), "old");
		final var link = Files.createSymbolicLink(tempDir.resolve("Link.java"), real);
		CheckstyleFixAction.writeAtomically(link, "new");
		assertTrue(Files.isSymbolicLink(link));
		assertEquals("new", Files.readString(real));
		try (var listing = Files.list(tempDir)) {
			assertEquals(List.of(link, real), listing.sorted().toList());
		}
	}
}