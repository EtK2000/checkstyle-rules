package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Decides whether a {@code fragments.in.java} case is really a disguised case
 * slice. A fragment is justified ONLY when it is non-compilable or drives the
 * fixer at a synthetic {@code // target:} the check never reports; anything else
 * is a case that belongs under {@code cases.*.java}. Three migratable shapes are
 * flagged, one per slice kind:
 * <ul>
 *   <li>{@link Verdict#MIGRATABLE_CLEAN}: the body compiles and the check never
 *       fires, so it is a clean input that belongs in {@code cases.clean.java}.
 *       The fixer is never invoked where the check is silent, so a
 *       {@code // target:} on such a body tests nothing production reaches.
 *   <li>{@link Verdict#MIGRATABLE}: the check fires and the fixer reproduces
 *       {@code .out} as a fixed point, exactly the {@code assertCaseFix} contract
 *       for a {@code cases.in.java}/{@code cases.out.java} fix-slice.
 *   <li>{@link Verdict#REDUNDANT_TARGET}: the {@code // target:} duplicates a
 *       position the check already reports, so the case should derive its fix site
 *       from the check as a slice does. This holds regardless of the fixer's
 *       outcome: a {@link FixResult} is a fix-slice, a {@link SkipResult} is a
 *       {@code // skip-reason:} skip-slice, and a bare {@code null} at a reported
 *       site is a fixer that should return a {@link SkipResult} (change it, then
 *       migrate to a skip-slice), never a fragment excuse.
 * </ul>
 * Used by {@code FragmentMigrationGuardTest}.
 *
 * <p>Each fragment body is wrapped into a minimal compilation unit (class member,
 * method-body, constructor-body, member-with-appended-field, interface /
 * {@code @interface} / enum member, or, last, an empty wrapper that judges the
 * body as its own top-level unit) so the check can run on it; the shape chosen is
 * the first under which the check FIRES (else the first that merely parses), so a
 * violation that needs a particular type context is judged there rather than
 * misread as clean. A body that is itself a package/import declaration or one or
 * more top-level type declarations cannot nest inside any of the class shapes, so
 * only the empty top-level wrapper compiles it. The fixer then runs at the
 * check-reported site and the result is compared to the {@code .out} body wrapped
 * the same way. The appended-member shape adds an annotatable {@code int __x;} so
 * a body ending in a bare annotation ({@code @A(x)} with nothing after it) parses.
 *
 * <p>A would-be-flagged case whose body trips a non-suppressible style invariant
 * ({@code NoSpaceIndent} or {@code NoTrailingWhitespace}, the two the migration
 * policy refuses to suppress) is demoted to {@link Verdict#STYLE_INVARIANT} and
 * stays a fragment, since it would fail the full-config lint as a slice.
 *
 * <p>Documented limitations (cases the detector cannot fully judge):
 * <ul>
 *   <li>Topics in {@link #UNMAPPED_TOPICS} have no single {@link AbstractCheck}
 *       to run (regex-rule fixers bound to {@code MODULE_ID_FIXERS}, the shared
 *       {@code DeleteLineFixer}, or full-pipeline integration fixtures), so they
 *       are not scanned.
 *   <li>Each check runs under its DEFAULT configuration. A case that fires only
 *       under non-default check properties reads as check-silent and so is flagged
 *       {@link Verdict#MIGRATABLE_CLEAN}; confirm the check is genuinely silent
 *       under the intended properties before making it a clean case.
 *   <li>Only the single topic check runs, not the full fix pipeline, so a case
 *       that is single-check-migratable but needs a {@code cases.fixed.java}
 *       override for a sibling fixer is still flagged; migrating it then
 *       requires authoring that override.
 * </ul>
 */
final class FragmentMigrationDetector {
	enum Verdict {
		FIXER_SKIPS,
		MIGRATABLE,
		MIGRATABLE_CLEAN,
		NO_FIX,
		NON_COMPILABLE,
		NOT_FIXED_POINT,
		OUTPUT_DIFFERS,
		REDUNDANT_TARGET,
		STYLE_INVARIANT
	}

	private record TargetPoint(int line, int column) {}

	private record WrapEval(@Nonnull Wrapper wrapper, @Nonnull List<AuditEvent> violations) {}

	private record Wrapper(@Nonnull List<String> prefix, @Nonnull List<String> suffix) {}

	private static final List<Wrapper> WRAPPERS = List.of(
			new Wrapper(List.of("class __FragWrap {"), List.of("}")),
			new Wrapper(List.of("class __FragWrap {", "\tvoid __frag() {"), List.of("\t}", "}")),
			new Wrapper(List.of("class __FragWrap {", "\t__FragWrap() {"), List.of("\t}", "}")),
			// Append an annotatable member so a body ending in a bare annotation
			// (@A(x) with nothing after it) parses and can be judged.
			new Wrapper(List.of("class __FragWrap {"), List.of("\tint __x;", "}")),
			// Non-class type contexts. A modifier is only redundant on an interface
			// or @interface member (public/abstract/static/final are implicit there),
			// so a body whose violation needs that context is silent under the class
			// shapes above and would otherwise read as a clean case.
			new Wrapper(List.of("interface __FragWrap {"), List.of("}")),
			new Wrapper(List.of("@interface __FragWrap {"), List.of("}")),
			new Wrapper(List.of("enum __FragWrap {", "\t;"), List.of("}")),
			// Top-level compilation unit (empty prefix/suffix). A body that is itself a
			// package or import declaration, or one or more top-level type declarations,
			// cannot nest inside any class/interface/enum shape above, so it is judged
			// as-is. Placed last so it only wins when no nesting shape parses-and-fires.
			new Wrapper(List.of(), List.of())
	);
	static final Map<String, String> UNMAPPED_TOPICS = Map.ofEntries(
			Map.entry("blanklineafterbreak", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("blanklineafterclassbrace", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("blanklinebeforeclosingbrace", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("doubleblankline", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("trailingnewline", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("trailingwhitespace", "regex-rule fixer (MODULE_ID_FIXERS), not an AbstractCheck"),
			Map.entry("deleteline", "shared DeleteLineFixer (RedundantImport); no dedicated check"),
			Map.entry("integration", "full-pipeline integration fixtures (assertFullFix); no single check"),
			Map.entry("integrationtrailingws", "full-pipeline integration fixtures (assertFullFix); no single check"),
			Map.entry("hintaccuracy", "hint-message accuracy fixtures; no single check/fixer"),
			Map.entry("nofix", "no-fix pipeline fixtures; no single check/fixer"),
			Map.entry("preferimportfixcontext", "fix-context/sibling fixtures; SAME_PACKAGE_SIBLING strip needs an on-disk sibling the detector cannot supply"),
			Map.entry("preferstaticimportconstantfixcontext", "fix-context variant fixtures; not a standalone check topic"),
			Map.entry("unusedimportsfixcontext", "fix-context fixtures; check-silent fixer branches (re-verify/malformed/wildcard) the single check never drives")
	);
	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");
	// The two per-line style invariants the project refuses to suppress for a slice
	// (checkstyle-test-resources.xml ids NoSpaceIndent and NoTrailingWhitespace). A
	// body that trips either can only stay a fragment, never migrate to a slice.
	private static final Pattern SPACE_INDENT = Pattern.compile("^ (?!\\*)(?!@)");
	private static final Pattern TRAILING_WHITESPACE = Pattern.compile("[ \\t]+$");
	private static final String FRAGMENTS_IN = "fragments.in.java";
	private static final String TARGET_DIRECTIVE = "// " + "target:";
	private static final String TARGET_COL_PREFIX = TARGET_DIRECTIVE + " col=";
	private static final String TARGET_LINE_PREFIX = TARGET_DIRECTIVE + " line=";

	/**
	 * The document that results from invoking the fixer at {@code (lineIndex,
	 * column)}: an in-bounds {@link FixResult} is applied, while a {@link SkipResult},
	 * a bare {@code null}, or an out-of-range line/result leaves the document
	 * unchanged. Mirrors the pipeline's apply-or-skip so the outcome comparison
	 * reflects what production would do at each site.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> applyAttempt(@Nonnull CheckstyleFixer fixer, @Nonnull List<String> wrappedIn, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= wrappedIn.size())
			return wrappedIn;
		final var working = new ArrayList<>(wrappedIn);
		final var attempt = invokeFix(fixer, working, lineIndex, column);
		if (attempt instanceof FixResult result
				&& result.startLine() >= 0 && result.startLine() <= working.size() && result.endLine() < working.size()) {
			FixerTestUtil.applyFixResult(working, result);
			return working;
		}
		return wrappedIn;
	}

	@CheckReturnValue
	@Nonnull
	static Verdict classify(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull List<String> inBody,
			@Nullable List<String> outBody
	) {
		final var codeIn = stripTargetDirectives(inBody);
		final var codeOut = outBody == null ? null : stripTargetDirectives(outBody);
		final var eval = firstFiringWrapper(checkClass, codeIn);
		if (eval == null)
			return Verdict.NON_COMPILABLE;
		// A compilable body the check never fires on is a disguised clean case: the
		// fixer is never invoked at a position the check does not report, so driving
		// it at a synthetic // target: here tests nothing production reaches. It
		// belongs in cases.clean.java unless it trips a non-suppressible style
		// invariant, which no lint-clean slice may carry.
		if (eval.violations().isEmpty())
			return violatesStyleInvariant(codeIn) ? Verdict.STYLE_INVARIANT : Verdict.MIGRATABLE_CLEAN;

		final var wrappedIn = wrap(eval.wrapper(), codeIn);
		final var fixedPoint = fixedPointVerdict(checkClass, fixer, eval, wrappedIn, codeOut);
		final var declaredTarget = parseTarget(inBody);
		// A // target: that duplicates what the check already drives is a disguised
		// slice: a slice derives its fix site from the check rather than declaring a
		// synthetic one. The .out-present path stays on position equality (its
		// fixedPoint verdict already flags a reproduced fix as a fix-slice); the
		// no-.out path uses outcome equality, so a target whose fixer result matches
		// the check-reported result (a skip-slice, a null the fixer should turn into a
		// SkipResult, or a fix duplicating the reported fix) is caught as a disguised
		// slice rather than presumed a justified NO_FIX fragment.
		final var redundant = declaredTarget != null && (codeOut != null
				? matchesReportedPosition(declaredTarget, eval, wrappedIn)
				: targetOutcomeMatchesReported(fixer, eval, wrappedIn, declaredTarget));
		final var verdict = redundant ? Verdict.REDUNDANT_TARGET : fixedPoint;
		// A migratable body that trips a non-suppressible style invariant (space
		// indent, trailing whitespace) can only stay a fragment: it would fail the
		// full-config lint as a slice and the policy forbids suppressing those.
		if ((verdict == Verdict.MIGRATABLE || verdict == Verdict.REDUNDANT_TARGET)
				&& (violatesStyleInvariant(codeIn) || (codeOut != null && violatesStyleInvariant(codeOut))))
			return Verdict.STYLE_INVARIANT;
		return verdict;
	}

	/**
	 * Wraps the body into each candidate compilation unit in turn and returns the
	 * first shape under which the check FIRES, so a body whose violation needs a
	 * particular type context (an interface member for a redundant modifier, a
	 * method body for a local-variable rule) is judged in that context rather than
	 * misread as clean under a class shape that happens to parse but stay silent.
	 * Falls back to the first shape that merely parses when none fire (a genuinely
	 * clean body), or {@code null} when none parse (non-compilable).
	 */
	@CheckReturnValue
	@Nullable
	private static WrapEval firstFiringWrapper(@Nonnull Class<? extends AbstractCheck> checkClass, @Nonnull List<String> inBody) {
		WrapEval firstParsing = null;
		for (var w : WRAPPERS) {
			final List<AuditEvent> violations;
			try {
				violations = BaseCheckTest.runCheckInline(checkClass, String.join("\n", wrap(w, inBody)));
			}
			catch (Exception doesNotParseUnderThisShape) {
				continue;
			}
			if (!violations.isEmpty())
				return new WrapEval(w, violations);
			if (firstParsing == null)
				firstParsing = new WrapEval(w, violations);
		}
		return firstParsing;
	}

	@CheckReturnValue
	@Nonnull
	private static Verdict fixedPointVerdict(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull CheckstyleFixer fixer,
			@Nonnull WrapEval eval,
			@Nonnull List<String> wrappedIn,
			@Nullable List<String> outBody
	) {
		if (outBody == null)
			return Verdict.NO_FIX;
		final var event = eval.violations().getFirst();
		final var lineIndex = event.getLine() - 1;
		if (lineIndex < 0 || lineIndex >= wrappedIn.size())
			return Verdict.FIXER_SKIPS;
		final var charColumn = CheckstyleFixAction.tabColumnToCharIndex(wrappedIn.get(lineIndex), event.getColumn() - 1);

		final var working = new ArrayList<>(wrappedIn);
		final var attempt = invokeFix(fixer, working, lineIndex, charColumn);
		if (!(attempt instanceof FixResult result))
			return Verdict.FIXER_SKIPS;
		if (result.startLine() < 0 || result.startLine() > working.size() || result.endLine() >= working.size())
			return Verdict.OUTPUT_DIFFERS;
		FixerTestUtil.applyFixResult(working, result);

		if (!working.equals(wrap(eval.wrapper(), outBody)))
			return Verdict.OUTPUT_DIFFERS;
		try {
			return BaseCheckTest.runCheckInline(checkClass, String.join("\n", working)).isEmpty()
					? Verdict.MIGRATABLE
					: Verdict.NOT_FIXED_POINT;
		}
		catch (Exception fixedDoesNotParse) {
			return Verdict.NOT_FIXED_POINT;
		}
	}

	@CheckReturnValue
	@Nonnull
	static List<String> fragmentTopics() {
		try (var paths = Files.walk(INPUTS_ROOT)) {
			return paths.filter(p -> p.getFileName().toString().equals(FRAGMENTS_IN))
					.map(p -> p.getParent().getFileName().toString())
					.sorted()
					.toList();
		}
		catch (IOException e) {
			throw new IllegalStateException("Failed to enumerate fragment topics under " + INPUTS_ROOT, e);
		}
	}

	@Nullable
	private static FixAttempt invokeFix(@Nonnull CheckstyleFixer fixer, @Nonnull List<String> lines, int lineIndex, int column) {
		try {
			final var tempFile = File.createTempFile("fragMigration", ".java");
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
		catch (IOException e) {
			throw new IllegalStateException("Failed to create temp file for fixer invocation", e);
		}
	}

	/**
	 * Whether {@code target} (declared by a {@code // target:} directive, in body
	 * coordinates) equals a position the check actually reports. The check runs on
	 * the wrapped body, so each violation's line is shifted back into body space by
	 * the wrapper prefix size and its tab-expanded column converted to a char index.
	 * A directive that matches is redundant: the case should be a slice, which
	 * derives the fix position from the check rather than declaring a synthetic one.
	 */
	@CheckReturnValue
	private static boolean matchesReportedPosition(
			@Nonnull TargetPoint target,
			@Nonnull WrapEval eval,
			@Nonnull List<String> wrappedIn
	) {
		final var prefixSize = eval.wrapper().prefix().size();
		for (var v : eval.violations()) {
			final var wrappedLineIndex = v.getLine() - 1;
			if (wrappedLineIndex < 0 || wrappedLineIndex >= wrappedIn.size())
				continue;
			if (wrappedLineIndex - prefixSize != target.line())
				continue;
			if (CheckstyleFixAction.tabColumnToCharIndex(wrappedIn.get(wrappedLineIndex), v.getColumn() - 1) == target.column())
				return true;
		}
		return false;
	}

	/**
	 * Parses the case's {@code // target:} directive into a body-coordinate point,
	 * or {@code null} when the case has none. {@code col=N} points at the following
	 * code line (char index {@code N}); {@code line=L col=C} is an explicit point.
	 * Mirrors {@code TestResources}'s directive semantics.
	 */
	@CheckReturnValue
	@Nullable
	private static TargetPoint parseTarget(@Nonnull List<String> rawBody) {
		var nonDirectiveBefore = 0;
		for (var line : rawBody) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(TARGET_LINE_PREFIX)) {
				final var rest = trimmed.substring(TARGET_LINE_PREFIX.length()).trim();
				final var colIdx = rest.indexOf("col=");
				if (colIdx < 0)
					return null;
				try {
					return new TargetPoint(
							Integer.parseInt(rest.substring(0, colIdx).trim()),
							Integer.parseInt(rest.substring(colIdx + "col=".length()).trim())
					);
				}
				catch (NumberFormatException malformed) {
					return null;
				}
			}
			if (trimmed.startsWith(TARGET_COL_PREFIX)) {
				try {
					return new TargetPoint(nonDirectiveBefore, Integer.parseInt(trimmed.substring(TARGET_COL_PREFIX.length()).trim()));
				}
				catch (NumberFormatException malformed) {
					return null;
				}
			}
			if (!trimmed.startsWith(TARGET_DIRECTIVE))
				++nonDirectiveBefore;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	static List<String> scanFlagged() {
		final var checks = topicChecks();
		final var flagged = new ArrayList<String>();
		for (var topic : fragmentTopics()) {
			final var checkClass = checks.get(topic);
			if (checkClass == null)
				continue;
			final var fixer = CheckstyleFixAction.FIXERS.get(checkClass.getName());
			if (fixer == null)
				continue;
			for (var caseName : TestResources.caseNames(topic)) {
				final var fx = TestResources.loadCase(topic, caseName);
				final var outBody = fx.hasFixed() ? fx.fixedLines() : null;
				final var verdict = classify(checkClass, fixer, fx.inputLines(), outBody);
				if (verdict == Verdict.MIGRATABLE || verdict == Verdict.MIGRATABLE_CLEAN || verdict == Verdict.REDUNDANT_TARGET)
					flagged.add(topic + "/" + caseName);
			}
		}
		flagged.sort(null);
		return List.copyOf(flagged);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> stripTargetDirectives(@Nonnull List<String> lines) {
		final var out = new ArrayList<String>(lines.size());
		for (var line : lines) {
			if (!line.trim().startsWith(TARGET_DIRECTIVE))
				out.add(line);
		}
		return out;
	}

	/**
	 * Whether the fixer, driven at {@code target} (a {@code // target:} in body
	 * coordinates), produces the same resulting document as when driven at any
	 * position the check reports. When it does, the synthetic target adds nothing a
	 * slice (which derives its fix site from the check) would not already cover, so
	 * the fragment is a disguised slice.
	 */
	@CheckReturnValue
	private static boolean targetOutcomeMatchesReported(
			@Nonnull CheckstyleFixer fixer,
			@Nonnull WrapEval eval,
			@Nonnull List<String> wrappedIn,
			@Nonnull TargetPoint target
	) {
		final var prefixSize = eval.wrapper().prefix().size();
		if (targetOutOfBounds(target, wrappedIn, prefixSize, eval.wrapper().suffix().size()))
			return true;
		final var targetDoc = applyAttempt(fixer, wrappedIn, target.line() + prefixSize, target.column());
		for (var v : eval.violations()) {
			final var reportedLine = v.getLine() - 1;
			if (reportedLine < 0 || reportedLine >= wrappedIn.size())
				continue;
			final var reportedColumn = CheckstyleFixAction.tabColumnToCharIndex(wrappedIn.get(reportedLine), v.getColumn() - 1);
			if (applyAttempt(fixer, wrappedIn, reportedLine, reportedColumn).equals(targetDoc))
				return true;
		}
		return false;
	}

	/**
	 * Whether a {@code // target:} falls outside the fragment body: a negative or
	 * past-end body line, or a column past the target line's end. Bounds are
	 * body-relative (not wrapped-unit-relative), so a target just past the body
	 * cannot land on a wrapper line. An out-of-bounds target can never denote a
	 * fixer-reachable site the check omits, so the caller treats it as redundant.
	 */
	@CheckReturnValue
	private static boolean targetOutOfBounds(@Nonnull TargetPoint target, @Nonnull List<String> wrappedIn, int prefixSize, int suffixSize) {
		if (target.line() < 0 || target.line() >= wrappedIn.size() - prefixSize - suffixSize)
			return true;
		final var bodyLine = wrappedIn.get(target.line() + prefixSize);
		return target.column() < 0 || target.column() > bodyLine.length();
	}

	@CheckReturnValue
	@Nonnull
	static Map<String, Class<? extends AbstractCheck>> topicChecks() {
		try {
			final var map = new HashMap<String, Class<? extends AbstractCheck>>();
			for (var fqcn : CheckstyleFixAction.FIXERS.keySet()) {
				final var cls = Class.forName(fqcn).asSubclass(AbstractCheck.class);
				map.put(BaseCheckTest.deriveTopic(cls), cls);
			}
			return Map.copyOf(map);
		}
		catch (ClassNotFoundException e) {
			throw new IllegalStateException("Failed to resolve a check class for the fragment-migration map", e);
		}
	}

	@CheckReturnValue
	private static boolean violatesStyleInvariant(@Nonnull List<String> lines) {
		for (var line : lines) {
			if (SPACE_INDENT.matcher(line).find() || TRAILING_WHITESPACE.matcher(line).find())
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> wrap(@Nonnull Wrapper w, @Nonnull List<String> body) {
		final var out = new ArrayList<String>(w.prefix().size() + body.size() + w.suffix().size());
		out.addAll(w.prefix());
		out.addAll(body);
		out.addAll(w.suffix());
		return out;
	}

	private FragmentMigrationDetector() {
	}
}