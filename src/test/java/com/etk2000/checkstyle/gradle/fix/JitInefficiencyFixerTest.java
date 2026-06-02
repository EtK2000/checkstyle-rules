package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class JitInefficiencyFixerTest {
	private static final String TOPIC = "jitinefficiency";

	private final CheckstyleFixer fixer = new JitInefficiencyFixer();

	@Test
	public void appendConcatInsideBlockCommentBails() throws Exception {
		assertSkip(fixer, TOPIC, "append_concat_inside_block_comment_bails");
	}

	@Test
	public void appendConcatUnclosedParen() throws Exception {
		assertSkip(fixer, TOPIC, "append_concat_unclosed_paren");
	}

	@Test
	public void boxedConstructorRefusesUnclosedParen() throws Exception {
		assertSkip(fixer, TOPIC, "boxed_constructor_refuses_unclosed_paren");
	}

	@Test
	public void emptyStringConcatLineCommentBeforeEndBails() throws Exception {
		assertSkip(fixer, TOPIC, "empty_string_concat_line_comment_before_end_bails");
	}

	@Test
	public void emptyStringConcatUnterminatedBlockCommentBails() throws Exception {
		assertSkip(fixer, TOPIC, "empty_string_concat_unterminated_block_comment_bails");
	}

	@Test
	public void newStringRefusesUnclosedParen() throws Exception {
		assertSkip(fixer, TOPIC, "new_string_refuses_unclosed_paren");
	}

	@Test
	public void stringConcatArrayLhsClassicForUnparseableHeaderBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_array_lhs_classic_for_unparseable_header_bails");
	}

	@Test
	public void stringConcatGapCrossScopeCleanBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_gap_cross_scope_clean_bails");
	}

	@Test
	public void stringConcatGapNestedScopeBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_gap_nested_scope_bails");
	}

	@Test
	public void stringConcatGapTextBlockBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_gap_text_block_bails");
	}

	@Test
	public void stringConcatInLoopInsideBlockCommentBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_in_loop_inside_block_comment_bails");
	}

	@Test
	public void stringConcatLhsMalformedBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_lhs_malformed_bails");
	}

	@Test
	public void stringConcatTier2DoWhileLastLineBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_tier2_do_while_last_line_bails");
	}

	@Test
	public void stringConcatTier2DoWhileMissingSemicolonBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_tier2_do_while_missing_semicolon_bails");
	}

	@Test
	public void stringConcatTier2DoWhileNestedScopeBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_tier2_do_while_nested_scope_bails");
	}

	@Test
	public void stringConcatTier2DoWhileNoMatchingWhileBails() throws Exception {
		assertSkip(fixer, TOPIC, "string_concat_tier2_do_while_no_matching_while_bails");
	}

	@CsvSource(delimiter = '|', value = {
			"a.b = c;|a.b|false|true",
			"a.b += c;|a.b|false|true",
			"a.b -= c;|a.b|false|true",
			"a.b *= c;|a.b|false|true",
			"a.b /= c;|a.b|false|true",
			"a.b %= c;|a.b|false|true",
			"a.b &= c;|a.b|false|true",
			"'a.b |= c;'|a.b|false|true",
			"a.b ^= c;|a.b|false|true",
			"x.a.b = c;|a.b|false|false",
			"a.b == c;|a.b|false|false",
			"a.b >= c;|a.b|false|false",
			"a.b <= c;|a.b|false|false",
			"a.b != c;|a.b|false|false",
			"a.b >>= c;|a.b|false|false",
			"a.bc = d;|a.b|false|false",
			"1a.b = c;|a.b|false|false",
			"a.b|a.b|false|false",
			"a.b = c;|a.b|true|false",
			"*/ a.b = c;|a.b|true|true"
	})
	@ParameterizedTest
	public void testContainsChainAssignment(String line, String chain, boolean inBlockComment, boolean expected) {
		final var entryState = new LexerState(inBlockComment, false);
		assertEquals(expected, JitInefficiencyFixer.containsChainAssignment(line, chain, entryState));
	}

	@CsvSource(delimiter = '|', value = {
			"k = c;|k|false|true",
			"k += c;|k|false|true",
			"k -= c;|k|false|true",
			"k *= c;|k|false|true",
			"k /= c;|k|false|true",
			"k %= c;|k|false|true",
			"k &= c;|k|false|true",
			"'k |= c;'|k|false|true",
			"k ^= c;|k|false|true",
			"k <<= c;|k|false|true",
			"k >>= c;|k|false|true",
			"k >>>= c;|k|false|true",
			"++k;|k|false|true",
			"--k;|k|false|true",
			"k++;|k|false|true",
			"k--;|k|false|true",
			"k == c;|k|false|false",
			"k >= c;|k|false|false",
			"k <= c;|k|false|false",
			"k != c;|k|false|false",
			"k << c;|k|false|false",
			"obj.k = c;|k|false|false",
			"kc = d;|k|false|false",
			"++kc;|k|false|false",
			"k|k|false|false",
			"k = c;|k|true|false",
			"*/ k = c;|k|true|true"
	})
	@ParameterizedTest
	public void testMutatesIdentifier(String line, String name, boolean inBlockComment, boolean expected) {
		final var entryState = new LexerState(inBlockComment, false);
		assertEquals(expected, JitInefficiencyFixer.mutatesIdentifier(line, name, entryState));
	}
}