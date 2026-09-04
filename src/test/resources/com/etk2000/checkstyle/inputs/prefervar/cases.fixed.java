// === case: annotation_plus_final ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationPlusFinalSliceViolation {
	void m(List<String> l) {
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

// === case: cast_to_matching_type ===
class InputPreferVarLiteralMismatchCastToMatchingTypeSliceViolation {
	void m(Object obj, int x) {
		final float cf = x;
		final long cl = x;
		final var cs = (String) obj;
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

// === case: conditional_diamond_whose_class_cannot_take_the_declared_arguments_warns ===
// imports: java.util.function.Function
class InputPreferVarConditionalDiamondWhoseClassCannotTakeTheDeclaredArgumentsWarnsSliceViolation {
	static class MyFunc<T> implements Function<T, Integer> {
		@Override
		public Integer apply(T value) {
			return 0;
		}
	}

	static class MyPair<A> extends Src<A, Integer> {
	}

	static class Src<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m(boolean flag, int key) {
		final Function<String, Integer> everyArmTernary = flag ? new MyFunc<>() : new MyFunc<>();
		final Function<String, Integer> everyArmSwitch = switch (key) { default -> new MyFunc<>(); };
		final Src<String, Integer> firstArmCannotTake = flag ? new MyPair<>() : new Src<>();
		final Src<String, Integer> secondArmCannotTake = flag ? new Src<>() : new MyPair<>();
		final Src<String, Integer> switchArmCannotTake = switch (key) { case 1 -> new Src<>(); default -> new MyPair<>(); };
		final Src<String, Integer> nestedArmCannotTake = flag ? new Src<>() : key == 1 ? new Src<>() : new MyPair<>();
		final Function<String, Integer> qualifiedArmCannotTake = flag ? new MyFunc<>() : new MyFunc<>();
		System.out.println(everyArmTernary);
		System.out.println(everyArmSwitch);
		System.out.println(firstArmCannotTake);
		System.out.println(secondArmCannotTake);
		System.out.println(switchArmCannotTake);
		System.out.println(nestedArmCannotTake);
		System.out.println(qualifiedArmCannotTake);
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

// === case: explicit_array_init_annotated_declared_dimension_with_an_argument ===
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
class InputPreferVarExplicitArrayInitAnnotatedDeclaredDimensionWithAnArgumentSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {
		String value();
	}

	void m() {
		final String @Ann("x") [] declared = {"d"};
		System.out.println(declared.length);
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

// === case: explicit_array_init_c_style_typed_matching ===
class InputPreferVarExplicitArrayInitCStyleTypedMatchingSliceViolation {
	void m() {
		final String[] values = {"a"};
		final int[][] grid = {{1}};
		System.out.println(values.length + grid.length);
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

// === case: explicit_array_init_primitive_element_into_a_supertype_warns ===
class InputPreferVarExplicitArrayInitPrimitiveElementIntoASupertypeWarnsSliceViolation {
	void m() {
		final Object[] rows = new int[][]{{1}};
		final Object[] cStyleRows = new int[][]{{1}};
		final Cloneable cloneable = new int[]{1};
		System.out.println(rows.length + cStyleRows.length + cloneable.hashCode());
	}
}
// === end ===

// === case: explicit_array_init_qualified_declared_and_new_type ===
class InputPreferVarExplicitArrayInitQualifiedDeclaredAndNewTypeSliceViolation {
	void m() {
		final String[] both = {"b"};
		final String[] cStyle = {"c"};
		final java.util.Date[] dates = {new java.util.Date()};
		System.out.println(both.length + cStyle.length + dates.length);
	}
}
// === end ===

// === case: explicit_array_init_qualified_declared_type ===
class InputPreferVarExplicitArrayInitQualifiedDeclaredTypeSliceViolation {
	void m() {
		final String[] qualified = {"a"};
		final String[] simple = {"b"};
		System.out.println(qualified.length + simple.length);
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

// === case: explicit_array_init_supertype_element_with_more_dimensions_warns ===
class InputPreferVarExplicitArrayInitSupertypeElementWithMoreDimensionsWarnsSliceViolation {
	void m() {
		final Object[] rows = new Object[][]{{1}};
		final Object[] cStyleRows = new Object[][]{{1}};
		System.out.println(rows.length + cStyleRows.length);
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
		final List<String> items = new ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_prev_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnPrevLineSliceViolation {
	void m(List<String> list) {
		for (@Nonnull var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_var_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnVarLineSliceViolation {
	void m(List<String> list) {
		for (@Nonnull var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_final ===
// imports: java.util.List
class InputPreferVarForEachFinalSliceViolation {
	void m(List<String> l) {
		for (var i : l)
			System.out.println(i);
	}
}
// === end ===

// === case: for_each_final_only_prev_line ===
// imports: java.util.List
class InputPreferVarForEachFinalOnlyPrevLineSliceViolation {
	void m(List<String> list) {
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_final_qualified_generic_element_type_converts ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarForEachFinalQualifiedGenericElementTypeConvertsSliceViolation {
	void m(List<Map.Entry<String, Integer>> entries) {
		for (var entry : entries)
			System.out.println(entry);
	}
}
// === end ===

// === case: generic_return_on_an_enclosing_type_qualified_call_warns ===
class InputPreferVarGenericReturnOnAnEnclosingTypeQualifiedCallSliceViolation {
	static class Outer {
		static class Inner {
			void m() {
				final String name = Outer.make();
				final String qualified = InputPreferVarGenericReturnOnAnEnclosingTypeQualifiedCallSliceViolation.Outer.make();
				final var any = Outer.make();
				final String typed = Outer.<String>make();
				final var label = Outer.label();
				System.out.println(name + qualified + any + typed + label);
			}
		}

		static String label() {
			return "";
		}

		static <T> T make() {
			return null;
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
		final var m = new int[3][3];
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

// === case: overload_selection_ignores_a_this_constructor_of_a_different_arity ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAThisConstructorOfADifferentAritySliceViolation {
	void m() {
		final List<String> items = new ArrayList<>();
		class Sink {
			Sink() {
				this(items);
			}

			Sink(List<String> values) {
			}

			Sink(List<String> values, int limit) {
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_through_a_cast_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughACastWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>();
		take(items);
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
		final float pfCast = a;
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

// === case: qualified_types_are_reported_at_their_first_token_warns ===
class InputPreferVarQualifiedTypesAreReportedAtTheirFirstTokenWarnsSliceViolation {
	@interface Marker {
	}

	static class Outer {
		static final class Res<T> implements AutoCloseable {
			@Override
			public void close() {
			}
		}
	}

	<T> T acquire() {
		return null;
	}

	void m(Holder holder, String x, String y) throws Exception {
		try (@Marker AutoCloseable wide = new Outer.Res<String>()) {
			System.out.println(wide);
		}
		try (@Marker AutoCloseable typed = this.<AutoCloseable>acquire()) {
			System.out.println(typed);
		}
		for (String item : holder.items)
			System.out.println(item);
		final java.util.List<String> first = java.util.List.of(x), second = java.util.List.of(y);
		final java.util.List<String> empty = java.util.Collections.<String>emptyList();
		System.out.println(String.valueOf(first) + second + empty);
	}
}
// === end ===

// === case: reassigned_name_shadowed_by_a_nested_class ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarReassignedNameShadowedByANestedClassSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>();
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

// === case: reassigned_to_the_same_fqcn_different_spelling ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReassignedToTheSameFqcnDifferentSpellingSliceViolation {
	void m(boolean flag) {
		List<String> items = new ArrayList<>();
		if (flag)
			items = new ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: receiver_cast_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverCastWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>();
		System.out.println(ids.remove(1));
	}
}
// === end ===

// === case: receiver_method_reference_cast_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.function.IntFunction
class InputPreferVarReceiverMethodReferenceCastWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>();
		final IntFunction<Object> remover = ids::remove;
		System.out.println(remover.apply(1));
	}
}
// === end ===

// === case: receiver_same_file_override_spelled_qualified_warns ===
// imports: java.util.Date
class InputPreferVarReceiverSameFileOverrideSpelledQualifiedWarnsSliceViolation {
	static class Store {
		String put(Date value) {
			return "Store";
		}
	}

	static class TimedStore extends Store {
		@Override
		String put(Date value) {
			return "TimedStore";
		}
	}

	static TimedStore create() {
		return new TimedStore();
	}

	void m() {
		final Store store = create();
		System.out.println(store.put(new Date()));
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
		final List<String> items = new ArrayList<>();
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
		try (Closeable c = openReader()) {
			System.out.println(use(c));
		}
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