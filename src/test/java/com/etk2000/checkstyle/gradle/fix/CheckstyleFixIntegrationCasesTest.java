package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Dynamically fixes every {@code integration} fixture the way the fix task does, pass after
 * pass up to {@link CheckstyleFixAction#MAX_FIX_PASSES}, and asserts
 * its output equals the {@code .fixed} slice (or the input, for a no-op case)
 * with no fixable violations left. Adding a new integration case needs no new
 * test method here; only cases that assert more than input-to-output (skip
 * reasons, minSdk gating, dry-run counts, {@code doExecute} mechanics, suppressions) keep a
 * dedicated method in {@link CheckstyleFixIntegrationTest} or {@link CheckstyleFixSuppressionsTest}.
 *
 * <p>{@code fixCount} is intentionally not asserted (the final content plus a
 * zero-fixable-residual check is the real invariant). A case that needs more
 * than one fixing pass (more than two passes, the last always finding nothing to
 * fix) is not a failure but prints a {@code FIXME} line, since the goal is for
 * one fixing pass to be enough.
 */
public class CheckstyleFixIntegrationCasesTest {
	// Cases a dedicated CheckstyleFixIntegrationTest method already covers in a
	// way this uniform factory can't: jit_inefficiency_to_array_sized is minSdk
	// dependent (its output differs from the MIN_SDK run here); the other three
	// have no .fixed slice because they assert doExecute/verify mechanics, not a
	// fixed-point output, so there is nothing for the factory to compare against.
	private static final Set<String> EXCLUDED = Set.of(
			"jit_inefficiency_to_array_sized",
			"do_execute_charset_for_name",
			"e2e_dry_vs_normal_run_fixable_count",
			"verify_clean_handles_multi_pass_stabilization"
	);
	private static final String MIN_SDK = String.valueOf(Integer.MAX_VALUE);

	@TempDir
	Path tempDir;

	private void assertNoFixableResidual(@Nonnull File file, @Nonnull String caseName) throws Exception {
		final var violations = FullPipelineRunner.runChecks(file, MIN_SDK);
		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(
				lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS
		);
		assertEquals(0, result.fixCount(), caseName + ": fixable violations remain after convergence");
	}

	@Test
	void everyExcludedCaseHasDedicatedMethod() throws Exception {
		final var integrationTestSource = Files.readString(Path.of(
				"src/test/java/com/etk2000/checkstyle/gradle/fix/CheckstyleFixIntegrationTest.java"
		));
		for (var caseName : EXCLUDED) {
			assertTrue(
					integrationTestSource.contains('"' + caseName + '"'),
					"EXCLUDED integration case has no dedicated CheckstyleFixIntegrationTest method: " + caseName
			);
		}
	}

	@Execution(ExecutionMode.CONCURRENT)
	@TestFactory
	Stream<DynamicTest> integrationCases() {
		return TestResources.caseNames("integration").stream()
				.filter(name -> !EXCLUDED.contains(name))
				.map(name -> dynamicTest(name, () -> runCase(name)));
	}

	private void runCase(@Nonnull String caseName) throws Exception {
		final var fx = TestResources.loadCase("integration", caseName);
		final var expected = String.join("\n", fx.hasFixed() ? fx.fixedLines() : fx.inputLines());
		final var file = tempDir.resolve(caseName + ".java").toFile();
		Files.writeString(file.toPath(), String.join("\n", fx.inputLines()));

		final var passes = CheckstyleFixAction.fixUntilSettled(CheckstyleFixAction.createCheckerConfig(MIN_SDK), List.of(file), CheckstyleFixAction.MAX_FIX_PASSES);

		assertEquals(expected, Files.readString(file.toPath()), caseName + ": fixed content");
		assertNoFixableResidual(file, caseName);
		assertNotEquals(CheckstyleFixAction.MAX_FIX_PASSES, passes, caseName + ": did not settle before the last allowed pass");
		if (passes > 2) {
			// FIXME: make one fixing pass enough so this drops to two passes.
			System.err.println("[second-pass] integration/" + caseName + " converged in " + passes + " passes");
		}
	}
}