package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferMathMethodCheck.MathTarget;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;

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
 * Direct tests for {@link PreferMathMethodCheck#mathTargetAt}, the seam
 * {@code PreferMathMethodFixer} dispatches its two expression shapes on.
 *
 * <p>The classifier matches a position exactly, so every row here is a position assertion. It takes
 * its line 0-based, like every sibling locator; {@link #sameColumnPairAboveTheFirstFindsNothing}
 * and its partners address two ternaries written at the same column on consecutive lines, where a
 * wrong base answers with the other one rather than with nothing.
 */
public class PreferMathMethodLocatorTest {
	/**
	 * Offset from the start of {@code Math.max}/{@code Math.min} to the call's {@code (}, which is
	 * the position a {@code METHOD_CALL} carries and so the only one that classifies a clamp.
	 */
	private static final int CALL_PAREN = "Math.max".length();
	private static final String FIXTURE = "prefermathmethod/InputLocatorPositions.java";

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

	/** The first 0-based line of the fixture carrying {@code needle}. */
	@CheckReturnValue
	private static int lineWith(@Nonnull List<String> lines, @Nonnull String needle) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] in " + FIXTURE);
	}

	/** What the check classifies where {@code needle} begins, on the line {@code onLine} holds. */
	@CheckReturnValue
	@Nullable
	private static MathTarget locate(@Nonnull String onLine, @Nonnull String needle) throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, onLine);
		return PreferMathMethodCheck.mathTargetAt(parseFixture(), line, columnOf(lines.get(line), needle));
	}

	/** What the check classifies {@code offset} code points past where {@code needle} begins. */
	@CheckReturnValue
	@Nullable
	private static MathTarget locateOffset(@Nonnull String onLine, @Nonnull String needle, int offset)
			throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, onLine);
		return PreferMathMethodCheck.mathTargetAt(
				parseFixture(),
				line,
				columnOf(lines.get(line), needle) + offset
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
				PreferMathMethodLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void chainedInnerQuestionClassifies() throws Exception {
		final var target = locate("return a > b ? a : b > c ? b : c;", "? b : c");
		assertNotNull(target);
		assertEquals("max", target.method());
		assertEquals(2, target.arguments().size());
	}

	/** The false branch is itself a ternary, which {@code isPureExpression} refuses. */
	@Test
	public void chainedOuterQuestionFindsNothing() throws Exception {
		assertNull(locate("return a > b ? a : b > c ? b : c;", "? a : b"));
	}

	@Test
	public void clampOnALineAlsoHoldingATernary() throws Exception {
		final var target = locateOffset("return (a > b ? a : b) + Math.max(lo", "Math.max(lo", CALL_PAREN);
		assertNotNull(target);
		assertEquals("clamp", target.method());
		assertEquals(3, target.arguments().size());
	}

	/** Only the outer call of a clamp pair classifies; the inner one is part of it. */
	@Test
	public void clampOnTheInnerCallFindsNothing() throws Exception {
		assertNull(locateOffset("return (a > b ? a : b) + Math.max(lo", "Math.min(hi", CALL_PAREN));
	}

	@Test
	public void foreignReceiverFindsNothing() throws Exception {
		assertNull(locateOffset("return MyMath.max(lo", "MyMath.max(", "MyMath.max".length()));
	}

	@Test
	public void positionHoldingNeitherFindsNothing() throws Exception {
		assertNull(locate("return MyMath.max(lo", "return"));
	}

	@Test
	public void sameColumnPairAboveTheFirstFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, "a > b ? a : b,");
		assertNull(
				PreferMathMethodCheck.mathTargetAt(
						parseFixture(), line - 1, columnOf(lines.get(line), "? a : b")
				)
		);
	}

	@Test
	public void sameColumnPairFirstClassifiesAsMax() throws Exception {
		final var target = locate("a > b ? a : b,", "? a : b");
		assertNotNull(target);
		assertEquals("max", target.method());
	}

	@Test
	public void sameColumnPairSecondClassifiesAsMin() throws Exception {
		final var target = locate("c < d ? c : d", "? c : d");
		assertNotNull(target);
		assertEquals("min", target.method());
	}

	@Test
	public void ternaryAtColumnZeroClassifies() throws Exception {
		final var target = locate("? a : b;", "?");
		assertNotNull(target);
		assertEquals("max", target.method());
	}

	/** The partner forbidding a fallback to the line's first construct when the column is 0. */
	@Test
	public void ternaryColumnZeroDoesNotFallBackToTheFirstOnTheLine() throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, "return a > b ? a : b > c ? b : c;");
		assertNull(PreferMathMethodCheck.mathTargetAt(parseFixture(), line, 0));
	}

	@Test
	public void ternaryOnALineAlsoHoldingAClamp() throws Exception {
		final var target = locate("return (a > b ? a : b) + Math.max(lo", "? a : b");
		assertNotNull(target);
		assertEquals("max", target.method());
		assertEquals(2, target.arguments().size());
	}

	/** One past the {@code ?} is not the {@code ?}, and the match is exact. */
	@Test
	public void ternaryOnePastTheQuestionFindsNothing() throws Exception {
		assertNull(locateOffset("a > b ? a : b,", "? a : b", 1));
	}

	@Test
	public void twoClampsOnOneLineEachClassify() throws Exception {
		final var onLine = "Math.max(lo, Math.min(hi, v)) + Math.max(lo2";
		final var first = locateOffset(onLine, "Math.max(lo,", CALL_PAREN);
		final var second = locateOffset(onLine, "Math.max(lo2", CALL_PAREN);
		assertNotNull(first);
		assertNotNull(second);
		assertEquals("clamp", first.method());
		assertEquals("clamp", second.method());
	}
}