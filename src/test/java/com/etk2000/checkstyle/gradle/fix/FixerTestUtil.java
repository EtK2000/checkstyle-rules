package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.TestResources;
import com.etk2000.checkstyle.TestResources.SnippetFixture;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
	 * Applies a single {@link FixResult} to {@code lines}. Mirrors only the
	 * clear-then-insert block of production {@code CheckstyleFixAction.applyFixes}
	 * and intentionally omits everything that's a multi-violation concern:
	 * import accumulation, skip-result / skipped-reason tracking, the
	 * {@code suppressedLine} cross-violation logic, {@link FixContext} setup, and
	 * the {@code passedThrough} blank-line allowance. Use this only for
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
		// residual-violation assertions below catch any violation left unfixed.
		final var ordered = new ArrayList<>(violations);
		ordered.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);
		final var actualImportsToAdd = new TreeSet<String>();
		for (var event : ordered) {
			final var lineIndex = event.getLine() - 1;
			// an earlier fix in this sweep can shrink the buffer past a later violation's
			// line; production applyFixes bounds-checks the index before dispatching, so
			// the sweep has to as well rather than throwing
			if (lineIndex < 0 || lineIndex >= lines.size())
				continue;
			final var charColumn = CheckstyleFixAction.tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
			if (invokeFixWithContext(fixer, lines, lineIndex, charColumn) instanceof FixResult result) {
				actualImportsToAdd.addAll(result.importsToAdd());
				applyFixResult(lines, result);
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

		final var residualViolations = BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), checkProperties);
		assertEquals(
				0,
				residualViolations.size(),
				"Case '" + topic + "/" + caseName + "': fixed content is not a fixed point ("
						+ residualViolations.size() + " residual violations from " + checkClass.getSimpleName() + ")"
		);
	}

	/**
	 * Multi-violation analog of {@link #assertCaseFix}. The slice has more
	 * than one violation-bearing line, so a single {@code fix()} call cannot
	 * produce the Fixed slice. Sorts violations bottom-up (mirroring
	 * production {@code CheckstyleFixAction.applyFixes}), invokes
	 * {@code fix()} at each violation in turn, applies each {@link FixResult},
	 * accumulates {@code importsToAdd} across calls, and asserts the final
	 * lines equal the Fixed slice. Every fix call must return a
	 * {@link FixResult} (no {@link SkipResult}).
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
		final var ordered = new ArrayList<>(violations);
		ordered.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);
		final var actualImportsToAdd = new TreeSet<String>();
		for (var event : ordered) {
			final var lineIndex = event.getLine() - 1;
			// an earlier fix in this sweep can shrink the buffer past a later violation's
			// line; production applyFixes bounds-checks the index before dispatching, so
			// the sweep has to as well rather than throwing
			if (lineIndex < 0 || lineIndex >= lines.size())
				continue;
			final var charColumn = CheckstyleFixAction.tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
			if (invokeFixWithContext(fixer, lines, lineIndex, charColumn) instanceof FixResult result) {
				actualImportsToAdd.addAll(result.importsToAdd());
				applyFixResult(lines, result);
			}
			// SkipResult / null intentionally ignored: matches production
			// CheckstyleFixAction.applyFixes behavior, where some violations
			// in a multi-violation file are skipped while others are fixed.
			// The Fixed slice text comparison below catches any divergence.
		}
		assertEquals(
				expectedImports,
				actualImportsToAdd,
				"Case '" + topic + "/" + caseName + "': importsToAdd mismatch (union across all fix calls)"
		);
		insertAddedImportsAtFixedPositions(lines, slice.fixedLines(), inputImportFqcns);
		final var linesForCompare = lines.stream()
				.filter(l -> !l.trim().startsWith("// multi-fix-expected"))
				.toList();
		assertEquals(slice.fixedLines(), linesForCompare, "Case '" + topic + "/" + caseName + "' output mismatch (after multi-violation fix)");

		final var residualViolations = BaseCheckTest.runCheckInline(checkClass, String.join("\n", lines), checkProperties);
		assertEquals(
				0,
				residualViolations.size(),
				"Case '" + topic + "/" + caseName + "': fixed content is not a fixed point ("
						+ residualViolations.size() + " residual violations from " + checkClass.getSimpleName() + ")"
		);
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
		final var sorted = new ArrayList<>(violations);
		final var bottomUp = Comparator
				.comparingInt(AuditEvent::getLine)
				.thenComparingInt(AuditEvent::getColumn)
				.reversed();
		sorted.sort(bottomUp);
		for (var event : sorted) {
			final var lineIndex = event.getLine() - 1;
			final var charColumn = CheckstyleFixAction.tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
			final var result = invokeFixWithContext(fixer, lines, lineIndex, charColumn);
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
		final var violations = BaseCheckTest.runCheckInline(checkClass, strippedInput, checkProperties);
		assertFalse(
				violations.isEmpty(),
				"Case '" + topic + "/" + caseName + "': skipped slice expected check to fire but got 0 violations"
		);

		final var lines = new ArrayList<>(List.of(strippedInput.split("\n", -1)));
		final var event = violations.getFirst();
		final var lineIndex = event.getLine() - 1;
		final var charColumn = CheckstyleFixAction.tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
		final var result = invokeFixWithContext(fixer, lines, lineIndex, charColumn);
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
				invokeFixWithContext(fixer, lines, t.line(), t.column())
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
		assertNull(invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), t.line(), t.column()));
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
				invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), t.line(), t.column())
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
				invokeFixWithContext(fixer, new ArrayList<>(fx.inputLines()), t.line(), t.column())
		);
		assertEquals(expectedReason, result.reason());
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
			// once the type body starts, the only line worth flagging is a stray import (it must precede
			// the body so its splice index lands above the fix range). A line that begins inside a block
			// comment or text block is body content, not an import declaration, even when it reads as
			// `import ...;`, so it is skipped.
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
	 * Invokes {@code fixer.fix(...)} with {@link FixContext} set to a real
	 * temp file path, mirroring how production {@code CheckstyleFixAction.applyFixes}
	 * sets {@link FixContext#setFilePath} before each fixer call. Fixers that
	 * read {@link FixContext#getFilePath()} (e.g. {@code PreferStaticImportConstantFixer}
	 * for sibling-class resolution) see a non-null path here, matching prod.
	 */
	@Nullable
	private static FixAttempt invokeFixWithContext(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> lines,
			int lineIndex,
			int column
	) throws IOException {
		final var tempFile = File.createTempFile("fixerTest", ".java");
		try {
			FixContext.setFilePath(tempFile.toString());
			try {
				return fixer.fix(lines, lineIndex, column);
			}
			finally {
				FixContext.clearFilePath();
			}
		}
		finally {
			tempFile.delete();
		}
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

	private FixerTestUtil() {
	}
}