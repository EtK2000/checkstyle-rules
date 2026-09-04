package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferDirectBooleanReturnCheck.DirectBooleanRule;
import com.etk2000.checkstyle.PreferDirectBooleanReturnCheck.DirectBooleanTarget;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link PreferDirectBooleanReturnCheck#locateAt}, the seam the fixer dispatches on.
 *
 * <p>The fixer no longer finds the {@code if}, its body or its paired return in the line text, so a
 * reported position the locator cannot resolve is a fix that silently stops happening, and a position
 * it resolves to the wrong {@code if} is a collapse applied to someone else's violation. Both are
 * invisible to a fixture-driven test, which only sees the output.
 */
public class PreferDirectBooleanReturnLocatorTest {
	private static final String CORPUS = "preferdirectbooleanreturn/cases.in.java";
	private static final String FIXTURE = "preferdirectbooleanreturn/InputLocatorPositions.java";

	/** An AST column counts code points, so a char index into a line carrying one must be converted. */
	@CheckReturnValue
	private static int codePointColumnOf(@Nonnull String line, int charIndex) {
		return line.codePointCount(0, charIndex);
	}

	@CheckReturnValue
	private static int columnOf(@Nonnull String line, @Nonnull String needle) {
		final var index = line.indexOf(needle);
		if (index < 0)
			throw new IllegalArgumentException("no [" + needle + "] in [" + line + "]");
		return index;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return linesOf(FIXTURE);
	}

	/** The 0-based lines {@code method}'s body spans, so a needle is looked for inside one method. */
	@CheckReturnValue
	private static int[] lineRangeOf(@Nonnull DetailAST root, @Nonnull String method) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				final var body = node.findFirstToken(TokenTypes.SLIST);
				if (ident != null && body != null && method.equals(ident.getText()))
					return new int[]{body.getLineNo() - 1, body.getLastChild().getLineNo() - 1};
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new IllegalArgumentException("no method named " + method + " in " + FIXTURE);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> linesOf(@Nonnull String path) throws Exception {
		return Files.readAllLines(Path.of(resource(path).toURI()), StandardCharsets.UTF_8);
	}

	@CheckReturnValue
	private static int lineWith(
			@Nonnull DetailAST root, @Nonnull List<String> lines, @Nonnull String method, @Nonnull String needle
	) {
		final var range = lineRangeOf(root, method);
		for (var i = range[0]; i <= range[1]; ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] inside " + method);
	}

	/** The target at the start of {@code needle} on the first line of {@code method} that carries it. */
	@CheckReturnValue
	@Nullable
	private static DirectBooleanTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, method, needle);
		final var text = lines.get(line);
		return PreferDirectBooleanReturnCheck.locateAt(
				root, lines, line, codePointColumnOf(text, columnOf(text, needle))
		);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parse(@Nonnull String path) throws Exception {
		final var file = new File(resource(path).toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		return parse(FIXTURE);
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource(@Nonnull String path) {
		return requireNonNull(
				PreferDirectBooleanReturnLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path),
				"Test input file not found: " + path
		);
	}

	/**
	 * Mirrors {@code CheckstyleFixAction.tabColumnToCodePointColumn}, which
	 * {@code CheckstyleFixAction.eventCharIndex} applies to a reported column before turning it into
	 * the char index a fixer receives.
	 */
	@CheckReturnValue
	private static int tabColumnToCodePointColumn(@Nonnull String line, int tabExpandedColumn) {
		var visualColumn = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (visualColumn >= tabExpandedColumn)
				return i;
			if (line.charAt(i) == '\t')
				visualColumn += LineLength.TAB_WIDTH - (visualColumn % LineLength.TAB_WIDTH);
			else
				++visualColumn;
		}
		return line.length();
	}

	/** Every target the check's own output over {@link #CORPUS} drives. */
	@CheckReturnValue
	@Nonnull
	private static List<DirectBooleanTarget> targetsOverTheCorpus(@Nonnull List<String> misses) throws Exception {
		final var lines = linesOf(CORPUS);
		final var root = parse(CORPUS);
		final var targets = new ArrayList<DirectBooleanTarget>();
		for (var event : BaseCheckTest.runCheck(PreferDirectBooleanReturnCheck.class, CORPUS)) {
			final var text = lines.get(event.getLine() - 1);
			final var target = PreferDirectBooleanReturnCheck.locateAt(
					root, lines, event.getLine() - 1, tabColumnToCodePointColumn(text, event.getColumn() - 1)
			);
			if (target == null)
				misses.add("line " + event.getLine() + ": " + text.strip());
			else
				targets.add(target);
		}
		return targets;
	}

	@Test
	public void anElseIfResolvesToTheInnerIfNotTheElse() throws Exception {
		final var target = locate("elseIfInner", "if (b)");
		assertNotNull(target);
		assertEquals(DirectBooleanRule.OPPOSITE, target.rule());
		assertEquals(TokenTypes.LITERAL_IF, target.ifAst().getType());
		assertEquals("b", target.conditionExpr().getFirstChild().getText(), "the inner if's own condition");
	}

	@Test
	public void aPositionNoNodeStartsAtResolvesToNothing() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, "oppositeForward", "if (");
		final var text = lines.get(line);
		// the `f` of the condition: inside the parentheses, where no `if` begins
		assertNull(PreferDirectBooleanReturnCheck.locateAt(
				root, lines, line, codePointColumnOf(text, columnOf(text, "flag"))
		));
	}

	/** The position predates any sibling fix applied to the same line earlier in the pass. */
	@Test
	public void aStaleIfPositionResolvesToNothing() throws Exception {
		final var lines = new ArrayList<>(fixtureLines());
		final var root = parseFixture();
		final var line = lineWith(root, lines, "oppositeForward", "if (");
		final var column = codePointColumnOf(lines.get(line), columnOf(lines.get(line), "if ("));

		// what a collapse leaves behind: those statement lines are gone and everything below moved up
		lines.subList(line, line + 3).clear();
		final var rewritten = JavaParser.parseFileText(
				new FileText(new File(resource(FIXTURE).toURI()), lines), JavaParser.Options.WITHOUT_COMMENTS
		);
		assertNull(PreferDirectBooleanReturnCheck.locateAt(rewritten, lines, line, column));
	}

	@Test
	public void aTabIndentedPositionResolvesAfterConversion() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, "tabIndentedPair", "if (flag)");
		final var text = lines.get(line);
		final var charIndex = columnOf(text, "if (flag)");
		assertEquals(3, charIndex, "the inner if must sit at three tabs");

		final var target = PreferDirectBooleanReturnCheck.locateAt(root, lines, line, charIndex);
		assertNotNull(target, "the code-point column is the char index here, tabs being one code point each");
		assertEquals(DirectBooleanRule.OPPOSITE, target.rule());
		// what the pipeline would pass if it handed the fixer the tab-expanded column unconverted
		assertNull(
				PreferDirectBooleanReturnCheck.locateAt(root, lines, line, 12),
				"the tab-expanded column must not resolve"
		);
	}

	@Test
	public void everyReportedPositionInTheCorpusResolvesToARule() throws Exception {
		final var misses = new ArrayList<String>();
		final var targets = targetsOverTheCorpus(misses);

		final var seen = EnumSet.noneOf(DirectBooleanRule.class);
		targets.forEach(target -> seen.add(target.rule()));

		assertEquals(List.of(), misses, "reported positions the locator could not resolve");
		assertEquals(
				EnumSet.allOf(DirectBooleanRule.class),
				seen,
				"rules the corpus never drives, which would leave a dispatch arm unreachable"
		);
		assertEquals(274, targets.size(), "reported positions resolved; update when the corpus grows");
	}

	/** Resolving to the right rule is not enough: the fixer splices the span of the node it is handed. */
	@Test
	public void everyReportedPositionResolvesToTheIfKeyword() throws Exception {
		final var wrong = new ArrayList<String>();
		for (var target : targetsOverTheCorpus(new ArrayList<>())) {
			if (target.ifAst().getType() != TokenTypes.LITERAL_IF) {
				wrong.add(target.rule()
						+ " resolved to " + TokenUtil.getTokenName(target.ifAst().getType())
						+ " at line " + target.ifAst().getLineNo()
				);
			}
		}
		assertEquals(List.of(), wrong, "positions that resolved to something other than an if");
	}

	/** One {@code if} logs at most one key, which is what lets one position name one rule. */
	@Test
	public void noPositionCarriesTwoViolations() throws Exception {
		final var seen = new HashSet<String>();
		final var repeated = new TreeSet<String>();
		for (var event : BaseCheckTest.runCheck(PreferDirectBooleanReturnCheck.class, CORPUS)) {
			final var position = event.getLine() + ":" + event.getColumn();
			if (!seen.add(position))
				repeated.add(position);
		}
		assertEquals(List.of(), List.copyOf(repeated), "positions carrying more than one violation");
	}

	@Test
	public void theCodePointColumnLocatesWhereTheCharIndexMisses() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, "supplementaryBeforeIf", "if (");
		final var text = lines.get(line);
		final var charIndex = columnOf(text, "if (");
		final var codePointColumn = codePointColumnOf(text, charIndex);

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		final var hit = PreferDirectBooleanReturnCheck.locateAt(root, lines, line, codePointColumn);
		assertNotNull(hit, "the code-point column must locate the if");
		assertEquals(DirectBooleanRule.OPPOSITE, hit.rule());
		assertNull(PreferDirectBooleanReturnCheck.locateAt(root, lines, line, charIndex), "the char index must not");
	}

	/**
	 * The locator re-applies every refusal the check applies, the line-length budget included. The
	 * budget reads the buffer, so a locator that answered past it would hand the fixer a collapse the
	 * check declined to ask for.
	 */
	@Test
	public void theLocatorAnswersExactlyWhereTheCheckReports() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();

		final var reported = new TreeSet<String>();
		for (var event : BaseCheckTest.runCheck(PreferDirectBooleanReturnCheck.class, FIXTURE))
			reported.add((event.getLine() - 1) + ":" + tabColumnToCodePointColumn(lines.get(event.getLine() - 1), event.getColumn() - 1));

		final var located = new TreeSet<String>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LITERAL_IF) {
				final var line = node.getLineNo() - 1;
				if (PreferDirectBooleanReturnCheck.locateAt(root, lines, line, node.getColumnNo()) != null)
					located.add(line + ":" + node.getColumnNo());
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}

		assertEquals(reported, located);
		assertNull(locate("lengthRefused", "if ("), "the over-budget collapse is refused on both sides");
	}

	@Test
	public void theThreeRulesCarryTheirOwnPayload() throws Exception {
		final var opposite = requireNonNull(locate("oppositeForward", "if ("));
		assertEquals(DirectBooleanRule.OPPOSITE, opposite.rule());
		assertEquals(Boolean.TRUE, opposite.thenLiteral());
		assertEquals(Boolean.FALSE, opposite.pairedLiteral());

		final var redundant = requireNonNull(locate("redundantDrop", "if ("));
		assertEquals(DirectBooleanRule.REDUNDANT, redundant.rule());
		assertEquals(Boolean.TRUE, redundant.thenLiteral());
		assertEquals(Boolean.TRUE, redundant.pairedLiteral());

		final var combine = requireNonNull(locate("combineAnd", "if ("));
		assertEquals(DirectBooleanRule.COMBINE, combine.rule());
		assertNull(combine.thenLiteral(), "the non-literal branch is the one with no literal");
		assertEquals(Boolean.FALSE, combine.pairedLiteral());
		assertEquals(TokenTypes.LITERAL_RETURN, combine.thenReturn().getType());
		assertEquals(TokenTypes.LITERAL_RETURN, combine.pairedReturn().getType());
	}
}