package com.etk2000.checkstyle.inputs.redundantarraycreation;

// === case: block_comment_with_brace ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationBlockCommentWithBraceSliceViolation {
	void m() {
		Arrays.asList("a", /* } */ "b");
	}
}
// === end ===

// === case: char_literal_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationCharLiteralWithBracesSliceViolation {
	void m() {
		Arrays.asList('}');
	}
}
// === end ===

// === case: empty_array_comment_before_new ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayCommentBeforeNewSliceViolation {
	void m() {
		String.join(",");
	}
}
// === end ===

// === case: empty_array_only_argument ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayOnlyArgumentSliceViolation {
	void m() {
		Arrays.asList();
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
		Collections.addAll(list);
	}
}
// === end ===

// === case: escaped_quote_in_string ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationEscapedQuoteInStringSliceViolation {
	void m() {
		Arrays.asList("a\"b}c");
	}
}
// === end ===

// === case: main ===
// imports: java.util.ArrayList
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationViolation {
	void arraysAsList() {
		List.of("a", "b");
	}

	void collectionsAddAll() {
		final var list = new ArrayList<String>();
		Collections.addAll(list, "a", "b");
	}

	void constructorVarargs() {
		new ProcessBuilder("cmd", "arg");
	}

	void stringFormat() {
		"%s%s".formatted("a", "b");
	}

	void stringJoin() {
		String.join(",", "a", "b");
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
		Collections.addAll(list, new String[][]{{"a"}, {"b"}});
	}
}
// === end ===

// === case: multidim_plain_elements ===
// imports: java.util.Collections
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultidimPlainElementsSliceViolation {
	void m(String[] arr1, String[] arr2, List<String[]> list) {
		Collections.addAll(list, arr1, arr2);
	}
}
// === end ===

// === case: multiline_array_skipped ===
// skip-reason: multi-line array initializer
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineArraySkippedSliceViolation {
	void m() {
		Arrays.asList(new Object[]{
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
		Arrays.asList("a", "b", "c");
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
		String.format("%s", foo(1, 2));
	}
}
// === end ===

// === case: primitive_varargs_method ===
// imports: java.util.stream.IntStream
@SuppressWarnings("unused")
class InputRedundantArrayCreationPrimitiveVarargsMethodSliceViolation {
	void m() {
		IntStream.of(1, 2);
	}
}
// === end ===

// === case: single_element ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationSingleElementSliceViolation {
	void m() {
		Arrays.asList("a");
	}
}
// === end ===

// === case: string_literals_with_braces ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringLiteralsWithBracesSliceViolation {
	void m() {
		Arrays.asList("a{b}c");
	}
}
// === end ===

// === case: string_unbalanced_open ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringUnbalancedOpenSliceViolation {
	void m() {
		Arrays.asList("a(b[c");
	}
}
// === end ===

// === case: text_block_continuation ===
// imports: java.util.Arrays
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + Arrays.asList("a").toString();
	}
}
// === end ===

// === case: trailing_comma_in_initializer ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationTrailingCommaInInitializerSliceViolation {
	void m() {
		String.join(",", "a", "b");
	}
}
// === end ===

// === case: with_preceding_arguments ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationWithPrecedingArgumentsSliceViolation {
	void m() {
		String.format("%s%s", "a", "b");
	}
}
// === end ===