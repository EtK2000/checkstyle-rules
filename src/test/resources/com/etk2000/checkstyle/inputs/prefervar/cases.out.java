package com.etk2000.checkstyle.inputs.prefervar;

// === case: all_object_arguments_drop_across_a_block_comment ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossBlockCommentSliceViolation {
	void m() {
		final var items = new ArrayList<> /* keep
				*/ ();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_comment_inside_the_parens ===
// imports: java.util.ArrayList
class InputPreferVarCommentInsideParensSliceViolation {
	void m() {
		final var items = new ArrayList<>( /* keep */ );
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_line_break ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossLineBreakSliceViolation {
	void m() {
		final var items = new ArrayList<>
				();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_line_comment ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossLineCommentSliceViolation {
	void m() {
		final var items = new ArrayList<> // keep
				();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_paren_line_break ===
// imports: java.util.ArrayList
class InputPreferVarParenLineBreakSliceViolation {
	void m() {
		final var items = new ArrayList<>
				(
				);
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_survive_an_argument_on_the_next_line ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectArgumentOnNextLineSliceViolation {
	void m() {
		final var items = new ArrayList<Object>
				(4);
		System.out.println(items);
	}
}
// === end ===

// === case: all_primitive_same_type_literals ===
class InputPreferVarLiteralMismatchAllPrimitiveSameTypeLiteralsSliceViolation {
	void m() {
		final var b = true;
		final var bFalse = false;
		final var c = 'a';
		final var d = 5.0;
		final var f = 5.0f;
		final var fUpper = 5.0F;
		final var i = 5;
		final var l = 5L;
	}
}
// === end ===

// === case: annotated_local_variable ===
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotatedLocalVariableViolation {
	void m() {
		@Nonnull
		final var s = "hello";
	}
}
// === end ===

// === case: annotation_multiple ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationMultipleSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (@Deprecated @Nonnull var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: annotation_plus_final ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationPlusFinalSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (@Nonnull final var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: annotation_unbalanced_paren_string ===
class InputPreferVarAnnotationUnbalancedParenStringSliceViolation {
	void m() {
		@SuppressWarnings("(") var s = "";
	}
}
// === end ===

// === case: annotation_with_args ===
class InputPreferVarAnnotationWithArgsSliceViolation {
	void m() {
		@SuppressWarnings("x") var s = "";
	}
}
// === end ===

// === case: anonymous_class_with_object_type_arg ===
// imports: java.util.Comparator
class InputPreferVarDiamondAnonymousClassWithObjectTypeArgSliceViolation {
	void m() {
		final var cmp = new Comparator<>() {
			@Override
			public int compare(Object a, Object b) {
				return 0;
			}
		};
	}
}
// === end ===

// === case: array_type ===
class InputPreferVarArrayTypeSliceViolation {
	void m() {
		var a = new String[5];
	}
}
// === end ===

// === case: auto_detected_generic_var_warns ===
class InputPreferVarGenericReturnAutoDetectedGenericVarSliceViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	void m() {
		final var s = cast("hello");
	}
}
// === end ===

// === case: bare_call_resolution_survives_cyclic_inheritance ===
// imports: java.util.List
class InputPreferVarCycleBareCallSliceViolation extends InputPreferVarCycleBareCallPartner {
	void m() {
		final var items = build();
		System.out.println(items);
	}
}

class InputPreferVarCycleBareCallPartner extends InputPreferVarCycleBareCallSliceViolation {
	List<String> build() {
		return List.of();
	}
}
// === end ===

// === case: boolean_bitwise_operator_warns ===
class InputPreferVarBooleanBitwiseOperatorSliceViolation {
	void m(boolean flag, int x, int y) {
		final boolean b = flag & x < y;
	}
}
// === end ===

// === case: boolean_logical_operators ===
class InputPreferVarBooleanLogicalOperatorsSliceViolation {
	void m(boolean flag, Object o, int x, int y) {
		final var b = !flag && (o instanceof String || x < y);
	}
}
// === end ===

// === case: boxed_type_from_boxed_constructor ===
class InputPreferVarBoxedTypeFromBoxedConstructorSliceViolation {
	void m() {
		final var i = new Integer(5);
		System.out.println(i);
	}
}
// === end ===

// === case: boxed_type_from_boxed_initializer ===
class InputPreferVarBoxedTypeFromBoxedInitializerSliceViolation {
	void m() {
		final var b = Byte.valueOf((byte) 5);
		System.out.println(b);
	}
}
// === end ===

// === case: boxed_type_from_qualified_boxed_initializer ===
class InputPreferVarBoxedTypeFromQualifiedBoxedInitializerSliceViolation {
	void m() {
		final var boxed = java.lang.Integer.valueOf(5);
		System.out.println(boxed);
	}
}
// === end ===

// === case: call_on_a_cast_receiver ===
class InputPreferVarCallOnACastReceiverSliceViolation {
	void m(Object value) {
		final var text = ((CharSequence) value).toString();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_for_each_variable_receiver ===
// imports: java.util.List
class InputPreferVarCallOnAForEachVariableReceiverSliceViolation {
	void m(List<String> names) {
		for (var name : names) {
			final var trimmed = name.trim();
			System.out.println(trimmed);
		}
	}
}
// === end ===

// === case: call_on_a_lambda_parameter_receiver ===
// imports: java.util.List
class InputPreferVarCallOnALambdaParameterReceiverSliceViolation {
	void m(List<String> names) {
		names.forEach(name -> {
			final var upper = name.toUpperCase();
			System.out.println(upper);
		});
	}
}
// === end ===

// === case: call_on_a_new_receiver ===
class InputPreferVarCallOnANewReceiverSliceViolation {
	void m() {
		final var text = new StringBuilder().toString();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_receiver_typed_with_a_same_file_class ===
class InputPreferVarCallOnAReceiverTypedWithASameFileClassSliceViolation {
	static class Holder {
	}

	void m(Holder holder) {
		final var text = holder.toString();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_super_receiver ===
class InputPreferVarCallOnASuperReceiverSliceViolation {
	void m() {
		final var text = super.toString();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_text_block_receiver ===
class InputPreferVarCallOnATextBlockReceiverSliceViolation {
	void m() {
		final var text = """
				hello""".trim();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_an_indexed_receiver ===
class InputPreferVarCallOnAnIndexedReceiverSliceViolation {
	void m(String[] rows) {
		final var first = rows[0].trim();
		System.out.println(first);
	}
}
// === end ===

// === case: call_on_an_inherited_non_generic_method ===
class InputPreferVarCallOnAnInheritedNonGenericMethodSliceBase {
	static String base() {
		return "";
	}
}

class InputPreferVarCallOnAnInheritedNonGenericMethodSliceViolation extends InputPreferVarCallOnAnInheritedNonGenericMethodSliceBase {
	void m() {
		final var value = base();
		System.out.println(value);
	}
}
// === end ===

// === case: cast_to_matching_type ===
class InputPreferVarLiteralMismatchCastToMatchingTypeSliceViolation {
	void m(Object obj, int x) {
		final var cf = x;
		final var cl = x;
		final var cs = (String) obj;
	}
}
// === end ===

// === case: chained_generic_return_warns ===
// imports: com.etk2000.checkstyle.testhelpers.GenericReturnHelper
class InputPreferVarChainViolation {
	void chainedGenericReturnExplicitType() {
		final String s = GenericReturnHelper.create().find(1);
	}

	void chainedGenericReturnVar() {
		final var s = GenericReturnHelper.create().find(1);
	}
}
// === end ===

// === case: classpath_call_return_type_does_not_narrow ===
// imports: java.util.Map
class InputPreferVarClasspathCallReturnTypeDoesNotNarrowSliceViolation {
	void m(Map<String, String> values) {
		var first = values.get("k");
		first = "x";
		System.out.println(first);
	}
}
// === end ===

// === case: code_line_with_comment_ending_in_open_paren ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarCodeLineWithCommentEndingInOpenParenSliceViolation {
	void m() {
		System.out.println("x"); // note (
		final var values = new ArrayList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: comma_in_block_comment_not_multi_var ===
class InputPreferVarCommaInBlockCommentNotMultiVarSliceViolation {
	void m() {
		var x = "ab" /* a, b */;
	}
}
// === end ===

// === case: comma_in_char_literal_not_multi_var ===
class InputPreferVarCommaInCharLiteralNotMultiVarSliceViolation {
	void m() {
		var c = ',';
	}
}
// === end ===

// === case: comma_in_method_call_not_multi_var ===
class InputPreferVarCommaInMethodCallNotMultiVarSliceViolation {
	String m(String a, String b) {
		return a + b;
	}

	void n(String a, String b) {
		var x = m(a, b);
	}
}
// === end ===

// === case: comma_in_string_not_multi_var ===
class InputPreferVarCommaInStringNotMultiVarSliceViolation {
	void m() {
		var x = "a,b";
	}
}
// === end ===

// === case: comment_between_name_and_assignment_unterminated ===
// skip-reason: cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes
// imports: java.util.List
class InputPreferVarCommentBetweenNameAndAssignmentUnterminatedSliceViolation {
	void m(List<String> x) {
		final List<String> l /* note
				spanning */ = x;
		System.out.println(l);
	}
}
// === end ===

// === case: comment_between_type_and_name ===
// imports: java.util.List
class InputPreferVarCommentBetweenTypeAndNameSliceViolation {
	void m(List<String> x) {
		var /* c */ l = x;
	}
}
// === end ===

// === case: comment_between_type_and_name_unterminated ===
// skip-reason: cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes
// imports: java.util.List
class InputPreferVarCommentBetweenTypeAndNameUnterminatedSliceViolation {
	void m(List<String> x) {
		final List<String> /* note
				spanning */ l = x;
		System.out.println(l);
	}
}
// === end ===

// === case: comment_ending_in_open_paren_above_declaration ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarCommentEndingInOpenParenAboveDeclarationSliceViolation {
	void m() {
		// keep this comment intact (
		final var values = new ArrayList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: conditional_initializer_reassigned_beyond_the_arm_type_warns ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarConditionalInitializerReassignedBeyondTheArmTypeSliceViolation {
	void m(boolean flag) {
		List<String> items = flag ? new ArrayList<>() : new ArrayList<>();
		items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: conditional_initializer_with_a_non_new_arm_and_no_type_arguments ===
class InputPreferVarConditionalInitializerWithANonNewArmAndNoTypeArgumentsSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m(boolean flag) {
		final var item = flag ? makeDerived() : new Derived();
		System.out.println(item);
	}
}
// === end ===

// === case: conditional_initializer_with_differing_arms_reassigned_warns ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarConditionalInitializerWithDifferingArmsReassignedSliceViolation {
	void m(boolean flag) {
		List<String> items = flag ? new ArrayList<>() : new LinkedList<>();
		items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: constructor_call ===
// imports: java.util.HashMap
class InputPreferVarConstructorCallSliceViolation {
	void m() {
		final var obj = new Object();
		final var map = new HashMap<String, Integer>();
	}
}
// === end ===

// === case: declared_object_type_args ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarDeclaredObjectTypeArgsSliceViolation {
	void m() {
		final var values = new ArrayList<>();
		final var pairs = new HashMap<>();
		System.out.println(values.size() + pairs.size());
	}
}
// === end ===

// === case: declared_object_type_args_later_non_object ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDeclaredObjectTypeArgsLaterNonObjectSliceViolation {
	void m() {
		final var mixed = new HashMap<Object, String>();
		System.out.println(mixed);
	}
}
// === end ===

// === case: declared_object_type_args_with_constructor_argument ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDeclaredObjectTypeArgsWithConstructorArgumentSliceViolation {
	void m(List<String> names) {
		final var values = new ArrayList<Object>(names);
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_annotation_ending_in_var ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEndingInVarSliceViolation {
	@interface Autovar {}

	void m() {
		@Autovar var x = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_annotation_equal_sign ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "unchecked") var x = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_annotation_on_previous_line ===
// imports: java.util.HashMap
// imports: javax.annotation.Nonnull
class InputPreferVarDiamondAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final var map = new HashMap<>();
	}
}
// === end ===

// === case: diamond_carries_declared_type_arguments ===
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarDiamondCarriesDeclaredTypeArgumentsSliceViolation {
	void m() {
		final var nested = new HashMap<String, List<Integer>>();
		final var concrete = new ArrayList<String>();
		final var iface = new ArrayList<String>();
	}
}
// === end ===

// === case: diamond_in_nested_call_only ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondInNestedCallOnlySliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final List<String> wrapped = wrap(new ArrayList<>());
		System.out.println(wrapped);
	}
}
// === end ===

// === case: diamond_new_in_comment ===
// imports: java.util.ArrayList
class InputPreferVarDiamondNewInCommentSliceViolation {
	void m() {
		final var values = /* new ArrayList<Object>() */ new ArrayList<>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_on_chain_receiver ===
// imports: java.util.List
class InputPreferVarDiamondOnChainReceiverSliceViolation {
	static class Holder<T extends Number> {
		List<String> names() {
			return List.of();
		}
	}

	void m() {
		final var values = new Holder<>().names();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_on_continuation_line ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineSliceViolation {
	void m() {
		final Map<String, String> lookup =
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_on_continuation_line_type_wrapped ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineTypeWrappedSliceViolation {
	void m() {
		final Map<String, String>
				lookup =
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_single_object_arg_with_whitespace ===
// imports: java.util.ArrayList
class InputPreferVarDiamondSingleObjectArgWithWhitespaceSliceViolation {
	void m() {
		var a = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_supplementary_type_name ===
class InputPreferVarDiamondSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o<T> {}

	void m() {
		final var x = new Fo𝛼o<>();
		System.out.println(x);
	}
}
// === end ===

// === case: diamond_switch_expression_all_arms ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondSwitchExpressionAllArmsSliceViolation {
	void m(int key) {
		final var values = switch (key) { case 1 -> new ArrayList<String>(); default -> new LinkedList<String>(); };
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_arm_is_switch ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernaryArmIsSwitchSliceViolation {
	void m(boolean flag, int key) {
		final var values = flag ? switch (key) { case 1 -> new ArrayList<String>(); default -> new LinkedList<String>(); } : new ArrayList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_both_arms ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernaryBothArmsSliceViolation {
	void m(boolean useArray) {
		final var values = useArray ? new ArrayList<String>() : new LinkedList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_nested_all_arms ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernaryNestedAllArmsSliceViolation {
	void m(boolean useArray, boolean useLinked) {
		final var values = useArray ? new ArrayList<String>() : useLinked ? new LinkedList<String>() : new ArrayList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_nested_true_arm ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernaryNestedTrueArmSliceViolation {
	void m(boolean useArray, boolean useLinked) {
		final var values = useArray ? useLinked ? new LinkedList<String>() : new ArrayList<String>() : new ArrayList<String>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_second_arm_below ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernarySecondArmBelowSliceViolation {
	void m(boolean useArray) {
		final List<String> values = useArray ? new ArrayList<>()
				: new LinkedList<>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_three_object_args ===
class InputPreferVarDiamondThreeObjectArgsSliceViolation {
	static class Triple<A, B, C> {}

	void m() {
		var t = new Triple<>();
	}
}
// === end ===

// === case: diamond_top_level_and_nested ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondTopLevelAndNestedSliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final var merged = new ArrayList<String>(wrap(new ArrayList<>()));
		System.out.println(merged);
	}
}
// === end ===

// === case: diamond_var_in_variable_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondVarInVariableNameSliceViolation {
	void m() {
		var myvar = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_with_final ===
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondWithFinalSliceViolation {
	void m() {
		final var s = new LinkedHashSet<>();
	}
}
// === end ===

// === case: double_escaped_backslash_not_multi_var_warns ===
class InputPreferVarDoubleEscapedBackslashSliceViolation {
	void m() {
		final String x = "\\", y = "z";
	}
}
// === end ===

// === case: equals_comparison_not_assignment ===
class InputPreferVarEqualsComparisonNotAssignmentSliceViolation {
	void m(int x, int y) {
		final var b = x == y;
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign_var ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignVarSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_nested_equal_sign ===
class InputPreferVarExplicitArrayInitAnnotationNestedEqualSignSliceViolation {
	@interface Anno {
		String a();
		Inner b();
	}

	@interface Inner {
		String c();
	}

	void m() {
		@Anno(a = "x", b = @Inner(c = "y")) String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_nested_equal_sign_var ===
class InputPreferVarExplicitArrayInitAnnotationNestedEqualSignVarSliceViolation {
	@interface Anno {
		String a();
		Inner b();
	}

	@interface Inner {
		String c();
	}

	void m() {
		@Anno(a = "x", b = @Inner(c = "y")) String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_on_previous_line ===
// imports: javax.annotation.Nonnull
class InputPreferVarExplicitArrayAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final String[] names = {"a", "b"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_paren_in_string ===
class InputPreferVarExplicitArrayInitAnnotationParenInStringSliceViolation {
	void m() {
		@SuppressWarnings("(") String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_brace_in_comment ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitBraceInCommentSliceViolation {
	void m() {
		final var values = new String[] /* {"z"} */ {"a"};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_c_style_name_spelled_var ===
class InputPreferVarExplicitArrayInitCStyleNameSpelledVarSliceViolation {
	void m() {
		String var[] = {"a"};
		System.out.println(var.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_explicit_type ===
class InputPreferVarExplicitArrayInitCommentBeforeNameExplicitTypeSliceViolation {
	void m() {
		final String[] /* keep */ arr = {"a"};
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_var ===
class InputPreferVarExplicitArrayInitCommentBeforeNameVarSliceViolation {
	void m() {
		final String[] /* keep */ arr = {"a"};
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_equals_before_assignment ===
class InputPreferVarExplicitArrayInitCommentEqualsBeforeAssignmentSliceViolation {
	void m() {
		String[] /* = */ arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_empty_typed ===
class InputPreferVarExplicitArrayInitEmptyTypedSliceViolation {
	void m() {
		final String[] a = {};
	}
}
// === end ===

// === case: explicit_array_init_empty_var ===
class InputPreferVarExplicitArrayInitEmptyVarSliceViolation {
	void m() {
		final String[] a = {};
	}
}
// === end ===

// === case: explicit_array_init_generic_declared_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericDeclaredTypeSliceViolation {
	void m(List<String> list) {
		final List<String>[] arr = {list};
	}
}
// === end ===

// === case: explicit_array_init_generic_declared_type_nested ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarExplicitArrayInitGenericDeclaredTypeNestedSliceViolation {
	void m(Map<String, List<Integer>> map) {
		final Map<String, List<Integer>>[] arr = {map};
	}
}
// === end ===

// === case: explicit_array_init_generic_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericTypeSliceViolation {
	void m(List<String> list) {
		final List<String>[] a = {list};
	}
}
// === end ===

// === case: explicit_array_init_list_of ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitListOfSliceViolation {
	void m() {
		final var a = List.of(new Object[]{"a"});
	}
}
// === end ===

// === case: explicit_array_init_method_call_arg ===
class InputPreferVarExplicitArrayInitMethodCallArgSliceViolation {
	void m() {
		final var result = String.join(",", new String[]{"a", "b"});
	}
}
// === end ===

// === case: explicit_array_init_multi_arg_method_call ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitMultiArgMethodCallSliceViolation {
	void m() {
		final var a = List.of(new int[]{1}, new int[]{2}).toArray();
	}
}
// === end ===

// === case: explicit_array_init_multi_dim ===
class InputPreferVarExplicitArrayInitMultiDimSliceViolation {
	void m() {
		final int[][] m = {{1}};
	}
}
// === end ===

// === case: explicit_array_init_multi_space ===
class InputPreferVarExplicitArrayInitMultiSpaceSliceViolation {
	void m() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_qualified_new_type ===
class InputPreferVarExplicitArrayInitQualifiedNewTypeSliceViolation {
	void m() {
		final java.lang.String[] a = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_space_before_brace ===
class InputPreferVarExplicitArrayInitSpaceBeforeBraceSliceViolation {
	void m() {
		final String[] values = {"a"};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_split_across_lines ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitSplitAcrossLinesSliceViolation {
	void m() {
		final int[] values = new int[]
				{1, 2, 3};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_type_name ===
class InputPreferVarExplicitArrayInitSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o {}

	void m() {
		final Fo𝛼o[] a = {new Fo𝛼o()};
		System.out.println(a.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_var_name ===
class InputPreferVarExplicitArrayInitSupplementaryVarNameSliceViolation {
	void m() {
		final String[] 𝛼a = {"x"};
		System.out.println(𝛼a.length);
	}
}
// === end ===

// === case: explicit_array_init_ternary ===
class InputPreferVarExplicitArrayInitTernarySliceViolation {
	void m(boolean cond) {
		final var a = cond ? new int[]{1} : new int[]{2};
	}
}
// === end ===

// === case: explicit_array_init_typed_matching ===
class InputPreferVarExplicitArrayInitTypedMatchingSliceViolation {
	void m() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_var_separated_by_a_comment ===
class InputPreferVarExplicitArrayInitVarSeparatedByACommentSliceViolation {
	void m() {
		final String[]/* keep */arr = {"a"};
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_var_to_primitive ===
class InputPreferVarExplicitArrayInitVarToPrimitiveSliceViolation {
	void m() {
		final int[] a = {1, 2};
	}
}
// === end ===

// === case: explicit_array_init_var_to_string ===
class InputPreferVarExplicitArrayInitVarToStringSliceViolation {
	void m() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: explicit_type_arguments_in_initializer ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarExplicitTypeArgumentsInInitializerSliceViolation {
	void m() {
		final var counts = new HashMap<String, Integer>();
		System.out.println(counts);
	}
}
// === end ===

// === case: factory_initializer_reassigned_beyond_the_returned_type_warns ===
class InputPreferVarFactoryInitializerReassignedBeyondTheReturnedTypeSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m(boolean flag) {
		Base item = makeDerived();
		if (flag)
			item = new Base();
		System.out.println(item);
	}
}
// === end ===

// === case: factory_initializer_reassigned_to_the_constructed_same_file_type ===
class InputPreferVarFactoryInitializerReassignedToTheConstructedSameFileTypeSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m(boolean flag) {
		var item = makeDerived();
		if (flag)
			item = new Derived();
		System.out.println(item);
	}
}
// === end ===

// === case: factory_initializer_reassigned_to_the_returned_type ===
class InputPreferVarFactoryInitializerReassignedToTheReturnedTypeSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Base makeBase() {
		return new Base();
	}

	void m(boolean flag) {
		var item = makeBase();
		if (flag)
			item = new Derived();
		System.out.println(item);
	}
}
// === end ===

// === case: field_assigned_above_shadowing_local ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarFieldAssignedAboveShadowingLocalSliceViolation {
	private List<String> items = new ArrayList<>();

	void m() {
		items = new LinkedList<>();
		var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: final_column_at_final ===
class InputPreferVarFinalColumnAtFinalSliceViolation {
	void m() {
		final var x = 5;
	}
}
// === end ===

// === case: for_each ===
// imports: java.util.List
class InputPreferVarForEachSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotated ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotatedSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_prev_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull final var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_var_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnVarLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull final var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_on_previous_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationOnPreviousLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_blank_between_paren_and_decl ===
// imports: java.util.List
class InputPreferVarForEachBlankBetweenParenAndDeclSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_body_assigns_an_array_on_the_same_line ===
// imports: java.util.List
class InputPreferVarForEachBodyAssignsAnArrayOnTheSameLineSliceViolation {
	static void consume(String[] values) {
		System.out.println(values.length);
	}

	void m(List<String> list, String[] a) {
		for (var s : list) { consume(a = new String[]{"b"}); }
	}
}
// === end ===

// === case: for_each_boxed_element_type_over_a_matching_array ===
class InputPreferVarForEachBoxedElementTypeOverAMatchingArraySliceViolation {
	void m(Integer[] counts) {
		for (var count : counts)
			System.out.println(count);
	}
}
// === end ===

// === case: for_each_final ===
// imports: java.util.List
class InputPreferVarForEachFinalSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (final var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: for_each_final_only_prev_line ===
// imports: java.util.List
class InputPreferVarForEachFinalOnlyPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (final var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_generic_type ===
// imports: java.util.Map
// imports: java.util.Map.Entry
class InputPreferVarForEachGenericTypeSliceViolation {
	void m() {
		final var map = Map.of("a", 1);
		for (var entry : map.entrySet())
			System.out.println(entry);
	}
}
// === end ===

// === case: for_each_open_paren_prev_line ===
// imports: java.util.List
class InputPreferVarForEachOpenParenPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_open_paren_with_trailing_comment ===
// imports: java.util.List
class InputPreferVarForEachOpenParenWithTrailingCommentSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for ( // header
				var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_primitive_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachPrimitiveArrayElementTypeSliceViolation {
	void m(List<int[]> intRows) {
		for (var row : intRows)
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_primitive_element_type_over_an_array ===
class InputPreferVarForEachPrimitiveElementTypeOverAnArraySliceViolation {
	void m(int[] sizes) {
		for (var size : sizes)
			System.out.println(size);
	}
}
// === end ===

// === case: for_each_reference_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachReferenceArrayElementTypeSliceViolation {
	void m(List<String[]> rows) {
		for (var row : rows)
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_two_annotations_prev_lines ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachTwoAnnotationsPrevLinesSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull @SuppressWarnings("unused") var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_init_reassigned_beyond_the_constructed_type_warns ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarForInitReassignedBeyondTheConstructedTypeSliceViolation {
	void m(boolean flag) {
		for (List<String> items = new ArrayList<>(); flag; flag = false) {
			items = new LinkedList<>();
			System.out.println(items);
		}
	}
}
// === end ===

// === case: for_init_reassigned_to_the_constructed_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarForInitReassignedToTheConstructedTypeSliceViolation {
	void m(boolean flag) {
		for (var items = new ArrayList<String>(); flag; flag = false) {
			items = new ArrayList<>();
			System.out.println(items);
		}
	}
}
// === end ===

// === case: for_loop_init ===
class InputPreferVarForLoopInitSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: for_loop_init_reference_type ===
// imports: java.util.Iterator
// imports: java.util.List
class InputPreferVarForLoopInitReferenceTypeSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (var it = list.iterator(); it.hasNext(); )
			System.out.println(it.next());
	}
}
// === end ===

// === case: generic_factory_with_lambda_arguments ===
// imports: java.util.List
class InputPreferVarGenericFactoryWithLambdaArgumentsSliceViolation {
	void m() {
		final var tasks = List.of(() -> System.out.println("x"));
	}
}
// === end ===

// === case: generic_return_declared_on_an_enclosing_class_warns ===
class InputPreferVarGenericReturnDeclaredOnAnEnclosingClassSliceViolation {
	static <T> T make() {
		return null;
	}

	void m() {
		final Runnable task = new Runnable() {
			@Override
			public void run() {
				final var value = make();
				System.out.println(value);
			}
		};
		System.out.println(task);
	}
}
// === end ===

// === case: generic_return_inherited_from_a_same_file_interface_warns ===
interface InputPreferVarGenericReturnInheritedSliceSource {
	default <T> T make() {
		return null;
	}
}

class InputPreferVarGenericReturnInheritedFromASameFileInterfaceSliceViolation implements InputPreferVarGenericReturnInheritedSliceSource {
	void m() {
		final var value = make();
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_inherited_from_a_same_file_superclass_warns ===
class InputPreferVarGenericReturnInheritedSliceBase {
	static <T> T pick() {
		return null;
	}
}

class InputPreferVarGenericReturnInheritedFromASameFileSuperclassSliceViolation extends InputPreferVarGenericReturnInheritedSliceBase {
	void m() {
		final var value = pick();
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_no_parameters_warns ===
class InputPreferVarGenericReturnNoParametersSliceViolation {
	static <T> T make() {
		return null;
	}

	void m() {
		final var value = make();
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_second_type_parameter_warns ===
class InputPreferVarGenericReturnSecondTypeParameterSliceViolation {
	static <K, V> V get(K key) {
		return null;
	}

	void m() {
		final var value = get("a");
		System.out.println(value);
	}
}
// === end ===

// === case: generic_var_with_diamond_new_warns ===
// imports: java.util.ArrayList
class InputPreferVarGenericVarWithDiamondNewSliceViolation {
	static <T> T cast(Object obj) {
		return null;
	}

	void m() {
		final var value = cast(new ArrayList<>());
		System.out.println(value);
	}
}
// === end ===

// === case: generic_var_with_non_object_new_warns ===
// imports: java.util.ArrayList
class InputPreferVarGenericVarWithNonObjectNewSliceViolation {
	static <T> T cast(Object obj) {
		return null;
	}

	void m() {
		final var value = cast(new ArrayList<String>());
		System.out.println(value);
	}
}
// === end ===

// === case: inheritance_cycle_terminates ===
class InputPreferVarInheritanceCycleSliceFirst extends InputPreferVarInheritanceCycleTerminatesSliceViolation {
}

class InputPreferVarInheritanceCycleTerminatesSliceViolation extends InputPreferVarInheritanceCycleSliceFirst {
	void m() {
		final var text = "  x  ".trim();
		System.out.println(text);
	}
}
// === end ===

// === case: inherited_non_generic_call_converts ===
class InputPreferVarInheritedNonGenericCallSliceBase {
	static String base() {
		return "";
	}
}

class InputPreferVarInheritedNonGenericCallConvertsSliceViolation extends InputPreferVarInheritedNonGenericCallSliceBase {
	void m() {
		final var value = base();
		System.out.println(value);
	}
}
// === end ===

// === case: local_generic_class_diamond ===
class InputPreferVarLocalGenericClassDiamondSliceViolation {
	static class Box<T> {
	}

	void m() {
		final var boxed = new Box<String>();
		System.out.println(boxed);
	}
}
// === end ===

// === case: local_variables ===
// imports: java.util.List
class InputPreferVarLocalVariablesSliceViolation {
	void m() {
		final var x = 42;
		final var s = "hello";
		final var list = List.of(1, 2, 3);
		final String[] names = {"a", "b"};
		final String[] numbers = {"1"};
		final int[][] matrix = {{1}, {2}};
		final var parenArr = (new String[]{"x"});
	}
}
// === end ===

// === case: method_call_and_chain ===
class InputPreferVarMethodCallAndChainSliceViolation {
	void m() {
		final var s = String.valueOf(42);
		final var trimmed = "  hello  ".trim().toLowerCase();
	}
}
// === end ===

// === case: mixed_qualified_and_bare_object_type_args ===
// imports: java.util.HashMap
class InputPreferVarDiamondMixedQualifiedAndBareObjectTypeArgsSliceViolation {
	void m() {
		final var map = new HashMap<>();
	}
}
// === end ===

// === case: multi_dim_array ===
class InputPreferVarMultiDimArraySliceViolation {
	void m() {
		var m = new int[3][3];
	}
}
// === end ===

// === case: multi_var_supplementary_before_type_warns ===
class InputPreferVarMultiVarSupplementaryBeforeTypeSliceViolation {
	void m() {
		/* 𝛼 */ final int x = 1, y = 2;
		System.out.println(x + y);
	}
}
// === end ===

// === case: multi_var_ternary_initializer_warns ===
class InputPreferVarMultiVarTernaryInitializerSliceViolation {
	void m(boolean cond, int a, int b, int d) {
		final int x = cond ? a : b, y = d;
	}
}
// === end ===

// === case: multi_var_warns ===
class InputPreferVarMultiVarSliceViolation {
	void m() {
		final int x = 1, y = 2;
	}
}
// === end ===

// === case: multi_variable_declarations ===
// imports: javax.annotation.Nonnull
class InputPreferVarMultiVarViolation {
	void multiVarAnnotated() {
		@Nonnull
		final int x = 1, y = 2;
	}

	void multiVarForInit() {
		for (int i = 0, j = 10; i < j; ++i)
			System.out.println(i);
	}

	void multiVarForInitAnnotated() {
		for (@Nonnull int i = 0, j = 10; i < j; ++i)
			System.out.println(i);
	}

	void multiVarFourVariables() {
		final int w = 1, x = 2, y = 3, z = 4;
	}

	void multiVarLocal() {
		final int x = 1, y = 2;
		final String a = "a", b = "b";
	}

	void multiVarMixedInit() {
		final int x = Integer.parseInt("5"), y = 2;
	}

	void multiVarPartialInit() {
		final int x = 1, y = x;
	}

	void multiVarThreeVariables() {
		final int x = 1, y = 2, z = 3;
	}
}
// === end ===

// === case: multiple_object_type_args ===
// imports: java.util.HashMap
class InputPreferVarDiamondMultipleObjectTypeArgsSliceViolation {
	void m() {
		final var map = new HashMap<>();
	}
}
// === end ===

// === case: name_ends_line_assignment_below ===
// imports: java.util.List
class InputPreferVarNameEndsLineAssignmentBelowSliceViolation {
	static List<String> build() {
		return null;
	}

	void m() {
		final var values
				= build();
		System.out.println(values);
	}
}
// === end ===

// === case: nested_ternary_initializer ===
class InputPreferVarNestedTernaryInitializerSliceViolation {
	void m(boolean cond, boolean flag, String a, String b, String c) {
		final var x = cond ? a : flag ? b : c;
		System.out.println(x);
	}
}
// === end ===

// === case: nested_ternary_multi_var_warns ===
class InputPreferVarNestedTernaryMultiVarSliceViolation {
	void m(boolean cond, boolean flag, int a, int b, int c, int d) {
		final int x = cond ? a : flag ? b : c, y = d;
		System.out.println(x + y);
	}
}
// === end ===

// === case: non_generic_method ===
class InputPreferVarGenericReturnNonGenericMethodSliceViolation {
	static String nonGeneric() {
		return "";
	}

	void m() {
		final var s = nonGeneric();
	}
}
// === end ===

// === case: overload_selection_changes_from_an_enclosing_class_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesFromAnEnclosingClassSliceViolation {
	static class Inner {
		void m() {
			final List<String> items = new ArrayList<>();
			take(items);
		}
	}

	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_selection_changes_on_a_classpath_array_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionChangesOnAClasspathArrayWarnsSliceViolation {
	static BufferedReader[] create() {
		return new BufferedReader[0];
	}

	static String take(BufferedReader[] value) {
		return "BufferedReaders";
	}

	static String take(Closeable[] value) {
		return "Closeables";
	}

	void m() {
		final Closeable[] value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_constructor_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAConstructorArgumentSliceViolation {
	static class Sink {
		Sink(ArrayList<String> values) {
		}

		Sink(List<String> values) {
		}
	}

	void m() {
		final List<String> items = new ArrayList<>();
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_local_class_constructor_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnALocalClassConstructorWarnsSliceViolation {
	void m(boolean flag) {
		class LocalSink {
			LocalSink(ArrayList<String> values) {
			}

			LocalSink(List<String> values) {
			}
		}

		final List<String> value = flag ? new ArrayList<>() : new ArrayList<>();
		System.out.println(new LocalSink(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_parameter_of_fewer_dimensions_warns ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionChangesOnAParameterOfFewerDimensionsWarnsSliceViolation {
	static class FooStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static FooStream[][] create() {
		return new FooStream[0][];
	}

	static String take(Closeable[][] value) {
		return "Closeabless";
	}

	static String take(Object[] value) {
		return "Objects";
	}

	void m() {
		final Object[] value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_super_qualified_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionSuperQualifiedBase {
	void take(ArrayList<String> values) {
		System.out.println(values);
	}

	void take(List<String> values) {
		System.out.println(values);
	}
}

class InputPreferVarOverloadSelectionChangesOnASuperQualifiedCallSliceViolation extends InputPreferVarOverloadSelectionSuperQualifiedBase {
	void m() {
		final List<String> items = new ArrayList<>();
		super.take(items);
	}
}
// === end ===

// === case: overload_selection_changes_under_var_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesUnderVarSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final List<String> items = new ArrayList<>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_changes_via_an_inherited_overload_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadInheritedBase {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}
}

class InputPreferVarOverloadSelectionChangesViaAnInheritedOverloadSliceViolation extends InputPreferVarOverloadInheritedBase {
	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final List<String> items = new ArrayList<>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_changes_with_qualified_types_warns ===
class InputPreferVarOverloadSelectionChangesWithQualifiedTypesSliceViolation {
	static void take(java.util.ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(java.util.List<String> values) {
		System.out.println(values);
	}

	void m() {
		final java.util.List<String> items = new java.util.ArrayList<>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_differs_at_a_later_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionDiffersAtALaterArgumentSliceViolation {
	static void take(int count, ArrayList<String> values) {
		System.out.println(count + values.size());
	}

	static void take(int count, List<String> values) {
		System.out.println(count + values.size());
	}

	void m() {
		final List<String> items = new ArrayList<>();
		take(1, items);
	}
}
// === end ===

// === case: overload_selection_ignores_a_different_arity ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresADifferentAritySliceViolation {
	static void take(List<String> values) {
		System.out.println(values);
	}

	static void take(ArrayList<String> values, int count) {
		System.out.println(count + values.size());
	}

	void m() {
		final var items = new ArrayList<String>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_ignores_a_foreign_receiver ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarOverloadSelectionIgnoresAForeignReceiverSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m(Map<String, String> other) {
		final var items = new ArrayList<String>();
		System.out.println(other.getOrDefault("k", "v") + items.size());
	}
}
// === end ===

// === case: overload_selection_ignores_a_nested_parameter_of_the_same_name ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNestedParameterShadowSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final var items = new ArrayList<String>();
		final Object nested = new Object() {
			void consume(List<String> items) {
				take(items);
			}
		};
		System.out.println(items);
		System.out.println(nested);
	}
}
// === end ===

// === case: overload_selection_ignores_a_sibling_block_use ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresASiblingBlockUseSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m(boolean flag) {
		if (flag) {
			final List<String> items = new ArrayList<>();
			take(items);
		}
		final var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: overload_selection_ignores_a_varargs_overload ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAVarargsOverloadSliceViolation {
	static void take(List<String> values) {
		System.out.println(values);
	}

	static void take(ArrayList<String> values, Object... rest) {
		System.out.println(values.size() + rest.length);
	}

	void m() {
		final var items = new ArrayList<String>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_ignores_an_unrelated_same_file_parameter_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAnUnrelatedSameFileParameterTypeSliceViolation {
	static class Bag {
	}

	static void take(Bag values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final var items = new ArrayList<String>();
		take(items);
	}
}
// === end ===

// === case: overload_selection_on_a_type_variable_parameter_converts ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionOnATypeVariableParameterConvertsSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(String value) {
		return "String";
	}

	static <T extends Closeable> String take(T value) {
		return "T";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_on_an_array_accepting_both_converts ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionOnAnArrayAcceptingBothConvertsSliceViolation {
	static class FooStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static FooStream[] create() {
		return new FooStream[0];
	}

	static String take(Closeable[] value) {
		return "Closeables";
	}

	static String take(Object value) {
		return "Object";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_sees_a_captured_use_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionSeesACapturedUseSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final List<String> items = new ArrayList<>();
		final Runnable task = new Runnable() {
			@Override
			public void run() {
				take(items);
			}
		};
		System.out.println(task);
	}
}
// === end ===

// === case: overload_selection_skips_a_genuinely_unresolvable_parameter_type ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionSkipsAGenuinelyUnresolvableParameterTypeSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(MissingType value) {
		return "MissingType";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_survives_cyclic_inheritance_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarCyclicInheritanceSliceViolation extends InputPreferVarCyclicInheritancePartner {
	void m() {
		final List<String> items = new ArrayList<>();
		take(items);
	}

	void take(List<String> values) {
		System.out.println(values);
	}
}

class InputPreferVarCyclicInheritancePartner extends InputPreferVarCyclicInheritanceSliceViolation {
	void take(ArrayList<String> values) {
		System.out.println(values);
	}
}
// === end ===

// === case: overload_selection_unchanged_on_a_constructor_argument ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedOnAConstructorArgumentSliceViolation {
	static class Sink {
		Sink(List<String> values) {
		}

		Sink(String value) {
		}
	}

	void m() {
		final var items = new ArrayList<String>();
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_unchanged_under_var ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedUnderVarSliceViolation {
	static void take(List<String> values) {
		System.out.println(values);
	}

	static void take(String value) {
		System.out.println(value);
	}

	void m() {
		final var items = new ArrayList<String>();
		take(items);
	}
}
// === end ===

// === case: paren_wrapped_generic_method_warns ===
class InputPreferVarGenericReturnParenWrappedGenericMethodSliceViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	void m() {
		final var s = (cast("hello"));
	}
}
// === end ===

// === case: parse_boolean ===
class InputPreferVarLiteralMismatchParseBooleanSliceViolation {
	void m() {
		final var b = Boolean.parseBoolean("true");
	}
}
// === end ===

// === case: parse_byte ===
class InputPreferVarLiteralMismatchParseByteSliceViolation {
	void m() {
		final var b = Byte.parseByte("5");
		final double bd = Byte.parseByte("5");
		final float bf = Byte.parseByte("5");
		final int bi = Byte.parseByte("5");
		final long bl = Byte.parseByte("5");
		final short bs = Byte.parseByte("5");
	}
}
// === end ===

// === case: parse_double ===
class InputPreferVarLiteralMismatchParseDoubleSliceViolation {
	void m() {
		final var d = Double.parseDouble("5.0");
	}
}
// === end ===

// === case: parse_float ===
class InputPreferVarLiteralMismatchParseFloatSliceViolation {
	void m() {
		final var f = Float.parseFloat("5.0");
		final double fd = Float.parseFloat("5.0");
	}
}
// === end ===

// === case: parse_int ===
class InputPreferVarLiteralMismatchParseIntSliceViolation {
	void m() {
		final var i = Integer.parseInt("5");
		final double id = Integer.parseInt("5");
		final float ifl = Integer.parseInt("5");
		final long il = Integer.parseInt("5");
	}
}
// === end ===

// === case: parse_long ===
class InputPreferVarLiteralMismatchParseLongSliceViolation {
	void m() {
		final var l = Long.parseLong("5");
		final double ld = Long.parseLong("5");
		final float lf = Long.parseLong("5");
	}
}
// === end ===

// === case: parse_short ===
class InputPreferVarLiteralMismatchParseShortSliceViolation {
	void m() {
		final var s = Short.parseShort("5");
		final double sd = Short.parseShort("5");
		final float sf = Short.parseShort("5");
		final int si = Short.parseShort("5");
		final long sl = Short.parseShort("5");
	}
}
// === end ===

// === case: primitive_with_non_literal_expression ===
class InputPreferVarLiteralMismatchPrimitiveWithNonLiteralExpressionSliceViolation {
	void m(int a, byte b, boolean flag) {
		final float fAdd = a + b;
		final int fCast = (byte) a;
		final long lMul = a * b;
		final long lShift = a << b;
		final long tern = flag ? 1 : 2;
		final var piParen = (5);
		final var pfCast = a;
	}
}
// === end ===

// === case: qualified_constructor_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedConstructorNameSliceViolation {
	void m() {
		final var list = new java.util.ArrayList<>();
	}
}
// === end ===

// === case: qualified_new_diamond_matching_qualified_name ===
class InputPreferVarQualifiedNewDiamondMatchingQualifiedNameSliceViolation {
	static class Outer {
		static class Box<T> {
		}
	}

	void m() {
		final var boxed = new Outer.Box<String>();
		System.out.println(boxed);
	}
}
// === end ===

// === case: qualified_new_matching_simple_name ===
class InputPreferVarQualifiedNewMatchingSimpleNameSliceViolation {
	void m() {
		final var o = new java.lang.Object();
		final var sb = new java.lang.StringBuilder();
		System.out.println(o);
		System.out.println(sb);
	}
}
// === end ===

// === case: qualified_new_resolvable_diamond ===
// imports: java.util.List
class InputPreferVarQualifiedNewResolvableDiamondSliceViolation {
	void m() {
		final var names = new java.util.ArrayList<String>();
		System.out.println(names);
	}
}
// === end ===

// === case: qualified_object_type_arg ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<>();
	}
}
// === end ===

// === case: qualified_static_call_generic_var_warns ===
class InputPreferVarQualifiedStaticCallGenericVarSliceViolation {
	void m() {
		final var values = java.util.Collections.emptyList();
		System.out.println(values);
	}
}
// === end ===

// === case: qualified_type ===
// imports: java.util.List
class InputPreferVarQualifiedTypeSliceViolation {
	void m(List<String> x) {
		var l = x;
	}
}
// === end ===

// === case: qualified_type_annotation_on_previous_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarQualifiedTypeAnnotationOnPreviousLineSliceViolation {
	void m(List<String> x) {
		@Nonnull
		final var l = x;
	}
}
// === end ===

// === case: qualified_type_argument_nested_not_widening ===
// imports: java.util.Map
class InputPreferVarQualifiedTypeArgumentNestedNotWideningSliceViolation {
	void m(Map<String, java.util.List<String>> src) {
		final var index = src;
		System.out.println(index);
	}
}
// === end ===

// === case: qualified_type_argument_not_widening ===
// imports: java.util.Map
class InputPreferVarQualifiedTypeArgumentNotWideningSliceViolation {
	static class Types {
		static class Number {
		}
	}

	void m(Map<String, Types.Number> src) {
		final var index = src;
		System.out.println(index);
	}
}
// === end ===

// === case: qualified_type_final ===
// imports: java.util.List
class InputPreferVarQualifiedTypeFinalSliceViolation {
	void m(List<String> x) {
		final var l = x;
	}
}
// === end ===

// === case: reassigned_and_overload_selection_changes_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.LinkedList
class InputPreferVarReassignedAndOverloadSelectionChangesSliceViolation {
	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m(boolean flag) {
		List<String> items = new ArrayList<>();
		if (flag)
			items = new LinkedList<>();
		take(items);
	}
}
// === end ===

// === case: reassigned_between_two_unresolvable_names_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedBetweenTwoUnresolvableNamesSliceViolation {
	void m(boolean flag) {
		List<String> items = new ArrayList<>();
		if (flag)
			items = new Absent();
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_name_shadowed_by_a_nested_class ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarReassignedNameShadowedByANestedClassSliceViolation {
	void m() {
		var items = new ArrayList<String>();
		final Runnable task = new Runnable() {
			private List<String> items;

			@Override
			public void run() {
				items = new LinkedList<>();
			}
		};
		System.out.println(items);
		System.out.println(task);
	}
}
// === end ===

// === case: reassigned_to_a_different_type_warns ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarReassignedToADifferentTypeSliceViolation {
	void m(boolean flag) {
		List<String> items = new ArrayList<>();
		if (flag)
			items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_a_non_constructed_value_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToANonConstructedValueSliceViolation {
	void m(boolean flag, List<String> other) {
		List<String> items = new ArrayList<>();
		if (flag)
			items = other;
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_a_same_simple_name_different_package_warns ===
// imports: java.util.List
class InputPreferVarReassignedToASameSimpleNameDifferentPackageSliceViolation {
	void m(boolean flag) {
		List<String> items = new java.util.ArrayList<>();
		if (flag)
			items = new absent.ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_null ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToNullSliceViolation {
	void m(boolean flag) {
		var items = new ArrayList<String>();
		if (flag)
			items = null;
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_the_same_fqcn_different_spelling ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToTheSameFqcnDifferentSpellingSliceViolation {
	void m(boolean flag) {
		var items = new ArrayList<String>();
		if (flag)
			items = new java.util.ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_the_same_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToTheSameTypeSliceViolation {
	void m(boolean flag) {
		var items = new ArrayList<String>();
		if (flag)
			items = new ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: same_file_array_narrowing_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileArrayNarrowingOverloadWarnsSliceViolation {
	static class FooStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static FooStream[] create() {
		return new FooStream[0];
	}

	static String take(Closeable[] value) {
		return "Closeables";
	}

	static String take(FooStream[] value) {
		return "FooStreams";
	}

	void m() {
		final Closeable[] value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_constructor_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileConstructorOverloadWarnsSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static class Sink {
		Sink(Closeable value) {
		}

		Sink(Box<?> value) {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	void m() {
		final Closeable value = create();
		System.out.println(new Sink(value));
	}
}
// === end ===

// === case: same_file_cyclic_type_hierarchy_terminates ===
// imports: java.io.Closeable
class InputPreferVarSameFileCyclicTypeHierarchyTerminatesSliceViolation {
	interface Shape {
	}

	static class CycA extends CycB implements Closeable {
		@Override
		public void close() {
		}
	}

	static class CycB extends CycA {
	}

	static CycA create() {
		return new CycA();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Shape value) {
		return "Shape";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_double_nested_qualified_parameter_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileDoubleNestedQualifiedParameterOverloadWarnsSliceViolation {
	static class Mid {
		static class Deep implements Closeable {
			@Override
			public void close() {
			}
		}
	}

	static Mid.Deep create() {
		return new Mid.Deep();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Mid.Deep value) {
		return "MidDeep";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_enum_type_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileEnumTypeOverloadWarnsSliceViolation {
	enum Mode implements Closeable {
		ON;

		@Override
		public void close() {
		}
	}

	static Mode create() {
		return Mode.ON;
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Mode value) {
		return "Mode";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_generic_declared_type_overload_warns ===
class InputPreferVarSameFileGenericDeclaredTypeOverloadWarnsSliceViolation {
	static class Holder<T> {
	}

	static class Sub<T> extends Holder<T> {
	}

	static Sub<String> create() {
		return new Sub<>();
	}

	static String take(Holder<String> value) {
		return "Holder";
	}

	static String take(Sub<String> value) {
		return "Sub";
	}

	void m() {
		final Holder<String> value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_generic_type_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileGenericTypeOverloadWarnsSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(Box<?> value) {
		return "Box";
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_inner_non_static_class_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileInnerNonStaticClassOverloadWarnsSliceViolation {
	class InnerStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(InnerStream value) {
		return "InnerStream";
	}

	InnerStream create() {
		return new InnerStream();
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_classpath_parameter_not_accepting_converts ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingClasspathParameterNotAcceptingConvertsSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(BufferedReader value) {
		return "BufferedReader";
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_local_class_constructor_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingLocalClassConstructorOverloadWarnsSliceViolation {
	void m(boolean flag) {
		class LocalStream implements Closeable {
			@Override
			public void close() {
			}
		}

		class LocalSink {
			LocalSink(Closeable value) {
			}

			LocalSink(LocalStream value) {
			}
		}

		final Closeable value = flag ? new LocalStream() : new LocalStream();
		System.out.println(new LocalSink(value));
	}
}
// === end ===

// === case: same_file_narrowing_nested_class_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingNestedClassOverloadWarnsSliceViolation {
	static class FooStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static FooStream create() {
		return new FooStream();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(FooStream value) {
		return "FooStream";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_supertype_parameter_accepting_both_converts ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingSupertypeParameterAcceptingBothConvertsSliceViolation {
	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(AutoCloseable value) {
		return "AutoCloseable";
	}

	static String take(String value) {
		return "String";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_top_level_class_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingTopLevelClassOverloadWarnsSliceViolation {
	static InputPreferVarSameFileTopLevelStream create() {
		return new InputPreferVarSameFileTopLevelStream();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(InputPreferVarSameFileTopLevelStream value) {
		return "InputPreferVarSameFileTopLevelStream";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}

	class InputPreferVarSameFileTopLevelStream implements Closeable {
		@Override
		public void close() {
		}
	}
// === end ===

// === case: same_file_narrowing_unrelated_same_file_parameter_converts ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingUnrelatedSameFileParameterConvertsSliceViolation {
	interface Shape {
	}

	static class Box<T> implements Closeable {
		@Override
		public void close() {
		}
	}

	static Box<String> create() {
		return new Box<>();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Shape value) {
		return "Shape";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_unresolvable_supertype_identity_parameter_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingUnresolvableSupertypeIdentityParameterWarnsSliceViolation {
	static class Weird extends MissingBase implements Closeable {
		@Override
		public void close() {
		}
	}

	static Weird create() {
		return new Weird();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Weird value) {
		return "Weird";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_unresolvable_supertype_parameter_converts ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingUnresolvableSupertypeParameterConvertsSliceViolation {
	interface Shape {
	}

	static class Weird extends MissingBase implements Closeable {
		@Override
		public void close() {
		}
	}

	static Weird create() {
		return new Weird();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Shape value) {
		return "Shape";
	}

	void m() {
		final var value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_via_extends_overload_warns ===
class InputPreferVarSameFileNarrowingViaExtendsOverloadWarnsSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived create() {
		return new Derived();
	}

	static String take(Base value) {
		return "Base";
	}

	static String take(Derived value) {
		return "Derived";
	}

	void m() {
		final Base value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_via_interface_extends_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingViaInterfaceExtendsOverloadWarnsSliceViolation {
	interface Lockable extends Closeable {
	}

	static class LockImpl implements Lockable {
		@Override
		public void close() {
		}
	}

	static LockImpl create() {
		return new LockImpl();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Lockable value) {
		return "Lockable";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_via_interface_overload_warns ===
class InputPreferVarSameFileNarrowingViaInterfaceOverloadWarnsSliceViolation {
	interface Shape {
	}

	static class Circle implements Shape {
	}

	static Circle create() {
		return new Circle();
	}

	static String take(Circle value) {
		return "Circle";
	}

	static String take(Shape value) {
		return "Shape";
	}

	void m() {
		final Shape value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_via_transitive_supertype_overload_warns ===
class InputPreferVarSameFileNarrowingViaTransitiveSupertypeOverloadWarnsSliceViolation {
	interface Shape {
	}

	static class Leaf extends Middle {
	}

	static class Middle implements Shape {
	}

	static Leaf create() {
		return new Leaf();
	}

	static String take(Middle value) {
		return "Middle";
	}

	static String take(Shape value) {
		return "Shape";
	}

	void m() {
		final Shape value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_record_type_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileRecordTypeOverloadWarnsSliceViolation {
	record Handle(int id) implements Closeable {
		@Override
		public void close() {
		}
	}

	static Handle create() {
		return new Handle(1);
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Handle value) {
		return "Handle";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_sealed_interface_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileSealedInterfaceOverloadWarnsSliceViolation {
	sealed interface Res extends Closeable permits ResImpl {
	}

	static final class ResImpl implements Res {
		@Override
		public void close() {
		}
	}

	static ResImpl create() {
		return new ResImpl();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(Res value) {
		return "Res";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_type_extending_a_classpath_type_overload_warns ===
// imports: java.io.BufferedReader
// imports: java.io.StringReader
class InputPreferVarSameFileTypeExtendingAClasspathTypeOverloadWarnsSliceViolation {
	static class MyReader extends BufferedReader {
		MyReader() {
			super(new StringReader(""));
		}
	}

	static MyReader create() {
		return new MyReader();
	}

	static String take(BufferedReader value) {
		return "BufferedReader";
	}

	static String take(MyReader value) {
		return "MyReader";
	}

	void m() {
		final BufferedReader value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_type_shadowing_a_classpath_name_overload_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileTypeShadowingAClasspathNameOverloadWarnsSliceViolation {
	static class BufferedReader implements Closeable {
		@Override
		public void close() {
		}
	}

	static BufferedReader create() {
		return new BufferedReader();
	}

	static String take(BufferedReader value) {
		return "BufferedReader";
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	void m() {
		final Closeable value = create();
		System.out.println(take(value));
	}
}
// === end ===

// === case: sibling_block_same_name_reassignment ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarSiblingBlockSameNameReassignmentSliceViolation {
	void m(boolean flag) {
		if (flag) {
			var items = new ArrayList<String>();
			items = new LinkedList<>();
			System.out.println(items);
		}
		var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: simple_declared_type_qualified_new ===
class InputPreferVarSimpleDeclaredTypeQualifiedNewSliceViolation {
	static class Box<T> {
	}

	void m() {
		final var boxed = new InputPreferVarSimpleDeclaredTypeQualifiedNewSliceViolation.Box<String>();
		System.out.println(boxed);
	}
}
// === end ===

// === case: simple_int ===
class InputPreferVarSimpleIntSliceViolation {
	void m() {
		var x = 5;
	}
}
// === end ===

// === case: simple_string ===
class InputPreferVarSimpleStringSliceViolation {
	void m() {
		var s = "hi";
	}
}
// === end ===

// === case: single_object_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondSingleObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<>();
		final var set = new LinkedHashSet<>();
		final var parenWrapped = (new ArrayList<>());
	}
}
// === end ===

// === case: static_call_generic_var ===
// imports: java.util.Collections
// imports: java.util.List
// imports: java.util.Optional
class InputPreferVarReflectionStaticCallGenericVarSliceViolation {
	void m() {
		final var list = List.of();
		final var opt = Optional.empty();
	}
}
// === end ===

// === case: static_call_non_generic ===
class InputPreferVarReflectionStaticCallNonGenericSliceViolation {
	void m() {
		final var s = String.valueOf(42);
	}
}
// === end ===

// === case: static_import_generic_return_var_warns ===
// imports: static java.util.Collections.emptyList
class InputPreferVarStaticImportGenericReturnVarSliceViolation {
	void m() {
		final var values = emptyList();
		System.out.println(values);
	}
}
// === end ===

// === case: static_import_non_generic_call ===
// imports: static java.util.Arrays.asList
// imports: java.util.List
class InputPreferVarStaticImportNonGenericCallSliceViolation {
	void m() {
		final var names = asList("a", "b");
		System.out.println(names);
	}
}
// === end ===

// === case: static_star_import_call_refused ===
// imports: static java.util.Collections.*
// imports: java.util.List
class InputPreferVarStaticStarImportCallRefusedSliceViolation {
	void m() {
		final List<String> names = emptyList();
		System.out.println(names);
	}
}
// === end ===

// === case: supplementary_char_before_type ===
// imports: java.util.Map
class InputPreferVarSupplementaryCharBeforeTypeSliceViolation {
	void m(Map<String, String> map) {
		/* 𝛼 */ for (var entry : map.entrySet())
			System.out.println(entry);
	}
}
// === end ===

// === case: supplementary_char_in_type_name ===
class InputPreferVarSupplementaryCharInTypeNameSliceViolation {
	static class Fo𝛼o {
	}

	private Fo𝛼o build() {
		return new Fo𝛼o();
	}

	void m() {
		final var value = build();
		final var 𝛼value = build();
	}
}
// === end ===

// === case: try_existing_variable_narrowing_local_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryExistingVariableNarrowingLocalWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		final Closeable existing = openReader();
		try (existing) {
			System.out.println(use(existing));
		}
	}
}
// === end ===

// === case: try_resource_annotated_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceAnnotatedWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (@Deprecated Closeable c = openReader()) {
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_resource_both_of_two_narrow_warn ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceBothOfTwoNarrowWarnSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (Closeable a = openReader(); Closeable b = openReader()) {
			System.out.println(use(a) + use(b));
		}
	}
}
// === end ===

// === case: try_resource_call_on_a_declared_variable_converts ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceCallOnADeclaredVariableConvertsSliceViolation {
	static class Holder {
		BufferedReader open() {
			return new BufferedReader(new StringReader(""));
		}
	}

	static Holder holder = new Holder();

	void m() throws IOException {
		try (var reader = holder.open()) {
			System.out.println(reader.read());
		}
	}
}
// === end ===

// === case: try_resource_constructor_overload_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceConstructorOverloadWarnsSliceViolation {
	static class Sink {
		Sink(BufferedReader reader) {
		}

		Sink(Closeable closeable) {
		}
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (Closeable c = openReader()) {
			System.out.println(new Sink(c));
		}
	}
}
// === end ===

// === case: try_resource_first_of_two_narrows_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceFirstOfTwoNarrowsWarnsSliceViolation {
	static class Wrapper implements Closeable {
		Wrapper(Closeable inner) {
		}

		@Override
		public void close() {
		}
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (Closeable a = openReader(); var b = new Wrapper(null)) {
			System.out.println(use(a) + b);
		}
	}
}
// === end ===

// === case: try_resource_imported_classpath_call_converts ===
// imports: java.io.IOException
// imports: java.io.InputStream
// imports: java.nio.file.Files
// imports: java.nio.file.Path
class InputPreferVarTryResourceImportedClasspathCallConvertsSliceViolation {
	static Path path;

	void m() throws IOException {
		try (var in = Files.newInputStream(path)) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_resource_narrowing_to_a_same_file_type_warns ===
// imports: java.io.Closeable
// imports: java.io.IOException
class InputPreferVarTryResourceNarrowingToASameFileTypeWarnsSliceViolation {
	static class FooStream implements Closeable {
		@Override
		public void close() {
		}
	}

	static FooStream create() {
		return new FooStream();
	}

	static String take(Closeable value) {
		return "Closeable";
	}

	static String take(FooStream value) {
		return "FooStream";
	}

	void m() throws IOException {
		try (Closeable value = create()) {
			System.out.println(take(value));
		}
	}
}
// === end ===

// === case: try_resource_overload_selection_changes_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceOverloadSelectionChangesWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (Closeable c = openReader()) {
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_resource_overload_selection_ignores_a_different_arity ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceOverloadSelectionIgnoresADifferentAritySliceViolation {
	static String describe(Closeable closeable) {
		return "closeable";
	}

	static String describe(BufferedReader reader, int limit) {
		return "reader";
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (var c = openReader()) {
			System.out.println(describe(c));
		}
	}
}
// === end ===

// === case: try_resource_overload_selection_unchanged_converts ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceOverloadSelectionUnchangedConvertsSliceViolation {
	static String describe(Closeable closeable) {
		return "closeable";
	}

	static String describe(String text) {
		return text;
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (var c = openReader()) {
			System.out.println(describe(c));
		}
	}
}
// === end ===

// === case: try_resource_qualified_classpath_call_converts ===
// imports: java.io.IOException
// imports: java.io.InputStream
// imports: java.nio.file.Path
class InputPreferVarTryResourceQualifiedClasspathCallConvertsSliceViolation {
	static Path path;

	void m() throws IOException {
		try (var in = java.nio.file.Files.newInputStream(path)) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_resource_qualified_type_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceQualifiedTypeWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (java.io.Closeable c = openReader()) {
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_resource_referenced_by_a_later_resource_constructor_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceReferencedByALaterResourceConstructorWarnsSliceViolation {
	static class SinkResource implements Closeable {
		SinkResource(BufferedReader reader) {
		}

		SinkResource(Closeable closeable) {
		}

		@Override
		public void close() {
		}
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (Closeable a = openReader(); var sink = new SinkResource(a)) {
			System.out.println(sink);
		}
	}
}
// === end ===

// === case: try_resource_referenced_by_a_nested_try_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceReferencedByANestedTryWarnsSliceViolation {
	static class Wrapper implements Closeable {
		Wrapper(Closeable inner) {
		}

		@Override
		public void close() {
		}
	}

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (Closeable a = openReader()) {
			try (var b = new Wrapper(null)) {
				System.out.println(use(a) + b);
			}
		}
	}
}
// === end ===

// === case: try_resource_use_after_the_try_is_another_variable ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceUseAfterTheTryIsAnotherVariableSliceViolation {
	static Closeable c;

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (var c = openReader()) {
			System.out.println(c);
		}
		System.out.println(use(c));
	}
}
// === end ===

// === case: try_resource_use_in_a_nested_lambda_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceUseInANestedLambdaWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (Closeable c = openReader()) {
			final Runnable task = () -> System.out.println(use(c));
			task.run();
		}
	}
}
// === end ===

// === case: try_resource_use_in_catch_is_another_variable ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceUseInCatchIsAnotherVariableSliceViolation {
	static Closeable c;

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (var c = openReader()) {
			System.out.println(c);
		}
		catch (IOException e) {
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_resource_use_in_finally_is_another_variable ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceUseInFinallyIsAnotherVariableSliceViolation {
	static Closeable c;

	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m() throws IOException {
		try (var c = openReader()) {
			System.out.println(c);
		}
		finally {
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_with_resources ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryWithResourcesSliceViolation {
	void m() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_annotated ===
// imports: java.io.ByteArrayInputStream
// imports: javax.annotation.Nonnull
class InputPreferVarTryWithResourcesAnnotatedSliceViolation {
	void m() throws Exception {
		try (@Nonnull var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_annotation_on_previous_line ===
// imports: java.io.ByteArrayInputStream
// imports: javax.annotation.Nonnull
class InputPreferVarTryResAnnotationOnPreviousLineSliceViolation {
	void m() throws Exception {
		try (@Nonnull var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_body_assigns_an_array_on_the_same_line ===
// imports: java.util.Scanner
class InputPreferVarTryWithResourcesBodyAssignsAnArrayOnTheSameLineSliceViolation {
	void m(Scanner in, String[] a) throws Exception {
		try (var sc = in) { a = new String[]{"b"}; }
		System.out.println(a.length);
	}
}
// === end ===

// === case: try_with_resources_body_call_on_resource_receiver ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryWithResourcesBodyCallOnResourceReceiverSliceViolation {
	void m(byte[] data) throws Exception {
		try (var in = new ByteArrayInputStream(data)) {
			final var all = in.readAllBytes();
			System.out.println(all.length);
		}
	}
}
// === end ===

// === case: try_with_resources_diamond ===
class InputPreferVarTryWithResourcesDiamondSliceViolation {
	static class Pair<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m() throws Exception {
		try (var p = new Pair<String, Integer>()) {
			System.out.println(p);
		}
	}
}
// === end ===

// === case: try_with_resources_factory_initializer ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryWithResourcesFactoryInitializerSliceViolation {
	static ByteArrayInputStream open() {
		return new ByteArrayInputStream(new byte[0]);
	}

	void m() throws Exception {
		try (var in = open()) {
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_generic_type_arguments ===
class InputPreferVarTryWithResourcesGenericTypeArgumentsSliceViolation {
	static class Pair<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m() throws Exception {
		try (var p = new Pair<String, Integer>()) {
			System.out.println(p);
		}
	}
}
// === end ===

// === case: try_with_resources_mixed_ref_and_decl ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryMixedRefAndDeclSliceViolation {
	void m(ByteArrayInputStream existing) throws Exception {
		try (existing; var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(existing.read() + in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_qualified_type ===
class InputPreferVarTryWithResourcesQualifiedTypeSliceViolation {
	void m() throws Exception {
		try (var s = new java.io.ByteArrayInputStream(new byte[0])) {
			s.read();
		}
	}
}
// === end ===

// === case: try_with_resources_two_declarations ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryTwoDeclarationsSliceViolation {
	void m() throws Exception {
		try (var a = new ByteArrayInputStream(new byte[0]);
				var b = new ByteArrayInputStream(new byte[0])) {
			System.out.println(a.read() + b.read());
		}
	}
}
// === end ===

// === case: two_tab_indent ===
class InputPreferVarTwoTabIndentSliceViolation {
	static class Inner {
		void m() {
			var x = 5;
		}
	}
}
// === end ===

// === case: type_args_on_reflection_generic_warns ===
// imports: java.util.Collections
class InputPreferVarReflectionTypeArgsOnReflectionGenericSliceViolation {
	void m() {
		final var list = Collections.<String>emptyList();
	}
}
// === end ===

// === case: type_on_own_line ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineSliceViolation {
	void m(List<String> x) {
		var
				l = x;
	}
}
// === end ===

// === case: type_on_own_line_with_trailing_comment ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineWithTrailingCommentSliceViolation {
	void m(List<String> x) {
		var // note
				l = x;
		System.out.println(l);
	}
}
// === end ===

// === case: wrapped_generic_type_does_not_close_on_line ===
// skip-reason: cannot rewrite the declaration: the declared type arguments do not close on the reported line
// imports: java.util.Map
class InputPreferVarWrappedGenericTypeDoesNotCloseOnLineSliceViolation {
	void m(Map<String, Integer> src) {
		final Map<String,
				Integer> lookup = src;
		System.out.println(lookup);
	}
}
// === end ===

// === case: wrapped_initializer_without_diamond ===
// imports: java.util.List
class InputPreferVarWrappedInitializerWithoutDiamondSliceViolation {
	static List<String> build() {
		return null;
	}

	void m() {
		final var values =
				build();
		System.out.println(values);
	}
}
// === end ===