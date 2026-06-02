package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Generic robustness guard for every column-anchored fixer in
 * {@link CheckstyleFixAction#FIXERS}: an invalid column (negative, or at/past the
 * end of the target line) can never correspond to a real check-reported violation,
 * so no fixer may return a {@link FixResult} or throw when driven there. This is
 * the fixer-agnostic replacement for the per-topic {@code negative_column} /
 * {@code column_beyond_line} fixture fragments: a fixer's positional guard is
 * verified here once rather than by a bespoke fragment in every topic.
 */
public class FixerInputRobustnessTest {
	// Fixers that operate on the whole target line rather than locating a construct
	// at the column, so an out-of-range column is not meaningful to them. FLAG
	// (docs/TODO-fragment-migration.md): confirm these are intentionally
	// column-independent, not silently ignoring a bad column.
	private static final Set<String> COLUMN_INDEPENDENT = Set.of(
			RedundantImportCheck.class.getName()
	);
	private static final String PROBE_LINE = "a benign robustness probe line";

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> columnAnchoredFixers() {
		return CheckstyleFixAction.FIXERS.entrySet().stream()
				.filter(e -> !COLUMN_INDEPENDENT.contains(e.getKey()))
				.map(e -> Arguments.of(e.getKey(), e.getValue()));
	}

	@Nonnull
	private static FixAttempt invoke(@Nonnull CheckstyleFixer fixer, @Nonnull List<String> lines, int lineIndex, int column) throws IOException {
		final var tempFile = File.createTempFile("robustness", ".java");
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

	@Test
	public void fieldConsolidationInvalidColumnProducesNoFix() throws IOException {
		// the shared probe drives lineIndex 0, which this fixer rejects outright (a merge
		// needs a preceding field line), so the parameterized case never reaches its
		// column guards; a two-line buffer is required to exercise them
		final var lines = List.of("\tint alpha;", "\tint beta;");
		for (var column : new int[]{-1, -100, 10, 11, 10_000})
			assertNull(invoke(new FieldConsolidationFixer(), new ArrayList<>(lines), 1, column));
	}

	@Test
	public void fieldSortingNoMemberAtPositionProducesNoFix() throws IOException {
		// a parseable buffer with no VARIABLE_DEF/ENUM_CONSTANT_DEF at the reported
		// position: FieldSortingCheck.objblockAt returns null, so the fixer must
		// not attempt (or misapply) a fix. It returns null (pipeline treats that as
		// not-fixable) rather than a FixResult.
		final var result = invoke(new FieldSortingFixer(), new ArrayList<>(List.of("class Robustness", "{", "}")), 0, 0);
		assertNull(result);
	}

	@Test
	public void fieldSortingSingleConstantEnumProducesNoFix() throws IOException {
		// a stale coordinate landing on the sole constant of a one-constant enum: the
		// check can never report an ordering violation there, so the rebuild's
		// fewer-than-two-constants guard is only reachable this way. It must return
		// null rather than throw on the empty sort.
		final var result = invoke(
				new FieldSortingFixer(), new ArrayList<>(List.of("enum Robustness", "{", "\tONLY", "}")), 2, 1
		);
		assertNull(result);
	}

	@MethodSource("columnAnchoredFixers")
	@ParameterizedTest
	public void invalidColumnNeverProducesFix(@Nonnull String checkName, @Nonnull CheckstyleFixer fixer) throws IOException {
		for (var column : new int[]{-1, -100, PROBE_LINE.length(), PROBE_LINE.length() + 1, 10_000}) {
			final var result = invoke(fixer, new ArrayList<>(List.of(PROBE_LINE)), 0, column);
			assertFalse(
					FixResult.class.isInstance(result),
					checkName + " produced a FixResult at invalid column " + column
			);
		}
	}
}