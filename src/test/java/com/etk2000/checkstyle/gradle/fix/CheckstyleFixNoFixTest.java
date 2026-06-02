package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction.ApplyFixesResult;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

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
	public void doExecuteDryRunSecondPassFlag() throws Exception {
		final var file = tempDir.resolve("DryPass.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "do_execute_dry_run_second_pass_flag"));

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
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
	public void testFieldConsolidationBlockCommentBeforeFieldNameSkipped() throws Exception {
		final var file = tempDir.resolve("FieldConsBlockComment.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_field_consolidation_block_comment_before_field_name_skipped");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
	}

	@Test
	public void testFieldConsolidationBlockCommentPostNameSkipped() throws Exception {
		final var file = tempDir.resolve("FieldConsBlockCommentPost.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_field_consolidation_block_comment_post_name_skipped");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
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
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingFieldViolationNowFixed() throws Exception {
		final var file = tempDir.resolve("FieldOrder.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_field_sorting_field_violation_now_fixed"));

		final var output = runFixAndGetResult(file);
		assertEquals(TestResources.loadCaseExpected(TOPIC, "test_field_sorting_field_violation_now_fixed"), output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testMultilineCloseCoOccurringInlineBlockNowFixed() throws Exception {
		final var file = tempDir.resolve("MultilineCloseCoOccurringInlineBlock.java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, "test_multiline_close_co_occurring_inline_block_now_fixed"));

		final var output = runFixAndGetResult(file);
		assertEquals(TestResources.loadCaseExpected(TOPIC, "test_multiline_close_co_occurring_inline_block_now_fixed"), output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
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
		assertFalse(result.needsSecondPass());
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
	public void testPreferVarWarningNotFixed() throws Exception {
		final var file = tempDir.resolve("VarWarn.java").toFile();
		final var content = TestResources.loadCaseSource(TOPIC, "test_prefer_var_warning_not_fixed");
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
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
}