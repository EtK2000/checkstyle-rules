package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FragmentMigrationDetector.Verdict;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Keeps {@code fragments.in.java} honest: a fragment case is only justified when
 * it is non-compilable or drives the fixer at a synthetic {@code // target:} the
 * check never reports. {@link FragmentMigrationDetector} flags any case that is
 * instead a disguised slice: a compilable check-silent body (a clean case), a
 * check-firing body the fixer reproduces as a fixed point (a fix-slice), or a
 * {@code // target:} that duplicates a position the check already reports (a fix-
 * or skip-slice).
 *
 * <p>The invariant is that nothing is flagged; the test fails listing any case
 * that should move. Resolve one by migrating it to the appropriate slice
 * ({@code cases.clean.java}, or {@code cases.in.java}/{@code cases.out.java}) per
 * docs/testing.md, or deleting it if an existing slice already covers it.
 */
public class FragmentMigrationGuardTest {
	@CheckReturnValue
	@Nonnull
	private static CheckstyleFixer columnBranchingFixer(int fixColumn, @Nonnull List<String> atColumn, @Nonnull List<String> elsewhere) {
		return (lines, lineIndex, column) -> new FixResult(lineIndex, lineIndex, column == fixColumn ? atColumn : elsewhere);
	}

	@CheckReturnValue
	@Nonnull
	private static CheckstyleFixer columnSensitiveFixer(int fixColumn, @Nonnull List<String> replacement) {
		return (lines, lineIndex, column) -> column == fixColumn
				? new FixResult(lineIndex, lineIndex, replacement)
				: new SkipResult("stub-off-target");
	}

	@CheckReturnValue
	@Nonnull
	private static CheckstyleFixer nullFixer() {
		return (lines, lineIndex, column) -> null;
	}

	@CheckReturnValue
	@Nonnull
	private static CheckstyleFixer replaceFixer(@Nonnull List<String> replacement) {
		return (lines, lineIndex, column) -> new FixResult(lineIndex, lineIndex, replacement);
	}

	@CheckReturnValue
	@Nonnull
	private static CheckstyleFixer skipFixer() {
		return (lines, lineIndex, column) -> new SkipResult("stub-skip");
	}

	@Test
	public void classifyFixerSkipsWhenFixerReturnsSkip() {
		final var in = List.of("\tlong x = 100l;");
		final var out = List.of("\tlong x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, out);
		assertEquals(Verdict.FIXER_SKIPS, verdict);
	}

	@Test
	public void classifyMigratableCleanWhenCheckNeverFires() {
		// compiles and UpperEll stays silent (already uppercase), so it is a clean
		// input for cases.clean.java, not a fixer-robustness fragment.
		final var clean = List.of("\tlong x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), clean, clean);
		assertEquals(Verdict.MIGRATABLE_CLEAN, verdict);
	}

	@Test
	public void classifyMigratableCleanWhenTargetPresentButCheckNeverFires() {
		// a // target: on a check-silent body drives the fixer where production
		// never would, so the directive is meaningless: still a clean case.
		final var in = List.of("// target: col=10", "\tlong x = 100L;");
		final var out = List.of("\tlong x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE_CLEAN, verdict);
	}

	@Test
	public void classifyMigratableWhenAnnotatedMemberParsesViaEarlierWrapper() {
		// annotation followed by a member parses under wrapper 1, so wrapper 4 is
		// never reached; the value= redundancy is still fixed to a fixed point.
		final var in = List.of("\t@A(value = 5)", "\tint field;");
		final var out = List.of("\t@A(5)", "\tint field;");
		final var verdict = FragmentMigrationDetector.classify(RedundantAnnotationSyntaxCheck.class, new RedundantAnnotationSyntaxFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE, verdict);
	}

	@Test
	public void classifyMigratableWhenAppendedMemberDoesNotShiftMultilineFixTarget() {
		// multi-line trailing bare annotation: wrapper 4's appended int __x; sits
		// after the fix region, so the fixer still targets the annotation lines
		// (here collapsing the short value onto one line).
		final var in = List.of("\t@A(", "\t\tvalue = 5)");
		final var out = List.of("\t@A(5)");
		final var verdict = FragmentMigrationDetector.classify(RedundantAnnotationSyntaxCheck.class, new RedundantAnnotationSyntaxFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE, verdict);
	}

	@Test
	public void classifyMigratableWhenCheckFiresAndFixReproducesFixedPoint() {
		final var in = List.of("\tlong x = 100l;");
		final var out = List.of("\tlong x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE, verdict);
	}

	@Test
	public void classifyMigratableWhenLeadingIndentIsTabThenSpace() {
		// NoSpaceIndent (^ (?!\*)(?!@)) only fires when the FIRST char is a space;
		// a leading tab exempts a tab-then-space line, so it stays migratable.
		final var in = List.of("\t long x = 100l;");
		final var out = List.of("\t long x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE, verdict);
	}

	@Test
	public void classifyMigratableWhenTrailingBareAnnotationParsesViaMemberAppend() {
		// bare @A(value = 5) with nothing after it fails wrappers 1-3; wrapper 4
		// appends int __x; so it parses and the value= redundancy is judged.
		final var in = List.of("\t@A(value = 5)");
		final var out = List.of("\t@A(5)");
		final var verdict = FragmentMigrationDetector.classify(RedundantAnnotationSyntaxCheck.class, new RedundantAnnotationSyntaxFixer(), in, out);
		assertEquals(Verdict.MIGRATABLE, verdict);
	}

	@Test
	public void classifyNoFixWhenNoFixedSibling() {
		final var in = List.of("\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, null);
		assertEquals(Verdict.NO_FIX, verdict);
	}

	@Test
	public void classifyNoFixWhenReportedFixesTargetSkips() {
		// the fixer fixes only at the reported char 10 and skips at the synthetic
		// col=5, so the target's no-op outcome differs from the reported fix: justified.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(10, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.NO_FIX, verdict);
	}

	@Test
	public void classifyNoFixWhenTargetFixesDifferentDocument() {
		// both sites fix, but to different documents (the fixer branches on column),
		// so the target is not redundant with the reported fix: a justified fragment.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var fixer = columnBranchingFixer(10, List.of("\tlong x = 100L;"), List.of("\tlong y = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.NO_FIX, verdict);
	}

	@Test
	public void classifyNoFixWhenTargetOutcomeDiffersFromReported() {
		// the fixer fixes at the synthetic col=5 (which the check never reports) and
		// skips at the reported char 10, so the target exercises a distinct path: the
		// one shape in which a no-.out fragment stays justified.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(5, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.NO_FIX, verdict);
	}

	@Test
	public void classifyNonCompilableWhenBodyHasSyntaxError() {
		final var in = List.of("\tlong x = ;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, in);
		assertEquals(Verdict.NON_COMPILABLE, verdict);
	}

	@Test
	public void classifyNonCompilableWhenTrailingBareAnnotationUnclosedEvenWithMemberAppend() {
		// unclosed paren: even wrapper 4's appended member cannot make @B( parse.
		final var in = List.of("\t@B(");
		final var verdict = FragmentMigrationDetector.classify(RedundantAnnotationSyntaxCheck.class, new RedundantAnnotationSyntaxFixer(), in, in);
		assertEquals(Verdict.NON_COMPILABLE, verdict);
	}

	@Test
	public void classifyNotFixedPointWhenFixedOutputStillViolates() {
		final var in = List.of("\tlong x = 100l;");
		final var out = List.of("\tlong x = 200l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, replaceFixer(out), in, out);
		assertEquals(Verdict.NOT_FIXED_POINT, verdict);
	}

	@Test
	public void classifyOutputDiffersWhenFixDoesNotMatchOut() {
		final var in = List.of("\tlong x = 100l;");
		final var out = List.of("\tlong x = 999L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.OUTPUT_DIFFERS, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenDeclaredColumnEqualsReportedAndAlsoFixedPoint() {
		// redundancy wins over the fixed-point MIGRATABLE verdict: the directive
		// duplicates the position the check reports, so the case should be a slice.
		final var in = List.of("// target: col=10", "\tlong x = 100l;");
		final var out = List.of("\tlong x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenDeclaredColumnEqualsReportedWithoutFixedSibling() {
		// redundancy is independent of the fix outcome: even with no .out sibling
		// (a skip fragment), a directive at the reported position is redundant.
		final var in = List.of("// target: col=10", "\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenDeclaredColumnMatchesSecondReportedViolation() {
		// the fixer fixes only at char 20 (the SECOND reported violation) and skips at
		// char 10, and the target sits at 20: outcome redundancy must scan every
		// reported position, not just the first.
		final var in = List.of("// target: col=20", "\tlong x = 100l, y = 200l;");
		final var fixer = columnSensitiveFixer(20, List.of("\tlong x = 100L, y = 200L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenFixerReturnsNull() {
		// the fixer bare-nulls at the reported site, but the // target: still
		// duplicates that position: a disguised skip-slice whose fixer must be
		// changed to return a SkipResult, not a legitimate fragment.
		final var in = List.of("// target: col=10", "\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, nullFixer(), in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenSameLineReplaceFixerSameOutcome() {
		// replaceFixer ignores the column, so driving it at the synthetic col=5 and at
		// the reported char 10 replaces the same body line identically: redundant even
		// though the target position differs from the reported one.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, replaceFixer(List.of("\tlong x = 100L;")), in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenTargetColumnMatchesButLineDiffersSameOutcome() {
		// the fixer fixes at the reported char 10; the target column also matches but
		// body line 1 does not exist (the body is one line), so the out-of-bounds line
		// makes the target redundant rather than a synthetic justification.
		final var in = List.of("// target: line=1 col=10", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(10, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenTargetColumnOutOfBounds() {
		// col=999 is past the body line's end while the reported char 10 fixes, so the
		// out-of-bounds guard makes the target redundant.
		final var in = List.of("// target: col=999", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(10, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenTargetLineNegative() {
		// a negative target line is out of bounds (and must not clamp to body line 0)
		// while the reported char 10 fixes, so the guard makes the target redundant.
		final var in = List.of("// target: line=-1 col=10", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(10, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenTargetLinePastEnd() {
		// the target line is past the one-line body while the reported char 10 fixes,
		// so the out-of-bounds guard makes the target redundant.
		final var in = List.of("// target: line=5 col=10", "\tlong x = 100l;");
		final var fixer = columnSensitiveFixer(10, List.of("\tlong x = 100L;"));
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, fixer, in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenUnreportedColumnNullFixerSameOutcome() {
		// nullFixer leaves the document unchanged at both the synthetic col=5 and the
		// reported char 10, so their outcomes match: a disguised skip-slice.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, nullFixer(), in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyRedundantTargetWhenUnreportedColumnSameOutcome() {
		// col=5 is in-bounds but not where UpperEll reports (char 10); the fixer skips
		// at both, so the outcomes match and the synthetic target is redundant.
		final var in = List.of("// target: col=5", "\tlong x = 100l;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), in, null);
		assertEquals(Verdict.REDUNDANT_TARGET, verdict);
	}

	@Test
	public void classifyStyleInvariantWhenCheckSilentSpaceIndentedBody() {
		// check-silent AND space-indented: it would be a clean case, but a clean
		// slice cannot carry space indentation (NoSpaceIndent), so it stays a
		// fragment rather than migrating to cases.clean.java.
		final var clean = List.of("    long x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, skipFixer(), clean, clean);
		assertEquals(Verdict.STYLE_INVARIANT, verdict);
	}

	@Test
	public void classifyStyleInvariantWhenSpaceIndentedBody() {
		// otherwise MIGRATABLE, but a space-indented body would trip NoSpaceIndent
		// as a slice (which the policy refuses to suppress), so it stays a fragment.
		final var in = List.of("    long x = 100l;");
		final var out = List.of("    long x = 100L;");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.STYLE_INVARIANT, verdict);
	}

	@Test
	public void classifyStyleInvariantWhenSpaceIndentedTrailingBareAnnotation() {
		// mirrors redundantannotationsyntax/extract_indent_space_value_line: the value
		// is long enough that collapsing it would exceed the max line width, so the
		// fixer keeps it multi-line (minimal value-line edit) and the space-indented
		// value line keeps it a fragment.
		final var value = "\"" + "a".repeat(115) + "\"";
		final var in = List.of("\t@A(", "    value = " + value + ")");
		final var out = List.of("\t@A(", "    " + value + ")");
		final var verdict = FragmentMigrationDetector.classify(RedundantAnnotationSyntaxCheck.class, new RedundantAnnotationSyntaxFixer(), in, out);
		assertEquals(Verdict.STYLE_INVARIANT, verdict);
	}

	@Test
	public void classifyStyleInvariantWhenTrailingWhitespaceBody() {
		final var in = List.of("\tlong x = 100l; ");
		final var out = List.of("\tlong x = 100L; ");
		final var verdict = FragmentMigrationDetector.classify(UpperEllCheck.class, new UpperEllFixer(), in, out);
		assertEquals(Verdict.STYLE_INVARIANT, verdict);
	}

	@Test
	public void everyFragmentTopicIsClassified() {
		final var mapped = FragmentMigrationDetector.topicChecks().keySet();
		final var unmapped = FragmentMigrationDetector.UNMAPPED_TOPICS.keySet();
		final var unclassified = new ArrayList<String>();
		for (var topic : FragmentMigrationDetector.fragmentTopics()) {
			if (!mapped.contains(topic) && !unmapped.contains(topic))
				unclassified.add(topic);
		}
		if (!unclassified.isEmpty()) {
			fail(
					"Fragment topic(s) not classified: " + unclassified
							+ ". Add each to FragmentMigrationDetector's topic-to-check map (so its fragments "
							+ "are scanned), or to UNMAPPED_TOPICS with a concrete reason (no single AbstractCheck)."
			);
		}
	}

	@Test
	public void mappedAndUnmappedTopicsAreDisjoint() {
		final var both = new TreeSet<>(FragmentMigrationDetector.topicChecks().keySet());
		both.retainAll(FragmentMigrationDetector.UNMAPPED_TOPICS.keySet());
		assertTrue(both.isEmpty(), "topic is both mapped to a check and listed as unmapped: " + both);
	}

	@Test
	public void noUnmigratedCompilableFragments() {
		final var flagged = FragmentMigrationDetector.scanFlagged();
		if (!flagged.isEmpty()) {
			fail(
					"\nFragment case(s) that are really disguised case slices and must be migrated (a fragment is only"
							+ " for a non-compilable body or a synthetic // target: the check never reports):\n"
							+ "  - check never fires -> move to cases.clean.java (a clean input);\n"
							+ "  - check fires and the fixer reproduces .out as a fixed point -> a cases.in.java /"
							+ " cases.out.java fix-slice;\n"
							+ "  - // target: duplicates a position the check reports -> a slice: a FixResult stays a"
							+ " fix-slice; a null/SkipResult means change the fixer to return a SkipResult and add a"
							+ " // skip-reason: skip-slice.\n"
							+ "Migrate each per docs/testing.md, or delete it if an existing slice already covers it:"
							+ "\n  " + String.join("\n  ", flagged)
			);
		}
	}

	@Test
	public void topicChecksResolvesDerivedNames() {
		final var map = FragmentMigrationDetector.topicChecks();
		assertEquals("UpperEllCheck", map.get("upperell").getSimpleName(), "derived topic name");
		assertEquals("ExplicitInitializationCheck", map.get("explicitinitialization").getSimpleName(), "derived topic name");
	}
}