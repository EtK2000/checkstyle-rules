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

// === case: empty_array_two_preceding_args ===
@SuppressWarnings("unused")
class InputRedundantArrayCreationEmptyArrayTwoPrecedingArgsSliceViolation {
	String foo(int a, int b) {
		return a + "-" + b;
	}

	void m() {
		"%s".formatted(foo(1, 2));
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

// === case: lone_object_creation_element ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneObjectCreationElementSliceViolation {
	void m() {
		List.of(new StringBuilder("a"));
	}
}
// === end ===

// === case: lone_provable_element_forms ===
// multi-fix-expected
@SuppressWarnings("unused")
class InputRedundantArrayCreationLoneProvableElementFormsSliceViolation {
	void booleans() {
		"%b".formatted(true);
		"%b".formatted(false);
	}

	void numbers() {
		"%d".formatted(1);
		"%d".formatted(1L);
		"%s".formatted(1.5f);
		"%s".formatted('c');
	}

	void operators(int a, int b) {
		"%d".formatted(a - b);
		"%d".formatted(a * b);
		"%d".formatted(a / b);
		"%d".formatted(a % b);
		"%d".formatted(-a);
		"%d".formatted(a & b);
		"%d".formatted(a << b);
		"%b".formatted(a > b);
		"%b".formatted(a == b);
	}

	void self() {
		"%s".formatted(this);
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

// === case: multiline_block_comment_inside_initializer ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineBlockCommentInsideInitializerSliceViolation {
	void m() {
		List.of(new Object[]{
			/* note */ "a", "b"
		});
	}
}
// === end ===

// === case: multiline_brace_on_next_line ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineBraceOnNextLineSliceViolation {
	void m() {
		List.of("a", "b");
	}
}
// === end ===

// === case: multiline_comment_inside_initializer ===
// skip-reason: cannot remove the array creation: a comment inside it would be lost when the initializer is folded onto one line
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineCommentInsideInitializerSliceViolation {
	void m() {
		List.of(new Object[]{
			// note
			"a", "b"
		});
	}
}
// === end ===

// === case: multiline_fold_at_line_budget ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineFoldAtLineBudgetSliceViolation {
	void m() {
		List.of("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
	}
}
// === end ===

// === case: multiline_fold_over_line_budget ===
// skip-reason: cannot remove the array creation: folding the initializer onto one line would exceed the line-length budget
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineFoldOverLineBudgetSliceViolation {
	void m() {
		List.of(new Object[]{
			"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
			"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
			"cccccccccccccccccccccccccccccccccccccccc"
		});
	}
}
// === end ===

// === case: multiline_initializer_body ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineInitializerBodySliceViolation {
	void m() {
		List.of("a", "b");
	}
}
// === end ===

// === case: multiline_trailing_comma ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationMultilineTrailingCommaSliceViolation {
	void m() {
		List.of("a", "b");
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
		"%s%s".formatted(foo(1, 2), "x");
	}
}
// === end ===

// === case: nested_violation_inner_and_outer ===
// multi-fix-expected
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationNestedViolationInnerAndOuterSliceViolation {
	void m() {
		List.of(String.join(",", "a"), "b");
	}
}
// === end ===

// === case: quote_char_and_block_marker_elements ===
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationQuoteCharAndBlockMarkerElementsSliceViolation {
	void m() {
		List.of('"', "/*");
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

// === case: text_block_element_skipped ===
// skip-reason: cannot remove the array creation: folding the initializer onto one line would break a text block inside it
// imports: java.util.List
@SuppressWarnings("unused")
class InputRedundantArrayCreationTextBlockElementSkippedSliceViolation {
	void m() {
		List.of(new Object[]{
			"""
			a
			"""
		});
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