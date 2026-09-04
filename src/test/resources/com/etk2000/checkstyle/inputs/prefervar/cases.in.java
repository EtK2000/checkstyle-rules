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
// multi-fix-expected
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
	void m(List<String> l) {
		for (@Deprecated @Nonnull String i : l) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(i);
	}
}
// === end ===

// === case: annotation_plus_final ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarAnnotationPlusFinalSliceViolation {
	void m(List<String> l) {
		for (@Nonnull final String i : l) // violation: For-each variable must use 'var' instead of an explicit type.
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
		final var cmp = new Comparator<Object>() { // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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

// === case: auto_detected_generic_var_warns ===
class InputPreferVarGenericReturnAutoDetectedGenericVarSliceViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	void m() {
		final var s = cast("hello"); // violation (warning): Consider using an explicit type ('var' with 'cast' loses generic type information).
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

// === case: boolean_bitwise_operator_warns ===
class InputPreferVarBooleanBitwiseOperatorSliceViolation {
	void m(boolean flag, int x, int y) {
		final boolean b = flag & x < y; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: boolean_logical_operators ===
class InputPreferVarBooleanLogicalOperatorsSliceViolation {
	void m(boolean flag, Object o, int x, int y) {
		final boolean b = !flag && (o instanceof String || x < y); // violation: Local variable must use 'var' instead of an explicit type.
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

// === case: call_on_a_bounded_class_type_variable_receiver ===
class InputPreferVarCallOnABoundedClassTypeVariableReceiverSliceViolation<C extends CharSequence> {
	void m(C sequence) {
		final String text = sequence.toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
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

// === case: call_on_a_field_of_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnAFieldOfAnImportedUnloadableClassIsNotResolvableSliceViolation {
	Registry registry;

	void m() {
		final String name = registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
	}
}
// === end ===

// === case: call_on_a_for_each_variable_receiver_is_not_resolvable ===
// imports: java.util.List
class InputPreferVarCallOnAForEachVariableReceiverIsNotResolvableSliceViolation {
	void m(List<String> names) {
		for (var name : names) {
			final String trimmed = name.trim();
			System.out.println(trimmed);
		}
	}
}
// === end ===

// === case: call_on_a_lambda_parameter_receiver_is_not_resolvable ===
// imports: java.util.List
class InputPreferVarCallOnALambdaParameterReceiverIsNotResolvableSliceViolation {
	void m(List<String> names) {
		names.forEach(name -> {
			final String upper = name.toUpperCase();
			System.out.println(upper);
		});
	}
}
// === end ===

// === case: call_on_a_local_of_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
class InputPreferVarCallOnALocalOfAnImportedUnloadableClassIsNotResolvableSliceViolation {
	void m() {
		final Registry registry = new Registry(); // violation: Local variable must use 'var' instead of an explicit type.
		final String name = registry.lookup("k");
		System.out.println(name);
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

// === case: call_on_a_parameter_of_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnAParameterOfAnImportedUnloadableClassIsNotResolvableSliceViolation {
	void m(Registry registry) {
		final String name = registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
	}
}
// === end ===

// === case: call_on_a_primitive_array_receiver ===
class InputPreferVarCallOnAPrimitiveArrayReceiverSliceViolation {
	void m(int[] values) {
		final int[] copy = values.clone(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(copy.length);
	}
}
// === end ===

// === case: call_on_a_qualified_unloadable_class_parameter_is_not_resolvable ===
// imports: java.util.Objects
class InputPreferVarCallOnAQualifiedUnloadableClassParameterIsNotResolvableSliceViolation {
	void m(com.example.Registry registry) {
		final String name = registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
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

// === case: call_on_a_type_variable_bounded_by_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnATypeVariableBoundedByAnImportedUnloadableClassIsNotResolvableSliceViolation {
	<R extends Registry> void m(R registry) {
		final String name = registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
	}
}
// === end ===

// === case: call_on_a_type_variable_receiver ===
class InputPreferVarCallOnATypeVariableReceiverSliceViolation {
	<T> void m(T value) {
		final String text = value.toString(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: call_on_a_var_of_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnAVarOfAnImportedUnloadableClassIsNotResolvableSliceViolation {
	void m() {
		final var registry = new Registry();
		final String name = registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
	}
}
// === end ===

// === case: call_on_a_variable_named_like_an_enclosing_type_converts ===
class InputPreferVarCallOnAVariableNamedLikeAnEnclosingTypeConvertsSliceViolation {
	static class Box {
		static <T> T trim() {
			return null;
		}

		void m(String Box) {
			final String text = Box.trim(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(text);
		}
	}
}
// === end ===

// === case: call_on_an_element_of_an_imported_unloadable_class_array_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnAnElementOfAnImportedUnloadableClassArrayIsNotResolvableSliceViolation {
	void m(Registry[] registries) {
		final String name = registries[0].lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
	}
}
// === end ===

// === case: call_on_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.util.Objects
class InputPreferVarCallOnAnImportedUnloadableClassIsNotResolvableSliceViolation {
	void m() {
		final String name = Registry.lookup("k");
		final String text = Objects.toString(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(name + text);
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

// === case: call_on_an_unresolvable_receiver_ignores_a_same_file_homonym ===
class InputPreferVarCallOnAnUnresolvableReceiverIgnoresASameFileHomonymSliceViolation {
	String lookup(String key) {
		return key;
	}

	void m() {
		final String foreign = Registry.lookup("k");
		final String own = lookup("k"); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(foreign + own);
	}
}
// === end ===

// === case: call_on_an_unresolvable_receiver_ignores_a_static_import_homonym ===
// imports: static java.lang.String.valueOf
class InputPreferVarCallOnAnUnresolvableReceiverIgnoresAStaticImportHomonymSliceViolation {
	void m() {
		final String foreign = Registry.valueOf(1);
		final String imported = valueOf(2); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(foreign + imported);
	}
}
// === end ===

// === case: cast_intersection_through_a_var_warns ===
// imports: java.io.Serializable
class InputPreferVarCastIntersectionThroughAVarWarnsSliceViolation {
	void m(Runnable other) {
		final var task = (Runnable & Serializable) () -> System.out.println("run");
		Runnable current = task; // violation (warning): Local variable should use 'var' instead of an explicit type.
		current = other;
		System.out.println(current);
	}
}
// === end ===

// === case: cast_intersection_warns ===
// imports: java.io.Serializable
// imports: java.util.Comparator
class InputPreferVarCastIntersectionWarnsSliceViolation {
	void m(Comparator<String> other) {
		Comparator<String> byLength = (Comparator<String> & Serializable) (first, second) -> Integer.compare(first.length(), second.length()); // violation (warning): Local variable should use 'var' instead of an explicit type.
		byLength = other;
		System.out.println(byLength);
	}
}
// === end ===

// === case: cast_to_matching_type ===
// multi-fix-expected
class InputPreferVarLiteralMismatchCastToMatchingTypeSliceViolation {
	void m(Object obj, int x) {
		final float cf = (float) x; // violation: Local variable must use 'var' instead of an explicit type.
		final long cl = (long) x; // violation: Local variable must use 'var' instead of an explicit type.
		final String cs = (String) obj; // violation: Local variable must use 'var' instead of an explicit type.
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
		final var s = GenericReturnHelper.create().find(1); // violation (warning): Consider using an explicit type ('var' with 'find' loses generic type information).
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
		final List<String> values = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: code_line_with_comment_ending_in_open_paren_exact ===
// imports: java.util.ArrayList
class InputPreferVarCodeLineWithCommentEndingInOpenParenExactSliceViolation {
	void m() {
		System.out.println("x"); // note (
		final ArrayList<String> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
// skip-reason: cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes
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
// skip-reason: cannot rewrite the declaration: the reported position is not a declaration this fixer recognizes
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
		final List<String> values = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: comment_ending_in_open_paren_above_declaration_exact ===
// imports: java.util.ArrayList
class InputPreferVarCommentEndingInOpenParenAboveDeclarationExactSliceViolation {
	void m() {
		// keep this comment intact (
		final ArrayList<String> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
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
		final Function<String, Integer> everyArmTernary = flag ? new MyFunc<>() : new MyFunc<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Function<String, Integer> everyArmSwitch = switch (key) { default -> new MyFunc<>(); }; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Src<String, Integer> firstArmCannotTake = flag ? new MyPair<>() : new Src<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Src<String, Integer> secondArmCannotTake = flag ? new Src<>() : new MyPair<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Src<String, Integer> switchArmCannotTake = switch (key) { case 1 -> new Src<>(); default -> new MyPair<>(); }; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Src<String, Integer> nestedArmCannotTake = flag ? new Src<>() : key == 1 ? new Src<>() : new MyPair<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final java.util.function.Function<String, Integer> qualifiedArmCannotTake = flag ? new MyFunc<>() : new MyFunc<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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

// === case: conditional_initializer_reassigned_beyond_the_arm_type_warns ===
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
		final Base item = flag ? makeDerived() : new Derived(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item);
	}
}
// === end ===

// === case: conditional_initializer_with_a_non_new_arm_and_no_type_arguments_exact ===
class InputPreferVarConditionalInitializerWithANonNewArmAndNoTypeArgumentsExactSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m(boolean flag) {
		final Derived item = flag ? makeDerived() : new Derived(); // violation: Local variable must use 'var' instead of an explicit type.
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
		List<String> items = flag ? new ArrayList<>() : new LinkedList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		items = new LinkedList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: constructor_call ===
// multi-fix-expected
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
		final List<Object> values = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Map<Object, Object> pairs = new HashMap<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.size() + pairs.size());
	}
}
// === end ===

// === case: declared_object_type_args_exact ===
// multi-fix-expected
// imports: java.util.ArrayList
// imports: java.util.HashMap
class InputPreferVarDeclaredObjectTypeArgsExactSliceViolation {
	void m() {
		final ArrayList<Object> values = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final HashMap<Object, Object> pairs = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values.size() + pairs.size());
	}
}
// === end ===

// === case: declared_object_type_args_later_non_object ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDeclaredObjectTypeArgsLaterNonObjectSliceViolation {
	void m() {
		final Map<Object, String> mixed = new HashMap<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(mixed);
	}
}
// === end ===

// === case: declared_object_type_args_later_non_object_exact ===
// imports: java.util.HashMap
class InputPreferVarDeclaredObjectTypeArgsLaterNonObjectExactSliceViolation {
	void m() {
		final HashMap<Object, String> mixed = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(mixed);
	}
}
// === end ===

// === case: declared_object_type_args_with_constructor_argument ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDeclaredObjectTypeArgsWithConstructorArgumentSliceViolation {
	void m(List<String> names) {
		final List<Object> values = new ArrayList<>(names); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: declared_object_type_args_with_constructor_argument_exact ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDeclaredObjectTypeArgsWithConstructorArgumentExactSliceViolation {
	void m(List<String> names) {
		final ArrayList<Object> values = new ArrayList<>(names); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: declared_type_arguments_exceed_constructed_class_arity_warns ===
// imports: java.util.Map
// imports: java.util.Properties
class InputPreferVarDeclaredTypeArgumentsExceedConstructedClassArityWarnsSliceViolation {
	void m() {
		final Map<Object, Object> props = new Properties(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(props.size());
	}
}
// === end ===

// === case: diamond_annotation_ending_in_var ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEndingInVarSliceViolation {
	@interface Autovar {}

	void m() {
		@Autovar var x = new ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_annotation_equal_sign ===
// imports: java.util.ArrayList
class InputPreferVarDiamondAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "unchecked") var x = new ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_annotation_on_previous_line ===
// imports: java.util.HashMap
// imports: javax.annotation.Nonnull
class InputPreferVarDiamondAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final var map = new HashMap<Object, Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final Map<String, List<Integer>> nested = new HashMap<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final ArrayList<String> concrete = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final List<String> iface = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: diamond_in_nested_call_only ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondInNestedCallOnlySliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final List<String> wrapped = wrap(new ArrayList<>()); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(wrapped);
	}
}
// === end ===

// === case: diamond_in_nested_call_only_exact ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondInNestedCallOnlyExactSliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final ArrayList<String> wrapped = (ArrayList<String>) wrap(new ArrayList<>()); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(wrapped);
	}
}
// === end ===

// === case: diamond_new_in_comment ===
// imports: java.util.ArrayList
class InputPreferVarDiamondNewInCommentSliceViolation {
	void m() {
		final var values = /* new ArrayList<Object>() */ new ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_object_type_arguments_wrapped_onto_a_later_line ===
// imports: java.util.HashMap
// skip-reason: cannot use 'var': the declaration already uses it
class InputPreferVarDiamondObjectTypeArgumentsWrappedOntoALaterLineSliceViolation {
	void m() {
		final var map = new HashMap<Object, // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
				Object>();
		System.out.println(map);
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
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineSliceViolation {
	void m() {
		final Map<String, String> lookup = // violation (warning): Local variable should use 'var' instead of an explicit type.
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_on_continuation_line_exact ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
class InputPreferVarDiamondOnContinuationLineExactSliceViolation {
	void m() {
		final HashMap<String, String> lookup = // violation: Local variable must use 'var' instead of an explicit type.
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_on_continuation_line_type_wrapped ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarDiamondOnContinuationLineTypeWrappedSliceViolation {
	void m() {
		final Map<String, String> // violation (warning): Local variable should use 'var' instead of an explicit type.
				lookup =
				new HashMap<>();
		System.out.println(lookup);
	}
}
// === end ===

// === case: diamond_on_continuation_line_type_wrapped_exact ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.HashMap
class InputPreferVarDiamondOnContinuationLineTypeWrappedExactSliceViolation {
	void m() {
		final HashMap<String, String> // violation: Local variable must use 'var' instead of an explicit type.
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
		var a = new ArrayList< Object >(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_supplementary_type_name ===
class InputPreferVarDiamondSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o<T> {}

	void m() {
		final var x = new Fo𝛼o<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> values = switch (key) { case 1 -> new ArrayList<>(); default -> new LinkedList<>(); }; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_switch_expression_all_arms_exact ===
// imports: java.util.ArrayList
class InputPreferVarDiamondSwitchExpressionAllArmsExactSliceViolation {
	void m(int key) {
		final ArrayList<String> values = switch (key) { case 1 -> new ArrayList<>(); default -> new ArrayList<>(16); }; // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = flag ? switch (key) { case 1 -> new ArrayList<>(); default -> new LinkedList<>(); } : new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_arm_is_switch_exact ===
// imports: java.util.ArrayList
class InputPreferVarDiamondTernaryArmIsSwitchExactSliceViolation {
	void m(boolean flag, int key) {
		final ArrayList<String> values = flag ? switch (key) { case 1 -> new ArrayList<>(); default -> new ArrayList<>(16); } : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? new ArrayList<>() : new LinkedList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_both_arms_exact ===
// imports: java.util.ArrayList
class InputPreferVarDiamondTernaryBothArmsExactSliceViolation {
	void m(boolean useArray) {
		final ArrayList<String> values = useArray ? new ArrayList<>() : new ArrayList<>(16); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? new ArrayList<>() : useLinked ? new LinkedList<>() : new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_nested_all_arms_exact ===
// imports: java.util.ArrayList
class InputPreferVarDiamondTernaryNestedAllArmsExactSliceViolation {
	void m(boolean useArray, boolean useLinked) {
		final ArrayList<String> values = useArray ? new ArrayList<>() : useLinked ? new ArrayList<>(16) : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		final List<String> values = useArray ? useLinked ? new LinkedList<>() : new ArrayList<>() : new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_nested_true_arm_exact ===
// imports: java.util.ArrayList
class InputPreferVarDiamondTernaryNestedTrueArmExactSliceViolation {
	void m(boolean useArray, boolean useLinked) {
		final ArrayList<String> values = useArray ? useLinked ? new ArrayList<>(16) : new ArrayList<>() : new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_second_arm_below ===
// imports: java.util.ArrayList
// imports: java.util.LinkedList
// imports: java.util.List
class InputPreferVarDiamondTernarySecondArmBelowSliceViolation {
	void m(boolean useArray) {
		final List<String> values = useArray ? new ArrayList<>() // violation (warning): Local variable should use 'var' instead of an explicit type.
				: new LinkedList<>();
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_ternary_second_arm_below_exact ===
// skip-reason: cannot use a diamond: the declared type arguments belong to a diamond this fixer cannot reach
// imports: java.util.ArrayList
class InputPreferVarDiamondTernarySecondArmBelowExactSliceViolation {
	void m(boolean useArray) {
		final ArrayList<String> values = useArray ? new ArrayList<>() // violation: Local variable must use 'var' instead of an explicit type.
				: new ArrayList<>(16);
		System.out.println(values);
	}
}
// === end ===

// === case: diamond_three_object_args ===
class InputPreferVarDiamondThreeObjectArgsSliceViolation {
	static class Triple<A, B, C> {}

	void m() {
		var t = new Triple<Object, Object, Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> merged = new ArrayList<>(wrap(new ArrayList<>())); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(merged);
	}
}
// === end ===

// === case: diamond_top_level_and_nested_exact ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarDiamondTopLevelAndNestedExactSliceViolation {
	static List<String> wrap(List<String> source) {
		return source;
	}

	void m() {
		final ArrayList<String> merged = new ArrayList<>(wrap(new ArrayList<>())); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(merged);
	}
}
// === end ===

// === case: diamond_var_in_variable_name ===
// imports: java.util.ArrayList
class InputPreferVarDiamondVarInVariableNameSliceViolation {
	void m() {
		var myvar = new ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: diamond_whose_class_cannot_take_the_declared_arguments_warns ===
// imports: java.util.function.Function
class InputPreferVarDiamondWhoseClassCannotTakeTheDeclaredArgumentsWarnsSliceViolation {
	static class MyFunc<T> implements Function<T, Integer> {
		@Override
		public Integer apply(T value) {
			return 0;
		}
	}

	void m() {
		final Function<String, Integer> converter = new MyFunc<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(converter);
	}
}
// === end ===

// === case: diamond_with_final ===
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondWithFinalSliceViolation {
	void m() {
		final var s = new LinkedHashSet<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: double_escaped_backslash_not_multi_var_warns ===
class InputPreferVarDoubleEscapedBackslashSliceViolation {
	void m() {
		final String x = "\\", y = "z"; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: equals_comparison_not_assignment ===
class InputPreferVarEqualsComparisonNotAssignmentSliceViolation {
	void m(int x, int y) {
		final boolean b = x == y; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: exact_annotated_type_argument_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactAnnotatedTypeArgumentConvertsSliceViolation {
	@interface Ann {
	}

	void m() {
		final ArrayList<@Ann String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: exact_array_element_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactArrayElementConvertsSliceViolation {
	void m(ArrayList<String>[] lists) {
		final ArrayList<String> first = lists[0]; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(first);
	}
}
// === end ===

// === case: exact_call_on_a_cast_receiver_converts ===
// imports: java.util.List
class InputPreferVarExactCallOnACastReceiverConvertsSliceViolation {
	static class Holder {
		List<String> names() {
			return List.of();
		}
	}

	void m(Object source) {
		final List<String> names = ((Holder) source).names(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: exact_call_on_a_written_type_receiver_converts ===
// imports: java.util.List
class InputPreferVarExactCallOnAWrittenTypeReceiverConvertsSliceViolation {
	static class Holder {
		List<String> names() {
			return List.of();
		}
	}

	void m(Holder holder) {
		final List<String> names = holder.names(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: exact_cast_initializer_dropped_by_a_sibling_fix_stays_explicit ===
// imports: java.util.List
class InputPreferVarExactCastInitializerDroppedByASiblingFixStaysExplicitSliceViolation {
	void m(List<String> typed) {
		final List raw = (List) typed; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(raw);
	}
}
// === end ===

// === case: exact_classpath_call_converts ===
// imports: java.io.IOException
// imports: java.io.InputStream
// imports: java.nio.file.Files
// imports: java.nio.file.Path
class InputPreferVarExactClasspathCallConvertsSliceViolation {
	void m(Path path) throws IOException {
		final InputStream in = Files.newInputStream(path); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(in);
	}
}
// === end ===

// === case: exact_declaration_annotation_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactDeclarationAnnotationConvertsSliceViolation {
	void m() {
		@Deprecated
		final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: exact_field_access_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactFieldAccessConvertsSliceViolation {
	final ArrayList<String> items = new ArrayList<>();

	void m(String items) {
		final ArrayList<String> copy = this.items; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(copy + items);
	}
}
// === end ===

// === case: exact_for_init_diamond_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactForInitDiamondConvertsSliceViolation {
	void m(boolean flag) {
		for (ArrayList<String> items = new ArrayList<>(); flag; flag = false) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(items);
	}
}
// === end ===

// === case: exact_identifier_with_a_written_type_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactIdentifierWithAWrittenTypeConvertsSliceViolation {
	void m(ArrayList<String> source) {
		final ArrayList<String> copy = source; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(copy);
	}
}
// === end ===

// === case: exact_in_anonymous_body_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactInAnonymousBodyConvertsSliceViolation {
	void m() {
		new Thread() {
			@Override
			public void run() {
				final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(items);
			}
		}.start();
	}
}
// === end ===

// === case: exact_in_enum_constant_body_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactInEnumConstantBodyConvertsSliceViolation {
	enum Kind {
		ONE {
			@Override
			void run() {
				final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(items);
			}
		};

		abstract void run();
	}
}
// === end ===

// === case: exact_in_lambda_body_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactInLambdaBodyConvertsSliceViolation {
	void m() {
		final Runnable task = () -> {
			final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(items);
		};
		task.run();
	}
}
// === end ===

// === case: exact_in_record_compact_constructor_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactInRecordCompactConstructorConvertsSliceViolation {
	record Point(int x, int y) {
		Point {
			final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(items);
		}
	}
}
// === end ===

// === case: exact_in_static_initializer_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactInStaticInitializerConvertsSliceViolation {
	static {
		final ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: exact_qualified_declared_simple_new_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactQualifiedDeclaredSimpleNewConvertsSliceViolation {
	void m() {
		final java.util.ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: exact_reassigned_to_a_different_value_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactReassignedToADifferentValueConvertsSliceViolation {
	void m(boolean flag, ArrayList<String> other) {
		ArrayList<String> items = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		if (flag)
			items = other;
		System.out.println(items);
	}
}
// === end ===

// === case: exact_record_new_converts ===
class InputPreferVarExactRecordNewConvertsSliceViolation {
	record Point(int x, int y) {}

	void m() {
		final Point point = new Point(1, 2); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(point);
	}
}
// === end ===

// === case: exact_switch_same_class_arms_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactSwitchSameClassArmsConvertsSliceViolation {
	void m(int key) {
		final ArrayList<String> items = switch (key) { case 1 -> new ArrayList<>(); default -> new ArrayList<>(16); }; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: exact_ternary_same_class_arms_converts ===
// imports: java.util.ArrayList
class InputPreferVarExactTernarySameClassArmsConvertsSliceViolation {
	void m(boolean flag) {
		final ArrayList<String> items = flag ? new ArrayList<>() : new ArrayList<>(16); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: explicit_array_creation_used_as_a_receiver_converts ===
// multi-fix-expected
class InputPreferVarExplicitArrayCreationUsedAsAReceiverConvertsSliceViolation {
	void m() {
		final int[] copy = new int[]{1, 2}.clone(); // violation: Local variable must use 'var' instead of an explicit type.
		final int[] row = new int[][]{{1}}[0]; // violation: Local variable must use 'var' instead of an explicit type.
		final String[] names = new String[]{"a"}.clone(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(copy.length + row.length + names.length);
	}
}
// === end ===

// === case: explicit_array_creation_used_as_a_receiver_on_a_later_line_converts ===
// multi-fix-expected
class InputPreferVarExplicitArrayCreationUsedAsAReceiverOnALaterLineConvertsSliceViolation {
	void m() {
		final int[] split = new int[]{1, 2} // violation: Local variable must use 'var' instead of an explicit type.
				.clone();
		final int[] wrapped = new int[]{ // violation: Local variable must use 'var' instead of an explicit type.
			1, 2
		}.clone();
		System.out.println(split.length + wrapped.length);
	}
}
// === end ===

// === case: explicit_array_init_annotated_creation_type ===
// multi-fix-expected
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitAnnotatedCreationTypeSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {
	}

	void m() {
		final String[] element = new @Ann String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final String[] dimension = new String @Ann []{"b"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(element.length + dimension.length);
	}
}
// === end ===

// === case: explicit_array_init_annotated_declared_dimension ===
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
class InputPreferVarExplicitArrayInitAnnotatedDeclaredDimensionSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Ann {
	}

	void m() {
		final String @Ann [] declared = new String[]{"d"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(declared.length);
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
		final String @Ann(value = "x") [] declared = new String[]{"d"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(declared.length);
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") String[] arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_equal_sign_var ===
class InputPreferVarExplicitArrayInitAnnotationEqualSignVarSliceViolation {
	void m() {
		@SuppressWarnings(value = "x") var arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
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
		@Anno(a = "x", b = @Inner(c = "y")) String[] arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
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
		@Anno(a = "x", b = @Inner(c = "y")) var arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_on_previous_line ===
// imports: javax.annotation.Nonnull
class InputPreferVarExplicitArrayAnnotationOnPreviousLineSliceViolation {
	void m() {
		@Nonnull
		final String[] names = new String[]{"a", "b"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_annotation_paren_in_string ===
class InputPreferVarExplicitArrayInitAnnotationParenInStringSliceViolation {
	void m() {
		@SuppressWarnings("(") String[] arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_boxed_typed_matching ===
class InputPreferVarExplicitArrayInitBoxedTypedMatchingSliceViolation {
	void m() {
		final Integer[] boxed = new Integer[]{1}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(boxed.length);
	}
}
// === end ===

// === case: explicit_array_init_brace_in_comment ===
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitBraceInCommentSliceViolation {
	void m() {
		final var values = new String[] /* {"z"} */ {"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_c_style_name_spelled_var ===
class InputPreferVarExplicitArrayInitCStyleNameSpelledVarSliceViolation {
	void m() {
		String var[] = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(var.length);
	}
}
// === end ===

// === case: explicit_array_init_c_style_typed_matching ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitCStyleTypedMatchingSliceViolation {
	void m() {
		final String values[] = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[] grid[] = new int[][]{{1}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length + grid.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_explicit_type ===
class InputPreferVarExplicitArrayInitCommentBeforeNameExplicitTypeSliceViolation {
	void m() {
		final String[] /* keep */ arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_before_name_var ===
class InputPreferVarExplicitArrayInitCommentBeforeNameVarSliceViolation {
	void m() {
		final var /* keep */ arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_between_equals_and_new ===
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitCommentBetweenEqualsAndNewSliceViolation {
	void m() {
		final int[] values = /* keep */ new int[]{1}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_comment_equals_before_assignment ===
class InputPreferVarExplicitArrayInitCommentEqualsBeforeAssignmentSliceViolation {
	void m() {
		String[] /* = */ arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_comment_inside_declared_brackets ===
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitCommentInsideDeclaredBracketsSliceViolation {
	void m() {
		final String[/* c */] names = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(names.length);
	}
}
// === end ===

// === case: explicit_array_init_creation_type_on_a_later_line ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitCreationTypeOnALaterLineSliceViolation {
	void m() {
		final int[] values = new // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				int[]{1};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_dimensions_on_a_later_line ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitDimensionsOnALaterLineSliceViolation {
	void m() {
		final int[] values = new int // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				[]{1};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_empty_typed ===
class InputPreferVarExplicitArrayInitEmptyTypedSliceViolation {
	void m() {
		final String[] a = new String[]{}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_empty_var ===
class InputPreferVarExplicitArrayInitEmptyVarSliceViolation {
	void m() {
		final var a = new String[]{}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_for_init_single_declarator ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitForInitSingleDeclaratorSliceViolation {
	void m() {
		for (int[] counter = new int[]{0}; counter[0] < 3; ++counter[0]) // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
			System.out.println(counter[0]);
		for (var limit = new int[]{3}; limit[0] > 0; --limit[0]) // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
			System.out.println(limit[0]);
	}
}
// === end ===

// === case: explicit_array_init_generic_element_of_a_raw_creation_warns ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericElementOfARawCreationWarnsSliceViolation {
	void m(List<String> list) {
		final List<String>[] lists = new List[]{list}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(lists.length);
	}
}
// === end ===

// === case: explicit_array_init_generic_nested_element_of_a_raw_creation_warns ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarExplicitArrayInitGenericNestedElementOfARawCreationWarnsSliceViolation {
	void m(Map<String, List<Integer>> map) {
		final Map<String, List<Integer>>[] arr = new Map[]{map}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_generic_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitGenericTypeSliceViolation {
	void m(List<String> list) {
		final var a = new List<?>[]{list}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(a.length);
	}
}
// === end ===

// === case: explicit_array_init_generic_type_with_two_wildcards ===
// imports: java.util.Map
class InputPreferVarExplicitArrayInitGenericTypeWithTwoWildcardsSliceViolation {
	void m(Map<String, Integer> map) {
		final var maps = new Map<?, ?>[]{map}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(maps.length);
	}
}
// === end ===

// === case: explicit_array_init_list_of ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitListOfSliceViolation {
	void m() {
		final Object[] a = List.of(new Object[]{"a"}); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final int[][] m = new int[][]{{1}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_multi_space ===
class InputPreferVarExplicitArrayInitMultiSpaceSliceViolation {
	void m() {
		final String[] a =   new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_multi_var_warns ===
class InputPreferVarExplicitArrayInitMultiVarWarnsSliceViolation {
	void m() {
		final int[] same = new int[]{1}, other = new int[]{2}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Object[] wide = new String[]{"a"}, boxes = new Integer[]{1}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		for (int[] a = new int[]{1}, b = new int[]{2}; a.length < b.length; ++a[0]) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(a[0]);
		System.out.println(same.length + other.length + wide.length + boxes.length);
	}
}
// === end ===

// === case: explicit_array_init_name_on_a_later_line ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitNameOnALaterLineSliceViolation {
	void m() {
		final String[] // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				names = new String[]{"a"};
		System.out.println(names.length);
	}
}
// === end ===

// === case: explicit_array_init_nested_creation_element ===
class InputPreferVarExplicitArrayInitNestedCreationElementSliceViolation {
	void m() {
		final Object[] nested = new Object[]{new int[]{1}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(nested.length);
	}
}
// === end ===

// === case: explicit_array_init_new_on_a_later_line ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitNewOnALaterLineSliceViolation {
	void m() {
		final String[] names = // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				new String[]{"a"};
		System.out.println(names.length);
	}
}
// === end ===

// === case: explicit_array_init_non_final_typed_matching ===
class InputPreferVarExplicitArrayInitNonFinalTypedMatchingSliceViolation {
	void m() {
		String[] names = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		names = new String[]{"b"};
		System.out.println(names.length);
	}
}
// === end ===

// === case: explicit_array_init_object_typed_matching ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitObjectTypedMatchingSliceViolation {
	void m() {
		final Object[] values = new Object[]{"a", 1}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final Object[][] rows = new Object[][]{{1}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length + rows.length);
	}
}
// === end ===

// === case: explicit_array_init_parenthesized_typed ===
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitParenthesizedTypedSliceViolation {
	void m() {
		final int[] wrapped = (new int[]{1}); // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(wrapped.length);
	}
}
// === end ===

// === case: explicit_array_init_primitive_element_into_a_supertype_warns ===
class InputPreferVarExplicitArrayInitPrimitiveElementIntoASupertypeWarnsSliceViolation {
	void m() {
		final Object[] rows = new int[][]{{1}}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Object cStyleRows[] = new int[][]{{1}}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Cloneable cloneable = new int[]{1}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(rows.length + cStyleRows.length + cloneable.hashCode());
	}
}
// === end ===

// === case: explicit_array_init_primitive_typed_matching ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitPrimitiveTypedMatchingSliceViolation {
	void m() {
		final int[] plain = new int[]{1, 2}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[] commented = new int[]{1, 2} /* c */; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[] spanning = new int[]{ // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
			1, 2
		};
		System.out.println(plain.length + commented.length + spanning.length);
	}
}
// === end ===

// === case: explicit_array_init_qualified_declared_and_new_type ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitQualifiedDeclaredAndNewTypeSliceViolation {
	void m() {
		final java.lang.String[] both = new java.lang.String[]{"b"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final java.lang.String cStyle[] = new java.lang.String[]{"c"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final java.util.Date[] dates = new java.util.Date[]{new java.util.Date()}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(both.length + cStyle.length + dates.length);
	}
}
// === end ===

// === case: explicit_array_init_qualified_declared_type ===
// multi-fix-expected
class InputPreferVarExplicitArrayInitQualifiedDeclaredTypeSliceViolation {
	void m() {
		final java.lang.String[] qualified = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final String[] simple = new java.lang.String[]{"b"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(qualified.length + simple.length);
	}
}
// === end ===

// === case: explicit_array_init_qualified_new_type ===
class InputPreferVarExplicitArrayInitQualifiedNewTypeSliceViolation {
	void m() {
		final var a = new java.lang.String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_raw_declared_type ===
// imports: java.util.List
class InputPreferVarExplicitArrayInitRawDeclaredTypeSliceViolation {
	void m(List<String> list) {
		final List[] raws = new List[]{list}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(raws.length);
	}
}
// === end ===

// === case: explicit_array_init_space_before_brace ===
class InputPreferVarExplicitArrayInitSpaceBeforeBraceSliceViolation {
	void m() {
		final String[] values = new String[] {"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_spacing_inside_the_creation ===
// multi-fix-expected
// skip-reason: cannot rewrite the declaration: a comment, parenthesis, annotation or spacing in the array declaration would be lost or misread
class InputPreferVarExplicitArrayInitSpacingInsideTheCreationSliceViolation {
	void m() {
		final String[] spaced = new String [] {"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[] tabbed = new	int[]{1}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(spaced.length + tabbed.length);
	}
}
// === end ===

// === case: explicit_array_init_split_across_lines ===
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitSplitAcrossLinesSliceViolation {
	void m() {
		final int[] values = new int[] // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				{1, 2, 3};
		System.out.println(values.length);
	}
}
// === end ===

// === case: explicit_array_init_supertype_element_with_more_dimensions_warns ===
class InputPreferVarExplicitArrayInitSupertypeElementWithMoreDimensionsWarnsSliceViolation {
	void m() {
		final Object[] rows = new Object[][]{{1}}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Object cStyleRows[] = new Object[][]{{1}}; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(rows.length + cStyleRows.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_type_name ===
class InputPreferVarExplicitArrayInitSupplementaryTypeNameSliceViolation {
	static class Fo𝛼o {}

	void m() {
		final var a = new Fo𝛼o[]{new Fo𝛼o()}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(a.length);
	}
}
// === end ===

// === case: explicit_array_init_supplementary_var_name ===
class InputPreferVarExplicitArrayInitSupplementaryVarNameSliceViolation {
	void m() {
		final var 𝛼a = new String[]{"x"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(𝛼a.length);
	}
}
// === end ===

// === case: explicit_array_init_ternary ===
class InputPreferVarExplicitArrayInitTernarySliceViolation {
	void m(boolean cond) {
		final int[] a = cond ? new int[]{1} : new int[]{2}; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: explicit_array_init_type_arguments_on_a_later_line ===
// imports: java.util.List
// skip-reason: cannot rewrite the declaration: the explicit array initializer does not resolve on the reported line
class InputPreferVarExplicitArrayInitTypeArgumentsOnALaterLineSliceViolation {
	void m(List<String> list) {
		final var lists = new List< // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				?>[]{list};
		System.out.println(lists.length);
	}
}
// === end ===

// === case: explicit_array_init_typed_matching ===
class InputPreferVarExplicitArrayInitTypedMatchingSliceViolation {
	void m() {
		final String[] a = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_var_multi_dim ===
class InputPreferVarExplicitArrayInitVarMultiDimSliceViolation {
	void m() {
		final var rows = new int[][]{{1}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(rows.length);
	}
}
// === end ===

// === case: explicit_array_init_var_separated_by_a_comment ===
class InputPreferVarExplicitArrayInitVarSeparatedByACommentSliceViolation {
	void m() {
		final var/* keep */arr = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		System.out.println(arr.length);
	}
}
// === end ===

// === case: explicit_array_init_var_to_primitive ===
class InputPreferVarExplicitArrayInitVarToPrimitiveSliceViolation {
	void m() {
		final var a = new int[]{1, 2}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_array_init_var_to_string ===
class InputPreferVarExplicitArrayInitVarToStringSliceViolation {
	void m() {
		final var a = new String[]{"a"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: explicit_type_arguments_in_initializer ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarExplicitTypeArgumentsInInitializerSliceViolation {
	void m() {
		final Map<String, Integer> counts = new HashMap<String, Integer>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(counts);
	}
}
// === end ===

// === case: explicit_type_arguments_in_initializer_exact ===
// imports: java.util.HashMap
class InputPreferVarExplicitTypeArgumentsInInitializerExactSliceViolation {
	void m() {
		final HashMap<String, Integer> counts = new HashMap<String, Integer>(); // violation: Local variable must use 'var' instead of an explicit type.
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
		Base item = makeDerived(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: final_class_shortcut_all_null_conditional_warns ===
class InputPreferVarFinalClassShortcutAllNullConditionalWarnsSliceViolation {
	void m(boolean flag) {
		final String text = flag ? null : null; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: final_class_shortcut_conditional_argument_warns ===
// imports: java.util.Objects
class InputPreferVarFinalClassShortcutConditionalArgumentWarnsSliceViolation {
	<T> T get(String key) {
		return null;
	}

	void m(boolean flag) {
		final String text = Objects.requireNonNull(flag ? get("a") : get("b")); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(text.length());
	}
}
// === end ===

// === case: final_class_shortcut_conditional_arm_unproven_call_warns ===
class InputPreferVarFinalClassShortcutConditionalArmUnprovenCallWarnsSliceViolation {
	record Label(String text) {}

	static <T> T make() {
		return null;
	}

	void m(boolean flag, Label fallback) {
		final Label label = flag ? make() : fallback; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_conditional_name_arms_warns ===
class InputPreferVarFinalClassShortcutConditionalNameArmsWarnsSliceViolation {
	record Label(String text) {}

	void m(boolean flag, Holder holder, Label fallback) {
		final Label label = flag ? holder.label : fallback; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_fully_qualified_warns ===
class InputPreferVarFinalClassShortcutFullyQualifiedWarnsSliceViolation {
	void m(Holder holder) {
		final java.time.Duration total = holder.total; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(total);
	}
}
// === end ===

// === case: final_class_shortcut_generic_call_on_a_foreign_receiver_warns ===
class InputPreferVarFinalClassShortcutGenericCallOnAForeignReceiverWarnsSliceViolation {
	static class Registry {
		<T> T get(String key) {
			return null;
		}
	}

	void m(Registry registry, Object source) {
		final String cast = ((Registry) source).get("name"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final String created = new Registry().get("name"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final String name = registry.get("name"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(cast.length() + created.length() + name.length());
	}
}
// === end ===

// === case: final_class_shortcut_java_lang_module_warns ===
class InputPreferVarFinalClassShortcutJavaLangModuleWarnsSliceViolation {
	void m(Holder holder) {
		final Module module = holder.module; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(module);
	}
}
// === end ===

// === case: final_class_shortcut_java_lang_string_builder_warns ===
class InputPreferVarFinalClassShortcutJavaLangStringBuilderWarnsSliceViolation {
	void m(Holder holder) {
		final StringBuilder builder = holder.builder; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(builder);
	}
}
// === end ===

// === case: final_class_shortcut_java_lang_string_proven_initializers_convert ===
// multi-fix-expected
class InputPreferVarFinalClassShortcutJavaLangStringProvenInitializersConvertSliceViolation {
	void m(char[] chars, Object source, String typed) {
		final String literal = "x"; // violation: Local variable must use 'var' instead of an explicit type.
		final String created = new String(chars); // violation: Local variable must use 'var' instead of an explicit type.
		final String cast = (String) source; // violation: Local variable must use 'var' instead of an explicit type.
		final String name = typed; // violation: Local variable must use 'var' instead of an explicit type.
		final String call = typed.substring(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(literal + created + cast + name + call);
	}
}
// === end ===

// === case: final_class_shortcut_java_lang_string_warns ===
class InputPreferVarFinalClassShortcutJavaLangStringWarnsSliceViolation {
	void m(Holder holder) {
		final String text = holder.text; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: final_class_shortcut_lambda_argument_warns ===
// imports: java.util.function.Supplier
class InputPreferVarFinalClassShortcutLambdaArgumentWarnsSliceViolation {
	record Label(String text) {}

	static <T> T get(Supplier<T> supplier) {
		return supplier.get();
	}

	static <T> T make() {
		return null;
	}

	void m() {
		final Label label = get(InputPreferVarFinalClassShortcutLambdaArgumentWarnsSliceViolation::make); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_name_argument_warns ===
// imports: java.util.Objects
class InputPreferVarFinalClassShortcutNameArgumentWarnsSliceViolation {
	record Label(String text) {}

	void m(Holder holder) {
		final Label label = Objects.requireNonNull(holder.label); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_on_demand_import_warns ===
// imports: java.time.*
class InputPreferVarFinalClassShortcutOnDemandImportWarnsSliceViolation {
	void m(Holder holder) {
		final Duration total = holder.total; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(total);
	}
}
// === end ===

// === case: final_class_shortcut_raw_generic_classpath_class_warns ===
// imports: java.util.Optional
class InputPreferVarFinalClassShortcutRawGenericClasspathClassWarnsSliceViolation {
	void m() {
		Optional raw = Optional.of("a"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		raw = Optional.of(1);
		System.out.println(raw);
	}
}
// === end ===

// === case: final_class_shortcut_raw_generic_same_file_record_warns ===
class InputPreferVarFinalClassShortcutRawGenericSameFileRecordWarnsSliceViolation {
	record Pair<A, B>(A first, B second) {}

	void m() {
		Pair raw = new Pair<>("a", 1); // violation (warning): Local variable should use 'var' instead of an explicit type.
		raw = new Pair<>(1, "a");
		System.out.println(raw);
	}
}
// === end ===

// === case: final_class_shortcut_same_file_record_warns ===
class InputPreferVarFinalClassShortcutSameFileRecordWarnsSliceViolation {
	record Label(String text) {}

	void m(Holder holder) {
		final Label label = holder.label; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_single_type_import_warns ===
// imports: java.time.Duration
class InputPreferVarFinalClassShortcutSingleTypeImportWarnsSliceViolation {
	void m(Holder holder) {
		final Duration total = holder.total; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(total.compareTo(Duration.ZERO));
	}
}
// === end ===

// === case: final_class_shortcut_switch_arm_unproven_call_warns ===
class InputPreferVarFinalClassShortcutSwitchArmUnprovenCallWarnsSliceViolation {
	record Label(String text) {}

	static <T> T make() {
		return null;
	}

	void m(int kind, Label fallback) {
		final Label label = switch (kind) { // violation (warning): Local variable should use 'var' instead of an explicit type.
			case 1 -> make();
			default -> fallback;
		};
		System.out.println(label.text());
	}
}
// === end ===

// === case: final_class_shortcut_this_in_a_final_class_warns ===
class InputPreferVarFinalClassShortcutThisInAFinalClassWarnsSliceViolation {
	static final class Node {
		void m() {
			final Node self = this; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(self);
		}
	}
}
// === end ===

// === case: final_class_shortcut_this_in_an_enum_constant_body_through_a_conditional_and_a_call_warns ===
// imports: java.util.Objects
class InputPreferVarFinalClassShortcutThisInAnEnumConstantBodyThroughAConditionalAndACallWarnsSliceViolation {
	enum Mode {
		FAST {
			@Override
			void run(boolean flag) {
				final Mode chosen = flag ? this : null; // violation (warning): Local variable should use 'var' instead of an explicit type.
				final Mode checked = Objects.requireNonNull(this); // violation (warning): Local variable should use 'var' instead of an explicit type.
				System.out.println(chosen);
				System.out.println(checked);
			}
		},
		SLOW {
			@Override
			void run(boolean flag) {
				System.out.println(this);
			}
		};

		abstract void run(boolean flag);
	}
}
// === end ===

// === case: final_class_shortcut_this_in_an_enum_constant_body_through_a_var_warns ===
class InputPreferVarFinalClassShortcutThisInAnEnumConstantBodyThroughAVarWarnsSliceViolation {
	enum Mode {
		FAST {
			@Override
			void run() {
				final var self = this;
				final Mode current = self; // violation (warning): Local variable should use 'var' instead of an explicit type.
				System.out.println(current);
			}
		},
		SLOW {
			@Override
			void run() {
				System.out.println(this);
			}
		};

		abstract void run();
	}
}
// === end ===

// === case: final_class_shortcut_this_in_an_enum_constant_body_warns ===
class InputPreferVarFinalClassShortcutThisInAnEnumConstantBodyWarnsSliceViolation {
	enum Mode {
		FAST {
			@Override
			void run() {
				Mode current = this; // violation (warning): Local variable should use 'var' instead of an explicit type.
				current = SLOW;
				System.out.println(current);
			}
		},
		SLOW {
			@Override
			void run() {
				System.out.println(this);
			}
		};

		abstract void run();
	}
}
// === end ===

// === case: final_class_shortcut_unproven_call_argument_warns ===
// imports: java.util.Objects
class InputPreferVarFinalClassShortcutUnprovenCallArgumentWarnsSliceViolation {
	record Label(String text) {}

	static <T> T make() {
		return null;
	}

	void m() {
		final Label label = Objects.requireNonNull(make()); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(label.text());
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
	void m(List<String> list) {
		for (String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotated ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotatedSliceViolation {
	void m(List<String> list) {
		for (@Nonnull String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_prev_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnPrevLineSliceViolation {
	void m(List<String> list) {
		for (
				@Nonnull final
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_and_final_on_var_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationAndFinalOnVarLineSliceViolation {
	void m(List<String> list) {
		for (
				@Nonnull
				final String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_annotation_on_previous_line ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachAnnotationOnPreviousLineSliceViolation {
	void m(List<String> list) {
		for (
				@Nonnull
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_blank_between_paren_and_decl ===
// imports: java.util.List
class InputPreferVarForEachBlankBetweenParenAndDeclSliceViolation {
	void m(List<String> list) {
		for (

				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
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
		for (String s : list) { consume(a = new String[]{"b"}); } // violation: For-each variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: for_each_boxed_element_type_over_a_matching_array ===
class InputPreferVarForEachBoxedElementTypeOverAMatchingArraySliceViolation {
	void m(Integer[] counts) {
		for (Integer count : counts) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(count);
	}
}
// === end ===

// === case: for_each_closeable_over_input_streams_warns ===
// imports: java.io.Closeable
// imports: java.io.InputStream
// imports: java.util.List
class InputPreferVarForEachCloseableOverInputStreamsWarnsSliceViolation {
	void m(List<InputStream> streams) {
		for (Closeable stream : streams) // violation (warning): For-each variable should use 'var' instead of an explicit type.
			System.out.println(stream);
	}
}
// === end ===

// === case: for_each_collection_over_lists_warns ===
// imports: java.util.Collection
// imports: java.util.List
class InputPreferVarForEachCollectionOverListsWarnsSliceViolation {
	void m(List<List<String>> lists) {
		for (Collection<String> values : lists) // violation (warning): For-each variable should use 'var' instead of an explicit type.
			System.out.println(values);
	}
}
// === end ===

// === case: for_each_element_of_a_generic_call_warns ===
// imports: java.util.List
class InputPreferVarForEachElementOfAGenericCallWarnsSliceViolation {
	void m() {
		for (String item : List.of("a", "b")) // violation (warning): For-each variable should use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_final ===
// imports: java.util.List
class InputPreferVarForEachFinalSliceViolation {
	void m(List<String> l) {
		for (final String i : l) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(i);
	}
}
// === end ===

// === case: for_each_final_only_prev_line ===
// imports: java.util.List
class InputPreferVarForEachFinalOnlyPrevLineSliceViolation {
	void m(List<String> list) {
		for (
				final
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_final_qualified_generic_element_type_converts ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarForEachFinalQualifiedGenericElementTypeConvertsSliceViolation {
	void m(List<Map.Entry<String, Integer>> entries) {
		for (final java.util.Map.Entry<String, Integer> entry : entries) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(entry);
	}
}
// === end ===

// === case: for_each_generic_type ===
// imports: java.util.Map
// imports: java.util.Map.Entry
class InputPreferVarForEachGenericTypeSliceViolation {
	void m(Map<String, Integer> map) {
		for (Entry<String, Integer> entry : map.entrySet()) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(entry);
	}
}
// === end ===

// === case: for_each_input_stream_over_input_streams_converts ===
// imports: java.io.InputStream
// imports: java.util.List
class InputPreferVarForEachInputStreamOverInputStreamsConvertsSliceViolation {
	void m(List<InputStream> streams) {
		for (InputStream stream : streams) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(stream);
	}
}
// === end ===

// === case: for_each_open_paren_prev_line ===
// imports: java.util.List
class InputPreferVarForEachOpenParenPrevLineSliceViolation {
	void m(List<String> list) {
		for (
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_open_paren_with_trailing_comment ===
// imports: java.util.List
class InputPreferVarForEachOpenParenWithTrailingCommentSliceViolation {
	void m(List<String> list) {
		for ( // header
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_primitive_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachPrimitiveArrayElementTypeSliceViolation {
	void m(List<int[]> intRows) {
		for (int[] row : intRows) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_primitive_element_type_over_an_array ===
class InputPreferVarForEachPrimitiveElementTypeOverAnArraySliceViolation {
	void m(int[] sizes) {
		for (int size : sizes) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(size);
	}
}
// === end ===

// === case: for_each_qualified_element_type_converts ===
// multi-fix-expected
// imports: java.util.List
class InputPreferVarForEachQualifiedElementTypeConvertsSliceViolation {
	void m(String[] names, List<String[]> rows) {
		for (java.lang.String name : names) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(name);
		for (java.lang.String[] row : rows) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_reference_array_element_type ===
// imports: java.util.List
class InputPreferVarForEachReferenceArrayElementTypeSliceViolation {
	void m(List<String[]> rows) {
		for (String[] row : rows) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(row.length);
	}
}
// === end ===

// === case: for_each_two_annotations_prev_lines ===
// imports: java.util.List
// imports: javax.annotation.Nonnull
class InputPreferVarForEachTwoAnnotationsPrevLinesSliceViolation {
	void m(List<String> list) {
		for (
				@Nonnull
				@SuppressWarnings("unused")
				String item : list) // violation: For-each variable must use 'var' instead of an explicit type.
			System.out.println(item);
	}
}
// === end ===

// === case: for_each_wildcard_element_warns ===
// imports: java.io.InputStream
// imports: java.util.List
class InputPreferVarForEachWildcardElementWarnsSliceViolation {
	void m(List<? extends InputStream> streams) {
		for (InputStream stream : streams) // violation (warning): For-each variable should use 'var' instead of an explicit type.
			System.out.println(stream);
	}
}
// === end ===

// === case: for_init_reassigned_beyond_the_constructed_type_warns ===
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
		for (List<String> items = new ArrayList<>(); flag; flag = false) { // violation (warning): Local variable should use 'var' instead of an explicit type.
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

// === case: for_loop_init_reference_type_on_a_typed_receiver ===
// imports: java.util.Iterator
// imports: java.util.List
class InputPreferVarForLoopInitReferenceTypeOnATypedReceiverSliceViolation {
	void m(List<String> list) {
		for (Iterator<String> it = list.iterator(); it.hasNext(); ) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(it.next());
	}
}
// === end ===

// === case: for_loop_init_reference_type_on_a_var_receiver_is_not_resolvable ===
// imports: java.util.Iterator
// imports: java.util.List
class InputPreferVarForLoopInitReferenceTypeOnAVarReceiverIsNotResolvableSliceViolation {
	void m() {
		final var list = List.of("a", "b");
		for (Iterator<String> it = list.iterator(); it.hasNext(); )
			System.out.println(it.next());
	}
}
// === end ===

// === case: generic_factory_with_lambda_arguments ===
// imports: java.util.List
class InputPreferVarGenericFactoryWithLambdaArgumentsSliceViolation {
	void m() {
		final List<Runnable> tasks = List.of(() -> System.out.println("x")); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
				final var value = make(); // violation (warning): Consider using an explicit type ('var' with 'make' loses generic type information).
				System.out.println(value);
			}
		};
		System.out.println(task);
	}
}
// === end ===

// === case: generic_return_inferable_from_a_parameter_warns ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarGenericReturnInferableFromAParameterWarnsSliceViolation {
	static <K, V> Map<K, V> pair(K key, V value) {
		return Map.of(key, value);
	}

	static <T> List<T> wrap(T value) {
		return List.of(value);
	}

	void m() {
		final List<String> names = wrap("a"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Map<String, Integer> counts = pair("a", 1); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names.getFirst().length() + counts.get("a"));
	}
}
// === end ===

// === case: generic_return_inherited_by_an_anonymous_class_warns ===
abstract class InputPreferVarGenericReturnAnonymousSliceBase {
	<T> T lookup(String key) {
		return null;
	}

	abstract void run();
}

class InputPreferVarGenericReturnInheritedByAnAnonymousClassSliceViolation {
	void m() {
		new InputPreferVarGenericReturnAnonymousSliceBase() {
			@Override
			void run() {
				final String name = lookup("k");
				final var any = lookup("j"); // violation (warning): Consider using an explicit type ('var' with 'lookup' loses generic type information).
				System.out.println(name.length() + any.hashCode());
			}
		}.run();
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
		final var value = make(); // violation (warning): Consider using an explicit type ('var' with 'make' loses generic type information).
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
		final var value = pick(); // violation (warning): Consider using an explicit type ('var' with 'pick' loses generic type information).
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_mentioned_by_two_type_variables_warns ===
// imports: java.util.HashMap
// imports: java.util.Map
class InputPreferVarGenericReturnMentionedByTwoTypeVariablesSliceViolation {
	static <K, V> Map<K, V> keyed(K key) {
		return new HashMap<>();
	}

	static <K, V> Map<K, V> newMap() {
		return new HashMap<>();
	}

	void m() {
		final Map<String, Integer> counts = newMap();
		final Map<String, Integer> byKey = keyed("a");
		final var all = newMap(); // violation (warning): Consider using an explicit type ('var' with 'newMap' loses generic type information).
		final var some = keyed("b"); // violation (warning): Consider using an explicit type ('var' with 'keyed' loses generic type information).
		System.out.println(counts.get("a") + byKey.get("a") + all.size() + some.size());
	}
}
// === end ===

// === case: generic_return_mentioned_in_a_qualified_type_warns ===
// imports: java.util.ArrayList
class InputPreferVarGenericReturnMentionedInAQualifiedTypeSliceViolation {
	static <T> java.util.List<T> empty() {
		return new ArrayList<>();
	}

	void m() {
		final java.util.List<String> names = empty();
		final var all = empty(); // violation (warning): Consider using an explicit type ('var' with 'empty' loses generic type information).
		System.out.println(names.getFirst().length() + all.size());
	}
}
// === end ===

// === case: generic_return_mentioned_in_a_type_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Optional
class InputPreferVarGenericReturnMentionedInATypeArgumentSliceViolation {
	static <T> List<T> empty() {
		return new ArrayList<>();
	}

	static <T> Optional<T> find() {
		return Optional.empty();
	}

	void m() {
		final List<String> names = empty();
		final Optional<String> found = find();
		final var all = empty(); // violation (warning): Consider using an explicit type ('var' with 'empty' loses generic type information).
		final var any = find(); // violation (warning): Consider using an explicit type ('var' with 'find' loses generic type information).
		System.out.println(names.getFirst().length() + found.map(String::length).orElse(0) + all.size() + any.hashCode());
	}
}
// === end ===

// === case: generic_return_mentioned_in_a_wildcard_bound_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarGenericReturnMentionedInAWildcardBoundSliceViolation {
	static <T> List<? extends T> items() {
		return new ArrayList<>();
	}

	void m() {
		final List<? extends CharSequence> texts = items();
		final var all = items(); // violation (warning): Consider using an explicit type ('var' with 'items' loses generic type information).
		System.out.println(texts.getFirst().length() + all.size());
	}
}
// === end ===

// === case: generic_return_mentioned_in_an_array_element_warns ===
// imports: java.util.List
class InputPreferVarGenericReturnMentionedInAnArrayElementSliceViolation {
	static <T> List<T>[] buckets() {
		return null;
	}

	void m() {
		final List<String>[] names = buckets();
		final var all = buckets(); // violation (warning): Consider using an explicit type ('var' with 'buckets' loses generic type information).
		System.out.println(names[0].getFirst().length() + all.length);
	}
}
// === end ===

// === case: generic_return_no_parameters_warns ===
class InputPreferVarGenericReturnNoParametersSliceViolation {
	static <T> T make() {
		return null;
	}

	void m() {
		final var value = make(); // violation (warning): Consider using an explicit type ('var' with 'make' loses generic type information).
		System.out.println(value);
	}
}
// === end ===

// === case: generic_return_not_mentioning_its_type_variable_converts ===
// imports: java.util.List
class InputPreferVarGenericReturnNotMentioningItsTypeVariableConvertsSliceViolation {
	static <T> List<String> labels() {
		return List.of("a");
	}

	void m() {
		final List<String> names = labels(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names.getFirst().length());
	}
}
// === end ===

// === case: generic_return_on_a_qualified_this_call_warns ===
class InputPreferVarGenericReturnOnAQualifiedThisCallSliceViolation {
	static class Outer {
		class Inner {
			void m() {
				final String name = Outer.this.find("k");
				final var any = Outer.this.find("j"); // violation (warning): Consider using an explicit type ('var' with 'find' loses generic type information).
				final String label = Outer.this.label(); // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(name.length() + label.length() + any.hashCode());
			}
		}

		<T> T find(String key) {
			return null;
		}

		String label() {
			return "";
		}
	}
}
// === end ===

// === case: generic_return_on_a_super_qualified_call_warns ===
class InputPreferVarGenericReturnSuperQualifiedSliceBase {
	<T> T lookup(String key) {
		return null;
	}
}

class InputPreferVarGenericReturnOnASuperQualifiedCallSliceViolation extends InputPreferVarGenericReturnSuperQualifiedSliceBase {
	void m() {
		final String name = super.lookup("k");
		final var any = super.lookup("j"); // violation (warning): Consider using an explicit type ('var' with 'lookup' loses generic type information).
		System.out.println(name.length() + any.hashCode());
	}
}
// === end ===

// === case: generic_return_on_a_this_qualified_call_with_type_arguments_warns ===
class InputPreferVarGenericReturnOnAThisQualifiedCallWithTypeArgumentsSliceViolation {
	<T> T lookup(String key) {
		return null;
	}

	void m() {
		final String name = this.<String>lookup("k"); // violation (warning): Consider using an explicit type instead of type arguments on 'lookup'.
		final var any = this.<String>lookup("j"); // violation (warning): Consider using an explicit type instead of type arguments on 'lookup'.
		System.out.println(name.length() + any.length());
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
				final var any = Outer.make(); // violation (warning): Consider using an explicit type ('var' with 'make' loses generic type information).
				final String typed = Outer.<String>make(); // violation (warning): Consider using an explicit type instead of type arguments on 'make'.
				final String label = Outer.label(); // violation: Local variable must use 'var' instead of an explicit type.
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

// === case: generic_return_on_an_interface_super_call_warns ===
interface InputPreferVarGenericReturnInterfaceSuperSliceSource {
	default <T> T pick() {
		return null;
	}

	default String plain() {
		return "";
	}
}

class InputPreferVarGenericReturnOnAnInterfaceSuperCallSliceViolation implements InputPreferVarGenericReturnInterfaceSuperSliceSource {
	void m() {
		final String name = InputPreferVarGenericReturnInterfaceSuperSliceSource.super.pick();
		final var any = InputPreferVarGenericReturnInterfaceSuperSliceSource.super.pick(); // violation (warning): Consider using an explicit type ('var' with 'pick' loses generic type information).
		final String label = InputPreferVarGenericReturnInterfaceSuperSliceSource.super.plain(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(name.length() + label.length() + any.hashCode());
	}
}
// === end ===

// === case: generic_return_second_type_parameter_warns ===
class InputPreferVarGenericReturnSecondTypeParameterSliceViolation {
	static <K, V> V get(K key) {
		return null;
	}

	void m() {
		final var value = get("a"); // violation (warning): Consider using an explicit type ('var' with 'get' loses generic type information).
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
		final var value = cast(new ArrayList<>()); // violation (warning): Consider using an explicit type ('var' with 'cast' loses generic type information).
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
		final var value = cast(new ArrayList<String>()); // violation (warning): Consider using an explicit type ('var' with 'cast' loses generic type information).
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

// === case: inherited_classpath_field_of_another_name_converts ===
// imports: java.io.Closeable
// imports: java.io.FilterInputStream
// imports: java.io.IOException
// imports: java.io.InputStream
class InputPreferVarInheritedClasspathFieldOfAnotherNameConvertsSliceViolation {
	Closeable stream;

	InputStream wrap(InputStream raw) {
		return new FilterInputStream(raw) {
			@Override
			public int read() throws IOException {
				final Closeable source = stream; // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(source);
				return super.read();
			}
		};
	}
}
// === end ===

// === case: inherited_classpath_field_shadows_an_outer_field_in_a_named_inner_class_warns ===
// imports: java.io.Closeable
// imports: java.io.FilterInputStream
// imports: java.io.IOException
// imports: java.io.InputStream
class InputPreferVarInheritedClasspathFieldShadowsAnOuterFieldInANamedInnerClassWarnsSliceViolation {
	class Wrapped extends FilterInputStream {
		Wrapped(InputStream raw) {
			super(raw);
		}

		@Override
		public int read() throws IOException {
			final Closeable source = in; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(source);
			return super.read();
		}
	}

	Closeable in;
}
// === end ===

// === case: inherited_classpath_field_shadows_an_outer_field_in_an_anonymous_class_warns ===
// imports: java.io.Closeable
// imports: java.io.FilterInputStream
// imports: java.io.IOException
// imports: java.io.InputStream
class InputPreferVarInheritedClasspathFieldShadowsAnOuterFieldInAnAnonymousClassWarnsSliceViolation {
	Closeable in;

	InputStream wrap(InputStream raw) {
		return new FilterInputStream(raw) {
			@Override
			public int read() throws IOException {
				final Closeable source = in; // violation (warning): Local variable should use 'var' instead of an explicit type.
				System.out.println(source);
				return super.read();
			}
		};
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

// === case: inner_class_of_a_classpath_generic_class_inherited_by_a_same_file_class_warns ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathGenericOuter
class InputPreferVarInnerClassOfAClasspathGenericClassInheritedByASameFileClassWarnsSliceViolation {
	static class Strings extends ClasspathGenericOuter<String> {
		Inner make() {
			return new Inner();
		}
	}

	void m(Object source, Strings strings) {
		ClasspathGenericOuter.Inner raw = strings.make(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		raw = new ClasspathGenericOuter<Integer>().new Inner();
		final ClasspathGenericOuter.Inner cast = (ClasspathGenericOuter.Inner) source; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(raw);
		System.out.println(cast);
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_created_on_another_instance_of_the_same_type_warns ===
class InputPreferVarInnerClassOfAGenericClassCreatedOnAnotherInstanceOfTheSameTypeWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		void m(Outer<T> other) {
			final Inner created = other.new Inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(created);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_inherited_warns ===
class InputPreferVarInnerClassOfAGenericClassInheritedWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		Inner inner() {
			return new Inner();
		}
	}

	static class Strings extends Outer<String> {
		void m() {
			final Inner mine = inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(mine);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_inside_a_cyclic_nested_hierarchy_terminates ===
class InputPreferVarInnerClassOfAGenericClassInsideACyclicNestedHierarchyTerminatesSliceViolation {
	static class Outer<T> {
		class Inner {
		}

		class Sub extends Outer<T> implements Sub.Named {
			interface Named extends Inner {
			}

			void m() {
				final Inner value = new Inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
				System.out.println(value);
			}
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_read_through_a_raw_receiver_warns ===
class InputPreferVarInnerClassOfAGenericClassReadThroughARawReceiverWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
			T value() {
				return null;
			}
		}

		Inner field = new Inner();

		Inner inner() {
			return new Inner();
		}

		void m(Outer raw) {
			final Inner called = raw.inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			final Inner read = raw.field; // violation (warning): Local variable should use 'var' instead of an explicit type.
			final Inner created = raw.new Inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(called.value());
			System.out.println(read.value());
			System.out.println(created.value());
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_read_through_another_instance_of_the_same_type_converts ===
// multi-fix-expected
class InputPreferVarInnerClassOfAGenericClassReadThroughAnotherInstanceOfTheSameTypeConvertsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		Inner field = new Inner();

		Inner inner() {
			return new Inner();
		}

		void m(Outer<T> other) {
			final Inner called = other.inner(); // violation: Local variable must use 'var' instead of an explicit type.
			final Inner read = other.field; // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(called);
			System.out.println(read);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_class_nested_in_a_raw_subclass_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideAClassNestedInARawSubclassWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		void run(Inner given) {
			new Outer() {
				void use() {
					new Object() {
						void nested() {
							Inner copy = given; // violation (warning): Local variable should use 'var' instead of an explicit type.
							copy = new Outer<Integer>().new Inner();
							System.out.println(copy);
						}
					};
				}
			};
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_cyclic_same_file_hierarchy_converts ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideACyclicSameFileHierarchyConvertsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		class CycA extends CycB {
			void use(Inner given) {
				final Inner copy = given; // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(copy);
			}
		}

		class CycB extends CycA {
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_raw_subclass_beside_another_declaring_class ===
// multi-fix-expected
class InputPreferVarInnerClassOfAGenericClassWrittenInsideARawSubclassBesideAnotherDeclaringClassSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		class Mid<U> {
			final class MidInner {
			}

			void run(Inner given, MidInner givenMid) {
				new Outer() {
					void use() {
						final Inner copy = given; // violation (warning): Local variable should use 'var' instead of an explicit type.
						final MidInner kept = givenMid; // violation: Local variable must use 'var' instead of an explicit type.
						System.out.println(copy);
						System.out.println(kept);
					}
				};
				new Outer() {
					void use() {
						final MidInner kept = givenMid; // violation: Local variable must use 'var' instead of an explicit type.
						final Inner copy = given; // violation (warning): Local variable should use 'var' instead of an explicit type.
						System.out.println(copy);
						System.out.println(kept);
					}
				};
			}
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_raw_subclass_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideARawSubclassWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
			T value() {
				return null;
			}
		}

		class Raw extends Outer {
			Inner held;
		}

		void run(Inner given, Raw raw) {
			new Outer() {
				void use() {
					Inner copy = given; // violation (warning): Local variable should use 'var' instead of an explicit type.
					copy = new Outer<Integer>().new Inner();
					System.out.println(copy);
				}
			};
			final Inner held = raw.held; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(held.value());
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_subclass_of_its_own_type_converts ===
// multi-fix-expected
class InputPreferVarInnerClassOfAGenericClassWrittenInsideASubclassOfItsOwnTypeConvertsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		class Typed extends Outer<T> {
			Inner held;
		}

		void run(Inner given, Typed typed) {
			new Outer<T>() {
				void use() {
					final Inner copy = given; // violation: Local variable must use 'var' instead of an explicit type.
					System.out.println(copy);
				}
			};
			new Object() {
				void use() {
					final Inner copy = given; // violation: Local variable must use 'var' instead of an explicit type.
					System.out.println(copy);
				}
			};
			final Inner held = typed.held; // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(held);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_subclass_with_an_unresolvable_supertype_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideASubclassWithAnUnresolvableSupertypeWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		interface Marker extends MissingBase {
		}

		class Typed extends Outer<T> implements Marker {
			Inner held;
		}

		void run(Typed typed) {
			final Inner held = typed.held; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(held);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_a_subclass_with_other_type_arguments_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideASubclassWithOtherTypeArgumentsWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		void run() {
			new Outer<String>() {
				void use() {
					final Inner made = new Inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
					System.out.println(made);
				}
			};
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_an_indirect_subclass_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenInsideAnIndirectSubclassWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		class Middle extends Outer<T> {
		}

		class Typed extends Middle {
			Inner held;
		}

		void run(Typed typed) {
			final Inner held = typed.held; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(held);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_inside_it_converts ===
// multi-fix-expected
class InputPreferVarInnerClassOfAGenericClassWrittenInsideItConvertsSliceViolation {
	static class Outer<T> {
		final class Inner {
		}

		Inner field = new Inner();

		Inner inner() {
			return new Inner();
		}

		void m() {
			final Inner called = inner(); // violation: Local variable must use 'var' instead of an explicit type.
			final Inner created = new Inner(); // violation: Local variable must use 'var' instead of an explicit type.
			final Inner read = field; // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(called);
			System.out.println(created);
			System.out.println(read);
		}
	}
}
// === end ===

// === case: inner_class_of_a_generic_class_written_raw_warns ===
class InputPreferVarInnerClassOfAGenericClassWrittenRawWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
			T value() {
				return null;
			}
		}

		Inner field = new Inner();

		Inner inner() {
			return new Inner();
		}
	}

	void m() {
		final Outer<String> strings = new Outer<>(); // violation: Local variable must use 'var' instead of an explicit type.
		Outer.Inner raw = strings.inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		raw = new Outer<Integer>().inner();
		final Outer.Inner read = strings.field; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(raw.value());
		System.out.println(read.value());
	}
}
// === end ===

// === case: inner_class_of_a_generic_record_written_raw_warns ===
class InputPreferVarInnerClassOfAGenericRecordWrittenRawWarnsSliceViolation {
	record Pair<A>(A first) {
		final class Inner {
		}

		Inner inner() {
			return new Inner();
		}
	}

	void m(Pair<String> pair) {
		final Pair.Inner inner = pair.inner(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(inner);
	}
}
// === end ===

// === case: inner_class_of_a_non_generic_class_written_qualified_converts ===
class InputPreferVarInnerClassOfANonGenericClassWrittenQualifiedConvertsSliceViolation {
	class Plain {
		final class Inner {
		}

		Inner inner() {
			return new Inner();
		}
	}

	void m(Plain plain) {
		final Plain.Inner inner = plain.inner(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(inner);
	}
}
// === end ===

// === case: inner_class_of_an_inner_class_of_a_generic_class_written_inside_it_converts ===
class InputPreferVarInnerClassOfAnInnerClassOfAGenericClassWrittenInsideItConvertsSliceViolation {
	static class Outer<T> {
		final class Inner {
			final class Deeper {
			}

			Deeper deeper() {
				return new Deeper();
			}

			void m() {
				final Deeper own = deeper(); // violation: Local variable must use 'var' instead of an explicit type.
				System.out.println(own);
			}
		}
	}
}
// === end ===

// === case: inner_class_of_an_inner_class_of_a_generic_class_written_raw_warns ===
class InputPreferVarInnerClassOfAnInnerClassOfAGenericClassWrittenRawWarnsSliceViolation {
	static class Outer<T> {
		final class Inner {
			final class Deeper {
			}

			Deeper deeper() {
				return new Deeper();
			}
		}
	}

	void m(Outer<String>.Inner typed) {
		final Outer.Inner.Deeper deeper = typed.deeper(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(deeper);
	}
}
// === end ===

// === case: local_class_inside_a_generic_class_converts ===
class InputPreferVarLocalClassInsideAGenericClassConvertsSliceViolation {
	static class Outer<T> {
		void m() {
			class Local {
			}

			final Local local = new Local(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(local);
		}
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
		final List<Integer> list = List.of(1, 2, 3); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final var names = new String[]{"a", "b"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final String[] numbers = new String[]{"1"}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[][] matrix = new int[][]{{1}, {2}}; // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final var parenArr = (new String[]{"x"}); // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
	}
}
// === end ===

// === case: method_call_and_chain ===
// multi-fix-expected
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
		final var map = new HashMap<Object, java.lang.Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: multi_dim_array ===
class InputPreferVarMultiDimArraySliceViolation {
	void m() {
		int[][] m = new int[3][3]; // violation: Local variable must use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multi_var_supplementary_before_type_warns ===
class InputPreferVarMultiVarSupplementaryBeforeTypeSliceViolation {
	void m() {
		/* 𝛼 */ final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(x + y);
	}
}
// === end ===

// === case: multi_var_ternary_initializer_warns ===
class InputPreferVarMultiVarTernaryInitializerSliceViolation {
	void m(boolean cond, int a, int b, int d) {
		final int x = cond ? a : b, y = d; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}
// === end ===

// === case: multi_var_warns ===
class InputPreferVarMultiVarSliceViolation {
	void m() {
		final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final var map = new HashMap<Object, Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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

// === case: narrowing_annotated_type_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingAnnotatedTypeArgumentWarnsSliceViolation {
	@interface Ann {
	}

	void m() {
		final List<@Ann String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: narrowing_cast_warns ===
class InputPreferVarNarrowingCastWarnsSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	void m(Object source) {
		final Base item = (Derived) source; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item);
	}
}
// === end ===

// === case: narrowing_classpath_call_warns ===
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.nio.file.Files
// imports: java.nio.file.Path
class InputPreferVarNarrowingClasspathCallWarnsSliceViolation {
	void m(Path path) throws IOException {
		final Closeable in = Files.newInputStream(path); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(in);
	}
}
// === end ===

// === case: narrowing_declaration_annotation_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingDeclarationAnnotationWarnsSliceViolation {
	void m() {
		@Deprecated
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: narrowing_factory_final_warns ===
class InputPreferVarNarrowingFactoryFinalWarnsSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Derived makeDerived() {
		return new Derived();
	}

	void m() {
		final Base item = makeDerived(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item);
	}
}
// === end ===

// === case: narrowing_for_init_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingForInitWarnsSliceViolation {
	void m(boolean flag) {
		for (List<String> items = new ArrayList<>(); flag; flag = false) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(items);
	}
}
// === end ===

// === case: narrowing_identifier_with_a_written_subtype_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingIdentifierWithAWrittenSubtypeWarnsSliceViolation {
	void m(ArrayList<String> source) {
		final List<String> view = source; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(view);
	}
}
// === end ===

// === case: narrowing_multi_var_first_declarator_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingMultiVarFirstDeclaratorWarnsSliceViolation {
	void m() {
		final List<String> first = new ArrayList<>(), second = null; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(first);
		System.out.println(second);
	}
}
// === end ===

// === case: narrowing_new_without_uses_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarNarrowingNewWithoutUsesWarnsSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: narrowing_resource_factory_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarNarrowingResourceFactoryWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (Closeable reader = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(reader);
		}
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

// === case: nested_ternary_multi_var_warns ===
class InputPreferVarNestedTernaryMultiVarSliceViolation {
	void m(boolean cond, boolean flag, int a, int b, int c, int d) {
		final int x = cond ? a : flag ? b : c, y = d; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(x + y);
	}
}
// === end ===

// === case: non_generic_call_with_a_conditional_argument_of_agreeing_arms_converts ===
class InputPreferVarNonGenericCallWithAConditionalArgumentOfAgreeingArmsConvertsSliceViolation {
	void m(boolean flag, Object first, Object second) {
		final String text = String.valueOf(flag ? first : second); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: non_generic_call_with_a_conditional_argument_of_an_unproven_arm_warns ===
class InputPreferVarNonGenericCallWithAConditionalArgumentOfAnUnprovenArmWarnsSliceViolation {
	static <T> T make() {
		return null;
	}

	void m(boolean flag, Object first) {
		final String text = String.valueOf(flag ? first : make()); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: non_generic_call_with_a_conditional_argument_of_disagreeing_arms_warns ===
class InputPreferVarNonGenericCallWithAConditionalArgumentOfDisagreeingArmsWarnsSliceViolation {
	void m(boolean flag, char[] chars, String text) {
		final String joined = String.valueOf(flag ? chars : text); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(joined);
	}
}
// === end ===

// === case: non_generic_call_with_a_switch_argument_of_agreeing_arms_converts ===
class InputPreferVarNonGenericCallWithASwitchArgumentOfAgreeingArmsConvertsSliceViolation {
	void m(int kind, Object first, Object second) {
		final String text = String.valueOf(switch (kind) { case 1 -> first; default -> second; }); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}
// === end ===

// === case: non_generic_call_with_a_switch_argument_of_an_unproven_arm_warns ===
class InputPreferVarNonGenericCallWithASwitchArgumentOfAnUnprovenArmWarnsSliceViolation {
	static <T> T make() {
		return null;
	}

	void m(int kind, Object first) {
		final String text = String.valueOf(switch (kind) { case 1 -> first; default -> make(); }); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(text);
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

// === case: overload_selection_changes_from_an_enclosing_class_warns ===
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

// === case: overload_selection_changes_in_a_nested_anonymous_class_body_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputPreferVarOverloadSelectionNestedAnonymousSource {
	void run();

	default void take(ArrayList<String> values) {
		System.out.println(values);
	}

	default void take(List<String> values) {
		System.out.println(values);
	}
}

class InputPreferVarOverloadSelectionChangesInANestedAnonymousClassBodyWarnsSliceViolation {
	void m() {
		new InputPreferVarOverloadSelectionNestedAnonymousSource() {
			@Override
			public void run() {
				new Object() {
					void inner() {
						final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
						take(items);
					}
				}.inner();
			}
		}.run();
	}
}
// === end ===

// === case: overload_selection_changes_in_an_anonymous_class_body_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
abstract class InputPreferVarOverloadSelectionAnonymousSink {
	abstract void run();

	void take(ArrayList<String> values) {
		System.out.println(values);
	}

	void take(Object value) {
		System.out.println(value);
	}
}

class InputPreferVarOverloadSelectionChangesInAnAnonymousClassBodyWarnsSliceViolation {
	void m() {
		new InputPreferVarOverloadSelectionAnonymousSink() {
			@Override
			void run() {
				final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
				take(items);
			}
		}.run();
	}
}
// === end ===

// === case: overload_selection_changes_in_an_enum_constant_body_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputPreferVarOverloadSelectionEnumConstantSource {
	default void take(ArrayList<String> values) {
		System.out.println(values);
	}

	default void take(List<String> values) {
		System.out.println(values);
	}
}

enum InputPreferVarOverloadSelectionChangesInAnEnumConstantBodyWarnsSliceViolation implements InputPreferVarOverloadSelectionEnumConstantSource {
	FIRST {
		@Override
		void run() {
			final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			take(items);
		}
	};

	abstract void run();
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
		final Closeable[] value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_compact_canonical_constructor_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnACompactCanonicalConstructorWarnsSliceViolation {
	record Sink(ArrayList<String> values) {
		Sink {
			System.out.println(values);
		}

		Sink(List<String> values) {
			this(new ArrayList<>(values));
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_constructor_type_variable_bound_warns ===
class InputPreferVarOverloadSelectionChangesOnAConstructorTypeVariableBoundWarnsSliceViolation {
	static class Shape {
	}

	static class Circle extends Shape {
	}

	static class Sink {
		Sink(Shape value) {
		}

		<T extends Circle> Sink(T value) {
		}
	}

	static Circle create() {
		return new Circle();
	}

	void m() {
		final Shape value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(value));
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

		final List<String> value = flag ? new ArrayList<>() : new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new LocalSink(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_method_type_variable_bound_warns ===
class InputPreferVarOverloadSelectionChangesOnAMethodTypeVariableBoundWarnsSliceViolation {
	static class Shape {
	}

	static class Circle extends Shape {
	}

	static Circle create() {
		return new Circle();
	}

	static String take(Shape value) {
		return "Shape";
	}

	static <T extends Circle> String take(T value) {
		return "T";
	}

	void m() {
		final Shape value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_method_with_a_receiver_parameter_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAMethodWithAReceiverParameterWarnsSliceViolation {
	static class Taker {
		void m() {
			final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(take(items));
		}

		String take(List<String> values) {
			return "List";
		}

		String take(Taker this, ArrayList<String> values) {
			return "ArrayList";
		}
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
		final Object[] value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_qualified_constructor_name_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAQualifiedConstructorNameWarnsSliceViolation {
	static class Outer {
		static class Sink {
			Sink(ArrayList<String> values) {
			}

			Sink(List<String> values) {
			}
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Outer.Sink(items));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_qualified_super_constructor_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAQualifiedSuperConstructorCallWarnsSliceViolation {
	static class Outer {
		static class Base {
			Base(ArrayList<String> values) {
			}

			Base(List<String> values) {
			}
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink extends Outer.Base {
			Sink() {
				super(items);
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_changes_on_a_qualified_this_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAQualifiedThisCallWarnsSliceViolation {
	static class Outer {
		class Inner {
			void m() {
				final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
				Outer.this.take(items);
			}
		}

		void take(ArrayList<String> values) {
			System.out.println(values);
		}

		void take(List<String> values) {
			System.out.println(values);
		}
	}
}
// === end ===

// === case: overload_selection_changes_on_a_record_canonical_constructor_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnARecordCanonicalConstructorWarnsSliceViolation {
	record Sink(ArrayList<String> values) {
		Sink(List<String> values) {
			this(new ArrayList<>(values));
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_changes_on_a_super_constructor_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnASuperConstructorCallWarnsSliceViolation {
	static class Base {
		Base(ArrayList<String> values) {
		}

		Base(List<String> values) {
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink extends Base {
			Sink() {
				super(items);
			}
		}

		System.out.println(new Sink());
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		super.take(items);
	}
}
// === end ===

// === case: overload_selection_changes_on_a_this_constructor_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAThisConstructorCallWarnsSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink {
			Sink() {
				this(items);
			}

			Sink(ArrayList<String> values) {
			}

			Sink(List<String> values) {
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_changes_on_an_enclosing_type_qualified_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionChangesOnAnEnclosingTypeQualifiedCallWarnsSliceViolation {
	static class Outer {
		static class Inner {
			void m() {
				final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
				Outer.take(items);
			}
		}

		static void take(ArrayList<String> values) {
			System.out.println(values);
		}

		static void take(List<String> values) {
			System.out.println(values);
		}
	}
}
// === end ===

// === case: overload_selection_changes_on_an_interface_super_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
interface InputPreferVarOverloadSelectionInterfaceSuperSource {
	default void take(ArrayList<String> values) {
		System.out.println(values);
	}

	default void take(List<String> values) {
		System.out.println(values);
	}
}

class InputPreferVarOverloadSelectionChangesOnAnInterfaceSuperCallWarnsSliceViolation implements InputPreferVarOverloadSelectionInterfaceSuperSource {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		InputPreferVarOverloadSelectionInterfaceSuperSource.super.take(items);
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final java.util.List<String> items = new java.util.ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_climbs_a_receiver_chain_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionClimbsAReceiverChainWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items.subList(0, 1));
	}
}
// === end ===

// === case: overload_selection_climbs_a_wrapper_used_as_receiver_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionClimbsAWrapperUsedAsReceiverWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(same(items).subList(0, 1));
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(1, items);
	}
}
// === end ===

// === case: overload_selection_does_not_climb_a_lambda_body ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.function.Supplier
class InputPreferVarOverloadSelectionDoesNotClimbALambdaBodySliceViolation {
	static void supply(Supplier<List<String>> source) {
		source.get();
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		supply(() -> items);
	}
}
// === end ===

// === case: overload_selection_does_not_climb_a_switch_expression_arm ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionDoesNotClimbASwitchExpressionArmSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(int key) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(switch (key) {
			case 1 -> items;
			default -> null;
		});
	}
}
// === end ===

// === case: overload_selection_ignores_a_classpath_super_constructor ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAClasspathSuperConstructorSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink extends ArrayList<String> {
			Sink() {
				super(items);
			}
		}

		System.out.println(new Sink());
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(other.getOrDefault("k", "v") + items.size());
	}
}
// === end ===

// === case: overload_selection_ignores_a_foreign_receiver_after_a_ternary ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAForeignReceiverAfterATernarySliceViolation {
	static class Sink {
		void take(ArrayList<String> values) {
		}

		void take(List<String> values) {
		}
	}

	final Sink sink = new Sink();

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		sink.take(flag ? items : null);
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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

// === case: overload_selection_ignores_a_record_canonical_constructor_of_a_different_arity ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresARecordCanonicalConstructorOfADifferentAritySliceViolation {
	record Sink(ArrayList<String> values, int size) {
		Sink(List<String> values) {
			this(new ArrayList<>(values), values.size());
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_ignores_a_same_file_homonym_of_a_foreign_receiver_call ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Map
class InputPreferVarOverloadSelectionIgnoresASameFileHomonymOfAForeignReceiverCallSliceViolation {
	static ArrayList<String> get(String key) {
		return new ArrayList<>();
	}

	static void take(ArrayList<String> values) {
		System.out.println(values);
	}

	static void take(List<String> values) {
		System.out.println(values);
	}

	void m(Map<String, List<String>> other) {
		final List<String> values = other.get("k"); // violation: Local variable must use 'var' instead of an explicit type.
		take(values);
		final List<String> own = get("k"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(own);
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items);
	}
}
// === end ===

// === case: overload_selection_ignores_a_ternary_condition ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresATernaryConditionSliceViolation {
	static boolean isReady(List<String> values) {
		return values.isEmpty();
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(List<String> fallback) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(isReady(items) ? fallback : fallback);
	}
}
// === end ===

// === case: overload_selection_ignores_a_this_constructor_of_a_different_arity ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionIgnoresAThisConstructorOfADifferentAritySliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink {
			Sink() {
				this(items);
			}

			Sink(List<String> values) {
			}

			Sink(ArrayList<String> values, int limit) {
			}
		}

		System.out.println(new Sink());
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(items);
	}
}
// === end ===

// === case: overload_selection_on_a_type_variable_parameter_warns ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionOnATypeVariableParameterWarnsSliceViolation {
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_on_an_array_accepting_both_warns ===
// imports: java.io.Closeable
class InputPreferVarOverloadSelectionOnAnArrayAcceptingBothWarnsSliceViolation {
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
		final Closeable[] value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_on_an_unbounded_method_type_variable_warns ===
class InputPreferVarOverloadSelectionOnAnUnboundedMethodTypeVariableWarnsSliceViolation {
	static class Shape {
	}

	static class Circle extends Shape {
	}

	static Circle create() {
		return new Circle();
	}

	static String take(Shape value) {
		return "Shape";
	}

	static <T> String take(T value) {
		return "T";
	}

	void m() {
		final Shape value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: overload_selection_survives_cyclic_inheritance_warns ===
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

// === case: overload_selection_through_a_cast_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughACastWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take((List<String>) items);
	}
}
// === end ===

// === case: overload_selection_through_a_covariant_receiver_chain_warns ===
class InputPreferVarOverloadSelectionThroughACovariantReceiverChainWarnsSliceViolation {
	static class Base {
		Base self() {
			return this;
		}
	}

	static class Derived extends Base {
		@Override
		Derived self() {
			return this;
		}
	}

	static Derived create() {
		return new Derived();
	}

	static String pass(Base value) {
		return "Base";
	}

	static String pass(Derived value) {
		return "Derived";
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(pass(item.self()));
	}
}
// === end ===

// === case: overload_selection_through_a_foreign_receiver_wrapper_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAForeignReceiverWrapperWarnsSliceViolation {
	static class Holder {
		<T> T same(T value) {
			return value;
		}
	}

	static final Holder HOLDER = new Holder();

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(HOLDER.same(items));
	}
}
// === end ===

// === case: overload_selection_through_a_generic_identity_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAGenericIdentityCallWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(same(items));
	}
}
// === end ===

// === case: overload_selection_through_a_nested_ternary_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughANestedTernaryWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean first, boolean second) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(first ? null : second ? null : items);
	}
}
// === end ===

// === case: overload_selection_through_a_non_generic_wrapper_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughANonGenericWrapperWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	static List<String> wrap(List<String> values) {
		return values;
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(wrap(items));
	}
}
// === end ===

// === case: overload_selection_through_a_parenthesized_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAParenthesizedArgumentWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take((items));
	}
}
// === end ===

// === case: overload_selection_through_a_parenthesized_nested_ternary_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAParenthesizedNestedTernaryWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean first, boolean second) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(first ? (second ? items : null) : null);
	}
}
// === end ===

// === case: overload_selection_through_a_parenthesized_ternary_arm_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAParenthesizedTernaryArmWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? (items) : null);
	}
}
// === end ===

// === case: overload_selection_through_a_parenthesized_ternary_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAParenthesizedTernaryWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take((flag ? items : null));
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_false_arm_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryFalseArmWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? null : items);
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_in_a_lambda_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryInALambdaWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Runnable task = () -> take(flag ? items : null);
		task.run();
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_in_a_wrapper_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryInAWrapperWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(same(flag ? items : null));
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_on_a_super_qualified_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryOnASuperQualifiedCallWarnsSliceViolation {
	static class Base {
		void take(ArrayList<String> values) {
		}

		void take(List<String> values) {
		}
	}

	static class Child extends Base {
		void m(boolean flag) {
			final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			super.take(flag ? items : null);
		}
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_on_a_this_constructor_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryOnAThisConstructorCallWarnsSliceViolation {
	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink {
			Sink() {
				this(flag ? items : null);
			}

			Sink(ArrayList<String> values) {
			}

			Sink(List<String> values) {
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_true_arm_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryTrueArmWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? items : null);
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_with_a_declared_type_arm_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryWithADeclaredTypeArmWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag, List<String> other) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? items : other);
	}
}
// === end ===

// === case: overload_selection_through_a_ternary_with_a_parenthesized_condition_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughATernaryWithAParenthesizedConditionWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take((flag) ? items : null);
	}
}
// === end ===

// === case: overload_selection_through_a_wrapper_at_a_later_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAWrapperAtALaterArgumentWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(int limit, ArrayList<String> values) {
	}

	static void take(int limit, List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(1, same(items));
	}
}
// === end ===

// === case: overload_selection_through_a_wrapper_in_a_ternary_arm_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAWrapperInATernaryArmWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? same(items) : null);
	}
}
// === end ===

// === case: overload_selection_through_a_wrapper_on_a_super_constructor_call_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughAWrapperOnASuperConstructorCallWarnsSliceViolation {
	static class Base {
		Base(ArrayList<String> values) {
		}

		Base(List<String> values) {
		}
	}

	static <T> T same(T value) {
		return value;
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink extends Base {
			Sink() {
				super(same(items));
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_through_require_non_null_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
// imports: java.util.Objects
class InputPreferVarOverloadSelectionThroughRequireNonNullWarnsSliceViolation {
	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(Objects.requireNonNull(items));
	}
}
// === end ===

// === case: overload_selection_through_two_wrappers_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionThroughTwoWrappersWarnsSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(ArrayList<String> values) {
	}

	static void take(List<String> values) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(same(same(items)));
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_unchanged_on_a_record_canonical_constructor ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedOnARecordCanonicalConstructorSliceViolation {
	record Sink(List<String> values) {
		Sink(Collection<String> values) {
			this(new ArrayList<>(values));
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(new Sink(items));
	}
}
// === end ===

// === case: overload_selection_unchanged_on_a_super_constructor_call ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedOnASuperConstructorCallSliceViolation {
	static class Base {
		Base(List<String> values) {
		}

		Base(String value) {
		}
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink extends Base {
			Sink() {
				super(items);
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_unchanged_on_a_this_constructor_call ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedOnAThisConstructorCallSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		class Sink {
			Sink() {
				this(items);
			}

			Sink(List<String> values) {
			}

			Sink(String value) {
			}
		}

		System.out.println(new Sink());
	}
}
// === end ===

// === case: overload_selection_unchanged_through_a_ternary ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedThroughATernarySliceViolation {
	static void take(List<String> values) {
	}

	static void take(String value) {
	}

	void m(boolean flag) {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(flag ? items : null);
	}
}
// === end ===

// === case: overload_selection_unchanged_through_a_wrapper ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarOverloadSelectionUnchangedThroughAWrapperSliceViolation {
	static <T> T same(T value) {
		return value;
	}

	static void take(List<String> values) {
	}

	static void take(String value) {
	}

	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		take(same(items));
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
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final var s = (cast("hello")); // violation (warning): Consider using an explicit type ('var' with 'cast' loses generic type information).
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

// === case: pattern_variable_after_a_negated_if_shadows_a_field_warns ===
// imports: java.io.IOException
class InputPreferVarPatternVariableAfterANegatedIfShadowsAFieldWarnsSliceViolation {
	Exception error;

	void m(Object cause) {
		if (!(cause instanceof IOException error))
			return;
		final Exception shown = error; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shown);
	}
}
// === end ===

// === case: pattern_variable_in_a_case_shadows_a_field_warns ===
// imports: java.io.IOException
class InputPreferVarPatternVariableInACaseShadowsAFieldWarnsSliceViolation {
	Exception error;

	void m(Object cause) {
		switch (cause) {
			case IOException error -> {
				final Exception shown = error; // violation (warning): Local variable should use 'var' instead of an explicit type.
				System.out.println(shown);
			}
			default -> System.out.println(cause);
		}
	}
}
// === end ===

// === case: pattern_variable_in_a_conditional_arm_shadows_a_field_warns ===
// imports: java.io.IOException
// imports: java.util.function.Supplier
class InputPreferVarPatternVariableInAConditionalArmShadowsAFieldWarnsSliceViolation {
	static String describe(Supplier<String> supplier) {
		return supplier.get();
	}

	Exception error;

	void m(Object cause) {
		final var text = cause instanceof IOException error ? describe(() -> {
			final Exception shown = error; // violation (warning): Local variable should use 'var' instead of an explicit type.
			return String.valueOf(shown);
		}) : "none";
		System.out.println(text);
	}
}
// === end ===

// === case: pattern_variable_in_a_negated_if_else_shadows_a_field_warns ===
// imports: java.io.IOException
class InputPreferVarPatternVariableInANegatedIfElseShadowsAFieldWarnsSliceViolation {
	Exception error;

	void m(Object cause) {
		if (!(cause instanceof IOException error))
			System.out.println(cause);
		else {
			final Exception shown = error; // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(shown);
		}
	}
}
// === end ===

// === case: pattern_variable_in_an_if_then_converts ===
// imports: java.io.IOException
class InputPreferVarPatternVariableInAnIfThenConvertsSliceViolation {
	Exception error;

	void m(Object cause) {
		if (cause instanceof IOException error) {
			final IOException shown = error; // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(shown);
		}
	}
}
// === end ===

// === case: pattern_variable_in_an_or_right_operand_shadows_a_field_warns ===
// imports: java.io.IOException
// imports: java.util.function.BooleanSupplier
class InputPreferVarPatternVariableInAnOrRightOperandShadowsAFieldWarnsSliceViolation {
	static boolean check(BooleanSupplier supplier) {
		return supplier.getAsBoolean();
	}

	Exception error;

	void m(Object cause) {
		if (!(cause instanceof IOException error) || check(() -> {
			final Exception shown = error; // violation (warning): Local variable should use 'var' instead of an explicit type.
			return shown.getMessage() == null;
		}))
			return;
		System.out.println(cause);
	}
}
// === end ===

// === case: pattern_variable_of_another_name_leaves_the_field_converts ===
// imports: java.io.IOException
class InputPreferVarPatternVariableOfAnotherNameLeavesTheFieldConvertsSliceViolation {
	Exception error;

	void m(Object cause) {
		if (cause instanceof IOException failure)
			System.out.println(failure);
		final Exception shown = error; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(shown);
	}
}
// === end ===

// === case: primitive_array_all_null_conditional_warns ===
class InputPreferVarPrimitiveArrayAllNullConditionalWarnsSliceViolation {
	void m(boolean flag, boolean other) {
		final int[] values = flag ? null : null; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int[] nested = flag ? null : other ? null : null; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length + nested.length);
	}
}
// === end ===

// === case: primitive_array_all_null_switch_warns ===
class InputPreferVarPrimitiveArrayAllNullSwitchWarnsSliceViolation {
	void m(int kind) {
		final int[] values = switch (kind) { case 1 -> null; default -> null; }; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_argument_agreeing_arms_warns ===
// imports: java.util.Objects
class InputPreferVarPrimitiveArrayConditionalArgumentAgreeingArmsWarnsSliceViolation {
	void m(boolean flag, int[] first, int[] second) {
		final int[] values = Objects.requireNonNull(flag ? first : second); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_argument_null_arm_warns ===
// imports: java.util.Objects
class InputPreferVarPrimitiveArrayConditionalArgumentNullArmWarnsSliceViolation {
	void m(boolean flag, int[] first) {
		final int[] values = Objects.requireNonNull(flag ? first : null); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_argument_unproven_arm_warns ===
// imports: java.util.Objects
class InputPreferVarPrimitiveArrayConditionalArgumentUnprovenArmWarnsSliceViolation {
	static <T> T make() {
		return null;
	}

	void m(boolean flag) {
		final int[] values = Objects.requireNonNull(flag ? make() : null); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_arm_unproven_call_warns ===
class InputPreferVarPrimitiveArrayConditionalArmUnprovenCallWarnsSliceViolation {
	static <T> T make() {
		return null;
	}

	void m(boolean flag) {
		final int[] values = flag ? new int[0] : make(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_name_arms_converts ===
class InputPreferVarPrimitiveArrayConditionalNameArmsConvertsSliceViolation {
	void m(boolean flag, int[] other) {
		final int[] values = flag ? new int[0] : other; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_conditional_one_null_arm_warns ===
class InputPreferVarPrimitiveArrayConditionalOneNullArmWarnsSliceViolation {
	void m(boolean flag, int[] other) {
		final int[] values = flag ? null : other; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_field_of_an_unresolvable_receiver_warns ===
class InputPreferVarPrimitiveArrayFieldOfAnUnresolvableReceiverWarnsSliceViolation {
	void m(Holder holder) {
		final int[] values = holder.values; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_new_converts ===
// multi-fix-expected
class InputPreferVarPrimitiveArrayNewConvertsSliceViolation {
	void m() {
		final int[] values = new int[3]; // violation: Local variable must use 'var' instead of an explicit type.
		final int[][] grid = new int[3][3]; // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(values.length + grid.length);
	}
}
// === end ===

// === case: primitive_array_proven_initializers_convert ===
// multi-fix-expected
class InputPreferVarPrimitiveArrayProvenInitializersConvertSliceViolation {
	void m(Object source, int[] typed, int[][] rows) {
		final int[] cast = (int[]) source; // violation: Local variable must use 'var' instead of an explicit type.
		final int[] name = typed; // violation: Local variable must use 'var' instead of an explicit type.
		final int[] element = rows[0]; // violation: Local variable must use 'var' instead of an explicit type.
		final int[] call = typed.clone(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(cast.length + name.length + element.length + call.length);
	}
}
// === end ===

// === case: primitive_array_same_file_generic_call_on_a_type_variable_receiver_warns ===
class InputPreferVarPrimitiveArraySameFileGenericCallOnATypeVariableReceiverWarnsSliceViolation {
	static class Bundle {
		<T> T get(String key) {
			return null;
		}
	}

	<B extends Bundle> void m(B bundle) {
		final int[] values = bundle.get("k"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_same_file_generic_call_on_a_variable_warns ===
class InputPreferVarPrimitiveArraySameFileGenericCallOnAVariableWarnsSliceViolation {
	static class Cache {
		<T> T get(String key) {
			return null;
		}
	}

	void m(Cache cache) {
		final int[] ids = cache.get("ids"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.length);
	}
}
// === end ===

// === case: primitive_array_same_file_generic_call_without_arguments_warns ===
class InputPreferVarPrimitiveArraySameFileGenericCallWithoutArgumentsWarnsSliceViolation {
	static class Helper {
		<T> T make() {
			return null;
		}
	}

	void m(Helper helper) {
		final int[] values = helper.make(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_switch_argument_agreeing_arms_warns ===
// imports: java.util.Objects
class InputPreferVarPrimitiveArraySwitchArgumentAgreeingArmsWarnsSliceViolation {
	void m(int kind, int[] first, int[] second) {
		final int[] values = Objects.requireNonNull(switch (kind) { case 1 -> first; default -> second; }); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_switch_argument_unproven_arm_warns ===
// imports: java.util.Objects
class InputPreferVarPrimitiveArraySwitchArgumentUnprovenArmWarnsSliceViolation {
	static <T> T make() {
		return null;
	}

	void m(int kind, int[] first) {
		final int[] values = Objects.requireNonNull(switch (kind) { case 1 -> first; default -> make(); }); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_array_switch_of_a_null_arm_and_a_throw_arm_warns ===
class InputPreferVarPrimitiveArraySwitchOfANullArmAndAThrowArmWarnsSliceViolation {
	void m(int kind) {
		final int[] values = switch (kind) { case 1 -> null; default -> throw new IllegalStateException(); }; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.length);
	}
}
// === end ===

// === case: primitive_with_non_literal_expression ===
// multi-fix-expected
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
		final var list = new java.util.ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
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
		final List<String> names = new java.util.ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: qualified_new_resolvable_diamond_exact ===
class InputPreferVarQualifiedNewResolvableDiamondExactSliceViolation {
	void m() {
		final java.util.ArrayList<String> names = new java.util.ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: qualified_new_whose_same_file_class_remaps_its_type_parameters_warns ===
class InputPreferVarQualifiedNewWhoseSameFileClassRemapsItsTypeParametersWarnsSliceViolation {
	static class Pairing<A, B> {
	}

	static class Remap {
		static class Swapped<B, A> extends Pairing<A, B> {
		}
	}

	void m() {
		final Pairing<String, Integer> remapped = new Remap.Swapped<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(remapped);
	}
}
// === end ===

// === case: qualified_new_whose_same_file_class_takes_a_different_arity_warns ===
class InputPreferVarQualifiedNewWhoseSameFileClassTakesADifferentArityWarnsSliceViolation {
	static class Holder {
		static class Slot<A, B> implements InputPreferVarQualifiedNewWhoseSameFileClassTakesADifferentArityWarnsSliceViolation.Slot<String> {
		}
	}

	interface Slot<T> {
	}

	void m() {
		final Slot<String> collided = new Holder.Slot<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Slot<String> offFile = new Absent.Slot<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(collided);
		System.out.println(offFile);
	}
}
// === end ===

// === case: qualified_new_whose_simple_name_collides_with_the_declared_type_warns ===
class InputPreferVarQualifiedNewWhoseSimpleNameCollidesWithTheDeclaredTypeWarnsSliceViolation {
	static class Api {
		static class Cache<K, V> {
		}
	}

	static class Impl {
		static class Cache<V> extends Api.Cache<String, V> {
		}
	}

	void m() {
		final Api.Cache<String, Integer> cache = new Impl.Cache<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(cache);
	}
}
// === end ===

// === case: qualified_object_type_arg ===
// imports: java.util.ArrayList
class InputPreferVarDiamondQualifiedObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<java.lang.Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: qualified_static_call_generic_var_warns ===
class InputPreferVarQualifiedStaticCallGenericVarSliceViolation {
	void m() {
		final var values = java.util.Collections.emptyList(); // violation (warning): Consider using an explicit type ('var' with 'emptyList' loses generic type information).
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
		try (@Marker java.lang.AutoCloseable wide = new Outer.Res<String>()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(wide);
		}
		try (@Marker java.lang.AutoCloseable typed = this.<java.lang.AutoCloseable>acquire()) { // violation (warning): Consider using an explicit type instead of type arguments on 'acquire'.
			System.out.println(typed);
		}
		for (final java.lang.String item : holder.items) // violation (warning): For-each variable should use 'var' instead of an explicit type.
			System.out.println(item);
		final java.util.List<String> first = java.util.List.of(x), second = java.util.List.of(y); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final java.util.List<String> empty = java.util.Collections.<String>emptyList(); // violation (warning): Consider using an explicit type instead of type arguments on 'emptyList'.
		System.out.println(first + "" + second + empty);
	}
}
// === end ===

// === case: raw_declared_type_with_a_diamond_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarRawDeclaredTypeWithADiamondWarnsSliceViolation {
	void m() {
		final List rawDiamond = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(rawDiamond);
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		if (flag)
			items = new ArrayList<>();
		System.out.println(items);
	}
}
// === end ===

// === case: receiver_and_argument_warn_once ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverAndArgumentWarnOnceSliceViolation {
	static void take(ArrayList<Integer> values) {
	}

	static void take(Collection<Integer> values) {
	}

	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.remove(1));
		take(ids);
	}
}
// === end ===

// === case: receiver_call_beside_a_same_named_field_warns ===
class InputPreferVarReceiverCallBesideASameNamedFieldWarnsSliceViolation {
	static class Base {
		String label() {
			return "base";
		}
	}

	static class Derived extends Base {
		String label = "derived";
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.label());
	}
}
// === end ===

// === case: receiver_call_on_a_field_read_through_it_warns ===
class InputPreferVarReceiverCallOnAFieldReadThroughItWarnsSliceViolation {
	static class Base {
		Base next;

		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.next.use("x"));
	}
}
// === end ===

// === case: receiver_cast_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverCastWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(((Collection<Integer>) ids).remove(1));
	}
}
// === end ===

// === case: receiver_chained_call_on_a_covariant_override_warns ===
class InputPreferVarReceiverChainedCallOnACovariantOverrideWarnsSliceViolation {
	static class Base {
		Base self() {
			return this;
		}

		String take(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		@Override
		Derived self() {
			return this;
		}

		String take(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.self().take("x"));
	}
}
// === end ===

// === case: receiver_chained_call_through_a_wrapper_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.Optional
class InputPreferVarReceiverChainedCallThroughAWrapperWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(Optional.of(ids).get().remove(1));
	}
}
// === end ===

// === case: receiver_chained_call_without_a_new_overload_warns ===
class InputPreferVarReceiverChainedCallWithoutANewOverloadWarnsSliceViolation {
	static class Base {
		Base self() {
			return this;
		}

		String use(Object value) {
			return "Base";
		}
	}

	static class Derived extends Base {
		@Override
		Derived self() {
			return this;
		}

		@Override
		String use(Object value) {
			return "Derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.self().use("x"));
	}
}
// === end ===

// === case: receiver_classpath_class_new_overload_warns ===
// imports: java.sql.Timestamp
// imports: java.util.Date
class InputPreferVarReceiverClasspathClassNewOverloadWarnsSliceViolation {
	static Timestamp create() {
		return new Timestamp(0);
	}

	void m(Timestamp other) {
		final Date stamp = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(stamp.compareTo(other));
	}
}
// === end ===

// === case: receiver_classpath_class_override_warns ===
// imports: java.sql.Timestamp
// imports: java.util.Date
class InputPreferVarReceiverClasspathClassOverrideWarnsSliceViolation {
	static Timestamp create() {
		return new Timestamp(0);
	}

	void m() {
		final Date stamp = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(stamp.getTime());
	}
}
// === end ===

// === case: receiver_collection_add_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverCollectionAddWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.add(1));
	}
}
// === end ===

// === case: receiver_collection_remove_int_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverCollectionRemoveIntWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.remove(1));
	}
}
// === end ===

// === case: receiver_declared_type_with_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverDeclaredTypeWithAnUnresolvableSupertypeWarnsSliceViolation {
	static class Base extends MissingBase {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_default_method_override_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverDefaultMethodOverrideWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.removeIf(id -> id > 1));
	}
}
// === end ===

// === case: receiver_deque_remove_int_warns ===
// imports: java.util.Deque
// imports: java.util.LinkedList
class InputPreferVarReceiverDequeRemoveIntWarnsSliceViolation {
	void m() {
		final Deque<Integer> pending = new LinkedList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(pending.remove(1));
	}
}
// === end ===

// === case: receiver_deque_remove_without_arguments_warns ===
// imports: java.util.Deque
// imports: java.util.LinkedList
class InputPreferVarReceiverDequeRemoveWithoutArgumentsWarnsSliceViolation {
	void m() {
		final Deque<Integer> pending = new LinkedList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(pending.remove());
	}
}
// === end ===

// === case: receiver_every_chained_call_counts_warns ===
class InputPreferVarReceiverEveryChainedCallCountsWarnsSliceViolation {
	static class Base {
		Base self() {
			return this;
		}

		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.self().use("x"));
	}
}
// === end ===

// === case: receiver_field_hiding_on_write_warns ===
class InputPreferVarReceiverFieldHidingOnWriteWarnsSliceViolation {
	static class Base {
		String name = "base";
	}

	static class Derived extends Base {
		String name = "derived";
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		item.name = "x";
	}
}
// === end ===

// === case: receiver_field_hiding_then_call_warns ===
class InputPreferVarReceiverFieldHidingThenCallWarnsSliceViolation {
	static class Base {
		String name = "base";
	}

	static class Derived extends Base {
		String name = "derived";
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.name.length());
	}
}
// === end ===

// === case: receiver_field_hiding_through_a_chained_call_warns ===
class InputPreferVarReceiverFieldHidingThroughAChainedCallWarnsSliceViolation {
	static class Base {
		String name = "base";

		Base self() {
			return this;
		}
	}

	static class Derived extends Base {
		String name = "derived";

		@Override
		Derived self() {
			return this;
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.self().name);
	}
}
// === end ===

// === case: receiver_field_hiding_warns ===
class InputPreferVarReceiverFieldHidingWarnsSliceViolation {
	static class Base {
		String name = "base";
	}

	static class Derived extends Base {
		String name = "derived";
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.name);
	}
}
// === end ===

// === case: receiver_field_not_hidden_warns ===
class InputPreferVarReceiverFieldNotHiddenWarnsSliceViolation {
	static class Base {
		String name = "base";
	}

	static class Derived extends Base {
		String label = "derived";
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.name);
	}
}
// === end ===

// === case: receiver_for_init_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverForInitWarnsSliceViolation {
	void m() {
		for (Collection<Integer> ids = new ArrayList<>(); ids.remove(1); ) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(ids);
	}
}
// === end ===

// === case: receiver_in_a_lambda_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverInALambdaWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Runnable task = () -> ids.remove(1);
		task.run();
	}
}
// === end ===

// === case: receiver_interface_constant_hiding_warns ===
class InputPreferVarReceiverInterfaceConstantHidingWarnsSliceViolation {
	interface Shape {
		int SIZE = 1;
	}

	static class Circle implements Shape {
		static final int SIZE = 2;
	}

	static Circle create() {
		return new Circle();
	}

	void m() {
		final Shape shape = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shape.SIZE);
	}
}
// === end ===

// === case: receiver_list_remove_int_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverListRemoveIntWarnsSliceViolation {
	void m() {
		final List<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final IntFunction<Object> remover = ((Collection<Integer>) ids)::remove;
		System.out.println(remover.apply(1));
	}
}
// === end ===

// === case: receiver_method_reference_class_type_variable_overload_warns ===
// imports: java.util.function.Function
class InputPreferVarReceiverMethodReferenceClassTypeVariableOverloadWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived<T extends CharSequence> extends Base {
		String use(T value) {
			return "T";
		}
	}

	static Derived<String> create() {
		return new Derived<>();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Function<String, String> use = item::use;
		System.out.println(use.apply("x"));
	}
}
// === end ===

// === case: receiver_method_reference_new_overload_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.function.IntFunction
class InputPreferVarReceiverMethodReferenceNewOverloadWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final IntFunction<Object> remover = ids::remove;
		System.out.println(remover.apply(1));
	}
}
// === end ===

// === case: receiver_method_reference_on_a_chained_call_warns ===
// imports: java.util.function.Function
class InputPreferVarReceiverMethodReferenceOnAChainedCallWarnsSliceViolation {
	static class Base {
		Base self() {
			return this;
		}

		String take(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		@Override
		Derived self() {
			return this;
		}

		String take(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Function<String, String> take = item.self()::take;
		System.out.println(take.apply("x"));
	}
}
// === end ===

// === case: receiver_method_reference_parenthesized_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.function.IntFunction
class InputPreferVarReceiverMethodReferenceParenthesizedWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final IntFunction<Object> remover = (ids)::remove;
		System.out.println(remover.apply(1));
	}
}
// === end ===

// === case: receiver_method_reference_parenthesized_without_overloads_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.function.Supplier
class InputPreferVarReceiverMethodReferenceParenthesizedWithoutOverloadsWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Supplier<Integer> size = (ids)::size;
		System.out.println(size.get());
	}
}
// === end ===

// === case: receiver_method_reference_with_type_arguments_warns ===
// imports: java.util.function.Function
class InputPreferVarReceiverMethodReferenceWithTypeArgumentsWarnsSliceViolation {
	static class Base {
		String accept(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		<T> String accept(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Function<String, String> accept = item::<String>accept;
		System.out.println(accept.apply("x"));
	}
}
// === end ===

// === case: receiver_method_reference_without_overloads_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
// imports: java.util.function.Supplier
class InputPreferVarReceiverMethodReferenceWithoutOverloadsWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		final Supplier<Integer> size = ids::size;
		System.out.println(size.get());
	}
}
// === end ===

// === case: receiver_method_reference_zero_parameter_overload_of_a_generic_warns ===
// imports: java.util.function.Consumer
// imports: java.util.function.Supplier
class InputPreferVarReceiverMethodReferenceZeroParameterOverloadOfAGenericWarnsSliceViolation {
	static class Box<T> {
		String take(T value) {
			return "T";
		}
	}

	static class Sub extends Box<String> {
		String take() {
			return "none";
		}
	}

	static Sub create() {
		return new Sub();
	}

	static void run(Consumer<String> action) {
		action.accept("x");
	}

	static void run(Supplier<String> action) {
		System.out.println(action.get());
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		run(box::take);
	}
}
// === end ===

// === case: receiver_object_methods_on_an_interface_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverObjectMethodsOnAnInterfaceWarnsSliceViolation {
	void m() {
		final List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(items.toString() + items.getClass());
	}
}
// === end ===

// === case: receiver_parenthesized_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverParenthesizedWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println((ids).remove(1));
	}
}
// === end ===

// === case: receiver_same_file_array_override_of_a_type_variable_array_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileArrayOverrideOfATypeVariableArrayThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T[] values) {
			return "Box";
		}
	}

	static class Labels extends MissingBox {
		String put(String[] values) {
			return "Labels";
		}
	}

	static Labels create() {
		return new Labels();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(new String[0]));
	}
}
// === end ===

// === case: receiver_same_file_array_override_of_a_type_variable_warns ===
class InputPreferVarReceiverSameFileArrayOverrideOfATypeVariableWarnsSliceViolation {
	static class Box<T> {
		String put(T[] values) {
			return "T[]";
		}
	}

	static class StringBox extends Box<String> {
		@Override
		String put(String[] values) {
			return "String[]";
		}
	}

	static StringBox create() {
		return new StringBox();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(new String[0]));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_of_a_classpath_concrete_argument_warns ===
// imports: java.util.ArrayList
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadOfAClasspathConcreteArgumentWarnsSliceViolation {
	static class Numbers<E extends Number> extends Values {
		public boolean add(E value) {
			return true;
		}
	}

	static class Values extends ArrayList<Object> {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.add(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_of_a_concrete_supertype_argument_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadOfAConcreteSupertypeArgumentWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers<E extends Number> extends Values {
		String put(E value) {
			return "Numbers";
		}
	}

	static class Values extends Box<Object> {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_of_a_qualified_supertype_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadOfAQualifiedSupertypeWarnsSliceViolation {
	static class Numbers<E extends Number> extends Values {
		String put(E value) {
			return "Numbers";
		}
	}

	static class Outer {
		static class Box<T> {
			String put(T value) {
				return "Box";
			}
		}
	}

	static class Values extends Outer.Box<Object> {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_of_a_raw_classpath_supertype_warns ===
// imports: java.util.ArrayList
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadOfARawClasspathSupertypeWarnsSliceViolation {
	static class Numbers<E extends Number> extends Values {
		public boolean add(E value) {
			return true;
		}
	}

	static class Values extends ArrayList {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.add(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_of_a_raw_supertype_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadOfARawSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers<E extends Number> extends Values {
		String put(E value) {
			return "Numbers";
		}
	}

	static class Values extends Box {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_through_a_generic_middle_class_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadThroughAGenericMiddleClassWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Middle<U> extends Box<U> {
	}

	static class Numbers<E extends Number> extends Values {
		String put(E value) {
			return "Numbers";
		}
	}

	static class Values extends Middle<Object> {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_overload_through_an_implements_clause_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverloadThroughAnImplementsClauseWarnsSliceViolation {
	interface Sink<T> {
		String put(T value);
	}

	abstract static class Numbers<E extends Number> extends Values {
		String put(E value) {
			return "Numbers";
		}
	}

	abstract static class Values implements Runnable, Sink<Object> {
		@Override
		public void run() {
		}
	}

	static Numbers<Integer> create() {
		return null;
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_override_of_a_classpath_generic_through_a_middle_class_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverSameFileBoundedTypeVariableOverrideOfAClasspathGenericThroughAMiddleClassWarnsSliceViolation {
	static class Middle<U> extends ArrayList<U> {
	}

	static class Numbers<E extends Number> extends Middle<E> {
		@Override
		public boolean add(E value) {
			return super.add(value);
		}
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final List<Integer> numbers = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(numbers.add(5));
	}
}
// === end ===

// === case: receiver_same_file_bounded_type_variable_override_warns ===
class InputPreferVarReceiverSameFileBoundedTypeVariableOverrideWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers<E extends Number> extends Box<E> {
		@Override
		String put(E value) {
			return "Numbers";
		}
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(5));
	}
}
// === end ===

// === case: receiver_same_file_class_type_variable_array_overload_warns ===
class InputPreferVarReceiverSameFileClassTypeVariableArrayOverloadWarnsSliceViolation {
	static class Base {
		String use(Object[] values) {
			return "Object[]";
		}
	}

	static class Derived<T extends CharSequence> extends Base {
		String use(T[] values) {
			return "T[]";
		}
	}

	static Derived<String> create() {
		return new Derived<>();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use(new String[0]));
	}
}
// === end ===

// === case: receiver_same_file_class_type_variable_overload_of_a_classpath_method_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverSameFileClassTypeVariableOverloadOfAClasspathMethodWarnsSliceViolation {
	static class Tags<T extends CharSequence> extends ArrayList<String> {
		public boolean remove(T value) {
			return false;
		}
	}

	static Tags<String> create() {
		return new Tags<>();
	}

	void m() {
		final Collection<String> tags = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(tags.remove("x"));
	}
}
// === end ===

// === case: receiver_same_file_class_type_variable_overload_warns ===
class InputPreferVarReceiverSameFileClassTypeVariableOverloadWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived<T extends CharSequence> extends Base {
		String use(T value) {
			return "T";
		}
	}

	static Derived<String> create() {
		return new Derived<>();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_class_type_variable_override_of_a_classpath_generic_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverSameFileClassTypeVariableOverrideOfAClasspathGenericWarnsSliceViolation {
	static class Names<T> extends ArrayList<T> {
		@Override
		public boolean add(T value) {
			return super.add(value);
		}
	}

	static Names<String> create() {
		return new Names<>();
	}

	void m() {
		final List<String> names = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names.add("x"));
	}
}
// === end ===

// === case: receiver_same_file_class_type_variable_override_warns ===
class InputPreferVarReceiverSameFileClassTypeVariableOverrideWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class SubBox<T> extends Box<T> {
		@Override
		String put(T value) {
			return "SubBox";
		}
	}

	static SubBox<String> create() {
		return new SubBox<>();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_classpath_implementation_of_a_same_file_generic_warns ===
// imports: java.util.ArrayList
class InputPreferVarReceiverSameFileClasspathImplementationOfASameFileGenericWarnsSliceViolation {
	interface Sink<T> {
		boolean add(T value);
	}

	static class Names extends ArrayList<String> implements Sink<String> {
	}

	static Names create() {
		return new Names();
	}

	void m() {
		final Sink<String> names = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names.add("x"));
	}
}
// === end ===

// === case: receiver_same_file_classpath_interface_static_is_not_inherited_warns ===
// imports: java.util.Comparator
class InputPreferVarReceiverSameFileClasspathInterfaceStaticIsNotInheritedWarnsSliceViolation {
	static class Base {
		String nullsFirst(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base implements Comparator<String> {
		@Override
		public int compare(String left, String right) {
			return 0;
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.nullsFirst(null));
	}
}
// === end ===

// === case: receiver_same_file_classpath_override_through_an_unresolvable_supertype_warns ===
// imports: java.util.List
class InputPreferVarReceiverSameFileClasspathOverrideThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Names extends MissingList {
		public boolean add(String value) {
			return true;
		}
	}

	static Names create() {
		return new Names();
	}

	void m() {
		final List<String> names = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names.add("x"));
	}
}
// === end ===

// === case: receiver_same_file_classpath_supertype_list_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverSameFileClasspathSupertypeListWarnsSliceViolation {
	static class Special extends ArrayList<Integer> {
	}

	static Special create() {
		return new Special();
	}

	void m() {
		final List<Integer> ids = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.remove(1));
	}
}
// === end ===

// === case: receiver_same_file_classpath_supertype_overload_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverSameFileClasspathSupertypeOverloadWarnsSliceViolation {
	static class Special extends ArrayList<Integer> {
	}

	static Special create() {
		return new Special();
	}

	void m() {
		final Collection<Integer> ids = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.remove(1));
	}
}
// === end ===

// === case: receiver_same_file_classpath_supertype_same_simple_name_overload_warns ===
// imports: java.util.Timer
// imports: java.util.TimerTask
class InputPreferVarReceiverSameFileClasspathSupertypeSameSimpleNameOverloadWarnsSliceViolation {
	static class SqlTimer extends Timer {
		void schedule(TimerTask task, java.sql.Date time) {
		}
	}

	static SqlTimer create() {
		return new SqlTimer();
	}

	void m(TimerTask task) {
		final Timer timer = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		timer.schedule(task, new java.sql.Date(0));
	}
}
// === end ===

// === case: receiver_same_file_concrete_overload_of_a_passed_through_type_variable_warns ===
class InputPreferVarReceiverSameFileConcreteOverloadOfAPassedThroughTypeVariableWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Labels<T> extends Box<T> {
		String put(String value) {
			return "Labels";
		}
	}

	static Labels<String> create() {
		return new Labels<>();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_default_method_overload_warns ===
class InputPreferVarReceiverSameFileDefaultMethodOverloadWarnsSliceViolation {
	interface Named {
		default String draw(String value) {
			return "String";
		}
	}

	interface Shape {
		String draw(Object value);
	}

	static class Circle implements Named, Shape {
		@Override
		public String draw(Object value) {
			return "Object";
		}
	}

	static Circle create() {
		return new Circle();
	}

	void m() {
		final Shape shape = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shape.draw("x"));
	}
}
// === end ===

// === case: receiver_same_file_different_arity_warns ===
class InputPreferVarReceiverSameFileDifferentArityWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(Object first, Object second) {
			return "pair";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_erased_override_of_a_chained_method_type_variable_warns ===
class InputPreferVarReceiverSameFileErasedOverrideOfAChainedMethodTypeVariableWarnsSliceViolation {
	static class Base {
		<U extends CharSequence, T extends U> String accept(T value) {
			return "T";
		}
	}

	static class Derived extends Base {
		@Override
		String accept(CharSequence value) {
			return "CharSequence";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.accept("x"));
	}
}
// === end ===

// === case: receiver_same_file_erased_override_of_a_method_type_variable_warns ===
class InputPreferVarReceiverSameFileErasedOverrideOfAMethodTypeVariableWarnsSliceViolation {
	static class Base {
		<T extends CharSequence> String accept(T value) {
			return "T";
		}
	}

	static class Derived extends Base {
		@Override
		String accept(CharSequence value) {
			return "CharSequence";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.accept("x"));
	}
}
// === end ===

// === case: receiver_same_file_generic_override_warns ===
class InputPreferVarReceiverSameFileGenericOverrideWarnsSliceViolation {
	static class Box<T> {
		void put(T value) {
		}
	}

	static class IntBox extends Box<Integer> {
		@Override
		void put(Integer value) {
		}
	}

	static IntBox create() {
		return new IntBox();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		box.put(1);
	}
}
// === end ===

// === case: receiver_same_file_interface_object_override_warns ===
class InputPreferVarReceiverSameFileInterfaceObjectOverrideWarnsSliceViolation {
	interface Shape {
		String draw(Object value);
	}

	static class Circle implements Shape {
		@Override
		public String draw(Object value) {
			return "Object";
		}

		@Override
		public String toString() {
			return "circle";
		}
	}

	static Circle create() {
		return new Circle();
	}

	void m(Object other) {
		final Shape shape = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shape.toString() + shape.equals(other));
	}
}
// === end ===

// === case: receiver_same_file_interface_overload_warns ===
class InputPreferVarReceiverSameFileInterfaceOverloadWarnsSliceViolation {
	interface Shape {
		String draw(Object value);
	}

	static class Circle implements Shape {
		@Override
		public String draw(Object value) {
			return "Object";
		}

		public String draw(String value) {
			return "String";
		}
	}

	static Circle create() {
		return new Circle();
	}

	void m() {
		final Shape shape = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shape.draw("x"));
	}
}
// === end ===

// === case: receiver_same_file_interface_static_is_not_inherited_warns ===
class InputPreferVarReceiverSameFileInterfaceStaticIsNotInheritedWarnsSliceViolation {
	interface Helper {
		static String use(String value) {
			return "static";
		}
	}

	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base implements Helper {
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_interface_static_on_the_declared_type_is_not_inherited_warns ===
class InputPreferVarReceiverSameFileInterfaceStaticOnTheDeclaredTypeIsNotInheritedWarnsSliceViolation {
	interface Helper {
		static String use(String value) {
			return "static";
		}
	}

	static class Base implements Helper {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_intersection_bound_type_variable_overload_warns ===
class InputPreferVarReceiverSameFileIntersectionBoundTypeVariableOverloadWarnsSliceViolation {
	static class Base {
		String accept(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		<T extends java.lang.CharSequence & Comparable<T>> String accept(T value) {
			return "T";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.accept("x"));
	}
}
// === end ===

// === case: receiver_same_file_method_type_variable_overload_warns ===
class InputPreferVarReceiverSameFileMethodTypeVariableOverloadWarnsSliceViolation {
	static class Base {
		String accept(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		<T extends CharSequence> String accept(T value) {
			return "T";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.accept("x"));
	}
}
// === end ===

// === case: receiver_same_file_method_type_variable_override_warns ===
class InputPreferVarReceiverSameFileMethodTypeVariableOverrideWarnsSliceViolation {
	static class Base {
		<T extends CharSequence> String accept(T value) {
			return "Base";
		}
	}

	static class Derived extends Base {
		@Override
		<T extends CharSequence> String accept(T value) {
			return "Derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.accept("x"));
	}
}
// === end ===

// === case: receiver_same_file_new_overload_beside_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileNewOverloadBesideAnUnresolvableSupertypeWarnsSliceViolation {
	interface Shape {
		String draw(Object value);
	}

	static class Circle extends MissingBase implements Shape {
		@Override
		public String draw(Object value) {
			return "Object";
		}

		public String draw(String value) {
			return "String";
		}
	}

	static Circle create() {
		return new Circle();
	}

	void m() {
		final Shape shape = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(shape.draw("x"));
	}
}
// === end ===

// === case: receiver_same_file_new_overload_warns ===
class InputPreferVarReceiverSameFileNewOverloadWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_overload_beside_a_concrete_supertype_argument_warns ===
class InputPreferVarReceiverSameFileOverloadBesideAConcreteSupertypeArgumentWarnsSliceViolation {
	static class Animal {
	}

	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Dog extends Animal {
	}

	static class Kennel extends Box<Animal> {
		String put(Dog value) {
			return "Kennel";
		}
	}

	static Kennel create() {
		return new Kennel();
	}

	void m() {
		final Box<Animal> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(new Dog()));
	}
}
// === end ===

// === case: receiver_same_file_overload_of_a_different_enclosing_type_variable_warns ===
class InputPreferVarReceiverSameFileOverloadOfADifferentEnclosingTypeVariableWarnsSliceViolation<A extends Number, B extends CharSequence> {
	class Base {
		String put(A value) {
			return "Base";
		}
	}

	class Derived extends Base {
		String put(B value) {
			return "Derived";
		}
	}

	Derived create() {
		return new Derived();
	}

	void m() {
		final Base base = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(base.put(null));
	}
}
// === end ===

// === case: receiver_same_file_overload_of_a_shadowing_enclosing_type_variable_warns ===
class InputPreferVarReceiverSameFileOverloadOfAShadowingEnclosingTypeVariableWarnsSliceViolation<A extends Number> {
	class Base {
		String put(A value) {
			return "Base";
		}
	}

	class Middle<A extends CharSequence> {
		class Derived extends Base {
			String put(A value) {
				return "Derived";
			}
		}

		Derived create() {
			return new Derived();
		}

		void m() {
			final Base base = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(base.put(null));
		}
	}
}
// === end ===

// === case: receiver_same_file_overload_of_a_type_variable_array_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileOverloadOfATypeVariableArrayThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T[] values) {
			return "Box";
		}
	}

	static class Tags extends MissingBox {
		String put(String value) {
			return "Tags";
		}
	}

	static Tags create() {
		return new Tags();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_overload_of_an_enclosing_type_variable_parameter_warns ===
class InputPreferVarReceiverSameFileOverloadOfAnEnclosingTypeVariableParameterWarnsSliceViolation<T> {
	class Box {
		String put(T value) {
			return "Box";
		}
	}

	class Labels extends Box {
		String put(String value) {
			return "Labels";
		}
	}

	Labels create() {
		return new Labels();
	}

	void m() {
		final Box box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(null));
	}
}
// === end ===

// === case: receiver_same_file_overload_of_an_enclosing_type_variable_supertype_argument_warns ===
class InputPreferVarReceiverSameFileOverloadOfAnEnclosingTypeVariableSupertypeArgumentWarnsSliceViolation<A extends Number, B extends CharSequence> {
	class Base<T> {
		String put(T value) {
			return "Base";
		}
	}

	class Derived extends Base<A> {
		String put(B value) {
			return "Derived";
		}
	}

	Derived create() {
		return new Derived();
	}

	void m() {
		final Base<A> base = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(base.put(null));
	}
}
// === end ===

// === case: receiver_same_file_overload_on_a_same_file_parameter_type_warns ===
class InputPreferVarReceiverSameFileOverloadOnASameFileParameterTypeWarnsSliceViolation {
	static class Shape {
	}

	static class Circle extends Shape {
	}

	static class Base {
		String use(Shape value) {
			return "Shape";
		}
	}

	static class Derived extends Base {
		String use(Circle value) {
			return "Circle";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use(new Circle()));
	}
}
// === end ===

// === case: receiver_same_file_overload_on_the_declared_type_warns ===
class InputPreferVarReceiverSameFileOverloadOnTheDeclaredTypeWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Leaf extends Middle {
	}

	static class Middle extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Leaf create() {
		return new Leaf();
	}

	void m() {
		final Middle item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_overload_through_a_qualified_inner_supertype_warns ===
class InputPreferVarReceiverSameFileOverloadThroughAQualifiedInnerSupertypeWarnsSliceViolation {
	static class Host extends Outer<String> {
		Sub create() {
			return new Sub(this);
		}

		void m() {
			final Inner<Integer> inner = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(inner.put(null));
		}
	}

	static class Outer<A> {
		class Inner<T> {
			String put(T value) {
				return "Inner";
			}
		}
	}

	static class Sub extends Outer<String>.Inner<Integer> {
		Sub(Outer<String> outer) {
			outer.super();
		}

		String put(String value) {
			return "Sub";
		}
	}
}
// === end ===

// === case: receiver_same_file_overload_with_a_receiver_parameter_warns ===
class InputPreferVarReceiverSameFileOverloadWithAReceiverParameterWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(Derived this, String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_override_of_a_classpath_concrete_argument_warns ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputPreferVarReceiverSameFileOverrideOfAClasspathConcreteArgumentWarnsSliceViolation {
	static class Names extends ArrayList<String> {
		@Override
		public boolean add(String value) {
			return super.add(value);
		}
	}

	static Names create() {
		return new Names();
	}

	void m() {
		final List<String> names = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names.add("x"));
	}
}
// === end ===

// === case: receiver_same_file_override_of_a_classpath_member_type_parameter_warns ===
class InputPreferVarReceiverSameFileOverrideOfAClasspathMemberTypeParameterWarnsSliceViolation {
	static class QuietThread extends Thread {
		@Override
		public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
		}
	}

	static QuietThread create() {
		return new QuietThread();
	}

	void m() {
		final Thread thread = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		thread.setUncaughtExceptionHandler(null);
	}
}
// === end ===

// === case: receiver_same_file_override_of_a_same_file_type_argument_warns ===
class InputPreferVarReceiverSameFileOverrideOfASameFileTypeArgumentWarnsSliceViolation {
	static class Animal {
	}

	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Dog extends Animal {
	}

	static class Kennel extends Box<Animal> {
		@Override
		String put(Animal value) {
			return "Kennel";
		}
	}

	static Kennel create() {
		return new Kennel();
	}

	void m() {
		final Box<Animal> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(new Dog()));
	}
}
// === end ===

// === case: receiver_same_file_override_of_an_enclosing_type_variable_supertype_argument_warns ===
class InputPreferVarReceiverSameFileOverrideOfAnEnclosingTypeVariableSupertypeArgumentWarnsSliceViolation<A extends Number, B extends CharSequence> {
	class Base<T> {
		String put(T value) {
			return "Base";
		}
	}

	class Derived extends Base<A> {
		@Override
		String put(A value) {
			return "Derived";
		}
	}

	Derived create() {
		return new Derived();
	}

	void m() {
		final Base<A> base = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(base.put(null));
	}
}
// === end ===

// === case: receiver_same_file_override_of_an_enclosing_type_variable_warns ===
class InputPreferVarReceiverSameFileOverrideOfAnEnclosingTypeVariableWarnsSliceViolation<A extends Number, B extends CharSequence> {
	class Base {
		String put(A value) {
			return "Base";
		}
	}

	class Derived extends Base {
		@Override
		String put(A value) {
			return "Derived";
		}
	}

	Derived create() {
		return new Derived();
	}

	void m() {
		final Base base = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(base.put(null));
	}
}
// === end ===

// === case: receiver_same_file_override_of_an_inherited_member_type_parameter_warns ===
class InputPreferVarReceiverSameFileOverrideOfAnInheritedMemberTypeParameterWarnsSliceViolation {
	static class QuietThread extends Thread {
		@Override
		public void setUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
		}
	}

	static QuietThread create() {
		return new QuietThread();
	}

	void m() {
		final Thread thread = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		thread.setUncaughtExceptionHandler(null);
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
		String put(java.util.Date value) {
			return "TimedStore";
		}
	}

	static TimedStore create() {
		return new TimedStore();
	}

	void m() {
		final Store store = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(store.put(new Date()));
	}
}
// === end ===

// === case: receiver_same_file_override_through_a_qualified_inner_supertype_warns ===
class InputPreferVarReceiverSameFileOverrideThroughAQualifiedInnerSupertypeWarnsSliceViolation {
	static class Host extends Outer<String> {
		Sub create() {
			return new Sub(this);
		}

		void m() {
			final Inner<Integer> inner = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(inner.put(null));
		}
	}

	static class Outer<A> {
		class Inner<T> {
			String put(T value) {
				return "Inner";
			}
		}
	}

	static class Sub extends Outer<String>.Inner<Integer> {
		Sub(Outer<String> outer) {
			outer.super();
		}

		@Override
		String put(Integer value) {
			return "Sub";
		}
	}
}
// === end ===

// === case: receiver_same_file_override_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileOverrideThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Labels extends MissingBox {
		String put(String value) {
			return "Labels";
		}
	}

	static Labels create() {
		return new Labels();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_override_warns ===
class InputPreferVarReceiverSameFileOverrideWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		@Override
		String use(Object value) {
			return "Object";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_override_with_a_receiver_parameter_warns ===
class InputPreferVarReceiverSameFileOverrideWithAReceiverParameterWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		@Override
		String use(Derived this, Object value) {
			return "Derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_primitive_array_overload_of_a_type_variable_array_warns ===
class InputPreferVarReceiverSameFilePrimitiveArrayOverloadOfATypeVariableArrayWarnsSliceViolation {
	static class Box<T> {
		String put(T[] values) {
			return "T[]";
		}
	}

	static class IntBox extends Box<Integer> {
		String put(int[] values) {
			return "int[]";
		}
	}

	static IntBox create() {
		return new IntBox();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(null));
	}
}
// === end ===

// === case: receiver_same_file_primitive_overload_of_a_generic_warns ===
class InputPreferVarReceiverSameFilePrimitiveOverloadOfAGenericWarnsSliceViolation {
	static class Box<T> {
		void put(T value) {
		}
	}

	static class IntBox extends Box<Integer> {
		void put(int value) {
		}
	}

	static IntBox create() {
		return new IntBox();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		box.put(1);
	}
}
// === end ===

// === case: receiver_same_file_primitive_overload_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFilePrimitiveOverloadThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers extends MissingBox {
		String put(int value) {
			return "Numbers";
		}
	}

	static Numbers create() {
		return new Numbers();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(5));
	}
}
// === end ===

// === case: receiver_same_file_record_override_of_a_generic_interface_warns ===
class InputPreferVarReceiverSameFileRecordOverrideOfAGenericInterfaceWarnsSliceViolation {
	interface Sink<T> {
		String put(T value);
	}

	record Labels(String name) implements Sink<String> {
		@Override
		public String put(String value) {
			return name;
		}
	}

	static Labels create() {
		return new Labels("x");
	}

	void m() {
		final Sink<String> sink = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(sink.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_same_simple_name_overload_warns ===
class InputPreferVarReceiverSameFileSameSimpleNameOverloadWarnsSliceViolation {
	static class Store {
		String put(java.util.Date value) {
			return "util";
		}
	}

	static class SqlStore extends Store {
		String put(java.sql.Date value) {
			return "sql";
		}
	}

	static SqlStore create() {
		return new SqlStore();
	}

	void m() {
		final Store store = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(store.put(new java.sql.Date(0)));
	}
}
// === end ===

// === case: receiver_same_file_static_hiding_warns ===
class InputPreferVarReceiverSameFileStaticHidingWarnsSliceViolation {
	static class Base {
		static String tag() {
			return "base";
		}
	}

	static class Derived extends Base {
		static String tag() {
			return "derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.tag());
	}
}
// === end ===

// === case: receiver_same_file_static_new_overload_warns ===
class InputPreferVarReceiverSameFileStaticNewOverloadWarnsSliceViolation {
	static class Base {
		static String tag(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		static String tag(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.tag("x"));
	}
}
// === end ===

// === case: receiver_same_file_static_not_hidden_warns ===
class InputPreferVarReceiverSameFileStaticNotHiddenWarnsSliceViolation {
	static class Base {
		static String tag() {
			return "base";
		}
	}

	static class Derived extends Base {
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.tag());
	}
}
// === end ===

// === case: receiver_same_file_transitive_overload_warns ===
class InputPreferVarReceiverSameFileTransitiveOverloadWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Leaf extends Middle {
	}

	static class Middle extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Leaf create() {
		return new Leaf();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_array_overload_of_a_concrete_array_argument_warns ===
class InputPreferVarReceiverSameFileTypeVariableArrayOverloadOfAConcreteArrayArgumentWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers<E extends Number> extends Values {
		String put(E[] values) {
			return "Numbers";
		}
	}

	static class Values extends Box<Object[]> {
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Values values = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(values.put(new Integer[0]));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_array_overload_warns ===
class InputPreferVarReceiverSameFileTypeVariableArrayOverloadWarnsSliceViolation {
	static class Box<T> {
		String put(T[] values) {
			return "T[]";
		}
	}

	static class StringBox extends Box<String> {
		String put(String value) {
			return "String";
		}
	}

	static StringBox create() {
		return new StringBox();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(null));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_overload_beside_a_concrete_supertype_argument_warns ===
class InputPreferVarReceiverSameFileTypeVariableOverloadBesideAConcreteSupertypeArgumentWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Numbers<E extends Number> extends Box<Integer> {
		String put(E value) {
			return "Numbers";
		}
	}

	static Numbers<Integer> create() {
		return new Numbers<>();
	}

	void m() {
		final Box<Integer> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(5));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_overload_beside_another_passed_through_type_variable_warns ===
class InputPreferVarReceiverSameFileTypeVariableOverloadBesideAnotherPassedThroughTypeVariableWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Pair<A extends Number, B> extends Box<B> {
		String put(A value) {
			return "Pair";
		}
	}

	static Pair<Integer, String> create() {
		return new Pair<>();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put(null));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_overload_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileTypeVariableOverloadThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived<T extends CharSequence> extends MissingBase {
		String use(T value) {
			return "T";
		}
	}

	static Derived<String> create() {
		return new Derived<>();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_type_variable_override_through_an_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileTypeVariableOverrideThroughAnUnresolvableSupertypeWarnsSliceViolation {
	static class Box<T> {
		String put(T value) {
			return "Box";
		}
	}

	static class Values<T> extends MissingBox<T> {
		String put(T value) {
			return "Values";
		}
	}

	static Values<String> create() {
		return new Values<>();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_unresolvable_parameter_type_override_warns ===
class InputPreferVarReceiverSameFileUnresolvableParameterTypeOverrideWarnsSliceViolation {
	static class Base {
		String use(Missing value) {
			return "Base";
		}
	}

	static class Derived extends Base {
		@Override
		String use(Missing value) {
			return "Derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use(null));
	}
}
// === end ===

// === case: receiver_same_file_unresolvable_supertype_warns ===
class InputPreferVarReceiverSameFileUnresolvableSupertypeWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base implements MissingType {
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_varargs_minimum_arity_warns ===
class InputPreferVarReceiverSameFileVarargsMinimumArityWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(Object first, Object second, Object... rest) {
			return "varargs";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_varargs_overload_warns ===
class InputPreferVarReceiverSameFileVarargsOverloadWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(Object... values) {
			return "varargs";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item.use("x"));
	}
}
// === end ===

// === case: receiver_same_file_varargs_override_of_a_type_variable_warns ===
class InputPreferVarReceiverSameFileVarargsOverrideOfATypeVariableWarnsSliceViolation {
	static class Box<T> {
		String put(T... values) {
			return "T...";
		}
	}

	static class StringBox extends Box<String> {
		@Override
		String put(String... values) {
			return "String...";
		}
	}

	static StringBox create() {
		return new StringBox();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.put("x"));
	}
}
// === end ===

// === case: receiver_same_file_zero_parameter_overload_of_a_generic_warns ===
class InputPreferVarReceiverSameFileZeroParameterOverloadOfAGenericWarnsSliceViolation {
	static class Box<T> {
		String take(T value) {
			return "T";
		}
	}

	static class Sub extends Box<String> {
		String take() {
			return "none";
		}
	}

	static Sub create() {
		return new Sub();
	}

	void m() {
		final Box<String> box = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(box.take("x"));
	}
}
// === end ===

// === case: receiver_shared_arity_overloads_warns ===
// imports: java.util.ArrayList
// imports: java.util.Collection
class InputPreferVarReceiverSharedArityOverloadsWarnsSliceViolation {
	void m() {
		final Collection<Integer> ids = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(ids.toArray(Integer[]::new));
	}
}
// === end ===

// === case: receiver_through_a_call_result_warns ===
// imports: java.util.Objects
class InputPreferVarReceiverThroughACallResultWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m() {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(Objects.requireNonNull(item).use("x"));
	}
}
// === end ===

// === case: receiver_through_a_ternary_warns ===
class InputPreferVarReceiverThroughATernaryWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		String use(String value) {
			return "String";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m(boolean flag) {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println((flag ? item : item).use("x"));
	}
}
// === end ===

// === case: receiver_through_a_ternary_without_overloads_warns ===
class InputPreferVarReceiverThroughATernaryWithoutOverloadsWarnsSliceViolation {
	static class Base {
		String use(Object value) {
			return "Object";
		}
	}

	static class Derived extends Base {
		@Override
		String use(Object value) {
			return "Derived";
		}
	}

	static Derived create() {
		return new Derived();
	}

	void m(boolean flag) {
		final Base item = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println((flag ? item : item).use("x"));
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
		final Closeable[] value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Holder<String> value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_classpath_parameter_not_accepting_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingClasspathParameterNotAcceptingWarnsSliceViolation {
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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

		final Closeable value = flag ? new LocalStream() : new LocalStream(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_supertype_parameter_accepting_both_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingSupertypeParameterAcceptingBothWarnsSliceViolation {
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}

	class InputPreferVarSameFileTopLevelStream implements Closeable {
		@Override
		public void close() {
		}
	}
// === end ===

// === case: same_file_narrowing_unrelated_same_file_parameter_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingUnrelatedSameFileParameterWarnsSliceViolation {
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_narrowing_unresolvable_supertype_parameter_warns ===
// imports: java.io.Closeable
class InputPreferVarSameFileNarrowingUnresolvableSupertypeParameterWarnsSliceViolation {
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Base value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Shape value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Shape value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(take(value));
	}
}
// === end ===

// === case: same_file_overloads_disagree_warns ===
class InputPreferVarSameFileOverloadsDisagreeWarnsSliceViolation {
	static class Base {
	}

	static class Derived extends Base {
	}

	static Base make(int value) {
		return new Base();
	}

	static Derived make(String value) {
		return new Derived();
	}

	void m() {
		final Base item = make(1); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(item);
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final BufferedReader value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
		final Closeable value = create(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
			List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
			items = new LinkedList<>();
			System.out.println(items);
		}
		List<String> items = new ArrayList<>(); // violation (warning): Local variable should use 'var' instead of an explicit type.
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
// multi-fix-expected
// imports: java.util.ArrayList
// imports: java.util.LinkedHashSet
class InputPreferVarDiamondSingleObjectTypeArgSliceViolation {
	void m() {
		final var list = new ArrayList<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var set = new LinkedHashSet<Object>(); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
		final var parenWrapped = (new ArrayList<Object>()); // violation: Use the diamond operator '<>' instead of explicit '<Object>' with 'var'.
	}
}
// === end ===

// === case: static_call_generic_var ===
// imports: java.util.Collections
// imports: java.util.Optional
class InputPreferVarReflectionStaticCallGenericVarSliceViolation {
	void m() {
		final var list = Collections.emptyList(); // violation (warning): Consider using an explicit type ('var' with 'emptyList' loses generic type information).
		final var opt = Optional.empty(); // violation (warning): Consider using an explicit type ('var' with 'empty' loses generic type information).
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

// === case: static_import_generic_return_var_warns ===
// imports: static java.util.Collections.emptyList
class InputPreferVarStaticImportGenericReturnVarSliceViolation {
	void m() {
		final var values = emptyList(); // violation (warning): Consider using an explicit type ('var' with 'emptyList' loses generic type information).
		System.out.println(values);
	}
}
// === end ===

// === case: static_import_non_generic_call ===
// imports: static java.util.Arrays.asList
// imports: java.util.List
class InputPreferVarStaticImportNonGenericCallSliceViolation {
	void m() {
		final List<String> names = asList("a", "b"); // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: static_member_types_of_a_generic_class_written_qualified_convert ===
// multi-fix-expected
class InputPreferVarStaticMemberTypesOfAGenericClassWrittenQualifiedConvertSliceViolation {
	interface Api<T> {
		class Member {
		}

		Member member();
	}

	static class Outer<T> {
		enum Mode {
			ON
		}

		static final class Nested {
		}

		Mode mode() {
			return Mode.ON;
		}

		Nested nested() {
			return new Nested();
		}
	}

	void m(Api<String> api, Outer<String> strings) {
		final Api.Member member = api.member(); // violation: Local variable must use 'var' instead of an explicit type.
		final Outer.Mode mode = strings.mode(); // violation: Local variable must use 'var' instead of an explicit type.
		final Outer.Nested nested = strings.nested(); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(member);
		System.out.println(mode);
		System.out.println(nested);
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
		/* 𝛼 */ for (Map.Entry<String, String> entry : map.entrySet()) // violation: For-each variable must use 'var' instead of an explicit type.
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
		final Closeable existing = openReader(); // violation (warning): Local variable should use 'var' instead of an explicit type.
		try (existing) {
			System.out.println(use(existing));
		}
	}
}
// === end ===

// === case: try_resource_annotated_qualified_generic_type_converts ===
class InputPreferVarTryResourceAnnotatedQualifiedGenericTypeConvertsSliceViolation {
	@interface Marker {
	}

	static class Outer {
		static final class Res<T> implements AutoCloseable {
			@Override
			public void close() {
			}
		}
	}

	void m() {
		try (@Marker Outer.Res<String> res = new Outer.Res<>()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(res);
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
		try (@Deprecated Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable a = openReader(); Closeable b = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type. // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (BufferedReader reader = holder.open()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(reader.read());
		}
	}
}
// === end ===

// === case: try_resource_call_on_a_variable_of_an_imported_unloadable_class_is_not_resolvable ===
// imports: com.example.Registry
// imports: java.io.BufferedReader
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceCallOnAVariableOfAnImportedUnloadableClassIsNotResolvableSliceViolation {
	void m(Registry registry) throws IOException {
		try (BufferedReader reader = registry.open()) {
			System.out.println(reader.read());
		}
		try (BufferedReader other = new BufferedReader(new StringReader(""))) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(other.read());
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable a = openReader(); Wrapper b = new Wrapper(null)) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type. // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (InputStream in = Files.newInputStream(path)) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_resource_inside_a_field_initializer_lambda_converts ===
// imports: java.util.Scanner
class InputPreferVarTryResourceInsideAFieldInitializerLambdaConvertsSliceViolation {
	final Runnable task = () -> {
		try (Scanner scanner = new Scanner("a")) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(scanner.next());
		}
	};
}
// === end ===

// === case: try_resource_inside_a_multi_variable_local_initializer_converts ===
// multi-fix-expected
class InputPreferVarTryResourceInsideAMultiVariableLocalInitializerConvertsSliceViolation {
	static final class Pair<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m() {
		final Runnable first = () -> { // violation (warning): Local variable should use 'var' instead of an explicit type.
			try (Pair<String, Integer> pair = new Pair<>()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
				System.out.println(pair);
			}
		}, second = first;
		second.run();
	}
}
// === end ===

// === case: try_resource_inside_a_primitive_local_initializer_converts ===
// multi-fix-expected
// imports: java.util.Scanner
// imports: java.util.function.IntSupplier
class InputPreferVarTryResourceInsideAPrimitiveLocalInitializerConvertsSliceViolation {
	static int measure(IntSupplier supplier) {
		return supplier.getAsInt();
	}

	void m() {
		final int length = measure(() -> { // violation (warning): Local variable should use 'var' instead of an explicit type.
			try (Scanner scanner = new Scanner("a")) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
				return scanner.next().length();
			}
		});
		System.out.println(length);
	}
}
// === end ===

// === case: try_resource_inside_a_wrapped_explicit_array_initializer_converts ===
// imports: java.util.Scanner
class InputPreferVarTryResourceInsideAWrappedExplicitArrayInitializerConvertsSliceViolation {
	void m() {
		final Runnable[] tasks = // violation: Use an implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
				new Runnable[]{() -> {
					try (Scanner scanner = new Scanner("a")) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
						System.out.println(scanner.next());
					}
				}};
		System.out.println(tasks.length);
	}
}
// === end ===

// === case: try_resource_inside_a_wrapped_reference_local_initializer_converts ===
// multi-fix-expected
class InputPreferVarTryResourceInsideAWrappedReferenceLocalInitializerConvertsSliceViolation {
	static final class Pair<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	static <T> T wrap(T value) {
		return value;
	}

	void m() {
		final Runnable task = wrap(() -> { // violation (warning): Local variable should use 'var' instead of an explicit type.
			try (Pair<String, Integer> pair = new Pair<>()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
				System.out.println(pair);
			}
		});
		task.run();
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
		try (Closeable value = create()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(describe(c));
		}
	}
}
// === end ===

// === case: try_resource_overload_selection_through_a_ternary_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceOverloadSelectionThroughATernaryWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	static String use(BufferedReader reader) {
		return "reader";
	}

	static String use(Closeable closeable) {
		return "closeable";
	}

	void m(boolean flag) throws IOException {
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(use(flag ? c : null));
		}
	}
}
// === end ===

// === case: try_resource_overload_selection_unchanged_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceOverloadSelectionUnchangedWarnsSliceViolation {
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (InputStream in = java.nio.file.Files.newInputStream(path)) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (java.io.Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(use(c));
		}
	}
}
// === end ===

// === case: try_resource_receiver_new_overload_warns ===
class InputPreferVarTryResourceReceiverNewOverloadWarnsSliceViolation {
	static class Res implements AutoCloseable {
		@Override
		public void close() {
		}

		String use(Object value) {
			return "Object";
		}
	}

	static class SpecialRes extends Res {
		String use(String value) {
			return "String";
		}
	}

	static SpecialRes create() {
		return new SpecialRes();
	}

	void m() {
		try (Res resource = create()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(resource.use("x"));
		}
	}
}
// === end ===

// === case: try_resource_receiver_override_warns ===
// imports: java.io.BufferedReader
// imports: java.io.Closeable
// imports: java.io.IOException
// imports: java.io.StringReader
class InputPreferVarTryResourceReceiverOverrideWarnsSliceViolation {
	static BufferedReader openReader() {
		return new BufferedReader(new StringReader(""));
	}

	void m() throws IOException {
		try (Closeable reader = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			reader.close();
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
		try (Closeable a = openReader(); SinkResource sink = new SinkResource(a)) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type. // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(sink);
		}
	}
}
// === end ===

// === case: try_resource_referenced_by_a_nested_try_warns ===
// multi-fix-expected
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
		try (Closeable a = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			try (Wrapper b = new Wrapper(null)) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (Closeable c = openReader()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
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
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (@Nonnull ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
				ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_body_assigns_an_array_on_the_same_line ===
// imports: java.util.Scanner
class InputPreferVarTryWithResourcesBodyAssignsAnArrayOnTheSameLineSliceViolation {
	void m(Scanner in, String[] a) throws Exception {
		try (Scanner sc = in) { a = new String[]{"b"}; } // violation: Try-with-resources variable must use 'var' instead of an explicit type.
		System.out.println(a.length);
	}
}
// === end ===

// === case: try_with_resources_body_call_on_resource_receiver ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryWithResourcesBodyCallOnResourceReceiverSliceViolation {
	void m(byte[] data) throws Exception {
		try (var in = new ByteArrayInputStream(data)) {
			final byte[] all = in.readAllBytes(); // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(all.length);
		}
	}
}
// === end ===

// === case: try_with_resources_conditional_arm_cannot_take_warns ===
class InputPreferVarTryWithResourcesConditionalArmCannotTakeWarnsSliceViolation {
	static class MyPair<A> extends Src<A, Integer> {
	}

	static class Src<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m(boolean flag) throws Exception {
		try (Src<String, Integer> armCannotTake = flag ? new Src<>() : new MyPair<>()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(armCannotTake);
		}
	}
}
// === end ===

// === case: try_with_resources_declared_supertype_warns ===
// imports: java.io.ByteArrayInputStream
// imports: java.io.InputStream
class InputPreferVarTryWithResourcesDeclaredSupertypeWarnsSliceViolation {
	void m() throws Exception {
		try (InputStream in = new ByteArrayInputStream(new byte[0])) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(in.read());
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
		try (Pair<String, Integer> p = new Pair<>()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (ByteArrayInputStream in = open()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
		try (Pair<String, Integer> p = new Pair<String, Integer>()) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(p);
		}
	}
}
// === end ===

// === case: try_with_resources_mixed_ref_and_decl ===
// imports: java.io.ByteArrayInputStream
class InputPreferVarTryMixedRefAndDeclSliceViolation {
	void m(ByteArrayInputStream existing) throws Exception {
		try (existing; ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(existing.read() + in.read());
		}
	}
}
// === end ===

// === case: try_with_resources_narrowing_close_warns ===
// imports: java.io.Reader
// imports: java.io.StringReader
class InputPreferVarTryWithResourcesNarrowingCloseWarnsSliceViolation {
	void m(String text) throws Exception {
		try (Reader reader = new StringReader(text)) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(reader.read());
		}
	}
}
// === end ===

// === case: try_with_resources_qualified_type ===
class InputPreferVarTryWithResourcesQualifiedTypeSliceViolation {
	void m() throws Exception {
		try (java.io.ByteArrayInputStream s = new java.io.ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
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
				ByteArrayInputStream a = new ByteArrayInputStream(new byte[0]); // violation: Try-with-resources variable must use 'var' instead of an explicit type.
				ByteArrayInputStream b = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources variable must use 'var' instead of an explicit type.
			System.out.println(a.read() + b.read());
		}
	}
}
// === end ===

// === case: try_with_resources_whose_class_cannot_take_the_declared_arguments_warns ===
class InputPreferVarTryWithResourcesWhoseClassCannotTakeTheDeclaredArgumentsWarnsSliceViolation {
	static class MyPair<A> extends Src<A, Integer> {
	}

	static class Src<A, B> implements AutoCloseable {
		@Override
		public void close() {
		}
	}

	void m() throws Exception {
		try (Src<String, Integer> pair = new MyPair<>()) { // violation (warning): Try-with-resources variable should use 'var' instead of an explicit type.
			System.out.println(pair);
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

// === case: type_args_on_reflection_generic_warns ===
// imports: java.util.Collections
class InputPreferVarReflectionTypeArgsOnReflectionGenericSliceViolation {
	void m() {
		final var list = Collections.<String>emptyList(); // violation (warning): Consider using an explicit type instead of type arguments on 'emptyList'.
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

// === case: type_variable_bounded_by_a_final_class_warns ===
class InputPreferVarTypeVariableBoundedByAFinalClassWarnsSliceViolation {
	record Label(String text) {}

	<T extends Label> void m(T label) {
		final Label copy = label; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(copy.text());
	}
}
// === end ===

// === case: unproven_field_access_initializer_warns ===
// imports: java.util.List
class InputPreferVarUnprovenFieldAccessInitializerWarnsSliceViolation {
	void m(Registry registry) {
		final List<String> names = registry.names; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: unproven_identifier_initializer_warns ===
// imports: java.util.List
class InputPreferVarUnprovenIdentifierInitializerWarnsSliceViolation {
	void m() {
		final var raw = Registry.load();
		final List<String> names = raw; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(names);
	}
}
// === end ===

// === case: unproven_this_initializer_warns ===
class InputPreferVarUnprovenThisInitializerWarnsSliceViolation {
	void m() {
		final InputPreferVarUnprovenThisInitializerWarnsSliceViolation self = this; // violation (warning): Local variable should use 'var' instead of an explicit type.
		System.out.println(self);
	}
}
// === end ===

// === case: wrapped_generic_type_does_not_close_on_line ===
// skip-reason: cannot rewrite the declaration: the declared type arguments do not close on the reported line
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