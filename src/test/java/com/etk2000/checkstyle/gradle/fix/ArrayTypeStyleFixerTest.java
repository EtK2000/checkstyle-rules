package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class ArrayTypeStyleFixerTest {
	private static final String SKIP_ANNOTATION = "type-use annotation before the array brackets";
	private static final String SKIP_MULTI_VAR = "cannot move brackets in a multi-variable declaration";
	private static final String SKIP_TEXT_BLOCK = "possible text block prevents scanning the declaration";
	private static final String TOPIC = "arraytypestyle";

	private final CheckstyleFixer fixer = new ArrayTypeStyleFixer();

	@Test
	public void testBlockCommentAfterMethodReturnBracketsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "block_comment_after_method_return_brackets_returns_null");
	}

	@Test
	public void testBracketAtLineStartReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "bracket_at_line_start_returns_null");
	}

	@Test
	public void testCatchKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "catch_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testDoKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "do_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testEscapedBackslashAtEolInString() throws Exception {
		// can't migrate: snippet uses a multi-line string literal with a line-continuation backslash before EOL, which is not parseable Java when class-wrapped (Java doesn't have line-continuation strings)
		assertSimpleFix(fixer, TOPIC, "escaped_backslash_at_eol_in_string");
	}

	@Test
	public void testExpressionContextNotMethodReturnReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "expression_context_not_method_return_returns_null");
	}

	@Test
	public void testFakeIdentAfterMethodReturnBracketsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fake_ident_after_method_return_brackets_returns_null");
	}

	@Test
	public void testFieldAccessNotMethodReturnReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "field_access_not_method_return_returns_null");
	}

	@Test
	public void testFieldInitCallEndsInParenMultiVarReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "field_init_call_ends_in_paren_multi_var_returns_null", SKIP_MULTI_VAR);
	}

	@Test
	public void testGenericRecordMultiComponent() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSimpleFix(fixer, TOPIC, "generic_record_multi_component");
	}

	@Test
	public void testGenericRecordMultiTypeParam() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSimpleFix(fixer, TOPIC, "generic_record_multi_type_param");
	}

	@Test
	public void testGenericRecordNestedTypeBounds() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSimpleFix(fixer, TOPIC, "generic_record_nested_type_bounds");
	}

	@Test
	public void testIfKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "if_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testMethodIdentAtLineStartReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_ident_at_line_start_returns_null");
	}

	@Test
	public void testMethodReturnFollowedByPartialThrowsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_return_followed_by_partial_throws_returns_null");
	}

	@Test
	public void testMethodReturnFollowedByThrowsLikeIdentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_return_followed_by_throws_like_ident_returns_null");
	}

	@Test
	public void testMethodReturnWithTypeUseAnnotationReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "method_return_with_type_use_annotation_returns_null", SKIP_ANNOTATION);
	}

	@Test
	public void testMultiLineBracketsUnclosedReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_brackets_unclosed_returns_null");
	}

	@Test
	public void testMultiLineDeclEndingAtClosingBraceTreatedAsTerminator() throws Exception {
		// can't migrate: the declaration is terminated by a bare '}' with no ';', so class-wrapping yields a field declaration missing its semicolon (unparseable)
		assertSimpleFix(fixer, TOPIC, "multi_line_decl_ending_at_closing_brace_treated_as_terminator");
	}

	@Test
	public void testMultiLineEofWithoutTerminator() throws Exception {
		// can't migrate: the declaration has no terminator (ends at EOF after '[]'), so class-wrapping yields a field declaration missing its semicolon (unparseable)
		assertSimpleFix(fixer, TOPIC, "multi_line_eof_without_terminator");
	}

	@Test
	public void testMultiLineFirstLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_first_line_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInBraceReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_brace_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInPermitsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_permits_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInSemicolonReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_semicolon_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInThrowsIdent() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_throws_ident");
	}

	@Test
	public void testMultiLineSuperBlacklistReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_super_blacklist_returns_null");
	}

	@Test
	public void testMultiLineTextBlockOnBracketLineReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_text_block_on_bracket_line_returns_null", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testMultiLineTextBlockOnLaterLineReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_text_block_on_later_line_returns_null", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testMultiLineUnterminatedStringDoesNotEatCommaOnNextLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_unterminated_string_does_not_eat_comma_on_next_line", SKIP_MULTI_VAR);
	}

	@Test
	public void testMultiLineWithEmptyPreviousLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_with_empty_previous_line_returns_null");
	}

	@Test
	public void testMultiLineWithWhitespaceOnlyPrevLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_with_whitespace_only_prev_line_returns_null");
	}

	@Test
	public void testNoSuffixReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "no_suffix_returns_null");
	}

	@Test
	public void testOpenParenAtLineStartTreatedAsParens() throws Exception {
		// can't migrate: snippet `(int x[])` is not parseable Java when class-wrapped
		assertSimpleFix(fixer, TOPIC, "open_paren_at_line_start_treated_as_parens");
	}

	@Test
	public void testOrphanCloseParenIgnored() throws Exception {
		// can't migrate: snippet `) int x[];` starts with an orphan close paren and is not parseable Java when class-wrapped
		assertSimpleFix(fixer, TOPIC, "orphan_close_paren_ignored");
	}

	@Test
	public void testOrphanCloseParenWithoutMatchReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "orphan_close_paren_without_match_returns_null");
	}

	@Test
	public void testPermitsKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "permits_keyword_single_line_returns_null");
	}

	@Test
	public void testSuperKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "super_keyword_single_line_returns_null");
	}

	@Test
	public void testSwitchKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "switch_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testSynchronizedKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "synchronized_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testTextBlockAfterBracketSameLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "text_block_after_bracket_same_line", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testTextBlockOpenerAfterBrackets() throws Exception {
		assertSkipResult(fixer, TOPIC, "text_block_opener_after_brackets", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testThrowsKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "throws_keyword_single_line_returns_null");
	}

	@Test
	public void testTryKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "try_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testTypeUseAnnotationInParameterReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "type_use_annotation_in_parameter_returns_null", SKIP_ANNOTATION);
	}

	@Test
	public void testTypeUseAnnotationInRecordReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "type_use_annotation_in_record_returns_null", SKIP_ANNOTATION);
	}

	@Test
	public void testUnclosedBracketReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "unclosed_bracket_returns_null");
	}

	@Test
	public void testWhileKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "while_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testWithInvalidSuffixReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "with_invalid_suffix_returns_null");
	}
}