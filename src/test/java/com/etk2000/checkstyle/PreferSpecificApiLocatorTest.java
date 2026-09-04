package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.PreferSpecificApiCheck.ApiRule;
import com.etk2000.checkstyle.PreferSpecificApiCheck.ApiTarget;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link PreferSpecificApiCheck#locateAt}, the seam the fixer dispatches on.
 *
 * <p>Two things are only testable here. The locator deliberately drops the check's three refusal
 * gates (minSdk, reflective receiver, and the scope-wide other-indices suppression), so the inputs
 * where it answers while the check stays silent have no violation to drive them through any other
 * layer. And the imaginary {@code EXPR}/{@code ELIST} nodes that borrow a reported position are
 * invisible to a fixture-driven test, because the check never logs on them.
 */
public class PreferSpecificApiLocatorTest {
	private static final String CORPUS = "preferspecificapi/cases.in.java";
	private static final String FIXTURE = "preferspecificapi/InputLocatorPositions.java";

	/** Every minSdk the topic's {@code ENTRIES} rows register, so every gated rule is reached. */
	private static final String[] MIN_SDKS = {"23", "24", "29", "30", "31", "32", "33", "34", "35"};

	/** The 0-based line of the single statement in the fixture method named {@code method}. */
	@CheckReturnValue
	private static int bodyLineOf(@Nonnull String method) throws Exception {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(parseFixture());
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				final var body = node.findFirstToken(TokenTypes.SLIST);
				if (ident != null && body != null && method.equals(ident.getText()))
					return body.getFirstChild().getLineNo() - 1;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new IllegalArgumentException("no method named " + method + " in " + FIXTURE);
	}

	/** An AST column counts code points, so a char index into a line carrying one must be converted. */
	@CheckReturnValue
	private static int codePointColumnOf(@Nonnull String line, int charIndex) {
		return line.codePointCount(0, charIndex);
	}

	/** The char index of {@code needle}, which on a tab-indented BMP-only line is not its code-point column. */
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
		return Files.readAllLines(Path.of(resource(FIXTURE).toURI()), StandardCharsets.UTF_8);
	}

	@CheckReturnValue
	@Nullable
	private static ApiTarget locateCall(@Nonnull String method, @Nonnull String called) throws Exception {
		final var line = bodyLineOf(method);
		final var text = fixtureLines().get(line);
		return PreferSpecificApiCheck.locateAt(parseFixture(), line, lparenOf(text, called));
	}

	@CheckReturnValue
	@Nullable
	private static ApiTarget locateOperator(@Nonnull String method, @Nonnull String operator) throws Exception {
		final var line = bodyLineOf(method);
		final var text = fixtureLines().get(line);
		return PreferSpecificApiCheck.locateAt(parseFixture(), line, codePointColumnOf(text, columnOf(text, operator)));
	}

	/**
	 * The column a {@code METHOD_CALL} for {@code called} reports at, which is its <em>opening
	 * paren</em> and not the start of the name. Encoding that here rather than in each test is the
	 * point: the locator rests on a call and a comparison never sharing a character, and a test
	 * that searched for the name alone would silently probe the wrong column.
	 */
	@CheckReturnValue
	private static int lparenOf(@Nonnull String line, @Nonnull String called) {
		return codePointColumnOf(line, columnOf(line, called + "(") + called.length());
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var file = new File(resource(FIXTURE).toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource(@Nonnull String path) {
		return requireNonNull(
				PreferSpecificApiLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path),
				"Test input file not found: " + path
		);
	}

	/**
	 * Mirrors {@code CheckstyleFixAction.tabColumnToCharIndex}, which the fix pipeline applies to
	 * a reported column before calling the fixer.
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

	/** Whether the check reports anything on the statement line of {@code method} at {@code minSdk}. */
	@CheckReturnValue
	private static boolean violatesAt(@Nonnull String method, @Nonnull String minSdk) throws Exception {
		final var line = bodyLineOf(method) + 1;
		for (var event : BaseCheckTest.runCheck(PreferSpecificApiCheck.class, FIXTURE, "minSdk", minSdk)) {
			if (event.getLine() == line)
				return true;
		}
		return false;
	}

	@Test
	public void aCommentInsideTheCallDoesNotMoveTheReportedPosition() throws Exception {
		final var target = locateCall("commentInsideChain", "count");
		assertNotNull(target);
		assertEquals(ApiRule.STREAM_COUNT, target.rule());
	}

	/**
	 * {@code ELIST} borrows the {@code ==} column here and, unlike a one-child {@code EXPR}, has
	 * two children, so a classifier reading {@code left}/{@code right} off it would see
	 * {@code EXPR} and {@code COMMA} rather than short-circuiting on a null right operand.
	 */
	@Test
	public void anElistBorrowingTheOperatorColumnDoesNotDisplaceTheComparison() throws Exception {
		final var target = locateOperator("elistBorrowsOperatorColumn", "==");
		assertNotNull(target);
		assertEquals(ApiRule.SIZE_IS_EMPTY, target.rule());
		assertEquals(TokenTypes.EQUAL, target.node().getType(), "the fixer must receive the operator, not the ELIST");
	}

	@Test
	public void anExprBorrowingTheLparenColumnDoesNotDisplaceTheCall() throws Exception {
		final var target = locateCall("exprBorrowsLparenColumn", "get");
		assertNotNull(target);
		assertEquals(ApiRule.GET_FIRST, target.rule());
		assertEquals(TokenTypes.METHOD_CALL, target.node().getType(), "the fixer must receive the call, not the EXPR");
	}

	@Test
	public void anExprBorrowingTheOperatorColumnDoesNotDisplaceTheComparison() throws Exception {
		final var target = locateOperator("exprBorrowsOperatorColumn", "==");
		assertNotNull(target);
		assertEquals(ApiRule.SIZE_IS_EMPTY, target.rule());
		assertEquals(TokenTypes.EQUAL, target.node().getType(), "the fixer must receive the operator, not the EXPR");
	}

	@Test
	public void aPositionNoNodeStartsAtResolvesToNothing() throws Exception {
		final var line = bodyLineOf("getZero");
		final var text = fixtureLines().get(line);
		// the second character of the receiver: inside a token, so no node begins there at all
		final var inside = codePointColumnOf(text, columnOf(text, "al.get") + 1);
		assertNull(PreferSpecificApiCheck.locateAt(parseFixture(), line, inside));
	}

	/**
	 * The migration's load-bearing claim: the fixer stops reading the line's text, so any reported
	 * position the locator cannot resolve becomes a fix that silently stops happening. Runs the
	 * real check over the whole corpus at every registered minSdk and requires a rule for all of
	 * them.
	 */
	@Test
	public void everyReportedPositionInTheCorpusResolvesToARule() throws Exception {
		final var url = resource(CORPUS);
		final var lines = Files.readAllLines(Path.of(url.toURI()), StandardCharsets.UTF_8);
		final var root = JavaParser.parseFileText(
				new FileText(new File(url.toURI()), lines), JavaParser.Options.WITHOUT_COMMENTS
		);

		final var seen = EnumSet.noneOf(ApiRule.class);
		final var misses = new ArrayList<String>();
		var located = 0;
		for (var minSdk : MIN_SDKS) {
			for (var event : BaseCheckTest.runCheck(PreferSpecificApiCheck.class, CORPUS, "minSdk", minSdk)) {
				final var text = lines.get(event.getLine() - 1);
				final var target = PreferSpecificApiCheck.locateAt(
						root, event.getLine() - 1, tabColumnToCodePointColumn(text, event.getColumn() - 1)
				);
				if (target == null)
					misses.add("minSdk " + minSdk + " line " + event.getLine() + ": " + text.strip());
				else {
					seen.add(target.rule());
					++located;
				}
			}
		}

		assertEquals(List.of(), misses, "reported positions the locator could not resolve");
		assertEquals(
				EnumSet.allOf(ApiRule.class),
				seen,
				"rules the corpus never drives, which would leave a dispatch arm unreachable"
		);
		assertEquals(1642, located, "reported positions resolved; update when the corpus grows");
	}

	@Test
	public void getSizeMinusOneIsLastAndGetZeroIsFirst() throws Exception {
		final var first = locateCall("getZero", "get");
		final var last = locateCall("getSizeMinusOne", "get");
		assertNotNull(first);
		assertNotNull(last);
		assertEquals(ApiRule.GET_FIRST, first.rule());
		assertEquals(ApiRule.GET_LAST, last.rule());
	}

	@Test
	public void indexOfSingleCharAndItsComparisonAreDistinctRulesOnOneLine() throws Exception {
		final var call = locateCall("indexOfSingleCharComparison", "indexOf");
		final var comparison = locateOperator("indexOfSingleCharComparison", "!=");
		assertNotNull(call);
		assertNotNull(comparison);
		assertEquals(ApiRule.INDEX_OF_CHAR, call.rule());
		assertEquals(ApiRule.INDEX_OF_CONTAINS, comparison.rule());
	}

	/**
	 * The check never visits a field initializer ({@code getDefaultTokens} registers only method
	 * and initializer scopes), so this is a position it cannot report. The locator answering anyway
	 * is deliberate and harmless, because nothing dispatches the fixer here.
	 */
	@Test
	public void locatorAnswersOutsideEveryScopeTokenTheCheckVisits() throws Exception {
		final var lines = fixtureLines();
		var line = -1;
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains("fieldInitializer ="))
				line = i;
		}
		assertNotEquals(-1, line, "the fixture must still declare the initialized field");

		final var target = PreferSpecificApiCheck.locateAt(
				parseFixture(), line, lparenOf(lines.get(line), "emptyList")
		);
		assertNotNull(target);
		assertEquals(ApiRule.COLLECTIONS_FACTORY, target.rule());
		for (var event : BaseCheckTest.runCheck(PreferSpecificApiCheck.class, FIXTURE, "minSdk", "35"))
			assertNotEquals(line + 1, event.getLine(), "the check must not report a field initializer");
	}

	/** The locator carries no minSdk, so it answers identically on both sides of a gate. */
	@Test
	public void locatorIgnoresTheMinSdkGate() throws Exception {
		final var target = locateCall("getZero", "get");
		assertNotNull(target);
		assertEquals(ApiRule.GET_FIRST, target.rule());
		assertFalse(violatesAt("getZero", "34"), "getFirst is gated off below 35");
		assertTrue(violatesAt("getZero", "35"), "and the gate is what silences it, not the shape");
	}

	/**
	 * The third dropped gate, and the only scope-wide one: the check suppresses a {@code get(0)}
	 * whose receiver also indexes elsewhere in the same scope. A node-local locator cannot see
	 * that, which is why it must never be used as a detector.
	 */
	@Test
	public void locatorIgnoresTheScopeWideOtherIndicesGate() throws Exception {
		final var target = locateCall("otherIndicesSuppressed", "get");
		assertNotNull(target);
		assertEquals(ApiRule.GET_FIRST, target.rule());
		assertFalse(violatesAt("otherIndicesSuppressed", "35"), "the sibling get(2) suppresses the check");
	}

	@Test
	public void removeSizeMinusOneIsLastAndRemoveZeroIsFirst() throws Exception {
		final var first = locateCall("removeZero", "remove");
		final var last = locateCall("removeSizeMinusOne", "remove");
		assertNotNull(first);
		assertNotNull(last);
		assertEquals(ApiRule.REMOVE_FIRST, first.rule());
		assertEquals(ApiRule.REMOVE_LAST, last.rule());
	}

	@Test
	public void stringFormatSplitsOnArgumentCount() throws Exception {
		final var strip = locateCall("stringFormatOneArgument", "format");
		final var formatted = locateCall("stringFormatTwoArguments", "format");
		assertNotNull(strip);
		assertNotNull(formatted);
		assertEquals(ApiRule.STRING_FORMAT_STRIP, strip.rule());
		assertEquals(ApiRule.STRING_FORMAT_FORMATTED, formatted.rule());
	}

	/**
	 * A supplementary character ahead of the call makes the char index and the code-point column
	 * diverge. {@code locateAt} matches against {@link DetailAST#getColumnNo}, so it must be handed
	 * the code-point column; passing the char index is the silent-miss defect this pins.
	 */
	@Test
	public void supplementaryCharacterMakesTheCharIndexMissAndTheCodePointColumnHit() throws Exception {
		final var line = bodyLineOf("supplementaryBeforeGetZero");
		final var text = fixtureLines().get(line);
		final var charIndex = columnOf(text, "get(") + "get".length();
		final var codePointColumn = codePointColumnOf(text, charIndex);
		final var root = parseFixture();

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		final var hit = PreferSpecificApiCheck.locateAt(root, line, codePointColumn);
		assertNotNull(hit, "the code-point column must locate the call");
		assertEquals(ApiRule.GET_FIRST, hit.rule());
		assertNull(PreferSpecificApiCheck.locateAt(root, line, charIndex), "the char index must not");
	}

	@Test
	public void trimLengthComparisonIsBlankAndPlainLengthComparisonIsEmpty() throws Exception {
		final var trimmed = locateOperator("trimLengthComparison", "==");
		final var plain = locateOperator("plainLengthComparison", "==");
		assertNotNull(trimmed);
		assertNotNull(plain);
		assertEquals(ApiRule.TRIM_LENGTH_IS_BLANK, trimmed.rule());
		assertEquals(ApiRule.SIZE_IS_EMPTY, plain.rule());
	}

	@Test
	public void unmodifiableListCollapsesAnAsListAndCopiesAnythingElse() throws Exception {
		final var collapse = locateCall("unmodifiableOfAsList", "unmodifiableList");
		final var copy = locateCall("unmodifiableOfVariable", "unmodifiableList");
		assertNotNull(collapse);
		assertNotNull(copy);
		assertEquals(ApiRule.UNMODIFIABLE_AS_LIST, collapse.rule());
		assertEquals("List.of", collapse.replacement());
		assertEquals(ApiRule.COLLECTIONS_COPY_OF, copy.rule());
		assertEquals("List.copyOf", copy.replacement());
	}
}