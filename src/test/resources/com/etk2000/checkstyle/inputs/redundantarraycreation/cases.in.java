package com.etk2000.checkstyle.inputs.redundantarraycreation;

// === case: array_access_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationArrayAccessElementSliceViolation {
	void m(String[] parts) {
		String.join(",", new CharSequence[]{parts[0], parts[1]}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: block_comment_with_brace ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationBlockCommentWithBraceSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a", /* } */ "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: cast_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCastElementSliceViolation {
	void m(Object value) {
		String.join(",", new CharSequence[]{(CharSequence) value, "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: char_literal_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationCharLiteralWithBracesSliceViolation {
	void m() {
		Arrays.asList(new Object[]{'}'}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: comment_after_close_brace ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentAfterCloseBraceSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a"} /* c */); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_after_new_keyword ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentAfterNewKeywordSliceViolation {
	void m() {
		String.join(",", new /* c */ CharSequence[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_after_open_brace ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentAfterOpenBraceSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{/* c */ "a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_after_trailing_comma ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentAfterTrailingCommaSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a", "b", /* c */}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_after_trailing_comma_single_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentAfterTrailingCommaSingleElementSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a", /* c */}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_before_close_brace ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentBeforeCloseBraceSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a", "b" /* c */}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_before_new_nonempty ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentBeforeNewNonemptySliceViolation {
	void m() {
		String.join(",", /* c */ new CharSequence[]{"a"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_before_open_brace ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentBeforeOpenBraceSliceViolation {
	void m() {
		String.join(",", new CharSequence[] /* c */ {"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_inside_brackets ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentInsideBracketsSliceViolation {
	void m() {
		String.join(",", new CharSequence[/* c */]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_only_empty_initializer ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentOnlyEmptyInitializerSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{/* c */}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: comment_only_empty_initializer_sole_arg ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationCommentOnlyEmptyInitializerSoleArgSliceViolation {
	void m() {
		new ProcessBuilder(new String[]{/* c */}); // violation: Remove the redundant array creation for the varargs parameter of 'ProcessBuilder'.
	}
}
// === end ===

// === case: constructor_varargs_empty ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationConstructorVarargsEmptySliceViolation {
	void m() {
		new ProcessBuilder(new String[]{}); // violation: Remove the redundant array creation for the varargs parameter of 'ProcessBuilder'.
	}
}
// === end ===

// === case: empty_array_comment_before_new ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayCommentBeforeNewSliceViolation {
	void m() {
		String.join(",", /* note */ new CharSequence[]{}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: empty_array_only_argument ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayOnlyArgumentSliceViolation {
	void m() {
		Arrays.asList(new Object[]{}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: empty_array_two_preceding_args ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayTwoPrecedingArgsSliceViolation {
	String foo(int a, int b) {
		return a + "-" + b;
	}

	void m() {
		String.format("%s", foo(1, 2), new Object[]{}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}
}
// === end ===

// === case: empty_array_with_preceding_arg ===
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayWithPrecedingArgSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(list, new String[]{}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: escaped_quote_in_string ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEscapedQuoteInStringSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a\"b}c"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: field_initializer_call ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationFieldInitializerCallSliceViolation {
	final String names = String.join(",", new CharSequence[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
}
// === end ===

// === case: inside_compact_constructor ===
record InputRedundantArrayCreationInsideCompactConstructorSliceViolation(String value) {
	InputRedundantArrayCreationInsideCompactConstructorSliceViolation {
		value = String.join(",", new CharSequence[]{value, "x"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: inside_lambda_body ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationInsideLambdaBodySliceViolation {
	final Runnable task = () -> System.out.println(String.join(",", new CharSequence[]{"a"})); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
}
// === end ===

// === case: inside_one_line_if ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationInsideOneLineIfSliceViolation {
	void m(boolean flag) {
		if (flag)
			System.out.println(String.join(",", new CharSequence[]{"a"})); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: inside_resource_header ===
// imports: java.io.StringReader
@SuppressWarnings("unused")
class InputRedundantArrayCreationInsideResourceHeaderSliceViolation {
	void m() throws Exception {
		try (var reader = new StringReader(String.join(",", new CharSequence[]{"a"}))) { // violation: Remove the redundant array creation for the varargs parameter of 'join'.
			reader.read();
		}
	}
}
// === end ===

// === case: inside_switch_arm ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationInsideSwitchArmSliceViolation {
	void m(int key) {
		switch (key) {
			case 0 -> System.out.println(String.join(",", new CharSequence[]{"a"})); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
			default -> System.out.println(key);
		}
	}
}
// === end ===

// === case: lambda_elements ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationLambdaElementsSliceViolation {
	void m(List<Runnable> tasks) {
		Collections.addAll(tasks, new Runnable[]{() -> {}, () -> {}}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: line_comment_marker_in_string_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationLineCommentMarkerInStringElementSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"//x", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: lone_cast_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneCastElementSliceViolation {
	void m(Object value) {
		String.join(",", new CharSequence[]{(CharSequence) value}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: lone_concatenation_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneConcatenationElementSliceViolation {
	void m(String name) {
		String.join(",", new CharSequence[]{name + "!"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: lone_lambda_element ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneLambdaElementSliceViolation {
	void m(List<Runnable> tasks) {
		Collections.addAll(tasks, new Runnable[]{() -> {}}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: lone_object_creation_element ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneObjectCreationElementSliceViolation {
	void m() {
		Arrays.asList(new Object[]{new StringBuilder("a")}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: lone_provable_element_forms ===
// multi-fix-expected
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneProvableElementFormsSliceViolation {
	void booleans() {
		String.format("%b", new Object[]{true}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%b", new Object[]{false}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}

	void numbers() {
		String.format("%d", new Object[]{1}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{1L}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%s", new Object[]{1.5f}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%s", new Object[]{'c'}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}

	void operators(int a, int b) {
		String.format("%d", new Object[]{a - b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{a * b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{a / b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{a % b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{-a}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{a & b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%d", new Object[]{a << b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%b", new Object[]{a > b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		String.format("%b", new Object[]{a == b}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}

	void self() {
		String.format("%s", new Object[]{this}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}
}
// === end ===

// === case: main ===
// multi-fix-expected
// imports: java.util.ArrayList
// imports: java.util.Arrays
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationViolation {
	void arraysAsList() {
		Arrays.asList(new Object[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}

	void collectionsAddAll() {
		final var list = new ArrayList<String>();
		Collections.addAll(list, new String[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}

	void constructorVarargs() {
		new ProcessBuilder(new String[]{"cmd", "arg"}); // violation: Remove the redundant array creation for the varargs parameter of 'ProcessBuilder'.
	}

	void stringFormat() {
		String.format("%s%s", new Object[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}

	void stringJoin() {
		String.join(",", new CharSequence[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: multidim_brace_init_skipped ===
// skip-reason: cannot remove the array creation: its elements are nested array initializers
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultidimBraceInitSkippedSliceViolation {
	void m(List<String[]> list) {
		Collections.addAll(list, new String[][]{{"a"}, {"b"}}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: multidim_plain_elements ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultidimPlainElementsSliceViolation {
	void m(String[] arr1, String[] arr2, List<String[]> list) {
		Collections.addAll(list, new String[][]{arr1, arr2}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: multiline_block_comment_inside_initializer ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineBlockCommentInsideInitializerSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			/* note */ "a", "b"
		});
	}
}
// === end ===

// === case: multiline_brace_on_next_line ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineBraceOnNextLineSliceViolation {
	void m() {
		Arrays.asList(new Object[] // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
				{"a", "b"});
	}
}
// === end ===

// === case: multiline_close_split ===
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineCloseSplitSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(
				list,
				new String[]{ // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
						"a"
				}
		);
	}
}
// === end ===

// === case: multiline_comment_in_deleted_head ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineCommentInDeletedHeadSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(
				list,
				new /* c */ String[]{ // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
						"a"
				}
		);
	}
}
// === end ===

// === case: multiline_comment_inside_initializer ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineCommentInsideInitializerSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			// note
			"a", "b"
		});
	}
}
// === end ===

// === case: multiline_comment_only_empty_initializer ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineCommentOnlyEmptyInitializerSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(
				list,
				new String[]{ // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
						// note
				}
		);
	}
}
// === end ===

// === case: multiline_empty_array ===
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineEmptyArraySliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(
				list,
				new String[]{} // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
		);
	}
}
// === end ===

// === case: multiline_fold_at_line_budget ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineFoldAtLineBudgetSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"
		});
	}
}
// === end ===

// === case: multiline_fold_over_line_budget ===
// skip-reason: cannot remove the array creation: folding the initializer onto one line would exceed the line-length budget
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineFoldOverLineBudgetSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
			"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
			"cccccccccccccccccccccccccccccccccccccccc"
		});
	}
}
// === end ===

// === case: multiline_initializer_body ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineInitializerBodySliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			"a", "b"
		});
	}
}
// === end ===

// === case: multiline_open_paren_above ===
// imports: java.util.ArrayList
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineOpenParenAboveSliceViolation {
	void m() {
		final var list = new ArrayList<String>();
		Collections.addAll(
				list,
				new String[]{"a"} // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
		);
	}
}
// === end ===

// === case: multiline_trailing_comma ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineTrailingCommaSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			"a", "b",
		});
	}
}
// === end ===

// === case: multiple_elements ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultipleElementsSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a", "b", "c"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: nested_parens_in_elements ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationNestedParensInElementsSliceViolation {
	String foo(int a, int b) {
		return a + "-" + b;
	}

	void m() {
		String.format("%s%s", new Object[]{foo(1, 2), "x"}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}
}
// === end ===

// === case: nested_violation_inner_and_outer ===
// multi-fix-expected
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationNestedViolationInnerAndOuterSliceViolation {
	void m() {
		Arrays.asList(new Object[]{String.join(",", new CharSequence[]{"a"}), "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'. // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: primitive_varargs_method ===
// imports: java.util.stream.IntStream
@SuppressWarnings("unused")
class InputRedundantArrayCreationPrimitiveVarargsMethodSliceViolation {
	void m() {
		IntStream.of(new int[]{1, 2}); // violation: Remove the redundant array creation for the varargs parameter of 'of'.
	}
}
// === end ===

// === case: quote_char_and_block_marker_elements ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationQuoteCharAndBlockMarkerElementsSliceViolation {
	void m() {
		Arrays.asList(new Object[]{'"', "/*"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: resource_receiver_varargs ===
// imports: java.io.PrintWriter
@SuppressWarnings("unused")
class InputRedundantArrayCreationResourceReceiverVarargsSliceViolation {
	void m() throws Exception {
		try (var writer = new PrintWriter(System.out)) {
			writer.format("%s", new Object[]{"a"}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
		}
	}
}
// === end ===

// === case: single_element ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationSingleElementSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: string_literals_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringLiteralsWithBracesSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a{b}c"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: string_unbalanced_open ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringUnbalancedOpenSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a(b[c"}); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: supplementary_inside_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationSupplementaryInsideElementSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"𝒜", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: ternary_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTernaryElementSliceViolation {
	void m(boolean flag) {
		String.join(",", new CharSequence[]{flag ? "a" : "b", "c"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: text_block_continuation ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + Arrays.asList(new Object[]{"a"}).toString(); // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
	}
}
// === end ===

// === case: text_block_element_skipped ===
// skip-reason: cannot remove the array creation: folding the initializer onto one line would break a text block inside it
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockElementSkippedSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove the redundant array creation for the varargs parameter of 'asList'.
			"""
			a
			"""
		});
	}
}
// === end ===

// === case: trailing_comma_in_initializer ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTrailingCommaInInitializerSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a", "b",}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: trailing_comma_single_element ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTrailingCommaSingleElementSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a",}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: two_comments_in_span ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTwoCommentsInSpanSliceViolation {
	void m() {
		String.join(",", new /* a */ CharSequence[] /* b */ {"x"}); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: two_violations_one_line ===
// multi-fix-expected
@SuppressWarnings("unused")
class InputRedundantArrayCreationTwoViolationsOneLineSliceViolation {
	void m() {
		System.out.println(String.join(",", new CharSequence[]{"a"}) + String.join(";", new CharSequence[]{"b"})); // violation: Remove the redundant array creation for the varargs parameter of 'join'. // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: type_witness_on_owner_call ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationTypeWitnessOnOwnerCallSliceViolation {
	void m(List<String> list) {
		Collections.<String>addAll(list, new String[]{"a"}); // violation: Remove the redundant array creation for the varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: whitespace_only_initializer ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationWhitespaceOnlyInitializerSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{ }); // violation: Remove the redundant array creation for the varargs parameter of 'join'.
	}
}
// === end ===

// === case: with_preceding_arguments ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationWithPrecedingArgumentsSliceViolation {
	void m() {
		String.format("%s%s", new Object[]{"a", "b"}); // violation: Remove the redundant array creation for the varargs parameter of 'format'.
	}
}
// === end ===