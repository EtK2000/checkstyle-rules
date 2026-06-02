package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.StringUtil;
import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Runs the full fix pipeline against every {@code // === case: NAME ===}
 * slice in every {@code cases.*.in.java} resource. For each slice the
 * test synthesizes a standalone Java file (package + helper preamble +
 * the slice's translated imports + the slice body) and runs the full
 * fixer pipeline on that file. The pipeline output is then compared to
 * the slice's per-pipeline expected content (the slice's body inside
 * {@code cases.*.fixed.java} when present, falling back to the per-fixer
 * expected in {@code cases.*.out.java}).
 *
 * <p>Per-slice synthesis means each slice is a self-contained file
 * specification: its imports live in {@code // imports:} directives and
 * become real {@code import} statements via {@code TestResources}
 * directive translation. Slices that need a different file-level import
 * context (e.g. JUnit 4 vs JUnit 5 statics) can coexist in the same
 * {@code cases.in.java} because the pipeline runs against a per-slice
 * synthesized file, not the whole {@code cases.in.java} unit.
 *
 * <p>{@code cases.*.fixed.java} keeps its existing slice-only override
 * shape: only slices whose pipeline output differs from the per-fixer
 * {@code cases.*.out.java} need to be listed. Unlisted slices fall
 * through to {@code cases.*.out.java}.
 */
public class FullPipelineRegressionTest {
	private record FixedSlice(@Nonnull List<String> code, @Nonnull Set<String> imports) {}

	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");
	static final String CASE_END_MARKER = "// === end ===";
	static final String CASE_OPEN_PREFIX = "// === case: ";
	static final String CASE_OPEN_SUFFIX = " ===";
	private static final String IMPORTS_DIRECTIVE_PREFIX = "// imports: ";
	private static final String MULTI_FIX_DIRECTIVE = "// multi-fix-expected";
	private static final String TARGET_DIRECTIVE = "// " + "target:";

	/**
	 * Canonicalizes the synthesized expected to match the actual pipeline's
	 * import block layout. Runs {@code UnusedImportsCheck} cleanup, then
	 * finds the existing import block in the file and rewrites it in
	 * canonical layout (statics sorted, blank line, regulars sorted),
	 * preserving the surrounding blank-line structure.
	 */
	@CheckReturnValue
	@Nonnull
	private static String canonicalize(@Nonnull Path workDir, @Nonnull String fileName, @Nonnull String content) throws Exception {
		final var unusedFilteredContent = removeUnusedImports(workDir, fileName, content);
		final var lines = new ArrayList<>(List.of(unusedFilteredContent.split("\n", -1)));
		final var statics = new TreeSet<String>();
		final var regulars = new TreeSet<String>();
		var firstImportIdx = -1;
		var lastImportIdx = -1;
		for (var i = 0; i < lines.size(); ++i) {
			final var trimmed = lines.get(i).trim();
			if (!trimmed.startsWith("import ") || !trimmed.endsWith(";"))
				continue;
			if (firstImportIdx < 0)
				firstImportIdx = i;
			lastImportIdx = i;
			final var fq = trimmed.substring("import ".length(), trimmed.length() - 1).trim();
			if (fq.startsWith("static "))
				statics.add("import " + fq + ";");
			else
				regulars.add("import " + fq + ".;".substring(0, 1) + ";");
		}
		// Recompute regulars cleanly
		regulars.clear();
		for (var line : lines) {
			final var trimmed = line.trim();
			if (trimmed.startsWith("import ") && trimmed.endsWith(";")) {
				final var fq = trimmed.substring("import ".length(), trimmed.length() - 1).trim();
				if (!fq.startsWith("static "))
					regulars.add("import " + fq + ";");
			}
		}
		// Remove specific static imports covered by a wildcard from the same class
		// (mirrors insertMissingImports' wildcard-skip logic; the actual pipeline
		// never adds a specific when a wildcard already covers it)
		final var wildcardPrefixes = new ArrayList<String>();
		for (var imp : statics) {
			final var fq = imp.substring("import static ".length(), imp.length() - 1);
			if (fq.endsWith(".*"))
				wildcardPrefixes.add(fq.substring(0, fq.length() - 1));
		}
		statics.removeIf(imp -> {
			final var fq = imp.substring("import static ".length(), imp.length() - 1);
			if (fq.endsWith(".*"))
				return false;
			for (var prefix : wildcardPrefixes) {
				if (fq.startsWith(prefix))
					return true;
			}
			return false;
		});
		if (firstImportIdx < 0)
			return String.join("\n", lines);
		// Replace the import block (firstImportIdx..lastImportIdx inclusive) with canonical layout.
		// Mirror insertMissingImports' static-branch behavior: when statics are present,
		// the static block is surrounded by blank lines (blank before, blank after);
		// regulars (when present) follow that trailing blank.
		final var importBlock = new ArrayList<String>();
		importBlock.addAll(statics);
		if (!statics.isEmpty() && regulars.isEmpty())
			importBlock.add("");
		else if (!statics.isEmpty())
			importBlock.add("");
		importBlock.addAll(regulars);
		final var before = new ArrayList<>(lines.subList(0, firstImportIdx));
		final var after = new ArrayList<>(lines.subList(lastImportIdx + 1, lines.size()));
		// Drop a leading blank in "after" if importBlock ends with a blank, to avoid double-blank
		if (!importBlock.isEmpty() && importBlock.getLast().isBlank()
				&& !after.isEmpty() && after.getFirst().isBlank())
			after.removeFirst();
		final var combined = new ArrayList<String>();
		combined.addAll(before);
		combined.addAll(importBlock);
		combined.addAll(after);
		return String.join("\n", combined);
	}

	@CheckReturnValue
	@Nonnull
	private static String containerLabel(@Nonnull String topic, @Nonnull String violationFileName) {
		final var variant = extractVariantSuffix(violationFileName);
		return variant.isEmpty() ? topic : topic + "." + variant;
	}

	@CheckReturnValue
	@Nonnull
	private static String extractVariantSuffix(@Nonnull String violationFileName) {
		if (!violationFileName.startsWith("cases.") || !violationFileName.endsWith(".in.java"))
			return "";
		final var start = "cases.".length();
		final var end = violationFileName.length() - ".in.java".length();
		if (end <= start)
			return "";
		return violationFileName.substring(start, end);
	}

	static int findEndMarker(@Nonnull List<String> lines, int from) {
		for (var i = from; i < lines.size(); ++i) {
			if (lines.get(i).trim().equals(CASE_END_MARKER))
				return i;
		}
		return -1;
	}

	static int findExactLine(@Nonnull List<String> lines, @Nonnull String target) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).trim().equals(target))
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private static boolean isOverrideFile(@Nonnull Path p) {
		final var name = p.getFileName().toString();
		return name.startsWith("cases") && name.endsWith(".fixed.java");
	}

	@CheckReturnValue
	private static boolean isViolationFile(@Nonnull Path p) {
		final var name = p.getFileName().toString();
		return name.startsWith("cases.") && name.endsWith(".in.java");
	}

	/**
	 * Loads the per-slice pipeline-expected lines: prefers the slice's
	 * body in {@code cases.<variant>.fixed.java} (override) if present,
	 * otherwise returns the slice's body in {@code cases.<variant>.out.java}.
	 * Both sources have directives translated by {@code TestResources}.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> loadPipelineExpectedSlice(
			@Nonnull String topic,
			@Nonnull String violationFileName,
			@Nonnull String sliceName
	) throws IOException, URISyntaxException {
		final var fixedFileName = StringUtil.replaceSuffix(violationFileName, ".in.java", ".fixed.java");
		final var fixedPath = INPUTS_ROOT.resolve(topic).resolve(fixedFileName);
		if (Files.exists(fixedPath)) {
			final var sliced = sliceCaseTranslated(Files.readAllLines(fixedPath), sliceName);
			if (sliced != null)
				return sliced;
		}
		final var variantSuffix = extractVariantSuffix(violationFileName);
		final var slice = TestResources.loadCaseSlice(topic, sliceName, variantSuffix);
		return slice.fixedLines();
	}

	@CheckReturnValue
	@Nonnull
	private static Map<String, FixedSlice> parseFixedSlices(@Nonnull List<String> lines) {
		final var slices = new LinkedHashMap<String, FixedSlice>();
		var i = 0;
		while (i < lines.size()) {
			final var trimmed = lines.get(i).trim();
			if (!trimmed.startsWith(CASE_OPEN_PREFIX) || !trimmed.endsWith(CASE_OPEN_SUFFIX)) {
				++i;
				continue;
			}
			final var caseName = trimmed.substring(CASE_OPEN_PREFIX.length(), trimmed.length() - CASE_OPEN_SUFFIX.length());
			final var end = findEndMarker(lines, i + 1);
			if (end < 0)
				throw new IllegalStateException("Case '" + caseName + "' is missing its end marker");
			final var code = new ArrayList<String>();
			final var imports = new TreeSet<String>();
			for (var k = i + 1; k < end; ++k) {
				final var line = lines.get(k);
				final var content = line.trim();
				if (content.startsWith(IMPORTS_DIRECTIVE_PREFIX))
					imports.add(content.substring(IMPORTS_DIRECTIVE_PREFIX.length()).trim());
				else if (!content.startsWith(TARGET_DIRECTIVE) && !content.startsWith(MULTI_FIX_DIRECTIVE))
					code.add(line);
			}
			slices.put(caseName, new FixedSlice(List.copyOf(code), Set.copyOf(imports)));
			i = end + 1;
		}
		return slices;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> readPreamble(@Nonnull String topic, @Nonnull String violationFileName) throws IOException {
		final var path = INPUTS_ROOT.resolve(topic).resolve(violationFileName);
		final var lines = Files.readAllLines(path);
		final var preamble = new ArrayList<String>();
		for (var line : lines) {
			if (line.trim().startsWith(CASE_OPEN_PREFIX))
				break;
			preamble.add(line);
		}
		while (!preamble.isEmpty() && preamble.getLast().isBlank())
			preamble.removeLast();
		return preamble;
	}

	@CheckReturnValue
	@Nonnull
	private static String removeUnusedImports(@Nonnull Path workDir, @Nonnull String fileName, @Nonnull String content) throws Exception {
		final var unusedImportsFixerName = UnusedImportsCheck.class.getName();
		final var redundantImportFixerName = RedundantImportCheck.class.getName();
		final var importCleanupFixers = new HashMap<String, CheckstyleFixer>();
		final var unusedFixer = CheckstyleFixAction.FIXERS.get(unusedImportsFixerName);
		if (unusedFixer != null)
			importCleanupFixers.put(unusedImportsFixerName, unusedFixer);
		final var redundantFixer = CheckstyleFixAction.FIXERS.get(redundantImportFixerName);
		if (redundantFixer != null)
			importCleanupFixers.put(redundantImportFixerName, redundantFixer);
		if (importCleanupFixers.isEmpty())
			return content;
		final var file = workDir.resolve(fileName).toFile();
		final var lines = new ArrayList<>(List.of(content.split("\n", -1)));
		Files.writeString(file.toPath(), String.join("\n", lines));
		final var violations = FullPipelineRunner.runImportCleanupChecks(file);
		final var cleanupOnly = new ArrayList<>(violations.stream()
				.filter(v -> importCleanupFixers.containsKey(v.getSourceName()))
				.toList());
		CheckstyleFixAction.applyFixes(lines, cleanupOnly, importCleanupFixers);
		return String.join("\n", lines);
	}

	@CheckReturnValue
	@Nullable
	private static List<String> sliceCaseTranslated(@Nonnull List<String> allLines, @Nonnull String caseName) {
		final var openMarker = CASE_OPEN_PREFIX + caseName + CASE_OPEN_SUFFIX;
		var start = -1;
		for (var i = 0; i < allLines.size(); ++i) {
			if (allLines.get(i).trim().equals(openMarker)) {
				start = i + 1;
				break;
			}
		}
		if (start < 0)
			return null;
		for (var i = start; i < allLines.size(); ++i) {
			if (allLines.get(i).trim().equals(CASE_END_MARKER))
				return TestResources.translateDirectives(allLines.subList(start, i));
		}
		throw new IllegalStateException("Case '" + caseName + "' has open marker but no '" + CASE_END_MARKER + "' terminator");
	}

	/**
	 * Drops slice-test metadata directive lines from {@code content}: the
	 * {@code // imports: <fqcn>} directive consumed by
	 * {@code TestResources.loadCaseSlice} (translated to real {@code import X;}
	 * lines) and {@code FixerTestUtil.assertCaseFix} (diffed input vs fixed
	 * to derive expected {@code importsToAdd}), and the
	 * {@code // multi-fix-expected} opt-in directive consumed by
	 * {@code StandardFixerCases.casesFor} (gates the {@code fix-multi}
	 * sub-test for multi-violation slices). The full pipeline doesn't
	 * recognize either directive, so the in/out comment counts differ;
	 * strip both sides before comparing.
	 */
	@CheckReturnValue
	@Nonnull
	static String stripImportsDirectives(@Nonnull String content) {
		final var lines = content.split("\n", -1);
		final var kept = new ArrayList<String>(lines.length);
		for (var line : lines) {
			final var trimmed = line.trim();
			if (!trimmed.startsWith(IMPORTS_DIRECTIVE_PREFIX) && !trimmed.startsWith(MULTI_FIX_DIRECTIVE))
				kept.add(line);
		}
		return String.join("\n", kept);
	}

	/**
	 * Synthesizes a standalone Java file for the given slice. The slice's
	 * translated imports are placed BEFORE any helper type declarations from
	 * the preamble to satisfy Java's "imports before types" rule. Layout:
	 * {@code package; slice-imports; preamble-helpers; slice-non-imports}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String synthesizeSliceFile(@Nonnull List<String> preamble, @Nonnull List<String> sliceContent) {
		final var packageLines = new ArrayList<String>();
		final var helperLines = new ArrayList<String>();
		var seenNonBlank = false;
		for (var line : preamble) {
			final var trimmed = line.trim();
			if (!seenNonBlank && trimmed.isEmpty()) {
				packageLines.add(line);
				continue;
			}
			if (trimmed.startsWith("package ")) {
				packageLines.add(line);
				seenNonBlank = true;
				continue;
			}
			helperLines.add(line);
			seenNonBlank = true;
		}
		// A slice that declares its own package (via the `// package:` directive) replaces the
		// preamble's package; keeping both would emit two `package` declarations and fail to parse.
		// Only divert when the preamble supplies a package (the collision case): a slice that
		// legitimately carries a package line as body content (e.g. an annotated package declaration)
		// under a package-less preamble must keep it in place, not hoisted above its annotation.
		final var preambleHasPackage = !packageLines.isEmpty();
		final var slicePackageLines = new ArrayList<String>();
		final var sliceImports = new ArrayList<String>();
		final var sliceRest = new ArrayList<String>();
		// Only hoist REAL import declarations. A line that reads like `import ...;` but begins
		// inside a block comment or text block is body content (e.g. a masked wildcard a fixer
		// must ignore); hoisting it would inject a real import that changes pipeline behavior.
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var line : sliceContent) {
			final var trimmed = line.trim();
			final var insideCommentOrTextBlock = lexer.inBlockComment() || lexer.inTextBlock();
			lexer = JavaLineScanner.stateAfter(line, lexer);
			if (preambleHasPackage && !insideCommentOrTextBlock && trimmed.startsWith("package "))
				slicePackageLines.add(line);
			else if (!insideCommentOrTextBlock && trimmed.startsWith("import ") && trimmed.endsWith(";"))
				sliceImports.add(line);
			else
				sliceRest.add(line);
		}
		if (!slicePackageLines.isEmpty()) {
			packageLines.clear();
			packageLines.addAll(slicePackageLines);
		}
		while (!sliceRest.isEmpty() && sliceRest.getFirst().isBlank())
			sliceRest.removeFirst();
		while (!sliceImports.isEmpty() && sliceImports.getLast().isBlank())
			sliceImports.removeLast();
		while (!helperLines.isEmpty() && helperLines.getFirst().isBlank())
			helperLines.removeFirst();
		while (!helperLines.isEmpty() && helperLines.getLast().isBlank())
			helperLines.removeLast();
		final var out = new ArrayList<String>();
		out.addAll(packageLines);
		while (!out.isEmpty() && out.getLast().isBlank())
			out.removeLast();
		if (!sliceImports.isEmpty()) {
			out.add("");
			out.addAll(sliceImports);
		}
		if (!helperLines.isEmpty()) {
			out.add("");
			out.addAll(helperLines);
		}
		if (!sliceRest.isEmpty()) {
			if (sliceImports.isEmpty() && helperLines.isEmpty())
				out.add("");
			else if (!helperLines.isEmpty())
				out.add("");
			out.addAll(sliceRest);
		}
		return String.join("\n", out);
	}

	@TempDir
	Path tempDir;

	/**
	 * Explains a pipeline-output drift by re-running the checks on the fixed
	 * output: any violation that still fires there is a case where a check
	 * fired but no fixer changed the line (a swallowed skip). Returns the
	 * residual violations plus the fixers' own skip reasons, so a drift caused
	 * by an intentional {@code SkipResult} (e.g. an alias whose local name
	 * clashes with another field) surfaces its reason in the failure message
	 * instead of leaving an unexplained text diff. Computed lazily, only when
	 * the assertion fails.
	 */
	@CheckReturnValue
	@Nonnull
	private String diagnoseUnfixedViolations(@Nonnull File fixedFile) {
		try {
			final var residual = FullPipelineRunner.runChecks(fixedFile, String.valueOf(Integer.MAX_VALUE));
			if (residual.isEmpty())
				return "";
			final var lines = new ArrayList<>(Files.readAllLines(fixedFile.toPath()));
			final var skipReasons = CheckstyleFixAction.applyFixes(
					lines, residual, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS
			).skippedReasons();
			final var sb = new StringBuilder(
					"\n  a check still fires on the fixed output but no fixer changed it (swallowed skip):"
			);
			for (var event : residual) {
				sb.append("\n    ").append(event.getSourceName())
						.append(" @ line ").append(event.getLine())
						.append(": ").append(event.getMessage());
			}
			if (!skipReasons.isEmpty())
				sb.append("\n  fixer skip reasons: ").append(skipReasons);
			return sb.toString();
		}
		catch (Exception e) {
			return "\n  (skip diagnostic failed: " + e + ")";
		}
	}

	/**
	 * Guards that, within one topic, two {@code cases.out*.java} fixed-slice
	 * files never declare different {@code // imports:} directives for a case
	 * whose fixed code is byte-identical between them. Identical fixed code
	 * needs identical imports, so a divergence means one file's directive is
	 * stale or missing.
	 *
	 * <p>Catches the inert-but-latent trap where a base {@code cases.out.java}
	 * slice omits the directive its gated-on {@code cases.out.<variant>.java}
	 * sibling carries: {@code assertCaseFix} reads the variant file and
	 * {@link #stripImportsDirectives} drops directives, so no test fails today,
	 * yet a future property-less entry would fall back to the base file and
	 * compute the wrong expected {@code importsToAdd}. A gated-off variant
	 * (e.g. {@code cases.out.minSdk-18.java}) legitimately omits the directive
	 * because its fixed code is the unfixed input, which differs from the
	 * gated-on code, so this guard does not compare them.
	 */
	@Test
	public void fixedSlicesWithIdenticalCodeDeclareIdenticalImports() throws IOException {
		final var failures = new ArrayList<String>();
		try (var topics = Files.newDirectoryStream(INPUTS_ROOT, Files::isDirectory)) {
			for (var topicDir : topics) {
				final var fixedFiles = new ArrayList<Path>();
				try (var stream = Files.list(topicDir)) {
					stream
							.filter(p -> {
								final var name = p.getFileName().toString();
								return name.startsWith("cases.out.") && name.endsWith(".java");
							})
							.sorted()
							.forEach(fixedFiles::add);
				}
				final var parsed = new ArrayList<Map<String, FixedSlice>>();
				for (var file : fixedFiles)
					parsed.add(parseFixedSlices(Files.readAllLines(file)));
				for (var a = 0; a < fixedFiles.size(); ++a) {
					for (var b = a + 1; b < fixedFiles.size(); ++b) {
						for (var entry : parsed.get(a).entrySet()) {
							final var other = parsed.get(b).get(entry.getKey());
							if (other == null || !entry.getValue().code().equals(other.code()) || entry.getValue().imports().equals(other.imports()))
								continue;
							failures.add(
									topicDir.getFileName() + "/" + entry.getKey()
											+ ": identical fixed code but '// imports:' differ between "
											+ fixedFiles.get(a).getFileName() + " " + entry.getValue().imports()
											+ " and " + fixedFiles.get(b).getFileName() + " " + other.imports()
							);
						}
					}
				}
			}
		}
		assertEquals(List.of(), failures, "Fixed slices with identical code must declare identical '// imports:' directives");
	}

	@Test
	public void overrideCasesMustDifferFromBase() throws IOException {
		final var failures = new ArrayList<String>();
		try (var topics = Files.newDirectoryStream(INPUTS_ROOT, Files::isDirectory)) {
			for (var topicDir : topics) {
				try (var stream = Files.list(topicDir)) {
					for (var overridePath : stream.filter(FullPipelineRegressionTest::isOverrideFile).sorted().toList()) {
						final var basePath = overridePath.resolveSibling(
								overridePath.getFileName().toString().replace(".fixed.java", ".out.java")
						);
						if (!Files.exists(basePath))
							continue;
						final var baseLines = Files.readAllLines(basePath);
						final var overrideLines = Files.readAllLines(overridePath);
						for (var i = 0; i < overrideLines.size(); ++i) {
							final var trimmed = overrideLines.get(i).trim();
							if (!trimmed.startsWith(CASE_OPEN_PREFIX) || !trimmed.endsWith(CASE_OPEN_SUFFIX))
								continue;
							final var overrideEnd = findEndMarker(overrideLines, i + 1);
							if (overrideEnd < 0) {
								failures.add(
										topicDir.getFileName() + "/" + overridePath.getFileName()
												+ ": override case " + trimmed
												+ " missing '" + CASE_END_MARKER + "' terminator"
								);
								break;
							}
							final var baseStart = findExactLine(baseLines, trimmed);
							if (baseStart >= 0) {
								final var baseEnd = findEndMarker(baseLines, baseStart + 1);
								if (baseEnd < 0) {
									failures.add(
											topicDir.getFileName() + "/" + basePath.getFileName()
													+ ": base case " + trimmed
													+ " missing '" + CASE_END_MARKER + "' terminator"
									);
								}
								else {
									final var overrideContent = String.join("\n", overrideLines.subList(i, overrideEnd + 1));
									final var baseContent = String.join("\n", baseLines.subList(baseStart, baseEnd + 1));
									if (overrideContent.equals(baseContent)) {
										failures.add(
												topicDir.getFileName() + "/" + overridePath.getFileName()
														+ ": override case " + trimmed
														+ " equals base, drop the override"
										);
									}
								}
							}
							i = overrideEnd + 1;
						}
					}
				}
			}
		}
		assertEquals(List.of(), failures, "Override slices must diverge from base output");
	}

	private void runSlice(
			@Nonnull String topic,
			@Nonnull String violationFileName,
			@Nonnull String sliceName
	) throws Exception {
		final var inputPreamble = readPreamble(topic, violationFileName);
		final var variantSuffix = extractVariantSuffix(violationFileName);
		final var slice = TestResources.loadCaseSlice(topic, sliceName, variantSuffix);
		final var synthesizedInput = synthesizeSliceFile(inputPreamble, slice.inputLines());
		final var strippedInput = FullPipelineRunner.stripViolationComments(synthesizedInput);
		final var fileName = topic + "__" + StringUtil.replaceSuffix(violationFileName, ".in.java", "__" + sliceName + ".java");
		final var file = tempDir.resolve(fileName).toFile();
		Files.writeString(file.toPath(), strippedInput);
		final var pipelineOutput = FullPipelineRunner.runFixToFixedPoint(file, String.valueOf(Integer.MAX_VALUE));

		final var expectedSliceContent = loadPipelineExpectedSlice(topic, violationFileName, sliceName);
		final var expectedPreamble = readPreamble(topic, StringUtil.replaceSuffix(violationFileName, ".in.java", ".out.java"));
		final var rawExpected = synthesizeSliceFile(expectedPreamble, expectedSliceContent);
		final var expectedFileName = topic + "__" + StringUtil.replaceSuffix(violationFileName, ".in.java", "__" + sliceName + "_expected.java");
		final var synthesizedExpected = canonicalize(tempDir, expectedFileName, rawExpected);
		final var actualFileName = topic + "__" + StringUtil.replaceSuffix(violationFileName, ".in.java", "__" + sliceName + "_actual.java");
		final var canonicalActual = canonicalize(tempDir, actualFileName, pipelineOutput);
		assertEquals(
				FullPipelineRunner.stripViolationComments(stripImportsDirectives(synthesizedExpected)),
				FullPipelineRunner.stripViolationComments(stripImportsDirectives(canonicalActual)),
				() -> "Pipeline output drifted for " + topic + "/" + violationFileName + "::" + sliceName
						+ diagnoseUnfixedViolations(file)
		);
	}

	@TestFactory
	public Stream<DynamicNode> tests() throws IOException {
		final var containers = new ArrayList<DynamicNode>();
		try (var topics = Files.newDirectoryStream(INPUTS_ROOT, Files::isDirectory)) {
			for (var topicDir : topics) {
				final var topic = topicDir.getFileName().toString();
				try (var stream = Files.list(topicDir)) {
					for (var path : stream.filter(FullPipelineRegressionTest::isViolationFile).sorted().toList()) {
						final var violationFileName = path.getFileName().toString();
						final var children = new ArrayList<DynamicNode>();
						for (var sliceName : TestResources.caseNamesIn(Files.readAllLines(path)))
							children.add(dynamicTest(sliceName, () -> runSlice(topic, violationFileName, sliceName)));
						containers.add(dynamicContainer(containerLabel(topic, violationFileName), path.toUri(), children.stream()));
					}
				}
			}
		}
		return containers.stream();
	}
}