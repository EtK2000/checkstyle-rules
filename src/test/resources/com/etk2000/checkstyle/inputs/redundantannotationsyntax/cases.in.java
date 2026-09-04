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
	@Deprecated(/* pending */) // violation: Remove the empty parentheses from annotation '@Deprecated'.
	int value = 1;
}
// === end ===

// === case: empty_parens ===
class InputRedundantAnnotationSyntaxEmptyParensSliceViolation {
	@Override() // violation: Remove the empty parentheses from annotation '@Override'.
	public String toString() {
		return "";
	}
}
// === end ===

// === case: empty_parens_among_modifiers_embedded ===
class InputRedundantAnnotationSyntaxEmptyParensAmongModifiersEmbeddedSliceViolation {
	public @B() static int field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_before_declaration ===
class InputRedundantAnnotationSyntaxEmptyParensBeforeDeclarationSliceViolation {
	@Override() void f() {} // violation: Remove the empty parentheses from annotation '@Override'.
}
// === end ===

// === case: empty_parens_below_javadoc ===
class InputRedundantAnnotationSyntaxEmptyParensBelowJavadocSliceViolation {
	/** Doc. */
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_after_at ===
class InputRedundantAnnotationSyntaxEmptyParensCommentAfterAtSliceViolation {
	@/* c */B() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_before_paren_and_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBeforeParenAndInteriorSliceViolation {
	@B /* x */ (/* c */) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_between_name_paren ===
class InputRedundantAnnotationSyntaxEmptyParensCommentBetweenNameParenSliceViolation {
	@B /* x */ () // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorSliceViolation {
	@B(/* c */) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_multi ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorMultiSliceViolation {
	@B(/* a */ /* b */) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamSliceViolation {
	void m(@B(/* c */) String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_comment_interior_on_param_no_space ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorOnParamNoSpaceSliceViolation {
	void m(@B(/* c */)String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_comment_interior_padded ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorPaddedSliceViolation {
	@B(  /* c */  ) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_comment_interior_then_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensCommentInteriorThenAnnotationSliceViolation {
	void m(@A(/* c */)@B String param) {} // violation: Remove the empty parentheses from annotation '@A'.
}
// === end ===

// === case: empty_parens_empty_comment_interior ===
class InputRedundantAnnotationSyntaxEmptyParensEmptyCommentInteriorSliceViolation {
	@B(/**/) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_fold_over_limit ===
// skip-reason: cannot simplify the annotation: joining its lines would run past the line length limit
class InputRedundantAnnotationSyntaxEmptyParensFoldOverLimitSliceViolation {
	@B( /* a comment this long is what makes the folded line cross the limit aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa */ // violation: Remove the empty parentheses from annotation '@B'.
	) int field;
}
// === end ===

// === case: empty_parens_in_array_init ===
class InputRedundantAnnotationSyntaxEmptyParensInArrayInitSliceViolation {
	@E({@B(), @B}) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_in_array_init_last ===
class InputRedundantAnnotationSyntaxEmptyParensInArrayInitLastSliceViolation {
	@E({@B, @B()}) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_inside_anonymous_class ===
class InputRedundantAnnotationSyntaxEmptyParensInsideAnonymousClassSliceViolation {
	Object anon = new Object() {
		@B() // violation: Remove the empty parentheses from annotation '@B'.
		int field;
	};
}
// === end ===

// === case: empty_parens_multi_declarator ===
class InputRedundantAnnotationSyntaxEmptyParensMultiDeclaratorSliceViolation {
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	int first, second;
}
// === end ===

// === case: empty_parens_name_then_parens_next_line ===
class InputRedundantAnnotationSyntaxEmptyParensNameThenParensNextLineSliceViolation {
	@B // violation: Remove the empty parentheses from annotation '@B'.
	()
	int field;
}
// === end ===

// === case: empty_parens_nested_annotation_value ===
class InputRedundantAnnotationSyntaxEmptyParensNestedAnnotationValueSliceViolation {
	@D(@B()) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_no_separator_annotation ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorAnnotationSliceViolation {
	void m(@A()@B String param) {} // violation: Remove the empty parentheses from annotation '@A'.
}
// === end ===

// === case: empty_parens_no_separator_astral_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorAstralIdentSliceViolation {
	void m(@B()𝕏Box param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_no_separator_ident ===
class InputRedundantAnnotationSyntaxEmptyParensNoSeparatorIdentSliceViolation {
	void m(@B()String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_non_ascii_name ===
class InputRedundantAnnotationSyntaxEmptyParensNonAsciiNameSliceViolation {
	@Ä() // violation: Remove the empty parentheses from annotation '@Ä'.
	int field;
}
// === end ===

// === case: empty_parens_on_annotation_type ===
@B() // violation: Remove the empty parentheses from annotation '@B'.
@interface InputRedundantAnnotationSyntaxEmptyParensOnAnnotationTypeSliceViolation {
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	String key();
}
// === end ===

// === case: empty_parens_on_catch_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnCatchParamSliceViolation {
	void m() {
		try {
			throw new IllegalStateException();
		}
		catch (@B() IllegalStateException e) { // violation: Remove the empty parentheses from annotation '@B'.
			return;
		}
	}
}
// === end ===

// === case: empty_parens_on_class ===
@A() // violation: Remove the empty parentheses from annotation '@A'.
class InputRedundantAnnotationSyntaxEmptyParensOnClassSliceViolation {}
// === end ===

// === case: empty_parens_on_constructor ===
class InputRedundantAnnotationSyntaxEmptyParensOnConstructorSliceViolation {
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	InputRedundantAnnotationSyntaxEmptyParensOnConstructorSliceViolation() {}
}
// === end ===

// === case: empty_parens_on_enum_and_constant ===
@B() // violation: Remove the empty parentheses from annotation '@B'.
enum InputRedundantAnnotationSyntaxEmptyParensOnEnumAndConstantSliceViolation {
	@B() ALPHA // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_on_field ===
class InputRedundantAnnotationSyntaxEmptyParensOnFieldSliceViolation {
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_on_for_each_var ===
class InputRedundantAnnotationSyntaxEmptyParensOnForEachVarSliceViolation {
	int count;

	void m(Iterable<String> list) {
		for (@B() var s : list) // violation: Remove the empty parentheses from annotation '@B'.
			count += s.length();
	}
}
// === end ===

// === case: empty_parens_on_interface ===
@B() // violation: Remove the empty parentheses from annotation '@B'.
interface InputRedundantAnnotationSyntaxEmptyParensOnInterfaceSliceViolation {}
// === end ===

// === case: empty_parens_on_lambda_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnLambdaParamSliceViolation {
	java.util.function.IntUnaryOperator op = (@B() var x) -> x; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_on_local ===
class InputRedundantAnnotationSyntaxEmptyParensOnLocalSliceViolation {
	void m() {
		@B() // violation: Remove the empty parentheses from annotation '@B'.
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
		catch (@B() IllegalStateException | IllegalArgumentException e) { // violation: Remove the empty parentheses from annotation '@B'.
			return;
		}
	}
}
// === end ===

// === case: empty_parens_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnParamSliceViolation {
	void m(@B() String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_on_record ===
@B() // violation: Remove the empty parentheses from annotation '@B'.
record InputRedundantAnnotationSyntaxEmptyParensOnRecordSliceViolation(
		@B() int x // violation: Remove the empty parentheses from annotation '@B'.
) {}
// === end ===

// === case: empty_parens_on_try_resource ===
class InputRedundantAnnotationSyntaxEmptyParensOnTryResourceSliceViolation {
	void m(java.io.Closeable open) throws java.io.IOException {
		try (@B() var in = open) { // violation: Remove the empty parentheses from annotation '@B'.
			in.hashCode();
		}
	}
}
// === end ===

// === case: empty_parens_on_type_parameter ===
class InputRedundantAnnotationSyntaxEmptyParensOnTypeParameterSliceViolation {
	<@B() T> void m(T t) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_on_varargs_param ===
class InputRedundantAnnotationSyntaxEmptyParensOnVarargsParamSliceViolation {
	void m(@B() String... parts) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_qualified ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSliceViolation {
	@com.example.A() // violation: Remove the empty parentheses from annotation '@A'.
	int field;
}
// === end ===

// === case: empty_parens_qualified_spaced ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSpacedSliceViolation {
	@com.example.A () // violation: Remove the empty parentheses from annotation '@A'.
	int field;
}
// === end ===

// === case: empty_parens_qualified_split_lines ===
class InputRedundantAnnotationSyntaxEmptyParensQualifiedSplitLinesSliceViolation {
	@com.example // violation: Remove the empty parentheses from annotation '@A'.
			.A()
	int field;
}
// === end ===

// === case: empty_parens_space_after_at ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceAfterAtSliceViolation {
	@ B() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_space_before ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeSliceViolation {
	@B () // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_space_before_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBeforeOnParamSliceViolation {
	void m(@B () String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_space_both ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceBothSliceViolation {
	@B ( ) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_space_inside ===
class InputRedundantAnnotationSyntaxEmptyParensSpaceInsideSliceViolation {
	@B( ) // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_stacked_both_fire_on_param ===
class InputRedundantAnnotationSyntaxEmptyParensStackedBothFireOnParamSliceViolation {
	void m(@A() @B() String param) {} // violation: Remove the empty parentheses from annotation '@A'. // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_string_interior_survives_sibling_fix ===
class InputRedundantAnnotationSyntaxEmptyParensStringInteriorSurvivesSiblingFixSliceViolation {
	void m(@A("") @B() String param) {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_tail_block_comment ===
class InputRedundantAnnotationSyntaxEmptyParensTailBlockCommentSliceViolation {
	@B()/* c */int field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_text_block_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTextBlockDecoySliceViolation {
	@B() // violation: Remove the empty parentheses from annotation '@B'.
	String field = """
			@X()
			value = y""";
}
// === end ===

// === case: empty_parens_trailing_line_comment_decoy ===
class InputRedundantAnnotationSyntaxEmptyParensTrailingLineCommentDecoySliceViolation {
	@B () // @X() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: empty_parens_type_use_array_level ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseArrayLevelSliceViolation {
	String @B() [] field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_cast ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseCastSliceViolation {
	int m(Object o) {
		return ((@B() String) o).length(); // violation: Remove the empty parentheses from annotation '@B'.
	}
}
// === end ===

// === case: empty_parens_type_use_instanceof ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseInstanceofSliceViolation {
	boolean m(Object o) {
		return o instanceof @B() String s && !s.isEmpty(); // violation: Remove the empty parentheses from annotation '@B'.
	}
}
// === end ===

// === case: empty_parens_type_use_nested_generic ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseNestedGenericSliceViolation {
	java.util.Map<String, java.util.List<@B() Integer>> field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_new ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseNewSliceViolation {
	Object field = new @B() Object(); // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_qualified_inner ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseQualifiedInnerSliceViolation {
	class Inner {}

	InputRedundantAnnotationSyntaxEmptyParensTypeUseQualifiedInnerSliceViolation.@B() Inner field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_throws ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseThrowsSliceViolation {
	void m() throws @B() Exception {} // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_type_arg ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseTypeArgSliceViolation {
	java.util.List<@B() String> field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: empty_parens_type_use_wildcard_bound ===
class InputRedundantAnnotationSyntaxEmptyParensTypeUseWildcardBoundSliceViolation {
	java.util.List<? extends @B() Number> field; // violation: Remove the empty parentheses from annotation '@B'.
}
// === end ===

// === case: entry_state_annotation_after_block_comment_close ===
class InputRedundantAnnotationSyntaxEntryStateAnnotationAfterBlockCommentCloseSliceViolation {
	/*
	*/ @B() // @X() // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: explicit_value_already_over_limit ===
class InputRedundantAnnotationSyntaxExplicitValueAlreadyOverLimitSliceViolation {
	@A(value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_break_after_eq ===
class InputRedundantAnnotationSyntaxExplicitValueBreakAfterEqSliceViolation {
	@C(value = // violation: Remove the redundant 'value =' from annotation '@C'.
			1)
	int field;
}
// === end ===

// === case: explicit_value_cast_value ===
class InputRedundantAnnotationSyntaxExplicitValueCastValueSliceViolation {
	@C(value = (int) 1.5) // violation: Remove the redundant 'value =' from annotation '@C'.
	int field;
}
// === end ===

// === case: explicit_value_char_literal_decoys ===
class InputRedundantAnnotationSyntaxExplicitValueCharLiteralDecoysSliceViolation {
	@C(value = '(') // violation: Remove the redundant 'value =' from annotation '@C'.
	int open;

	@C(value = '"') // violation: Remove the redundant 'value =' from annotation '@C'.
	int quote;

	@C(value = '/') // violation: Remove the redundant 'value =' from annotation '@C'.
	int slash;

	@C(value = '\'') // violation: Remove the redundant 'value =' from annotation '@C'.
	int tick;
}
// === end ===

// === case: explicit_value_comment_after_at ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterAtSliceViolation {
	@/* c */A(value = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_after_at_multiline ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterAtMultilineSliceViolation {
	@/* keep */C( // violation: Remove the redundant 'value =' from annotation '@C'.
			value = 1
	)
	int field;
}
// === end ===

// === case: explicit_value_comment_after_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAfterEqSliceViolation {
	@A(value = /* keep */ "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_all_gaps ===
class InputRedundantAnnotationSyntaxExplicitValueCommentAllGapsSliceViolation {
	@A(/* a */ value /* b */ = /* c */ "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_before_value ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBeforeValueSliceViolation {
	@A(/* keep */ value = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_between_value_and_eq ===
class InputRedundantAnnotationSyntaxExplicitValueCommentBetweenValueAndEqSliceViolation {
	@A(value /* keep */ = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_comment_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueCommentValueDecoySliceViolation {
	@A(/* value = */ value = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_escaped_quote_value ===
class InputRedundantAnnotationSyntaxExplicitValueEscapedQuoteValueSliceViolation {
	@A(value = "a\"b") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_in_array_init ===
class InputRedundantAnnotationSyntaxExplicitValueInArrayInitSliceViolation {
	@G({@D(value = @B)}) // violation: Remove the redundant 'value =' from annotation '@D'.
	int field;
}
// === end ===

// === case: explicit_value_nested_both_value_keys ===
class InputRedundantAnnotationSyntaxExplicitValueNestedBothValueKeysSliceViolation {
	@F(value = @D(value = @B)) // violation: Remove the redundant 'value =' from annotation '@F'. // violation: Remove the redundant 'value =' from annotation '@D'.
	int field;
}
// === end ===

// === case: explicit_value_nested_empty_parens ===
class InputRedundantAnnotationSyntaxExplicitValueNestedEmptyParensSliceViolation {
	@D(value = @B()) // violation: Remove the redundant 'value =' from annotation '@D'. // violation: Remove the empty parentheses from annotation '@B'.
	int field;
}
// === end ===

// === case: explicit_value_nested_empty_parens_multiline ===
class InputRedundantAnnotationSyntaxExplicitValueNestedEmptyParensMultilineSliceViolation {
	@D(value = @B( // violation: Remove the redundant 'value =' from annotation '@D'. // violation: Remove the empty parentheses from annotation '@B'.
	))
	int field;
}
// === end ===

// === case: explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesSliceViolation {
	@A(value="x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_no_spaces_qualified ===
class InputRedundantAnnotationSyntaxExplicitValueNoSpacesQualifiedSliceViolation {
	@com.example.A(value="x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_non_ascii_name ===
class InputRedundantAnnotationSyntaxExplicitValueNonAsciiNameSliceViolation {
	@Ä(value = 1) // violation: Remove the redundant 'value =' from annotation '@Ä'.
	int field;
}
// === end ===

// === case: explicit_value_numeric_edge_values ===
class InputRedundantAnnotationSyntaxExplicitValueNumericEdgeValuesSliceViolation {
	@Dbl(value = -0.0) // violation: Remove the redundant 'value =' from annotation '@Dbl'.
	int negativeZero;

	@Dbl(value = Double.NaN) // violation: Remove the redundant 'value =' from annotation '@Dbl'.
	int notANumber;

	@C(value = Integer.MIN_VALUE) // violation: Remove the redundant 'value =' from annotation '@C'.
	int smallestInt;

	@C(value = -1) // violation: Remove the redundant 'value =' from annotation '@C'.
	int unaryMinus;
}
// === end ===

// === case: explicit_value_numeric_literal_forms ===
class InputRedundantAnnotationSyntaxExplicitValueNumericLiteralFormsSliceViolation {
	@C(value = 0b1010) // violation: Remove the redundant 'value =' from annotation '@C'.
	int binary;

	@C(value = 1_000) // violation: Remove the redundant 'value =' from annotation '@C'.
	int grouped;

	@C(value = 0xFF) // violation: Remove the redundant 'value =' from annotation '@C'.
	int hex;
}
// === end ===

// === case: explicit_value_pair_split_lines ===
class InputRedundantAnnotationSyntaxExplicitValuePairSplitLinesSliceViolation {
	@C(value // violation: Remove the redundant 'value =' from annotation '@C'.
			= 1)
	int field;
}
// === end ===

// === case: explicit_value_qualified_constant ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedConstantSliceViolation {
	@C(value = Integer.MAX_VALUE) // violation: Remove the redundant 'value =' from annotation '@C'.
	int field;
}
// === end ===

// === case: explicit_value_qualified_split_lines ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedSplitLinesSliceViolation {
	@com.example // violation: Remove the redundant 'value =' from annotation '@A'.
			.A(value = "x")
	int field;
}
// === end ===

// === case: explicit_value_qualified_split_lines_comment ===
class InputRedundantAnnotationSyntaxExplicitValueQualifiedSplitLinesCommentSliceViolation {
	@com.example // c // violation: Remove the redundant 'value =' from annotation '@A'.
			.A(value = "x")
	int field;
}
// === end ===

// === case: explicit_value_space_after_at ===
class InputRedundantAnnotationSyntaxExplicitValueSpaceAfterAtSliceViolation {
	@ A(value = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_string_contains_value_decoy ===
class InputRedundantAnnotationSyntaxExplicitValueStringContainsValueDecoySliceViolation {
	@A(value = "value = x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: explicit_value_ternary_value ===
class InputRedundantAnnotationSyntaxExplicitValueTernaryValueSliceViolation {
	static final boolean FLAG = true;

	@C(value = FLAG ? 1 : 2) // violation: Remove the redundant 'value =' from annotation '@C'.
	int field;
}
// === end ===

// === case: explicit_value_type_use_type_arg ===
class InputRedundantAnnotationSyntaxExplicitValueTypeUseTypeArgSliceViolation {
	java.util.List<@C(value = 1) String> field; // violation: Remove the redundant 'value =' from annotation '@C'.
}
// === end ===

// === case: multi_line_empty_parens ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_blank_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlankBetweenSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.

	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_block_comment_middle ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensBlockCommentMiddleSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	/* c */
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_code_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCodeAfterCloseSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_before_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBeforeCloseSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	/* c */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_between ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentBetweenSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	// c
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_and_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenAndCloseSliceViolation {
	@B( /* c */ // violation: Remove the empty parentheses from annotation '@B'.
	) /* d */
	int field;
}
// === end ===

// === case: multi_line_empty_parens_comment_open_tail_and_close_prefix ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCommentOpenTailAndClosePrefixSliceViolation {
	@B( /* c */ // violation: Remove the empty parentheses from annotation '@B'.
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_content_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensContentAfterCloseSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	) // comment
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenSliceViolation {
	@B( // note // violation: Remove the empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_block ===
// skip-reason: cannot simplify the annotation: joining its lines would comment out the text after a // comment
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenBlockSliceViolation {
	@B( // note // violation: Remove the empty parentheses from annotation '@B'.
	/* d */)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_line_comment_open_then_code ===
// skip-reason: cannot simplify the annotation: joining its lines would comment out the text after a // comment
class InputRedundantAnnotationSyntaxMultiLineEmptyParensLineCommentOpenThenCodeSliceViolation {
	@B( // note // violation: Remove the empty parentheses from annotation '@B'.
	) int field;
}
// === end ===

// === case: multi_line_empty_parens_multiline_block_comment_between ===
// skip-reason: cannot simplify the annotation: a block comment inside it runs across lines
class InputRedundantAnnotationSyntaxMultiLineEmptyParensMultilineBlockCommentBetweenSliceViolation {
	@B( // violation: Remove the empty parentheses from annotation '@B'.
	/*
	x
	*/
	)
	int field;
}
// === end ===

// === case: multi_line_empty_parens_open_line_annotation_decoy ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensOpenLineAnnotationDecoySliceViolation {
	@B( /* @X() */ // violation: Remove the empty parentheses from annotation '@B'.
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_already_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueAlreadyOverLimitSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_annotation ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueAnnotationSliceViolation {
	@D( // violation: Remove the redundant 'value =' from annotation '@D'.
			value = @B
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_blank_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlankBeforeSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.

			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			/* note */
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before_value_fallback ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeValueFallbackSliceViolation {
	@A( // note // violation: Remove the redundant 'value =' from annotation '@A'.
			/* keep */ value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_before_value_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentBeforeValueOnValueLineSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			/* keep */ value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_block_comment_crosses_before_value ===
// skip-reason: cannot simplify the annotation: a block comment inside it runs across lines
class InputRedundantAnnotationSyntaxMultiLineExplicitValueBlockCommentCrossesBeforeValueSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = /*
			note */ "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_close_on_value_line ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCloseOnValueLineSliceViolation {
	@C( // violation: Remove the redundant 'value =' from annotation '@C'.
			value = 5)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_after_eq ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentAfterEqSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = /* keep */ "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_after_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentAfterValueSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x" /* keep */
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_comment_pushes_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueCommentPushesOverLimitSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			/* a comment long enough that folding it in is what crosses the limit */
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_deep_tab_indent_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueDeepTabIndentOverLimitSliceViolation {
	static class Inner {
		static class Deeper {
			@A( // violation: Remove the redundant 'value =' from annotation '@A'.
					value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
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
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = // note
			"x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_line_comment_after_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentAfterValueSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x" // note
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_line_comment_in_span ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueLineCommentInSpanSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			// note
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_comment_decoy ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineCommentDecoySliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
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
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "a"
			+ "b"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_multiline_expr_overflow ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueMultilineExprOverflowSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_no_spaces ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueNoSpacesSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value="x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockCommentSliceViolation {
	@A( /* c */ // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_block_then_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineBlockThenLineCommentSliceViolation {
	@A( /* c */ // note // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_open_line_comment ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueOpenLineCommentSliceViolation {
	@A( // note // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_paren_value ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueParenValueSliceViolation {
	@C( // violation: Remove the redundant 'value =' from annotation '@C'.
			value = (1 + 2) * 3
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_text_block ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueTextBlockSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = """
			x"""
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_at_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthAtLimitSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_width_over_limit ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWidthOverLimitSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
			+ "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
	)
	int field;
}
// === end ===

// === case: multi_line_explicit_value_with_array ===
class InputRedundantAnnotationSyntaxMultiLineExplicitValueWithArraySliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = {"x", "y"}
	)
	int field;
}
// === end ===

// === case: multi_line_redundant_value ===
class InputRedundantAnnotationSyntaxMultiLineRedundantValueSliceViolation {
	@A( // violation: Remove the redundant 'value =' from annotation '@A'.
			value = "x"
	)
	int field;
}
// === end ===

// === case: redundant_value_on_param ===
class InputRedundantAnnotationSyntaxRedundantValueOnParamSliceViolation {
	void m(@A(value = "x") String param) {} // violation: Remove the redundant 'value =' from annotation '@A'.
}
// === end ===

// === case: redundant_value_with_annotation_value ===
class InputRedundantAnnotationSyntaxRedundantValueWithAnnotationValueSliceViolation {
	@D(value = @B) // violation: Remove the redundant 'value =' from annotation '@D'.
	int field;
}
// === end ===

// === case: redundant_value_with_array ===
class InputRedundantAnnotationSyntaxRedundantValueWithArraySliceViolation {
	@A(value = {"x", "y"}) // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===

// === case: redundant_value_with_numeric ===
class InputRedundantAnnotationSyntaxRedundantValueWithNumericSliceViolation {
	@C(value = 42) // violation: Remove the redundant 'value =' from annotation '@C'.
	int field;
}
// === end ===

// === case: redundant_value_with_string ===
class InputRedundantAnnotationSyntaxRedundantValueWithStringSliceViolation {
	@A(value = "x") // violation: Remove the redundant 'value =' from annotation '@A'.
	int field;
}
// === end ===