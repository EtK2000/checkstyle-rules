package com.etk2000.checkstyle.inputs.controlflowbraces;

// === case: brace_on_own_line_do_body_on_brace_line ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineDoBodyOnBraceLineSliceViolation {
	void m(int x) {
		do { --x;
		} while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_comment_on_brace_line ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineDoCommentOnBraceLineSliceViolation {
	void m(int x) {
		do {
			/* note */
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_with_comment_on_do_and_body ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineDoWithCommentOnDoAndBodySliceViolation {
	void m(int x) {
		do { // note
			--x; // pending
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_with_comment_on_do_and_brace ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineDoWithCommentOnDoAndBraceSliceViolation {
	void m(int x) {
		do { // note
			// brace note
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_if_close_brace_after_text_block ===
// skip-reason: cannot reshape the body: the block does not end where the body does
class InputControlFlowBracesBraceOnOwnLineIfCloseBraceAfterTextBlockSliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) {
			s = """
} """; }
		System.out.println(s);
	}
}
// === end ===

// === case: brace_on_own_line_if_comment_before_close_brace ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineIfCommentBeforeCloseBraceSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
		/* trailing note */ }
	}
}
// === end ===

// === case: brace_on_own_line_if_comment_on_brace_line ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineIfCommentOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* note */
			--x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_with_block_comment_on_brace ===
// skip-reason: cannot remove the braces: other content shares the brace line
class InputControlFlowBracesBraceOnOwnLineWithBlockCommentOnBraceSliceViolation {
	void m(int x) {
		if (x > 0) { /* important */
			--x;
		}
	}
}
// === end ===

// === case: braced_tier3_this_chained_call ===
class InputControlFlowBracesBracedTier3ThisChainedCallSliceViolation {
	private void chain() { }

	private InputControlFlowBracesBracedTier3ThisChainedCallSliceViolation helper() {
		return this;
	}

	void m(int x) {
		do
			helper().chain();
		while (x > 0);
	}
}
// === end ===

// === case: else_if_split_cuddled_comment_header_opens_block_comment ===
// skip-reason: cannot reshape the body: a block comment or text block does not close on the line it opens
class InputControlFlowBracesElseIfSplitCuddledCommentHeaderOpensBlockCommentSliceViolation {
	void m(boolean a, boolean b) {
		if (a) {
			System.out.println(a);
			System.out.println(b);
		} else // note
			if (b) { /* c
				d */
				System.out.println(a);
				System.out.println(b);
			}
	}
}
// === end ===

// === case: else_if_split_inner_allman ===
class InputControlFlowBracesElseIfSplitInnerAllmanSliceViolation {
	void m(boolean a, boolean b) {
		if (a)
			System.out.println(a);
		else if (b) {
			System.out.println(a);
			System.out.println(b);
		}
	}
}
// === end ===

// === case: missing_braces_else_with_for ===
class InputControlFlowBracesMissingBracesElseWithForSliceViolation {
	void m(int x) {
		if (x > 0)
			System.out.println("positive");
		else {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_for_with_braced_switch ===
class InputControlFlowBracesMissingBracesForWithBracedSwitchSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) {
			switch (i) {
				case 0 -> System.out.println(i);
				default -> System.out.println(-i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_for_with_braced_try_finally ===
class InputControlFlowBracesMissingBracesForWithBracedTryFinallySliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) {
			try {
				System.out.println(i);
				System.out.println(-i);
			}
			finally {
				System.out.println(x);
				System.out.println(i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_for_with_if ===
class InputControlFlowBracesMissingBracesForWithIfSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) {
			if (i > 0)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_compact_multi_line_body ===
class InputControlFlowBracesMissingBracesIfCompactMultiLineBodySliceViolation {
	void m(int x) {
		if(x > 0) {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_with_block_comment_containing_slash_slash ===
class InputControlFlowBracesMissingBracesIfWithBlockCommentContainingSlashSlashSliceViolation {
	void m(int x) {
		if (x > 0) /* contains // tricky */ {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_with_block_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithBlockCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) {
			/* note */ for (var i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_braced_inner_then_trailing_stmt ===
class InputControlFlowBracesMissingBracesIfWithBracedInnerThenTrailingStmtSliceViolation {
	void m(int x) {
		if (x > 0) {
			for (var i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) {
			// leading comment before the inner statement
			for (var i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_for ===
class InputControlFlowBracesMissingBracesIfWithForSliceViolation {
	void m(int x) {
		if (x > 0) {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_with_multiline_block_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithMultilineBlockCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) {
			/*
			 * note line one
			 * note line two
			 */
			for (var i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_block_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* guard */ {
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_block_then_line_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingBlockThenLineCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* note */ { // guard
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingCommentSliceViolation {
	void m(int x) {
		if (x > 0) { // guard
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		}
	}
}
// === end ===

// === case: nested_inner_for_with_if ===
class InputControlFlowBracesNestedInnerForWithIfSliceViolation {
	void m(int x) {
		if (x > 0) {
			for (var i = 0; i < x; ++i) {
				if (i > 0)
					System.out.println(i);
			}
		}
	}
}
// === end ===

// === case: non_do_while_one_liners ===
// imports: java.util.List
class InputControlFlowBracesNonDoWhileOneLinersSliceViolation {
	void elseOneLiner(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			System.out.println("negative");
	}

	void method(int x) {
		if (x > 0)
			System.out.println("positive");
		while (x > 0)
			--x;
		for (var i = 0; i < x; ++i)
			System.out.println(i);
		final var list = List.of("a");
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===

// === case: on_do_line_false_while_in_body ===
class InputControlFlowBracesOnDoLineFalseWhileInBodySliceViolation {
	void m() {
		final var whileTimer = 5;
		var whileVar = 0;
		do ++whileVar;
		while (whileTimer > 0);
	}
}
// === end ===

// === case: one_liner_for_loop ===
class InputControlFlowBracesOneLinerForLoopSliceViolation {
	void m() {
		for (var i = 0; i < 10; ++i)
			System.out.println(i);
	}
}
// === end ===

// === case: unnecessary_braces_for_loops ===
// imports: java.util.List
class InputControlFlowBracesUnnecessaryBracesForLoopsSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i)
			System.out.println(i);

		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
	}
}
// === end ===