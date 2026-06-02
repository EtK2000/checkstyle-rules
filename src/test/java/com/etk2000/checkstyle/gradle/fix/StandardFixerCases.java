package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseFixMultiViolation;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseFixMultiViolationSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.collectImportFqcns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.TestResources.CaseSlice;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import org.junit.jupiter.api.DynamicNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class StandardFixerCases {
	private static final int MAX_FIX_PASSES = 10;
	private static final String CASE_MARKER_PREFIX = "// === case:";
	private static final String IMPORT_PREFIX = "import ";
	private static final String INPUTS_BASE = "/com/etk2000/checkstyle/inputs/";
	private static final String PACKAGE_PREFIX = "package ";
	private static final String SKIP_REASON_PREFIX = "// skip-reason: ";

	/**
	 * Asserts that running only {@code checkClass}'s own fixer over its
	 * {@code cleanFileName} leaves the content unchanged. Clean fixtures are clean
	 * for the check/fixer they exercise (not the whole pipeline), so the check
	 * reports nothing and its fixer must be a no-op. This is what lets parsable,
	 * check-silent inputs live in {@code cases.clean.java} instead of
	 * {@code fragments.in.java} and still have their fixer's no-op behaviour
	 * verified. Topics without a registered fixer or clean file are skipped.
	 */
	public static void assertCleanFixerFixedPoint(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull Map<String, String> properties,
			@Nonnull String topic,
			@Nonnull String cleanFileName
	) throws Exception {
		final var fixer = CheckstyleFixAction.FIXERS.get(checkClass.getName());
		if (fixer == null)
			return;
		final var url = StandardFixerCases.class.getResource(INPUTS_BASE + topic + "/" + cleanFileName);
		if (url == null)
			return;
		final var original = Files.readString(Path.of(url.toURI()));
		final var propertyArgs = PropertiesUtil.propertiesAsArray(properties);
		final var fixers = Map.of(checkClass.getName(), fixer);
		final var lines = new ArrayList<>(List.of(original.split("\n", -1)));
		for (var pass = 0; pass < MAX_FIX_PASSES; ++pass) {
			final var violations = BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), propertyArgs);
			final var result = CheckstyleFixAction.applyFixes(lines, violations, fixers);
			if (result.fixCount() == 0 && !result.needsSecondPass())
				break;
		}
		assertEquals(
				original,
				String.join("\n", lines),
				checkClass.getSimpleName() + "'s fixer modified its clean fixture " + topic + "/" + cleanFileName + " (not a fixed point)"
		);
	}

	@CheckReturnValue
	@Nonnull
	public static Stream<DynamicNode> casesFor(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			boolean hasFixer
	) {
		return casesFor(checkClass, hasFixer, Map.of());
	}

	@CheckReturnValue
	@Nonnull
	public static Stream<DynamicNode> casesFor(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			boolean hasFixer,
			@Nonnull Map<String, String> properties
	) {
		return casesFor(checkClass, hasFixer, properties, BaseCheckTest.deriveTopic(checkClass));
	}

	@CheckReturnValue
	@Nonnull
	public static Stream<DynamicNode> casesFor(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			boolean hasFixer,
			@Nonnull Map<String, String> properties,
			@Nonnull String topic
	) {
		return casesFor(checkClass, hasFixer, properties, topic, "");
	}

	@CheckReturnValue
	@Nonnull
	public static Stream<DynamicNode> casesFor(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			boolean hasFixer,
			@Nonnull Map<String, String> properties,
			@Nonnull String topic,
			@Nonnull String fixtureKey
	) {
		final CheckstyleFixer fixer;
		if (hasFixer) {
			fixer = CheckstyleFixAction.FIXERS.get(checkClass.getName());
			if (fixer == null) {
				throw new IllegalStateException(
						"No fixer registered in CheckstyleFixAction.FIXERS for: " + checkClass.getName()
				);
			}
		}
		else
			fixer = null;
		final var propSuffix = PropertiesUtil.variantSuffix(properties);
		final var variantSuffix = combineSuffix(propSuffix, fixtureKey);
		final var inputFileName = resolveInputFileName(topic, propSuffix, fixtureKey);
		if (inputFileName == null)
			return Stream.empty();
		final var filePrefix = readFilePackageAndImports(topic, inputFileName);
		final var propertyArgs = PropertiesUtil.propertiesAsArray(properties);
		final var nodes = new ArrayList<DynamicNode>();
		for (var name : readCaseNames(topic, inputFileName)) {
			final var slice = loadSlice(topic, name, variantSuffix);
			final var sliceLines = slice.inputLines();
			// A slice carrying its own `// package:` directive supplies a package line in
			// its input body; prepending the file-level package too would produce a second
			// `package` declaration and fail to parse. Drop the file-level package for those
			// slices (file-level imports, if any, are still prepended).
			final var sliceDeclaresPackage = sliceLines.stream().anyMatch(l -> l.strip().startsWith(PACKAGE_PREFIX));
			final var effectivePrefix = sliceDeclaresPackage
					? filePrefix.stream().filter(l -> !l.strip().startsWith(PACKAGE_PREFIX)).toList()
					: filePrefix;
			final var contentForViolations = new ArrayList<String>(effectivePrefix.size() + sliceLines.size());
			contentForViolations.addAll(effectivePrefix);
			contentForViolations.addAll(sliceLines);
			final var perCase = new ArrayList<DynamicNode>();
			final var skipReason = findSkipReason(sliceLines);
			perCase.add(dynamicTest(
					skipReason == null ? "violations" : "violations (skipped: " + skipReason + ")",
					() -> BaseCheckTest.assertCheckMatchesMarkers(checkClass, contentForViolations, topic + "/" + name, propertyArgs)
			));
			if (hasFixer && countLinesWithGatedInMarkers(sliceLines, properties) == 1) {
				if (skipReason == null) {
					perCase.add(dynamicTest(
							"fix",
							() -> assertCaseFix(checkClass, fixer, topic, name, properties)
					));
				}
				else {
					perCase.add(dynamicTest(
							"fix-skip",
							() -> assertCaseSkip(checkClass, fixer, topic, name, sliceLines, skipReason, propertyArgs)
					));
				}
			}
			else if (hasFixer && countLinesWithGatedInMarkers(sliceLines, properties) == 0) {
				perCase.add(dynamicTest(
						"imports-unchanged",
						() -> assertEquals(
								collectImportFqcns(slice.inputLines()),
								collectImportFqcns(slice.fixedLines()),
								"Case '" + topic + "/" + name + "': check gated off under " + properties
										+ ", but Fixed slice's imports differ from Violation slice"
						)
				));
			}
			else if (hasFixer && skipReason == null && hasMultiFixOptIn(sliceLines)) {
				perCase.add(dynamicTest(
						"fix-multi",
						() -> assertCaseFixMultiViolation(checkClass, fixer, topic, name, properties)
				));
			}
			else if (hasFixer && skipReason != null && hasMultiFixOptIn(sliceLines)) {
				perCase.add(dynamicTest(
						"fix-multi-skip",
						() -> assertCaseFixMultiViolationSkip(checkClass, fixer, topic, name, sliceLines, skipReason, propertyArgs)
				));
			}
			nodes.add(dynamicContainer(name, perCase.stream()));
		}
		return nodes.stream();
	}

	@CheckReturnValue
	@Nonnull
	private static String combineSuffix(@Nonnull String propSuffix, @Nonnull String fixtureKey) {
		if (propSuffix.isEmpty())
			return fixtureKey;
		if (fixtureKey.isEmpty())
			return propSuffix;
		return propSuffix + "." + fixtureKey;
	}

	/**
	 * The auto-pipeline runs {@code assertCaseFix}, which invokes the fixer
	 * once at the first violation and expects the result to equal the Fixed
	 * slice. That model works when every marker is on a single line (the
	 * fixer's single call rewrites that line, resolving all markers on it),
	 * and breaks when markers span multiple lines (each line needs its own
	 * fix call). So slices with exactly one violation-bearing line are
	 * eligible; everything else is overview content for the {@code
	 * violations} dynamic test.
	 */
	@CheckReturnValue
	private static int countLinesWithGatedInMarkers(@Nonnull List<String> lines, @Nonnull Map<String, String> properties) {
		final var seenLines = new HashSet<Integer>();
		final var parsed = BaseCheckTest.parseViolationMarkers(lines, properties);
		for (var v : parsed)
			seenLines.add(v.line());
		return seenLines.size();
	}

	@CheckReturnValue
	@Nullable
	private static String findSkipReason(@Nonnull List<String> sliceLines) {
		for (var line : sliceLines) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(SKIP_REASON_PREFIX))
				return trimmed.substring(SKIP_REASON_PREFIX.length()).trim();
		}
		return null;
	}

	/**
	 * Slice opt-in for the multi-violation per-fixer test. Slices with more
	 * than one violation-bearing line normally get only the {@code violations}
	 * sub-test (no {@code fix}); adding a {@code // multi-fix-expected}
	 * directive line near the top of the slice opts it into the
	 * {@code fix-multi} sub-test, which invokes the fixer at each violation
	 * (sorted bottom-up) and compares the post-loop output to the Fixed
	 * slice. Overview slices with intentionally-unfixable patterns should
	 * NOT carry this directive; the bulk {@code violations} test alone is
	 * the right shape for those.
	 */
	@CheckReturnValue
	private static boolean hasMultiFixOptIn(@Nonnull List<String> sliceLines) {
		for (var line : sliceLines) {
			if (line.trim().startsWith("// multi-fix-expected"))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static CaseSlice loadSlice(@Nonnull String topic, @Nonnull String caseName, @Nonnull String variantSuffix) {
		try {
			return TestResources.loadCaseSlice(topic, caseName, variantSuffix);
		}
		catch (IOException | URISyntaxException e) {
			throw new IllegalStateException("Failed to load case slice '" + topic + "/" + caseName + "'", e);
		}
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> readCaseNames(@Nonnull String topic, @Nonnull String inputFileName) {
		final var resourcePath = INPUTS_BASE + topic + "/" + inputFileName;
		if (StandardFixerCases.class.getResource(resourcePath) == null)
			return List.of();
		try {
			return TestResources.caseNamesIn(TestResources.readResourceLines(resourcePath));
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException("Failed to read case names for topic: " + topic, e);
		}
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> readFilePackageAndImports(@Nonnull String topic, @Nonnull String inputFileName) {
		final var resourcePath = INPUTS_BASE + topic + "/" + inputFileName;
		if (StandardFixerCases.class.getResource(resourcePath) == null)
			return List.of();
		try {
			final var prefix = new ArrayList<String>();
			for (var line : TestResources.readResourceLines(resourcePath)) {
				final var trimmed = line.trim();
				if (trimmed.startsWith(CASE_MARKER_PREFIX))
					break;
				if (trimmed.startsWith(PACKAGE_PREFIX) || trimmed.startsWith(IMPORT_PREFIX))
					prefix.add(line);
			}
			return List.copyOf(prefix);
		}
		catch (URISyntaxException e) {
			throw new IllegalStateException("Failed to read file prefix for topic: " + topic, e);
		}
	}

	@CheckReturnValue
	@Nullable
	private static String resolveInputFileName(@Nonnull String topic, @Nonnull String propSuffix, @Nonnull String fixtureKey) {
		final var variantSuffix = combineSuffix(propSuffix, fixtureKey);
		if (!variantSuffix.isEmpty()) {
			final var variantName = "cases." + variantSuffix + ".in.java";
			if (StandardFixerCases.class.getResource(INPUTS_BASE + topic + "/" + variantName) != null)
				return variantName;
		}
		if (!fixtureKey.isEmpty()) {
			if (!propSuffix.isEmpty()) {
				final var fixtureName = "cases." + fixtureKey + ".in.java";
				if (StandardFixerCases.class.getResource(INPUTS_BASE + topic + "/" + fixtureName) != null)
					return fixtureName;
			}
			return null;
		}
		return "cases.in.java";
	}

	private StandardFixerCases() {
	}
}