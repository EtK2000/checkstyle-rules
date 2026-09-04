package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferMathMethodCheck.IfTarget;
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
 * Direct tests for {@link PreferMathMethodCheck#ifTargetAt}, the seam
 * {@code PreferMathMethodFixer} dispatches its statement shape on, and sibling of
 * {@code PreferMathMethodLocatorTest}, which covers the expression shapes. The two classifiers
 * partition what the check reports, so two rows here assert that neither answers for the other's
 * position.
 */
public class PreferMathMethodIfLocatorTest {
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

	/**
	 * The first 0-based line below {@code method}'s declaration carrying {@code needle}. Anchoring
	 * inside one method keeps a needle such as {@code if (a > b)}, which several of them share,
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
	private static IfTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static IfTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return PreferMathMethodCheck.ifTargetAt(
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
				PreferMathMethodIfLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void absIfClassifiesWithIntegralZeroLiteral() throws Exception {
		final var target = locate("absWithElse", "if (a < 0)");
		assertNotNull(target);
		assertEquals("abs", target.math().method());
		assertEquals(1, target.math().arguments().size());
		assertNotNull(target.math().zeroLiteral());
		assertEquals(TokenTypes.NUM_INT, target.math().zeroLiteral().getType());
	}

	@Test
	public void assignIsNullForTheReturnShape() throws Exception {
		final var target = locate("returnWithElse", "if (a > b)");
		assertNotNull(target);
		assertNull(target.assign());
	}

	@Test
	public void assignIsSetForTheAssignShape() throws Exception {
		final var target = locate("assignWithElse", "if (a > b)");
		assertNotNull(target);
		assertNotNull(target.assign());
		assertEquals("=", target.assign().getText());
		assertNull(target.declaration());
		assertNull(target.trailingReturn());
	}

	@Test
	public void declarationIsNullWhenAnElseSuppliesTheValue() throws Exception {
		final var target = locate("declWithInitializer", "if (a > b)");
		assertNotNull(target);
		assertNull(target.declaration());
	}

	@Test
	public void declarationIsSetForInitOverwrite() throws Exception {
		final var target = locate("initOverwrite", "if (a > b)");
		assertNotNull(target);
		assertNotNull(target.declaration());
		assertNull(target.trailingReturn());
	}

	@Test
	public void elseIfClassifiesAtTheInnerKeyword() throws Exception {
		final var target = locateColumn("elseIfChain", "else if (a > b)", "if (a > b)", 0);
		assertNotNull(target);
		assertEquals("max", target.math().method());
	}

	@Test
	public void ifAtColumnZeroClassifies() throws Exception {
		final var target = locate("ifAtColumnZero", "if (a > b)");
		assertNotNull(target);
		assertEquals("max", target.math().method());
	}

	@Test
	public void ifBesideACommentedIfClassifies() throws Exception {
		final var target = locate("ifInsideACommentAndAString", "if (a > b)");
		assertNotNull(target);
		assertEquals("max", target.math().method());
	}

	@Test
	public void ifColumnZeroDoesNotFallBackToTheFirstOnTheLine() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "sameColumnIfPair", "if (a > b)");
		assertNull(PreferMathMethodCheck.ifTargetAt(parseFixture(), line, 0));
	}

	@Test
	public void ifInsideACommentOnTheSameLineFindsNothing() throws Exception {
		assertNull(locateColumn("ifInsideACommentAndAString", "if (a > b)", "if (a <= b)", 0));
	}

	@Test
	public void ifInsideAStringOnTheSameLineFindsNothing() throws Exception {
		assertNull(locateColumn("ifInsideACommentAndAString", "var text =", "if (a < b)", 0));
	}

	@Test
	public void ifOnePastTheKeywordFindsNothing() throws Exception {
		assertNull(locateColumn("sameColumnIfPair", "if (a > b)", "if (a > b)", 1));
	}

	@Test
	public void ifOnTheLineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "sameColumnIfPair", "if (a > b)");
		assertNull(
				PreferMathMethodCheck.ifTargetAt(
						parseFixture(), line - 1, columnOf(lines.get(line), "if (a > b)")
				)
		);
	}

	@Test
	public void ifTargetAtFindsNothingAtATernaryQuestion() throws Exception {
		assertNull(locate("chainedOnOneLine", "? b : c"));
	}

	@Test
	public void mathTargetAtFindsNothingAtAnIfKeyword() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "assignWithElse", "if (a > b)");
		assertNull(
				PreferMathMethodCheck.mathTargetAt(
						parseFixture(), line, columnOf(lines.get(line), "if (a > b)")
				)
		);
	}

	@Test
	public void nestedInnerIfClassifies() throws Exception {
		final var target = locate("nestedIfPair", "if (a > b)");
		assertNotNull(target);
		assertEquals("max", target.math().method());
	}

	@Test
	public void nestedOuterIfFindsNothing() throws Exception {
		assertNull(locate("nestedIfPair", "if (cond)"));
	}

	@Test
	public void sameColumnPairFirstClassifiesAsMax() throws Exception {
		final var target = locate("sameColumnIfPair", "if (a > b)");
		assertNotNull(target);
		assertEquals("max", target.math().method());
	}

	@Test
	public void sameColumnPairSecondClassifiesAsMin() throws Exception {
		final var target = locate("sameColumnIfPair", "if (c < d)");
		assertNotNull(target);
		assertEquals("min", target.math().method());
	}

	@Test
	public void trailingReturnIsNullForTheIfElseReturnShape() throws Exception {
		final var target = locate("returnWithElse", "if (a > b)");
		assertNotNull(target);
		assertNull(target.trailingReturn());
	}

	@Test
	public void trailingReturnIsSetForTheNoElseReturnShape() throws Exception {
		final var target = locate("trailingReturn", "if (a > b)");
		assertNotNull(target);
		assertNotNull(target.trailingReturn());
		assertNull(target.assign());
	}
}