package com.etk2000.checkstyle.inputs.preferdirectbooleanreturn;

// === case: anonymous_class_in_cond ===
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInCondSliceViolation {
	boolean m(Object o) {
		if (new Object() {
			@Override
			public String toString() {
				return "x";
			}
		}.equals(o))
			return true;
		return true;
	}
}
// === end ===

// === case: anonymous_class_in_cond_combine ===
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInCondCombineSliceViolation {
	boolean m(Object o, String s) {
		if (new Object() {
			@Override
			public String toString() {
				return "x";
			}
		}.equals(o))
			return s.isEmpty();
		return false;
	}
}
// === end ===

// === case: anonymous_class_in_cond_opposite ===
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInCondOppositeSliceViolation {
	boolean m(Object o) {
		if (new Object() {
			@Override
			public String toString() {
				return "x";
			}
		}.equals(o))
			return true;
		return false;
	}
}
// === end ===

// === case: anonymous_class_in_cond_unbraced_parent ===
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInCondUnbracedParentSliceViolation {
	boolean m(boolean flag, Object o) {
		if (flag)
			if (new Object() {
				@Override
				public String toString() {
					return "x";
				}
			}.equals(o))
				return true;
			else
				return true;
		return false;
	}
}
// === end ===

// === case: anonymous_class_in_guard ===
// imports: java.util.List
// skip-reason: cannot collapse the if: it would fold a class body onto one line
class InputPreferDirectBooleanReturnAnonymousClassInGuardSliceViolation {
	boolean m(Object o, List<String> list) {
		if (new Object() {
			@Override
			public String toString() {
				return "x";
			}
		}.equals(o) && list.remove("x"))
			return true;
		return true;
	}
}
// === end ===

// === case: anonymous_class_single_line_in_cond ===
class InputPreferDirectBooleanReturnAnonymousClassSingleLineInCondSliceViolation {
	boolean m(Object o) {
		new Object() { }.equals(o);
		return true;
	}
}
// === end ===

// === case: array_index_cond_negated ===
class InputPreferDirectBooleanReturnArrayIndexCondNegatedSliceViolation {
	boolean m(boolean[] flags, int i) {
		return !(flags[i]);
	}
}
// === end ===

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

// === case: atomic_underscore_digit ===
class InputPreferDirectBooleanReturnAtomicUnderscoreDigitSliceViolation {
	boolean m(boolean my_flag2) {
		return !my_flag2;
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

// === case: blank_line_between_body_and_trailing ===
class InputPreferDirectBooleanReturnBlankLineBetweenBodyAndTrailingSliceViolation {
	boolean m(boolean flag) {
		return flag;
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
class InputPreferDirectBooleanReturnBracedBodyCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: braced_body_comment_after_return ===
class InputPreferDirectBooleanReturnBracedBodyCommentAfterReturnSliceViolation {
	boolean m(boolean flag) {
		return flag; // explains the body
	}
}
// === end ===

// === case: braced_body_comment_before_return ===
class InputPreferDirectBooleanReturnBracedBodyCommentBeforeReturnSliceViolation {
	boolean m(boolean flag) {
		return flag; // explains the body
	}
}
// === end ===

// === case: braced_else_close_brace_indent_mismatch ===
class InputPreferDirectBooleanReturnBracedElseCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		return flag;
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

// === case: cast_intersection_in_cond ===
// imports: java.io.Serializable
class InputPreferDirectBooleanReturnCastIntersectionInCondSliceViolation {
	boolean m(Object r) {
		return ((Runnable & Serializable) r) != null;
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

// === case: combine_grid_braced_else_unbraced ===
class InputPreferDirectBooleanReturnCombineGridBracedElseUnbracedSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_braced_trailing ===
class InputPreferDirectBooleanReturnCombineGridBracedTrailingSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_inline_else_braced ===
class InputPreferDirectBooleanReturnCombineGridInlineElseBracedSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_inline_else_unbraced ===
class InputPreferDirectBooleanReturnCombineGridInlineElseUnbracedSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_next_line_else_braced ===
class InputPreferDirectBooleanReturnCombineGridNextLineElseBracedSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_next_line_else_unbraced ===
class InputPreferDirectBooleanReturnCombineGridNextLineElseUnbracedSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_next_line_trailing ===
class InputPreferDirectBooleanReturnCombineGridNextLineTrailingSliceViolation {
	boolean m(boolean flag, String s) {
		return flag && s.isEmpty();
	}
}
// === end ===

// === case: combine_grid_parent_if_then_unbraced ===
class InputPreferDirectBooleanReturnCombineGridParentIfThenUnbracedSliceViolation {
	boolean m(boolean flag, boolean other, String s) {
		if (flag)
			return other || s.isEmpty();
		throw new IllegalStateException();
	}
}
// === end ===

// === case: combine_length_exactly_max ===
class InputPreferDirectBooleanReturnCombineLengthExactlyMaxSliceViolation {
	boolean m(
			boolean conditionFlagWithAVeryLongDescriptiveParameterName,
			boolean valueFlagWithAnEquallyLongDescriptiveParameterName
	) {
		return conditionFlagWithAVeryLongDescriptiveParameterName && valueFlagWithAnEquallyLongDescriptiveParameterName;
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

// === case: comment_above_if_preserved ===
class InputPreferDirectBooleanReturnCommentAboveIfPreservedSliceViolation {
	boolean m(boolean flag) {
		// why we check
		return flag;
	}
}
// === end ===

// === case: comment_after_cond_relocated ===
class InputPreferDirectBooleanReturnCommentAfterCondRelocatedSliceViolation {
	boolean m(boolean flag) {
		return flag; // comment
	}
}
// === end ===

// === case: comment_after_paired_return_preserved ===
class InputPreferDirectBooleanReturnCommentAfterPairedReturnPreservedSliceViolation {
	boolean m(boolean flag) {
		return flag;
		// nothing follows
	}
}
// === end ===

// === case: comment_before_if_same_line ===
class InputPreferDirectBooleanReturnCommentBeforeIfSameLineSliceViolation {
	boolean m(boolean flag) {
		/* why */ return flag;
	}
}
// === end ===

// === case: comment_between_if_and_paren ===
class InputPreferDirectBooleanReturnCommentBetweenIfAndParenSliceViolation {
	boolean m(boolean flag) {
		return flag; // why
	}
}
// === end ===

// === case: comment_over_budget_still_reported ===
class InputPreferDirectBooleanReturnCommentOverBudgetStillReportedSliceViolation {
	boolean m(boolean flag) {
		return flag; // budget note budget note budget note budget note budget note budget note budget note budget note budget note
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

// === case: cond_unicode_escape_char_literal ===
class InputPreferDirectBooleanReturnCondUnicodeEscapeCharLiteralSliceViolation {
	boolean m(char c) {
		return c == '\u0041';
	}
}
// === end ===

// === case: cond_unicode_escape_string_literal ===
class InputPreferDirectBooleanReturnCondUnicodeEscapeStringLiteralSliceViolation {
	boolean m(String s) {
		return s.equals("\u0041");
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

// === case: else_line_trailing_comment ===
class InputPreferDirectBooleanReturnElseLineTrailingCommentSliceViolation {
	boolean m(boolean flag) {
		return flag; // otherwise
	}
}
// === end ===

// === case: enum_constant_body_return_pair ===
class InputPreferDirectBooleanReturnEnumConstantBodyReturnPairSliceViolation {
	enum Kind {
		FIRST {
			@Override
			boolean m(boolean flag) {
				return flag;
			}
		};

		abstract boolean m(boolean flag);
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

// === case: if_no_space_before_paren ===
class InputPreferDirectBooleanReturnIfNoSpaceBeforeParenSliceViolation {
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

// === case: instanceof_pattern_binding_combine ===
class InputPreferDirectBooleanReturnInstanceofPatternBindingCombineSliceViolation {
	boolean m(Object o) {
		return o instanceof String s && s.isEmpty();
	}
}
// === end ===

// === case: instanceof_pattern_binding_forward ===
class InputPreferDirectBooleanReturnInstanceofPatternBindingForwardSliceViolation {
	boolean m(Object o) {
		return o instanceof String s;
	}
}
// === end ===

// === case: instanceof_pattern_binding_negated ===
class InputPreferDirectBooleanReturnInstanceofPatternBindingNegatedSliceViolation {
	boolean m(Object o) {
		return (o instanceof String s) && s.isEmpty();
	}
}
// === end ===

// === case: javadoc_form_comment_in_cond ===
class InputPreferDirectBooleanReturnJavadocFormCommentInCondSliceViolation {
	boolean m(boolean flag) {
		return /** why */ flag;
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

// === case: masked_block_comment_balanced_atomic ===
class InputPreferDirectBooleanReturnMaskedBlockCommentBalancedAtomicSliceViolation {
	boolean m(String s) {
		return !s.equals(/*()*/"x");
	}
}
// === end ===

// === case: masked_block_comment_hidden_or_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentHiddenOrNegatedSliceViolation {
	boolean m(String s) {
		return !(s.equals(/*(*/"x") || s.contains(/*)*/"y"));
	}
}
// === end ===

// === case: masked_block_comment_leading_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentLeadingNegatedSliceViolation {
	boolean m(boolean flag) {
		return !(/* ( */ flag);
	}
}
// === end ===

// === case: masked_block_comment_lone_close_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentLoneCloseNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(/*)*/"x");
	}
}
// === end ===

// === case: masked_block_comment_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentLoneOpenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(/*(*/"x");
	}
}
// === end ===

// === case: masked_block_comment_spans_lines_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentSpansLinesNegatedSliceViolation {
	boolean m(String s) {
		return !(/* ( */ s.isEmpty());
	}
}
// === end ===

// === case: masked_block_comment_spans_lines_nested_negated ===
class InputPreferDirectBooleanReturnMaskedBlockCommentSpansLinesNestedNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(/* ( */ "x");
	}
}
// === end ===

// === case: masked_chain_before_literal_paren_negated ===
class InputPreferDirectBooleanReturnMaskedChainBeforeLiteralParenNegatedSliceViolation {
	boolean m(String s) {
		return !s.trim().equals("(");
	}
}
// === end ===

// === case: masked_char_backslash_escape_negated ===
class InputPreferDirectBooleanReturnMaskedCharBackslashEscapeNegatedSliceViolation {
	boolean m() {
		return !Character.isDigit('\\');
	}
}
// === end ===

// === case: masked_char_balanced_chain_atomic ===
class InputPreferDirectBooleanReturnMaskedCharBalancedChainAtomicSliceViolation {
	boolean m() {
		return !String.valueOf('(').equals(String.valueOf(')'));
	}
}
// === end ===

// === case: masked_char_escaped_quote_negated ===
class InputPreferDirectBooleanReturnMaskedCharEscapedQuoteNegatedSliceViolation {
	boolean m() {
		return !Character.isDigit('\'');
	}
}
// === end ===

// === case: masked_char_hidden_or_negated ===
class InputPreferDirectBooleanReturnMaskedCharHiddenOrNegatedSliceViolation {
	boolean m() {
		return !(Character.isDigit('(') || Character.isLetter(')'));
	}
}
// === end ===

// === case: masked_char_lone_close_negated ===
class InputPreferDirectBooleanReturnMaskedCharLoneCloseNegatedSliceViolation {
	boolean m() {
		return !Character.isDigit(')');
	}
}
// === end ===

// === case: masked_char_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedCharLoneOpenNegatedSliceViolation {
	boolean m() {
		return !Character.isDigit('(');
	}
}
// === end ===

// === case: masked_char_quote_negated ===
class InputPreferDirectBooleanReturnMaskedCharQuoteNegatedSliceViolation {
	boolean m() {
		return !Character.isDigit('"');
	}
}
// === end ===

// === case: masked_combine_and_double_negation_negated ===
class InputPreferDirectBooleanReturnMaskedCombineAndDoubleNegationNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !(!s.equals("(") && s.contains(")")) && flag;
	}
}
// === end ===

// === case: masked_combine_and_hidden_negated ===
class InputPreferDirectBooleanReturnMaskedCombineAndHiddenNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !(s.equals("(") && s.contains(")")) && flag;
	}
}
// === end ===

// === case: masked_combine_and_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedCombineAndLoneOpenNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !s.equals("(") && flag;
	}
}
// === end ===

// === case: masked_combine_or_double_negation_negated ===
class InputPreferDirectBooleanReturnMaskedCombineOrDoubleNegationNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return s.equals("(") || flag;
	}
}
// === end ===

// === case: masked_combine_or_hidden_negated ===
class InputPreferDirectBooleanReturnMaskedCombineOrHiddenNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !(s.equals("(") || s.contains(")")) || flag;
	}
}
// === end ===

// === case: masked_combine_or_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedCombineOrLoneOpenNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !s.equals("(") || flag;
	}
}
// === end ===

// === case: masked_double_negation_hidden_or ===
class InputPreferDirectBooleanReturnMaskedDoubleNegationHiddenOrSliceViolation {
	boolean m(String s) {
		return !(!s.equals("(") || s.contains(")"));
	}
}
// === end ===

// === case: masked_double_negation_lone_open ===
class InputPreferDirectBooleanReturnMaskedDoubleNegationLoneOpenSliceViolation {
	boolean m(String s) {
		return s.equals("(");
	}
}
// === end ===

// === case: masked_escaped_quote_before_paren_negated ===
class InputPreferDirectBooleanReturnMaskedEscapedQuoteBeforeParenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals("\"(");
	}
}
// === end ===

// === case: masked_javadoc_comment_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedJavadocCommentLoneOpenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(/**(*/"x");
	}
}
// === end ===

// === case: masked_line_comment_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedLineCommentLoneOpenNegatedSliceViolation {
	boolean m(String s) {
		return !s.isEmpty(); // (
	}
}
// === end ===

// === case: masked_line_comment_quote_negated ===
class InputPreferDirectBooleanReturnMaskedLineCommentQuoteNegatedSliceViolation {
	boolean m(String s) {
		return !s.isEmpty(); // say "(
	}
}
// === end ===

// === case: masked_string_backslash_escape_negated ===
class InputPreferDirectBooleanReturnMaskedStringBackslashEscapeNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals("(\\");
	}
}
// === end ===

// === case: masked_string_balanced_atomic ===
class InputPreferDirectBooleanReturnMaskedStringBalancedAtomicSliceViolation {
	boolean m(String s) {
		return !s.equals("()");
	}
}
// === end ===

// === case: masked_string_block_comment_opener_negated ===
class InputPreferDirectBooleanReturnMaskedStringBlockCommentOpenerNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals("/*(");
	}
}
// === end ===

// === case: masked_string_empty_literal_negated ===
class InputPreferDirectBooleanReturnMaskedStringEmptyLiteralNegatedSliceViolation {
	boolean m(String s) {
		return !s.contains("");
	}
}
// === end ===

// === case: masked_string_hidden_or_forward ===
class InputPreferDirectBooleanReturnMaskedStringHiddenOrForwardSliceViolation {
	boolean m(String s) {
		return s.equals("(") || s.contains(")");
	}
}
// === end ===

// === case: masked_string_hidden_or_negated ===
class InputPreferDirectBooleanReturnMaskedStringHiddenOrNegatedSliceViolation {
	boolean m(String s) {
		return !(s.equals("(") || s.contains(")"));
	}
}
// === end ===

// === case: masked_string_lone_close_negated ===
class InputPreferDirectBooleanReturnMaskedStringLoneCloseNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(")");
	}
}
// === end ===

// === case: masked_string_lone_open_negated ===
class InputPreferDirectBooleanReturnMaskedStringLoneOpenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals("(");
	}
}
// === end ===

// === case: masked_string_slashes_hide_paren_negated ===
class InputPreferDirectBooleanReturnMaskedStringSlashesHideParenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals(")//(");
	}
}
// === end ===

// === case: masked_string_supplementary_paren_negated ===
class InputPreferDirectBooleanReturnMaskedStringSupplementaryParenNegatedSliceViolation {
	boolean m(String s) {
		return !s.equals("𝐀(");
	}
}
// === end ===

// === case: masked_string_unbalanced_close_or_negated ===
class InputPreferDirectBooleanReturnMaskedStringUnbalancedCloseOrNegatedSliceViolation {
	boolean m(String s, boolean flag) {
		return !(s.equals(")") || flag);
	}
}
// === end ===

// === case: masked_text_block_lone_open_negated ===
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnMaskedTextBlockLoneOpenNegatedSliceViolation {
	boolean m() {
		if ("""
				(
				""".isEmpty())
			return false;
		return true;
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

// === case: mixed_3c_assign_wraps ===
class InputPreferDirectBooleanReturnMixed3cAssignWrapsSliceViolation {
	boolean m(boolean flag, boolean x, String s) {
		return flag || (x = s.isEmpty());
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
class InputPreferDirectBooleanReturnMultilineCondBlockCommentSpansSliceViolation {
	boolean m(boolean a, boolean b) {
		return a /* spanning block */ && b;
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

// === case: multiline_cond_break_before_dot ===
class InputPreferDirectBooleanReturnMultilineCondBreakBeforeDotSliceViolation {
	boolean m(String s) {
		return s.isEmpty();
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
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
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

// === case: multiline_cond_text_block_combine ===
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnMultilineCondTextBlockCombineSliceViolation {
	boolean m(String s) {
		if ("""
				text
				""".isEmpty())
			return s.isEmpty();
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

// === case: negation_not_flipped_on_comparison ===
class InputPreferDirectBooleanReturnNegationNotFlippedOnComparisonSliceViolation {
	// a flipped operator would diverge on NaN: !(a < b) is true where a >= b is false
	boolean m(double a, double b) {
		return !(a < b);
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

// === case: not_equal_cond_negated ===
class InputPreferDirectBooleanReturnNotEqualCondNegatedSliceViolation {
	boolean m(int a, int b) {
		return !(a != b);
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

// === case: opposite_grid_inline_else_unbraced ===
class InputPreferDirectBooleanReturnOppositeGridInlineElseUnbracedSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: opposite_grid_parent_if_then_unbraced ===
class InputPreferDirectBooleanReturnOppositeGridParentIfThenUnbracedSliceViolation {
	boolean m(boolean flag, boolean other) {
		if (flag)
			return other;
		throw new IllegalStateException();
	}
}
// === end ===

// === case: opposite_length_exactly_max ===
class InputPreferDirectBooleanReturnOppositeLengthExactlyMaxSliceViolation {
	boolean m(
			boolean conditionFlagWithAVeryLongDescriptiveParameterName,
			boolean valueFlagWithAnEquallyLongDescriptiveParameterName
	) {
		return conditionFlagWithAVeryLongDescriptiveParameterName && valueFlagWithAnEquallyLongDescriptiveParameterName;
	}
}
// === end ===

// === case: opposite_masked_atomic_length_exactly_max ===
class InputPreferDirectBooleanReturnOppositeMaskedAtomicLengthExactlyMaxSliceViolation {
	boolean m(String s) {
		return !s.equals("(aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
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

// === case: opposite_prefix_length_exactly_max ===
class InputPreferDirectBooleanReturnOppositePrefixLengthExactlyMaxSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) return s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		throw new IllegalStateException();
	}
}
// === end ===

// === case: opposite_suffix_length_exactly_max ===
class InputPreferDirectBooleanReturnOppositeSuffixLengthExactlyMaxSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			return s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); }
		throw new IllegalStateException();
	}
}
// === end ===

// === case: or_condition_negated_adds_parens ===
class InputPreferDirectBooleanReturnOrConditionNegatedAddsParensSliceViolation {
	boolean m(boolean a, boolean b) {
		return !(a || b);
	}
}
// === end ===

// === case: paired_return_trailing_block_comment ===
class InputPreferDirectBooleanReturnPairedReturnTrailingBlockCommentSliceViolation {
	boolean m(boolean flag) {
		return flag; // fallthrough
	}
}
// === end ===

// === case: paired_return_trailing_block_comment_spans ===
class InputPreferDirectBooleanReturnPairedReturnTrailingBlockCommentSpansSliceViolation {
	boolean m(boolean flag) {
		return flag; /* note
				continues */
	}
}
// === end ===

// === case: record_pattern_cond ===
class InputPreferDirectBooleanReturnRecordPatternCondSliceViolation {
	record Point(int x, int y) {}

	boolean m(Object o) {
		return o instanceof Point(int x, int y);
	}
}
// === end ===

// === case: redundant_grid_braced_else_braced ===
class InputPreferDirectBooleanReturnRedundantGridBracedElseBracedSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_braced_else_unbraced ===
class InputPreferDirectBooleanReturnRedundantGridBracedElseUnbracedSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_braced_trailing ===
class InputPreferDirectBooleanReturnRedundantGridBracedTrailingSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_inline_else_braced ===
class InputPreferDirectBooleanReturnRedundantGridInlineElseBracedSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_inline_else_unbraced ===
class InputPreferDirectBooleanReturnRedundantGridInlineElseUnbracedSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_next_line_else_braced ===
class InputPreferDirectBooleanReturnRedundantGridNextLineElseBracedSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_next_line_trailing ===
class InputPreferDirectBooleanReturnRedundantGridNextLineTrailingSliceViolation {
	boolean m(boolean flag) {
		return true;
	}
}
// === end ===

// === case: redundant_grid_parent_if_then_unbraced ===
class InputPreferDirectBooleanReturnRedundantGridParentIfThenUnbracedSliceViolation {
	boolean m(boolean flag, boolean other) {
		if (flag)
			return true;
		throw new IllegalStateException();
	}
}
// === end ===

// === case: return_block_comment_before_literal ===
class InputPreferDirectBooleanReturnReturnBlockCommentBeforeLiteralSliceViolation {
	boolean m(boolean flag) {
		return flag; // always
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

// === case: same_literal_chained_comparison ===
class InputPreferDirectBooleanReturnSameLiteralChainedComparisonSliceViolation {
	boolean m(int index, int size, String s) {
		s.isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_diamond_call ===
// imports: java.util.ArrayList
class InputPreferDirectBooleanReturnSameLiteralDiamondCallSliceViolation {
	boolean m() {
		new ArrayList<>().isEmpty();
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

// === case: same_literal_drop_nan_comparison ===
class InputPreferDirectBooleanReturnSameLiteralDropNanComparisonSliceViolation {
	// d != d is the NaN test; dropping it is safe only because both branches already returned true
	boolean m(double d) {
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

// === case: same_literal_extract_braced_parent_else ===
class InputPreferDirectBooleanReturnSameLiteralExtractBracedParentElseSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			return false;
		else {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
	}
}
// === end ===

// === case: same_literal_extract_braced_parent_if_then ===
class InputPreferDirectBooleanReturnSameLiteralExtractBracedParentIfThenSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
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

// === case: same_literal_extract_budget_braced_exactly_max ===
class InputPreferDirectBooleanReturnSameLiteralExtractBudgetBracedExactlyMaxSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_budget_wrapped_exactly_max ===
class InputPreferDirectBooleanReturnSameLiteralExtractBudgetWrappedExactlyMaxSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				s.equals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
				return true;
			}
		return false;
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

// === case: same_literal_extract_case_group ===
class InputPreferDirectBooleanReturnSameLiteralExtractCaseGroupSliceViolation {
	boolean m(int x, boolean flag, String s) {
		switch (x) {
			case 1:
				flag = !flag;
				if (s.isEmpty())
					s.isBlank();
				return true;

			case 2:
				flag = !flag;
			default:
				return flag;
		}
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

// === case: same_literal_extract_else_form_guard ===
class InputPreferDirectBooleanReturnSameLiteralExtractElseFormGuardSliceViolation {
	boolean m(String s) {
		if (s.isEmpty())
			s.isBlank();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_else_form_noguard ===
class InputPreferDirectBooleanReturnSameLiteralExtractElseFormNoguardSliceViolation {
	boolean m(String s) {
		s.isBlank();
		return true;
	}
}
// === end ===

// === case: same_literal_extract_eq ===
class InputPreferDirectBooleanReturnSameLiteralExtractEqSliceViolation {
	boolean m(int i) {
		++i;
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

// === case: same_literal_extract_lt ===
class InputPreferDirectBooleanReturnSameLiteralExtractLtSliceViolation {
	boolean m(int i) {
		++i;
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

// === case: same_literal_extract_parent_do ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentDoSliceViolation {
	boolean m(boolean flag, String s) {
		do
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		while (flag);
	}
}
// === end ===

// === case: same_literal_extract_parent_do_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentDoInlineSliceViolation {
	boolean m(boolean flag, String s) {
		do {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		while (flag);
	}
}
// === end ===

// === case: same_literal_extract_parent_else ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentElseSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			return false;
		else
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
	}
}
// === end ===

// === case: same_literal_extract_parent_else_if ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentElseIfSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			return false;
		else {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
	}
}
// === end ===

// === case: same_literal_extract_parent_for ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentForSliceViolation {
	boolean m(String s) {
		for (var i = 0; i < 2; ++i)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_for_each ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentForEachSliceViolation {
	boolean m(String s, String[] names) {
		for (var name : names)
			{
				if (name.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_for_each_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentForEachInlineSliceViolation {
	boolean m(String s, String[] names) {
		for (var name : names) {
			if (name.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_for_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentForInlineSliceViolation {
	boolean m(String s) {
		for (var i = 0; i < 2; ++i) {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_braced_else_braced ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenBracedElseBracedSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_comment ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenCommentSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			} // why
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_comment_block_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenCommentBlockInlineSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			} // c
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_dangling_else ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenDanglingElseSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		else
			return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenInlineSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_inline_noguard ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenInlineNoguardSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag) {
			s.isBlank();
			return true;
		}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_multiline_cond ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenMultilineCondSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_noguard ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenNoguardSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			{
				s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_if_then_suffix ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentIfThenSuffixSliceViolation {
	boolean m(boolean flag, boolean other, String s) {
		if (flag) {
			if (other)
				{
					if (s.isEmpty())
						s.isBlank();
					return true;
				} }
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_labeled ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentLabeledSliceViolation {
	boolean m(String s) {
		check:
		{
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
	}
}
// === end ===

// === case: same_literal_extract_parent_labeled_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentLabeledInlineSliceViolation {
	boolean m(String s) {
		check: {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
	}
}
// === end ===

// === case: same_literal_extract_parent_nested_for_in_if ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentNestedForInIfSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			for (var i = 0; i < 2; ++i)
				{
					if (s.isEmpty())
						s.isBlank();
					return true;
				}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_while ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentWhileSliceViolation {
	boolean m(boolean flag, String s) {
		while (flag)
			{
				if (s.isEmpty())
					s.isBlank();
				return true;
			}
		return false;
	}
}
// === end ===

// === case: same_literal_extract_parent_while_inline ===
class InputPreferDirectBooleanReturnSameLiteralExtractParentWhileInlineSliceViolation {
	boolean m(boolean flag, String s) {
		while (flag) {
			if (s.isEmpty())
				s.isBlank();
			return true;
		}
		return false;
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

// === case: same_literal_generic_tight_call ===
// imports: java.util.HashSet
class InputPreferDirectBooleanReturnSameLiteralGenericTightCallSliceViolation {
	boolean m() {
		new HashSet<String>().size();
		return true;
	}
}
// === end ===

// === case: same_literal_lambda_in_cond ===
// imports: java.util.List
class InputPreferDirectBooleanReturnSameLiteralLambdaInCondSliceViolation {
	boolean m(List<Integer> list) {
		list.removeIf(x -> x > 0);
		return true;
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

// === case: same_literal_nested_generics_close_run ===
// imports: java.util.Map
class InputPreferDirectBooleanReturnSameLiteralNestedGenericsCloseRunSliceViolation {
	boolean m(Object o) {
		((Map<String, Map<String, Map<String, Integer>>>) o).isEmpty();
		return true;
	}
}
// === end ===

// === case: same_literal_text_block_effect ===
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnSameLiteralTextBlockEffectSliceViolation {
	boolean m(String s) {
		if ((s = """
				text
				""") != null)
			return true;
		return true;
	}
}
// === end ===

// === case: same_literal_text_block_effect_unbraced_parent ===
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnSameLiteralTextBlockEffectUnbracedParentSliceViolation {
	boolean m(boolean flag, String s) {
		if (flag)
			if ((s = """
					text
					""") != null)
				return true;
			else
				return true;
		return false;
	}
}
// === end ===

// === case: same_literal_text_block_guard ===
// imports: java.util.List
// skip-reason: cannot collapse the if: it would destroy a text block's significant whitespace
class InputPreferDirectBooleanReturnSameLiteralTextBlockGuardSliceViolation {
	boolean m(List<String> list) {
		if ("""
				text
				""".isEmpty() && list.remove("x"))
			return true;
		return true;
	}
}
// === end ===

// === case: same_literal_tight_comparison ===
class InputPreferDirectBooleanReturnSameLiteralTightComparisonSliceViolation {
	boolean m(String s) {
		s.length();
		return true;
	}
}
// === end ===

// === case: switch_case_group_return_pair ===
class InputPreferDirectBooleanReturnSwitchCaseGroupReturnPairSliceViolation {
	boolean m(int x, boolean flag) {
		switch (x) {
			case 1 -> {
				flag = !flag;
				return flag;
			}
			default -> flag = !flag;
		}
		return flag;
	}
}
// === end ===

// === case: then_close_brace_trailing_comment ===
class InputPreferDirectBooleanReturnThenCloseBraceTrailingCommentSliceViolation {
	boolean m(boolean flag) {
		return flag; // done
	}
}
// === end ===

// === case: trailing_comment_after_close_paren_relocated ===
class InputPreferDirectBooleanReturnTrailingCommentAfterCloseParenRelocatedSliceViolation {
	boolean m(boolean a, boolean b) {
		return a && b; // trailing
	}
}
// === end ===

// === case: trailing_return_indent_mismatch ===
class InputPreferDirectBooleanReturnTrailingReturnIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		return flag;
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