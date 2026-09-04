// === case: block_comment_with_brace ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationBlockCommentWithBraceSliceViolation {
	void m() {
		List.of("a", /* } */ "b");
	}
}
// === end ===

// === case: char_literal_with_braces ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationCharLiteralWithBracesSliceViolation {
	void m() {
		List.of('}');
	}
}
// === end ===

// === case: empty_array_only_argument ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayOnlyArgumentSliceViolation {
	void m() {
		List.of();
	}
}
// === end ===

// === case: escaped_quote_in_string ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationEscapedQuoteInStringSliceViolation {
	void m() {
		List.of("a\"b}c");
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

// === case: multiline_array_skipped ===
// skip-reason: cannot remove the array creation: the initializer spans multiple lines
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineArraySkippedSliceViolation {
	void m() {
		List.of(new Object[]{
			"a", "b"
		});
	}
}
// === end ===

// === case: multiple_elements ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultipleElementsSliceViolation {
	void m() {
		List.of("a", "b", "c");
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
		"%s".formatted(foo(1, 2));
	}
}
// === end ===

// === case: single_element ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationSingleElementSliceViolation {
	void m() {
		List.of("a");
	}
}
// === end ===

// === case: string_literals_with_braces ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringLiteralsWithBracesSliceViolation {
	void m() {
		List.of("a{b}c");
	}
}
// === end ===

// === case: string_unbalanced_open ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationStringUnbalancedOpenSliceViolation {
	void m() {
		List.of("a(b[c");
	}
}
// === end ===

// === case: text_block_continuation ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + List.of("a").toString();
	}
}
// === end ===

// === case: with_preceding_arguments ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationWithPrecedingArgumentsSliceViolation {
	void m() {
		"%s%s".formatted("a", "b");
	}
}
// === end ===