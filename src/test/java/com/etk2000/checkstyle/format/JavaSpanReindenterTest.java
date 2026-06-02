package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class JavaSpanReindenterTest {
	@Test
	public void testAnnotatedParameterInteriorContinuation() {
		assertEquals(
				List.of("method(", "\t\t@Deprecated int x", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "@Deprecated int x", ")"), 0)
		);
	}

	@Test
	public void testArrayIndexContinuationStepTwo() {
		assertEquals(
				List.of("arr[", "\t\ti + 1", "]"),
				JavaSpanReindenter.reindent(List.of("arr[", "i + 1", "]"), 0)
		);
	}

	@Test
	public void testArrayInitBraceStepOne() {
		assertEquals(
				List.of("new int[] {", "\t1, 2, 3", "}"),
				JavaSpanReindenter.reindent(List.of("new int[] {", "1, 2, 3", "}"), 0)
		);
	}

	@Test
	public void testBaseTabsDeepNesting() {
		assertEquals(
				List.of("\t\t\tmethod(", "\t\t\t\t\targ", "\t\t\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 3)
		);
	}

	@Test
	public void testBaseTabsShiftsWholeSpan() {
		assertEquals(
				List.of("\tmethod(", "\t\t\targ", "\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 1)
		);
	}

	@Test
	public void testBaseTabsTwo() {
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\targ", "\t\t)"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 2)
		);
	}

	@Test
	public void testBlockCommentSingleLineBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tfoo(/* )]} */ y)", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "foo(/* )]} */ y)", ")"), 0)
		);
	}

	@Test
	public void testBraceBodyStepOne() {
		assertEquals(
				List.of("method(x -> {", "\tbody();", "});"),
				JavaSpanReindenter.reindent(List.of("method(x -> {", "body();", "});"), 0)
		);
	}

	@Test
	public void testBraceCloseThenParenContinuationAlignsWithOpener() {
		// the } closes the lambda brace and aligns with its opener line (base), not one continuation
		// level in, otherwise the } would be more indented than the body it closes
		assertEquals(
				List.of("a(() -> {", "\tbody();", "}, x)"),
				JavaSpanReindenter.reindent(List.of("a(() -> {", "body();", "}, x)"), 0)
		);
	}

	@Test
	public void testBracelessLambdaContinuationStepTwo() {
		assertEquals(
				List.of("cache.put(\"k\", v ->", "\t\tSystem.out.println(v)", ")"),
				JavaSpanReindenter.reindent(List.of("cache.put(\"k\", v ->", "System.out.println(v)", ")"), 0)
		);
	}

	@Test
	public void testChainAfterNonChainContinuationStepsInAgain() {
		// a non-chain continuation (`+ x`) between two chain links resets the chain state, so the second
		// link steps in from that line (2 -> 4) rather than aligning with the first link
		assertEquals(
				List.of("method(", "\t\tobj", "\t\t\t\t.a()", "\t\t+ x", "\t\t\t\t.b()", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "obj", ".a()", "+ x", ".b()", ")"), 0)
		);
	}

	@Test
	public void testChainedPutTwoLevels() {
		assertEquals(
				List.of("cache.put(\"k\", new JSONObject()", "\t\t.put(\"a\", 1)", "\t\t.put(\"b\", 2)", ")"),
				JavaSpanReindenter.reindent(List.of("cache.put(\"k\", new JSONObject()", ".put(\"a\", 1)", ".put(\"b\", 2)", ")"), 0)
		);
	}

	@Test
	public void testChainReceiverOnOwnContinuationLine() {
		// the receiver `new JSONObject()` is a continuation line (4 tabs), so the chain steps in from IT
		// (6 tabs), not from the enclosing `put(` opener (which the bracket-depth rule alone would put at 4)
		assertEquals(
				List.of("\t\tcache.put(", "\t\t\t\t\"k\",", "\t\t\t\tnew JSONObject()", "\t\t\t\t\t\t.put(\"a\", 1)", "\t\t\t\t\t\t.put(\"b\", 2)", "\t\t);"),
				JavaSpanReindenter.reindent(List.of("cache.put(", "\"k\",", "new JSONObject()", ".put(\"a\", 1)", ".put(\"b\", 2)", ");"), 2)
		);
	}

	@Test
	public void testCharLiteralBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tc = ')'", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "c = ')'", ")"), 0)
		);
	}

	@Test
	public void testConstructorWithMethodCallArgOracle() {
		assertEquals(
				List.of("\t\tmethod(new ArrayList<>(other(", "\t\t\t\targ", "\t\t)))"),
				JavaSpanReindenter.reindent(List.of("method(new ArrayList<>(other(", "arg", ")))"), 2)
		);
	}

	@Test
	public void testContentLineAtBaseAfterBalancedSpan() {
		assertEquals(
				List.of("List.of(", "\t\tx", ");", "y"),
				JavaSpanReindenter.reindent(List.of("List.of(", "x", ");", "y"), 0)
		);
	}

	@Test
	public void testDeepNestingNoStackOverflow() {
		final var input = new ArrayList<String>();
		for (var i = 0; i < 200; ++i)
			input.add("a(");
		input.add("x");
		for (var i = 0; i < 200; ++i)
			input.add(")");
		final var out = assertDoesNotThrow(() -> JavaSpanReindenter.reindent(input, 0));
		assertEquals(input.size(), out.size());
		// the single content line sits 200 opener levels deep, two tabs per level
		assertEquals("\t".repeat(400) + "x", out.get(200));
	}

	@Test
	public void testEmptyInteriorLineStaysEmpty() {
		assertEquals(
				List.of("method(", "", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "", "arg", ")"), 0)
		);
	}

	@Test
	public void testEmptyListReturnedAsIs() {
		assertEquals(List.of(), JavaSpanReindenter.reindent(List.of(), 0));
	}

	@Test
	public void testEscapedQuoteKeepsStringMasked() {
		assertEquals(
				List.of("method(", "\t\ts = \"\\\")\"", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "s = \"\\\")\"", ")"), 0)
		);
	}

	@Test
	public void testFirstLineMisindentedForcedToBase() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("    method(", "arg", ")"), 0)
		);
	}

	@Test
	public void testGenericAngleBracketsNotCounted() {
		assertEquals(
				List.of("method(", "\t\tMap<String, List<Integer>> m", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "Map<String, List<Integer>> m", ")"), 0)
		);
	}

	@Test
	public void testIdempotentOnCanonicalNestedPut() {
		final var canonical = List.of(
				"\t\tcache.put(\"View\", new JSONObject()",
				"\t\t\t\t.put(\"Account\", new JSONObject()",
				"\t\t\t\t\t\t.put(\"id\", 1)",
				"\t\t\t\t)",
				"\t\t);"
		);
		assertEquals(canonical, JavaSpanReindenter.reindent(canonical, 2));
	}

	@Test
	public void testIdempotentOnCanonicalParen() {
		final var canonical = List.of("method(", "\t\targ", ")");
		assertEquals(canonical, JavaSpanReindenter.reindent(canonical, 0));
	}

	@Test
	public void testLeadingSpacesNormalizedToTabs() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "    arg", ")"), 0)
		);
	}

	@Test
	public void testLineCommentBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tx() // )])}", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "x() // )])}", ")"), 0)
		);
	}

	@Test
	public void testMixedTabsSpacesNormalized() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "\t   arg", ")"), 0)
		);
	}

	@Test
	public void testMultiLineBlockCommentInteriorVerbatim() {
		assertEquals(
				List.of("method(", "\t\tx = a; /* start", "  comment ( with ) brackets", "  end */ z", ")"),
				JavaSpanReindenter.reindent(
						List.of("method(", "x = a; /* start", "  comment ( with ) brackets", "  end */ z", ")"), 0
				)
		);
	}

	@Test
	public void testMultipleOpensOneLevel() {
		assertEquals(
				List.of("method(new ArrayList<>(other(", "\t\targ", ")))"),
				JavaSpanReindenter.reindent(List.of("method(new ArrayList<>(other(", "arg", ")))"), 0)
		);
	}

	@Test
	public void testNestedChainedPutDepthFour() {
		assertEquals(
				List.of(
						"cache.put(\"View\", new JSONObject()",
						"\t\t.put(\"Account\", new JSONObject()",
						"\t\t\t\t.put(\"id\", 1)",
						"\t\t\t\t.put(\"name\", \"x\")",
						"\t\t)",
						")"
				),
				JavaSpanReindenter.reindent(
						List.of(
								"cache.put(\"View\", new JSONObject()",
								".put(\"Account\", new JSONObject()",
								".put(\"id\", 1)",
								".put(\"name\", \"x\")",
								")",
								")"
						),
						0
				)
		);
	}

	@Test
	public void testOverClosedClampsAtBase() {
		assertEquals(
				List.of("method(", ")", ")", ")"),
				JavaSpanReindenter.reindent(List.of("method(", ")", ")", ")"), 0)
		);
	}

	@Test
	public void testParenSingleLevelBase0() {
		assertEquals(
				List.of("method(", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "arg", ")"), 0)
		);
	}

	@Test
	public void testPerLineClosersProgressiveDedent() {
		assertEquals(
				List.of("a(", "\t\tb(", "\t\t\t\tc(", "\t\t\t\t\t\tx", "\t\t\t\t)", "\t\t)", ")"),
				JavaSpanReindenter.reindent(List.of("a(", "b(", "c(", "x", ")", ")", ")"), 0)
		);
	}

	@Test
	public void testSingleLineSpanReturnedAsIs() {
		assertEquals(List.of("method(x);"), JavaSpanReindenter.reindent(List.of("method(x);"), 0));
	}

	@Test
	public void testStackedClosersSingleDedent() {
		assertEquals(
				List.of("a(b(c(", "\t\tx", ")))"),
				JavaSpanReindenter.reindent(List.of("a(b(c(", "x", ")))"), 0)
		);
	}

	@Test
	public void testStringUnbalancedBracketMasked() {
		assertEquals(
				List.of("method(", "\t\tfoo(\"a)b\")", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "foo(\"a)b\")", ")"), 0)
		);
	}

	@Test
	public void testTextBlockCloseTrailingClosersSplitToOwnLine() {
		// a value lambda's closing `});` shares the text-block delimiter line; the delimiter (with the
		// statement terminator) stays verbatim and the closer run drops to its own line at the call depth
		assertEquals(
				List.of("m(() -> {", "\treturn \"\"\"", "\t\t\ttext\"\"\";", "});"),
				JavaSpanReindenter.reindent(List.of("m(() -> {", "return \"\"\"", "\t\t\ttext\"\"\"; });"), 0)
		);
	}

	@Test
	public void testTextBlockInteriorVerbatim() {
		assertEquals(
				List.of("\t\tmethod(\"\"\"", "  raw ( content", "  more }", "  \"\"\")"),
				JavaSpanReindenter.reindent(List.of("method(\"\"\"", "  raw ( content", "  more }", "  \"\"\")"), 2)
		);
	}

	@Test
	public void testTrailingCloserSplitRejectsCodeAfterLiteralClose() {
		// real code after the text-block close (not a pure closer run) is left inline, never split off
		assertEquals(
				List.of("m(() -> {", "\treturn \"\"\"", "\t\t\ttext\"\"\"; foo();"),
				JavaSpanReindenter.reindent(List.of("m(() -> {", "return \"\"\"", "\t\t\ttext\"\"\"; foo();"), 0)
		);
	}

	@Test
	public void testUnterminatedBlockCommentNoCrash() {
		final var input = List.of("method(", "x = a; /* never closes", "  still comment ) (", "  and more");
		final var out = assertDoesNotThrow(() -> JavaSpanReindenter.reindent(input, 0));
		assertEquals(
				List.of("method(", "\t\tx = a; /* never closes", "  still comment ) (", "  and more"),
				out
		);
	}

	@Test
	public void testWhitespaceOnlyInteriorLineBecomesEmpty() {
		assertEquals(
				List.of("method(", "", "\t\targ", ")"),
				JavaSpanReindenter.reindent(List.of("method(", "   ", "arg", ")"), 0)
		);
	}
}