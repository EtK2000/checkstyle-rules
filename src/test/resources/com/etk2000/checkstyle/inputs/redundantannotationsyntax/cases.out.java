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
@interface Dbl {
	double value();
}
@interface E {
	B[] value();
}
@interface F {
	D value();
}
@interface G {
	D[] value();
}
@interface Ä {
	int value() default 0;
}
class 𝕏Box {}

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

// === case: empty_parens_among_modifiers_embedded ===
class InputRedundantAnnotationSyntaxEmptyParensAmongModifiersEmbeddedSliceViolation {
	public @B static int field;
}
// === end ===

// === case: empty_parens_before_declaration ===
class InputRedundantAnnotationSyntaxEmptyParensBeforeDeclarationSliceViolation {
	@Override void f() {}
}
// === end ===

// === case: empty_parens_below_javadoc ===
class InputRedundantAnnotationSyntaxEmptyParensBelowJavadocSliceViolation {
	/** Doc. */
	@B
	int field;
}
// === end ===

// === case: empty_parens_comment_after_at ===
class InputRedundantAnnotationSyntaxEmptyParensCommentAfterAtSliceViolation {
	@/* c */B
	int field;
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

// === case: empty_parens_fold_over_limit ===
// skip-reason: cannot simplify the annotation: joining its lines would run past the line length limit
class InputRedundantAnnotationSyntaxEmptyParensFoldOverLimitSliceViolation {
	@B( /* a comment this long is what makes the folded line cross the limit aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa */
	) int field;
}
// === end ===

// === case: empty_parens_in_array_init ===
class InputRedundantAnnotationSyntaxEmptyParensInArrayInitSliceViolation {
	@E({@B, @B})
	int field;
}
// === end ===

// === case: empty_parens_in_array_init_last ===
class InputRedundantAnnotationSyntaxEmptyParensInArrayInitLastSliceViolation {
	@E({@B, @B})
	int field;
}
// === end ===

// === case: empty_parens_inside_anonymous_class ===
class InputRedundantAnnotationSyntaxEmptyParensInsideAnonymousClassSliceViolation {
	Object anon = new Object() {
		@B
		int field;
	};
}
// === end ===

// === case: empty_parens_multi_declarator ===
class InputRedundantAnnotationSyntaxEmptyParensMultiDeclaratorSliceViolation {
	@B
	int first, second;
}
// === end ===

// === case: empty_parens_name_then_parens_next_line ===
class InputRedundantAnnotationSyntaxEmptyParensNameThenParensNextLineSliceViolation {
	@B
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

// === case: empty_parens_no_separator_astral_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorAstralIdentSliceViolation {
	void m(@B 𝕏Box param) {}
}
// === end ===

// === case: empty_parens_no_separator_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorIdentSliceViolation {
	void m(@B String param) {}
}
// === end ===

// === case: empty_parens_non_ascii_name ===
class InputRedundantAnnotationSyntaxEmptyParensNonAsciiNameSliceViolation {
	@Ä
	int field;
}
// === end ===

// === case: empty_parens_on_annotation_type ===
@B
@interface InputRedundantAnnotationSyntaxEmptyParensOnAnnotationTypeSliceViolation {
	@B
	String key();
}
// === end ===

// === case: empty_parens_on_catch_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnCatchParamSliceViolation {
	void m() {
		try {
			throw new IllegalStateException();
		}
		catch (@B IllegalStateException e) {
			return;
		}
	}
}
// === end ===

// === case: empty_parens_on_class ===
@A
class InputRedundantAnnotationSyntaxEmptyParensOnClassSliceViolation {}
// === end ===

// === case: empty_parens_on_constructor ===
class InputRedundantAnnotationSyntaxEmptyParensOnConstructorSliceViolation {
	@B
	InputRedundantAnnotationSyntaxEmptyParensOnConstructorSliceViolation() {}
}
// === end ===

// === case: empty_parens_on_enum_and_constant ===
@B
enum InputRedundantAnnotationSyntaxEmptyParensOnEnumAndConstantSliceViolation {
	@B ALPHA
}
// === end ===

// === case: empty_parens_on_field ===
class InputRedundantAnnotationSyntaxEmptyParensOnFieldSliceViolation {
	@B
	int field;
}
// === end ===

// === case: empty_parens_on_for_each_var ===
class InputRedundantAnnotationSyntaxEmptyParensOnForEachVarSliceViolation {
	int count;

	void m(Iterable<String> list) {
		for (@B var s : list)
			count += s.length();
	}
}
// === end ===

// === case: empty_parens_on_interface ===
@B
interface InputRedundantAnnotationSyntaxEmptyParensOnInterfaceSliceViolation {}
// === end ===

// === case: empty_parens_on_lambda_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnLambdaParamSliceViolation {
	java.util.function.IntUnaryOperator op = (@B var x) -> x;
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

// === case: empty_parens_on_multi_catch ===
class InputRedundantAnnotationSyntaxEmptyParensOnMultiCatchSliceViolation {
	void m() {
		try {
			throw new IllegalStateException();
		}
		catch (@B IllegalStateException | IllegalArgumentException e) {
			return;
		}
	}
}
// === end ===

// === case: empty_parens_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnParamSliceViolation {
	void m(@B String param) {}
}
// === end ===

// === case: empty_parens_on_record ===
@B
record InputRedundantAnnotationSyntaxEmptyParensOnRecordSliceViolation(
		@B int x
) {}
// === end ===

// === case: empty_parens_on_try_resource ===
class InputRedundantAnnotationSyntaxEmptyParensOnTryResourceSliceViolation {
	void m(java.io.Closeable open) throws java.io.IOException {
		try (@B var in = open) {
			in.hashCode();
		}
	}
}
// === end ===

// === case: empty_parens_on_type_parameter ===
class InputRedundantAnnotationSyntaxEmptyParensOnTypeParameterSliceViolation {
	<@B T> void m(T t) {}
}
// === end ===

// === case: empty_parens_on_varargs_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnVarargsParamSliceViolation {
	void m(@B String... parts) {}
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

// === case: empty_parens_qualified_split_lines ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSplitLinesSliceViolation {
	@com.example
			.A
	int field;
}
// === end ===

// === case: empty_parens_space_after_at ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceAfterAtSliceViolation {
	@ B
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

// === case: empty_parens_tail_block_comment ===
class InputRedundantAnnotationSyntaxEmptyParensTailBlockCommentSliceViolation {
	@B/* c */int field;
}
// === end ===

// === case: empty_parens_text_block_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTextBlockDecoySliceViolation {
	@B
	String field = """
			@X()
			value = y""";
}
// === end ===

// === case: empty_parens_trailing_line_comment_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTrailingLineCommentDecoySliceViolation {
	@B // @X()
	int field;
}
// === end ===

// === case: empty_parens_type_use_array_level ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseArrayLevelSliceViolation {
	String @B [] field;
}
// === end ===

// === case: empty_parens_type_use_cast ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseCastSliceViolation {
	int m(Object o) {
		return ((@B String) o).length();
	}
}
// === end ===

// === case: empty_parens_type_use_instanceof ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseInstanceofSliceViolation {
	boolean m(Object o) {
		return o instanceof @B String s && !s.isEmpty();
	}
}
// === end ===

// === case: empty_parens_type_use_nested_generic ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseNestedGenericSliceViolation {
	java.util.Map<String, java.util.List<@B Integer>> field;
}
// === end ===

// === case: empty_parens_type_use_new ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseNewSliceViolation {
	Object field = new @B Object();
}
// === end ===

// === case: empty_parens_type_use_qualified_inner ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseQualifiedInnerSliceViolation {
	class Inner {}

	InputRedundantAnnotationSyntaxEmptyParensTypeUseQualifiedInnerSliceViolation.@B Inner field;
}
// === end ===

// === case: empty_parens_type_use_throws ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseThrowsSliceViolation {
	void m() throws @B Exception {}
}
// === end ===

// === case: empty_parens_type_use_type_arg ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseTypeArgSliceViolation {
	java.util.List<@B String> field;
}
// === end ===

// === case: empty_parens_type_use_wildcard_bound ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseWildcardBoundSliceViolation {
	java.util.List<? extends @B Number> field;
}
// === end ===

// === case: entry_state_annotation_after_block_comment_close ===
class InputRedundantAnnotationSyntaxEntryStateAnnotationAfterBlockCommentCloseSliceViolation {
	/*
	*/ @B // @X()
	int field;
}
// === end ===

// === case: explicit_value_already_over_limit ===
class InputRedundantAnnotationSyntaxExplicitValueAlreadyOverLimitSliceViolation {
	@A("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
	int field;
}
// === end ===

// === case: explicit_value_break_after_eq ===
class InputRedundantAnnotationSyntaxExplicitValueBreakAfterEqSliceViolation {
	@C(1)
	int field;
}
// === end ===

// === case: explicit_value_cast_value ===
class InputRedundantAnnotationSyntaxExplicitValueCastValueSliceViolation {
	@C((int) 1.5)
	int field;
}
// === end ===

// === case: explicit_value_char_literal_decoys ===
class InputRedundantAnnotationSyntaxExplicitValueCharLiteralDecoysSliceViolation {
	@C('(')
	int open;

	@C('"')
	int quote;

	@C('/')
	int slash;

	@C('\'')
	int tick;
}
// === end ===

// === case: explicit_value_comment_after_at ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterAtSliceViolation {
	@/* c */A("x")
	int field;
}
// === end ===

// === case: explicit_value_comment_after_at_multiline ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterAtMultilineSliceViolation {
	@/* keep */C(1)
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

// === case: explicit_value_escaped_quote_value ===
class InputRedundantAnnotationSyntaxExplicitValueEscapedQuoteValueSliceViolation {
	@A("a\"b")
	int field;
}
// === end ===

// === case: explicit_value_in_array_init ===
class InputRedundantAnnotationSyntaxExplicitValueInArrayInitSliceViolation {
	@G({@D(@B)})
	int field;
}
// === end ===

// === case: explicit_value_nested_both_value_keys ===
class InputRedundantAnnotationSyntaxExplicitValueNestedBothValueKeysSliceViolation {
	@F(@D(@B))
	int field;
}
// === end ===

// === case: explicit_value_nested_empty_parens ===
class InputRedundantAnnotationSyntaxExplicitValueNestedEmptyParensSliceViolation {
	@D(@B)
	int field;
}
// === end ===

// === case: explicit_value_nested_empty_parens_multiline ===
class InputRedundantAnnotationSyntaxExplicitValueNestedEmptyParensMultilineSliceViolation {
	@D(@B)
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

// === case: explicit_value_non_ascii_name ===
class InputRedundantAnnotationSyntaxExplicitValueNonAsciiNameSliceViolation {
	@Ä(1)
	int field;
}
// === end ===

// === case: explicit_value_numeric_edge_values ===
class InputRedundantAnnotationSyntaxExplicitValueNumericEdgeValuesSliceViolation {
	@Dbl(-0.0)
	int negativeZero;

	@Dbl(Double.NaN)
	int notANumber;

	@C(Integer.MIN_VALUE)
	int smallestInt;

	@C(-1)
	int unaryMinus;
}
// === end ===

// === case: explicit_value_numeric_literal_forms ===
class InputRedundantAnnotationSyntaxExplicitValueNumericLiteralFormsSliceViolation {
	@C(0b1010)
	int binary;

	@C(1_000)
	int grouped;

	@C(0xFF)
	int hex;
}
// === end ===

// === case: explicit_value_pair_split_lines ===
class InputRedundantAnnotationSyntaxExplicitValuePairSplitLinesSliceViolation {
	@C(1)
	int field;
}
// === end ===

// === case: explicit_value_qualified_constant ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedConstantSliceViolation {
	@C(Integer.MAX_VALUE)
	int field;
}
// === end ===

// === case: explicit_value_qualified_split_lines ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedSplitLinesSliceViolation {
	@com.example.A("x")
	int field;
}
// === end ===

// === case: explicit_value_qualified_split_lines_comment ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedSplitLinesCommentSliceViolation {
	@com.example // c
			.A("x")
	int field;
}
// === end ===

// === case: explicit_value_space_after_at ===
class InputRedundantAnnotationSyntaxExplicitValueSpaceAfterAtSliceViolation {
	@ A("x")
	int field;
}
// === end ===

// === case: explicit_value_string_contains_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueStringContainsValueDecoySliceViolation {
	@A("value = x")
	int field;
}
// === end ===

// === case: explicit_value_ternary_value ===
class InputRedundantAnnotationSyntaxExplicitValueTernaryValueSliceViolation {
	static final boolean FLAG = true;

	@C(FLAG ? 1 : 2)
	int field;
}
// === end ===

// === case: explicit_value_type_use_type_arg ===
class InputRedundantAnnotationSyntaxExplicitValueTypeUseTypeArgSliceViolation {
	java.util.List<@C(1) String> field;
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
// skip-reason: cannot simplify the annotation: joining its lines would comment out the text after a // comment
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenBlockSliceViolation {
	@B( // note
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_code ===
// skip-reason: cannot simplify the annotation: joining its lines would comment out the text after a // comment
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenCodeSliceViolation {
	@B( // note
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_multiline_block_comment_between ===
// skip-reason: cannot simplify the annotation: a block comment inside it runs across lines
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

// === case: multi_line_explicit_value_already_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueAlreadyOverLimitSliceViolation {
	@A(
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
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

// === case: multi_line_explicit_value_block_comment_before_value_fallback ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeValueFallbackSliceViolation {
	@A( // note
			/* keep */ "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before_value_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeValueOnValueLineSliceViolation {
	@A(/* keep */ "x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_crosses_before_value ===
// skip-reason: cannot simplify the annotation: a block comment inside it runs across lines
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentCrossesBeforeValueSliceViolation {
	@A(
			value = /*
			note */ "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_close_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCloseOnValueLineSliceViolation {
	@C(5)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_after_eq ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentAfterEqSliceViolation {
	@A(/* keep */ "x")
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_after_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentAfterValueSliceViolation {
	@A("x" /* keep */)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_pushes_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentPushesOverLimitSliceViolation {
	@A(
			/* a comment long enough that folding it in is what crosses the limit */
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_deep_tab_indent_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueDeepTabIndentOverLimitSliceViolation {
	static class Inner {
		static class Deeper {
			@A(
					"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
					+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
			)
			int field;
		}
	}
}
// === end ===

// === case: multi_line_explicit_value_line_comment_after_eq ===
// skip-reason: cannot simplify the annotation: joining its lines would comment out the text after a // comment
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentAfterEqSliceViolation {
	@A(
			value = // note
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_line_comment_after_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentAfterValueSliceViolation {
	@A(
			"x" // note
	)
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

// === case: multi_line_explicit_value_text_block ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueTextBlockSliceViolation {
	@A(
			"""
			x"""
	)
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