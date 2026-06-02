package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.etk2000.checkstyle.gradle.fix.PropertiesUtil;
import com.etk2000.checkstyle.gradle.fix.StandardFixerCases;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;
import com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
 * Parameterized fixture for checks that follow the standard test pattern:
 * a {@code cases.clean.java} that must produce zero violations and a
 * {@code cases.in.java} whose {@code // violation:} markers must match the
 * actual check output 1:1, with all violation-bearing content wrapped in
 * {@code // === case: NAME ===} slices. Entries flagged with
 * {@code hasFixer} additionally run a per-case fixer test for slices that
 * have exactly one violation-bearing line, sourced via
 * {@link StandardFixerCases#casesFor}.
 *
 * <p>Add a check by appending an entry to {@link #ENTRIES}. Each entry
 * expands to dynamic tests:
 * <ul>
 *   <li>{@code <topic> > clean} - runs the check against
 *       {@code cases.clean.java} and asserts no violations
 *   <li>{@code <topic> > <slice-name> > violations} - one per slice, runs
 *       the check against the slice content (with the file's
 *       {@code package} + {@code import} lines prepended so type-resolving
 *       checks see the right context) via {@link BaseCheckTest#runCheckInline}
 *       and asserts the slice's {@code // violation:} markers match 1:1
 *   <li>{@code <topic> > <slice-name> > fix} - emitted only when
 *       {@code hasFixer} is set and the slice has exactly one
 *       violation-bearing line; applies the fixer and compares against the
 *       {@code cases.out.java} slice
 * </ul>
 *
 * <p>Checks that need additional test methods beyond these stay as their
 * own dedicated {@code XxxCheckTest} class.
 */
public class StandardCheckTests {
	private record Entry(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			boolean hasFixer,
			@Nonnull Map<String, String> properties,
			@Nonnull List<String> fixtureKeys
	) {
		Entry(@Nonnull Class<? extends AbstractCheck> checkClass) {
			this(checkClass, false, Map.of(), List.of());
		}

		Entry(@Nonnull Class<? extends AbstractCheck> checkClass, boolean hasFixer) {
			this(checkClass, hasFixer, Map.of(), List.of());
		}

		Entry(@Nonnull Class<? extends AbstractCheck> checkClass, boolean hasFixer, @Nonnull List<String> fixtureKeys) {
			this(checkClass, hasFixer, Map.of(), fixtureKeys);
		}

		Entry(@Nonnull Class<? extends AbstractCheck> checkClass, boolean hasFixer, @Nonnull Map<String, String> properties) {
			this(checkClass, hasFixer, properties, List.of());
		}

		@CheckReturnValue
		@Nonnull
		String topic() {
			return BaseCheckTest.deriveTopic(checkClass);
		}
	}

	private static final List<Entry> ENTRIES = List.of(
			new Entry(AnnotationOwnLineCheck.class, true, List.of("package")),
			new Entry(AnnotationSameLineCheck.class, true),
			new Entry(ArrayTypeStyleCheck.class, true),
			new Entry(AvoidNoArgumentSuperConstructorCallCheck.class, true),
			new Entry(ClassStructureOrderCheck.class),
			new Entry(ConstructorAssignmentOrderCheck.class, true),
			new Entry(ControlFlowBracesCheck.class, true),
			new Entry(EmptyBodyCheck.class),
			new Entry(EmptySwitchCheck.class),
			new Entry(ExplicitInitializationCheck.class, true),
			new Entry(FieldConsolidationCheck.class, true),
			new Entry(FieldSortingCheck.class, true),
			new Entry(FinalLocalVariableCheck.class, true),
			new Entry(InfiniteEmptyLoopCheck.class),
			new Entry(InstanceofBeforeCastCheck.class),
			new Entry(JitInefficiencyCheck.class, true),
			new Entry(LambdaParameterTypeCheck.class, true),
			new Entry(MethodAlphabeticalOrderCheck.class),
			new Entry(MultilineCallFormattingCheck.class, true),
			new Entry(NoArrayTrailingCommaCheck.class, true),
			new Entry(NoBlankLineBetweenSingleCasesCheck.class, true),
			new Entry(NoCaseBracesCheck.class),
			new Entry(NoEnumTrailingSemicolonCheck.class, true),
			new Entry(NoFinalParametersCheck.class, true),
			new Entry(NoUnnecessaryThisCheck.class, true),
			new Entry(OverloadMethodOrderCheck.class),
			new Entry(PreferBulkOperationCheck.class, true),
			new Entry(PreferCollectionInterfaceCheck.class, true),
			new Entry(PreferDirectBooleanReturnCheck.class, true),
			new Entry(PreferDoWhileCheck.class, true),
			new Entry(PreferExactAssertionCheck.class, true, List.of("junit4", "junit4wildcard", "junit5", "junit5wildcard", "mixedimports", "nonstaticj4withj5static", "nostaticimport", "typewildcard", "unknownreceiver")),
			new Entry(PreferImportCheck.class, true),
			new Entry(PreferLambdaCheck.class),
			new Entry(PreferLiteralSuffixCheck.class, true),
			new Entry(PreferMathMethodCheck.class, true, Map.of("minSdk", "34")),
			new Entry(PreferMathMethodCheck.class, true, Map.of("minSdk", "35")),
			new Entry(PreferPatternMatchingInstanceofCheck.class),
			new Entry(PreferPrefixIncrementCheck.class, true),
			new Entry(PreferRecordCheck.class),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "23")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "24")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "29")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "30")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "31")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "32")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "33")),
			new Entry(PreferSpecificApiCheck.class, false, Map.of("minSdk", "34")),
			new Entry(PreferSpecificApiCheck.class, true, Map.of("minSdk", "35"), List.of("junit5")),
			new Entry(PreferStandardCharsetsCheck.class, true, Map.of("minSdk", "18")),
			new Entry(PreferStandardCharsetsCheck.class, true, Map.of("minSdk", "19")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minOccurrences", "1")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "18"), List.of("objects")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "2147483647"), List.of("chained", "collectors", "conflicts", "explicitshadow", "importconflict", "nestedtypeshadows", "objects", "predicate", "samefileshadow", "wildcard")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "23"), List.of("collectors")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "29"), List.of("objects")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "30"), List.of("objects")),
			new Entry(PreferStaticImportCheck.class, true, Map.of("minSdk", "32"), List.of("predicate")),
			new Entry(PreferStaticImportConstantCheck.class, true),
			new Entry(PreferVarCheck.class, true),
			new Entry(PreferVarCheck.class, false, Map.of("allowedMethods", "genericMethod")),
			new Entry(RecordFormattingCheck.class, true),
			new Entry(RedundantAnnotationSyntaxCheck.class, true),
			new Entry(RedundantArrayCreationCheck.class, true),
			new Entry(RedundantCastCheck.class, true),
			new Entry(RedundantEqualityBranchCheck.class, true),
			new Entry(RedundantModifierCheck.class, true),
			new Entry(RedundantNumericSuffixCheck.class, true),
			new Entry(SwitchCaseOrderCheck.class),
			new Entry(ThreadAnnotationCheck.class),
			new Entry(UnusedImportsCheck.class, true),
			new Entry(UpperEllCheck.class, true)
	);
	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");

	// Topics with cases.*.java fixtures that intentionally have no ENTRIES row:
	// `regex` bundles several RegexpSingleline/RegexpMultiline rules (AbstractFileSetCheck,
	// not AbstractCheck; keyed by format + module id, not one class per topic),
	// `markermatcher` is a negative fixture for the marker-matching harness itself.
	private static final Set<String> NON_CHECK_CASES_TOPICS = Set.of("markermatcher", "regex");

	private static final String CASE_MARKER = "// === case: ";

	private static void assertClean(@Nonnull Entry e, @Nonnull String fixtureKey) throws Exception {
		final var topic = e.topic();
		final var propSuffix = PropertiesUtil.variantSuffix(e.properties());
		final var cleanFile = resolveVariantFile(topic, "cases.clean", propSuffix, fixtureKey);
		if (cleanFile == null)
			return;
		assertTrue(BaseCheckTest.runCheck(e.checkClass(), topic + "/" + cleanFile, PropertiesUtil.propertiesAsArray(e.properties())).isEmpty());
		if (e.hasFixer())
			StandardFixerCases.assertCleanFixerFixedPoint(e.checkClass(), e.properties(), topic, cleanFile);
	}

	@Nonnull
	private static String containerLabel(@Nonnull Entry e, @Nonnull String fixtureKey) {
		final var variant = PropertiesUtil.variantSuffix(e.properties());
		final var base = variant.isEmpty() ? e.checkClass().getSimpleName() : e.checkClass().getSimpleName() + "." + variant;
		return fixtureKey.isEmpty() ? base : base + "." + fixtureKey;
	}

	@CheckReturnValue
	@Nonnull
	private static String fileVariant(@Nonnull Map<String, String> properties, @Nonnull String fixtureKey) {
		return fileVariant(PropertiesUtil.variantSuffix(properties), fixtureKey);
	}

	@CheckReturnValue
	@Nonnull
	private static String fileVariant(@Nonnull String propSuffix, @Nonnull String fixtureKey) {
		if (propSuffix.isEmpty())
			return fixtureKey;
		if (fixtureKey.isEmpty())
			return propSuffix;
		return propSuffix + "." + fixtureKey;
	}

	@CheckReturnValue
	@Nullable
	private static String resolveVariantFile(@Nonnull String topic, @Nonnull String stem, @Nonnull String propSuffix, @Nonnull String fixtureKey) {
		final var variantSuffix = fileVariant(propSuffix, fixtureKey);
		final var stemTail = stem.substring("cases.".length());
		if (!variantSuffix.isEmpty()) {
			final var variantName = "cases." + variantSuffix + "." + stemTail + ".java";
			if (StandardCheckTests.class.getResource("/com/etk2000/checkstyle/inputs/" + topic + "/" + variantName) != null)
				return variantName;
		}
		if (!fixtureKey.isEmpty()) {
			if (!propSuffix.isEmpty()) {
				final var fixtureName = "cases." + fixtureKey + "." + stemTail + ".java";
				if (StandardCheckTests.class.getResource("/com/etk2000/checkstyle/inputs/" + topic + "/" + fixtureName) != null)
					return fixtureName;
			}
			return null;
		}
		return stem + ".java";
	}

	/**
	 * Every slice in {@code casesIn} carrying a {@code // multi-fix-expected}
	 * directive, mapped to whether it also carries a {@code // skip-reason:} (which
	 * wins over the directive in {@code StandardFixerCases.casesFor}).
	 */
	@CheckReturnValue
	@Nonnull
	private static Map<String, Boolean> slicesWithMultiFixDirective(@Nonnull Path casesIn) throws IOException {
		final var found = new LinkedHashMap<String, Boolean>();
		String current = null;
		var sawDirective = false;
		var sawSkipReason = false;
		for (var line : Files.readAllLines(casesIn)) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(CASE_MARKER)) {
				current = trimmed.substring(CASE_MARKER.length(), trimmed.length() - " ===".length()).trim();
				sawDirective = false;
				sawSkipReason = false;
			}
			else if (trimmed.startsWith("// multi-fix-expected"))
				sawDirective = true;
			else if (trimmed.startsWith("// skip-reason:"))
				sawSkipReason = true;
			else if (trimmed.equals("// === end ===") && current != null && sawDirective)
				found.put(current, sawSkipReason);
		}
		return found;
	}

	@Nonnull
	private static URI testSourceUri(@Nonnull Entry e, @Nonnull String fixtureKey) {
		final var fileVariant = fileVariant(e.properties(), fixtureKey);
		final var fqcn = fileVariant.isEmpty()
				? e.checkClass().getName()
				: e.checkClass().getName() + "_" + fileVariant.replace('-', '_').replace('.', '_');
		return URI.create("class:" + fqcn);
	}

	@CheckReturnValue
	@Nonnull
	private static DynamicNode toContainer(@Nonnull Entry e, @Nonnull String fixtureKey) {
		final var topic = e.topic();
		final var propSuffix = PropertiesUtil.variantSuffix(e.properties());
		final var hasCleanFile = resolveVariantFile(topic, "cases.clean", propSuffix, fixtureKey) != null;
		final var children = new ArrayList<DynamicNode>();
		if (hasCleanFile)
			children.add(dynamicTest("clean", () -> assertClean(e, fixtureKey)));
		StandardFixerCases.casesFor(e.checkClass(), e.hasFixer(), e.properties(), e.topic(), fixtureKey)
				.forEach(children::add);
		return dynamicContainer(
				containerLabel(e, fixtureKey),
				testSourceUri(e, fixtureKey),
				children.stream()
		);
	}

	/**
	 * Asserts {@link #ENTRIES} is the single registry for check topics: every
	 * topic directory holding {@code cases.*.java} fixtures must either have an
	 * {@code ENTRIES} row or be declared in {@link #NON_CHECK_CASES_TOPICS}. A
	 * new topic added to disk without registering it fails here, as does a
	 * stale or redundant allowlist entry.
	 */
	@Test
	public void everyCasesTopicIsRegistered() throws IOException {
		final var registered = new TreeSet<String>();
		ENTRIES.forEach(e -> registered.add(e.topic()));
		final var onDisk = new TreeSet<String>();
		try (var topics = Files.newDirectoryStream(INPUTS_ROOT, Files::isDirectory)) {
			for (var topicDir : topics) {
				try (var files = Files.list(topicDir)) {
					if (files.map(p -> p.getFileName().toString()).anyMatch(n -> n.startsWith("cases.") && n.endsWith(".java")))
						onDisk.add(topicDir.getFileName().toString());
				}
			}
		}
		final var issues = new ArrayList<String>();
		for (var topic : onDisk) {
			if (!registered.contains(topic) && !NON_CHECK_CASES_TOPICS.contains(topic))
				issues.add("topic '" + topic + "' has cases.*.java but no ENTRIES row; register it or add to NON_CHECK_CASES_TOPICS");
		}
		for (var allowed : NON_CHECK_CASES_TOPICS) {
			if (!onDisk.contains(allowed))
				issues.add("NON_CHECK_CASES_TOPICS lists '" + allowed + "' but no such topic has cases.*.java; remove the stale entry");
			if (registered.contains(allowed))
				issues.add("topic '" + allowed + "' is both an ENTRIES row and in NON_CHECK_CASES_TOPICS; drop the allowlist entry");
		}
		if (!issues.isEmpty())
			fail("cases topic registry drift:\n  " + String.join("\n  ", issues));
	}

	@Test
	public void multiFixDirectivesActuallyEmitAFixMultiTest() throws IOException {
		final var withFixer = new TreeSet<String>();
		ENTRIES.forEach(e -> {
			if (e.hasFixer())
				withFixer.add(e.topic());
		});
		final var issues = new ArrayList<String>();
		try (var topics = Files.newDirectoryStream(INPUTS_ROOT, Files::isDirectory)) {
			for (var topicDir : topics) {
				final var topic = topicDir.getFileName().toString();
				final var casesIn = topicDir.resolve("cases.in.java");
				if (!Files.exists(casesIn))
					continue;
				for (var slice : slicesWithMultiFixDirective(casesIn).entrySet()) {
					if (!withFixer.contains(topic))
						issues.add(topic + "/" + slice.getKey() + ": topic has no fixer, so no fix-multi test can run");
				}
			}
		}
		if (!issues.isEmpty()) {
			fail(
					"\n// multi-fix-expected is inert on the slice(s) below, so their cases.out.java entry is asserted"
							+ " by nothing. Remove the directive, or make the slice satisfy the gate:\n  "
							+ String.join("\n  ", issues)
			);
		}
	}

	@Test
	public void testEntriesAreSortedAlphabetically() {
		final var labels = ENTRIES.stream().map(e -> containerLabel(e, "")).toList();
		final var issues = new ArrayList<String>();
		for (var i = 1; i < labels.size(); ++i) {
			final var prev = labels.get(i - 1);
			final var curr = labels.get(i);
			if (prev.compareTo(curr) >= 0)
				issues.add("'" + curr + "' must come strictly before '" + prev + "'");
		}
		for (var e : ENTRIES) {
			final var keys = e.fixtureKeys();
			for (var i = 1; i < keys.size(); ++i) {
				if (keys.get(i - 1).compareTo(keys.get(i)) >= 0)
					issues.add("Entry '" + containerLabel(e, "") + "' fixtureKeys not sorted: '" + keys.get(i) + "' after '" + keys.get(i - 1) + "'");
			}
		}
		if (!issues.isEmpty())
			fail("ENTRIES must be sorted alphabetically by container label: " + String.join("; ", issues));
	}

	@TestFactory
	public Stream<DynamicNode> tests() {
		return ENTRIES.stream().flatMap(e -> {
			final var defaults = Stream.of(toContainer(e, ""));
			if (e.fixtureKeys().isEmpty())
				return defaults;
			return Stream.concat(defaults, e.fixtureKeys().stream().map(k -> toContainer(e, k)));
		});
	}
}