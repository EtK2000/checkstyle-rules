package com.etk2000.checkstyle.inputs.lambdaparametertype;

// === case: annotation_arg_char_close_paren ===
class InputLambdaParamAnnotationArgCharCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(')') int a, int y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'int'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: annotation_arg_close_paren ===
class InputLambdaParamAnnotationArgCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(")") String x, String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_comma_string ===
class InputLambdaParamAnnotationArgCommaStringSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("a,b") String x, String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_open_paren ===
class InputLambdaParamAnnotationArgOpenParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("(") String x, String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_string_arrow ===
class InputLambdaParamAnnotationArgStringArrowSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(") ->") String x, int y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: block_comment_before_annotation ===
// imports: java.util.List
class InputLambdaParamBlockCommentBeforeAnnotationSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((/* c */ @A String x) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: block_comment_before_arrow ===
// imports: java.util.List
class InputLambdaParamBlockCommentBeforeArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((/* ) -> */ String x) -> System.out.println(x)); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: block_comment_trailing_name ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameSliceViolation {
	void m(List<String> list) {
		list.forEach((String x /* c */) -> System.out.println(x)); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: block_comment_trailing_name_annotated ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameAnnotatedSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((@A String x /* c */) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: block_comment_trailing_name_second_param ===
// imports: java.util.List
class InputLambdaParamBlockCommentTrailingNameSecondParamSliceViolation {
	void m(List<String> list) {
		list.sort((String x, String y /* c */) -> x.compareTo(y)); // violation: Lambda parameter must use an implicit type instead of 'String'. // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: implicit_array_type ===
// imports: java.util.function.Consumer
class InputLambdaParamImplicitArrayTypeSliceViolation {
	void m() {
		final Consumer<String[]> c = (String[] x) -> System.out.println(x.length); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: implicit_primitive_type ===
// imports: java.util.function.IntConsumer
class InputLambdaParamImplicitPrimitiveTypeSliceViolation {
	void m() {
		final IntConsumer c = (int x) -> System.out.println(x); // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: nested_lambda_inner_annotation_arg_paren ===
// imports: java.util.List
class InputLambdaParamNestedInnerAnnotationArgParenSliceViolation {
	@interface A {}

	void m(List<List<String>> lists) {
		lists.forEach(inner -> inner.forEach((@A(")") String y) -> System.out.println(y))); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: no_space_before_arrow ===
class InputLambdaParamNoSpaceBeforeArrowSliceViolation {
	void m() {
		final F f = (String[] s)-> s.length; // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: parens_braced_body ===
// imports: java.util.List
class InputLambdaParamParensBracedBodySliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> { // violation: Remove the unnecessary parentheses around the single lambda parameter.
			System.out.println(x);
		});
	}
}
// === end ===

// === case: parens_expression_body ===
// imports: java.util.List
class InputLambdaParamParensExpressionBodySliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> System.out.println(x)); // violation: Remove the unnecessary parentheses around the single lambda parameter.
	}
}
// === end ===

// === case: remove_type_c_style_array_interior_space ===
class InputLambdaParamRemoveTypeCStyleArrayInteriorSpaceSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[ ], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_last ===
class InputLambdaParamRemoveTypeCStyleArrayLastSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a, int y[]) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_middle ===
class InputLambdaParamRemoveTypeCStyleArrayMiddleSliceViolation {
	void m() {
		final TriConsumer<X, Y, Z> t = (int a, int b[], int c) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_multi ===
class InputLambdaParamRemoveTypeCStyleArrayMultiSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_multidim ===
class InputLambdaParamRemoveTypeCStyleArrayMultidimSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[][], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_single ===
// imports: java.util.function.Consumer
class InputLambdaParamRemoveTypeCStyleArraySingleSliceViolation {
	void m() {
		final Consumer<X> c = (int a[]) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_space_before_bracket ===
class InputLambdaParamRemoveTypeCStyleArraySpaceBeforeBracketSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a [], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_tab_before_name ===
class InputLambdaParamRemoveTypeCStyleArrayTabBeforeNameSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int	a[], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int[] a[], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type_spaced ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSpacedSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int [ ] a [ ], int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_mixed_with_java_style ===
class InputLambdaParamRemoveTypeCStyleMixedWithJavaStyleSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[], int[] b) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_object_array ===
class InputLambdaParamRemoveTypeCStyleObjectArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (String a[], String y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'String'. // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_generic_array_param ===
class InputLambdaParamRemoveTypeGenericArrayParamSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (Map<String, Integer> m, Integer x[]) -> 0; // violation: Lambda parameter must use an implicit type instead of 'Map'. // violation: Lambda parameter must use an implicit type instead of 'Integer'.
	}
}
// === end ===

// === case: remove_type_generic_param_with_comma ===
class InputLambdaParamRemoveTypeGenericParamWithCommaSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (Map<String, Integer> m, Integer x) -> m.size() + x; // violation: Lambda parameter must use an implicit type instead of 'Map'. // violation: Lambda parameter must use an implicit type instead of 'Integer'.
	}
}
// === end ===

// === case: remove_type_generic_single_param ===
class InputLambdaParamRemoveTypeGenericSingleParamSliceViolation {
	void m() {
		final Function<X, Z> c = (Map<String, Integer> m) -> m.size(); // violation: Lambda parameter must use an implicit type instead of 'Map'.
	}
}
// === end ===

// === case: remove_type_java_style_array ===
class InputLambdaParamRemoveTypeJavaStyleArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int[] a, int y) -> {}; // violation: Lambda parameter must use an implicit type instead of 'int'. // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_multi_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeMultiSliceViolation {
	void m(List<String> list) {
		list.sort((String x, String y) -> x.compareTo(y)); // violation: Lambda parameter must use an implicit type instead of 'String'. // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_nested_generics_param ===
class InputLambdaParamRemoveTypeNestedGenericsParamSliceViolation {
	void m() {
		final BiFunction<X, Y, Z> c = (Map<String, List<Integer>> m, Map<Integer, String> n) -> {}; // violation: Lambda parameter must use an implicit type instead of 'Map'. // violation: Lambda parameter must use an implicit type instead of 'Map'.
	}
}
// === end ===

// === case: remove_type_single_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeSingleSliceViolation {
	void m(List<String> list) {
		list.forEach((String x) -> System.out.println(x)); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_tab_between_type_and_name ===
// imports: java.util.List
class InputLambdaParamRemoveTypeTabSliceViolation {
	void m(List<String> list) {
		list.forEach((String	x) -> System.out.println(x)); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_trailing_space_in_list ===
class InputLambdaParamRemoveTypeTrailingSpaceSliceViolation {
	void m() {
		final F f = (String a , String b , String c ) -> a + b + c; // violation: Lambda parameter must use an implicit type instead of 'String'. // violation: Lambda parameter must use an implicit type instead of 'String'. // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_varargs_primitive ===
class InputLambdaParamRemoveTypeVarargsPrimitiveSliceViolation {
	void m() {
		final F f = (int... xs) -> xs.length; // violation: Lambda parameter must use an implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_varargs_reference ===
class InputLambdaParamRemoveTypeVarargsReferenceSliceViolation {
	void m() {
		final F f = (String... parts) -> parts.length; // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_wildcard_bare ===
class InputLambdaParamRemoveTypeWildcardBareSliceViolation {
	void m() {
		final F f = (List<?> n) -> n.size(); // violation: Lambda parameter must use an implicit type instead of 'List'.
	}
}
// === end ===

// === case: remove_type_wildcard_lower ===
class InputLambdaParamRemoveTypeWildcardLowerSliceViolation {
	void m() {
		final F f = (List<? super Integer> n) -> n.size(); // violation: Lambda parameter must use an implicit type instead of 'List'.
	}
}
// === end ===

// === case: remove_type_wildcard_upper ===
class InputLambdaParamRemoveTypeWildcardUpperSliceViolation {
	void m() {
		final F f = (List<? extends Number> n) -> n.size(); // violation: Lambda parameter must use an implicit type instead of 'List'.
	}
}
// === end ===

// === case: replace_type_with_var_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarAnnotatedSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((@A String x) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_annotation_with_args ===
class InputLambdaParamReplaceVarAnnotationWithArgsSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("v") String x, String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
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
		final BiFunction<X, Y, Z> c = (@A(x = 1, y = 2) Map<String, Integer> m, Integer x) -> 0; // violation: Annotated lambda parameter must use 'var' instead of 'Map'. // violation: Annotated lambda parameter must use 'var' instead of 'Integer'.
	}
}
// === end ===

// === case: replace_type_with_var_both_params_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarBothParamsAnnotatedSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.sort((@A String x, @B String y) -> x.compareTo(y)); // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_annotated ===
class InputLambdaParamReplaceVarCStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[], String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'int'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_both ===
class InputLambdaParamReplaceVarCStyleBothSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[], int b[]) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'int'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_multidim ===
class InputLambdaParamReplaceVarCStyleMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[][], String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'int'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int b[]) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated_multidim ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int b[][]) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_first_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarFirstParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((@A String x, String y) -> x.compareTo(y)); // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_java_style_annotated ===
class InputLambdaParamReplaceVarJavaStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int[] a, String y) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'int'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_java_style_non_annotated ===
class InputLambdaParamReplaceVarJavaStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int[] b) -> {}; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_multi_annotation ===
// imports: java.util.List
class InputLambdaParamReplaceVarMultiAnnotationSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.forEach((@A @B String x) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_second_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarSecondParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((String x, @A String y) -> x.compareTo(y)); // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_three_params_last_annotated ===
class InputLambdaParamReplaceVarThreeParamsLastSliceViolation {
	@interface A {}

	void m() {
		final F f = (String a, String b, @A String c) -> a + b + c; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_three_params_middle_annotated ===
class InputLambdaParamReplaceVarThreeParamsMiddleSliceViolation {
	@interface A {}

	void m() {
		final F f = (String a, @A String b, String c) -> a + b + c; // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'. // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_varargs_annotated ===
class InputLambdaParamReplaceVarVarargsSliceViolation {
	@interface A {}

	void m() {
		final F f = (@A String... parts) -> parts.length; // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: string_arrow_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringArrowAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((String x) -> System.out.println("a->b")); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: string_paren_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringParenAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> System.out.println(")")); // violation: Remove the unnecessary parentheses around the single lambda parameter.
	}
}
// === end ===

// === case: two_arrows_on_one_line ===
class InputLambdaParamTwoArrowsOnOneLineSliceViolation {
	void m() {
		final F f = (String a) -> () -> System.out.println(a); // violation: Lambda parameter must use an implicit type instead of 'String'.
	}
}
// === end ===

// === case: var ===
// imports: java.util.List
class InputLambdaParamVarViolation {
	@interface C {}

	@interface D {}

	void annotatedExplicitSingle(List<String> list) {
		list.forEach((@C String x) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}

	void annotatedMultiAnnotation(List<String> list) {
		list.forEach((@C @D String x) -> System.out.println(x)); // violation: Annotated lambda parameter must use 'var' instead of 'String'.
	}
}
// === end ===

// === case: var_multi_param ===
// imports: java.util.List
class InputLambdaParamVarMultiSliceViolation {
	void m(List<String> list) {
		list.sort((var x, var y) -> x.compareTo(y)); // violation: Lambda parameter must use an implicit type instead of 'var'. // violation: Lambda parameter must use an implicit type instead of 'var'.
	}
}
// === end ===

// === case: var_single_param ===
// imports: java.util.List
class InputLambdaParamVarSingleSliceViolation {
	void m(List<String> list) {
		list.forEach((var x) -> System.out.println(x)); // violation: Lambda parameter must use an implicit type instead of 'var'.
	}
}
// === end ===