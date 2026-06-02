package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class RedundantCastFixerTest {
	private static final String TOPIC = "redundantcast";

	@Nonnull
	private static String fixOneLineSnippet(@Nonnull CheckstyleFixer fixer, @Nonnull String snippetName) throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, snippetName);
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, t.line(), t.column()));
		return result.replacement().getFirst();
	}

	private final CheckstyleFixer fixer = new RedundantCastFixer();

	@Test
	public void testAfterOuterWalksPastEol_bareWrapStrips() throws Exception {
		assertEquals("return s", fixOneLineSnippet(fixer, "afterOuter_walks_past_eol"));
	}

	@Test
	public void testArrayAccessBeforeOuterParen_doesNotStrip() throws Exception {
		assertEquals("arr[0](s).length();", fixOneLineSnippet(fixer, "array_access_before_outer_paren"));
	}

	@Test
	public void testBlockCommentUnterminated_fallsToPlainStrip() throws Exception {
		assertEquals("return (s /* unterminated", fixOneLineSnippet(fixer, "block_comment_unterminated"));
	}

	@Test
	public void testChainedCallBeforeOuterParen_doesNotStrip() throws Exception {
		assertEquals("f.g()(s).length();", fixOneLineSnippet(fixer, "chained_call_before_outer_paren"));
	}

	@Test
	public void testCharLiteralUnterminated_fallsToPlainStrip() throws Exception {
		assertEquals("return ('a", fixOneLineSnippet(fixer, "char_literal_unterminated"));
	}

	@Test
	public void testColumnNotOpenParen() throws Exception {
		assertSkip(fixer, TOPIC, "column_not_open_paren");
	}

	@Test
	public void testLineCommentInReceiverWrap_fallsToPlainStrip() throws Exception {
		assertEquals("return (s //x).length();", fixOneLineSnippet(fixer, "line_comment_in_receiver_wrap"));
	}

	@Test
	public void testMalformedNoExpression() throws Exception {
		assertSkipResult(fixer, TOPIC, "malformed_no_expression", "malformed-cast-no-expression");
	}

	@Test
	public void testMultiLineBlockCommentPrior_doesNotStrip() throws Exception {
		assertEquals("(s).length();", fixOneLineSnippet(fixer, "multi_line_block_comment_prior_doesNotStrip"));
	}

	@Test
	public void testMultiLineCast() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_cast", "multi-line-cast");
	}

	@Test
	public void testOuterOpenButNoOuterCloseSameLine_fallsToPlainStrip() throws Exception {
		assertEquals("takesString(s,", fixOneLineSnippet(fixer, "outer_open_no_close_same_line"));
	}

	@Test
	public void testPathA_priorLineRbracket_doesNotStrip() throws Exception {
		assertEquals("(s).length();", fixOneLineSnippet(fixer, "path_a_prior_line_rbracket_rejected"));
	}

	@Test
	public void testPathA_priorLineRparen_doesNotStrip() throws Exception {
		assertEquals("(s).length();", fixOneLineSnippet(fixer, "path_a_prior_line_rparen_rejected"));
	}

	@Test
	public void testStringLiteralUnterminated_fallsToPlainStrip() throws Exception {
		assertEquals("return (\"unterminated", fixOneLineSnippet(fixer, "string_literal_unterminated"));
	}

	@Test
	public void testSupplementaryIdentifierBeforeOuterParen_doesNotStrip() {
		final var lines = new ArrayList<>(List.of("Bold𝐀((String) s).y"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals("Bold𝐀(s).y", result.replacement().getFirst());
	}

	@Test
	public void testTextBlockBodyWithEscape_doesNotStrip() throws Exception {
		assertEquals("(s).length();", fixOneLineSnippet(fixer, "text_block_body_with_escape_doesNotStrip"));
	}

	@Test
	public void testTextBlockClosesAtLineEnd_strips() throws Exception {
		assertEquals("return s.length();", fixOneLineSnippet(fixer, "text_block_closes_at_line_end_strips"));
	}

	@Test
	public void testTextBlockCloseThenOuterOpenNoClose_plainStrips() throws Exception {
		assertEquals("\"\"\"; foo(s,", fixOneLineSnippet(fixer, "text_block_close_then_outer_open_no_close_plain_strips"));
	}

	@Test
	public void testTextBlockUnterminated_fallsToPlainStrip() throws Exception {
		assertEquals("return (\"\"\"abc", fixOneLineSnippet(fixer, "text_block_unterminated"));
	}
}