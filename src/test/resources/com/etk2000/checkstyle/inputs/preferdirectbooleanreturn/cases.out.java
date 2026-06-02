package com.etk2000.checkstyle.inputs.preferdirectbooleanreturn;

// === case: atomic_dollar_sign ===
class InputPreferDirectBooleanReturnAtomicDollarSignSliceViolation {
	boolean m(boolean foo$bar) {
		return !foo$bar;
	}
}
// === end ===

// === case: atomic_method_call_negated ===
class InputPreferDirectBooleanReturnAtomicMethodCallNegatedSliceViolation {
	boolean m(String s) {
		return !s.isEmpty();
	}
}
// === end ===

// === case: atomic_supplementary_codepoint ===
class InputPreferDirectBooleanReturnAtomicSupplementaryCodepointSliceViolation {
	boolean m(boolean 𝐀) {
		return !𝐀;
	}
}
// === end ===

// === case: atomic_whitespace ===
class InputPreferDirectBooleanReturnAtomicWhitespaceSliceViolation {
	boolean m(String s) {
		return !(s . isEmpty());
	}
}
// === end ===

// === case: blank_line_between_body_and_trailing_skipped ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnBlankLineBetweenBodyAndTrailingSkippedSliceViolation {
	boolean m(boolean flag) {
		if (flag) return true;

		return false;
	}
}
// === end ===

// === case: block_comment_with_paren_in_cond ===
class InputPreferDirectBooleanReturnBlockCommentWithParenInCondSliceViolation {
	boolean m(boolean flag) {
		return /* ) */ flag;
	}
}
// === end ===

// === case: block_comment_with_star_mid_body ===
class InputPreferDirectBooleanReturnBlockCommentWithStarMidBodySliceViolation {
	boolean m(boolean flag) {
		return /* a * b ) */ flag;
	}
}
// === end ===

// === case: braced_body_close_brace_indent_mismatch ===
// skip-reason: if body is not a simple collapsible return
class InputPreferDirectBooleanReturnBracedBodyCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		if (flag) {
			return true;
	}
		return false;
	}
}
// === end ===

// === case: braced_else_close_brace_indent_mismatch ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnBracedElseCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		if (flag) {
			return true;
		}
		else {
			return false;
	}
	}
}
// === end ===

// === case: braced_if_body_trailing_return ===
class InputPreferDirectBooleanReturnBracedIfBodyTrailingReturnSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: braced_if_body_trailing_return_negated ===
class InputPreferDirectBooleanReturnBracedIfBodyTrailingReturnNegatedSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: braced_if_else ===
class InputPreferDirectBooleanReturnBracedIfElseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: braced_then_unbraced_else ===
class InputPreferDirectBooleanReturnBracedThenUnbracedElseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: chain_inner_fires ===
class InputPreferDirectBooleanReturnChainInnerFiresSliceViolation {
	boolean m(int x, int y) {
		if (x > 0)
			++x;
		return y > 0;
	}
}
// === end ===

// === case: char_literal_escape ===
class InputPreferDirectBooleanReturnCharLiteralEscapeSliceViolation {
	boolean m(char c) {
		return c == '\'';
	}
}
// === end ===

// === case: char_literal_with_paren ===
class InputPreferDirectBooleanReturnCharLiteralWithParenSliceViolation {
	boolean m(char c) {
		return c == ')';
	}
}
// === end ===

// === case: combine_and ===
class InputPreferDirectBooleanReturnCombineAndSliceViolation {
	boolean m(boolean a, String s) {
		return a && s.isEmpty();
	}
}
// === end ===

// === case: combine_and_assign_wraps ===
class InputPreferDirectBooleanReturnCombineAndAssignWrapsSliceViolation {
	boolean m(boolean x, String s) {
		return (x = s.isEmpty()) && s.isBlank();
	}
}
// === end ===

// === case: combine_and_comment ===
class InputPreferDirectBooleanReturnCombineAndCommentSliceViolation {
	boolean m(boolean a, boolean b, String s) {
		return a && b && s.isEmpty(); // note
	}
}
// === end ===

// === case: combine_and_multiline_cond ===
class InputPreferDirectBooleanReturnCombineAndMultilineCondSliceViolation {
	boolean m(boolean a, boolean b, String s) {
		return a && b && s.isEmpty();
	}
}
// === end ===

// === case: combine_and_ternary_wraps ===
class InputPreferDirectBooleanReturnCombineAndTernaryWrapsSliceViolation {
	boolean m(boolean a, boolean b, boolean c, boolean d) {
		return a && (b ? c : d);
	}
}
// === end ===

// === case: combine_or_negated ===
class InputPreferDirectBooleanReturnCombineOrNegatedSliceViolation {
	boolean m(boolean a, String s) {
		return !a || s.isEmpty();
	}
}
// === end ===

// === case: comment_between_braces_skipped ===
// skip-reason: comment between condition and body
class InputPreferDirectBooleanReturnCommentBetweenBracesSkippedSliceViolation {
	boolean m(boolean flag) {
		if (flag) // comment
			return true;
		return false;
	}
}
// === end ===

// === case: comparison_negated ===
class InputPreferDirectBooleanReturnComparisonNegatedSliceViolation {
	boolean m(int x) {
		return !(x > 0);
	}
}
// === end ===

// === case: comparison_negated_adds_parens ===
class InputPreferDirectBooleanReturnComparisonNegatedAddsParensSliceViolation {
	boolean m(int x) {
		return !(x > 0);
	}
}
// === end ===

// === case: cond_in_quotes_ignored ===
class InputPreferDirectBooleanReturnCondInQuotesIgnoredSliceViolation {
	boolean m(String s) {
		return s.equals(")");
	}
}
// === end ===

// === case: cond_with_string_escape ===
class InputPreferDirectBooleanReturnCondWithStringEscapeSliceViolation {
	boolean m(String s) {
		return s.equals("\"");
	}
}
// === end ===

// === case: cond_with_string_literal_is_not_atomic ===
class InputPreferDirectBooleanReturnCondWithStringLiteralIsNotAtomicSliceViolation {
	boolean m(String s) {
		return !("x".equals(s));
	}
}
// === end ===

// === case: double_negation_simplifies ===
class InputPreferDirectBooleanReturnDoubleNegationSimplifiesSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: double_negative_cond ===
class InputPreferDirectBooleanReturnDoubleNegativeCondSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: forward_inline_false_true ===
class InputPreferDirectBooleanReturnForwardInlineFalseTrueSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: forward_inline_true_false ===
class InputPreferDirectBooleanReturnForwardInlineTrueFalseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: indentation_preserved ===
class InputPreferDirectBooleanReturnIndentationPreservedSliceViolation {
	class Inner1 {
		class Inner2 {
			boolean m(boolean flag) {
				return flag;
			}
		}
	}
}
// === end ===

// === case: inline_then_braced_else ===
class InputPreferDirectBooleanReturnInlineThenBracedElseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: land_condition_forward ===
class InputPreferDirectBooleanReturnLandConditionForwardSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: land_condition_negated_adds_parens ===
class InputPreferDirectBooleanReturnLandConditionNegatedAddsParensSliceViolation {
	boolean m(boolean a, boolean b) {
		return !(a && b);
	}
}
// === end ===

// === case: land_condition_not_negated_no_parens ===
class InputPreferDirectBooleanReturnLandConditionNotNegatedNoParensSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: method_call_cond ===
class InputPreferDirectBooleanReturnMethodCallCondSliceViolation {
	boolean m(String s) {
		return s.isEmpty();
	}
}
// === end ===

// === case: mixed_3c_basic ===
class InputPreferDirectBooleanReturnMixed3cBasicSliceViolation {
	boolean m(boolean flag, String s) {
		return flag || s.isEmpty();
	}
}
// === end ===

// === case: mixed_3c_else_form ===
class InputPreferDirectBooleanReturnMixed3cElseFormSliceViolation {
	boolean m(boolean flag, String s) {
		return flag || s.isEmpty();
	}
}
// === end ===

// === case: mixed_3c_ternary_wraps ===
class InputPreferDirectBooleanReturnMixed3cTernaryWrapsSliceViolation {
	boolean m(boolean a, boolean b, boolean c, boolean d) {
		return a || (b ? c : d);
	}
}
// === end ===

// === case: mixed_3d_basic ===
class InputPreferDirectBooleanReturnMixed3dBasicSliceViolation {
	boolean m(boolean flag, String s) {
		return !flag && s.isEmpty();
	}
}
// === end ===

// === case: multiline_cond_atomic_forward ===
class InputPreferDirectBooleanReturnMultilineCondAtomicForwardSliceViolation {
	boolean m(String s) {
		return s.isEmpty();
	}
}
// === end ===

// === case: multiline_cond_atomic_negated ===
class InputPreferDirectBooleanReturnMultilineCondAtomicNegatedSliceViolation {
	boolean m(String s) {
		return !s.isEmpty();
	}
}
// === end ===

// === case: multiline_cond_block_comment_inline ===
class InputPreferDirectBooleanReturnMultilineCondBlockCommentInlineSliceViolation {
	boolean m(boolean a, boolean b) {
		return a /* note */ && b;
	}
}
// === end ===

// === case: multiline_cond_block_comment_spans ===
// skip-reason: multi-line if condition
class InputPreferDirectBooleanReturnMultilineCondBlockCommentSpansSliceViolation {
	boolean m(boolean a, boolean b) {
		if (a
				/* spanning
				block */ && b) return true;
		return false;
	}
}
// === end ===

// === case: multiline_cond_braced_body ===
class InputPreferDirectBooleanReturnMultilineCondBracedBodySliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_braced_else ===
class InputPreferDirectBooleanReturnMultilineCondBracedElseSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_char_paren ===
class InputPreferDirectBooleanReturnMultilineCondCharParenSliceViolation {
	boolean m(char c, boolean flag) {
		return c == ')' && flag;
	}
}
// === end ===

// === case: multiline_cond_comment_only_line ===
class InputPreferDirectBooleanReturnMultilineCondCommentOnlyLineSliceViolation {
	boolean m(boolean flag) {
		return flag; // comment
	}
}
// === end ===

// === case: multiline_cond_deep_indent ===
class InputPreferDirectBooleanReturnMultilineCondDeepIndentSliceViolation {
	class Inner1 {
		class Inner2 {
			boolean m(boolean a, boolean b) {
				return a && b;
			}
		}
	}
}
// === end ===

// === case: multiline_cond_empty_comment ===
class InputPreferDirectBooleanReturnMultilineCondEmptyCommentSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: multiline_cond_inline_body ===
class InputPreferDirectBooleanReturnMultilineCondInlineBodySliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_inline_body_else_unbraced ===
class InputPreferDirectBooleanReturnMultilineCondInlineBodyElseUnbracedSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_line_comment_middle ===
class InputPreferDirectBooleanReturnMultilineCondLineCommentMiddleSliceViolation {
	boolean m(boolean a, boolean b, boolean c) {
		return a && b && c; // middle
	}
}
// === end ===

// === case: multiline_cond_line_comment_negated ===
class InputPreferDirectBooleanReturnMultilineCondLineCommentNegatedSliceViolation {
	boolean m(boolean a, boolean b) {
		return !(a && b); // note
	}
}
// === end ===

// === case: multiline_cond_line_comment_nonatomic ===
class InputPreferDirectBooleanReturnMultilineCondLineCommentNonatomicSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b; // note
	}
}
// === end ===

// === case: multiline_cond_line_comment_relocated ===
class InputPreferDirectBooleanReturnMultilineCondLineCommentRelocatedSliceViolation {
	boolean m(boolean flag) {
		return flag; // comment
	}
}
// === end ===

// === case: multiline_cond_multi_comments ===
class InputPreferDirectBooleanReturnMultilineCondMultiCommentsSliceViolation {
	boolean m(boolean a, boolean b, boolean c) {
		return a && b && c; // c1 c2
	}
}
// === end ===

// === case: multiline_cond_negated_nonatomic ===
class InputPreferDirectBooleanReturnMultilineCondNegatedNonatomicSliceViolation {
	boolean m(boolean a, boolean b) {
		return !(a && b);
	}
}
// === end ===

// === case: multiline_cond_nested_parens_spanning ===
class InputPreferDirectBooleanReturnMultilineCondNestedParensSpanningSliceViolation {
	boolean m(boolean a, boolean b, boolean flag) {
		return (a || b) && flag;
	}
}
// === end ===

// === case: multiline_cond_next_line_body ===
class InputPreferDirectBooleanReturnMultilineCondNextLineBodySliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_next_line_else_unbraced ===
class InputPreferDirectBooleanReturnMultilineCondNextLineElseUnbracedSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b;
	}
}
// === end ===

// === case: multiline_cond_pattern_instanceof ===
class InputPreferDirectBooleanReturnMultilineCondPatternInstanceofSliceViolation {
	boolean m(Object obj) {
		return obj instanceof String s && !s.isEmpty();
	}
}
// === end ===

// === case: multiline_cond_string_paren ===
class InputPreferDirectBooleanReturnMultilineCondStringParenSliceViolation {
	boolean m(String s, boolean flag) {
		return s.equals("a)b") && flag;
	}
}
// === end ===

// === case: multiline_cond_string_slashes ===
class InputPreferDirectBooleanReturnMultilineCondStringSlashesSliceViolation {
	boolean m(String s, boolean flag) {
		return s.equals("//x") && flag;
	}
}
// === end ===

// === case: multiline_cond_text_block ===
// skip-reason: multi-line if condition
class InputPreferDirectBooleanReturnMultilineCondTextBlockSliceViolation {
	boolean m(boolean flag) {
		if (flag
				|| """
				text
				""".isEmpty()) return true;
		return false;
	}
}
// === end ===

// === case: multiline_cond_three_line ===
class InputPreferDirectBooleanReturnMultilineCondThreeLineSliceViolation {
	boolean m(boolean a, boolean b, boolean c) {
		return a && b && c;
	}
}
// === end ===

// === case: nested_parens_in_condition ===
class InputPreferDirectBooleanReturnNestedParensInConditionSliceViolation {
	boolean m(boolean a, boolean b, boolean c) {
		return (a || b) && c;
	}
}
// === end ===

// === case: nested_parens_in_condition_negated ===
class InputPreferDirectBooleanReturnNestedParensInConditionNegatedSliceViolation {
	boolean m(boolean a, boolean b, boolean c) {
		return !((a || b) && c);
	}
}
// === end ===

// === case: next_line_body_trailing_return ===
class InputPreferDirectBooleanReturnNextLineSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: next_line_body_trailing_return_negated ===
class InputPreferDirectBooleanReturnNextLineBodyTrailingReturnNegatedSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: next_line_then_braced_else ===
class InputPreferDirectBooleanReturnNextLineThenBracedElseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: not_ident_forward ===
class InputPreferDirectBooleanReturnNotIdentForwardSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: not_ident_in_precedence ===
class InputPreferDirectBooleanReturnNotIdentInPrecedenceSliceViolation {
	boolean m(boolean a, boolean b) {
		return !(!a && b);
	}
}
// === end ===

// === case: not_ident_not_negated_kept_as_is ===
class InputPreferDirectBooleanReturnNotIdentNotNegatedKeptAsIsSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: not_parenthesized_simplifies ===
class InputPreferDirectBooleanReturnNotParenthesizedSimplifiesSliceViolation {
	boolean m(boolean flag) {
		return (flag);
	}
}
// === end ===

// === case: opposite_paren_literal ===
class InputPreferDirectBooleanReturnOppositeParenLiteralSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: same_literal_both ===
class InputPreferDirectBooleanReturnSameLiteralBothSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: same_literal_chained_comparison_skipped ===
// skip-reason: ambiguous comparison operator
class InputPreferDirectBooleanReturnSameLiteralChainedComparisonSkippedSliceViolation {
	boolean m(int index, int size, String s) {
		if (index < size == s.isEmpty())
			return true;
		return true;
	}
}
// === end ===

// === case: same_literal_drop_array_index ===
class InputPreferDirectBooleanReturnSameLiteralDropArrayIndexSliceViolation {
	boolean m(boolean[] flags, int i) {
		return true;
	}
}
// === end ===

// === case: same_literal_drop_cast ===
class InputPreferDirectBooleanReturnSameLiteralDropCastSliceViolation {
	boolean m(Object obj) {
		return true;
	}
}
// === end ===

// === case: same_literal_drop_division ===
class InputPreferDirectBooleanReturnSameLiteralDropDivisionSliceViolation {
	boolean m(int a, int b) {
		return true;
	}
}
// === end ===

// === case: same_literal_else_form ===
class InputPreferDirectBooleanReturnSameLiteralElseFormSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: same_literal_extract_and_both ===
class InputPreferDirectBooleanReturnSameLiteralExtractAndBothSliceViolation {
	boolean m(String s) {
		if (s.isEmpty())
			s.isBlank();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_and_instanceof ===
class InputPreferDirectBooleanReturnSameLiteralExtractAndInstanceofSliceViolation {
	private Object box() {
		return null;
	}

	boolean m() {
		if (box() instanceof String)
			sideEffect();
		return true;
	}

	private boolean sideEffect() {
		return true;
	}
}
// === end ===

// === case: same_literal_extract_and_left ===
class InputPreferDirectBooleanReturnSameLiteralExtractAndLeftSliceViolation {
	boolean m(boolean a, String s) {
		s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_and_paren_right ===
class InputPreferDirectBooleanReturnSameLiteralExtractAndParenRightSliceViolation {
	boolean m(boolean flag, String s) {
		if (s.isEmpty())
			flag = s.isBlank();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractAssignSliceViolation {
	boolean m(boolean flag, boolean other) {
		flag = other;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_assign_comparison ===
class InputPreferDirectBooleanReturnSameLiteralExtractAssignComparisonSliceViolation {
	boolean m(boolean found, int a, int b) {
		found = a > b;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_assign_paren_comparison ===
class InputPreferDirectBooleanReturnSameLiteralExtractAssignParenComparisonSliceViolation {
	boolean m(int len, String s) {
		len = s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_assign_postfix_rhs ===
class InputPreferDirectBooleanReturnSameLiteralExtractAssignPostfixRhsSliceViolation {
	boolean m(int i, int x) {
		x = i++;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_band_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractBandAssignSliceViolation {
	boolean m(boolean flag, boolean other) {
		flag &= other;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_bor_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractBorAssignSliceViolation {
	boolean m(boolean flag, boolean other) {
		flag |= other;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_bsr_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractBsrAssignSliceViolation {
	boolean m(int x) {
		x >>>= 1;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_bxor_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractBxorAssignSliceViolation {
	boolean m(boolean flag, boolean other) {
		flag ^= other;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_cast_call ===
class InputPreferDirectBooleanReturnSameLiteralExtractCastCallSliceViolation {
	private Object box() {
		return null;
	}

	boolean m() {
		box();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_cast_instanceof_prefix ===
class InputPreferDirectBooleanReturnSameLiteralExtractCastInstanceofPrefixSliceViolation {
	private Object instanceofCheck() {
		return null;
	}

	boolean m() {
		instanceofCheck();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_cast_nested ===
class InputPreferDirectBooleanReturnSameLiteralExtractCastNestedSliceViolation {
	private Object box() {
		return null;
	}

	boolean m() {
		box();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_cast_paren ===
class InputPreferDirectBooleanReturnSameLiteralExtractCastParenSliceViolation {
	private Object box() {
		return null;
	}

	boolean m() {
		box();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_div_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractDivAssignSliceViolation {
	boolean m(int x) {
		x /= 2;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_ge ===
class InputPreferDirectBooleanReturnSameLiteralExtractGeSliceViolation {
	boolean m(String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_generic_call ===
// imports: java.util.HashSet
class InputPreferDirectBooleanReturnSameLiteralExtractGenericCallSliceViolation {
	boolean m() {
		new HashSet<String>().add("x");
		return true;
	}
}
// === end ===

// === case: same_literal_extract_gt ===
class InputPreferDirectBooleanReturnSameLiteralExtractGtSliceViolation {
	boolean m(String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_le ===
class InputPreferDirectBooleanReturnSameLiteralExtractLeSliceViolation {
	boolean m(String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_method_call ===
class InputPreferDirectBooleanReturnSameLiteralExtractMethodCallSliceViolation {
	boolean m(String s) {
		s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_minus_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractMinusAssignSliceViolation {
	boolean m(int x) {
		x -= 1;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_mod_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractModAssignSliceViolation {
	boolean m(int x) {
		x %= 2;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_ne ===
class InputPreferDirectBooleanReturnSameLiteralExtractNeSliceViolation {
	boolean m(String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_receiver_call ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenReceiverCallSliceViolation {
	boolean m(String s) {
		(s).isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_receiver_instanceof ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenReceiverInstanceofSliceViolation {
	private Object box() {
		return null;
	}

	boolean m() {
		if ((box()) instanceof String)
			sideEffect();
		return true;
	}

	private boolean sideEffect() {
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_whole_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenWholeAssignSliceViolation {
	boolean m(boolean flag, boolean other) {
		flag = other;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_whole_call ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenWholeCallSliceViolation {
	boolean m(String s) {
		s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_whole_comparison ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenWholeComparisonSliceViolation {
	boolean m(int size, String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_paren_whole_shortcircuit ===
class InputPreferDirectBooleanReturnSameLiteralExtractParenWholeShortcircuitSliceViolation {
	boolean m(boolean a, String s) {
		if (a)
			s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_plus_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractPlusAssignSliceViolation {
	boolean m(int x) {
		x += 1;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_postfix_dec ===
class InputPreferDirectBooleanReturnSameLiteralExtractPostfixDecSliceViolation {
	boolean m(int i) {
		--i;
		return false;
	}
}
// === end ===

// === case: same_literal_extract_postfix_inc ===
class InputPreferDirectBooleanReturnSameLiteralExtractPostfixIncSliceViolation {
	boolean m(int i) {
		++i;
		return false;
	}
}
// === end ===

// === case: same_literal_extract_postfix_index ===
class InputPreferDirectBooleanReturnSameLiteralExtractPostfixIndexSliceViolation {
	boolean m(int[] arr, int i) {
		++arr[i];
		return false;
	}
}
// === end ===

// === case: same_literal_extract_prefix_dec ===
class InputPreferDirectBooleanReturnSameLiteralExtractPrefixDecSliceViolation {
	boolean m(int i) {
		--i;
		return false;
	}
}
// === end ===

// === case: same_literal_extract_prefix_inc ===
class InputPreferDirectBooleanReturnSameLiteralExtractPrefixIncSliceViolation {
	boolean m(int i) {
		++i;
		return false;
	}
}
// === end ===

// === case: same_literal_extract_prefix_inc_reversed ===
class InputPreferDirectBooleanReturnSameLiteralExtractPrefixIncReversedSliceViolation {
	boolean m(int i) {
		++i;
		return false;
	}
}
// === end ===

// === case: same_literal_extract_shift ===
class InputPreferDirectBooleanReturnSameLiteralExtractShiftSliceViolation {
	boolean m(int a, int b, String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_shift_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractShiftAssignSliceViolation {
	boolean m(int x) {
		x <<= 1;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_shift_left ===
class InputPreferDirectBooleanReturnSameLiteralExtractShiftLeftSliceViolation {
	boolean m(int a, int b, String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_shift_right_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractShiftRightAssignSliceViolation {
	boolean m(int n) {
		n >>= 1;
		return true;
	}
}
// === end ===

// === case: same_literal_extract_shortcircuit ===
class InputPreferDirectBooleanReturnSameLiteralExtractShortcircuitSliceViolation {
	boolean m(boolean a, String s) {
		if (a)
			s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_star_assign ===
class InputPreferDirectBooleanReturnSameLiteralExtractStarAssignSliceViolation {
	boolean m(int x) {
		x *= 2;
		return true;
	}
}
// === end ===

// === case: same_literal_false ===
class InputPreferDirectBooleanReturnSameLiteralFalseSliceViolation {
	boolean m(boolean flag) {
		return false;
	}
}
// === end ===

// === case: same_literal_land_cond ===
class InputPreferDirectBooleanReturnSameLiteralLandCondSliceViolation {
	boolean m(boolean a, boolean b) {
		return true;
	}
}
// === end ===

// === case: same_literal_multiline_cond ===
class InputPreferDirectBooleanReturnSameLiteralMultilineCondSliceViolation {
	boolean m(boolean a, boolean b) {
		return true;
	}
}
// === end ===

// === case: same_literal_tight_comparison_skipped ===
// skip-reason: ambiguous comparison operator
class InputPreferDirectBooleanReturnSameLiteralTightComparisonSkippedSliceViolation {
	boolean m(String s) {
		if (s.length()>2)
			return true;
		return true;
	}
}
// === end ===

// === case: trailing_comment_after_close_paren_skipped ===
// skip-reason: comment between condition and body
class InputPreferDirectBooleanReturnTrailingCommentAfterCloseParenSkippedSliceViolation {
	boolean m(boolean a, boolean b) {
		if (a
				&& b) // trailing
			return true;
		return false;
	}
}
// === end ===

// === case: trailing_return_indent_mismatch_skipped ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnTrailingReturnIndentMismatchSkippedSliceViolation {
	boolean m(boolean flag) {
		if (flag) return true;
	return false;
	}
}
// === end ===

// === case: with_else_false_true ===
class InputPreferDirectBooleanReturnWithElseFalseTrueSliceViolation {
	boolean m(boolean flag) {
		return !flag;
	}
}
// === end ===

// === case: with_else_true_false ===
class InputPreferDirectBooleanReturnWithElseTrueFalseSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===