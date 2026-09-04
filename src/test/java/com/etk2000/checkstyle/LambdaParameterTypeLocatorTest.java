package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.LambdaParameterTypeCheck.LambdaParam;
import com.etk2000.checkstyle.LambdaParameterTypeCheck.LambdaParams;
import com.etk2000.checkstyle.gradle.fix.LineLength;
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
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link LambdaParameterTypeCheck#paramsAt} and {@link
 * LambdaParameterTypeCheck#collapsible}, the two seams the fixer dispatches on.
 *
 * <p>It takes its line 0-based, like every sibling locator. It did not always: it was the one that
 * took a 1-based line, and its only caller compensated. A flip that changes one side and not the
 * other is invisible to the topic's slices wherever the off-by-one lands on no lambda at all, so
 * the pair below addresses two lambdas written at the same column on consecutive lines, where a
 * wrong base answers with the other one rather than with nothing.
 */
public class LambdaParameterTypeLocatorTest {
	private static final String FIXTURE = "lambdaparametertype/InputLocatorPositions.java";

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

	/** The column the {@code LAMBDA} token itself carries on the 0-based {@code line}. */
	@CheckReturnValue
	private static int lambdaColumnOn(@Nonnull DetailAST root, int line) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LAMBDA && node.getLineNo() == line + 1)
				return node.getColumnNo();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new IllegalArgumentException("no lambda on line " + line);
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

	/**
	 * A one-parameter list named {@code alpha}, spanning {@code text} within the single-line buffer
	 * {@code line}. {@code annotated} picks the emitted form: {@code alpha} when false,
	 * {@code (var alpha)} when true, so one helper drives both a shortening and a widening fold.
	 */
	@CheckReturnValue
	@Nonnull
	private static LambdaParams listOn(@Nonnull String line, @Nonnull String text, boolean annotated) {
		final var start = line.indexOf(text);
		return new LambdaParams(
				new TextSpan(new TextPos(0, start), new TextPos(0, start + text.length())),
				List.of(new LambdaParam("alpha", null, false)),
				annotated
		);
	}

	/** The parameter list reported where {@code needle} begins, on the line {@code onLine} holds. */
	@CheckReturnValue
	@Nullable
	private static LambdaParams locate(@Nonnull String onLine, @Nonnull String needle) throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, onLine);
		return LambdaParameterTypeCheck.paramsAt(
				parseFixture(), lines, line, columnOf(lines.get(line), needle)
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
				LambdaParameterTypeLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	/**
	 * A two-parameter list spanning {@code (A alpha, B beta)} on the single-line {@code line}, built
	 * annotated so the rewrite emits {@code (var alpha, var beta)}: four characters wider than the
	 * text it replaces, which is what makes a fold able to cross the budget.
	 */
	@CheckReturnValue
	@Nonnull
	private static LambdaParams varListOn(@Nonnull String line) {
		final var start = line.indexOf("(A alpha");
		return new LambdaParams(
				new TextSpan(new TextPos(0, start), new TextPos(0, line.indexOf(" ->"))),
				List.of(new LambdaParam("alpha", null, false), new LambdaParam("beta", null, false)),
				true
		);
	}

	/**
	 * Dropping a type or a pair of parens only ever shortens a line, so a list on a line already
	 * over the budget is still worth folding: the result is narrower than what it replaces.
	 * Measuring the budget alone suppressed every such fix.
	 *
	 * <p>The parameters are built by hand rather than parsed, because {@code rewrittenList} reads
	 * {@code lines} only to slice an annotation span and these cases carry none. That keeps the
	 * buffer plain text, which a Java fixture of this width could not be: the test resources are
	 * themselves held to the line length. The padding alone is {@code MAX_LINE_LENGTH} wide, so
	 * both buffers below start over budget by construction.
	 */
	@Test
	public void collapsibleAcceptsAFoldThatShortensAnAlreadyLongLine() {
		final var line = "x".repeat(LineLength.MAX_LINE_LENGTH) + "(String alpha);";
		final var params = listOn(line, "(String alpha)", false);
		assertTrue(LambdaParameterTypeCheck.collapsible(List.of(line), List.of(line), params));
	}

	/** The boundary partner of the tab-expansion refusal: one tab less is 114 columns folding to 118. */
	@Test
	public void collapsibleAcceptsATabIndentedFoldWithinTheExpandedLimit() {
		final var line = "\t".repeat(21) + "f((A alpha, B beta) -> alpha);";
		assertTrue(LambdaParameterTypeCheck.collapsible(List.of(line), List.of(line), varListOn(line)));
	}

	/** The boundary partner of the two mask refusals below: the real mask is accepted. */
	@Test
	public void collapsibleAcceptsTheRealMask() throws Exception {
		final var lines = fixtureLines();
		final var params = locate("(String alpha,", "String alpha");
		assertNotNull(params);
		assertTrue(LambdaParameterTypeCheck.collapsible(lines, JavaLineScanner.maskAll(lines), params));
	}

	/** The boundary partner: on an equally over-budget line, a fold that widens it is still refused. */
	@Test
	public void collapsibleRefusesAFoldThatWidensAnAlreadyLongLine() {
		final var line = "x".repeat(LineLength.MAX_LINE_LENGTH) + "(alpha);";
		final var params = listOn(line, "(alpha)", true);
		assertFalse(LambdaParameterTypeCheck.collapsible(List.of(line), List.of(line), params));
	}

	/**
	 * {@code collapsible} indexes {@code masked} at positions derived from {@code lines}, so a mask
	 * whose lines are shorter must be refused rather than allowed to throw out of {@code visitToken}
	 * and fail the whole file. {@code JavaLineScanner.maskAll} is length-preserving and both real
	 * callers pass its output, so only a direct call reaches this.
	 */
	@Test
	public void collapsibleRefusesAShortLinedMask() throws Exception {
		final var lines = fixtureLines();
		final var params = locate("(String alpha,", "String alpha");
		assertNotNull(params);
		assertFalse(LambdaParameterTypeCheck.collapsible(lines, Collections.nCopies(lines.size(), ""), params));
	}

	/**
	 * A mask list shorter than {@code lines} is a second buffer the span's own bounds check knows
	 * nothing about, which is why {@code collapsible} tests its size separately.
	 */
	@Test
	public void collapsibleRefusesAShortMaskList() throws Exception {
		final var lines = fixtureLines();
		final var params = locate("(String alpha,", "String alpha");
		assertNotNull(params);
		assertFalse(LambdaParameterTypeCheck.collapsible(lines, List.of(), params));
	}

	/**
	 * The fold is measured in tab-expanded columns, not characters. Twenty-two leading tabs are 22
	 * characters but 88 columns, so this line is a legal 118 columns and folds to 122. A gate
	 * counting characters instead sees 52 folding to 56 and accepts. Every other direct case here
	 * is space-padded, where the two measurements agree, while real source in this project is
	 * tab-indented, so this is the only case that tells the two apart.
	 */
	@Test
	public void collapsibleRefusesATabIndentedFoldPastTheExpandedLimit() {
		final var line = "\t".repeat(22) + "f((A alpha, B beta) -> alpha);";
		assertFalse(LambdaParameterTypeCheck.collapsible(List.of(line), List.of(line), varListOn(line)));
	}

	/** The half of the pair a base off by one would hand back for the other line's position. */
	@Test
	public void paramsAtAtTheNextLineFindsTheNextLambda() throws Exception {
		final var params = locate("(int beta)", "int beta");
		assertNotNull(params);
		assertEquals("beta", params.params().getFirst().name());
	}

	@Test
	public void paramsAtBelowTheFirstLambdaFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var alpha = lineWith(lines, "(int alpha)");
		assertNull(LambdaParameterTypeCheck.paramsAt(
				parseFixture(), lines, alpha - 1, columnOf(lines.get(alpha), "int alpha")
		));
	}

	/** The span still opens on the line the list opens on, not on the line reported. */
	@Test
	public void paramsAtOnAContinuationLine() throws Exception {
		final var lines = fixtureLines();
		final var opening = lineWith(lines, "(String alpha,");
		final var params = locate("String beta)", "String beta");
		assertNotNull(params);
		assertEquals(opening, params.listSpan().start().line());
	}

	@Test
	public void paramsAtOnANestedLambdaUsesTheColumn() throws Exception {
		assertEquals("outer", requireNonNull(locate("(int outer)", "int outer")).params().getFirst().name());
		assertEquals("inner", requireNonNull(locate("(int inner)", "int inner")).params().getFirst().name());
	}

	/** The arm matching the {@code LAMBDA} node itself, which the unnecessary-parens rule reports on. */
	@Test
	public void paramsAtOnTheLambdaTokenItself() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(lines, "return (x) -> x;");
		final var params = LambdaParameterTypeCheck.paramsAt(root, lines, line, lambdaColumnOn(root, line));
		assertNotNull(params);
		assertEquals("x", params.params().getFirst().name());
	}

	@Test
	public void paramsAtPastTheLastLambdaFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var beta = lineWith(lines, "(int beta)");
		assertNull(LambdaParameterTypeCheck.paramsAt(
				parseFixture(), lines, beta + 1, columnOf(lines.get(beta), "int beta")
		));
	}

	/** The half a base off by one would miss, reported on a {@code PARAMETER_DEF}. */
	@Test
	public void paramsAtUsesZeroBasedLines() throws Exception {
		final var params = locate("(int alpha)", "int alpha");
		assertNotNull(params);
		assertEquals("alpha", params.params().getFirst().name());
	}
}