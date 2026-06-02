package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.CheckerCache;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared full-pipeline driver for integration-style tests. Delegates to
 * {@link CheckstyleFixAction#createCheckerConfig} so the test path exercises
 * the same Checkstyle configuration that the production fix task uses.
 */
public final class FullPipelineRunner {
	private static final Pattern VIOLATION_MARKER_PREFIX = Pattern.compile("// violation(?:\\s*\\(warning\\))?(?:\\s*\\[[^\\]]+\\])?(?:\\s*@opener)?\\s*:");

	@CheckReturnValue
	@Nonnull
	private static DefaultConfiguration importCleanupConfig() {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", String.valueOf(LineLength.TAB_WIDTH));
		treeWalkerConfig.addChild(new DefaultConfiguration(RedundantImportCheck.class.getName()));
		treeWalkerConfig.addChild(new DefaultConfiguration(UnusedImportsCheck.class.getName()));
		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);
		return checkerConfig;
	}

	@CheckReturnValue
	@Nonnull
	private static String numberLines(@Nonnull String content) {
		final var lines = content.split("\n", -1);
		final var sb = new StringBuilder();
		for (var i = 0; i < lines.length; ++i)
			sb.append(i + 1).append('\t').append(lines[i]).append('\n');
		return sb.toString();
	}

	@CheckReturnValue
	@Nonnull
	public static List<AuditEvent> runChecks(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		return CheckerCache.process(
				CheckstyleFixAction.createCheckerConfig(minSdk),
				FullPipelineRunner.class.getClassLoader(),
				file
		);
	}

	@CheckReturnValue
	@Nonnull
	public static String runFixToFixedPoint(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		final var lines = new ArrayList<>(CheckstyleFixAction.readSourceLines(file.toPath()));
		for (var pass = 0; pass < 10; ++pass) {
			final var current = String.join("\n", lines);
			Files.writeString(file.toPath(), current);
			final List<AuditEvent> violations;
			try {
				violations = runChecks(file, minSdk);
			}
			catch (Exception e) {
				throw new IllegalStateException(
						"A fixer produced unparseable Java on pass " + pass
								+ " (checkstyle could not re-parse the pipeline output). Offending content:\n"
								+ numberLines(current),
						e
				);
			}
			if (CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS).fixCount() == 0)
				break;
		}
		return String.join("\n", lines);
	}

	/**
	 * Runs only {@link UnusedImportsCheck} and {@link RedundantImportCheck}.
	 * Equivalent to running the full ruleset and keeping just those two
	 * checks' violations (they are independent TreeWalker checks), but skips
	 * instantiating and visiting the rest of the modules.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<AuditEvent> runImportCleanupChecks(@Nonnull File file) throws Exception {
		return CheckerCache.process(importCleanupConfig(), FullPipelineRunner.class.getClassLoader(), file);
	}

	/**
	 * Strips {@code // violation: ...} marker text from each line, leaving
	 * only the code prefix. Uses {@code Matcher.find()} so the regex can
	 * match anywhere on the line.
	 * <p>Limitation: if a line embeds the marker text inside a string
	 * literal (e.g. {@code String s = "// violation: foo";}), the strip
	 * truncates the line at the literal's opening, producing corrupt
	 * Java. No existing {@code cases.*.java} resource carries marker
	 * text inside a string literal or text block, so the limitation is
	 * currently inert. The same limitation applies symmetrically to the
	 * comparison in {@code FullPipelineRegressionTest}: both sides get
	 * stripped, so the assertion still passes on corrupt-but-equal
	 * strings, a false-pass risk if a future resource adds such
	 * a literal.
	 */
	@CheckReturnValue
	@Nonnull
	public static String stripViolationComments(@Nonnull String content) {
		final var lines = content.split("\n", -1);
		for (var i = 0; i < lines.length; ++i) {
			final var m = VIOLATION_MARKER_PREFIX.matcher(lines[i]);
			if (!m.find())
				continue;
			var end = m.start();
			while (end > 0 && (lines[i].charAt(end - 1) == ' ' || lines[i].charAt(end - 1) == '\t'))
				--end;
			lines[i] = lines[i].substring(0, end);
		}
		return String.join("\n", lines);
	}

	private FullPipelineRunner() {
	}
}