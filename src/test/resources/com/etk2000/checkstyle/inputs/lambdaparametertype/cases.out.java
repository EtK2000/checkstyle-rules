package com.etk2000.checkstyle.inputs.lambdaparametertype;

// === case: annotation_arg_char_close_paren ===
class InputLambdaParamAnnotationArgCharCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(')') var a, var y) -> {};
	}
}
// === end ===

// === case: annotation_arg_close_paren ===
class InputLambdaParamAnnotationArgCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(")") var x, var y) -> {};
	}
}
// === end ===

// === case: annotation_arg_comma_string ===
class InputLambdaParamAnnotationArgCommaStringSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("a,b") var x, var y) -> {};
	}
}
// === end ===

// === case: annotation_arg_open_paren ===
class InputLambdaParamAnnotationArgOpenParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("(") var x, var y) -> {};
	}
}
// === end ===

// === case: annotation_arg_string_arrow ===
class InputLambdaParamAnnotationArgStringArrowSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(") ->") var x, var y) -> {};
	}
}
// === end ===

// === case: block_comment_before_annotation ===
// imports: java.util.List
class InputLambdaParamBlockCommentBeforeAnnotationSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((/* c */ @A var x) -> System.out.println(x));
	}
}
// === end ===

// === case: block_comment_before_arrow ===
// imports: java.util.List
class InputLambdaParamBlockCommentBeforeArrowSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: block_comment_trailing_name ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: block_comment_trailing_name_annotated ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameAnnotatedSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((@A var x) -> System.out.println(x));
	}
}
// === end ===

// === case: block_comment_trailing_name_second_param ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameSecondParamSliceViolation {
	void m(List<String> list) {
		list.sort((x, y) -> x.compareTo(y));
	}
}
// === end ===

// === case: implicit_array_type ===
// imports: java.util.function.Consumer
class InputLambdaParamImplicitArrayTypeSliceViolation {
	void m() {
		final Consumer<String[]> c = x -> System.out.println(x.length);
	}
}
// === end ===

// === case: implicit_primitive_type ===
// imports: java.util.function.IntConsumer
class InputLambdaParamImplicitPrimitiveTypeSliceViolation {
	void m() {
		final IntConsumer c = x -> System.out.println(x);
	}
}
// === end ===

// === case: multiline_annotated_both_lines ===
// imports: java.util.Comparator
class InputLambdaParamMultilineAnnotatedBothLinesSliceViolation {
	@interface A {}

	@interface B {}

	void m() {
		final Comparator<String> c = (@A var x, @B var y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_annotated_on_first_line ===
// imports: java.util.Comparator
class InputLambdaParamMultilineAnnotatedOnFirstLineSliceViolation {
	@interface A {}

	void m() {
		final Comparator<String> c = (@A var x, var y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_annotated_on_second_line ===
// imports: java.util.Comparator
class InputLambdaParamMultilineAnnotatedOnSecondLineSliceViolation {
	@interface A {}

	void m() {
		final Comparator<String> c = (var x, @A var y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_arrow_after_close_paren ===
// imports: java.util.Comparator
class InputLambdaParamMultilineArrowAfterCloseParenSliceViolation {
	void m() {
		final Comparator<String> c = (x, y)
				-> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_break_after_comma ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBreakAfterCommaSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_break_after_open_paren ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBreakAfterOpenParenSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_break_before_close_paren ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBreakBeforeCloseParenSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_budget_exactly_at_limit ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBudgetExactlyAtLimitSliceViolation {
	void m() {
		final Comparator<String> c = (firstParameterPaddedTo37CharactersAbc, secondParameterPaddedTo36CharsAbcdef) -> 0;
	}
}
// === end ===

// === case: multiline_budget_relaxation_arm ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBudgetRelaxationArmSliceViolation {
	void m() {
		final Comparator<String> c = (firstParameterWithAnExtremelyLongDescriptiveNameHere, secondParameterWithALongName) -> 0;
	}
}
// === end ===

// === case: multiline_budget_relaxation_at_equality ===
// imports: java.util.Comparator
class InputLambdaParamMultilineBudgetRelaxationAtEqualitySliceViolation {
	void m() {
		final Comparator<String> c = (firstParameterPaddedTo37CharactersAbc, secondParameterPaddedTo37CharsAbcdefg) -> 0 + 1234;
	}
}
// === end ===

// === case: multiline_comment_after_the_list ===
// imports: java.util.Comparator
class InputLambdaParamMultilineCommentAfterTheListSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y); /* tail */
	}
}
// === end ===

// === case: multiline_comment_before_the_list ===
// imports: java.util.Comparator
class InputLambdaParamMultilineCommentBeforeTheListSliceViolation {
	void m() {
		final Comparator<String> c = /* note */ (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_curried_lambdas_sharing_a_line ===
class InputLambdaParamMultilineCurriedLambdasSharingALineSliceViolation {
	void m() {
		final Bi b = (p, q) -> (r, s) -> p;
	}
}
// === end ===

// === case: multiline_inner_list_with_clean_outer ===
class InputLambdaParamMultilineInnerListWithCleanOuterSliceViolation {
	void m() {
		final Bi b = (p, q) -> (r, s) -> p;
	}
}
// === end ===

// === case: multiline_open_and_close_alone ===
// imports: java.util.Comparator
class InputLambdaParamMultilineOpenAndCloseAloneSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_outer_list_with_nested_naked_lambda ===
// imports: java.util.Comparator
class InputLambdaParamMultilineOuterListWithNestedNakedLambdaSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.chars().map(z -> z).sum() - y.length();
	}
}
// === end ===

// === case: multiline_parens_arrow_after_close_paren ===
// imports: java.util.function.UnaryOperator
class InputLambdaParamMultilineParensArrowAfterCloseParenSliceViolation {
	void m() {
		final UnaryOperator<String> u = x
				-> x;
	}
}
// === end ===

// === case: multiline_parens_break_after_open_paren ===
// imports: java.util.function.UnaryOperator
class InputLambdaParamMultilineParensBreakAfterOpenParenSliceViolation {
	void m() {
		final UnaryOperator<String> u = x -> x;
	}
}
// === end ===

// === case: multiline_parens_break_before_close_paren ===
// imports: java.util.function.UnaryOperator
class InputLambdaParamMultilineParensBreakBeforeCloseParenSliceViolation {
	void m() {
		final UnaryOperator<String> u = x -> x;
	}
}
// === end ===

// === case: multiline_single_annotated_param ===
// imports: java.util.function.UnaryOperator
class InputLambdaParamMultilineSingleAnnotatedParamSliceViolation {
	@interface A {}

	void m() {
		final UnaryOperator<String> u = (@A var x) -> x;
	}
}
// === end ===

// === case: multiline_single_explicit_param ===
// imports: java.util.function.UnaryOperator
class InputLambdaParamMultilineSingleExplicitParamSliceViolation {
	void m() {
		final UnaryOperator<String> u = x -> x;
	}
}
// === end ===

// === case: multiline_string_literal_masking_to_itself ===
// imports: java.util.Comparator
class InputLambdaParamMultilineStringLiteralMaskingToItselfSliceViolation {
	@interface A {}

	void m() {
		final Comparator<String> c = (@A(" ") var x, @A(" ") var y) -> x.compareTo(y);
	}
}
// === end ===

// === case: multiline_three_params_each_own_line ===
class InputLambdaParamMultilineThreeParamsEachOwnLineSliceViolation {
	void m() {
		final Tri t = (a, b, c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_three_params_first_annotated ===
class InputLambdaParamMultilineThreeParamsFirstAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final Tri t = (@A var a, var b, var c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_three_params_last_annotated ===
class InputLambdaParamMultilineThreeParamsLastAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final Tri t = (var a, var b, @A var c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_three_params_middle_annotated ===
class InputLambdaParamMultilineThreeParamsMiddleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final Tri t = (var a, @A var b, var c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_three_params_two_on_first_line ===
class InputLambdaParamMultilineThreeParamsTwoOnFirstLineSliceViolation {
	void m() {
		final Tri t = (a, b, c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_three_params_two_on_last_line ===
class InputLambdaParamMultilineThreeParamsTwoOnLastLineSliceViolation {
	void m() {
		final Tri t = (a, b, c) -> a + b + c;
	}
}
// === end ===

// === case: multiline_two_type_names ===
class InputLambdaParamMultilineTwoTypeNamesSliceViolation {
	void m() {
		final BiFn f = (x, y) -> x;
	}
}
// === end ===

// === case: multiline_var_type ===
// imports: java.util.Comparator
class InputLambdaParamMultilineVarTypeSliceViolation {
	void m() {
		final Comparator<String> c = (x, y) -> x.compareTo(y);
	}
}
// === end ===

// === case: nested_lambda_inner_annotation_arg_paren ===
// imports: java.util.List
class InputLambdaParamNestedInnerAnnotationArgParenSliceViolation {
	@interface A {}

	void m(List<List<String>> lists) {
		lists.forEach(inner -> inner.forEach((@A(")") var y) -> System.out.println(y)));
	}
}
// === end ===

// === case: no_space_before_arrow ===
class InputLambdaParamNoSpaceBeforeArrowSliceViolation {
	void m() {
		final F f = s-> s.length;
	}
}
// === end ===

// === case: parens_braced_body ===
// imports: java.util.List
class InputLambdaParamParensBracedBodySliceViolation {
	void m(List<String> list) {
		list.forEach(x -> {
			System.out.println(x);
		});
	}
}
// === end ===

// === case: parens_expression_body ===
// imports: java.util.List
class InputLambdaParamParensExpressionBodySliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: remove_type_c_style_array_interior_space ===
class InputLambdaParamRemoveTypeCStyleArrayInteriorSpaceSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_last ===
class InputLambdaParamRemoveTypeCStyleArrayLastSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_middle ===
class InputLambdaParamRemoveTypeCStyleArrayMiddleSliceViolation {
	void m() {
		final TriConsumer<X, Y, Z> t = (a, b, c) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_multi ===
class InputLambdaParamRemoveTypeCStyleArrayMultiSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_multidim ===
class InputLambdaParamRemoveTypeCStyleArrayMultidimSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_single ===
// imports: java.util.function.Consumer
class InputLambdaParamRemoveTypeCStyleArraySingleSliceViolation {
	void m() {
		final Consumer<X> c = a -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_space_before_bracket ===
class InputLambdaParamRemoveTypeCStyleArraySpaceBeforeBracketSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_tab_before_name ===
class InputLambdaParamRemoveTypeCStyleArrayTabBeforeNameSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type_spaced ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSpacedSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_mixed_with_java_style ===
class InputLambdaParamRemoveTypeCStyleMixedWithJavaStyleSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, b) -> {};
	}
}
// === end ===

// === case: remove_type_c_style_object_array ===
class InputLambdaParamRemoveTypeCStyleObjectArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_generic_array_param ===
class InputLambdaParamRemoveTypeGenericArrayParamSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (m, x) -> 0;
	}
}
// === end ===

// === case: remove_type_generic_param_with_comma ===
class InputLambdaParamRemoveTypeGenericParamWithCommaSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (m, x) -> m.size() + x;
	}
}
// === end ===

// === case: remove_type_generic_single_param ===
class InputLambdaParamRemoveTypeGenericSingleParamSliceViolation {
	void m() {
		final Function<X, Z> c = m -> m.size();
	}
}
// === end ===

// === case: remove_type_java_style_array ===
class InputLambdaParamRemoveTypeJavaStyleArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (a, y) -> {};
	}
}
// === end ===

// === case: remove_type_multi_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeMultiSliceViolation {
	void m(List<String> list) {
		list.sort((x, y) -> x.compareTo(y));
	}
}
// === end ===

// === case: remove_type_nested_generics_param ===
class InputLambdaParamRemoveTypeNestedGenericsParamSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (m, n) -> {};
	}
}
// === end ===

// === case: remove_type_single_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeSingleSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: remove_type_tab_between_type_and_name ===
// imports: java.util.List
class InputLambdaParamRemoveTypeTabSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===

// === case: remove_type_trailing_space_in_list ===
class InputLambdaParamRemoveTypeTrailingSpaceSliceViolation {
	void m() {
		final F f = (a, b, c) -> a + b + c;
	}
}
// === end ===

// === case: remove_type_varargs_primitive ===
class InputLambdaParamRemoveTypeVarargsPrimitiveSliceViolation {
	void m() {
		final F f = xs -> xs.length;
	}
}
// === end ===

// === case: remove_type_varargs_reference ===
class InputLambdaParamRemoveTypeVarargsReferenceSliceViolation {
	void m() {
		final F f = parts -> parts.length;
	}
}
// === end ===

// === case: remove_type_wildcard_bare ===
class InputLambdaParamRemoveTypeWildcardBareSliceViolation {
	void m() {
		final F f = n -> n.size();
	}
}
// === end ===

// === case: remove_type_wildcard_lower ===
class InputLambdaParamRemoveTypeWildcardLowerSliceViolation {
	void m() {
		final F f = n -> n.size();
	}
}
// === end ===

// === case: remove_type_wildcard_upper ===
class InputLambdaParamRemoveTypeWildcardUpperSliceViolation {
	void m() {
		final F f = n -> n.size();
	}
}
// === end ===

// === case: replace_type_with_var_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarAnnotatedSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((@A var x) -> System.out.println(x));
	}
}
// === end ===

// === case: replace_type_with_var_annotation_with_args ===
class InputLambdaParamReplaceVarAnnotationWithArgsSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("v") var x, var y) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_annotation_with_comma_args ===
class InputLambdaParamReplaceVarAnnotationWithCommaArgsSliceViolation {
	@interface A {
		int x();

		int y();
	}

	void m() {
		final BiFunction<X, Y, Z> c = (@A(x = 1, y = 2) var m, var x) -> 0;
	}
}
// === end ===

// === case: replace_type_with_var_both_params_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarBothParamsAnnotatedSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.sort((@A var x, @B var y) -> x.compareTo(y));
	}
}
// === end ===

// === case: replace_type_with_var_c_style_annotated ===
class InputLambdaParamReplaceVarCStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var a, var y) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_c_style_both ===
class InputLambdaParamReplaceVarCStyleBothSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var a, var b) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_c_style_multidim ===
class InputLambdaParamReplaceVarCStyleMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var a, var y) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var x, var b) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated_multidim ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var x, var b) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_first_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarFirstParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((@A var x, var y) -> x.compareTo(y));
	}
}
// === end ===

// === case: replace_type_with_var_java_style_annotated ===
class InputLambdaParamReplaceVarJavaStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var a, var y) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_java_style_non_annotated ===
class InputLambdaParamReplaceVarJavaStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A var x, var b) -> {};
	}
}
// === end ===

// === case: replace_type_with_var_multi_annotation ===
// imports: java.util.List
class InputLambdaParamReplaceVarMultiAnnotationSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.forEach((@A @B var x) -> System.out.println(x));
	}
}
// === end ===

// === case: replace_type_with_var_second_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarSecondParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((var x, @A var y) -> x.compareTo(y));
	}
}
// === end ===

// === case: replace_type_with_var_three_params_last_annotated ===
class InputLambdaParamReplaceVarThreeParamsLastSliceViolation {
	@interface A {}

	void m() {
		final F f = (var a, var b, @A var c) -> a + b + c;
	}
}
// === end ===

// === case: replace_type_with_var_three_params_middle_annotated ===
class InputLambdaParamReplaceVarThreeParamsMiddleSliceViolation {
	@interface A {}

	void m() {
		final F f = (var a, @A var b, var c) -> a + b + c;
	}
}
// === end ===

// === case: replace_type_with_var_varargs_annotated ===
class InputLambdaParamReplaceVarVarargsSliceViolation {
	@interface A {}

	void m() {
		final F f = (@A var parts) -> parts.length;
	}
}
// === end ===

// === case: string_arrow_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringArrowAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println("a->b"));
	}
}
// === end ===

// === case: string_paren_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringParenAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(")"));
	}
}
// === end ===

// === case: two_arrows_on_one_line ===
class InputLambdaParamTwoArrowsOnOneLineSliceViolation {
	void m() {
		final F f = a -> () -> System.out.println(a);
	}
}
// === end ===

// === case: var ===
// imports: java.util.List
class InputLambdaParamVarViolation {
	@interface C {}

	@interface D {}

	void annotatedExplicitSingle(List<String> list) {
		list.forEach((@C var x) -> System.out.println(x));
	}

	void annotatedMultiAnnotation(List<String> list) {
		list.forEach((@C @D var x) -> System.out.println(x));
	}
}
// === end ===

// === case: var_multi_param ===
// imports: java.util.List
class InputLambdaParamVarMultiSliceViolation {
	void m(List<String> list) {
		list.sort((x, y) -> x.compareTo(y));
	}
}
// === end ===

// === case: var_single_param ===
// imports: java.util.List
class InputLambdaParamVarSingleSliceViolation {
	void m(List<String> list) {
		list.forEach(x -> System.out.println(x));
	}
}
// === end ===