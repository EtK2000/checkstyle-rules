package com.etk2000.checkstyle.inputs.redundantarraycreation;

// === case: block_comment_with_brace ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationBlockCommentWithBraceSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a", /* } */ "b"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: char_literal_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationCharLiteralWithBracesSliceViolation {
	void m() {
		Arrays.asList(new Object[]{'}'}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: empty_array_comment_before_new ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayCommentBeforeNewSliceViolation {
	void m() {
		String.join(",", /* note */ new CharSequence[]{}); // violation: Remove redundant array creation for varargs parameter of 'join'.
	}
}
// === end ===

// === case: empty_array_only_argument ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayOnlyArgumentSliceViolation {
	void m() {
		Arrays.asList(new Object[]{}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
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
		Collections.addAll(list, new String[]{}); // violation: Remove redundant array creation for varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: escaped_quote_in_string ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEscapedQuoteInStringSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a\"b}c"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.Arrays
// imports: java.util.Collections
@SuppressWarnings("unused")
class InputRedundantArrayCreationViolation {
	void arraysAsList() {
		Arrays.asList(new Object[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}

	void collectionsAddAll() {
		final var list = new ArrayList<String>();
		Collections.addAll(list, new String[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'addAll'.
	}

	void constructorVarargs() {
		new ProcessBuilder(new String[]{"cmd", "arg"}); // violation: Remove redundant array creation for varargs parameter of 'ProcessBuilder'.
	}

	void stringFormat() {
		String.format("%s%s", new Object[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'format'.
	}

	void stringJoin() {
		String.join(",", new CharSequence[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'join'.
	}
}
// === end ===

// === case: multidim_brace_init_skipped ===
// skip-reason: nested array initializer
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultidimBraceInitSkippedSliceViolation {
	void m(List<String[]> list) {
		Collections.addAll(list, new String[][]{{"a"}, {"b"}}); // violation: Remove redundant array creation for varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: multidim_plain_elements ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultidimPlainElementsSliceViolation {
	void m(String[] arr1, String[] arr2, List<String[]> list) {
		Collections.addAll(list, new String[][]{arr1, arr2}); // violation: Remove redundant array creation for varargs parameter of 'addAll'.
	}
}
// === end ===

// === case: multiline_array_skipped ===
// skip-reason: multi-line array initializer
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineArraySkippedSliceViolation {
	void m() {
		Arrays.asList(new Object[]{ // violation: Remove redundant array creation for varargs parameter of 'asList'.
			"a", "b"
		});
	}
}
// === end ===

// === case: multiple_elements ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultipleElementsSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a", "b", "c"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
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
		String.format("%s", new Object[]{foo(1, 2)}); // violation: Remove redundant array creation for varargs parameter of 'format'.
	}
}
// === end ===

// === case: primitive_varargs_method ===
// imports: java.util.stream.IntStream
@SuppressWarnings("unused")
class InputRedundantArrayCreationPrimitiveVarargsMethodSliceViolation {
	void m() {
		IntStream.of(new int[]{1, 2}); // violation: Remove redundant array creation for varargs parameter of 'of'.
	}
}
// === end ===

// === case: single_element ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationSingleElementSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: string_literals_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringLiteralsWithBracesSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a{b}c"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: string_unbalanced_open ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringUnbalancedOpenSliceViolation {
	void m() {
		Arrays.asList(new Object[]{"a(b[c"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: text_block_continuation ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + Arrays.asList(new Object[]{"a"}).toString(); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}
}
// === end ===

// === case: trailing_comma_in_initializer ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTrailingCommaInInitializerSliceViolation {
	void m() {
		String.join(",", new CharSequence[]{"a", "b",}); // violation: Remove redundant array creation for varargs parameter of 'join'.
	}
}
// === end ===

// === case: with_preceding_arguments ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationWithPrecedingArgumentsSliceViolation {
	void m() {
		String.format("%s%s", new Object[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'format'.
	}
}
// === end ===