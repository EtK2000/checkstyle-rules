package com.etk2000.checkstyle.inputs.preferdowhile;

// === case: array_assign ===
class InputPreferDoWhileArrayAssignSliceViolation {
	void m(int[] arr, int i) {
		do arr[i] = 0;
		while (i-- > 0);
	}
}
// === end ===

// === case: assignment_body ===
class InputPreferDoWhileAssignmentBodySliceViolation {
	void m(int i) {
		do
			i = i + 1;
		while (i < 10);
	}
}
// === end ===

// === case: bare_call_no_args ===
class InputPreferDoWhileBareCallNoArgsSliceViolation {
	boolean cond() {
		return false;
	}

	void foo() {
	}

	void m() {
		do foo();
		while (cond());
	}
}
// === end ===

// === case: bare_method_call ===
class InputPreferDoWhileBareMethodCallSliceViolation {
	void m(int i) {
		do System.out.println(i);
		while (--i > 0);
	}
}
// === end ===

// === case: body_formatting_skipped ===
// skip-reason: cannot convert to do-while: the loop body does not end in a semicolon
class InputPreferDoWhileBodyFormattingSkippedSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10)
			x = a
				+ b;
	}
}
// === end ===

// === case: braced_body ===
class InputPreferDoWhileBracedBodySliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: braced_closing_indent_mismatch_skipped ===
// skip-reason: cannot convert to do-while: the braced body has more than one statement or an unusual closing brace
class InputPreferDoWhileBracedClosingIndentMismatchSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) {
			++i;
	}
	}
}
// === end ===

// === case: braced_closing_trailing_content_skipped ===
// skip-reason: cannot convert to do-while: the braced body has more than one statement or an unusual closing brace
class InputPreferDoWhileBracedClosingTrailingContentSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) {
			++i;
		} // done
	}
}
// === end ===

// === case: chained_assignment_body ===
class InputPreferDoWhileChainedAssignmentBodySliceViolation {
	static class Node {
		Node next() {
			return null;
		}
	}

	void m(Node node) {
		do node = node.next();
		while (node != null);
	}
}
// === end ===

// === case: chained_call_body ===
class InputPreferDoWhileChainedCallBodySliceViolation {
	void m(StringBuilder sb, int i) {
		do
			sb.append("a").append("b");
		while (--i > 0);
	}
}
// === end ===

// === case: comment_on_body_line_skipped ===
// skip-reason: cannot convert to do-while: the loop body line has a comment
class InputPreferDoWhileCommentOnBodyLineSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10)
			++i; // comment
	}
}
// === end ===

// === case: comment_on_pre_line_skipped ===
// skip-reason: cannot convert to do-while: the statement before the loop has a comment
class InputPreferDoWhileCommentOnPreLineSkippedSliceViolation {
	void m(int i) {
		++i; // comment
		while (i < 10)
			++i;
	}
}
// === end ===

// === case: compound_assign_body ===
class InputPreferDoWhileCompoundAssignBodySliceViolation {
	void m(int i) {
		do i += 2;
		while (i < 100);
	}
}
// === end ===

// === case: condition_with_nested_parens ===
class InputPreferDoWhileConditionWithNestedParensSliceViolation {
	boolean check(int i) {
		return true;
	}

	void m(int i) {
		do ++i;
		while ((i < 10) && check(i));
	}
}
// === end ===

// === case: indent_mismatch_skipped ===
// skip-reason: cannot convert to do-while: the statement before the loop is not at the loop's indent
class InputPreferDoWhileIndentMismatchSkippedSliceViolation {
	void m(int i) {
	++i;
		while (i < 10)
			++i;
	}
}
// === end ===

// === case: main ===
class InputPreferDoWhileViolation {
	void bracedBody(int i) {
		do ++i;
		while (i < 10);
	}

	void commentBetween(int i) {
		++i;
		// walking forward
		while (i < 10)
			++i;
	}
}
// === end ===

// === case: method_call_body ===
// imports: java.util.List
class InputPreferDoWhileMethodCallBodySliceViolation {
	void m(List<Integer> list) {
		do list.add(1);
		while (list.size() < 10);
	}
}
// === end ===

// === case: nested_in_for_body ===
class InputPreferDoWhileNestedInForBodySliceViolation {
	void m(int n) {
		for (var k = 0; k < n; ++k) {
			var i = 0;
			do ++i;
			while (i < n);
		}
	}
}
// === end ===

// === case: new_expression_body ===
class InputPreferDoWhileNewExpressionBodySliceViolation {
	void m(int i) {
		StringBuilder sb;
		do
			sb = new StringBuilder();
		while (--i > 0);
	}
}
// === end ===

// === case: pre_stmt_extra_whitespace_after_indent_skipped ===
// skip-reason: cannot convert to do-while: the statement before the loop is not a single statement ending in a semicolon
class InputPreferDoWhilePreStmtExtraWhitespaceAfterIndentSkippedSliceViolation {
	void m(int i) {
		 	++i;
		while (i < 10)
			++i;
	}
}
// === end ===

// === case: prefix_decrement ===
class InputPreferDoWhilePrefixDecrementSliceViolation {
	void m(int i) {
		do --i;
		while (i > 0);
	}
}
// === end ===

// === case: prefix_inc ===
class InputPreferDoWhilePrefixIncSliceViolation {
	void m(int i, String text) {
		do ++i;
		while (i < text.length() && Character.isLetterOrDigit(text.charAt(i)));
	}
}
// === end ===

// === case: prefix_increment ===
class InputPreferDoWhilePrefixIncrementSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: textual_mismatch_whitespace_skipped ===
// skip-reason: cannot convert to do-while: the loop body does not repeat the statement before the loop
class InputPreferDoWhileTextualMismatchWhitespaceSkippedSliceViolation {
	void m(int i) {
		i +=2;
		while (i < 10)
			i += 2;
	}
}
// === end ===

// === case: while_line_has_trailing_comment ===
// skip-reason: cannot convert to do-while: the while line has a multi-line condition, trailing content or a comment
class InputPreferDoWhileWhileLineHasTrailingCommentSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // loop
			++i;
	}
}
// === end ===