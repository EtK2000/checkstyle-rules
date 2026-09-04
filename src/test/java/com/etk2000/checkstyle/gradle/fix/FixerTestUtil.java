package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.JavaLangClasses;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.TestResources.SnippetFixture;
import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.api.Violation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

final class FixerTestUtil {
	record AnnotationLineScan(int depth, boolean inBlockComment) {}

	/**
	 * Where a comment goes on a line: at char index {@code at}, followed by a space when
	 * {@code beforeCode}, else preceded by one.
	 */
	private record CommentSpot(int at, boolean beforeCode) {}

	// as many chars and code points as SUPPLEMENTARY_STAND_IN, but letters ordered above every
	// ASCII char as SUPPLEMENTARY_TEXT's surrogates are, so a name ending in it sorts the same
	private static final String IDENTIFIER_STAND_IN = String.valueOf((char) 0xAC00).repeat(6);
	// as many chars as SUPPLEMENTARY_TEXT (twice its code points), and nowhere else in a fixture
	private static final String SUPPLEMENTARY_STAND_IN = "Qx7Qx7";
	private static final String SUPPLEMENTARY_TEXT = new String(Character.toChars(0x1D465)).repeat(3);

	/**
	 * Applies a single {@link FixResult} to {@code lines}. Mirrors only the
	 * clear-then-insert block of production {@code CheckstyleFixAction.applyFixes}
	 * and intentionally omits everything that's a multi-violation concern. Use this only for
	 * single-fix unit tests; route any test that needs the full orchestration
	 * through {@link CheckstyleFixAction#applyFixes} directly.
	 */
	static void applyFixResult(@Nonnull List<String> lines, @Nonnull FixResult result) {
		if (result.endLine() >= result.startLine())
			lines.subList(result.startLine(), result.endLine() + 1).clear();
		lines.addAll(result.startLine(), result.replacement());
	}

	/**
	 * Asserts the diff between {@code inputImportFqcns} and {@code fixedImportFqcns}
	 * is additive (the input is a subset of the fixed). The framework's
	 * diff-driven {@code expectedImports} relies on this invariant; production
	 * removes unused imports in a later pipeline pass, not via {@code FixResult},
	 * so a Fixed slice that omits an input-declared import would indicate a
	 * misauthored fixture rather than a real fixer behavior.
	 */
	static void assertAdditiveImports(
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Set<String> inputImportFqcns,
			@Nonnull Set<String> fixedImportFqcns
	) {
		final var removedImports = new TreeSet<String>();
		for (var fqcn : inputImportFqcns) {
			if (!fixedImportFqcns.contains(fqcn))
				removedImports.add(fqcn);
		}
		if (!removedImports.isEmpty()) {
			throw new IllegalStateException(
					"Case '" + topic + "/" + caseName + "': Violation slice declares imports absent from Fixed"
							+ " slice " + removedImports + ". The framework supports additive diffs only; production"
							+ " removes unused imports in a later pipeline pass, not via FixResult."
			);
		}
	}

	static void assertCaseFix(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties
	) throws Exception {
		assertCaseFix(checkClass, fixer, topic, caseName, PropertiesUtil.propertiesAsArray(properties));
	}

	static void assertCaseFix(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull String... checkProperties
	) throws Exception {
		final var slice = TestResources.loadCaseSlice(topic, caseName, PropertiesUtil.variantSuffixFromArray(checkProperties));
		final var propertiesMap = PropertiesUtil.arrayToMap(checkProperties);
		final var expectedViolations = BaseCheckTest.parseViolationMarkers(slice.inputLines(), propertiesMap);
		assertFalse(
				expectedViolations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': slice must contain at least one violation marker"
		);
		final var inputImportFqcns = collectImportFqcns(slice.inputLines());
		final var fixedImportFqcns = collectImportFqcns(slice.fixedLines());
		assertAdditiveImports(topic, caseName, inputImportFqcns, fixedImportFqcns);
		final var expectedImports = new TreeSet<String>();
		for (var fqcn : fixedImportFqcns) {
			if (!inputImportFqcns.contains(fqcn))
				expectedImports.add(fqcn);
		}

		final var strippedInput = FullPipelineRunner.stripViolationComments(String.join("\n", slice.inputLines()));
		final var violations = BaseCheckTest.runCheckInline(checkClass, strippedInput, checkProperties);
		assertEquals(
				expectedViolations.size(),
				violations.size(),
				"Case '" + topic + "/" + caseName + "': expected " + expectedViolations.size()
						+ " violation(s) from " + checkClass.getSimpleName() + ", got " + violations.size()
		);

		final var lines = new ArrayList<>(List.of(strippedInput.split("\n", -1)));
		for (var i = 0; i < violations.size(); ++i) {
			final var v = violations.get(i);
			final var exp = expectedViolations.get(i);
			assertEquals(
					exp.line(),
					v.getLine(),
					"Case '" + topic + "/" + caseName + "': violation[" + i + "] line mismatch (check log site moved?)"
			);
			assertEquals(
					exp.severity(),
					v.getSeverityLevel(),
					"Case '" + topic + "/" + caseName + "': violation[" + i + "] severity mismatch"
			);
			assertEquals(
					exp.message(),
					v.getMessage(),
					"Case '" + topic + "/" + caseName + "': violation[" + i + "] message mismatch"
			);
		}
		// Apply every violation bottom-to-top, mirroring production
		// CheckstyleFixAction.applyFixes: a slice may carry more than one
		// violation on a line (e.g. two implicitly-redundant modifiers on one
		// interface member), and a fixer resolves one per fix() call. SkipResult
		// / null are ignored, exactly as production does; the output and
		// residual-error assertions below catch any error left unfixed.
		final var ordered = new ArrayList<>(errorsOf(violations));
		ordered.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);
		final var actualImportsToAdd = new TreeSet<String>();
		var applied = 0;
		for (var event : ordered) {
			final var lineIndex = event.getLine() - 1;
			// an earlier fix in this sweep can shrink the buffer past a later violation's
			// line; production applyFixes bounds-checks the index before dispatching, so
			// the sweep has to as well rather than throwing
			if (lineIndex < 0 || lineIndex >= lines.size())
				continue;
			final var charColumn = CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
			if (invokeFixWithContext(fixer, lines, applied, lineIndex, charColumn, event.getViolation()) instanceof FixResult result) {
				actualImportsToAdd.addAll(result.importsToAdd());
				applyFixResult(lines, result);
				++applied;
			}
		}
		// A fixer may report an import the file already has (e.g. List.copyOf
		// when a List<> parameter already imports java.util.List); production's
		// insertMissingImports drops those, so compare net-new imports only.
		final var netNewImportsToAdd = new TreeSet<>(actualImportsToAdd);
		netNewImportsToAdd.removeAll(inputImportFqcns);
		assertEquals(
				expectedImports,
				netNewImportsToAdd,
				() -> "Case '" + topic + "/" + caseName + "': importsToAdd mismatch"
						+ wildcardSubsumptionHint(expectedImports, netNewImportsToAdd, inputImportFqcns)
		);
		insertAddedImportsAtFixedPositions(lines, slice.fixedLines(), inputImportFqcns);
		assertEquals(slice.fixedLines(), lines, "Case '" + topic + "/" + caseName + "' output mismatch");
		assertFixedPoint(checkClass, lines, topic, caseName, checkProperties);
	}

	/**
	 * Multi-violation analog of {@link #assertCaseFix}. The slice has more
	 * than one violation-bearing line, so a single {@code fix()} call cannot
	 * produce the Fixed slice. Sorts violations bottom-up (mirroring
	 * production {@code CheckstyleFixAction.applyFixes}), invokes
	 * {@code fix()} at each violation in turn, applies each {@link FixResult},
	 * accumulates {@code importsToAdd} across calls, and asserts the final
	 * lines equal the Fixed slice.
	 */
	static void assertCaseFixMultiViolation(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties
	) throws Exception {
		assertCaseFixMultiViolation(checkClass, fixer, topic, caseName, PropertiesUtil.propertiesAsArray(properties));
	}

	static void assertCaseFixMultiViolation(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull String... checkProperties
	) throws Exception {
		final var slice = TestResources.loadCaseSlice(topic, caseName, PropertiesUtil.variantSuffixFromArray(checkProperties));
		final var propertiesMap = PropertiesUtil.arrayToMap(checkProperties);
		final var expectedViolations = BaseCheckTest.parseViolationMarkers(slice.inputLines(), propertiesMap);
		assertFalse(
				expectedViolations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': slice must contain at least one violation marker"
		);
		final var inputImportFqcns = collectImportFqcns(slice.inputLines());
		final var fixedImportFqcns = collectImportFqcns(slice.fixedLines());
		assertAdditiveImports(topic, caseName, inputImportFqcns, fixedImportFqcns);
		final var expectedImports = new TreeSet<String>();
		for (var fqcn : fixedImportFqcns) {
			if (!inputImportFqcns.contains(fqcn))
				expectedImports.add(fqcn);
		}

		final var strippedInput = FullPipelineRunner.stripViolationComments(String.join("\n", slice.inputLines()));
		final var violations = BaseCheckTest.runCheckInline(checkClass, strippedInput, checkProperties);
		assertEquals(
				expectedViolations.size(),
				violations.size(),
				"Case '" + topic + "/" + caseName + "': expected " + expectedViolations.size()
						+ " violation(s) from " + checkClass.getSimpleName() + ", got " + violations.size()
		);
		for (var i = 0; i < violations.size(); ++i) {
			final var v = violations.get(i);
			final var exp = expectedViolations.get(i);
			assertEquals(exp.line(), v.getLine(), "Case '" + topic + "/" + caseName + "': violation[" + i + "] line mismatch");
			assertEquals(exp.severity(), v.getSeverityLevel(), "Case '" + topic + "/" + caseName + "': violation[" + i + "] severity mismatch");
			assertEquals(exp.message(), v.getMessage(), "Case '" + topic + "/" + caseName + "': violation[" + i + "] message mismatch");
		}

		final var lines = new ArrayList<>(List.of(strippedInput.split("\n", -1)));
		final var ordered = new ArrayList<>(errorsOf(violations));
		ordered.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);
		final var actualImportsToAdd = new TreeSet<String>();
		var applied = 0;
		for (var event : ordered) {
			final var lineIndex = event.getLine() - 1;
			// an earlier fix in this sweep can shrink the buffer past a later violation's
			// line; production applyFixes bounds-checks the index before dispatching, so
			// the sweep has to as well rather than throwing
			if (lineIndex < 0 || lineIndex >= lines.size())
				continue;
			final var charColumn = CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
			if (invokeFixWithContext(fixer, lines, applied, lineIndex, charColumn, event.getViolation()) instanceof FixResult result) {
				actualImportsToAdd.addAll(result.importsToAdd());
				applyFixResult(lines, result);
				++applied;
			}
			// SkipResult / null intentionally ignored: matches production
			// CheckstyleFixAction.applyFixes behavior, where some violations
			// in a multi-violation file are skipped while others are fixed.
			// The Fixed slice text comparison below catches any divergence.
		}
		// net-new for the same reason as the single-violation path: a fixer may report an import
		// the file already has, and production's insertMissingImports drops those
		final var netNewImportsToAdd = new TreeSet<>(actualImportsToAdd);
		netNewImportsToAdd.removeAll(inputImportFqcns);
		assertEquals(
				expectedImports,
				netNewImportsToAdd,
				"Case '" + topic + "/" + caseName + "': importsToAdd mismatch (union across all fix calls)"
		);
		insertAddedImportsAtFixedPositions(lines, slice.fixedLines(), inputImportFqcns);
		final var linesForCompare = lines.stream()
				.filter(l -> !l.trim().startsWith("// multi-fix-expected"))
				.toList();
		assertEquals(slice.fixedLines(), linesForCompare, "Case '" + topic + "/" + caseName + "' output mismatch (after multi-violation fix)");
		assertFixedPoint(checkClass, lines, topic, caseName, checkProperties);
	}

	/**
	 * Multi-violation counterpart of {@link #assertCaseSkip}: drives the fixer at every
	 * reported position and asserts each refuses with the slice's {@code // skip-reason:},
	 * then that the buffer is unchanged. {@code assertCaseFixMultiViolation} cannot express
	 * this shape, because its fixed-point assertion requires the residual count to reach
	 * zero and an all-skip slice keeps every violation by design.
	 */
	static void assertCaseFixMultiViolationSkip(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull List<String> sliceInputLines,
			@Nonnull String expectedReason,
			@Nonnull String... checkProperties
	) throws Exception {
		final var propertiesMap = PropertiesUtil.arrayToMap(checkProperties);
		final var expectedViolations = BaseCheckTest.parseViolationMarkers(sliceInputLines, propertiesMap);
		assertFalse(
				expectedViolations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': skipped slice must contain at least one violation marker"
		);

		final var strippedInput = FullPipelineRunner.stripViolationComments(String.join("\n", sliceInputLines));
		final var violations = BaseCheckTest.runCheckInline(checkClass, strippedInput, checkProperties);
		assertEquals(
				expectedViolations.size(),
				violations.size(),
				"Case '" + topic + "/" + caseName + "': expected " + expectedViolations.size()
						+ " violation(s) from " + checkClass.getSimpleName() + ", got " + violations.size()
		);

		final var original = List.copyOf(List.of(strippedInput.split("\n", -1)));
		final var lines = new ArrayList<>(original);
		final var sorted = new ArrayList<>(errorsOf(violations));
		assertFalse(sorted.isEmpty(), "Case '" + topic + "/" + caseName + "': a skipped slice needs an error, since no warning reaches the fixer");
		final var bottomUp = Comparator
				.comparingInt(AuditEvent::getLine)
				.thenComparingInt(AuditEvent::getColumn)
				.reversed();
		sorted.sort(bottomUp);
		for (var event : sorted) {
			final var lineIndex = event.getLine() - 1;
			final var charColumn = CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
			final var result = invokeFixWithContext(fixer, lines, 0, lineIndex, charColumn, event.getViolation());
			final var skip = assertInstanceOf(
					SkipResult.class,
					result,
					"Case '" + topic + "/" + caseName + "': expected SkipResult at line " + event.getLine()
							+ ", got " + (result == null ? "null" : result.getClass().getSimpleName())
			);
			assertEquals(
					expectedReason,
					skip.reason(),
					"Case '" + topic + "/" + caseName + "': SkipResult.reason() at line " + event.getLine()
							+ " does not match // skip-reason: directive"
			);
		}
		assertEquals(original, lines, "Case '" + topic + "/" + caseName + "': an all-skip slice must leave the buffer unchanged");
	}

	/** {@link #assertSupplementary} for a slice the fixer fixes. */
	static void assertCaseFixSupplementary(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties
	) throws Exception {
		assertSupplementary(checkClass, fixer, topic, caseName, properties, null);
	}

	/**
	 * Skipped-slice analog of {@link #assertCaseFix}. The check must fire on
	 * the slice (proves the violation is real); the fixer must then return a
	 * {@link SkipResult} whose {@link SkipResult#reason()} exactly matches
	 * {@code expectedReason} (the slice's {@code // skip-reason:} directive).
	 */
	static void assertCaseSkip(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull List<String> sliceInputLines,
			@Nonnull String expectedReason,
			@Nonnull String... checkProperties
	) throws Exception {
		final var propertiesMap = PropertiesUtil.arrayToMap(checkProperties);
		final var expectedViolations = BaseCheckTest.parseViolationMarkers(sliceInputLines, propertiesMap);
		assertFalse(
				expectedViolations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': skipped slice must contain at least one violation marker"
		);

		final var strippedInput = FullPipelineRunner.stripViolationComments(String.join("\n", sliceInputLines));
		final var violations = errorsOf(BaseCheckTest.runCheckInline(checkClass, strippedInput, checkProperties));
		assertFalse(
				violations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': skipped slice expected check to report an error but got none"
		);

		final var lines = new ArrayList<>(List.of(strippedInput.split("\n", -1)));
		final var event = violations.getFirst();
		final var lineIndex = event.getLine() - 1;
		final var charColumn = CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
		final var result = invokeFixWithContext(fixer, lines, 0, lineIndex, charColumn, event.getViolation());
		final var skip = assertInstanceOf(
				SkipResult.class,
				result,
				"Case '" + topic + "/" + caseName + "': expected SkipResult, got "
						+ (result == null ? "null" : result.getClass().getSimpleName())
		);
		assertEquals(
				expectedReason,
				skip.reason(),
				"Case '" + topic + "/" + caseName + "': SkipResult.reason() does not match // skip-reason: directive"
		);
	}

	/**
	 * {@link #assertSupplementary} for a {@code // skip-reason:} slice: the slice's own run must refuse
	 * with {@code skipReason}, and the wide run must end with the same text and skip reasons as its
	 * ASCII stand-in, so a code-point column used as a char index cannot turn the refusal into an edit.
	 */
	static void assertCaseSkipSupplementary(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties,
			@Nonnull String skipReason
	) throws Exception {
		assertSupplementary(checkClass, fixer, topic, caseName, properties, skipReason);
	}

	/**
	 * Asserts {@code checkClass} reports no error on {@code lines}. A warning is no residual, since
	 * {@link CheckstyleFixAction#applyFixes} never hands one to a fixer, so no fix could clear it.
	 */
	static void assertFixedPoint(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull List<String> lines,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull String... checkProperties
	) throws Exception {
		final var residualErrors = BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), checkProperties).stream()
				.filter(event -> event.getSeverityLevel() == SeverityLevel.ERROR)
				.count();
		assertEquals(
				0,
				residualErrors,
				"Case '" + topic + "/" + caseName + "': fixed content is not a fixed point ("
						+ residualErrors + " residual errors from " + checkClass.getSimpleName() + ")"
		);
	}

	// Migrate call sites to assertCaseFix.
	@Deprecated(forRemoval = true)
	@Nonnull
	static SnippetFixture assertSimpleFix(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String snippetName
	) throws Exception {
		return assertSimpleFix(fixer, topic, snippetName, Set.of());
	}

	// Migrate call sites to assertCaseFix.
	@Deprecated(forRemoval = true)
	@Nonnull
	static SnippetFixture assertSimpleFix(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String snippetName,
			@Nonnull Set<String> expectedImports
	) throws Exception {
		final var fx = TestResources.loadSnippet(topic, snippetName);
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(
				FixResult.class,
				invokeFixWithContext(fixer, lines, 0, t.line(), t.column(), null)
		);
		assertEquals(expectedImports, result.importsToAdd());
		applyFixResult(lines, result);
		assertEquals(fx.fixedLines(), lines);
		return fx;
	}

	/**
	 * null `fix` responses are deprecated and will be removed
	 */
	@Deprecated(forRemoval = true)
	static void assertSkip(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String snippetName
	) throws Exception {
		final var fx = TestResources.loadSnippet(topic, snippetName);
		final var t = fx.firstTarget();
		assertNull(invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), 0, t.line(), t.column(), null));
	}

	static void assertSkipResult(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String snippetName
	) throws Exception {
		final var fx = TestResources.loadSnippet(topic, snippetName);
		final var t = fx.firstTarget();
		assertInstanceOf(
				SkipResult.class,
				invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), 0, t.line(), t.column(), null)
		);
	}

	static void assertSkipResult(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String snippetName,
			@Nonnull String expectedReason
	) throws Exception {
		final var fx = TestResources.loadSnippet(topic, snippetName);
		final var t = fx.firstTarget();
		final var result = assertInstanceOf(
				SkipResult.class,
				invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), 0, t.line(), t.column(), null)
		);
		assertEquals(expectedReason, result.reason());
	}

	/**
	 * Runs the slice's check and fixer twice with the violation line's text ahead of the reported
	 * column made longer: once by six ASCII or BMP characters, once by three supplementary ones.
	 * Both take six chars, so every char index and line width is the same, while the second line's
	 * code-point columns are three lower. A fixer that indexes the line with the code-point column
	 * then edits the wrong chars, so the two outputs must match once the added text is mapped back.
	 * The text goes in the first way that leaves the ASCII run ending the way the slice does without
	 * it (the same text once mapped back, the same skip reasons, {@code skipReason} among them for a
	 * skip slice), so the comparison exercises the slice's own fix or refusal. Tried in order: a
	 * name the slice declares, used ahead of the column, renamed everywhere ({@link #renameCandidates});
	 * text added to a string literal ahead of the column; the end of such a literal overwritten
	 * instead, which keeps every char index and line width for a slice sitting on a width limit; and
	 * a comment at one of the {@linkplain #commentSpots comment spots}, taken only where the fix keeps
	 * the comment. When none does, the wide run must still match the ASCII run with the comment at
	 * the first spot that kept the outcome but whose fix dropped the comment, else at the first spot
	 * that parses, since a comment can change the outcome on its own (a line past a width limit, a
	 * comment the fixer refuses to move) without the code-point column playing any part. A slice
	 * where no spot parses fails, naming what each attempt changed.
	 */
	private static void assertSupplementary(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties,
			@Nullable String skipReason
	) throws Exception {
		final var checkProperties = PropertiesUtil.propertiesAsArray(properties);
		final var violationLine = violationLineOf(topic, caseName, properties);
		final var stripped = strippedSlice(topic, caseName, checkProperties);
		final var plainSkips = new ArrayList<String>();
		final var plain = fixAll(checkClass, fixer, new ArrayList<>(stripped), plainSkips, checkProperties);
		final var label = "Case '" + topic + "/" + caseName + "': ";
		if (skipReason != null)
			assertTrue(plainSkips.contains(skipReason), label + "expected a skip for '" + skipReason + "', got " + plainSkips);
		final var column = eventColumn(checkClass, stripped, violationLine, checkProperties);
		final var changed = new ArrayList<String>();
		for (var name : renameCandidates(stripped, violationLine, column)) {
			final var asciiName = name + IDENTIFIER_STAND_IN;
			final var wideName = name + SUPPLEMENTARY_TEXT;
			final var ascii = renamed(stripped, name, asciiName);
			if (shiftKeepsOutcome(checkClass, fixer, ascii, renamed(stripped, name, wideName), asciiName, wideName, name, plain, plainSkips, label, checkProperties))
				return;
			changed.add("renaming " + name + " changed the outcome");
		}
		for (var at : literalEnds(stripped, violationLine, column)) {
			final var ascii = withInsertion(stripped, violationLine, at, SUPPLEMENTARY_STAND_IN);
			final var wide = withInsertion(stripped, violationLine, at, SUPPLEMENTARY_TEXT);
			if (shiftKeepsOutcome(checkClass, fixer, ascii, wide, SUPPLEMENTARY_STAND_IN, SUPPLEMENTARY_TEXT, "", plain, plainSkips, label, checkProperties))
				return;
			changed.add("lengthening the string literal ending at " + at + " changed the outcome");
		}
		final var eventLine = stripped.get(violationLine);
		for (var at : literalEnds(stripped, violationLine, column)) {
			final var from = at - SUPPLEMENTARY_STAND_IN.length();
			final var replaced = from < 1 ? "" : eventLine.substring(from, at);
			if (from < 1 || replaced.indexOf('"') >= 0 || replaced.indexOf('\\') >= 0 || LineText.isEscaped(eventLine, from))
				continue;
			final var ascii = overwritten(stripped, violationLine, from, SUPPLEMENTARY_STAND_IN);
			final var wide = overwritten(stripped, violationLine, from, SUPPLEMENTARY_TEXT);
			if (shiftKeepsOutcome(checkClass, fixer, ascii, wide, SUPPLEMENTARY_STAND_IN, SUPPLEMENTARY_TEXT, replaced, plain, plainSkips, label, checkProperties))
				return;
			changed.add("overwriting the end of the string literal ending at " + at + " changed the outcome");
		}
		final var dropping = new ArrayList<CommentSpot>();
		final var parseable = new ArrayList<CommentSpot>();
		for (var spot : commentSpots(stripped.get(violationLine), column)) {
			final var asciiSkips = new ArrayList<String>();
			final String ascii;
			try {
				ascii = fixAll(checkClass, fixer, withComment(stripped, violationLine, spot, SUPPLEMENTARY_STAND_IN), asciiSkips, checkProperties);
			}
			catch (CheckstyleException e) {
				changed.add(spot + ": no longer parses");
				continue;
			}
			parseable.add(spot);
			if (!asciiSkips.equals(plainSkips) || !withoutComment(ascii).equals(withoutComment(plain)))
				continue;
			// a fix that drops the comment also drops what told the two runs apart, which leaves
			// later events in the pass at columns that differ for no fault of the fixer, so a spot
			// whose comment survives is preferred
			if (!ascii.contains(SUPPLEMENTARY_STAND_IN)) {
				dropping.add(spot);
				continue;
			}
			assertWideRunMatches(checkClass, fixer, stripped, violationLine, spot, label, checkProperties);
			return;
		}
		if (!dropping.isEmpty())
			assertWideRunMatches(checkClass, fixer, stripped, violationLine, dropping.getFirst(), label, checkProperties);
		else if (!parseable.isEmpty())
			assertWideRunMatches(checkClass, fixer, stripped, violationLine, parseable.getFirst(), label, checkProperties);
		else
			fail(label + "no rename, literal or comment on the violation line parses: " + changed);
	}

	/**
	 * Runs {@link #fixAll} on {@code lines} with the ASCII and the supplementary comment at
	 * {@code spot} of line {@code lineIndex}, and asserts both end the same once the comment's text
	 * is swapped, with the same skip reasons.
	 */
	private static void assertWideRunMatches(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull CommentSpot spot,
			@Nonnull String label,
			@Nonnull String... checkProperties
	) throws Exception {
		final var asciiSkips = new ArrayList<String>();
		final var ascii = fixAll(checkClass, fixer, withComment(lines, lineIndex, spot, SUPPLEMENTARY_STAND_IN), asciiSkips, checkProperties);
		final var wideSkips = new ArrayList<String>();
		final var wide = fixAll(checkClass, fixer, withComment(lines, lineIndex, spot, SUPPLEMENTARY_TEXT), wideSkips, checkProperties);
		assertEquals(ascii.replace(SUPPLEMENTARY_STAND_IN, SUPPLEMENTARY_TEXT), wide, label + "supplementary characters at " + spot + " changed the fix");
		assertEquals(asciiSkips, wideSkips, label + "supplementary characters at " + spot + " changed a skip");
	}

	@Nonnull
	static Set<String> collectImportFqcns(@Nonnull List<String> lines) {
		final var imports = new TreeSet<String>();
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var trimmed = line.trim();
			final var insideCommentOrTextBlock = lexer.inBlockComment() || lexer.inTextBlock();
			lexer = JavaLineScanner.stateAfter(line, lexer);
			if (insideCommentOrTextBlock || !trimmed.startsWith("import ") || !trimmed.endsWith(";"))
				continue;
			final var fqcn = trimmed.substring("import ".length(), trimmed.length() - 1).trim();
			if (fqcn.isEmpty()) {
				throw new IllegalStateException(
						"collectImportFqcns: malformed import line (empty FQCN): '" + line + "'"
				);
			}
			imports.add(fqcn);
		}
		return imports;
	}

	/**
	 * Where {@link #assertSupplementary} may put its comment on {@code line}, in the order it tries
	 * them: just before the reported char {@code column}, after the line's indentation, and after
	 * the last token ahead of the column. A column ahead of the code counts as the indentation's end.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<CommentSpot> commentSpots(@Nonnull String line, int column) {
		final var indent = LineText.extractIndent(line).length();
		final var at = Math.clamp(column, indent, line.length());
		final var spots = new ArrayList<CommentSpot>();
		spots.add(new CommentSpot(at, true));
		if (at != indent)
			spots.add(new CommentSpot(indent, true));
		var tokenEnd = at;
		while (tokenEnd > indent && Character.isWhitespace(line.charAt(tokenEnd - 1)))
			--tokenEnd;
		if (tokenEnd > indent)
			spots.add(new CommentSpot(tokenEnd, false));
		return spots;
	}

	/**
	 * The events of {@code violations} that production {@code CheckstyleFixAction.applyFixes} hands a
	 * fixer: its errors. A warning is never fixed there, so handing it to a fixer here would test a
	 * fix no run makes.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AuditEvent> errorsOf(@Nonnull List<AuditEvent> violations) {
		return violations.stream().filter(event -> event.getSeverityLevel() == SeverityLevel.ERROR).toList();
	}

	/**
	 * The char index on line {@code lineIndex} of {@code lines} at which {@code checkClass} first
	 * reports an error on that line, or -1 when it reports none there.
	 */
	@CheckReturnValue
	private static int eventColumn(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String... checkProperties
	) throws Exception {
		for (var event : errorsOf(BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), checkProperties))) {
			if (event.getLine() == lineIndex + 1)
				return CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
		}
		return -1;
	}

	/**
	 * Runs {@code checkClass} on {@code lines} and applies {@code fixer} to every violation
	 * bottom-up the way {@link #assertCaseFix} does, returning the text it ends with and adding
	 * each skip reason to {@code skips}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String fixAll(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> lines,
			@Nonnull List<String> skips,
			@Nonnull String... checkProperties
	) throws Exception {
		final var violations = new ArrayList<>(errorsOf(BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), checkProperties)));
		violations.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);
		var applied = 0;
		for (var event : violations) {
			final var eventLine = event.getLine() - 1;
			if (eventLine < 0 || eventLine >= lines.size())
				continue;
			final var column = CheckstyleFixAction.eventCharIndex(lines.get(eventLine), event);
			final var attempt = invokeFixWithContext(fixer, lines, applied, eventLine, column, event.getViolation());
			if (attempt instanceof FixResult result) {
				applyFixResult(lines, result);
				++applied;
			}
			else if (attempt instanceof SkipResult skip)
				skips.add(skip.reason());
		}
		return String.join("\n", lines);
	}

	/**
	 * After {@link #applyFixResult} has rewritten the body, splice in any
	 * import line present in the fixed slice but not the input slice, at the
	 * same index it occupies in the fixed slice. Mirrors what production
	 * {@code CheckstyleFixAction.insertMissingImports} does after collecting
	 * {@link FixResult#importsToAdd()} across all fixes, so the post-apply
	 * lines match the fixed slice exactly. Rejects malformed fixtures: a
	 * fixed slice with an {@code import} line below body content (the
	 * directive must precede the body so the splice index lands above the
	 * fix range) or with an import-line index past the post-apply line count.
	 */
	static void insertAddedImportsAtFixedPositions(
			@Nonnull List<String> lines,
			@Nonnull List<String> fixedLines,
			@Nonnull Set<String> inputImportFqcns
	) {
		var sawNonHeader = false;
		var annotationDepth = 0;
		var inBlockComment = false;
		// Threaded across every line (header and body) so the post-body import check can tell a real
		// stray import declaration from an import-like line that is merely block-comment or text-block
		// content (e.g. a masked `import foo.*;` a fixer must ignore); the latter must not be flagged.
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < fixedLines.size(); ++i) {
			final var line = fixedLines.get(i);
			final var trimmed = line.trim();
			final var startsInsideCommentOrTextBlock = lexer.inBlockComment() || lexer.inTextBlock();
			lexer = JavaLineScanner.stateAfter(line, lexer);
			if (sawNonHeader) {
				if (!startsInsideCommentOrTextBlock && trimmed.startsWith("import ") && trimmed.endsWith(";")) {
					throw new IllegalStateException(
							"insertAddedImportsAtFixedPositions: import line below body content at fixed-slice index "
									+ i + ": '" + line + "'. Place '// imports:' directives at the top of the slice."
					);
				}
				continue;
			}
			final var startedInsideAnnotation = annotationDepth > 0;
			final var startedInBlockComment = inBlockComment;
			final var isAnnotationStart = !startedInBlockComment && trimmed.startsWith("@");
			// An import declaration carrying a trailing comment ('import foo.Foo; // note')
			// is still a header line, but not a splice target: only the clean form ending
			// in ';' is a candidate for adding a missing import. Distinguishing the two keeps
			// a commented context import from being misread as the start of the body.
			final var isImportDeclaration = !startedInBlockComment && !startedInsideAnnotation
					&& trimmed.startsWith("import ");
			final var isImport = isImportDeclaration && trimmed.endsWith(";");
			final var isHeaderLine = isImportDeclaration
					|| trimmed.isEmpty()
					|| trimmed.startsWith("package ")
					|| trimmed.startsWith("//")
					|| trimmed.startsWith("/*")
					|| trimmed.startsWith("*")
					|| isAnnotationStart
					|| startedInsideAnnotation
					|| startedInBlockComment;
			if (!isHeaderLine) {
				sawNonHeader = true;
				continue;
			}
			final var scan = scanAnnotationLine(trimmed, annotationDepth, inBlockComment);
			annotationDepth = scan.depth();
			inBlockComment = scan.inBlockComment();
			if (isImport) {
				final var fqcn = trimmed.substring("import ".length(), trimmed.length() - 1).trim();
				if (!inputImportFqcns.contains(fqcn)) {
					if (i > lines.size()) {
						throw new IllegalStateException(
								"insertAddedImportsAtFixedPositions: import '" + line + "' at fixed-slice index "
										+ i + " is past the end of post-fix lines (size=" + lines.size() + ")."
						);
					}
					lines.add(i, line);
				}
			}
		}
		if (annotationDepth > 0 || inBlockComment) {
			final var problems = new ArrayList<String>();
			if (inBlockComment)
				problems.add("unclosed block comment");
			if (annotationDepth > 0)
				problems.add("unclosed annotation (annotationDepth=" + annotationDepth + ")");
			throw new IllegalStateException(
					"insertAddedImportsAtFixedPositions: " + String.join(", ", problems)
							+ " in fixed slice (at end of slice)."
			);
		}
	}

	/**
	 * Invokes {@code fixer.fix(...)} with {@link FixContext} populated the way
	 * production {@code CheckstyleFixAction.applyFixes} populates it: a real temp
	 * file path, the violation being fixed, and the buffer version, which the
	 * caller counts as production does: the fixes it has applied to {@code lines}
	 * so far. Fixers that read {@link FixContext#getFilePath()} (e.g.
	 * {@code PreferStaticImportConstantFixer} for sibling-class resolution) or
	 * {@link FixContext#getViolationKey()} (e.g. {@code PreferMathMethodFixer},
	 * which uses it to keep the expression rewrites off an if-shape line) see what
	 * they would see on a real run, and {@link FixerAst} serves its cached parse
	 * by the version as it does there.
	 *
	 * <p>{@code violation} is null only for the deprecated fragment callers, which
	 * drive the fixer at a {@code // target:} the check never reported and so have
	 * no violation to thread. That is a real limitation of the fragment form rather
	 * than a harness shortcut: a synthesized violation would assert a key no check
	 * produced. Any fixer branch gated on the key is therefore unreachable from a
	 * fragment and needs a slice.
	 *
	 * <p>The context is cleared on the way out rather than left for the next call
	 * to overwrite. {@link FixContext} is thread-local and JUnit reuses threads, so
	 * a key or version left behind would leak into an unrelated test as a stale answer.
	 */
	@Nullable
	private static FixAttempt invokeFixWithContext(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> lines,
			int bufferVersion,
			int lineIndex,
			int column,
			@Nullable Violation violation
	) throws IOException {
		final var tempFile = File.createTempFile("fixerTest", ".java");
		try {
			FixContext.setBufferVersion(bufferVersion);
			FixContext.setFilePath(tempFile.toString());
			if (violation != null)
				FixContext.setViolation(violation);
			try {
				return fixer.fix(lines, lineIndex, column);
			}
			finally {
				FixContext.clearBufferVersion();
				FixContext.clearFilePath();
				FixContext.clearViolation();
			}
		}
		finally {
			tempFile.delete();
		}
	}

	/**
	 * The char indexes just inside the closing quote of each string literal on line
	 * {@code lineIndex} of {@code lines} that ends ahead of char {@code column}, nearest first.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<Integer> literalEnds(@Nonnull List<String> lines, int lineIndex, int column) throws CheckstyleException {
		final var ends = new ArrayList<Integer>();
		final var line = lines.get(lineIndex);
		for (var node : nodesOf(parse(lines))) {
			if (node.getType() != TokenTypes.STRING_LITERAL || node.getLineNo() != lineIndex + 1)
				continue;
			final var end = LineText.charIndexOfColumn(line, node.getColumnNo()) + node.getText().length();
			if (end <= column)
				ends.addFirst(end - 1);
		}
		return ends;
	}

	/** Every node of the tree {@code root} starts, in source order. */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> nodesOf(@Nonnull DetailAST root) {
		final var nodes = new ArrayList<DetailAST>();
		final var pending = new ArrayDeque<DetailAST>();
		for (var top = root; top != null; top = top.getNextSibling())
			pending.addLast(top);
		while (!pending.isEmpty()) {
			final var node = pending.removeFirst();
			nodes.add(node);
			for (var child = node.getLastChild(); child != null; child = child.getPreviousSibling())
				pending.addFirst(child);
		}
		return nodes;
	}

	/**
	 * A copy of {@code lines} with the chars of line {@code lineIndex} from {@code from} on
	 * overwritten by {@code text}, as many as it has.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> overwritten(@Nonnull List<String> lines, int lineIndex, int from, @Nonnull String text) {
		final var copy = new ArrayList<>(lines);
		final var line = copy.get(lineIndex);
		copy.set(lineIndex, line.substring(0, from) + text + line.substring(from + text.length()));
		return copy;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parse(@Nonnull List<String> lines) throws CheckstyleException {
		return JavaParser.parse(new FileContents(new FileText(new File("Slice.java"), lines)));
	}

	/**
	 * The names {@link #assertSupplementary} may rename: each name the slice declares (a type, field,
	 * local, parameter, lambda parameter, type parameter, enum constant, record component, or a
	 * method that overrides nothing) used on line {@code lineIndex} ahead of char {@code column}, in
	 * the order they appear there. A name that is also selected through a dot or a method reference
	 * from anything but {@code this}, imported, written as an annotation, or a {@code java.lang}
	 * type may name something outside the slice, so it is left alone.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> renameCandidates(@Nonnull List<String> lines, int lineIndex, int column) throws CheckstyleException {
		final var declared = new HashSet<String>();
		final var foreign = new HashSet<>(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE));
		final var used = new LinkedHashSet<String>();
		for (var node : nodesOf(parse(lines))) {
			switch (node.getType()) {
				case TokenTypes.ANNOTATION -> {
					final var annotationName = node.findFirstToken(TokenTypes.IDENT);
					if (annotationName != null)
						foreign.add(annotationName.getText());
				}
				case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_CONSTANT_DEF, TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF,
						TokenTypes.PARAMETER_DEF, TokenTypes.RECORD_COMPONENT_DEF, TokenTypes.RECORD_DEF, TokenTypes.TYPE_PARAMETER,
						TokenTypes.VARIABLE_DEF -> {
					// a receiver parameter (`Outer this`) declares no name
					final var declaredName = node.findFirstToken(TokenTypes.IDENT);
					if (declaredName != null)
						declared.add(declaredName.getText());
				}
				case TokenTypes.DOT, TokenTypes.METHOD_REF -> {
					if (node.getFirstChild().getType() != TokenTypes.LITERAL_THIS && node.getLastChild().getType() == TokenTypes.IDENT)
						foreign.add(node.getLastChild().getText());
				}
				case TokenTypes.IDENT -> {
					if (node.getLineNo() == lineIndex + 1 && LineText.charIndexOfColumn(lines.get(lineIndex), node.getColumnNo()) < column)
						used.add(node.getText());
				}
				case TokenTypes.LAMBDA -> {
					// a switch rule's arrow is a LAMBDA with nothing under it
					if (node.getFirstChild() != null && node.getFirstChild().getType() == TokenTypes.IDENT)
						declared.add(node.getFirstChild().getText());
				}
				case TokenTypes.METHOD_DEF -> {
					final var overrides = AstQuery.collectAnnotations(node.findFirstToken(TokenTypes.MODIFIERS)).stream()
							.anyMatch(annotation -> annotation.findFirstToken(TokenTypes.IDENT) != null && "Override".equals(annotation.findFirstToken(TokenTypes.IDENT).getText()));
					if (!overrides)
						declared.add(node.findFirstToken(TokenTypes.IDENT).getText());
				}
				default -> {}
			}
		}
		used.retainAll(declared);
		used.removeAll(foreign);
		return used;
	}

	/** A copy of {@code lines} with every identifier spelled {@code name} spelled {@code newName}. */
	@CheckReturnValue
	@Nonnull
	private static List<String> renamed(@Nonnull List<String> lines, @Nonnull String name, @Nonnull String newName) throws CheckstyleException {
		final var copy = new ArrayList<>(lines);
		final var spellings = new ArrayList<DetailAST>();
		for (var node : nodesOf(parse(lines))) {
			if (node.getType() == TokenTypes.IDENT && node.getText().equals(name))
				spellings.add(node);
		}
		spellings.sort(Comparator.comparingInt(DetailAST::getLineNo).thenComparingInt(DetailAST::getColumnNo).reversed());
		for (var node : spellings) {
			final var line = copy.get(node.getLineNo() - 1);
			final var at = LineText.charIndexOfColumn(line, node.getColumnNo());
			copy.set(node.getLineNo() - 1, line.substring(0, at) + newName + line.substring(at + name.length()));
		}
		return copy;
	}

	/**
	 * Counts net paren delta on a slice line while ignoring tokens that
	 * aren't part of annotation argument syntax: string literals, char
	 * literals, line comments, and block comments. The {@code startInBlockComment}
	 * parameter threads block-comment state across lines so a {@code /*}
	 * opened on one line and closed on another is handled correctly.
	 */
	@CheckReturnValue
	@Nonnull
	static AnnotationLineScan scanAnnotationLine(
			@Nonnull String trimmed,
			int startDepth,
			boolean startInBlockComment
	) {
		var depth = startDepth;
		var inBlockComment = startInBlockComment;
		var inStr = false;
		var inChar = false;
		final var len = trimmed.length();
		var i = 0;
		while (i < len) {
			final var c = trimmed.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < len && trimmed.charAt(i + 1) == '/') {
					inBlockComment = false;
					i += 2;
					continue;
				}
				++i;
				continue;
			}
			if (inStr) {
				if (c == '\\' && i + 1 < len) {
					i += 2;
					continue;
				}
				if (c == '"')
					inStr = false;
				++i;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < len) {
					i += 2;
					continue;
				}
				if (c == '\'')
					inChar = false;
				++i;
				continue;
			}
			if (c == '/' && i + 1 < len) {
				final var n = trimmed.charAt(i + 1);
				if (n == '/')
					break;
				if (n == '*') {
					inBlockComment = true;
					i += 2;
					continue;
				}
			}
			if (c == '"') {
				inStr = true;
				++i;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				++i;
				continue;
			}
			if (c == '(')
				++depth;
			else if (c == ')' && depth > 0)
				--depth;
			++i;
		}
		return new AnnotationLineScan(depth, inBlockComment);
	}

	/**
	 * Runs {@link #fixAll} on {@code ascii} and {@code wide}, the slice with the text ahead of the
	 * column lengthened by {@code asciiText} and by {@code wideText} in its place. False when the
	 * ASCII run, with {@code asciiText} mapped back to {@code original}, does not end as the slice
	 * does without it ({@code plain} and {@code plainSkips}); otherwise asserts the wide run ends the
	 * same once mapped back, and returns true.
	 */
	@CheckReturnValue
	private static boolean shiftKeepsOutcome(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> ascii,
			@Nonnull List<String> wide,
			@Nonnull String asciiText,
			@Nonnull String wideText,
			@Nonnull String original,
			@Nonnull String plain,
			@Nonnull List<String> plainSkips,
			@Nonnull String label,
			@Nonnull String... checkProperties
	) throws Exception {
		final var asciiSkips = new ArrayList<String>();
		final String asciiOutput;
		try {
			asciiOutput = fixAll(checkClass, fixer, new ArrayList<>(ascii), asciiSkips, checkProperties).replace(asciiText, original);
		}
		catch (CheckstyleException e) {
			return false;
		}
		final var asciiMapped = asciiSkips.stream().map(skip -> skip.replace(asciiText, original)).toList();
		if (!asciiOutput.equals(plain) || !asciiMapped.equals(plainSkips))
			return false;
		final var wideSkips = new ArrayList<String>();
		final var wideOutput = fixAll(checkClass, fixer, new ArrayList<>(wide), wideSkips, checkProperties).replace(wideText, original);
		assertEquals(asciiOutput, wideOutput, label + "supplementary characters ahead of the column changed the fix");
		assertEquals(asciiMapped, wideSkips.stream().map(skip -> skip.replace(wideText, original)).toList(), label + "supplementary characters ahead of the column changed a skip");
		return true;
	}

	/** The slice's input lines with its {@code // violation} markers taken out. */
	@CheckReturnValue
	@Nonnull
	private static List<String> strippedSlice(@Nonnull String topic, @Nonnull String caseName, @Nonnull String... checkProperties) throws Exception {
		final var slice = TestResources.loadCaseSlice(topic, caseName, PropertiesUtil.variantSuffixFromArray(checkProperties));
		return List.of(FullPipelineRunner.stripViolationComments(String.join("\n", slice.inputLines())).split("\n", -1));
	}

	/**
	 * True when the slice's first violation is reported past the first non-blank char of its line,
	 * so text ahead of it can make its code-point and char columns differ, which the
	 * {@code *-supplementary} sub-tests rely on. A slice that cannot be read or checked counts, so
	 * its sub-test reports why.
	 */
	@CheckReturnValue
	static boolean supplementaryApplies(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String topic,
			@Nonnull String caseName,
			@Nonnull Map<String, String> properties
	) {
		try {
			final var checkProperties = PropertiesUtil.propertiesAsArray(properties);
			final var violationLine = violationLineOf(topic, caseName, properties);
			final var stripped = strippedSlice(topic, caseName, checkProperties);
			return eventColumn(checkClass, stripped, violationLine, checkProperties) > LineText.extractIndent(stripped.get(violationLine)).length();
		}
		catch (Exception e) {
			return true;
		}
	}

	/** The 0-based line of the slice's first {@code // violation} marker. */
	@CheckReturnValue
	private static int violationLineOf(@Nonnull String topic, @Nonnull String caseName, @Nonnull Map<String, String> properties) throws Exception {
		final var checkProperties = PropertiesUtil.propertiesAsArray(properties);
		final var slice = TestResources.loadCaseSlice(topic, caseName, PropertiesUtil.variantSuffixFromArray(checkProperties));
		return BaseCheckTest.parseViolationMarkers(slice.inputLines(), properties).getFirst().line() - 1;
	}

	/**
	 * Returns a hint suffix for the {@code importsToAdd} mismatch error when
	 * the fixer reports an FQCN subsumed by a wildcard already declared in the
	 * Violation slice. Without the hint the assertion message is "expected
	 * [] but got [java.util.List]" with no indication that production would
	 * deduplicate via {@code insertMissingImports}, leaving the contributor
	 * to figure out that the Fixed slice should declare the specific import
	 * explicitly. Returns an empty string when no wildcard subsumption applies.
	 */
	@CheckReturnValue
	@Nonnull
	static String wildcardSubsumptionHint(
			@Nonnull Set<String> expectedImports,
			@Nonnull Set<String> actualImports,
			@Nonnull Set<String> inputImportFqcns
	) {
		final var unexpectedActual = new TreeSet<>(actualImports);
		unexpectedActual.removeAll(expectedImports);
		final var hint = new StringBuilder();
		for (var fqcn : unexpectedActual) {
			for (var existing : inputImportFqcns) {
				if (!existing.endsWith(".*"))
					continue;
				final var prefix = existing.substring(0, existing.length() - 1);
				if (fqcn.startsWith(prefix) && !fqcn.equals(existing)) {
					hint.append("\n  Hint: fixer reports '").append(fqcn)
							.append("' which is subsumed by wildcard '").append(existing)
							.append("' in the Violation slice. Declare '// imports: ").append(fqcn)
							.append("' in the Fixed slice to document the fixer's intent (production deduplicates via insertMissingImports).");
					break;
				}
			}
		}
		return hint.toString();
	}

	/** A copy of {@code lines} with {@code /* text *&#47;} put on line {@code lineIndex} at {@code spot}. */
	@CheckReturnValue
	@Nonnull
	private static List<String> withComment(@Nonnull List<String> lines, int lineIndex, @Nonnull CommentSpot spot, @Nonnull String text) {
		final var copy = new ArrayList<>(lines);
		final var line = copy.get(lineIndex);
		final var comment = spot.beforeCode() ? "/* " + text + " */ " : " /* " + text + " */";
		copy.set(lineIndex, line.substring(0, spot.at()) + comment + line.substring(spot.at()));
		return copy;
	}

	/** A copy of {@code lines} with {@code text} put on line {@code lineIndex} at char {@code at}. */
	@CheckReturnValue
	@Nonnull
	private static List<String> withInsertion(@Nonnull List<String> lines, int lineIndex, int at, @Nonnull String text) {
		final var copy = new ArrayList<>(lines);
		final var line = copy.get(lineIndex);
		copy.set(lineIndex, line.substring(0, at) + text + line.substring(at));
		return copy;
	}

	/**
	 * {@code text} with the ASCII comment {@link #withComment} put in taken out again, wherever the
	 * fixer left it: in place, on a line of its own, or joined into another comment. Whitespace is
	 * dropped too, since the fixer may space the code around the comment differently.
	 */
	@CheckReturnValue
	@Nonnull
	private static String withoutComment(@Nonnull String text) {
		final var kept = new ArrayList<String>();
		for (var line : text.split("\n", -1)) {
			final var trimmed = line.strip();
			if (!trimmed.equals("// " + SUPPLEMENTARY_STAND_IN) && !trimmed.equals("/* " + SUPPLEMENTARY_STAND_IN + " */"))
				kept.add(line);
		}
		return String.join("\n", kept)
				.replace("/* " + SUPPLEMENTARY_STAND_IN + " */", "")
				.replace(SUPPLEMENTARY_STAND_IN, "")
				.replaceAll("[ \t]+", "");
	}

	private FixerTestUtil() {
	}
}