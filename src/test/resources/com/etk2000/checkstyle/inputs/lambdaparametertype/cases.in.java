package com.etk2000.checkstyle.inputs.lambdaparametertype;

// === case: annotation_arg_char_close_paren ===
class InputLambdaParamAnnotationArgCharCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(')') int a, int y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'int'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: annotation_arg_close_paren ===
class InputLambdaParamAnnotationArgCloseParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(")") String x, String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_comma_string ===
class InputLambdaParamAnnotationArgCommaStringSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("a,b") String x, String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_open_paren ===
class InputLambdaParamAnnotationArgOpenParenSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("(") String x, String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: annotation_arg_string_arrow ===
class InputLambdaParamAnnotationArgStringArrowSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A(") ->") String x, int y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: block_comment_before_arrow ===
// imports: java.util.List
class InputLambdaParamBlockCommentBeforeArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((/* ) -> */ String x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: implicit_array_type ===
// imports: java.util.function.Consumer
class InputLambdaParamImplicitArrayTypeSliceViolation {
	void m() {
		final Consumer<String[]> c = (String[] x) -> System.out.println(x.length); // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: implicit_primitive_type ===
// imports: java.util.function.IntConsumer
class InputLambdaParamImplicitPrimitiveTypeSliceViolation {
	void m() {
		final IntConsumer c = (int x) -> System.out.println(x); // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: nested_lambda_inner_annotation_arg_paren ===
// imports: java.util.List
class InputLambdaParamNestedInnerAnnotationArgParenSliceViolation {
	@interface A {}

	void m(List<List<String>> lists) {
		lists.forEach(inner -> inner.forEach((@A(")") String y) -> System.out.println(y))); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: parens_braced_body ===
// imports: java.util.List
class InputLambdaParamParensBracedBodySliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> { // violation: Remove unnecessary parentheses around single lambda parameter.
			System.out.println(x);
		});
	}
}
// === end ===

// === case: parens_expression_body ===
// imports: java.util.List
class InputLambdaParamParensExpressionBodySliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> System.out.println(x)); // violation: Remove unnecessary parentheses around single lambda parameter.
	}
}
// === end ===

// === case: remove_type_c_style_array_interior_space ===
class InputLambdaParamRemoveTypeCStyleArrayInteriorSpaceSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[ ], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_last ===
class InputLambdaParamRemoveTypeCStyleArrayLastSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a, int y[]) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_middle ===
class InputLambdaParamRemoveTypeCStyleArrayMiddleSliceViolation {
	void m() {
		final TriConsumer<X, Y, Z> t = (int a, int b[], int c) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_multi ===
class InputLambdaParamRemoveTypeCStyleArrayMultiSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_multidim ===
class InputLambdaParamRemoveTypeCStyleArrayMultidimSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[][], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_single ===
// imports: java.util.function.Consumer
class InputLambdaParamRemoveTypeCStyleArraySingleSliceViolation {
	void m() {
		final Consumer<X> c = (int a[]) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_space_before_bracket ===
class InputLambdaParamRemoveTypeCStyleArraySpaceBeforeBracketSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a [], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_tab_before_name ===
class InputLambdaParamRemoveTypeCStyleArrayTabBeforeNameSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int	a[], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int[] a[], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_array_with_java_style_type_spaced ===
class InputLambdaParamRemoveTypeCStyleArrayWithJavaStyleTypeSpacedSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int [ ] a [ ], int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_mixed_with_java_style ===
class InputLambdaParamRemoveTypeCStyleMixedWithJavaStyleSliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int a[], int[] b) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_c_style_object_array ===
class InputLambdaParamRemoveTypeCStyleObjectArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (String a[], String y) -> {}; // violation: Lambda parameter should use implicit type instead of 'String'. // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_java_style_array ===
class InputLambdaParamRemoveTypeJavaStyleArraySliceViolation {
	void m() {
		final BiConsumer<X, Y> c = (int[] a, int y) -> {}; // violation: Lambda parameter should use implicit type instead of 'int'. // violation: Lambda parameter should use implicit type instead of 'int'.
	}
}
// === end ===

// === case: remove_type_multi_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeMultiSliceViolation {
	void m(List<String> list) {
		list.sort((String x, String y) -> x.compareTo(y)); // violation: Lambda parameter should use implicit type instead of 'String'. // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_single_param ===
// imports: java.util.List
class InputLambdaParamRemoveTypeSingleSliceViolation {
	void m(List<String> list) {
		list.forEach((String x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: remove_type_tab_between_type_and_name ===
// imports: java.util.List
class InputLambdaParamRemoveTypeTabSliceViolation {
	void m(List<String> list) {
		list.forEach((String	x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarAnnotatedSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.forEach((@A String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_annotation_with_args ===
class InputLambdaParamReplaceVarAnnotationWithArgsSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A("v") String x, String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_both_params_annotated ===
// imports: java.util.List
class InputLambdaParamReplaceVarBothParamsAnnotatedSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.sort((@A String x, @B String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_annotated ===
class InputLambdaParamReplaceVarCStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[], String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'int'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_both ===
class InputLambdaParamReplaceVarCStyleBothSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[], int b[]) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'int'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_multidim ===
class InputLambdaParamReplaceVarCStyleMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int a[][], String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'int'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int b[]) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_c_style_non_annotated_multidim ===
class InputLambdaParamReplaceVarCStyleNonAnnotatedMultidimSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int b[][]) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_first_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarFirstParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((@A String x, String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_java_style_annotated ===
class InputLambdaParamReplaceVarJavaStyleAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A int[] a, String y) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'int'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_java_style_non_annotated ===
class InputLambdaParamReplaceVarJavaStyleNonAnnotatedSliceViolation {
	@interface A {}

	void m() {
		final BiConsumer<X, Y> c = (@A String x, int[] b) -> {}; // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'int'.
	}
}
// === end ===

// === case: replace_type_with_var_multi_annotation ===
// imports: java.util.List
class InputLambdaParamReplaceVarMultiAnnotationSliceViolation {
	@interface A {}

	@interface B {}

	void m(List<String> list) {
		list.forEach((@A @B String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: replace_type_with_var_second_param_annotated_multi ===
// imports: java.util.List
class InputLambdaParamReplaceVarSecondParamAnnotatedMultiSliceViolation {
	@interface A {}

	void m(List<String> list) {
		list.sort((String x, @A String y) -> x.compareTo(y)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'. // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: string_arrow_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringArrowAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((String x) -> System.out.println("a->b")); // violation: Lambda parameter should use implicit type instead of 'String'.
	}
}
// === end ===

// === case: string_paren_after_arrow_unchanged ===
// imports: java.util.List
class InputLambdaParamStringParenAfterArrowSliceViolation {
	void m(List<String> list) {
		list.forEach((x) -> System.out.println(")")); // violation: Remove unnecessary parentheses around single lambda parameter.
	}
}
// === end ===

// === case: var ===
// imports: java.util.List
class InputLambdaParamVarViolation {
	@interface C {}

	@interface D {}

	void annotatedExplicitSingle(List<String> list) {
		list.forEach((@C String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}

	void annotatedMultiAnnotation(List<String> list) {
		list.forEach((@C @D String x) -> System.out.println(x)); // violation: Lambda parameter with annotation should use 'var' instead of 'String'.
	}
}
// === end ===

// === case: var_multi_param ===
// imports: java.util.List
class InputLambdaParamVarMultiSliceViolation {
	void m(List<String> list) {
		list.sort((var x, var y) -> x.compareTo(y)); // violation: Lambda parameter should use implicit type instead of 'var'. // violation: Lambda parameter should use implicit type instead of 'var'.
	}
}
// === end ===

// === case: var_single_param ===
// imports: java.util.List
class InputLambdaParamVarSingleSliceViolation {
	void m(List<String> list) {
		list.forEach((var x) -> System.out.println(x)); // violation: Lambda parameter should use implicit type instead of 'var'.
	}
}
// === end ===