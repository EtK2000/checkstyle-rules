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
	@Deprecated(/* pending */) // violation: Remove empty parentheses from annotation '@Deprecated'.
	int value = 1;
}
// === end ===

// === case: empty_parens ===
class InputRedundantAnnotationSyntaxEmptyParensSliceViolation {
	@Override() // violation: Remove empty parentheses from annotation '@Override'.
	public String toString() {
		return "";
	}
}
// === end ===

// === case: empty_parens_before_declaration ===
class InputRedundantAnnotationSyntaxEmptyParensBeforeDeclarationSliceViolation {
	@Override() void f() {} // violation: Remove empty parentheses from annotation '@Override'.
}
// === end ===

// === case: empty_parens_comment_before_paren_and_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBeforeParenAndInteriorSliceViolation {
	@B /* x */ (/* c */) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_between_name_paren ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBetweenNameParenSliceViolation {
	@B /* x */ () // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorSliceViolation {
	@B(/* c */) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_multi ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorMultiSliceViolation {
	@B(/* a */ /* b */) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamSliceViolation {
	void m(@B(/* c */) String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_comment_interior_on_param_no_space ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamNoSpaceSliceViolation {
	void m(@B(/* c */)String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_comment_interior_padded ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorPaddedSliceViolation {
	@B(  /* c */  ) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_then_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorThenAnnotationSliceViolation {
	void m(@A(/* c */)@B String param) {} // violation: Remove empty parentheses from annotation '@A'.
}
// === end ===

// === case: empty_parens_empty_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensEmptyCommentInteriorSliceViolation {
	@B(/**/) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_nested_annotation_value ===
class InputRedundantAnnotationSyntaxEmptyParensNestedAnnotationValueSliceViolation {
	@D(@B()) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_no_separator_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorAnnotationSliceViolation {
	void m(@A()@B String param) {} // violation: Remove empty parentheses from annotation '@A'.
}
// === end ===

// === case: empty_parens_no_separator_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorIdentSliceViolation {
	void m(@B()String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_on_class ===
@A() // violation: Remove empty parentheses from annotation '@A'.
class InputRedundantAnnotationSyntaxEmptyParensOnClassSliceViolation {}
// === end ===

// === case: empty_parens_on_field ===
class InputRedundantAnnotationSyntaxEmptyParensOnFieldSliceViolation {
	@B() // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_on_local ===
class InputRedundantAnnotationSyntaxEmptyParensOnLocalSliceViolation {
	void m() {
		@B() // violation: Remove empty parentheses from annotation '@B'.
		final var x = 1;
	}
}
// === end ===

// === case: empty_parens_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnParamSliceViolation {
	void m(@B() String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_qualified ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSliceViolation {
	@com.example.A() // violation: Remove empty parentheses from annotation '@A'.
	int field;
}
// === end ===

// === case: empty_parens_qualified_spaced ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSpacedSliceViolation {
	@com.example.A () // violation: Remove empty parentheses from annotation '@A'.
	int field;
}
// === end ===

// === case: empty_parens_space_before ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeSliceViolation {
	@B () // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_space_before_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeOnParamSliceViolation {
	void m(@B () String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_space_both ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBothSliceViolation {
	@B ( ) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_space_inside ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceInsideSliceViolation {
	@B( ) // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_stacked_both_fire_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensStackedBothFireOnParamSliceViolation {
	void m(@A() @B() String param) {} // violation: Remove empty parentheses from annotation '@A'. // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_string_interior_survives_sibling_fix ===
class InputRedundantAnnotationSyntaxEmptyParensStringInteriorSurvivesSiblingFixSliceViolation {
	void m(@A("") @B() String param) {} // violation: Remove empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_trailing_line_comment_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTrailingLineCommentDecoySliceViolation {
	@B () // @X() // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: entry_state_annotation_after_block_comment_close ===
class InputRedundantAnnotationSyntaxEntryStateAnnotationAfterBlockCommentCloseSliceViolation {
	/*
	*/ @B() // @X() // violation: Remove empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: explicit_value_comment_after_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterEqSliceViolation {
	@A(value = /* keep */ "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_all_gaps ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAllGapsSliceViolation {
	@A(/* a */ value /* b */ = /* c */ "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_before_value ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBeforeValueSliceViolation {
	@A(/* keep */ value = "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_between_value_and_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBetweenValueAndEqSliceViolation {
	@A(value /* keep */ = "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueCommentValueDecoySliceViolation {
	@A(/* value = */ value = "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesSliceViolation {
	@A(value="x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_no_spaces_qualified ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesQualifiedSliceViolation {
	@com.example.A(value="x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_string_contains_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueStringContainsValueDecoySliceViolation {
	@A(value = "value = x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: multi_line_empty_parens ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_blank_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlankBetweenSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.

	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_block_comment_middle ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlockCommentMiddleSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	/* c */
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_code_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCodeAfterCloseSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_before_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBeforeCloseSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	/* c */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBetweenSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	// c
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_and_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenAndCloseSliceViolation {
	@B( /* c */ // violation: Remove empty parentheses from annotation '@B'.
	) /* d */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_tail_and_close_prefix ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenTailAndClosePrefixSliceViolation {
	@B( /* c */ // violation: Remove empty parentheses from annotation '@B'.
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_content_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensContentAfterCloseSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	) // comment
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenSliceViolation {
	@B( // note // violation: Remove empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_block ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenBlockSliceViolation {
	@B( // note // violation: Remove empty parentheses from annotation '@B'.
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_code ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenCodeSliceViolation {
	@B( // note // violation: Remove empty parentheses from annotation '@B'.
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_multiline_block_comment_between ===
// skip-reason: complex multiline annotation
class InputRedundantAnnotationSyntaxMultiLineEmptyParensMultilineBlockCommentBetweenSliceViolation {
	@B( // violation: Remove empty parentheses from annotation '@B'.
	/*
	x
	*/
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_open_line_annotation_decoy ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensOpenLineAnnotationDecoySliceViolation {
	@B( /* @X() */ // violation: Remove empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_annotation ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueAnnotationSliceViolation {
	@D( // violation: Remove redundant 'value =' from annotation '@D'.
			value = @B
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_blank_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlankBeforeSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.

			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			/* note */
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_close_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCloseOnValueLineSliceViolation {
	@C( // violation: Remove redundant 'value =' from annotation '@C'.
			value = 5)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_line_comment_in_span ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentInSpanSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			// note
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_comment_decoy ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineCommentDecoySliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			/*
			value = decoy
			*/
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_expr ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineExprSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "a"
			+ "b"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_expr_overflow ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineExprOverflowSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueNoSpacesSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value="x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockCommentSliceViolation {
	@A( /* c */ // violation: Remove redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_then_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockThenLineCommentSliceViolation {
	@A( /* c */ // note // violation: Remove redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineCommentSliceViolation {
	@A( // note // violation: Remove redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_paren_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueParenValueSliceViolation {
	@C( // violation: Remove redundant 'value =' from annotation '@C'.
			value = (1 + 2) * 3
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_at_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthAtLimitSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthOverLimitSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_with_array ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWithArraySliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = {"x", "y"}
	)
	int field;
}
// === end ===

// === case: multi_line_redundant_value ===
class InputRedundantAnnotationSyntaxMultiLineRedundantValueSliceViolation {
	@A( // violation: Remove redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: redundant_value_on_param ===
class InputRedundantAnnotationSyntaxRedundantValueOnParamSliceViolation {
	void m(@A(value = "x") String param) {} // violation: Remove redundant 'value =' from annotation '@A'.
}
// === end ===

// === case: redundant_value_with_annotation_value ===
class InputRedundantAnnotationSyntaxRedundantValueWithAnnotationValueSliceViolation {
	@D(value = @B) // violation: Remove redundant 'value =' from annotation '@D'.
	int field;
}
// === end ===

// === case: redundant_value_with_array ===
class InputRedundantAnnotationSyntaxRedundantValueWithArraySliceViolation {
	@A(value = {"x", "y"}) // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: redundant_value_with_numeric ===
class InputRedundantAnnotationSyntaxRedundantValueWithNumericSliceViolation {
	@C(value = 42) // violation: Remove redundant 'value =' from annotation '@C'.
	int field;
}
// === end ===

// === case: redundant_value_with_string ===
class InputRedundantAnnotationSyntaxRedundantValueWithStringSliceViolation {
	@A(value = "x") // violation: Remove redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===