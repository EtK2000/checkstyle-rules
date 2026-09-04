package com.etk2000.checkstyle.inputs.arraytypestyle;

// === case: annotated_base_dimension_field ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionFieldSliceViolation {
	@interface A {}

	int @A [] x[];
}
// === end ===

// === case: annotated_base_dimension_for_init ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionForInitSliceViolation {
	@interface A {}

	void m(int[][] grid, int step) {
		for (int @A [] x[] = grid; x.length < 10; step += 1)
			break;
	}
}
// === end ===

// === case: annotated_base_dimension_lambda_param ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
// imports: java.util.function.BiConsumer
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionLambdaParamSliceViolation {
	@interface A {}

	BiConsumer<int[][], Integer> b = (int @A [] a[], Integer y) -> {};
}
// === end ===

// === case: annotated_base_dimension_local ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionLocalSliceViolation {
	@interface A {}

	void m() {
		final int @A [] lb[] = {{1}};
		lb[0][0] = 1;
	}
}
// === end ===

// === case: annotated_base_dimension_method_return ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionMethodReturnSliceViolation {
	@interface A {}

	int @A [] method()[] {
		return null;
	}
}
// === end ===

// === case: annotated_base_dimension_mixed_depth_in_call_argument_lambda ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionMixedDepthInCallArgumentLambdaSliceViolation {
	@interface A {}

	void m() {
		run(() -> { final int @A [] alpha[], beta; });
	}

	void run(Runnable task) {
		task.run();
	}
}
// === end ===

// === case: annotated_base_dimension_multi_line_method_return ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionMultiLineMethodReturnSliceViolation {
	@interface A {}

	int @A [] method()
			[]
			{ return null; }
}
// === end ===

// === case: annotated_base_dimension_multi_line_var ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionMultiLineVarSliceViolation {
	@interface A {}

	int @A [] x
			[];
}
// === end ===

// === case: annotated_base_dimension_param ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionParamSliceViolation {
	@interface A {}

	void m(int @A [] x[]) {}
}
// === end ===

// === case: annotated_base_dimension_record_component ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionRecordComponentSliceViolation {
	@interface A {}

	record R(int @A [] x[]) {}
}
// === end ===

// === case: annotated_base_dimension_second_of_two ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedBaseDimensionSecondOfTwoSliceViolation {
	@interface A {}

	int[] @A [] x[];
}
// === end ===

// === case: annotated_field ===
class InputArrayTypeStyleAnnotatedFieldSliceViolation {
	@Deprecated
	int[] annotatedField;
}
// === end ===

// === case: annotated_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotatedParamSliceViolation {
	void annotatedParam(@Deprecated int[] p) {
		p[0] = 1;
	}
}
// === end ===

// === case: annotation_argument_parens_not_treated_as_method_parens ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleAnnotationArgumentParensNotTreatedAsMethodParensSliceViolation {
	@interface Ann {
		String value();
	}

	void m(int[] a, boolean cond) {
		for (@Ann("x") int x[] = a, y = 1; cond; ++y)
			break;
	}
}
// === end ===

// === case: block_comment_after_brackets_multi_var_next_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentAfterBracketsMultiVarNextLineSliceViolation {
	int x[] /* c
	*/, y;
}
// === end ===

// === case: block_comment_before_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentBeforeModifierSliceViolation {
	/* doc */ final int[] x = {1};
}
// === end ===

// === case: block_comment_containing_open_paren_before_bracket ===
@SuppressWarnings("unused")
class InputArrayTypeStyleBlockCommentContainingOpenParenBeforeBracketSliceViolation {
	/* ( */ int[] x;
}
// === end ===

// === case: bounded_wildcard_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleBoundedWildcardCStyleSliceViolation {
	List<? super Integer>[] x;
}
// === end ===

// === case: char_literal_comma_in_multi_var_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralCommaInMultiVarInitSliceViolation {
	void m(char c, char a, char b) {
		final char x[] = (c == ',' ? a : b), y = 'z';
	}
}
// === end ===

// === case: char_literal_escaped_quote_multi_var ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralEscapedQuoteMultiVarSliceViolation {
	char x[] = '\'', y = 'z';
}
// === end ===

// === case: char_literal_with_paren_in_annotation_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCharLiteralWithParenInAnnotationParamSliceViolation {
	@interface A {
		char value();
	}

	int[] m(@A('(') int x) {
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

	int[] m(@A(')') int x) {
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

	int[] m(@A(")") int x) {
		return null;
	}
}
// === end ===

// === case: comma_after_brackets_in_variable_decl ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCommaAfterBracketsInVariableDeclSliceViolation {
	int x[], y;
}
// === end ===

// === case: comma_in_trailing_line_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCommaInTrailingLineCommentDoesNotBlockSliceViolation {
	int[] x = {1}; // hello, world
}
// === end ===

// === case: comment_after_bracket_before_assignment ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentAfterBracketBeforeAssignmentSliceViolation {
	int[] x /* note */ = {1};
}
// === end ===

// === case: comment_between_comma_and_ident_multi_var ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentBetweenCommaAndIdentMultiVarSliceViolation {
	void m(int[] a) {
		final int i = 0, /* note */ x[] = a;
	}
}
// === end ===

// === case: comment_between_ident_and_bracket ===
// skip-reason: cannot move the brackets: a comment sits between the name and the brackets
@SuppressWarnings("unused")
class InputArrayTypeStyleCommentBetweenIdentAndBracketSliceViolation {
	int x /* note */ [];
}
// === end ===

// === case: compound_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCompoundCStyleSliceViolation {
	int[][] x;
}
// === end ===

// === case: compound_local ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCompoundLocalSliceViolation {
	void compoundLocal() {
		final int[][] lc = {{1}};
		lc[0][0] = 1;
	}
}
// === end ===

// === case: ctor_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleCtorParamSliceViolation {
	InputArrayTypeStyleCtorParamSliceViolation(int[] ctorParam) {}
}
// === end ===

// === case: escaped_apostrophe_in_char_literal ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedApostropheInCharLiteralSliceViolation {
	@interface A {
		char value();
	}

	int[] m(char c, @A('\'') int x) {
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

	int[] m(@A("\\") int x) {
		return null;
	}
}
// === end ===

// === case: escaped_backslash_in_string_success_single_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedBackslashInStringSuccessSingleLineSliceViolation {
	String[] s = "a\\b";
}
// === end ===

// === case: escaped_quote_inside_annotation_string_scans_correctly ===
@SuppressWarnings("unused")
class InputArrayTypeStyleEscapedQuoteInsideAnnotationStringScansCorrectlySliceViolation {
	@interface A {
		String value();
	}

	int[] m(@A("\"") int x) {
		return null;
	}
}
// === end ===

// === case: final_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleFinalModifierSliceViolation {
	final int[] x = {1};
}
// === end ===

// === case: for_each_method_not_treated_as_for_keyword ===
@SuppressWarnings("unused")
class InputArrayTypeStyleForEachMethodNotTreatedAsForKeywordSliceViolation {
	void forEach(int[] x) {}
}
// === end ===

// === case: for_loop_multi_var ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleForLoopMultiVarSliceViolation {
	void m(int[] a, boolean cond) {
		for (int x[] = a, y = 1; cond; ++y)
			break;
	}
}
// === end ===

// === case: for_loop_single_var_fixed ===
@SuppressWarnings("unused")
class InputArrayTypeStyleForLoopSingleVarFixedSliceViolation {
	void m(int[] a, int step) {
		for (int[] x = a; x.length < 10; step += 1)
			break;
	}
}
// === end ===

// === case: generic_field ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericFieldSliceViolation {
	List<String>[] gs;
}
// === end ===

// === case: generic_method_multi_param_lambda ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaSliceViolation {
	BiConsumer<X, Y> b = (int[] a, int y) -> {};
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_last ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnLastSliceViolation {
	BiConsumer<X, Y> b = (int a, int[] y) -> {};
}
// === end ===

// === case: generic_method_multi_param_lambda_c_style_on_middle ===
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodMultiParamLambdaCStyleOnMiddleSliceViolation {
	TriConsumer<X, Y, Z> t = (int a, int[] b, int c) -> {};
}
// === end ===

// === case: generic_method_return_type ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericMethodReturnTypeSliceViolation {
	List<String>[] m() {
		return null;
	}
}
// === end ===

// === case: generic_return_nested_type_args ===
// imports: java.util.List
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericReturnNestedTypeArgsSliceViolation {
	Map<String, List<Integer>>[] m() {
		return null;
	}
}
// === end ===

// === case: generic_type_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleGenericTypeCStyleSliceViolation {
	List<String>[] x;
}
// === end ===

// === case: inner_record_component ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInnerRecordComponentSliceViolation {
	record InnerRec(int[] comp) {}
}
// === end ===

// === case: instance_field ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInstanceFieldSliceViolation {
	int[] ib;
}
// === end ===

// === case: instance_field_two_dim ===
@SuppressWarnings("unused")
class InputArrayTypeStyleInstanceFieldTwoDimSliceViolation {
	int[][] ic;
}
// === end ===

// === case: local_simple ===
@SuppressWarnings("unused")
class InputArrayTypeStyleLocalSimpleSliceViolation {
	void simpleLocal() {
		final int[] lb = {1};
		lb[0] = 0;
	}
}
// === end ===

// === case: main ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleViolation {
	void multiVarMixed() {
		final int gamma[] = {1}, delta = 0;
		gamma[0] = delta;
	}

	void multiVarMixedReversed() {
		final int epsilon = 0, zeta[] = {1};
		zeta[0] = epsilon;
	}
}
// === end ===

// === case: method_compound_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodCompoundParamSliceViolation {
	void methodCompoundParam(int[][] c) {
		c[0][0] = 1;
	}
}
// === end ===

// === case: method_mixed_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMixedReturnSliceViolation {
	int[][] methodMixedReturn() {
		return null;
	}
}
// === end ===

// === case: method_multi_param_c_style_on_last ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCStyleOnLastSliceViolation {
	void m(int x, int[] y) {}
}
// === end ===

// === case: method_multi_param_c_style_on_middle ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCStyleOnMiddleSliceViolation {
	void m(int a, int[] b, int c) {}
}
// === end ===

// === case: method_multi_param_comma_separated ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodMultiParamCommaSeparatedSliceViolation {
	void m(int[] a, int b) {}
}
// === end ===

// === case: method_return_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnCStyleSliceViolation {
	int[] methodReturnCStyle() {
		return null;
	}
}
// === end ===

// === case: method_return_type_abstract_semicolon ===
@SuppressWarnings("unused")
abstract class InputArrayTypeStyleMethodReturnTypeAbstractSemicolonSliceViolation {
	abstract int[] m();
}
// === end ===

// === case: method_return_type_array_prefixed ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeArrayPrefixedSliceViolation {
	int[][] m() {
		return null;
	}
}
// === end ===

// === case: method_return_type_brace_body ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeBraceBodySliceViolation {
	int[] method() {
		return null;
	}
}
// === end ===

// === case: method_return_type_compound ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeCompoundSliceViolation {
	int[][] method() {
		return null;
	}
}
// === end ===

// === case: method_return_type_with_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnTypeWithThrowsSliceViolation {
	int[] method() throws Exception {
		return null;
	}
}
// === end ===

// === case: method_return_with_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodReturnWithThrowsSliceViolation {
	int[] methodReturnWithThrows() throws Exception {
		return null;
	}
}
// === end ===

// === case: method_simple_param ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMethodSimpleParamSliceViolation {
	void methodSimpleParam(int[] b) {
		b[0] = 1;
	}
}
// === end ===

// === case: mixed_declaration ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMixedDeclarationSliceViolation {
	void mixedDeclaration() {
		final int[][] mx = {{1}};
		mx[0][0] = 1;
	}
}
// === end ===

// === case: mixed_java_and_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMixedJavaAndCStyleSliceViolation {
	int[][] x;
}
// === end ===

// === case: multi_line_abstract_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineAbstractMethodReturnSliceViolation {
	int[] method()
			;
}
// === end ===

// === case: multi_line_block_comment_spans_across_lines_followed_by_comma ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansAcrossLinesFollowedByCommaSliceViolation {
	int x
		[] /*
fake */, y;
}
// === end ===

// === case: multi_line_block_comment_spans_across_lines_with_fake_comma ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansAcrossLinesWithFakeCommaSliceViolation {
	int[] x
		 /*
fake , */;
}
// === end ===

// === case: multi_line_block_comment_spans_lines_containing_comma ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBlockCommentSpansLinesContainingCommaSliceViolation {
	int x[] /*
fake , inside
*/ , y;
}
// === end ===

// === case: multi_line_bracket_line_has_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBracketLineHasInitializerSliceViolation {
	int[] x
			 = {1};
}
// === end ===

// === case: multi_line_bracket_line_has_method_body ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineBracketLineHasMethodBodySliceViolation {
	int[] method()
			 { return null; }
}
// === end ===

// === case: multi_line_final_modifier ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineFinalModifierSliceViolation {
	final int[] x
			 = {1};
}
// === end ===

// === case: multi_line_final_with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineFinalWithAnnotationSliceViolation {
	@Deprecated
	final int[] x
			;
}
// === end ===

// === case: multi_line_generic_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineGenericMethodReturnSliceViolation {
	<T> T[] method()
			;
}
// === end ===

// === case: multi_line_line_comment_before_comma_on_next_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineLineCommentBeforeCommaOnNextLineSliceViolation {
	int x
			[] = 1 // comment
	, y = 3;
}
// === end ===

// === case: multi_line_line_comment_triple_quote_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineLineCommentTripleQuoteDoesNotBlockSliceViolation {
	int[] x
			 // """
	= {1};
}
// === end ===

// === case: multi_line_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnSliceViolation {
	int[] method()
			{ return null; }
}
// === end ===

// === case: multi_line_method_return_with_comment_on_prev_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnWithCommentOnPrevLineSliceViolation {
	int[] method() // comment
			;
}
// === end ===

// === case: multi_line_method_return_with_multi_throws ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMethodReturnWithMultiThrowsSliceViolation {
	int[] method()
			 throws E1, E2 { return null; }
}
// === end ===

// === case: multi_line_multi_var ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarSliceViolation {
	int alpha, beta
			[];
}
// === end ===

// === case: multi_line_multi_var_on_bracket_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarOnBracketLineSliceViolation {
	int x
			[], y;
}
// === end ===

// === case: multi_line_multi_var_on_following_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarOnFollowingLineSliceViolation {
	int x
			[]
	, y;
}
// === end ===

// === case: multi_line_multi_var_spanning_brace_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineMultiVarSpanningBraceInitSliceViolation {
	int x
			[] = {1,
			2}, y = 3;
}
// === end ===

// === case: multi_line_simple_var ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineSimpleVarSliceViolation {
	int[] x
			;
}
// === end ===

// === case: multi_line_string_literal_init_with_comma ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineStringLiteralInitWithCommaSliceViolation {
	String x
			[] = ",", y = "z";
}
// === end ===

// === case: multi_line_type_use_annotation_on_prev_line ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineTypeUseAnnotationOnPrevLineSliceViolation {
	@interface Anno {}

	int x @Anno
			[];
}
// === end ===

// === case: multi_line_with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithAnnotationSliceViolation {
	@Deprecated
	int[] x
			;
}
// === end ===

// === case: multi_line_with_block_comment_on_prev_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithBlockCommentOnPrevLineSliceViolation {
	int[] x /* note */
			;
}
// === end ===

// === case: multi_line_with_generic_prev_return ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithGenericPrevReturnSliceViolation {
	List<String>[] method()
			;
}
// === end ===

// === case: multi_line_with_generic_prev_var ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithGenericPrevVarSliceViolation {
	List<String>[] x
			;
}
// === end ===

// === case: multi_line_with_java_style_array_prev_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithJavaStyleArrayPrevReturnSliceViolation {
	int[][] method()
			;
}
// === end ===

// === case: multi_line_with_java_style_array_prev_var ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithJavaStyleArrayPrevVarSliceViolation {
	int[][] x
			;
}
// === end ===

// === case: multi_line_with_string_literal_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiLineWithStringLiteralInitializerSliceViolation {
	String[] x
			 = "abc";
}
// === end ===

// === case: multi_var_base_type_annotated_brackets ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBaseTypeAnnotatedBracketsSliceViolation {
	@interface A {}

	int @A [] alpha[], beta[];
}
// === end ===

// === case: multi_var_base_type_brackets_first_only ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBaseTypeBracketsFirstOnlySliceViolation {
	int[] alpha[], beta;
}
// === end ===

// === case: multi_var_base_type_brackets_last_only ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBaseTypeBracketsLastOnlySliceViolation {
	int[] alpha, beta[];
}
// === end ===

// === case: multi_var_base_type_brackets_same_depth ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBaseTypeBracketsSameDepthSliceViolation {
	int[] alpha[], beta[];
}
// === end ===

// === case: multi_var_brace_initializer ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBraceInitializerSliceViolation {
	int x[] = {1, 2}, y = 3;
}
// === end ===

// === case: multi_var_bracket_index_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarBracketIndexInitSliceViolation {
	void m(int[] arr, int i) {
		final int x[] = arr[i], y = 3;
	}
}
// === end ===

// === case: multi_var_function_call_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarFunctionCallInitSliceViolation {
	int[] foo(int a, int b) {
		return null;
	}

	void m(int a, int b) {
		final int x[] = foo(a, b), y = 3;
	}
}
// === end ===

// === case: multi_var_gap_annotation_between_sibling_bracket_pairs ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapAnnotationBetweenSiblingBracketPairsSliceViolation {
	@interface A {}

	int alpha[][], beta[] @A [];
}
// === end ===

// === case: multi_var_gap_annotation_on_own_brackets ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapAnnotationOnOwnBracketsSliceViolation {
	@interface A {}

	int alpha @A [], beta[];
}
// === end ===

// === case: multi_var_gap_annotation_on_sibling_brackets ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapAnnotationOnSiblingBracketsSliceViolation {
	@interface A {}

	int alpha[], beta @A [];
}
// === end ===

// === case: multi_var_gap_comment_inside_brackets ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapCommentInsideBracketsSliceViolation {
	int alpha[/* note */], beta[];
}
// === end ===

// === case: multi_var_gap_comment_own ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapCommentOwnSliceViolation {
	int alpha /* note */ [], beta[];
}
// === end ===

// === case: multi_var_gap_comment_sibling ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarGapCommentSiblingSliceViolation {
	int alpha[], beta /* note */ [];
}
// === end ===

// === case: multi_var_mixed_depth_for_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthForInitSliceViolation {
	void m(int[] first, int[][] grid, boolean cond) {
		for (int alpha[] = first, beta[][] = grid; cond; alpha = first)
			break;
	}
}
// === end ===

// === case: multi_var_mixed_depth_in_call_argument_lambda ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthInCallArgumentLambdaSliceViolation {
	void m() {
		run(() -> { final int alpha[], beta; });
	}

	void run(Runnable task) {
		task.run();
	}
}
// === end ===

// === case: multi_var_mixed_depth_none_then_two ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthNoneThenTwoSliceViolation {
	int alpha, beta[][];
}
// === end ===

// === case: multi_var_mixed_depth_one_then_two ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthOneThenTwoSliceViolation {
	int alpha[], beta[][];
}
// === end ===

// === case: multi_var_mixed_depth_three_first ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthThreeFirstSliceViolation {
	int alpha[][], beta[], gamma[];
}
// === end ===

// === case: multi_var_mixed_depth_three_last ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthThreeLastSliceViolation {
	int alpha[], beta[], gamma[][];
}
// === end ===

// === case: multi_var_mixed_depth_three_middle ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthThreeMiddleSliceViolation {
	int alpha[], beta[][], gamma[];
}
// === end ===

// === case: multi_var_mixed_depth_three_none_in_middle ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthThreeNoneInMiddleSliceViolation {
	int alpha[], beta, gamma[];
}
// === end ===

// === case: multi_var_mixed_depth_two_then_none ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthTwoThenNoneSliceViolation {
	int alpha[][], beta;
}
// === end ===

// === case: multi_var_mixed_depth_two_then_one ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarMixedDepthTwoThenOneSliceViolation {
	int alpha[][], beta[];
}
// === end ===

// === case: multi_var_own_brackets_on_next_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarOwnBracketsOnNextLineSliceViolation {
	int alpha
			[], beta[];
}
// === end ===

// === case: multi_var_own_last_bracket_pair_on_next_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarOwnLastBracketPairOnNextLineSliceViolation {
	int alpha[]
			[], beta[][];
}
// === end ===

// === case: multi_var_prev ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarPrevSliceViolation {
	int alpha, beta[];
}
// === end ===

// === case: multi_var_same_depth_adjacent_declarations ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthAdjacentDeclarationsSliceViolation {
	int[] alpha, beta;
	long[] delta;
	short[] epsilon, gamma;
}
// === end ===

// === case: multi_var_same_depth_anonymous_class_field ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthAnonymousClassFieldSliceViolation {
	private final Object holder = new Object() {
		int[] alpha, beta;
	};
}
// === end ===

// === case: multi_var_same_depth_array_valued_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthArrayValuedAnnotationSliceViolation {
	@SuppressWarnings({"rawtypes", "unused"})
	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_block_comment_spans_lines ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthBlockCommentSpansLinesSliceViolation {
	int[] alpha /* note
			more */, beta;
}
// === end ===

// === case: multi_var_same_depth_brace_init_with_commas ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthBraceInitWithCommasSliceViolation {
	int[] alpha = {1, 2}, beta = {3, 4};
}
// === end ===

// === case: multi_var_same_depth_call_init_with_commas ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCallInitWithCommasSliceViolation {
	void m() {
		final int[] alpha = pair(1, 2), beta = pair(3, 4);
		alpha[0] = beta[0];
	}

	private int[] pair(int first, int second) {
		return new int[]{first, second};
	}
}
// === end ===

// === case: multi_var_same_depth_cast_and_ternary_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCastAndTernaryInitSliceViolation {
	void m(Object source, int[] first, int[] second, boolean cond) {
		final int[] alpha = (int[]) source, beta = cond ? first : second;
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: multi_var_same_depth_char_init_with_comma ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCharInitWithCommaSliceViolation {
	char[] alpha = {','}, beta = {'\''};
}
// === end ===

// === case: multi_var_same_depth_comma_on_next_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCommaOnNextLineSliceViolation {
	int[] alpha
			, beta;
}
// === end ===

// === case: multi_var_same_depth_comment_after_comma ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCommentAfterCommaSliceViolation {
	int[] alpha, /* note */ beta;
}
// === end ===

// === case: multi_var_same_depth_comment_after_type ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCommentAfterTypeSliceViolation {
	int[] /* note */ alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_comment_between_declarators ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthCommentBetweenDeclaratorsSliceViolation {
	int[] alpha /* [], note */, beta;
}
// === end ===

// === case: multi_var_same_depth_declaration_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthDeclarationAnnotationSliceViolation {
	@Deprecated
	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_declarators_on_two_lines ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthDeclaratorsOnTwoLinesSliceViolation {
	int[] alpha,
			beta;
}
// === end ===

// === case: multi_var_same_depth_enum_field ===
@SuppressWarnings("unused")
enum InputArrayTypeStyleMultiVarSameDepthEnumFieldSliceViolation {
	FIRST;

	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_field ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthFieldSliceViolation {
	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_for_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthForInitSliceViolation {
	void m(int[] first, int[] second, boolean cond) {
		for (int[] alpha = first, beta = second; cond; alpha = beta)
			break;
	}
}
// === end ===

// === case: multi_var_same_depth_for_init_call_with_commas ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthForInitCallWithCommasSliceViolation {
	void m(boolean cond) {
		for (int[] alpha = pair(1, 2), beta = pair(3, 4); cond; alpha = beta)
			break;
	}

	private int[] pair(int first, int second) {
		return new int[]{first, second};
	}
}
// === end ===

// === case: multi_var_same_depth_generic ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthGenericSliceViolation {
	List<String>[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_in_call_argument_lambda ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthInCallArgumentLambdaSliceViolation {
	void m() {
		run(() -> { final int[] alpha, beta; });
	}

	void run(Runnable task) {
		task.run();
	}
}
// === end ===

// === case: multi_var_same_depth_index_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthIndexInitSliceViolation {
	void m(int[][] grid) {
		final int[] alpha = grid[0], beta = grid[1];
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: multi_var_same_depth_initializer_spans_lines ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthInitializerSpansLinesSliceViolation {
	int[] alpha = {1,
			2}, beta = {3};
}
// === end ===

// === case: multi_var_same_depth_interface_constant ===
@SuppressWarnings("unused")
interface InputArrayTypeStyleMultiVarSameDepthInterfaceConstantSliceViolation {
	int[] ALPHA = {1}, BETA = {2};
}
// === end ===

// === case: multi_var_same_depth_lambda_body_local ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthLambdaBodyLocalSliceViolation {
	void m() {
		final Runnable task = () -> {
			final int[] alpha = {1}, beta = {2};
			alpha[0] = beta[0];
		};
		task.run();
	}
}
// === end ===

// === case: multi_var_same_depth_local_array_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthLocalArrayInitSliceViolation {
	void multiVarSame() {
		final int[] alpha = {1}, beta = {2};
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: multi_var_same_depth_method_ref_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthMethodRefInitSliceViolation {
	Runnable[] alpha = {System::gc}, beta = {};
}
// === end ===

// === case: multi_var_same_depth_mixed_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthMixedInitSliceViolation {
	int[] alpha = {1}, beta;
}
// === end ===

// === case: multi_var_same_depth_nested_generic ===
// imports: java.util.List
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthNestedGenericSliceViolation {
	Map<String, List<Integer>>[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_qualified_generic ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthQualifiedGenericSliceViolation {
	static class Box {
		static class Item<T> {}
	}

	Box.Item<String>[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_record_static_field ===
@SuppressWarnings("unused")
record InputArrayTypeStyleMultiVarSameDepthRecordStaticFieldSliceViolation() {
	static int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_space_between_bracket_pairs ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthSpaceBetweenBracketPairsSliceViolation {
	int[] [] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_static_final_constant ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthStaticFinalConstantSliceViolation {
	private static final int[] ALPHA = {1}, BETA = {2};
}
// === end ===

// === case: multi_var_same_depth_static_initializer_local ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthStaticInitializerLocalSliceViolation {
	static {
		final int[] alpha = {1}, beta = {2};
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: multi_var_same_depth_string_init_with_comma ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthStringInitWithCommaSliceViolation {
	String[] alpha = {","}, beta = {"z"};
}
// === end ===

// === case: multi_var_same_depth_supplementary_char_before_type ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthSupplementaryCharBeforeTypeSliceViolation {
	/* 𝒜 */ int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_switch_rule_local ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthSwitchRuleLocalSliceViolation {
	void m(int key) {
		switch (key) {
			case 1 -> {
				final int[] alpha = {1}, beta = {2};
				alpha[0] = beta[0];
			}
			default -> System.gc();
		}
	}
}
// === end ===

// === case: multi_var_same_depth_tab_between_type_and_name ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTabBetweenTypeAndNameSliceViolation {
	int[]	alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_text_block_in_first_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTextBlockInFirstInitSliceViolation {
	String[] alpha = {"""
			x"""}, beta = {"y"};
}
// === end ===

// === case: multi_var_same_depth_text_block_in_last_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTextBlockInLastInitSliceViolation {
	String[] alpha = {"x"}, beta = {"""
			y"""};
}
// === end ===

// === case: multi_var_same_depth_three_declarators ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthThreeDeclaratorsSliceViolation {
	int[] alpha, beta, gamma;
}
// === end ===

// === case: multi_var_same_depth_trailing_line_comment ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTrailingLineCommentSliceViolation {
	int[] alpha, beta; // gamma[], delta
}
// === end ===

// === case: multi_var_same_depth_two_dim ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTwoDimSliceViolation {
	int[][] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_two_dim_nested_brace_init ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTwoDimNestedBraceInitSliceViolation {
	int[][] alpha = {{1, 2}, {3}}, beta = {{4}};
}
// === end ===

// === case: multi_var_same_depth_type_args_with_brackets ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTypeArgsWithBracketsSliceViolation {
	List<int[]>[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_type_on_previous_line ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTypeOnPreviousLineSliceViolation {
	void m() {
		final int[]
				alpha = {1}, beta = {2};
		alpha[0] = beta[0];
	}
}
// === end ===

// === case: multi_var_same_depth_type_use_annotation_in_type_args ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthTypeUseAnnotationInTypeArgsSliceViolation {
	@interface A {}

	List<@A String>[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_whitespace_before_brackets ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthWhitespaceBeforeBracketsSliceViolation {
	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_whitespace_inside_first_brackets ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthWhitespaceInsideFirstBracketsSliceViolation {
	int[ ] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_whitespace_inside_later_brackets ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthWhitespaceInsideLaterBracketsSliceViolation {
	int[] alpha, beta;
}
// === end ===

// === case: multi_var_same_depth_wildcard_generic ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSameDepthWildcardGenericSliceViolation {
	List<? extends Number>[] alpha, beta;
}
// === end ===

// === case: multi_var_sibling_brackets_on_next_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSiblingBracketsOnNextLineSliceViolation {
	int alpha[], beta
			[];
}
// === end ===

// === case: multi_var_spanning_brace_init_on_single_line ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarSpanningBraceInitOnSingleLineSliceViolation {
	void m() {
		final int x[] = {1,
				2}, y = 3;
	}
}
// === end ===

// === case: multi_var_string_escaped_quote ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarStringEscapedQuoteSliceViolation {
	String x[] = "a\"b", y = "z";
}
// === end ===

// === case: multi_var_string_literal_init ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarStringLiteralInitSliceViolation {
	String x[] = ",", y = "z";
}
// === end ===

// === case: multi_var_with_initializer ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleMultiVarWithInitializerSliceViolation {
	int gamma[] = {1}, delta = 2;
}
// === end ===

// === case: multiple_modifiers ===
@SuppressWarnings("unused")
class InputArrayTypeStyleMultipleModifiersSliceViolation {
	public static final int[] x = {1};
}
// === end ===

// === case: multiple_type_args_c_style ===
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleMultipleTypeArgsCStyleSliceViolation {
	Map<String, Integer>[] x;
}
// === end ===

// === case: nested_generic_c_style ===
// imports: java.util.List
// imports: java.util.Map
@SuppressWarnings("unused")
class InputArrayTypeStyleNestedGenericCStyleSliceViolation {
	Map<String, List<Integer>>[] x;
}
// === end ===

// === case: parameter_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleParameterCStyleSliceViolation {
	void m(int[] x) {}
}
// === end ===

// === case: qualified_type_use_annotation_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleQualifiedTypeUseAnnotationCStyleSliceViolation {
	@interface A {}

	static class Box {
		class Item {}
	}

	Box.@A Item[] x;
}
// === end ===

// === case: record_component_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleRecordComponentCStyleSliceViolation {
	record R(int[] x) {}
}
// === end ===

// === case: record_component_declaration_annotation_c_style ===
@SuppressWarnings("unused")
class InputArrayTypeStyleRecordComponentDeclarationAnnotationCStyleSliceViolation {
	@interface A {}

	record R(@A int[] x) {}
}
// === end ===

// === case: record_multi_component_comma_separated ===
@SuppressWarnings("unused")
class InputArrayTypeStyleRecordMultiComponentCommaSeparatedSliceViolation {
	record R(int[] a, String s) {}
}
// === end ===

// === case: simple_c_style ===
class InputArrayTypeStyleSimpleCStyleSliceViolation {
	int[] field;
}
// === end ===

// === case: static_array ===
@SuppressWarnings("unused")
class InputArrayTypeStyleStaticArraySliceViolation {
	static int[] sb;
}
// === end ===

// === case: string_literal_with_paren_before_bracket ===
// skip-reason: cannot move the brackets: multi-variable declaration
@SuppressWarnings("unused")
class InputArrayTypeStyleStringLiteralWithParenBeforeBracketSliceViolation {
	String s = "(", x[];
}
// === end ===

// === case: tab_between_type_and_ident ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTabBetweenTypeAndIdentSliceViolation {
	int[]	x;
}
// === end ===

// === case: tab_indented ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTabIndentedSliceViolation {
		int[] x;
}
// === end ===

// === case: triple_quote_in_block_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTripleQuoteInBlockCommentDoesNotBlockSliceViolation {
	int[] x /* """ */ = {1};
}
// === end ===

// === case: triple_quote_in_line_comment_does_not_block ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTripleQuoteInLineCommentDoesNotBlockSliceViolation {
	int[] x = 1; // """ in comment
}
// === end ===

// === case: type_parameter_method_return ===
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeParameterMethodReturnSliceViolation {
	<T> T[] m() {
		return null;
	}
}
// === end ===

// === case: type_use_annotation_in_type_args_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeUseAnnotationInTypeArgsCStyleSliceViolation {
	@interface A {}

	List<@A String>[] x;
}
// === end ===

// === case: type_use_annotation_in_type_args_with_base_brackets ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeUseAnnotationInTypeArgsWithBaseBracketsSliceViolation {
	@interface A {}

	List<@A String>[][] x;
}
// === end ===

// === case: type_use_annotation_on_bracket ===
// skip-reason: cannot move the brackets: a type-use annotation precedes them
@SuppressWarnings("unused")
class InputArrayTypeStyleTypeUseAnnotationOnBracketSliceViolation {
	@interface Anno {}

	int x @Anno [];
}
// === end ===

// === case: whitespace_before_bracket ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWhitespaceBeforeBracketSliceViolation {
	int[] x;
}
// === end ===

// === case: whitespace_inside_brackets ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWhitespaceInsideBracketsSliceViolation {
	int[ ] x;
}
// === end ===

// === case: wildcard_generic_c_style ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputArrayTypeStyleWildcardGenericCStyleSliceViolation {
	List<? extends Number>[] x;
}
// === end ===

// === case: with_annotation ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWithAnnotationSliceViolation {
	@Deprecated
	int[] x;
}
// === end ===

// === case: with_initializer ===
@SuppressWarnings("unused")
class InputArrayTypeStyleWithInitializerSliceViolation {
	int[] x = {1};
}
// === end ===