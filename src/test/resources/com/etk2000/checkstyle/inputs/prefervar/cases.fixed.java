// === case: annotation_plus_final ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationPlusFinalSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (@Nonnull var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: annotation_unbalanced_paren_string ===
class InputPreferVarAnnotationUnbalancedParenStringSliceViolation {
	void m() {
		@SuppressWarnings("(")
		final var s = "";
	}
}
// === end ===

// === case: annotation_with_args ===
class InputPreferVarAnnotationWithArgsSliceViolation {
	void m() {
		@SuppressWarnings("x")
		final var s = "";
	}
}
// === end ===

// === case: array_type ===
class InputPreferVarArrayTypeSliceViolation {
	void m() {
		final var a = new String[5];
	}
}
// === end ===

// === case: boxed_type_from_boxed_constructor ===
class InputPreferVarBoxedTypeFromBoxedConstructorSliceViolation {
	void m() {
		final var i = Integer.valueOf(5);
		System.out.println(i);
	}
}
// === end ===

// === case: boxed_type_from_qualified_boxed_initializer ===
class InputPreferVarBoxedTypeFromQualifiedBoxedInitializerSliceViolation {
	void m() {
		final var boxed = Integer.valueOf(5);
		System.out.println(boxed);
	}
}
// === end ===

// === case: comma_in_block_comment_not_multi_var ===
class InputPreferVarCommaInBlockCommentNotMultiVarSliceViolation {
	void m() {
		final var x = "ab" /* a, b */;
	}
}
// === end ===

// === case: comma_in_char_literal_not_multi_var ===
class InputPreferVarCommaInCharLiteralNotMultiVarSliceViolation {
	void m() {
		final var c = ',';
	}
}
// === end ===

// === case: comma_in_method_call_not_multi_var ===
class InputPreferVarCommaInMethodCallNotMultiVarSliceViolation {
	String m(String a, String b) {
		return a + b;
	}

	void n(String a, String b) {
		final var x = m(a, b);
	}
}
// === end ===

// === case: comma_in_string_not_multi_var ===
class InputPreferVarCommaInStringNotMultiVarSliceViolation {
	void m() {
		final var x = "a,b";
	}
}
// === end ===

// === case: comment_between_type_and_name ===
// imports: java.util.List
class InputPreferVarCommentBetweenTypeAndNameSliceViolation {
	void m(List<String> x) {
		final var /* c */ l = x;
	}
}
// === end ===

// === case: conditional_initializer_reassigned_beyond_the_arm_type ===
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

// === case: conditional_initializer_with_differing_arms_reassigned ===
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

// === case: diamond_annotation_ending_in_var ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEndingInVarSliceViolation {
	@interface Autovar {}

	void m() {
		@Autovar
		final var x = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_annotation_equal_sign ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings("unchecked")
		final var x = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_single_object_arg_with_whitespace ===
// imports: java.util.ArrayList
class InputPreferVarDiamondSingleObjectArgWithWhitespaceSliceViolation {
	void m() {
		final var a = new ArrayList<>();
	}
}
// === end ===

// === case: diamond_three_object_args ===
class InputPreferVarDiamondThreeObjectArgsSliceViolation {
	static class Triple<A, B, C> {}

	void m() {
		final var t = new Triple<>();
	}
}
// === end ===

// === case: diamond_var_in_variable_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondVarInVariableNameSliceViolation {
	void m() {
		final var myvar = new ArrayList<>();
	}
}
// === end ===

// === case: equals_comparison_not_assignment ===
class InputPreferVarEqualsComparisonNotAssignmentSliceViolation {
	void m(int x, int y) {
		final boolean b = x == y;
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings("x")
		final String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign_var ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignVarSliceViolation {
	void m() {
		@SuppressWarnings("x")
		final String[] arr = {"a"};
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
		@Anno(a = "x", b = @Inner(c = "y"))
		final String[] arr = {"a"};
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
		@Anno(a = "x", b = @Inner(c = "y"))
		final String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_annotation_paren_in_string ===
class InputPreferVarExplicitArrayInitAnnotationParenInStringSliceViolation {
	void m() {
		@SuppressWarnings("(")
		final String[] arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_c_style_name_spelled_var ===
class InputPreferVarExplicitArrayInitCStyleNameSpelledVarSliceViolation {
	void m() {
		final String[] var = {"a"};
		System.out.println(var.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_equals_before_assignment ===
class InputPreferVarExplicitArrayInitCommentEqualsBeforeAssignmentSliceViolation {
	void m() {
		final String[] /* = */ arr = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_method_call_arg ===
class InputPreferVarExplicitArrayInitMethodCallArgSliceViolation {
	void m() {
		final var result = String.join(",", "a", "b");
	}
}
// === end ===

// === case: explicit_array_init_qualified_new_type ===
class InputPreferVarExplicitArrayInitQualifiedNewTypeSliceViolation {
	void m() {
		final String[] a = {"a"};
	}
}
// === end ===

// === case: explicit_array_init_ternary ===
class InputPreferVarExplicitArrayInitTernarySliceViolation {
	void m(boolean cond) {
		final int[] a = cond ? new int[]{1} : new int[]{2};
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
		Base item = makeDerived();
		if (flag)
			item = new Base();
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
		final var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_prev_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (@Nonnull var item : list)
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
		for (@Nonnull var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_final ===
// imports: java.util.List
class InputPreferVarForEachFinalSliceViolation {
	void m() {
		final var l = List.of("a", "b");
		for (var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: for_each_final_only_prev_line ===
// imports: java.util.List
class InputPreferVarForEachFinalOnlyPrevLineSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (var item : list)
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
		for (List<String> items = new ArrayList<>(); flag; flag = false) {
			items = new LinkedList<>();
			System.out.println(items);
		}
	}
}
// === end ===

// === case: generic_type ===
// imports: java.util.List
class InputPreferVarGenericTypeSliceViolation {
	void m() {
		final var l = List.of();
	}
}
// === end ===

// === case: multi_dim_array ===
class InputPreferVarMultiDimArraySliceViolation {
	void m() {
		final int[][] m = new int[3][3];
	}
}
// === end ===

// === case: nested_generic ===
// imports: java.util.Map
class InputPreferVarNestedGenericSliceViolation {
	void m() {
		final var map = Map.of();
	}
}
// === end ===

// === case: overload_selection_changes_from_an_enclosing_class ===
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
		final List<String> items = new ArrayList<>();
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
		final List<String> items = new ArrayList<>();
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
		final List<String> items = new ArrayList<>();
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
		final List<String> items = new ArrayList<>();
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
		final java.util.List<String> items = new java.util.ArrayList<>();
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

	static void take(List<String> values, int count) {
		System.out.println(count + values.size());
	}

	void m() {
		final var items = new ArrayList<String>();
		take(items);
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

	static void take(List<String> values, Object... rest) {
		System.out.println(values.size() + rest.length);
	}

	void m() {
		final var items = new ArrayList<String>();
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

// === case: overload_selection_survives_cyclic_inheritance ===
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

// === case: qualified_constructor_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedConstructorNameSliceViolation {
	void m() {
		final var list = new ArrayList<>();
	}
}
// === end ===

// === case: qualified_new_matching_simple_name ===
class InputPreferVarQualifiedNewMatchingSimpleNameSliceViolation {
	void m() {
		final var o = new Object();
		final var sb = new StringBuilder();
		System.out.println(o);
		System.out.println(sb);
	}
}
// === end ===

// === case: qualified_type ===
// imports: java.util.List
class InputPreferVarQualifiedTypeSliceViolation {
	void m(List<String> x) {
		final var l = x;
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
		List<String> items = new ArrayList<>();
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
		final var items = new ArrayList<String>();
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
		List<String> items = new ArrayList<>();
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
		List<String> items = new ArrayList<>();
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
		List<String> items = new java.util.ArrayList<>();
		if (flag)
			items = new absent.ArrayList<>();
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
			List<String> items = new ArrayList<>();
			items = new LinkedList<>();
			System.out.println(items);
		}
		final var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: simple_int ===
class InputPreferVarSimpleIntSliceViolation {
	void m() {
		final var x = 5;
	}
}
// === end ===

// === case: simple_string ===
class InputPreferVarSimpleStringSliceViolation {
	void m() {
		final var s = "hi";
	}
}
// === end ===

// === case: two_tab_indent ===
class InputPreferVarTwoTabIndentSliceViolation {
	static class Inner {
		void m() {
			final var x = 5;
		}
	}
}
// === end ===

// === case: type_on_own_line ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineSliceViolation {
	void m(List<String> x) {
		final var
				l = x;
	}
}
// === end ===

// === case: type_on_own_line_with_trailing_comment ===
// imports: java.util.List
class InputPreferVarTypeOnOwnLineWithTrailingCommentSliceViolation {
	void m(List<String> x) {
		final var // note
				l = x;
		System.out.println(l);
	}
}
// === end ===

// === case: wildcard_generic ===
// imports: java.util.List
class InputPreferVarWildcardGenericSliceViolation {
	void m() {
		final var l = List.of();
	}
}
// === end ===