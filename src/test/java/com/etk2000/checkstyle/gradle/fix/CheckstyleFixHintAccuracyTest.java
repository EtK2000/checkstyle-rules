package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Verifies the hint message ("auto-fix N of M violations") reflects what
 * {@code checkstyleFix} would actually fix. Each row exercises a distinct
 * skip-source in {@code applyFixes} and asserts both {@code result[1]}
 * (totalFixed, the value the hint uses) and the post-fix
 * {@code formatHintMessage} output.
 *
 * <p>FIX_BOUNDS (applyFixes lines 220-222) is not exercised here. Checkstyle
 * bounds-checks events to the file, so {@code lineIndex} out-of-range cannot
 * be reached through {@code doExecute}. Direct {@code applyFixes} unit-style
 * coverage of that branch lives in {@link CheckstyleFixUtilTest}.
 *
 * <p>FIX_NO_FIXER and FIX_SEVERITY are out of scope because the hint
 * denominator is the full violation count, not a speculative fixable count.
 */
public class CheckstyleFixHintAccuracyTest {
	private static final String TOPIC = "hintaccuracy";

	private static Stream<Arguments> hintScenarios() {
		return Stream.of(
				Arguments.of("prefer_direct_boolean_return_multiline_condition", 0, 1, null),
				Arguments.of("prefer_direct_boolean_return_unicode_escape", 0, 1, null),
				Arguments.of(
						"prefer_direct_boolean_return_two_statements_on_if_line",
						2,
						2,
						"Run ./gradlew checkstyleFix to auto-fix all 2 violations."
				),
				Arguments.of("control_flow_braces_text_block_body", 0, 1, null),
				Arguments.of("prefer_math_method_multiline_ternary", 0, 1, null),
				Arguments.of("prefer_exact_assertion_comparison_form", 0, 1, null),
				Arguments.of(
						"redundant_plus_unused_import_second_suppressed",
						1,
						2,
						"Run ./gradlew checkstyleFix to auto-fix 1 of 2 violations."
				),
				Arguments.of(
						"redundant_plus_unused_import_pass_through_blank",
						2,
						2,
						"Run ./gradlew checkstyleFix to auto-fix all 2 violations."
				),
				Arguments.of(
						"mixed_one_fixable_one_skipped",
						1,
						2,
						"Run ./gradlew checkstyleFix to auto-fix 1 of 2 violations."
				),
				Arguments.of(
						"mixed_two_fixable_one_skipped",
						2,
						3,
						"Run ./gradlew checkstyleFix to auto-fix 2 of 3 violations."
				),
				Arguments.of(
						"all_fixable_single_violation",
						1,
						1,
						"Run ./gradlew checkstyleFix to auto-fix all 1 violations."
				),
				Arguments.of(
						"all_fixable_multiple_violations",
						2,
						2,
						"Run ./gradlew checkstyleFix to auto-fix all 2 violations."
				),
				Arguments.of("no_violations", 0, 0, null)
		);
	}

	@TempDir
	Path tempDir;

	@MethodSource("hintScenarios")
	@ParameterizedTest(name = "{0}")
	public void hintReflectsActualFixCount(
			@Nonnull String caseName,
			int expectedTotalFixed,
			int expectedTotalViolations,
			@Nullable String expectedHint
	) throws Exception {
		final var file = tempDir.resolve(caseName + ".java").toFile();
		Files.writeString(file.toPath(), TestResources.loadCaseSource(TOPIC, caseName));
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));

		assertEquals(expectedTotalFixed, result[1]);

		final var hint = CheckstyleFixAction.formatHintMessage(result[1], expectedTotalViolations, "checkstyleFix");
		if (expectedHint == null)
			assertNull(hint);
		else
			assertEquals(expectedHint, hint);
	}
}