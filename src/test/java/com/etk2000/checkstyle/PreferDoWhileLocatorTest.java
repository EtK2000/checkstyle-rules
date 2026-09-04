package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferDoWhileCheck.DoWhileTarget;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link PreferDoWhileCheck#targetAt}, the seam {@code PreferDoWhileFixer}
 * dispatches on. The loop the fixer rewrites absorbs and deletes the statement above it, so the
 * rows that matter most are the ones asserting a position does <em>not</em> classify: each is a
 * shape the fixer would otherwise delete a statement from.
 */
public class PreferDoWhileLocatorTest {
	private static final String FIXTURE = "preferdowhile/InputLocatorPositions.java";

	/** An AST column counts code points, so a char index into the line is converted. */
	@CheckReturnValue
	private static int columnOf(@Nonnull String line, @Nonnull String needle) {
		final var index = line.indexOf(needle);
		if (index < 0)
			throw new IllegalArgumentException("no [" + needle + "] in [" + line + "]");
		return line.codePointCount(0, index);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return Files.readAllLines(Path.of(resource().toURI()), StandardCharsets.UTF_8);
	}

	/**
	 * The first 0-based line below {@code method}'s declaration carrying {@code needle}. Anchoring
	 * inside one method keeps a needle such as {@code while (i < 10)}, which several of them share,
	 * pointing at the one the test is about.
	 */
	@CheckReturnValue
	private static int lineIn(@Nonnull List<String> lines, @Nonnull String method, @Nonnull String needle) {
		for (var i = lineWith(lines, ' ' + method + "(") + 1; i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] under [" + method + "] in " + FIXTURE);
	}

	/** The first 0-based line of the fixture carrying {@code needle}. */
	@CheckReturnValue
	private static int lineWith(@Nonnull List<String> lines, @Nonnull String needle) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] in " + FIXTURE);
	}

	/** What the check classifies where {@code needle} begins, inside {@code method}. */
	@CheckReturnValue
	@Nullable
	private static DoWhileTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static DoWhileTarget locateAt(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return PreferDoWhileCheck.targetAt(parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static DoWhileTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return PreferDoWhileCheck.targetAt(
				parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var file = new File(resource().toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource() {
		return requireNonNull(
				PreferDoWhileLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void doWhileTerminatorFindsNothing() throws Exception {
		assertNull(locate("doWhileTerminator", "while (i < 10);"));
	}

	@Test
	public void labelledWhileFindsNothing() throws Exception {
		assertNull(locate("labelledWhile", "while (i < 10)"));
	}

	@Test
	public void nestedInnerWhileClassifies() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("nestedWhilePair", "while (i < 10)");
		assertNotNull(target);
		assertEquals(lineIn(lines, "nestedWhilePair", "while (i < 10)") + 1, target.whileAst().getLineNo());
	}

	@Test
	public void nestedOuterWhileFindsNothing() throws Exception {
		assertNull(locate("nestedWhilePair", "while (cond)"));
	}

	@Test
	public void sameColumnWhilePairFirstClassifies() throws Exception {
		final var lines = fixtureLines();
		final var first = locate("sameColumnWhilePair", "while (i < 10)");
		final var second = locate("sameColumnWhilePair", "while (j > 0)");
		assertNotNull(first);
		assertNotNull(second);
		assertEquals(lineIn(lines, "sameColumnWhilePair", "while (i < 10)") + 1, first.whileAst().getLineNo());
		assertEquals(lineIn(lines, "sameColumnWhilePair", "while (j > 0)") + 1, second.whileAst().getLineNo());
		assertEquals(first.whileAst().getColumnNo(), second.whileAst().getColumnNo());
	}

	@Test
	public void sameColumnWhilePairSecondClassifies() throws Exception {
		final var target = locate("sameColumnWhilePair", "while (j > 0)");
		assertNotNull(target);
		assertEquals("--", target.bodyStatement().getFirstChild().getText());
	}

	/**
	 * A {@code targetAt} column counts code points, so a supplementary character ahead of the
	 * keyword puts the char index and the column at different numbers. Only the column may classify;
	 * the char index addresses a position inside the preceding text.
	 */
	@Test
	public void theCodePointColumnLocatesWhereTheCharIndexMisses() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryBeforeWhile", "while (i < 10)");
		final var text = lines.get(line);
		final var charIndex = text.indexOf("while (i < 10)");
		final var codePointColumn = text.codePointCount(0, charIndex);

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		final var target = PreferDoWhileCheck.targetAt(parseFixture(), line, codePointColumn);
		assertNotNull(target);
		assertEquals(line + 1, target.whileAst().getLineNo());
		assertNull(PreferDoWhileCheck.targetAt(parseFixture(), line, charIndex));
	}

	@Test
	public void whileAtColumnZeroClassifies() throws Exception {
		final var target = locate("whileAtColumnZero", "while (i < 10)");
		assertNotNull(target);
		assertEquals(0, target.whileAst().getColumnNo());
	}

	@Test
	public void whileBesideACommentedWhileClassifies() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("whileInsideACommentAndAString", "while (i < 10)");
		assertNotNull(target);
		assertEquals(
				lineIn(lines, "whileInsideACommentAndAString", "while (i < 10)") + 1,
				target.whileAst().getLineNo()
		);
	}

	@Test
	public void whileColumnZeroDoesNotFallBackToTheFirstOnTheLine() throws Exception {
		assertNull(locateAt("whileWithDuplicate", "while (i < 10)", 0));
	}

	@Test
	public void whileInsideACommentOnTheSameLineFindsNothing() throws Exception {
		assertNull(locateColumn("whileInsideACommentAndAString", "while (i < 10)", "while (j < 10)", 0));
	}

	@Test
	public void whileInsideAStringOnTheSameLineFindsNothing() throws Exception {
		assertNull(locate("whileInsideACommentAndAString", "while (k < 10)"));
	}

	@Test
	public void whileOnePastTheKeywordFindsNothing() throws Exception {
		assertNull(locateColumn("whileWithDuplicate", "while (i < 10)", "while (i < 10)", 1));
	}

	@Test
	public void whileOnTheLineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "sameColumnWhilePair", "while (i < 10)");
		assertNull(PreferDoWhileCheck.targetAt(
				parseFixture(), line - 1, columnOf(lines.get(line), "while (i < 10)")
		));
	}

	@Test
	public void whileWithDuplicatedPreStatementClassifies() throws Exception {
		final var target = locate("whileWithDuplicate", "while (i < 10)");
		assertNotNull(target);
		assertEquals(TokenTypes.LITERAL_WHILE, target.whileAst().getType());
		assertEquals(TokenTypes.EXPR, target.bodyStatement().getType());
		assertEquals(TokenTypes.EXPR, target.precedingStatement().getType());
		assertEquals(TokenTypes.EXPR, target.condition().getType());
	}

	@Test
	public void whileWithoutADuplicatedPreStatementFindsNothing() throws Exception {
		assertNull(locate("whileNoDuplicate", "while (i < 10)"));
	}
}