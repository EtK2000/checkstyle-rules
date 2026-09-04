package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.PreferMathMethodCheck;
import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class PreferMathMethodFixerTest {
	private static final int PROBE_LINE = 2;
	/**
	 * A parseable buffer holding one real ternary and one written inside a trailing comment, so a
	 * position test can tell the two apart. The fixer locates through the check's classifier, which
	 * needs a compilation unit, so a bare statement line would refuse for want of a parse rather
	 * than for the reason a test here is asking about.
	 */
	private static final List<String> PROBE = List.of(
			"class T {",
			"\tint m(int a, int b, int c, int d) {",
			"\t\treturn a > b ? a : b; // c > d ? c : d",
			"\t}",
			"}"
	);
	private static final String PROBE_FIXED = "\t\treturn Math.max(a, b); // c > d ? c : d";
	private static final String TOPIC = "prefermathmethod";

	@CheckReturnValue
	private static int probeQuestion() {
		return PROBE.get(PROBE_LINE).indexOf('?');
	}

	private final CheckstyleFixer fixer = new PreferMathMethodFixer();

	@CheckReturnValue
	@Nonnull
	private FixAttempt fixUnderKey(@Nonnull String key, @Nonnull List<String> lines, int lineIndex, int column) {
		FixContext.setViolation(new Violation(1, "bundle", key, new Object[0], null, getClass(), key));
		try {
			return fixer.fix(lines, lineIndex, column);
		}
		finally {
			FixContext.clearViolation();
		}
	}

	@Test
	public void testClampColumnWithSupplementaryCharUsesCharIndex() {
		final var astral = "𝔘".repeat(7);
		final var statement = "\t\tString s = \"" + astral + "\"; int r = Math.max(lo, Math.min(hi, v));";
		final var lines = List.of("class T {", "\tvoid m(int lo, int hi, int v) {", statement, "\t}", "}");
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(lines, 2, statement.indexOf("Math.max(") + "Math.max".length())
		);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(
				List.of("\t\tString s = \"" + astral + "\"; int r = Math.clamp(v, lo, hi);"),
				result.replacement()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * The {@code NUM_LONG} arm of the zero test. A lint-clean slice cannot carry it: every long-zero
	 * spelling trips {@code RedundantNumericSuffixCheck}, which strips the suffix before this fixer
	 * ever sees the comparison.
	 */
	@Test
	public void testIfAbsLongZeroRewrites() {
		final var lines = List.of(
				"class T {",
				"\tlong m(long a, long r) {",
				"\t\tif (a < 0L)",
				"\t\t\tr = -a;",
				"\t\telse",
				"\t\t\tr = a;",
				"\t\treturn r;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(List.of("\t\tr = Math.abs(a);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * The reported column arrives as a char index while an AST position counts code points, and only
	 * a supplementary char ahead of the {@code if} on its line separates the two. Such a line always
	 * refuses, since code before the construct is what the rewrite will not write under; the skip
	 * reason is what says the conversion still landed on the keyword, because a column left
	 * unconverted names nothing and falls through to the expression skip instead.
	 */
	@Test
	public void testIfColumnWithSupplementaryCharUsesCharIndex() {
		final var astral = "𝔘".repeat(7);
		final var statement = "\t\tString s = \"" + astral + "\"; if (a > b) r = a; else r = b;";
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int r) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, statement.indexOf("if (")));
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, skip.reason());
	}

	/** {@code OneStatementPerLine} rules this out as a slice. */
	@Test
	public void testIfFollowedByCodeOnTheSameLineKeepsSuffix() {
		final var statement = "\t\tif (a > b) r = a; else r = b; System.out.println(r);";
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int r) {", statement, "\t}", "}");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tr = Math.max(a, b); System.out.println(r);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * An old-style {@code switch} label's block is not a scope of its own, so a local declared under
	 * one label is still in scope under the next and the collapse's "the declaration is dead" premise
	 * fails. The rewrite still applies; it just leaves the declaration and the return in place.
	 * A braced case body <em>is</em> a scope, and {@code NoCaseBraces} requires braces around a case
	 * that declares one, so the unbraced form this guards cannot exist in a linted fixture.
	 */
	@Test
	public void testIfInSwitchCaseKeepsTheDeclaration() {
		final var lines = List.of(
				"class T {",
				"\tint m(int k, int a, int b) {",
				"\t\tswitch (k) {",
				"\t\t\tcase 1:",
				"\t\t\t\tint r;",
				"\t\t\t\tif (a > b)",
				"\t\t\t\t\tr = a;",
				"\t\t\t\telse",
				"\t\t\t\t\tr = b;",
				"\t\t\t\treturn r;",
				"\t\t\tcase 2:",
				"\t\t\t\tr = 7;",
				"\t\t\t\treturn r;",
				"\t\t}",
				"\t\treturn 0;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 4));
		assertEquals(5, result.startLine());
		assertEquals(8, result.endLine());
		assertEquals(List.of("\t\t\t\tr = Math.max(a, b);"), result.replacement());
	}

	/**
	 * The key guard refuses a column the ternary anchor would accept, which is what keeps a stale
	 * column from rewriting the wrong construct: {@code applyFixes} can hand a fixer a column from
	 * before an earlier same-line edit. The boundary partner below proves the anchor accepts it.
	 */
	@Test
	public void testIfKeyKeepsTernaryRewriteOffTheIfLine() {
		final var statement = "\t\tif (a > b) r = c > d ? c : d;";
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int c, int d, int r) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixUnderKey(PreferMathMethodCheck.MSG_METHOD_IF, lines, 2, statement.indexOf('?'))
		);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	/**
	 * A buffer that does not parse classifies as nothing, so neither shape answers for the position
	 * and the if key's own skip is never reached. This is the one test driving the parse firewall on
	 * the if path.
	 */
	@Test
	public void testIfKeyOnUnparseableBufferSkips() {
		final var lines = List.of("if (a > b) // keep in sync with x > y ? x : y below");
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixUnderKey(PreferMathMethodCheck.MSG_METHOD_IF, lines, 0, 0)
		);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testIfNestedOnOneLineRefuses() {
		final var statement = "\t\tif (cond) if (a > b) r = a; else r = b;";
		final var lines = List.of("class T {", "\tvoid m(boolean cond, int a, int b, int r) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(lines, 2, statement.indexOf("if (a > b)"))
		);
		assertEquals(SkipMessages.MATH_METHOD_SKIP_IF, skip.reason());
	}

	@Test
	public void testIfNoBodyLineReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "if_no_body_line_returns_skip", SkipMessages.MATH_METHOD_SKIP);
	}

	/**
	 * An {@code if} that assigns, with neither an {@code else} nor a declaration above to supply the
	 * other value, is a position the check never reports; driven directly, the fixer must recognise
	 * nothing there rather than invent the missing branch.
	 */
	@Test
	public void testIfNoElsePlainAssignWithoutDeclSkips() {
		final var lines = List.of(
				"class T {",
				"\tvoid m(int a, int b, int r) {",
				"\t\tif (a > b)",
				"\t\t\tr = a;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 2));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, result.reason());
	}

	/** The operand class is the parser's; {@code IllegalSymbol} keeps this out of a fixture. */
	@Test
	public void testIfNonAsciiIdentifierRewrites() {
		final var lines = List.of(
				"class T {",
				"\tint m(int α, int b, int r) {",
				"\t\tif (α > b)",
				"\t\t\tr = α;",
				"\t\telse",
				"\t\t\tr = b;",
				"\t\treturn r;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		assertEquals(List.of("\t\tr = Math.max(α, b);"), result.replacement());
	}

	/** {@code OneStatementPerLine} rules this out as a slice. */
	@Test
	public void testIfOnOneLineRewrites() {
		final var statement = "\t\tif (a > b) r = a; else r = b;";
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int r) {", statement, "\t}", "}");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tr = Math.max(a, b);"), result.replacement());
	}

	@Test
	public void testIfRewriteIsNotAppliedTwice() {
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int r) {", "\t\tif (a > b) r = a; else r = b;", "\t}", "}");
		final var first = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		final var fixed = new ArrayList<>(lines);
		fixed.set(2, first.replacement().getFirst());
		final var second = assertInstanceOf(SkipResult.class, fixer.fix(fixed, 2, 2));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, second.reason());
	}

	/** The form feed and the vertical tab cannot live in a linted fixture. */
	@ParameterizedTest
	@ValueSource(strings = {" ", "  ", "\t", "\f", "\u000B"})
	public void testIfSpaceClassSeparators(String gap) {
		final var lines = List.of(
				"class T {",
				"\tvoid m(int a, int b, int r) {",
				"\t\tif (a" + gap + ">" + gap + "b) r = a; else r = b;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 2));
		assertEquals(List.of("\t\tr = Math.max(a, b);"), result.replacement());
	}

	@Test
	public void testNoMatchClampUnbalancedParens() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_match_clamp_unbalanced_parens", SkipMessages.MATH_METHOD_SKIP);
	}

	@Test
	public void testNonIfKeyStillRewritesTernaryOnTheSameLine() {
		final var statement = "\t\tif (a > b) r = c > d ? c : d;";
		final var lines = List.of("class T {", "\tvoid m(int a, int b, int c, int d, int r) {", statement, "\t}", "}");
		final var result = assertInstanceOf(
				FixResult.class,
				fixUnderKey("prefer.replacement", lines, 2, statement.indexOf('?'))
		);
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\tif (a > b) r = Math.max(c, d);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * A line that merely closes a text block opens with {@code """}, which a per-line mask reads as
	 * an opener.
	 */
	@Test
	public void testTernaryAfterTextBlockCloseStillRewrites() {
		final var statement = "\t\t\t\t\"\"\" + (a > b ? a : b);";
		final var lines = List.of(
				"class T {",
				"\tString m(int a, int b) {",
				"\t\treturn \"\"\"",
				"\t\t\t\tc > d ? c : d",
				statement,
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, statement.indexOf('?')));
		assertEquals(List.of("\t\t\t\t\"\"\" + (Math.max(a, b));"), result.replacement());
	}

	/**
	 * Paired with {@link #testTernaryColumnAtQuestionRewritesCodeNotComment}, which is the one
	 * column that does resolve.
	 */
	@Test
	public void testTernaryColumnAtExpressionStartRefuses() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(PROBE, PROBE_LINE, PROBE.get(PROBE_LINE).indexOf("a > b"))
		);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryColumnAtMatchEndRefuses() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(PROBE, PROBE_LINE, probeQuestion() + 1));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryColumnAtQuestionRewritesCodeNotComment() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(PROBE, PROBE_LINE, probeQuestion()));
		assertEquals(List.of(PROBE_FIXED), result.replacement());
	}

	@Test
	public void testTernaryColumnInsideCommentMatchRefuses() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(PROBE, PROBE_LINE, PROBE.get(PROBE_LINE).lastIndexOf('?'))
		);
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryColumnInsideStringMatchRefuses() {
		final var statement = "\t\treturn foo(\"c > d ? c : d\", a > b ? a : b);";
		final var lines = List.of("class T {", "\tString m(int a, int b) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryColumnPastEndOfLineRefuses() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(PROBE, PROBE_LINE, 100));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	/**
	 * Seven supplementary characters put the ternary's char index, which the pipeline passes,
	 * seven past its code-point column, which is further than the match's own offset to the
	 * {@code ?}: a fixer converting the index again, as if it counted code points, would anchor
	 * past the match and refuse a fixable line. Every fixture in the topic is ASCII, where the
	 * two agree.
	 */
	@Test
	public void testTernaryColumnWithSupplementaryCharUsesCharIndex() {
		final var astral = "𝔘".repeat(7);
		final var statement = "\t\tString s = \"" + astral + "\"; int r = a > b ? a : b;";
		final var lines = List.of("class T {", "\tvoid m(int a, int b) {", statement, "\t}", "}");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(
				List.of("\t\tString s = \"" + astral + "\"; int r = Math.max(a, b);"),
				result.replacement()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * The partner that forbids the lazy fix: refusing whenever the column is 0 would pass
	 * every other cell here. The {@code ?} is put at column 0 by continuing the expression onto
	 * its own zero-indented line.
	 */
	@Test
	public void testTernaryColumnZeroAtMatchStartStillRewrites() {
		final var lines = List.of(
				"class T {",
				"\tint m(int a, int b) {",
				"\t\treturn a > b",
				"? a : b;",
				"\t}",
				"}"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 0));
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("\t\treturn Math.max(a, b);"), result.replacement());
	}

	@Test
	public void testTernaryColumnZeroDoesNotFallBackToFirstMatch() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(PROBE, PROBE_LINE, 0));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryColumnZeroOutsideEveryMatchRefuses() {
		final var lines = List.of(
				"class T {",
				"\tint m(int a, int b) {",
				"\t\treturn a > b ? a : b; // mirrors c > d ? c : d",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryCommentInsideExpressionAfterQuestionRefuses() {
		final var statement = "\t\treturn a > b ? /* note */ a : b;";
		final var lines = List.of("class T {", "\tint m(int a, int b) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	@Test
	public void testTernaryCommentInsideExpressionRefusesRatherThanDeletingIt() {
		final var statement = "\t\treturn a /* note */ > b ? a : b;";
		final var lines = List.of("class T {", "\tint m(int a, int b) {", statement, "\t}", "}");
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	/**
	 * The operand class is the parser's, not a character range the fixer keeps of its own, so an
	 * identifier outside ASCII is an operand like any other. Every fixture in the topic is ASCII,
	 * and the project forbids such a character in source, so this is the only place it is asked.
	 */
	@Test
	public void testTernaryNonAsciiIdentifierRewrites() {
		final var statement = "\t\treturn α > b ? α : b;";
		final var lines = List.of("class T {", "\tint m(int α, int b) {", statement, "\t}", "}");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(List.of("\t\treturn Math.max(α, b);"), result.replacement());
	}

	/**
	 * Ternary shapes the check declines, each asked at its own {@code ?}.
	 * {@code cases.clean.java} holds the same shapes for the check's side.
	 */
	@CsvSource(delimiter = '|', value = {
			"boolean flag, int a, int b | return flag ? a : b;",
			"int a, int b               | return (a) > (b) ? (a) : (b);",
			"int a, int b               | return a == b ? a : b;",
			"int a, int b               | return a > b ? (a) : (b);",
			"int a, int b, int c, int d | return a > b ? a + c : d;",
			"int a, int b, int c        | return a > b ? a : c;"
	})
	@ParameterizedTest
	public void testTernaryRefusesWithoutRewriting(String params, String statement) {
		final var body = "\t\t" + statement.strip();
		final var lines = List.of("class T {", "\tint m(" + params.strip() + ") {", body, "\t}", "}");
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, body.indexOf('?')));
		assertEquals(SkipMessages.MATH_METHOD_SKIP, skip.reason());
	}

	/** Every fixture in the topic separates with a single space. */
	// the gap stays out of the name: a form feed or vertical tab there is not legal XML, which breaks exporting the results
	@ParameterizedTest(name = "[{index}]")
	@ValueSource(strings = {"", " ", "  ", "\t", "\f", "\u000B"})
	public void testTernarySpaceClassSeparators(String gap) {
		final var statement =
				"\t\treturn a" + gap + ">" + gap + "b" + gap + "?" + gap + "a" + gap + ":" + gap + "b;";
		final var lines = List.of("class T {", "\tint m(int a, int b) {", statement, "\t}", "}");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, statement.indexOf('?')));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("\t\treturn Math.max(a, b);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}