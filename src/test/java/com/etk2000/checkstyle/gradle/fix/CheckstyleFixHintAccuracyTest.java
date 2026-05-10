package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
 * (totalFixed — the value the hint uses) and the post-fix
 * {@code formatHintMessage} output.
 *
 * <p>FIX_BOUNDS (applyFixes lines 220-222) is not exercised here — Checkstyle
 * bounds-checks events to the file, so {@code lineIndex} out-of-range cannot
 * be reached through {@code doExecute}. Direct {@code applyFixes} unit-style
 * coverage of that branch lives in {@link CheckstyleFixUtilTest}.
 *
 * <p>FIX_NO_FIXER and FIX_SEVERITY are out of scope because the hint
 * denominator is the full violation count, not a speculative fixable count.
 */
public class CheckstyleFixHintAccuracyTest {
	private static Stream<Arguments> hintScenarios() {
		return Stream.of(
				// PreferDirectBooleanReturnFixer: SkipResult("multi-line if condition")
				Arguments.of(
						"preferDirectBooleanReturn_multilineCondition",
						"class T {\n\tboolean f(boolean a, boolean b) {\n\t\tif (a\n\t\t\t\t&& b) return true;\n\t\treturn false;\n\t}\n}",
						0,
						1,
						null
				),

				// PreferDirectBooleanReturnFixer: SkipResult("Unicode escape in condition")
				Arguments.of(
						"preferDirectBooleanReturn_unicodeEscape",
						"class T {\n\tboolean f(char c) {\n\t\tif (c == '\\u0041') return true;\n\t\treturn false;\n\t}\n}",
						0,
						2,
						null
				),

				// PreferDirectBooleanReturnFixer: returns null (parseBody's switch
				// has no case for "return true; return false;" on the if line).
				// ControlFlowBraces also fires on the one-liner `if (a) return true;`
				// (also SkipResult).
				Arguments.of(
						"preferDirectBooleanReturn_twoStatementsOnIfLine",
						"class T {\n\tboolean f(boolean a) {\n\t\tif (a) return true; return false;\n\t}\n}",
						0,
						2,
						null
				),

				// ControlFlowBracesFixer: SkipResult("one-liner or braced for-loop")
				Arguments.of(
						"controlFlowBraces_oneLinerIf",
						"class T {\n\tvoid f(int x) {\n\t\tif (x > 0) --x;\n\t}\n}",
						0,
						1,
						null
				),

				// PreferMathMethodFixer: SkipResult("parenthesized or multiline ternary")
				Arguments.of(
						"preferMathMethod_multilineTernary",
						"class T {\n\tint f(int a, int b) {\n\t\treturn a > b\n\t\t\t? a : b;\n\t}\n}",
						0,
						1,
						null
				),

				// PreferExactAssertionFixer: SkipResult (assertTrue+comparison, not instanceof)
				Arguments.of(
						"preferExactAssertion_comparisonForm",
						"import org.junit.jupiter.api.Assertions;\nclass T {\n\tvoid f(Object a, Object b) {\n\t\tAssertions.assertTrue(a == b);\n\t}\n}",
						0,
						1,
						null
				),

				// applyFixes FIX_SUPPRESSED branch: RedundantImport + UnusedImports
				// both fire on the same line. First DeleteLineFixer removes the import
				// line; the line that shifts into its position is the class declaration
				// (not empty), so the pass-through arm at lines 209-212 fails and the
				// second violation is suppressed.
				Arguments.of(
						"redundantPlusUnusedImport_secondSuppressed",
						"import java.lang.String;\nclass T {}",
						1,
						2,
						"Run ./gradlew checkstyleFix to auto-fix 1 of 2 violations."
				),

				// applyFixes pass-through arm at lines 209-212: same as the suppressed
				// row above, but the line that shifts in is blank, so the second
				// DeleteLineFixer is allowed through and both violations fix.
				Arguments.of(
						"redundantPlusUnusedImport_passThroughBlank",
						"import java.lang.String;\n\nclass T {}",
						2,
						2,
						"Run ./gradlew checkstyleFix to auto-fix all 2 violations."
				),

				// Mixed partial: 1 fixable + 1 skipped — boundary "1 of N"
				Arguments.of(
						"mixed_oneFixableOneSkipped",
						"class T {\n\tint x = 0;\n\tvoid f(int y) {\n\t\tif (y > 0) --y;\n\t}\n}",
						1,
						2,
						"Run ./gradlew checkstyleFix to auto-fix 1 of 2 violations."
				),

				// Mixed partial: 2 fixable + 1 skipped — boundary "N>1 of M"
				Arguments.of(
						"mixed_twoFixableOneSkipped",
						"class T {\n\tint x = 0;\n\tint y = 0;\n\tvoid f(int z) {\n\t\tif (z > 0) --z;\n\t}\n}",
						2,
						3,
						"Run ./gradlew checkstyleFix to auto-fix 2 of 3 violations."
				),

				// All-fixable, single — boundary "all 1"
				Arguments.of(
						"allFixable_singleViolation",
						"class T {\n\tint x = 0;\n}",
						1,
						1,
						"Run ./gradlew checkstyleFix to auto-fix all 1 violations."
				),

				// All-fixable, multiple — boundary "all N>1"
				Arguments.of(
						"allFixable_multipleViolations",
						"class T {\n\tint x = 0;\n\tint y = 0;\n}",
						2,
						2,
						"Run ./gradlew checkstyleFix to auto-fix all 2 violations."
				),

				// Zero-violation negative control
				Arguments.of(
						"noViolations",
						"class T {\n\tvoid m() {}\n}",
						0,
						0,
						null
				)
		);
	}

	@TempDir
	Path tempDir;

	@MethodSource("hintScenarios")
	@ParameterizedTest(name = "{0}")
	public void hintReflectsActualFixCount(
			@Nonnull String name,
			@Nonnull String source,
			int expectedTotalFixed,
			int expectedTotalViolations,
			@Nullable String expectedHint
	) throws Exception {
		final var file = tempDir.resolve(name + ".java").toFile();
		Files.writeString(file.toPath(), source);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));

		assertEquals(expectedTotalFixed, result[1], "totalFixed (the value the hint should use)");

		final var hint = CheckstyleFixAction.formatHintMessage(result[1], expectedTotalViolations, "checkstyleFix");
		if (expectedHint == null)
			assertNull(hint);
		else
			assertEquals(expectedHint, hint);
	}
}