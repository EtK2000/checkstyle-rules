package com.etk2000.checkstyle.inputs.prefervar;

// === case: all_object_arguments_drop_across_a_block_comment ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossBlockCommentSliceViolation {
	void m() {
		final ArrayList<Object> items = new ArrayList<> /* keep // violation: Local variable must use 'var' instead of an explicit type.
				*/ ();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_comment_inside_the_parens ===
// imports: java.util.ArrayList
class InputPreferVarCommentInsideParensSliceViolation {
	void m() {
		final ArrayList<Object> items = new ArrayList<>( /* keep */ ); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_line_break ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossLineBreakSliceViolation {
	void m() {
		final ArrayList<Object> items = new ArrayList<> // violation: Local variable must use 'var' instead of an explicit type.
				();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_line_comment ===
// imports: java.util.ArrayList
class InputPreferVarAllObjectDropAcrossLineCommentSliceViolation {
	void m() {
		final ArrayList<Object> items = new ArrayList<> // keep // violation: Local variable must use 'var' instead of an explicit type.
				();
		System.out.println(items);
	}
}
// === end ===

// === case: all_object_arguments_drop_across_a_paren_line_break ===
// imports: java.util.ArrayList
class InputPreferVarParenLineBreakSliceViolation {
	void m() {
		final ArrayList<Object> items = new ArrayList<> // violation: Local variable must use 'var' instead of an explicit type.
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
		final ArrayList<Object> items = new ArrayList<> // violation: Local variable must use 'var' instead of an explicit type.
				(4);
		System.out.println(items);
	}
}
// === end ===

// === case: all_primitive_same_type_literals ===
class InputPreferVarLiteralMismatchAllPrimitiveSameTypeLiteralsSliceViolation {
	void m() {
		final boolean b = true; // violation: Local variable must use 'var' instead of an explicit type.
		final boolean bFalse = false; // violation: Local variable must use 'var' instead of an explicit type.
		final char c = 'a'; // violation: Local variable must use 'var' instead of an explicit type.
		final double d = 5.0; // violation: Local variable must use 'var' instead of an explicit type.
		final float f = 5.0f; // violation: Local variable must use 'var' instead of an explicit type.
		final float fUpper = 5.0F; // violation: Local variable must use 'var' instead of an explicit type.
		final int i = 5; // violation: Local variable must use 'var' instead of an explicit type.
		final long l = 5L; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: annotated_local_variable ===
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotatedLocalVariableViolation {
	void m() {
		@Nonnull
		final String s = "hello"; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: annotation_multiple ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationMultipleSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (@Deprecated @Nonnull String i : l) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (@Nonnull final String i : l) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(i);
	}
}
// === end ===

// === case: annotation_unbalanced_paren_string ===
class InputPreferVarAnnotationUnbalancedParenStringSliceViolation {
	void m() {
		@SuppressWarnings("(") String s = ""; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: annotation_with_args ===
class InputPreferVarAnnotationWithArgsSliceViolation {
	void m() {
		@SuppressWarnings("x") String s = ""; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: anonymous_class_with_object_type_arg ===
// imports: java.util.Comparator
class InputPreferVarDiamondAnonymousClassWithObjectTypeArgSliceViolation {
	void m() {
		final var cmp = new Comparator<Object>() { // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		String[] a = new String[5]; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: auto_detected_generic_var ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnAutoDetectedGenericVarSliceViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	void m() {
		final var s = cast("hello"); // violation (warning): Using 'var' with 'cast' loses generic type information, consider using an explicit type.
	}
}
// === end ===

// === case: bare_call_resolution_survives_cyclic_inheritance ===
// imports: java.util.List
class InputPreferVarCycleBareCallSliceViolation extends InputPreferVarCycleBareCallPartner {
	void m() {
		final List<String> items = build(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}

class InputPreferVarCycleBareCallPartner extends InputPreferVarCycleBareCallSliceViolation {
	List<String> build() {
		return List.of();
	}
}
// === end ===

// === case: boxed_type_from_boxed_constructor ===
class InputPreferVarBoxedTypeFromBoxedConstructorSliceViolation {
	void m() {
		final Integer i = new Integer(5); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(i);
	}
}
// === end ===

// === case: boxed_type_from_boxed_initializer ===
class InputPreferVarBoxedTypeFromBoxedInitializerSliceViolation {
	void m() {
		final Byte b = Byte.valueOf((byte) 5); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(b);
	}
}
// === end ===

// === case: boxed_type_from_qualified_boxed_initializer ===
class InputPreferVarBoxedTypeFromQualifiedBoxedInitializerSliceViolation {
	void m() {
		final java.lang.Integer boxed = java.lang.Integer.valueOf(5); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(boxed);
	}
}
// === end ===

// === case: call_on_a_cast_receiver ===
class InputPreferVarCallOnACastReceiverSliceViolation {
	void m(Object value) {
		final String text = ((CharSequence) value).toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_for_each_variable_receiver ===
// imports: java.util.List
class InputPreferVarCallOnAForEachVariableReceiverSliceViolation {
	void m(List<String> names) {
		for (var name : names) {
			final String trimmed = name.trim(); // violation: Local variable must use 'var' instead of an explicit type.
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
			final String upper = name.toUpperCase(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(upper);
		});
	}
}
// === end ===

// === case: call_on_a_new_receiver ===
class InputPreferVarCallOnANewReceiverSliceViolation {
	void m() {
		final String text = new StringBuilder().toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_receiver_typed_with_a_same_file_class ===
class InputPreferVarCallOnAReceiverTypedWithASameFileClassSliceViolation {
	static class Holder {
	}

	void m(Holder holder) {
		final String text = holder.toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_super_receiver ===
class InputPreferVarCallOnASuperReceiverSliceViolation {
	void m() {
		final String text = super.toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_text_block_receiver ===
class InputPreferVarCallOnATextBlockReceiverSliceViolation {
	void m() {
		final String text = """ // violation: Local variable must use 'var' instead of an explicit type.
				hello""".trim();
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_an_indexed_receiver ===
class InputPreferVarCallOnAnIndexedReceiverSliceViolation {
	void m(String[] rows) {
		final String first = rows[0].trim(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final String value = base(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: cast_to_matching_type ===
class InputPreferVarLiteralMismatchCastToMatchingTypeSliceViolation {
	void m(Object obj, int x) {
		final float cf = (float) x; // violation: Local variable must use 'var' instead of an explicit type.
		final long cl = (long) x; // violation: Local variable must use 'var' instead of an explicit type.
		final String cs = (String) obj; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: chained_generic_return ===
// skip-reason: declaration already uses 'var'
// imports: com.etk2000.checkstyle.testhelpers.GenericReturnHelper
class InputPreferVarChainViolation {
	void chainedGenericReturnExplicitType() {
		final String s = GenericReturnHelper.create().find(1);
	}

	void chainedGenericReturnVar() {
		final var s = GenericReturnHelper.create().find(1); // violation (warning): Using 'var' with 'find' loses generic type information, consider using an explicit type.
	}
}
// === end ===

// === case: classpath_call_return_type_does_not_narrow ===
// imports: java.util.Map
class InputPreferVarClasspathCallReturnTypeDoesNotNarrowSliceViolation {
	void m(Map<String, String> values) {
		String first = values.get("k"); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: comma_in_block_comment_not_multi_var ===
class InputPreferVarCommaInBlockCommentNotMultiVarSliceViolation {
	void m() {
		String x = "ab" /* a, b */; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: comma_in_char_literal_not_multi_var ===
class InputPreferVarCommaInCharLiteralNotMultiVarSliceViolation {
	void m() {
		char c = ','; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: comma_in_method_call_not_multi_var ===
class InputPreferVarCommaInMethodCallNotMultiVarSliceViolation {
	String m(String a, String b) {
		return a + b;
	}

	void n(String a, String b) {
		String x = m(a, b); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: comma_in_string_not_multi_var ===
class InputPreferVarCommaInStringNotMultiVarSliceViolation {
	void m() {
		String x = "a,b"; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: comment_between_name_and_assignment_unterminated ===
// skip-reason: reported position is not a declaration this fixer recognises
// imports: java.util.List
class InputPreferVarCommentBetweenNameAndAssignmentUnterminatedSliceViolation {
	void m(List<String> x) {
		final List<String> l /* note // violation: Local variable must use 'var' instead of an explicit type.
				spanning */ = x;
		System.out.println(l);
	}
}
// === end ===

// === case: comment_between_type_and_name ===
// imports: java.util.List
class InputPreferVarCommentBetweenTypeAndNameSliceViolation {
	void m(List<String> x) {
		List<String> /* c */ l = x; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: comment_between_type_and_name_unterminated ===
// skip-reason: reported position is not a declaration this fixer recognises
// imports: java.util.List
class InputPreferVarCommentBetweenTypeAndNameUnterminatedSliceViolation {
	void m(List<String> x) {
		final List<String> /* note // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: conditional_initializer_reassigned_beyond_the_arm_type ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarConditionalInitializerReassignedBeyondTheArmTypeSliceViolation {
	void m(boolean flag) {
		List<String> items = flag ? new ArrayList<>() : new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Base item = flag ? makeDerived() : new Derived(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(item);
	}
}
// === end ===

// === case: conditional_initializer_with_differing_arms_reassigned ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarConditionalInitializerWithDifferingArmsReassignedSliceViolation {
	void m(boolean flag) {
		List<String> items = flag ? new ArrayList<>() : new LinkedList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: constructor_call ===
// imports: java.util.HashMap
class InputPreferVarConstructorCallSliceViolation {
	void m() {
		final Object obj = new Object(); // violation: Local variable must use 'var' instead of an explicit type.
		final HashMap<String, Integer> map = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: declared_object_type_args ===
// multi-fix-expected
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarDeclaredObjectTypeArgsSliceViolation {
	void m() {
		final List<Object> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final Map<Object, Object> pairs = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values.size() + pairs.size());
	}
}
// === end ===

// === case: declared_object_type_args_later_non_object ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDeclaredObjectTypeArgsLaterNonObjectSliceViolation {
	void m() {
		final Map<Object, String> mixed = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(mixed);
	}
}
// === end ===

// === case: declared_object_type_args_with_constructor_argument ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDeclaredObjectTypeArgsWithConstructorArgumentSliceViolation {
	void m(List<String> names) {
		final List<Object> values = new ArrayList<>(names); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_annotation_ending_in_var ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEndingInVarSliceViolation {
	@interface Autovar {}

	void m() {
		@Autovar var x = new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_annotation_equal_sign ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "unchecked") var x = new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_annotation_on_previous_line ===
// imports: java.util.HashMap
// imports: javax.annotation.Nonnull
class InputPreferVarDiamondAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final var map = new HashMap<Object, Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_carries_declared_type_arguments ===
// multi-fix-expected
// imports: java.util.ArrayList
// imports: java.util.HashMap
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarDiamondCarriesDeclaredTypeArgumentsSliceViolation {
	void m() {
		final Map<String, List<Integer>> nested = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final ArrayList<String> concrete = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final List<String> iface = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: diamond_in_nested_call_only ===
// skip-reason: declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondInNestedCallOnlySliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final List<String> wrapped = wrap(new ArrayList<>()); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(wrapped);
	}
}
// === end ===

// === case: diamond_new_in_comment ===
// imports: java.util.ArrayList
class InputPreferVarDiamondNewInCommentSliceViolation {
	void m() {
		final var values = /* new ArrayList<Object>() */ new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> values = new Holder<>().names(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_on_continuation_line ===
// skip-reason: declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineSliceViolation {
	void m() {
		final Map<String, String> lookup = // violation: Local variable must use 'var' instead of an explicit type.
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_on_continuation_line_type_wrapped ===
// skip-reason: declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineTypeWrappedSliceViolation {
	void m() {
		final Map<String, String> // violation: Local variable must use 'var' instead of an explicit type.
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
		var a = new ArrayList< Object >(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_supplementary_type_name ===
class InputPreferVarDiamondSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o<T> {}

	void m() {
		final var x = new Fo𝛼o<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> values = switch (key) { case 1 -> new ArrayList<>(); default -> new LinkedList<>(); }; // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = flag ? switch (key) { case 1 -> new ArrayList<>(); default -> new LinkedList<>(); } : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? new ArrayList<>() : new LinkedList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? new ArrayList<>() : useLinked ? new LinkedList<>() : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? useLinked ? new LinkedList<>() : new ArrayList<>() : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_second_arm_below ===
// skip-reason: declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernarySecondArmBelowSliceViolation {
	void m(boolean useArray) {
		final List<String> values = useArray ? new ArrayList<>() // violation: Local variable must use 'var' instead of an explicit type.
				: new LinkedList<>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_three_object_args ===
class InputPreferVarDiamondThreeObjectArgsSliceViolation {
	static class Triple<A, B, C> {}

	void m() {
		var t = new Triple<Object, Object, Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> merged = new ArrayList<>(wrap(new ArrayList<>())); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(merged);
	}
}
// === end ===

// === case: diamond_var_in_variable_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondVarInVariableNameSliceViolation {
	void m() {
		var myvar = new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_with_final ===
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondWithFinalSliceViolation {
	void m() {
		final var s = new LinkedHashSet<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: double_escaped_backslash_not_multi_var ===
// skip-reason: multi-variable declaration
class InputPreferVarDoubleEscapedBackslashSliceViolation {
	void m() {
		final String x = "\\", y = "z"; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: equals_comparison_not_assignment ===
class InputPreferVarEqualsComparisonNotAssignmentSliceViolation {
	void m(int x, int y) {
		final boolean b = x == y; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") String[] arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign_var ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignVarSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") var arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
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
		@Anno(a = "x", b = @Inner(c = "y")) String[] arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
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
		@Anno(a = "x", b = @Inner(c = "y")) var arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_on_previous_line ===
// imports: javax.annotation.Nonnull
class InputPreferVarExplicitArrayAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final String[] names = new String[]{"a", "b"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_paren_in_string ===
class InputPreferVarExplicitArrayInitAnnotationParenInStringSliceViolation {
	void m() {
		@SuppressWarnings("(") String[] arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_brace_in_comment ===
// skip-reason: explicit array initializer could not be resolved on the reported line
class InputPreferVarExplicitArrayInitBraceInCommentSliceViolation {
	void m() {
		final var values = new String[] /* {"z"} */ {"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_c_style_name_spelled_var ===
class InputPreferVarExplicitArrayInitCStyleNameSpelledVarSliceViolation {
	void m() {
		String var[] = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(var.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_explicit_type ===
class InputPreferVarExplicitArrayInitCommentBeforeNameExplicitTypeSliceViolation {
	void m() {
		final String[] /* keep */ arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_var ===
class InputPreferVarExplicitArrayInitCommentBeforeNameVarSliceViolation {
	void m() {
		final var /* keep */ arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_equals_before_assignment ===
class InputPreferVarExplicitArrayInitCommentEqualsBeforeAssignmentSliceViolation {
	void m() {
		String[] /* = */ arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_empty_typed ===
class InputPreferVarExplicitArrayInitEmptyTypedSliceViolation {
	void m() {
		final String[] a = new String[]{}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_empty_var ===
class InputPreferVarExplicitArrayInitEmptyVarSliceViolation {
	void m() {
		final var a = new String[]{}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_generic_declared_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericDeclaredTypeSliceViolation {
	void m(List<String> list) {
		final List<String>[] arr = new List<String>[]{list}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_generic_declared_type_nested ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarExplicitArrayInitGenericDeclaredTypeNestedSliceViolation {
	void m(Map<String, List<Integer>> map) {
		final Map<String, List<Integer>>[] arr = new Map<String, List<Integer>>[]{map}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_generic_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericTypeSliceViolation {
	void m(List<String> list) {
		final var a = new List<String>[]{list}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_list_of ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitListOfSliceViolation {
	void m() {
		final Object[] a = List.of(new Object[]{"a"}); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_method_call_arg ===
class InputPreferVarExplicitArrayInitMethodCallArgSliceViolation {
	void m() {
		final String result = String.join(",", new String[]{"a", "b"}); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_multi_arg_method_call ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitMultiArgMethodCallSliceViolation {
	void m() {
		final Object[] a = List.of(new int[]{1}, new int[]{2}).toArray(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_multi_dim ===
class InputPreferVarExplicitArrayInitMultiDimSliceViolation {
	void m() {
		final int[][] m = new int[][]{{1}}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_multi_space ===
class InputPreferVarExplicitArrayInitMultiSpaceSliceViolation {
	void m() {
		final String[] a =   new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_qualified_new_type ===
class InputPreferVarExplicitArrayInitQualifiedNewTypeSliceViolation {
	void m() {
		final var a = new java.lang.String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_space_before_brace ===
class InputPreferVarExplicitArrayInitSpaceBeforeBraceSliceViolation {
	void m() {
		final String[] values = new String[] {"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_split_across_lines ===
// skip-reason: explicit array initializer could not be resolved on the reported line
class InputPreferVarExplicitArrayInitSplitAcrossLinesSliceViolation {
	void m() {
		final int[] values = new int[] // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				{1, 2, 3};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_type_name ===
class InputPreferVarExplicitArrayInitSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o {}

	void m() {
		final var a = new Fo𝛼o[]{new Fo𝛼o()}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(a.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_var_name ===
class InputPreferVarExplicitArrayInitSupplementaryVarNameSliceViolation {
	void m() {
		final var 𝛼a = new String[]{"x"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(𝛼a.length);
	}
}
// === end ===

// === case: explicit_array_init_ternary ===
class InputPreferVarExplicitArrayInitTernarySliceViolation {
	void m(boolean cond) {
		final int[] a = cond ? new int[]{1} : new int[]{2}; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_typed_matching ===
class InputPreferVarExplicitArrayInitTypedMatchingSliceViolation {
	void m() {
		final String[] a = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_var_separated_by_a_comment ===
class InputPreferVarExplicitArrayInitVarSeparatedByACommentSliceViolation {
	void m() {
		final var/* keep */arr = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_var_to_primitive ===
class InputPreferVarExplicitArrayInitVarToPrimitiveSliceViolation {
	void m() {
		final var a = new int[]{1, 2}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_var_to_string ===
class InputPreferVarExplicitArrayInitVarToStringSliceViolation {
	void m() {
		final var a = new String[]{"a"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_type_arguments_in_initializer ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarExplicitTypeArgumentsInInitializerSliceViolation {
	void m() {
		final Map<String, Integer> counts = new HashMap<String, Integer>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(counts);
	}
}
// === end ===

// === case: factory_initializer_reassigned_beyond_the_returned_type ===
class InputPreferVarFactoryInitializerReassignedBeyondTheReturnedTypeSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m(boolean flag) {
		Base item = makeDerived(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		Base item = makeDerived(); // violation: Local variable must use 'var' instead of an explicit type.
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
		Base item = makeBase(); // violation: Local variable must use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: final_column_at_final ===
class InputPreferVarFinalColumnAtFinalSliceViolation {
	void m() {
		final int x = 5; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: for_each ===
// imports: java.util.List
class InputPreferVarForEachSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (@Nonnull String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (
				@Nonnull final
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (
				@Nonnull
				final String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (
				@Nonnull
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_blank_between_paren_and_decl ===
// imports: java.util.List
class InputPreferVarForEachBlankBetweenParenAndDeclSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (

				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (String s : list) { consume(a = new String[]{"b"}); } // violation: For-each loop must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: for_each_boxed_element_type_over_a_matching_array ===
class InputPreferVarForEachBoxedElementTypeOverAMatchingArraySliceViolation {
	void m(Integer[] counts) {
		for (Integer count : counts) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(count);
	}
}
// === end ===

// === case: for_each_final ===
// imports: java.util.List
class InputPreferVarForEachFinalSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (final String i : l) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(i);
	}
}
// === end ===

// === case: for_each_final_only_prev_line ===
// imports: java.util.List
class InputPreferVarForEachFinalOnlyPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (
				final
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (Entry<String, Integer> entry : map.entrySet()) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(entry);
	}
}
// === end ===

// === case: for_each_open_paren_prev_line ===
// imports: java.util.List
class InputPreferVarForEachOpenParenPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
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
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_primitive_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachPrimitiveArrayElementTypeSliceViolation {
	void m(List<int[]> intRows) {
		for (int[] row : intRows) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_primitive_element_type_over_an_array ===
class InputPreferVarForEachPrimitiveElementTypeOverAnArraySliceViolation {
	void m(int[] sizes) {
		for (int size : sizes) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(size);
	}
}
// === end ===

// === case: for_each_reference_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachReferenceArrayElementTypeSliceViolation {
	void m(List<String[]> rows) {
		for (String[] row : rows) // violation: For-each loop must use 'var' instead of an explicit type.
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
		for (
				@Nonnull
				@SuppressWarnings("unused")
				String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_init_reassigned_beyond_the_constructed_type ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarForInitReassignedBeyondTheConstructedTypeSliceViolation {
	void m(boolean flag) {
		for (List<String> items = new ArrayList<>(); flag; flag = false) { // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		for (List<String> items = new ArrayList<>(); flag; flag = false) { // violation: Local variable must use 'var' instead of an explicit type.
			items = new ArrayList<>();
			System.out.println(items);
		}
	}
}
// === end ===

// === case: for_loop_init ===
class InputPreferVarForLoopInitSliceViolation {
	void m() {
		for (int i = 0; i < 10; ++i) // violation: Local variable must use 'var' instead of an explicit type.
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
		for (Iterator<String> it = list.iterator(); it.hasNext(); ) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(it.next());
	}
}
// === end ===

// === case: generic_factory_with_lambda_arguments ===
// imports: java.util.List
class InputPreferVarGenericFactoryWithLambdaArgumentsSliceViolation {
	void m() {
		final List<Runnable> tasks = List.of(() -> System.out.println("x")); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: generic_return_declared_on_an_enclosing_class ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnDeclaredOnAnEnclosingClassSliceViolation {
	static <T> T make() {
		return null;
	}

	void m() {
		final Runnable task = new Runnable() {
			@Override
			public void run() {
				final var value = make(); // violation (warning): Using 'var' with 'make' loses generic type information, consider using an explicit type.
				System.out.println(value);
			}
		};
		System.out.println(task);
	}
}
// === end ===

// === case: generic_return_inherited_from_a_same_file_interface ===
// skip-reason: declaration already uses 'var'
interface InputPreferVarGenericReturnInheritedSliceSource {
	default <T> T make() {
		return null;
	}
}

class InputPreferVarGenericReturnInheritedFromASameFileInterfaceSliceViolation implements InputPreferVarGenericReturnInheritedSliceSource {
	void m() {
		final var value = make(); // violation (warning): Using 'var' with 'make' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_inherited_from_a_same_file_superclass ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnInheritedSliceBase {
	static <T> T pick() {
		return null;
	}
}

class InputPreferVarGenericReturnInheritedFromASameFileSuperclassSliceViolation extends InputPreferVarGenericReturnInheritedSliceBase {
	void m() {
		final var value = pick(); // violation (warning): Using 'var' with 'pick' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_no_parameters ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnNoParametersSliceViolation {
	static <T> T make() {
		return null;
	}

	void m() {
		final var value = make(); // violation (warning): Using 'var' with 'make' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_second_type_parameter ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnSecondTypeParameterSliceViolation {
	static <K, V> V get(K key) {
		return null;
	}

	void m() {
		final var value = get("a"); // violation (warning): Using 'var' with 'get' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: generic_var_with_diamond_new ===
// skip-reason: declaration already uses 'var'
// imports: java.util.ArrayList
class InputPreferVarGenericVarWithDiamondNewSliceViolation {
	static <T> T cast(Object obj) {
		return null;
	}

	void m() {
		final var value = cast(new ArrayList<>()); // violation (warning): Using 'var' with 'cast' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: generic_var_with_non_object_new ===
// skip-reason: declaration already uses 'var'
// imports: java.util.ArrayList
class InputPreferVarGenericVarWithNonObjectNewSliceViolation {
	static <T> T cast(Object obj) {
		return null;
	}

	void m() {
		final var value = cast(new ArrayList<String>()); // violation (warning): Using 'var' with 'cast' loses generic type information, consider using an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: inheritance_cycle_terminates ===
class InputPreferVarInheritanceCycleSliceFirst extends InputPreferVarInheritanceCycleTerminatesSliceViolation {
}

class InputPreferVarInheritanceCycleTerminatesSliceViolation extends InputPreferVarInheritanceCycleSliceFirst {
	void m() {
		final String text = "  x  ".trim(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final String value = base(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(value);
	}
}
// === end ===

// === case: local_generic_class_diamond ===
class InputPreferVarLocalGenericClassDiamondSliceViolation {
	static class Box<T> {
	}

	void m() {
		final Box<String> boxed = new Box<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(boxed);
	}
}
// === end ===

// === case: local_variables ===
// imports: java.util.List
class InputPreferVarLocalVariablesSliceViolation {
	void m() {
		final int x = 42; // violation: Local variable must use 'var' instead of an explicit type.
		final String s = "hello"; // violation: Local variable must use 'var' instead of an explicit type.
		final List<Integer> list = List.of(1, 2, 3); // violation: Local variable must use 'var' instead of an explicit type.
		final var names = new String[]{"a", "b"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final String[] numbers = new String[]{"1"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[][] matrix = new int[][]{{1}, {2}}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final var parenArr = (new String[]{"x"}); // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: method_call_and_chain ===
class InputPreferVarMethodCallAndChainSliceViolation {
	void m() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
		final String trimmed = "  hello  ".trim().toLowerCase(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: mixed_qualified_and_bare_object_type_args ===
// imports: java.util.HashMap
class InputPreferVarDiamondMixedQualifiedAndBareObjectTypeArgsSliceViolation {
	void m() {
		final var map = new HashMap<Object, java.lang.Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: multi_dim_array ===
class InputPreferVarMultiDimArraySliceViolation {
	void m() {
		int[][] m = new int[3][3]; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multi_var ===
// skip-reason: multi-variable declaration
class InputPreferVarMultiVarSliceViolation {
	void m() {
		final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multi_var_supplementary_before_type ===
// skip-reason: multi-variable declaration
class InputPreferVarMultiVarSupplementaryBeforeTypeSliceViolation {
	void m() {
		/* 𝛼 */ final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(x + y);
	}
}
// === end ===

// === case: multi_var_ternary_initializer ===
// skip-reason: multi-variable declaration
class InputPreferVarMultiVarTernaryInitializerSliceViolation {
	void m(boolean cond, int a, int b, int d) {
		final int x = cond ? a : b, y = d; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multi_variable_declarations ===
// imports: javax.annotation.Nonnull
class InputPreferVarMultiVarViolation {
	void multiVarAnnotated() {
		@Nonnull
		final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarForInit() {
		for (int i = 0, j = 10; i < j; ++i) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarForInitAnnotated() {
		for (@Nonnull int i = 0, j = 10; i < j; ++i) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarFourVariables() {
		final int w = 1, x = 2, y = 3, z = 4; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarLocal() {
		final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final String a = "a", b = "b"; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarMixedInit() {
		final int x = Integer.parseInt("5"), y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarPartialInit() {
		final int x = 1, y = x; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarThreeVariables() {
		final int x = 1, y = 2, z = 3; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multiple_object_type_args ===
// imports: java.util.HashMap
class InputPreferVarDiamondMultipleObjectTypeArgsSliceViolation {
	void m() {
		final var map = new HashMap<Object, Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> values // violation: Local variable must use 'var' instead of an explicit type.
				= build();
		System.out.println(values);
	}
}
// === end ===

// === case: nested_ternary_initializer ===
class InputPreferVarNestedTernaryInitializerSliceViolation {
	void m(boolean cond, boolean flag, String a, String b, String c) {
		final String x = cond ? a : flag ? b : c; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(x);
	}
}
// === end ===

// === case: nested_ternary_multi_var ===
// skip-reason: multi-variable declaration
class InputPreferVarNestedTernaryMultiVarSliceViolation {
	void m(boolean cond, boolean flag, int a, int b, int c, int d) {
		final int x = cond ? a : flag ? b : c, y = d; // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final String s = nonGeneric(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: overload_selection_changes_from_an_enclosing_class ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesFromAnEnclosingClassSliceViolation {
	static class Inner {
		void m() {
			final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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

// === case: overload_selection_changes_on_a_constructor_argument ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_super_qualified_call ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		super.take(items);
	}
}
// === end ===

// === case: overload_selection_changes_under_var ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_changes_via_an_inherited_overload ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_changes_with_qualified_types ===
class InputPreferVarOverloadSelectionChangesWithQualifiedTypesSliceViolation {
	static void take(java.util.ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(java.util.List<String> values) {
		System.out.println(values);
	}

	void m() {
		final java.util.List<String> items = new java.util.ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_differs_at_a_later_argument ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
			final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			take(items);
		}
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_sees_a_captured_use ===
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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

// === case: overload_selection_skips_an_unresolvable_parameter_type ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionSkipsAnUnresolvableParameterTypeSliceViolation {
	static class Bag {
	}

	static void take(Bag values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_survives_cyclic_inheritance ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarCyclicInheritanceSliceViolation extends InputPreferVarCyclicInheritancePartner {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: paren_wrapped_generic_method ===
// skip-reason: declaration already uses 'var'
class InputPreferVarGenericReturnParenWrappedGenericMethodSliceViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	void m() {
		final var s = (cast("hello")); // violation (warning): Using 'var' with 'cast' loses generic type information, consider using an explicit type.
	}
}
// === end ===

// === case: parse_boolean ===
class InputPreferVarLiteralMismatchParseBooleanSliceViolation {
	void m() {
		final boolean b = Boolean.parseBoolean("true"); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_byte ===
class InputPreferVarLiteralMismatchParseByteSliceViolation {
	void m() {
		final byte b = Byte.parseByte("5"); // violation: Local variable must use 'var' instead of an explicit type.
		final double bd = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float bf = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int bi = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long bl = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final short bs = Byte.parseByte("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_double ===
class InputPreferVarLiteralMismatchParseDoubleSliceViolation {
	void m() {
		final double d = Double.parseDouble("5.0"); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_float ===
class InputPreferVarLiteralMismatchParseFloatSliceViolation {
	void m() {
		final float f = Float.parseFloat("5.0"); // violation: Local variable must use 'var' instead of an explicit type.
		final double fd = Float.parseFloat("5.0"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_int ===
class InputPreferVarLiteralMismatchParseIntSliceViolation {
	void m() {
		final int i = Integer.parseInt("5"); // violation: Local variable must use 'var' instead of an explicit type.
		final double id = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float ifl = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long il = Integer.parseInt("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_long ===
class InputPreferVarLiteralMismatchParseLongSliceViolation {
	void m() {
		final long l = Long.parseLong("5"); // violation: Local variable must use 'var' instead of an explicit type.
		final double ld = Long.parseLong("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float lf = Long.parseLong("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: parse_short ===
class InputPreferVarLiteralMismatchParseShortSliceViolation {
	void m() {
		final short s = Short.parseShort("5"); // violation: Local variable must use 'var' instead of an explicit type.
		final double sd = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final float sf = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int si = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long sl = Short.parseShort("5"); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: primitive_with_non_literal_expression ===
class InputPreferVarLiteralMismatchPrimitiveWithNonLiteralExpressionSliceViolation {
	void m(int a, byte b, boolean flag) {
		final float fAdd = a + b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int fCast = (byte) a; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long lMul = a * b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long lShift = a << b; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final long tern = flag ? 1L : 2L; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int piParen = (5); // violation: Local variable must use 'var' instead of an explicit type.
		final float pfCast = ((float) a); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: qualified_constructor_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedConstructorNameSliceViolation {
	void m() {
		final var list = new java.util.ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final Outer.Box<String> boxed = new Outer.Box<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(boxed);
	}
}
// === end ===

// === case: qualified_new_matching_simple_name ===
// multi-fix-expected
class InputPreferVarQualifiedNewMatchingSimpleNameSliceViolation {
	void m() {
		final Object o = new java.lang.Object(); // violation: Local variable must use 'var' instead of an explicit type.
		final StringBuilder sb = new java.lang.StringBuilder(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(o);
		System.out.println(sb);
	}
}
// === end ===

// === case: qualified_new_resolvable_diamond ===
// imports: java.util.List
class InputPreferVarQualifiedNewResolvableDiamondSliceViolation {
	void m() {
		final List<String> names = new java.util.ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: qualified_object_type_arg ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<java.lang.Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: qualified_static_call_generic_var ===
// skip-reason: declaration already uses 'var'
class InputPreferVarQualifiedStaticCallGenericVarSliceViolation {
	void m() {
		final var values = java.util.Collections.emptyList(); // violation (warning): Using 'var' with 'emptyList' loses generic type information, consider using an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: qualified_type ===
// imports: java.util.List
class InputPreferVarQualifiedTypeSliceViolation {
	void m(List<String> x) {
		java.util.List<String> l = x; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: qualified_type_annotation_on_previous_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarQualifiedTypeAnnotationOnPreviousLineSliceViolation {
	void m(List<String> x) {
		@Nonnull
		final java.util.List<String> l = x; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: qualified_type_argument_nested_not_widening ===
// imports: java.util.Map
class InputPreferVarQualifiedTypeArgumentNestedNotWideningSliceViolation {
	void m(Map<String, java.util.List<String>> src) {
		final Map<String, java.util.List<String>> index = src; // violation: Local variable must use 'var' instead of an explicit type.
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
		final Map<String, Types.Number> index = src; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(index);
	}
}
// === end ===

// === case: qualified_type_final ===
// imports: java.util.List
class InputPreferVarQualifiedTypeFinalSliceViolation {
	void m(List<String> x) {
		final java.util.List<String> l = x; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: reassigned_and_overload_selection_changes ===
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		if (flag)
			items = new LinkedList<>();
		take(items);
	}
}
// === end ===

// === case: reassigned_between_two_unresolvable_names ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedBetweenTwoUnresolvableNamesSliceViolation {
	void m(boolean flag) {
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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

// === case: reassigned_to_a_different_type ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarReassignedToADifferentTypeSliceViolation {
	void m(boolean flag) {
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		if (flag)
			items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_a_non_constructed_value ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToANonConstructedValueSliceViolation {
	void m(boolean flag, List<String> other) {
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		if (flag)
			items = other;
		System.out.println(items);
	}
}
// === end ===

// === case: reassigned_to_a_same_simple_name_different_package ===
// imports: java.util.List
class InputPreferVarReassignedToASameSimpleNameDifferentPackageSliceViolation {
	void m(boolean flag) {
		List<String> items = new java.util.ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		if (flag)
			items = new ArrayList<>();
		System.out.println(items);
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
			List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			items = new LinkedList<>();
			System.out.println(items);
		}
		List<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: simple_declared_type_qualified_new ===
class InputPreferVarSimpleDeclaredTypeQualifiedNewSliceViolation {
	static class Box<T> {
	}

	void m() {
		final Box<String> boxed = new InputPreferVarSimpleDeclaredTypeQualifiedNewSliceViolation.Box<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(boxed);
	}
}
// === end ===

// === case: simple_int ===
class InputPreferVarSimpleIntSliceViolation {
	void m() {
		int x = 5; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: simple_string ===
class InputPreferVarSimpleStringSliceViolation {
	void m() {
		String s = "hi"; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: single_object_type_arg ===
// imports: java.util.ArrayList
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondSingleObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var set = new LinkedHashSet<Object>(); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var parenWrapped = (new ArrayList<Object>()); // violation: Use diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: static_call_generic_var ===
// imports: java.util.Collections
// imports: java.util.Optional
class InputPreferVarReflectionStaticCallGenericVarSliceViolation {
	void m() {
		final var list = Collections.emptyList(); // violation (warning): Using 'var' with 'emptyList' loses generic type information, consider using an explicit type.
		final var opt = Optional.empty(); // violation (warning): Using 'var' with 'empty' loses generic type information, consider using an explicit type.
	}
}
// === end ===

// === case: static_call_non_generic ===
class InputPreferVarReflectionStaticCallNonGenericSliceViolation {
	void m() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: static_import_generic_return_var ===
// skip-reason: declaration already uses 'var'
// imports: static java.util.Collections.emptyList
class InputPreferVarStaticImportGenericReturnVarSliceViolation {
	void m() {
		final var values = emptyList(); // violation (warning): Using 'var' with 'emptyList' loses generic type information, consider using an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: static_import_non_generic_call ===
// imports: static java.util.Arrays.asList
// imports: java.util.List
class InputPreferVarStaticImportNonGenericCallSliceViolation {
	void m() {
		final List<String> names = asList("a", "b"); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: supplementary_char_before_type ===
// imports: java.util.Map
class InputPreferVarSupplementaryCharBeforeTypeSliceViolation {
	void m(Map<String, String> map) {
		/* 𝛼 */ for (Map.Entry<String, String> entry : map.entrySet()) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(entry);
	}
}
// === end ===

// === case: supplementary_char_in_type_name ===
// multi-fix-expected
class InputPreferVarSupplementaryCharInTypeNameSliceViolation {
	static class Fo𝛼o {
	}

	private Fo𝛼o build() {
		return new Fo𝛼o();
	}

	void m() {
		final Fo𝛼o value = build(); // violation: Local variable must use 'var' instead of an explicit type.
		final Fo𝛼o 𝛼value = build(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: try_with_resources ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryWithResourcesSliceViolation {
	void m() throws Exception {
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
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
		try (@Nonnull ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
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
		try (
				@Nonnull
				ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_body_assigns_an_array_on_the_same_line ===
// imports: java.util.Scanner
class InputPreferVarTryWithResourcesBodyAssignsAnArrayOnTheSameLineSliceViolation {
	void m(Scanner in, String[] a) throws Exception {
		try (Scanner sc = in) { a = new String[]{"b"}; } // violation: Try-with-resources must use 'var' instead of an explicit type.
		System.out.println(a.length);
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
		try (Pair<String, Integer> p = new Pair<>()) { // violation: Try-with-resources must use 'var' instead of an explicit type.
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
		try (ByteArrayInputStream in = open()) { // violation: Try-with-resources must use 'var' instead of an explicit type.
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
		try (Pair<String, Integer> p = new Pair<String, Integer>()) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(p);
		}
	}
}
// === end ===

// === case: try_with_resources_mixed_ref_and_decl ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryMixedRefAndDeclSliceViolation {
	void m(ByteArrayInputStream existing) throws Exception {
		try (existing; ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(existing.read() + in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_qualified_type ===
class InputPreferVarTryWithResourcesQualifiedTypeSliceViolation {
	void m() throws Exception {
		try (java.io.ByteArrayInputStream s = new java.io.ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			s.read();
		}
	}
}
// === end ===

// === case: try_with_resources_two_declarations ===
// multi-fix-expected
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryTwoDeclarationsSliceViolation {
	void m() throws Exception {
		try (
				ByteArrayInputStream a = new ByteArrayInputStream(new byte[0]); // violation: Try-with-resources must use 'var' instead of an explicit type.
				ByteArrayInputStream b = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(a.read() + b.read());
		}
	}
}
// === end ===

// === case: two_tab_indent ===
class InputPreferVarTwoTabIndentSliceViolation {
	static class Inner {
		void m() {
			int x = 5; // violation: Local variable must use 'var' instead of an explicit type.
		}
	}
}
// === end ===

// === case: type_args_on_reflection_generic ===
// skip-reason: declaration already uses 'var'
// imports: java.util.Collections
class InputPreferVarReflectionTypeArgsOnReflectionGenericSliceViolation {
	void m() {
		final var list = Collections.<String>emptyList(); // violation (warning): Prefer explicit type over type arguments on 'emptyList'.
	}
}
// === end ===

// === case: type_on_own_line ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineSliceViolation {
	void m(List<String> x) {
		List<String> // violation: Local variable must use 'var' instead of an explicit type.
				l = x;
	}
}
// === end ===

// === case: type_on_own_line_with_trailing_comment ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineWithTrailingCommentSliceViolation {
	void m(List<String> x) {
		List<String> // note // violation: Local variable must use 'var' instead of an explicit type.
				l = x;
		System.out.println(l);
	}
}
// === end ===

// === case: wrapped_generic_type_does_not_close_on_line ===
// skip-reason: declared type arguments do not close on the reported line
// imports: java.util.Map
class InputPreferVarWrappedGenericTypeDoesNotCloseOnLineSliceViolation {
	void m(Map<String, Integer> src) {
		final Map<String, // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = // violation: Local variable must use 'var' instead of an explicit type.
				build();
		System.out.println(values);
	}
}
// === end ===