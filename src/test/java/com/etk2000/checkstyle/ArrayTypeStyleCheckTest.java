package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.ArrayTypeStyleCheck.CStyleDeclarator;
import com.etk2000.checkstyle.ArrayTypeStyleCheck.ReportedDeclarator;
import com.puppycrawl.tools.checkstyle.JavaParser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link ArrayTypeStyleCheck#inMultiVariableDeclarationAt}, since the slices cannot
 * reach the previous-declarator side of the answer.
 *
 * <p>Also covers {@link ArrayTypeStyleCheck#uniformCStyleGroupAt} on a base type ending the line
 * ahead of its declarators, in {@code InputLocatorPositions.java} rather than a slice since the
 * brackets on the type line make no slice of their own.
 */
public class ArrayTypeStyleCheckTest {
	private static final String BRACKETED_TYPE_LINE = "alpha[], beta[];";
	private static final String FIXTURE = "arraytypestyle/cases.in.java";
	private static final String MIXED_DEPTH = "int alpha[], beta[][];";
	private static final String PLAIN_TYPE_LINE = "gamma[], delta[];";
	private static final String POSITIONS = "arraytypestyle/InputLocatorPositions.java";

	@CheckReturnValue
	@Nonnull
	private static File fixture(@Nonnull String path) throws Exception {
		return new File(requireNonNull(
				ArrayTypeStyleCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path),
				"Test input file not found: " + path
		).toURI());
	}

	@CheckReturnValue
	private static int lineStartingWith(@Nonnull List<String> lines, @Nonnull String prefix) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).strip().startsWith(prefix))
				return i;
		}
		throw new AssertionError("no fixture line starts with " + prefix);
	}

	@CheckReturnValue
	private static boolean multiVariableAt(@Nonnull String linePrefix, @Nonnull String needle) throws Exception {
		final var file = fixture(FIXTURE);
		final var lines = Files.readAllLines(file.toPath());
		final var line = lineStartingWith(lines, linePrefix);
		final var root = JavaParser.parseFile(file, JavaParser.Options.WITHOUT_COMMENTS);
		return ArrayTypeStyleCheck.inMultiVariableDeclarationAt(root, line, lines.get(line).indexOf(needle) + needle.length() - 1);
	}

	@CheckReturnValue
	@Nullable
	private static ReportedDeclarator reportedAt(@Nonnull String path, @Nonnull String linePrefix, @Nonnull String needle) throws Exception {
		final var file = fixture(path);
		final var lines = Files.readAllLines(file.toPath());
		final var line = lineStartingWith(lines, linePrefix);
		final var root = JavaParser.parseFile(file, JavaParser.Options.WITHOUT_COMMENTS);
		return ArrayTypeStyleCheck.reportedDeclaratorAt(root, line, lines.get(line).indexOf(needle) + needle.length() - 1);
	}

	@CheckReturnValue
	@Nullable
	private static List<CStyleDeclarator> uniformGroupAt(@Nonnull String linePrefix, @Nonnull String needle) throws Exception {
		final var file = fixture(POSITIONS);
		final var lines = Files.readAllLines(file.toPath());
		final var line = lineStartingWith(lines, linePrefix);
		final var root = JavaParser.parseFile(file, JavaParser.Options.WITHOUT_COMMENTS);
		return ArrayTypeStyleCheck.uniformCStyleGroupAt(root, line, lines.get(line).indexOf(needle) + needle.length() - 1);
	}

	@Test
	public void testAdjacentSingleDeclaratorIsNotInAMultiVariableDeclaration() throws Exception {
		assertFalse(multiVariableAt("long delta[];", "delta["));
	}

	@Test
	public void testAnnotatedBaseDimensionIsReported() throws Exception {
		assertTrue(requireNonNull(reportedAt(FIXTURE, "int @A [] x[];", "x[")).annotatedBaseDimension());
	}

	@Test
	public void testAnnotationOnTheNamesOwnBracketsIsNoBaseDimension() throws Exception {
		assertFalse(requireNonNull(reportedAt(FIXTURE, "int x @Anno [];", "@Anno [")).annotatedBaseDimension());
	}

	@Test
	public void testBaseTypeBracketIsNotReported() throws Exception {
		assertNull(reportedAt(FIXTURE, "int[] x[];", "int["));
	}

	@Test
	public void testBaseTypeBracketsOnAnEarlierLineAreNoGroupAtTheFirstDeclarator() throws Exception {
		assertNull(uniformGroupAt(BRACKETED_TYPE_LINE, "alpha["));
	}

	@Test
	public void testBaseTypeBracketsOnAnEarlierLineAreNoGroupAtTheLaterDeclarator() throws Exception {
		assertNull(uniformGroupAt(BRACKETED_TYPE_LINE, "beta["));
	}

	@Test
	public void testCastBracketIsNotReported() throws Exception {
		assertNull(reportedAt(POSITIONS, "final Byte bytes[]", "(Byte["));
	}

	@Test
	public void testFirstDeclaratorIsInAMultiVariableDeclaration() throws Exception {
		assertTrue(multiVariableAt(MIXED_DEPTH, "alpha["));
	}

	@Test
	public void testLaterDeclaratorIsInAMultiVariableDeclaration() throws Exception {
		assertTrue(multiVariableAt(MIXED_DEPTH, "beta["));
	}

	@Test
	public void testParameterIsNotInAMultiVariableDeclaration() throws Exception {
		assertFalse(multiVariableAt("void m(int x[])", "x["));
	}

	@Test
	public void testSecondDimensionIsNotReported() throws Exception {
		assertNull(reportedAt(FIXTURE, "int x[][];", "x[]["));
	}

	@Test
	public void testSingleDeclaratorIsNotInAMultiVariableDeclaration() throws Exception {
		assertFalse(multiVariableAt("int field[];", "field["));
	}

	@Test
	public void testTypeOnAnEarlierLineIsAUniformGroup() throws Exception {
		assertEquals(2, requireNonNull(uniformGroupAt(PLAIN_TYPE_LINE, "gamma[")).size());
	}

	@Test
	public void testUnannotatedDeclaratorIsReportedByItsName() throws Exception {
		final var reported = requireNonNull(reportedAt(FIXTURE, "int field[];", "field["));
		assertEquals(List.of("field", false), List.of(reported.name().getText(), reported.annotatedBaseDimension()));
	}
}