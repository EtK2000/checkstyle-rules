package com.etk2000.checkstyle.inputs.redundantannotationsyntax;

@interface A {
	String[] value();
}
@interface B {}
@interface C {
	int value();
}
@interface D {
	B value();
}

// === case: closed_annotation_comment_then_value_code ===
class InputRedundantAnnotationSyntaxClosedAnnotationCommentThenValueCodeSliceViolation {
	@Deprecated /* pending */
	int value = 1;
}
// === end ===

// === case: empty_parens ===
class InputRedundantAnnotationSyntaxEmptyParensSliceViolation {
	@Override
	public String toString() {
		return "";
	}
}
// === end ===

// === case: empty_parens_before_declaration ===
class InputRedundantAnnotationSyntaxEmptyParensBeforeDeclarationSliceViolation {
	@Override void f() {}
}
// === end ===

// === case: empty_parens_comment_before_paren_and_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBeforeParenAndInteriorSliceViolation {
	@B /* x */ /* c */
	int field;
}
// === end ===

// === case: empty_parens_comment_between_name_paren ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBetweenNameParenSliceViolation {
	@B /* x */
	int field;
}
// === end ===

// === case: empty_parens_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorSliceViolation {
	@B /* c */
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_multi ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorMultiSliceViolation {
	@B /* a */ /* b */
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamSliceViolation {
	void m(@B /* c */ String param) {}
}
// === end ===

// === case: empty_parens_comment_interior_on_param_no_space ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamNoSpaceSliceViolation {
	void m(@B /* c */ String param) {}
}
// === end ===

// === case: empty_parens_comment_interior_padded ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorPaddedSliceViolation {
	@B /* c */
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_then_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorThenAnnotationSliceViolation {
	void m(@A /* c */ @B String param) {}
}
// === end ===

// === case: empty_parens_empty_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensEmptyCommentInteriorSliceViolation {
	@B /**/
	int field;
}
// === end ===

// === case: empty_parens_nested_annotation_value ===
class InputRedundantAnnotationSyntaxEmptyParensNestedAnnotationValueSliceViolation {
	@D(@B)
	int field;
}
// === end ===

// === case: empty_parens_no_separator_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorAnnotationSliceViolation {
	void m(@A @B String param) {}
}
// === end ===

// === case: empty_parens_no_separator_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorIdentSliceViolation {
	void m(@B String param) {}
}
// === end ===

// === case: empty_parens_on_class ===
@A
class InputRedundantAnnotationSyntaxEmptyParensOnClassSliceViolation {}
// === end ===

// === case: empty_parens_on_field ===
class InputRedundantAnnotationSyntaxEmptyParensOnFieldSliceViolation {
	@B
	int field;
}
// === end ===

// === case: empty_parens_on_local ===
class InputRedundantAnnotationSyntaxEmptyParensOnLocalSliceViolation {
	void m() {
		@B
		final var x = 1;
	}
}
// === end ===

// === case: empty_parens_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnParamSliceViolation {
	void m(@B String param) {}
}
// === end ===

// === case: empty_parens_qualified ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSliceViolation {
	@com.example.A
	int field;
}
// === end ===

// === case: empty_parens_qualified_spaced ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSpacedSliceViolation {
	@com.example.A
	int field;
}
// === end ===

// === case: empty_parens_space_before ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeSliceViolation {
	@B
	int field;
}
// === end ===

// === case: empty_parens_space_before_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeOnParamSliceViolation {
	void m(@B String param) {}
}
// === end ===

// === case: empty_parens_space_both ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBothSliceViolation {
	@B
	int field;
}
// === end ===

// === case: empty_parens_space_inside ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceInsideSliceViolation {
	@B
	int field;
}
// === end ===

// === case: empty_parens_stacked_both_fire_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensStackedBothFireOnParamSliceViolation {
	void m(@A @B String param) {}
}
// === end ===

// === case: empty_parens_string_interior_survives_sibling_fix ===
class InputRedundantAnnotationSyntaxEmptyParensStringInteriorSurvivesSiblingFixSliceViolation {
	void m(@A("") @B String param) {}
}
// === end ===

// === case: empty_parens_trailing_line_comment_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTrailingLineCommentDecoySliceViolation {
	@B // @X()
	int field;
}
// === end ===

// === case: entry_state_annotation_after_block_comment_close ===
class InputRedundantAnnotationSyntaxEntryStateAnnotationAfterBlockCommentCloseSliceViolation {
	/*
	*/ @B // @X()
	int field;
}
// === end ===

// === case: explicit_value_comment_after_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterEqSliceViolation {
	@A(/* keep */ "x")
	int field;
}
// === end ===

// === case: explicit_value_comment_all_gaps ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAllGapsSliceViolation {
	@A(/* a */ /* b */ /* c */ "x")
	int field;
}
// === end ===

// === case: explicit_value_comment_before_value ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBeforeValueSliceViolation {
	@A(/* keep */ "x")
	int field;
}
// === end ===

// === case: explicit_value_comment_between_value_and_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBetweenValueAndEqSliceViolation {
	@A(/* keep */ "x")
	int field;
}
// === end ===

// === case: explicit_value_comment_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueCommentValueDecoySliceViolation {
	@A(/* value = */ "x")
	int field;
}
// === end ===

// === case: explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesSliceViolation {
	@A("x")
	int field;
}
// === end ===

// === case: explicit_value_no_spaces_qualified ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesQualifiedSliceViolation {
	@com.example.A("x")
	int field;
}
// === end ===

// === case: explicit_value_string_contains_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueStringContainsValueDecoySliceViolation {
	@A("value = x")
	int field;
}
// === end ===

// === case: multi_line_empty_parens ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensSliceViolation {
	@B
	int field;
}
// === end ===

// === case: multi_line_empty_parens_blank_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlankBetweenSliceViolation {
	@B
	int field;
}
// === end ===

// === case: multi_line_empty_parens_block_comment_middle ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlockCommentMiddleSliceViolation {
	@B /* c */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_code_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCodeAfterCloseSliceViolation {
	@B int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_before_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBeforeCloseSliceViolation {
	@B /* c */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBetweenSliceViolation {
	@B // c
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_and_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenAndCloseSliceViolation {
	@B /* c */ /* d */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_tail_and_close_prefix ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenTailAndClosePrefixSliceViolation {
	@B /* c */ /* d */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_content_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensContentAfterCloseSliceViolation {
	@B // comment
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenSliceViolation {
	@B // note
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_block ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenBlockSliceViolation {
	@B( // note
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_code ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenCodeSliceViolation {
	@B( // note
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_multiline_block_comment_between ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensMultilineBlockCommentBetweenSliceViolation {
	@B(
	/*
	x
	*/
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_open_line_annotation_decoy ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensOpenLineAnnotationDecoySliceViolation {
	@B /* @X() */
	int field;
}
// === end ===

// === case: multi_line_explicit_value_annotation ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueAnnotationSliceViolation {
	@D(@B)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_blank_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlankBeforeSliceViolation {
	@A("x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeSliceViolation {
	@A(/* note */ "x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_close_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCloseOnValueLineSliceViolation {
	@C(5)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_line_comment_in_span ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentInSpanSliceViolation {
	@A(
			// note
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_comment_decoy ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineCommentDecoySliceViolation {
	@A(
			/*
			value = decoy
			*/
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_expr ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineExprSliceViolation {
	@A("a" + "b")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_expr_overflow ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineExprOverflowSliceViolation {
	@A(
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueNoSpacesSliceViolation {
	@A("x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockCommentSliceViolation {
	@A(/* c */ "x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_then_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockThenLineCommentSliceViolation {
	@A( /* c */ // note
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineCommentSliceViolation {
	@A( // note
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_paren_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueParenValueSliceViolation {
	@C((1 + 2) * 3)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_at_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthAtLimitSliceViolation {
	@A("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthOverLimitSliceViolation {
	@A(
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_with_array ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWithArraySliceViolation {
	@A({"x", "y"})
	int field;
}
// === end ===

// === case: multi_line_redundant_value ===
class InputRedundantAnnotationSyntaxMultiLineRedundantValueSliceViolation {
	@A("x")
	int field;
}
// === end ===

// === case: redundant_value_on_param ===
class InputRedundantAnnotationSyntaxRedundantValueOnParamSliceViolation {
	void m(@A("x") String param) {}
}
// === end ===

// === case: redundant_value_with_annotation_value ===
class InputRedundantAnnotationSyntaxRedundantValueWithAnnotationValueSliceViolation {
	@D(@B)
	int field;
}
// === end ===

// === case: redundant_value_with_array ===
class InputRedundantAnnotationSyntaxRedundantValueWithArraySliceViolation {
	@A({"x", "y"})
	int field;
}
// === end ===

// === case: redundant_value_with_numeric ===
class InputRedundantAnnotationSyntaxRedundantValueWithNumericSliceViolation {
	@C(42)
	int field;
}
// === end ===

// === case: redundant_value_with_string ===
class InputRedundantAnnotationSyntaxRedundantValueWithStringSliceViolation {
	@A("x")
	int field;
}
// === end ===