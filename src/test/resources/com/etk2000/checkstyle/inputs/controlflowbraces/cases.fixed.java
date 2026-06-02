package com.etk2000.checkstyle.inputs.controlflowbraces;

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