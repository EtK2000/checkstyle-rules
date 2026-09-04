package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
	// at the column, so an out-of-range column is not meaningful to them. TODO: confirm
	// these are intentionally column-independent, not silently ignoring a bad column.
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
	public void collectionInterfaceNonIdentifierColumnProducesNoFix() throws IOException {
		// the shared probe drives only out-of-range columns, which bail at the bounds guard; the
		// identifier guard is reachable only from a stale column landing inside the line
		final var lines = List.of("\tvoid f(List<String> items) {}");
		for (var column : new int[]{0, 5, 7, 12, 19})
			assertNull(invoke(new PreferCollectionInterfaceFixer(), new ArrayList<>(lines), 0, column));
	}

	@Test
	public void collectionInterfaceOutOfRangeColumnReturnsNull() throws IOException {
		// the shared probe only asserts "not a FixResult"; this fixer's bounds guard returns a bare
		// null, which the pipeline reports differently from a SkipResult
		final var line = "\tvoid f(List<String> items) {}";
		for (var column : new int[]{-1, -100, line.length(), line.length() + 1, 10_000})
			assertNull(invoke(new PreferCollectionInterfaceFixer(), new ArrayList<>(List.of(line)), 0, column));
	}

	@Test
	public void fieldConsolidationColumnAtOrPastLineEndFindsTheLinesDeclarator() throws IOException {
		// the shared probe drives lineIndex 0, which holds no field here, so it never reaches the
		// column handling; a parseable class with a run is needed. A column at or just past the
		// end of the line (FieldConsolidationFixerTest covers the negative and far-out ones) still
		// resolves to the line's only declarator
		final var lines = List.of("class Robustness", "{", "\tint alpha;", "\tint beta;", "}");
		for (var column : new int[]{10, 11})
			assertEquals(new FixResult(2, 3, List.of("\tint alpha, beta;")), invoke(new FieldConsolidationFixer(), new ArrayList<>(lines), 3, column));
	}

	@Test
	public void fieldSortingNoMemberAtPositionProducesNoFix() throws IOException {
		final var result = invoke(new FieldSortingFixer(), new ArrayList<>(List.of("class Robustness", "{", "}")), 0, 0);
		assertNull(result);
	}

	@Test
	public void fieldSortingSingleConstantEnumProducesNoFix() throws IOException {
		// a stale coordinate landing on the sole constant of a one-constant enum: the
		// check can never report an ordering violation there, so the rebuild's
		// fewer-than-two-constants guard is only reachable this way
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