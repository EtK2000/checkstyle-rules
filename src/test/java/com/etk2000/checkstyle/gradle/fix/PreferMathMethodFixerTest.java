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

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class PreferMathMethodFixerTest {
	// the code ternary matches over [4, 17), the comment's over [22, 35)
	private static final String PROBE = "r = a > b ? a : b; // c > d ? c : d";
	private static final String PROBE_FIXED = "r = Math.max(a, b); // c > d ? c : d";
	private static final String TOPIC = "prefermathmethod";

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

	/**
	 * Sibling of {@link #testTernaryColumnWithSupplementaryCharUsesCharIndex} for the clamp
	 * arm, which anchors its prefix against the same converted column.
	 */
	@Test
	public void testClampColumnWithSupplementaryCharUsesCharIndex() {
		final var astral = "𝔘".repeat(7);
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(List.of("s = \"" + astral + "\"; r = Math.max(lo, Math.min(hi, v));"), 0, 27)
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("s = \"" + astral + "\"; r = Math.clamp(v, lo, hi);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * The column-0 route the key guard used to be the only barrier against. It passes with
	 * the guard removed too: the anchor now closes it independently, so the two are no
	 * longer a single point of failure.
	 */
	@Test
	public void testIfKeyAtColumnZeroRefuses() {
		final var lines = List.of("if (a > b) // keep in sync with x > y ? x : y below");
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixUnderKey(PreferMathMethodCheck.MSG_METHOD_IF, lines, 0, 0)
		);
		assertEquals("if-else not auto-fixable", skip.reason());
	}

	/**
	 * The key guard refuses a column the anchoring would accept. For every column the check
	 * really reports it is redundant with the anchor; what it still buys is protection from
	 * a column that is <em>not</em> where the check reported: {@code applyFixes} can hand a
	 * fixer a stale column after an earlier same-line edit. Column 15 is such a position,
	 * and the boundary partner below proves the anchor accepts it.
	 */
	@Test
	public void testIfKeyKeepsTernaryRewriteOffTheIfLine() {
		final var lines = List.of("if (a > b) r = c > d ? c : d;");
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixUnderKey(PreferMathMethodCheck.MSG_METHOD_IF, lines, 0, 15)
		);
		assertEquals("if-else not auto-fixable", skip.reason());
	}

	@Test
	public void testIfNoBodyLineReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "if_no_body_line_returns_skip", "if-else not auto-fixable");
	}

	@Test
	public void testIfPlainAssignAtFileStartFallsBackToBare() {
		// An if-else at lineIndex 0 has no line above, so the fix must not index
		// lines.get(-1). The trailing `return r;` keeps trailingReturnIndex in
		// bounds, so `declIndex >= 0` is the only guard stopping the decl+return
		// collapse from reading lines.get(-1). The check never reports at line 0
		// (an if-else is always nested), so this path is only reached by direct
		// invocation.
		final var lines = List.of(
				"if (a > b)",
				"\tr = a;",
				"else",
				"\tr = b;",
				"return r;"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("r = Math.max(a, b);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIfPlainAssignNoElseAtFileStartSkipsWithoutIndexingAbove() {
		// At lineIndex 0 with no else, tryPlainAssignShape returns null, so
		// fixIfShape's `lineIndex >= 1` guard must skip tryInitOverwriteShape,
		// which would otherwise read lines.get(-1) for the decl line.
		final var lines = List.of(
				"if (a > b)",
				"\tr = a;"
		);
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals("if-else not auto-fixable", result.reason());
	}

	@Test
	public void testNoMatchClampUnbalancedParens() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_match_clamp_unbalanced_parens", "parenthesized or multiline ternary");
	}

	@ParameterizedTest
	@ValueSource(strings = {"assert", "break", "continue", "return", "throw", "yield"})
	public void testNonDeclKeywordAboveDoesNotTriggerTrailingReturnCollapse(String keyword) {
		// DECL_LINE_PATTERN's lookahead must reject lines starting with a
		// control-flow keyword so `tryPlainAssignShape`'s decl+return collapse
		// doesn't silently delete a `<keyword> r;` statement above the if-else.
		// Synthetic input: the corruption scenario isn't reachable from
		// compilable Java (would require <keyword> r; AND the same name `r` as
		// the if-else target), so direct fixer invocation is the right level
		// for this regression test.
		final var lines = List.of(
				keyword + " r;",
				"if (a > b)",
				"\tr = a;",
				"else",
				"\tr = b;",
				"return r;"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("r = Math.max(a, b);"), result.replacement());
	}

	/** Boundary partner of {@link #testIfKeyKeepsTernaryRewriteOffTheIfLine}. */
	@Test
	public void testNonIfKeyStillRewritesTernaryOnTheSameLine() {
		final var lines = List.of("if (a > b) r = c > d ? c : d;");
		final var result = assertInstanceOf(
				FixResult.class,
				fixUnderKey("prefer.replacement", lines, 0, 15)
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("if (a > b) r = Math.max(c, d);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * A line that merely closes a text block opens with {@code """}. Masking it in
	 * isolation reads that as an opener and blanks the expression after it, turning a
	 * working fix into a skip, so the mask has to be threaded from the top of the buffer.
	 */
	@Test
	public void testTernaryAfterTextBlockCloseStillRewrites() {
		final var lines = List.of(
				"var s = \"\"\"",
				"c > d ? c : d",
				"\"\"\" + (a > b ? a : b);"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 11));
		assertEquals(List.of("\"\"\" + (Math.max(a, b));"), result.replacement());
	}

	@Test
	public void testTernaryColumnAtLastMatchCharRewrites() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(PROBE), 0, 16));
		assertEquals(List.of(PROBE_FIXED), result.replacement());
	}

	/** Boundary partner of {@link #testTernaryColumnAtLastMatchCharRewrites}. */
	@Test
	public void testTernaryColumnAtMatchEndRefuses() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(PROBE), 0, 17));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	@Test
	public void testTernaryColumnAtMatchStartRewrites() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(PROBE), 0, 4));
		assertEquals(List.of(PROBE_FIXED), result.replacement());
	}

	/**
	 * The {@code ?} is where the check reports a single-line ternary, so this is the column
	 * production actually passes.
	 */
	@Test
	public void testTernaryColumnAtQuestionRewritesCodeNotComment() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of(PROBE), 0, 10));
		assertEquals(List.of(PROBE_FIXED), result.replacement());
	}

	@Test
	public void testTernaryColumnInsideCommentMatchRefuses() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(PROBE), 0, 22));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	@Test
	public void testTernaryColumnInsideStringMatchRefuses() {
		final var line = "r = foo(\"c > d ? c : d\", a > b ? a : b);";
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(line), 0, 9));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	@Test
	public void testTernaryColumnPastEndOfLineRefuses() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(PROBE), 0, 100));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	/**
	 * Seven supplementary characters put the ternary's char index seven past its code-point
	 * column, which is further than the match's own offset to the {@code ?}: without the
	 * conversion in {@code fix} the anchor lands before the match and a fixable line is
	 * refused. Every fixture in the topic is ASCII, where the conversion is the identity.
	 */
	@Test
	public void testTernaryColumnWithSupplementaryCharUsesCharIndex() {
		final var astral = "𝔘".repeat(7);
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(List.of("s = \"" + astral + "\"; r = a > b ? a : b;"), 0, 25)
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("s = \"" + astral + "\"; r = Math.max(a, b);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	/**
	 * The partner that forbids the lazy fix: refusing whenever the column is 0 would pass
	 * every other cell here.
	 */
	@Test
	public void testTernaryColumnZeroAtMatchStartStillRewrites() {
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of("a > b ? a : b;"), 0, 0));
		assertEquals(List.of("Math.max(a, b);"), result.replacement());
	}

	/**
	 * The old fallback produced the right text for the wrong reason: it rewrote the first
	 * match anywhere rather than the one the check reported.
	 */
	@Test
	public void testTernaryColumnZeroDoesNotFallBackToFirstMatch() {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(PROBE), 0, 0));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	/**
	 * The shape that corrupted: the only match on the line lies inside the comment, and the
	 * unanchored fallback took it.
	 */
	@Test
	public void testTernaryColumnZeroOutsideEveryMatchRefuses() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(List.of("? a // mirrors c > d ? c : d"), 0, 0)
		);
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	@Test
	public void testTernaryCommentInsideExpressionAfterQuestionRefuses() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(List.of("return a > b ? /* note */ a : b;"), 0, 7)
		);
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	@Test
	public void testTernaryCommentInsideExpressionRefusesRatherThanDeletingIt() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(List.of("return a /* note */ > b ? a : b;"), 0, 7)
		);
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	/**
	 * Nothing else pins the operand class as ASCII: the supplementary characters in the other
	 * tests sit inside string literals, which the mask blanks before the operand scan ever runs.
	 */
	@Test
	public void testTernaryNonAsciiIdentifierRefuses() {
		final var skip = assertInstanceOf(
				SkipResult.class,
				fixer.fix(List.of("return α > b ? α : b;"), 0, 13)
		);
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	/**
	 * Every way the outward parse from a {@code ?} can refuse, one row per guard. The column is
	 * the {@code ?} in each line, so a refusal there is the whole-line outcome.
	 */
	@CsvSource(delimiter = '|', value = {
			"b ? a : b;                | 2",
			"= b ? a : b;              | 4",
			"a + b ? a : b;            | 6",
			"a == b ? a : b;           | 7",
			"> b ? a : b;              | 4",
			"(a) > (b) ? (a) : (b);    | 10",
			"var r = a > b ?           | 14",
			"a > b ? (a) : (b);        | 6",
			"var r = a > b ? a         | 14",
			"a > b ? a + c : d;        | 6",
			"var r = a > b ? a :       | 14",
			"r = a > b ? a : c;        | 10"
	})
	@ParameterizedTest
	public void testTernaryRefusesWithoutRewriting(String line, int column) {
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(List.of(line.strip()), 0, column));
		assertEquals("parenthesized or multiline ternary", skip.reason());
	}

	/**
	 * Every fixture in the topic separates with a single space, so narrowing the class to that one
	 * character would pass all of them. {@code \r} only ever lands past the end of a span and
	 * {@code \n} cannot occur inside a physical line, so neither is driven here.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"", " ", "  ", "\t", "\f", "\u000B"})
	public void testTernarySpaceClassSeparators(String gap) {
		final var line = "return a" + gap + ">" + gap + "b" + gap + "?" + gap + "a" + gap + ":" + gap + "b;";
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(List.of(line), 0, 7 + 3 * gap.length() + 1)
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("return Math.max(a, b);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}
}