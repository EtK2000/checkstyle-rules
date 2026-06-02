package com.etk2000.checkstyle.inputs.arraytypestyle;

// === case: annotated_field ===
class InputArrayTypeStyleAnnotatedFieldSliceViolation {
	@Deprecated
	int annotatedField[]; // violation: Array brackets must be on the type, not after 'annotatedField'.
}
// === end ===

// === case: annotated_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedParamSliceViolation {
	void annotatedParam(@Deprecated int p[]) { // violation: Array brackets must be on the type, not after 'p'.
		p[0] = 1;
	}
}
// === end ===

// === case: annotation_argument_parens_not_treated_as_method_parens ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotationArgumentParensNotTreatedAsMethodParensSliceViolation {
	@interface Ann {
		String value();
	}

	void m(int[] a, boolean cond) {
		for (@Ann("x") int x[] = a, y = 1; cond; ++y) // violation: Array brackets must be on the type, not after 'x'.
			break;
	}
}
// === end ===

// === case: block_comment_after_brackets_multi_var_next_line ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentAfterBracketsMultiVarNextLineSliceViolation {
	int x[] /* c // violation: Array brackets must be on the type, not after 'x'.
	*/, y;
}
// === end ===

// === case: block_comment_before_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentBeforeModifierSliceViolation {
	/* doc */ final int x[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: block_comment_containing_open_paren_before_bracket ===
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentContainingOpenParenBeforeBracketSliceViolation {
	/* ( */ int x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: bounded_wildcard_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleBoundedWildcardCStyleSliceViolation {
	List<? super Integer> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: char_literal_comma_in_multi_var_init ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralCommaInMultiVarInitSliceViolation {
	void m(char c, char a, char b) {
		final char x[] = (c == ',' ? a : b), y = 'z'; // violation: Array brackets must be on the type, not after 'x'.
	}
}
// === end ===

// === case: char_literal_escaped_quote_multi_var ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralEscapedQuoteMultiVarSliceViolation {
	char x[] = '\'', y = 'z'; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: char_literal_with_paren_in_annotation_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralWithParenInAnnotationParamSliceViolation {
	@interface A {
		char value();
	}

	int m(@A('(') int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: close_paren_in_char_literal_on_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCloseParenInCharLiteralOnMethodReturnSliceViolation {
	@interface A {
		char value();
	}

	int m(@A(')') int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: close_paren_in_string_literal_on_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCloseParenInStringLiteralOnMethodReturnSliceViolation {
	@interface A {
		String value();
	}

	int m(@A(")") int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: comma_after_brackets_in_variable_decl ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCommaAfterBracketsInVariableDeclSliceViolation {
	int x[], y; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: comma_in_trailing_line_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCommaInTrailingLineCommentDoesNotBlockSliceViolation {
	int x[] = {1}; // hello, world // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: comment_after_bracket_before_assignment ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentAfterBracketBeforeAssignmentSliceViolation {
	int x[] /* note */ = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: comment_between_comma_and_ident_multi_var ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentBetweenCommaAndIdentMultiVarSliceViolation {
	void m(int[] a) {
		final int i = 0, /* note */ x[] = a; // violation: Array brackets must be on the type, not after 'x'.
	}
}
// === end ===

// === case: comment_between_ident_and_bracket ===
// skip-reason: comment between the identifier and the array brackets
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentBetweenIdentAndBracketSliceViolation {
	int x /* note */ []; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: compound_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCompoundCStyleSliceViolation {
	int x[][]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: compound_local ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCompoundLocalSliceViolation {
	void compoundLocal() {
		final int lc[][] = {{1}}; // violation: Array brackets must be on the type, not after 'lc'.
		lc[0][0] = 1;
	}
}
// === end ===

// === case: ctor_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCtorParamSliceViolation {
	InputArrayTypeStyleCtorParamSliceViolation(int ctorParam[]) {} // violation: Array brackets must be on the type, not after 'ctorParam'.
}
// === end ===

// === case: escaped_apostrophe_in_char_literal ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedApostropheInCharLiteralSliceViolation {
	@interface A {
		char value();
	}

	int m(char c, @A('\'') int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: escaped_backslash_in_annotation_string_scans_correctly ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedBackslashInAnnotationStringScansCorrectlySliceViolation {
	@interface A {
		String value();
	}

	int m(@A("\\") int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: escaped_backslash_in_string_success_single_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedBackslashInStringSuccessSingleLineSliceViolation {
	String s[] = "a\\b"; // violation: Array brackets must be on the type, not after 's'.
}
// === end ===

// === case: escaped_quote_inside_annotation_string_scans_correctly ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedQuoteInsideAnnotationStringScansCorrectlySliceViolation {
	@interface A {
		String value();
	}

	int m(@A("\"") int x)[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: final_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleFinalModifierSliceViolation {
	final int x[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: for_each_method_not_treated_as_for_keyword ===
@SuppressWarnings("unused")
class InputArrayTypeStyleForEachMethodNotTreatedAsForKeywordSliceViolation {
	void forEach(int x[]) {} // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: for_loop_multi_var ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleForLoopMultiVarSliceViolation {
	void m(int[] a, boolean cond) {
		for (int x[] = a, y = 1; cond; ++y) // violation: Array brackets must be on the type, not after 'x'.
			break;
	}
}
// === end ===

// === case: for_loop_single_var_fixed ===
@SuppressWarnings("unused")
class InputArrayTypeStyleForLoopSingleVarFixedSliceViolation {
	void m(int[] a, int step) {
		for (int x[] = a; x.length < 10; step += 1) // violation: Array brackets must be on the type, not after 'x'.
			break;
	}
}
// === end ===

// === case: generic_field ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericFieldSliceViolation {
	List<String> gs[]; // violation: Array brackets must be on the type, not after 'gs'.
}
// === end ===

// === case: generic_method_multi_param_lambda ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaSliceViolation {
	BiConsumer<X, Y> b = (int a[], int y) -> {}; // violation: Array brackets must be on the type, not after 'a'.
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_last ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnLastSliceViolation {
	BiConsumer<X, Y> b = (int a, int y[]) -> {}; // violation: Array brackets must be on the type, not after 'y'.
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_middle ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnMiddleSliceViolation {
	TriConsumer<X, Y, Z> t = (int a, int b[], int c) -> {}; // violation: Array brackets must be on the type, not after 'b'.
}
// === end ===

// === case: generic_method_return_type ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodReturnTypeSliceViolation {
	List<String> m()[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: generic_return_nested_type_args ===
// imports: java.util.List
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericReturnNestedTypeArgsSliceViolation {
	Map<String, List<Integer>> m()[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: generic_type_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericTypeCStyleSliceViolation {
	List<String> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: inner_record_component ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInnerRecordComponentSliceViolation {
	record InnerRec(int comp[]) {} // violation: Array brackets must be on the type, not after 'comp'.
}
// === end ===

// === case: instance_field ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInstanceFieldSliceViolation {
	int ib[]; // violation: Array brackets must be on the type, not after 'ib'.
}
// === end ===

// === case: instance_field_two_dim ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInstanceFieldTwoDimSliceViolation {
	int ic[][]; // violation: Array brackets must be on the type, not after 'ic'.
}
// === end ===

// === case: local_simple ===
@SuppressWarnings("unused")
class InputArrayTypeStyleLocalSimpleSliceViolation {
	void simpleLocal() {
		final int lb[] = {1}; // violation: Array brackets must be on the type, not after 'lb'.
		lb[0] = 0;
	}
}
// === end ===

// === case: main ===
@SuppressWarnings("unused")
class InputArrayTypeStyleViolation {
	void multiVarMixed() {
		final int gamma[] = {1}, delta = 0; // violation: Array brackets must be on the type, not after 'gamma'.
		gamma[0] = delta;
	}

	void multiVarMixedReversed() {
		final int epsilon = 0, zeta[] = {1}; // violation: Array brackets must be on the type, not after 'zeta'.
		zeta[0] = epsilon;
	}

	void multiVarSame() {
		final int alpha[] = {1}, beta[] = {2}; // violation: Array brackets must be on the type, not after 'alpha'. // violation: Array brackets must be on the type, not after 'beta'.
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: method_compound_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodCompoundParamSliceViolation {
	void methodCompoundParam(int c[][]) { // violation: Array brackets must be on the type, not after 'c'.
		c[0][0] = 1;
	}
}
// === end ===

// === case: method_mixed_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMixedReturnSliceViolation {
	int methodMixedReturn()[][] { // violation: Array brackets must be on the type, not after 'methodMixedReturn'.
		return null;
	}
}
// === end ===

// === case: method_multi_param_c_style_on_last ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCStyleOnLastSliceViolation {
	void m(int x, int y[]) {} // violation: Array brackets must be on the type, not after 'y'.
}
// === end ===

// === case: method_multi_param_c_style_on_middle ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCStyleOnMiddleSliceViolation {
	void m(int a, int b[], int c) {} // violation: Array brackets must be on the type, not after 'b'.
}
// === end ===

// === case: method_multi_param_comma_separated ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCommaSeparatedSliceViolation {
	void m(int a[], int b) {} // violation: Array brackets must be on the type, not after 'a'.
}
// === end ===

// === case: method_return_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnCStyleSliceViolation {
	int methodReturnCStyle()[] { // violation: Array brackets must be on the type, not after 'methodReturnCStyle'.
		return null;
	}
}
// === end ===

// === case: method_return_type_abstract_semicolon ===
@SuppressWarnings("unused")
abstract class InputArrayTypeStyleMethodReturnTypeAbstractSemicolonSliceViolation {
	abstract int m()[]; // violation: Array brackets must be on the type, not after 'm'.
}
// === end ===

// === case: method_return_type_array_prefixed ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeArrayPrefixedSliceViolation {
	int[] m()[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: method_return_type_brace_body ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeBraceBodySliceViolation {
	int method()[] { // violation: Array brackets must be on the type, not after 'method'.
		return null;
	}
}
// === end ===

// === case: method_return_type_compound ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeCompoundSliceViolation {
	int method()[][] { // violation: Array brackets must be on the type, not after 'method'.
		return null;
	}
}
// === end ===

// === case: method_return_type_with_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeWithThrowsSliceViolation {
	int method()[] throws Exception { // violation: Array brackets must be on the type, not after 'method'.
		return null;
	}
}
// === end ===

// === case: method_return_with_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnWithThrowsSliceViolation {
	int methodReturnWithThrows()[] throws Exception { // violation: Array brackets must be on the type, not after 'methodReturnWithThrows'.
		return null;
	}
}
// === end ===

// === case: method_simple_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodSimpleParamSliceViolation {
	void methodSimpleParam(int b[]) { // violation: Array brackets must be on the type, not after 'b'.
		b[0] = 1;
	}
}
// === end ===

// === case: mixed_declaration ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMixedDeclarationSliceViolation {
	void mixedDeclaration() {
		final int[] mx[] = {{1}}; // violation: Array brackets must be on the type, not after 'mx'.
		mx[0][0] = 1;
	}
}
// === end ===

// === case: mixed_java_and_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMixedJavaAndCStyleSliceViolation {
	int[] x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_abstract_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineAbstractMethodReturnSliceViolation {
	int method()
			[]; // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_block_comment_spans_across_lines_followed_by_comma ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansAcrossLinesFollowedByCommaSliceViolation {
	int x
		[] /* // violation: Array brackets must be on the type, not after 'x'.
fake */, y;
}
// === end ===

// === case: multi_line_block_comment_spans_across_lines_with_fake_comma ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansAcrossLinesWithFakeCommaSliceViolation {
	int x
		[] /* // violation: Array brackets must be on the type, not after 'x'.
fake , */;
}
// === end ===

// === case: multi_line_block_comment_spans_lines_containing_comma ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansLinesContainingCommaSliceViolation {
	int x[] /* // violation: Array brackets must be on the type, not after 'x'.
fake , inside
*/ , y;
}
// === end ===

// === case: multi_line_bracket_line_has_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBracketLineHasInitializerSliceViolation {
	int x
			[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_bracket_line_has_method_body ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBracketLineHasMethodBodySliceViolation {
	int method()
			[] { return null; } // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_final_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineFinalModifierSliceViolation {
	final int x
			[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_final_with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineFinalWithAnnotationSliceViolation {
	@Deprecated
	final int x
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_generic_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineGenericMethodReturnSliceViolation {
	<T> T method()
			[]; // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_line_comment_before_comma_on_next_line ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineLineCommentBeforeCommaOnNextLineSliceViolation {
	int x
			[] = 1 // comment // violation: Array brackets must be on the type, not after 'x'.
	, y = 3;
}
// === end ===

// === case: multi_line_line_comment_triple_quote_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineLineCommentTripleQuoteDoesNotBlockSliceViolation {
	int x
			[] // """ // violation: Array brackets must be on the type, not after 'x'.
	= {1};
}
// === end ===

// === case: multi_line_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnSliceViolation {
	int method()
			[] // violation: Array brackets must be on the type, not after 'method'.
			{ return null; }
}
// === end ===

// === case: multi_line_method_return_with_comment_on_prev_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnWithCommentOnPrevLineSliceViolation {
	int method() // comment
			[]; // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_method_return_with_multi_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnWithMultiThrowsSliceViolation {
	int method()
			[] throws E1, E2 { return null; } // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_multi_var ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarSliceViolation {
	int alpha, beta
			[]; // violation: Array brackets must be on the type, not after 'beta'.
}
// === end ===

// === case: multi_line_multi_var_on_bracket_line ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarOnBracketLineSliceViolation {
	int x
			[], y; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_multi_var_on_following_line ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarOnFollowingLineSliceViolation {
	int x
			[] // violation: Array brackets must be on the type, not after 'x'.
	, y;
}
// === end ===

// === case: multi_line_multi_var_spanning_brace_init ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarSpanningBraceInitSliceViolation {
	int x
			[] = {1, // violation: Array brackets must be on the type, not after 'x'.
			2}, y = 3;
}
// === end ===

// === case: multi_line_simple_var ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineSimpleVarSliceViolation {
	int x
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_string_literal_init_with_comma ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineStringLiteralInitWithCommaSliceViolation {
	String x
			[] = ",", y = "z"; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_type_use_annotation_on_prev_line ===
// skip-reason: type-use annotation before the array brackets
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineTypeUseAnnotationOnPrevLineSliceViolation {
	@interface Anno {}

	int x @Anno
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithAnnotationSliceViolation {
	@Deprecated
	int x
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_with_block_comment_on_prev_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithBlockCommentOnPrevLineSliceViolation {
	int x /* note */
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_with_generic_prev_return ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithGenericPrevReturnSliceViolation {
	List<String> method()
			[]; // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_with_generic_prev_var ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithGenericPrevVarSliceViolation {
	List<String> x
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_with_java_style_array_prev_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithJavaStyleArrayPrevReturnSliceViolation {
	int[] method()
			[]; // violation: Array brackets must be on the type, not after 'method'.
}
// === end ===

// === case: multi_line_with_java_style_array_prev_var ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithJavaStyleArrayPrevVarSliceViolation {
	int[] x
			[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_line_with_string_literal_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithStringLiteralInitializerSliceViolation {
	String x
			[] = "abc"; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_var_brace_initializer ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBraceInitializerSliceViolation {
	int x[] = {1, 2}, y = 3; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_var_bracket_index_init ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBracketIndexInitSliceViolation {
	void m(int[] arr, int i) {
		final int x[] = arr[i], y = 3; // violation: Array brackets must be on the type, not after 'x'.
	}
}
// === end ===

// === case: multi_var_function_call_init ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarFunctionCallInitSliceViolation {
	int[] foo(int a, int b) {
		return null;
	}

	void m(int a, int b) {
		final int x[] = foo(a, b), y = 3; // violation: Array brackets must be on the type, not after 'x'.
	}
}
// === end ===

// === case: multi_var_prev ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarPrevSliceViolation {
	int alpha, beta[]; // violation: Array brackets must be on the type, not after 'beta'.
}
// === end ===

// === case: multi_var_spanning_brace_init_on_single_line ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSpanningBraceInitOnSingleLineSliceViolation {
	void m() {
		final int x[] = {1, // violation: Array brackets must be on the type, not after 'x'.
				2}, y = 3;
	}
}
// === end ===

// === case: multi_var_string_escaped_quote ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarStringEscapedQuoteSliceViolation {
	String x[] = "a\"b", y = "z"; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_var_string_literal_init ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarStringLiteralInitSliceViolation {
	String x[] = ",", y = "z"; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multi_var_with_initializer ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarWithInitializerSliceViolation {
	int gamma[] = {1}, delta = 2; // violation: Array brackets must be on the type, not after 'gamma'.
}
// === end ===

// === case: multiple_modifiers ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultipleModifiersSliceViolation {
	public static final int x[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: multiple_type_args_c_style ===
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleMultipleTypeArgsCStyleSliceViolation {
	Map<String, Integer> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: nested_generic_c_style ===
// imports: java.util.List
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleNestedGenericCStyleSliceViolation {
	Map<String, List<Integer>> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: parameter_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleParameterCStyleSliceViolation {
	void m(int x[]) {} // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: record_component_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleRecordComponentCStyleSliceViolation {
	record R(int x[]) {} // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: record_multi_component_comma_separated ===
@SuppressWarnings("unused")
class InputArrayTypeStyleRecordMultiComponentCommaSeparatedSliceViolation {
	record R(int a[], String s) {} // violation: Array brackets must be on the type, not after 'a'.
}
// === end ===

// === case: simple_c_style ===
class InputArrayTypeStyleSimpleCStyleSliceViolation {
	int field[]; // violation: Array brackets must be on the type, not after 'field'.
}
// === end ===

// === case: static_array ===
@SuppressWarnings("unused")
class InputArrayTypeStyleStaticArraySliceViolation {
	static int sb[]; // violation: Array brackets must be on the type, not after 'sb'.
}
// === end ===

// === case: string_literal_with_paren_before_bracket ===
// skip-reason: cannot move brackets in a multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleStringLiteralWithParenBeforeBracketSliceViolation {
	String s = "(", x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: tab_between_type_and_ident ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTabBetweenTypeAndIdentSliceViolation {
	int	x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: tab_indented ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTabIndentedSliceViolation {
		int x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: triple_quote_in_block_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTripleQuoteInBlockCommentDoesNotBlockSliceViolation {
	int x[] /* """ */ = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: triple_quote_in_line_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTripleQuoteInLineCommentDoesNotBlockSliceViolation {
	int x[] = 1; // """ in comment // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: type_parameter_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeParameterMethodReturnSliceViolation {
	<T> T m()[] { // violation: Array brackets must be on the type, not after 'm'.
		return null;
	}
}
// === end ===

// === case: type_use_annotation_in_type_args_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeUseAnnotationInTypeArgsCStyleSliceViolation {
	@interface A {}

	List<@A String> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: type_use_annotation_on_bracket ===
// skip-reason: type-use annotation before the array brackets
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeUseAnnotationOnBracketSliceViolation {
	@interface Anno {}

	int x @Anno []; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: whitespace_before_bracket ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWhitespaceBeforeBracketSliceViolation {
	int x []; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: whitespace_inside_brackets ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWhitespaceInsideBracketsSliceViolation {
	int x[ ]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: wildcard_generic_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleWildcardGenericCStyleSliceViolation {
	List<? extends Number> x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWithAnnotationSliceViolation {
	@Deprecated
	int x[]; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===

// === case: with_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWithInitializerSliceViolation {
	int x[] = {1}; // violation: Array brackets must be on the type, not after 'x'.
}
// === end ===