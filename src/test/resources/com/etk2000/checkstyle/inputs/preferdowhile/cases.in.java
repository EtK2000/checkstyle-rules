package com.etk2000.checkstyle.inputs.preferdowhile;

// === case: array_assign ===
class InputPreferDoWhileArrayAssignSliceViolation {
	void m(int[] arr, int i) {
		arr[i] = 0;
		while (i-- > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			arr[i] = 0;
	}
}
// === end ===

// === case: assignment_body ===
class InputPreferDoWhileAssignmentBodySliceViolation {
	void m(int i) {
		i = i + 1;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			i = i + 1;
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
		foo();
		while (cond()) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			foo();
	}
}
// === end ===

// === case: bare_method_call ===
class InputPreferDoWhileBareMethodCallSliceViolation {
	void m(int i) {
		System.out.println(i);
		while (--i > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			System.out.println(i);
	}
}
// === end ===

// === case: body_formatting_skipped ===
// skip-reason: body formatting
class InputPreferDoWhileBodyFormattingSkippedSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			x = a
				+ b;
	}
}
// === end ===

// === case: braced_body ===
class InputPreferDoWhileBracedBodySliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
		}
	}
}
// === end ===

// === case: braced_closing_indent_mismatch_skipped ===
// skip-reason: braced body multi-statement or unusual closing
class InputPreferDoWhileBracedClosingIndentMismatchSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
	}
}
// === end ===

// === case: braced_closing_trailing_content_skipped ===
// skip-reason: braced body multi-statement or unusual closing
class InputPreferDoWhileBracedClosingTrailingContentSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace pre-loop statement and 'while' with 'do-while'.
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
		node = node.next();
		while (node != null) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			node = node.next();
	}
}
// === end ===

// === case: comment_on_body_line_skipped ===
// skip-reason: comment on body line
class InputPreferDoWhileCommentOnBodyLineSkippedSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i; // comment
	}
}
// === end ===

// === case: comment_on_pre_line_skipped ===
// skip-reason: comment on pre-statement line
class InputPreferDoWhileCommentOnPreLineSkippedSliceViolation {
	void m(int i) {
		++i; // comment
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: compound_assign_body ===
class InputPreferDoWhileCompoundAssignBodySliceViolation {
	void m(int i) {
		i += 2;
		while (i < 100) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			i += 2;
	}
}
// === end ===

// === case: condition_with_nested_parens ===
class InputPreferDoWhileConditionWithNestedParensSliceViolation {
	boolean check(int i) {
		return true;
	}

	void m(int i) {
		++i;
		while ((i < 10) && check(i)) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: indent_mismatch_skipped ===
// skip-reason: pre-statement indent mismatch
class InputPreferDoWhileIndentMismatchSkippedSliceViolation {
	void m(int i) {
	++i;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: main ===
class InputPreferDoWhileViolation {
	void bracedBody(int i) {
		++i;
		while (i < 10) { // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
		}
	}

	void commentBetween(int i) {
		++i;
		// walking forward
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: method_call_body ===
// imports: java.util.List
class InputPreferDoWhileMethodCallBodySliceViolation {
	void m(List<Integer> list) {
		list.add(1);
		while (list.size() < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			list.add(1);
	}
}
// === end ===

// === case: nested_in_for_body ===
class InputPreferDoWhileNestedInForBodySliceViolation {
	void m(int n) {
		for (var k = 0; k < n; ++k) {
			var i = 0;
			++i;
			while (i < n) // violation: Replace pre-loop statement and 'while' with 'do-while'.
				++i;
		}
	}
}
// === end ===

// === case: pre_stmt_extra_whitespace_after_indent_skipped ===
// skip-reason: pre-statement formatting
class InputPreferDoWhilePreStmtExtraWhitespaceAfterIndentSkippedSliceViolation {
	void m(int i) {
		 	++i;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: prefix_decrement ===
class InputPreferDoWhilePrefixDecrementSliceViolation {
	void m(int i) {
		--i;
		while (i > 0) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			--i;
	}
}
// === end ===

// === case: prefix_inc ===
class InputPreferDoWhilePrefixIncSliceViolation {
	void m(int i, String text) {
		++i;
		while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: prefix_increment ===
class InputPreferDoWhilePrefixIncrementSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: textual_mismatch_whitespace_skipped ===
// skip-reason: textual mismatch between pre-statement and body
class InputPreferDoWhileTextualMismatchWhitespaceSkippedSliceViolation {
	void m(int i) {
		i +=2;
		while (i < 10) // violation: Replace pre-loop statement and 'while' with 'do-while'.
			i += 2;
	}
}
// === end ===

// === case: while_line_has_trailing_comment ===
// skip-reason: while line not in expected format (multi-line cond, trailing content, or comment)
class InputPreferDoWhileWhileLineHasTrailingCommentSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // loop // violation: Replace pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===