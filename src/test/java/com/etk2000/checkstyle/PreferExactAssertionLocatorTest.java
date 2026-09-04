package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionRule;
import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionTarget;
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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Direct tests for {@link PreferExactAssertionCheck#locateAt}, the seam the fixer dispatches on.
 *
 * <p>The fixer no longer finds the call, its receiver or its arguments in the line text, so a
 * reported position the locator cannot resolve is a fix that silently stops happening, and a position
 * it resolves to the wrong call is a rewrite applied to someone else's violation. Both are invisible
 * to a fixture-driven test, which only sees the output.
 */
public class PreferExactAssertionLocatorTest {
	private static final String CORPUS = "preferexactassertion/cases.in.java";
	private static final String FIXTURE = "preferexactassertion/InputLocatorPositions.java";

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

	/**
	 * The targets at the call {@code needle} opens on the first line of {@code method} that carries it.
	 * {@code needle} ends with the {@code (} the check anchors on, so the position taken is its last
	 * character rather than its first.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AssertionTarget> locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, method, needle);
		final var text = lines.get(line);
		final var charIndex = columnOf(text, needle) + needle.length() - 1;
		return PreferExactAssertionCheck.locateAt(root, lines, line, codePointColumnOf(text, charIndex));
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
				PreferExactAssertionLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path),
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

	/**
	 * Every target the check's own output over {@link #FIXTURE} drives. Over the fixture rather than
	 * {@link #CORPUS}, because a slice's {@code // imports:} directives are applied to a translated copy
	 * the check runs on, so the tree parsed from the corpus as written has none of those imports in
	 * scope and the framework gate refuses targets the check reported.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AssertionTarget> targetsOverTheFixture(@Nonnull List<String> misses) throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var targets = new ArrayList<AssertionTarget>();
		for (var event : BaseCheckTest.runCheck(PreferExactAssertionCheck.class, FIXTURE)) {
			final var text = lines.get(event.getLine() - 1);
			final var found = PreferExactAssertionCheck.locateAt(
					root, lines, event.getLine() - 1, tabColumnToCodePointColumn(text, event.getColumn() - 1)
			);
			if (found.isEmpty())
				misses.add("line " + event.getLine() + ": " + text.strip());
			else
				targets.addAll(found);
		}
		return targets;
	}

	/** The position predates any sibling fix applied to the same line earlier in the pass. */
	@Test
	public void aStaleCallPositionResolvesToNothing() throws Exception {
		final var lines = new ArrayList<>(fixtureLines());
		final var root = parseFixture();
		final var line = lineWith(root, lines, "instanceofCall", "assertTrue(");
		final var column = codePointColumnOf(lines.get(line), columnOf(lines.get(line), "assertTrue(") + 10);

		// what a rewrite leaves behind: the statement line is gone and everything below moved up
		lines.remove(line);
		final var rewritten = JavaParser.parseFileText(
				new FileText(new File(resource(FIXTURE).toURI()), lines), JavaParser.Options.WITHOUT_COMMENTS
		);
		assertEquals(List.of(), PreferExactAssertionCheck.locateAt(rewritten, lines, line, column));
	}

	@Test
	public void aTabIndentedPositionResolvesAfterConversion() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, "tabIndentedCall", "assertTrue(");
		final var text = lines.get(line);
		final var charIndex = columnOf(text, "assertTrue(") + 10;
		assertEquals(13, charIndex, "the call must sit at three tabs");

		final var targets = PreferExactAssertionCheck.locateAt(root, lines, line, charIndex);
		assertEquals(1, targets.size(), "the code-point column is the char index here, tabs being one code point");
		assertEquals(AssertionRule.INSTANCEOF, targets.getFirst().rule());
		// what the pipeline would pass if it handed the fixer the tab-expanded column unconverted
		assertEquals(
				List.of(),
				PreferExactAssertionCheck.locateAt(root, lines, line, 22),
				"the tab-expanded column must not resolve"
		);
	}

	/** Resolving to the right rule is not enough: the fixer splices the span of the node it is handed. */
	@Test
	public void everyReportedPositionResolvesToAMethodCall() throws Exception {
		final var wrong = new ArrayList<String>();
		for (var target : targetsOverTheFixture(new ArrayList<>())) {
			if (target.methodCall().getType() != TokenTypes.METHOD_CALL) {
				wrong.add(target.rule()
						+ " resolved to " + TokenUtil.getTokenName(target.methodCall().getType())
						+ " at line " + target.methodCall().getLineNo()
				);
			}
		}
		assertEquals(List.of(), wrong, "positions that resolved to something other than a method call");
	}

	@Test
	public void everyReportedPositionResolvesToARule() throws Exception {
		final var misses = new ArrayList<String>();
		final var targets = targetsOverTheFixture(misses);

		final var seen = EnumSet.noneOf(AssertionRule.class);
		targets.forEach(target -> seen.add(target.rule()));

		assertEquals(List.of(), misses, "reported positions the locator could not resolve");
		assertEquals(
				EnumSet.allOf(AssertionRule.class),
				seen,
				"rules the fixture never drives, which would leave a dispatch arm unreachable"
		);
	}

	/**
	 * The anchor is the {@code (}, not the method name. A locator keyed off the name would miss every
	 * call whose name and {@code (} sit on different lines, which the check reports on the {@code (}
	 * line.
	 */
	@Test
	public void theAnchorIsTheOpenParenNotTheName() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var nameLine = lineWith(root, lines, "splitNameAndParen", "assertTrue");
		final var parenLine = nameLine + 1;
		assertEquals(2, columnOf(lines.get(parenLine), "("), "the paren must open the next line");

		assertEquals(
				1,
				PreferExactAssertionCheck.locateAt(root, lines, parenLine, 2).size(),
				"the open paren resolves"
		);
		assertEquals(
				List.of(),
				PreferExactAssertionCheck.locateAt(root, lines, nameLine, 2),
				"the name line does not"
		);
	}

	@Test
	public void theCodePointColumnLocatesWhereTheCharIndexMisses() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();
		final var line = lineWith(root, lines, "supplementaryBeforeCall", "assertTrue(");
		final var text = lines.get(line);
		final var charIndex = columnOf(text, "assertTrue(") + 10;
		final var codePointColumn = codePointColumnOf(text, charIndex);

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		assertEquals(
				1,
				PreferExactAssertionCheck.locateAt(root, lines, line, codePointColumn).size(),
				"the code-point column must locate the call"
		);
		assertEquals(
				List.of(),
				PreferExactAssertionCheck.locateAt(root, lines, line, charIndex),
				"the char index must not"
		);
	}

	/**
	 * A position carrying two rules is the one shape the fixer refuses outright, because the pipeline
	 * calls it once per event and says nothing about which event this is. The corpus holds exactly one,
	 * so a change that makes the anchor per-argument would show up here rather than as a silent loss of
	 * the refusal's only coverage.
	 */
	@Test
	public void theCorpusCarriesOneMultiTargetPosition() throws Exception {
		final var perPosition = new TreeMap<String, Integer>();
		for (var event : BaseCheckTest.runCheck(PreferExactAssertionCheck.class, CORPUS))
			perPosition.merge(event.getLine() + ":" + event.getColumn(), 1, Integer::sum);

		final var multi = perPosition.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.toList();
		assertEquals(1, multi.size(), "positions carrying more than one violation: " + multi);
	}

	/**
	 * Text ahead of the call is hoisted to its own line only when the assertion is a statement among
	 * statements. For a lambda or control-flow body that text is a header the assertion belongs to, and
	 * hoisting emits the body at the header's own indent.
	 *
	 * <p>The control-flow shape itself cannot be a slice: a one-line {@code if} body is a
	 * {@code ControlFlowBraces} violation, so the full-config lint over {@code cases.in.java} refuses
	 * it. Both polarities of the predicate are pinned here instead.
	 */
	@Test
	public void theLeadingTextHoistsOnlyForASiblingStatement() throws Exception {
		assertTrue(
				PreferExactAssertionCheck.hoistsLeadingText(locate("instanceofCall", "assertTrue(").getFirst()),
				"a statement among statements hoists"
		);
		assertFalse(
				PreferExactAssertionCheck.hoistsLeadingText(locate("lambdaBodyCall", "assertTrue(").getFirst()),
				"a lambda body does not"
		);
		// being in a statement list is not enough: the list's own brace precedes the call and is a
		// header of the same kind, so hoisting past it lands the body one indent level short
		assertFalse(
				PreferExactAssertionCheck.hoistsLeadingText(locate("oneLineBodyCall", "assertTrue(").getFirst()),
				"a statement list whose own brace is on the call's line does not"
		);
	}

	/**
	 * The locator re-applies every refusal the check applies, the line-length budget included. The
	 * budget reads the buffer, so a locator that answered past it would hand the fixer a rewrite the
	 * check declined to ask for.
	 */
	@Test
	public void theLocatorAnswersExactlyWhereTheCheckReports() throws Exception {
		final var lines = fixtureLines();
		final var root = parseFixture();

		final var reported = new TreeSet<String>();
		for (var event : BaseCheckTest.runCheck(PreferExactAssertionCheck.class, FIXTURE)) {
			final var text = lines.get(event.getLine() - 1);
			reported.add((event.getLine() - 1) + ":" + tabColumnToCodePointColumn(text, event.getColumn() - 1));
		}

		final var located = new TreeSet<String>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_CALL) {
				final var line = node.getLineNo() - 1;
				if (!PreferExactAssertionCheck.locateAt(root, lines, line, node.getColumnNo()).isEmpty())
					located.add(line + ":" + node.getColumnNo());
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}

		assertEquals(reported, located);
		assertEquals(List.of(), locate("lengthRefused", "assertTrue("), "the over-budget rewrite is refused");
		// the shape decides the rule, so an over-budget `instanceof` drops the report rather than
		// falling through to the negation arm and reporting a different rule at the same site
		assertEquals(
				List.of(),
				locate("lengthRefusedNegatedInstanceof", "assertTrue("),
				"the negation rule must not fill in for a refused instanceof rewrite"
		);
	}

	/**
	 * A {@code void} assertion is still an expression where a lambda body is one, and there it has no
	 * {@code ;} of its own. The locator must answer there, because the rewrite replaces the call rather
	 * than the statement.
	 */
	@Test
	public void theLocatorAnswersInsideALambdaBody() throws Exception {
		final var targets = locate("lambdaBodyCall", "assertTrue(");
		assertEquals(1, targets.size());
		assertEquals(AssertionRule.INSTANCEOF, targets.getFirst().rule());
		assertTrue(
				PreferExactAssertionCheck.rewriteOf(fixtureLines(), targets.getFirst())
						instanceof PreferExactAssertionCheck.Rewritten rewritten
						&& !rewritten.call().endsWith(";"),
				"a lambda body's assertion is rewritten without a semicolon"
		);
	}

	@Test
	public void theQualifiedReceiverStaysOutOfTheSpan() throws Exception {
		final var lines = fixtureLines();
		final var targets = locate("qualifiedCall", "assertTrue(");
		assertEquals(1, targets.size());
		assertEquals("Assertions", targets.getFirst().qualifierSimpleName());

		final var span = requireNonNull(PreferExactAssertionCheck.spanOf(lines, targets.getFirst()));
		final var text = lines.get(span.start().line());
		assertEquals(
				columnOf(text, "org.junit"),
				span.start().index(),
				"the whole receiver chain is on the call's line, so the span opens at it"
		);
	}

	@Test
	public void theThreeRulesCarryTheirOwnPayload() throws Exception {
		final var comparison = locate("comparisonCall", "assertTrue(").getFirst();
		assertEquals(AssertionRule.COMPARISON, comparison.rule());
		assertEquals("assertEquals", comparison.replacementMethod());
		assertEquals(TokenTypes.EQUAL, requireNonNull(comparison.comparisonOp()).getType());

		final var instanceOf = locate("instanceofCall", "assertTrue(").getFirst();
		assertEquals(AssertionRule.INSTANCEOF, instanceOf.rule());
		assertEquals("assertInstanceOf", instanceOf.replacementMethod());
		assertEquals(TokenTypes.LITERAL_INSTANCEOF, requireNonNull(instanceOf.instanceofAst()).getType());

		final var negation = locate("negationCall", "assertTrue(").getFirst();
		assertEquals(AssertionRule.NEGATION, negation.rule());
		assertEquals("assertFalse", negation.replacementMethod());
		assertEquals(1, negation.notCount());
	}
}