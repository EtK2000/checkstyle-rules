package com.etk2000.checkstyle.inputs.controlflowbraces;

// === case: brace_on_own_line_do_block_comment_before_brace ===
class InputControlFlowBracesBraceOnOwnLineDoBlockCommentBeforeBraceSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
			/* multi
			   line */
		{
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_block_comment_blank_line_before_brace ===
class InputControlFlowBracesBraceOnOwnLineDoBlockCommentBlankLineBeforeBraceSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
			/* multi

			   line */
		{
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_body_on_brace_line ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineDoBodyOnBraceLineSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
		{ --x;
		} while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_close_brace_shares_while ===
class InputControlFlowBracesBraceOnOwnLineDoCloseBraceSharesWhileSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		} while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_comment_before_brace ===
class InputControlFlowBracesBraceOnOwnLineDoCommentBeforeBraceSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
			// note
		{
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_comment_on_brace_line ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineDoCommentOnBraceLineSliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
		/* note */ {
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_tier2 ===
class InputControlFlowBracesBraceOnOwnLineDoTier2SliceViolation {
	void m(int x) {
		do // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_tier3 ===
class InputControlFlowBracesBraceOnOwnLineDoTier3SliceViolation {
	void m(int x, int y) {
		do // violation: Remove unnecessary braces from single-line control flow body.
		{
			x = x + y;
		}
		while (x < 100);
	}
}
// === end ===

// === case: brace_on_own_line_do_tier3_with_comment_on_do ===
class InputControlFlowBracesBraceOnOwnLineDoTier3WithCommentOnDoSliceViolation {
	void m(int x, int y) {
		do // note // violation: Remove unnecessary braces from single-line control flow body.
		{
			x = x + y;
		}
		while (x < 100);
	}
}
// === end ===

// === case: brace_on_own_line_do_with_comment_on_do ===
class InputControlFlowBracesBraceOnOwnLineDoWithCommentOnDoSliceViolation {
	void m(int x) {
		do // note // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_with_comment_on_do_and_body ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineDoWithCommentOnDoAndBodySliceViolation {
	void m(int x) {
		do // note // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x; // pending
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_do_with_comment_on_do_and_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineDoWithCommentOnDoAndBraceSliceViolation {
	void m(int x) {
		do // note // violation: Remove unnecessary braces from single-line control flow body.
		{ // brace note
			--x;
		}
		while (x > 0);
	}
}
// === end ===

// === case: brace_on_own_line_else ===
class InputControlFlowBracesBraceOnOwnLineElseSliceViolation {
	void m(int x) {
		if (x > 0)
			--x;
		else // violation: Remove unnecessary braces from single-line control flow body.
		{
			++x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_for ===
class InputControlFlowBracesBraceOnOwnLineForSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) // violation: Remove unnecessary braces from single-line control flow body.
		{
			System.out.println(i);
		}
	}
}
// === end ===

// === case: brace_on_own_line_for_each ===
// imports: java.util.List
class InputControlFlowBracesBraceOnOwnLineForEachSliceViolation {
	void m(List<String> list) {
		for (var item : list) // violation: Remove unnecessary braces from single-line control flow body.
		{
			System.out.println(item);
		}
	}
}
// === end ===

// === case: brace_on_own_line_if ===
class InputControlFlowBracesBraceOnOwnLineIfSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_if_close_brace_after_text_block ===
// skip-reason: the block does not end where the body does
class InputControlFlowBracesBraceOnOwnLineIfCloseBraceAfterTextBlockSliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{
			s = """
} """; }
		System.out.println(s);
	}
}
// === end ===

// === case: brace_on_own_line_if_comment_before_brace ===
class InputControlFlowBracesBraceOnOwnLineIfCommentBeforeBraceSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
			// only when positive
		{
			--x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_if_comment_before_close_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineIfCommentBeforeCloseBraceSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		/* trailing note */ }
	}
}
// === end ===

// === case: brace_on_own_line_if_comment_on_brace_line ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineIfCommentOnBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		/* note */ {
			--x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_if_with_else ===
class InputControlFlowBracesBraceOnOwnLineIfWithElseSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		} else {
			++x;
			System.out.println(x);
		}
	}
}
// === end ===

// === case: brace_on_own_line_while ===
class InputControlFlowBracesBraceOnOwnLineWhileSliceViolation {
	void m(int x) {
		while (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{
			--x;
		}
	}
}
// === end ===

// === case: brace_on_own_line_with_block_comment_on_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBraceOnOwnLineWithBlockCommentOnBraceSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Remove unnecessary braces from single-line control flow body.
		{ /* important */
			--x;
		}
	}
}
// === end ===

// === case: braced_body_block_comment_before_while ===
class InputControlFlowBracesBracedBodyBlockCommentBeforeWhileSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} /* loop while positive */ while (x > 0);
	}
}
// === end ===

// === case: braced_body_block_comment_opens_on_close_brace_line ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesBracedBodyBlockCommentOpensOnCloseBraceLineSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} /* multi
		     line */
		while (x > 0);
	}
}
// === end ===

// === case: braced_body_block_comment_opens_on_do_brace_line ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesBracedBodyBlockCommentOpensOnDoBraceLineSliceViolation {
	void m(int x) {
		do { /* multi // violation: Remove unnecessary braces from single-line control flow body.
			line */ --x;
		} while (x > 0);
	}
}
// === end ===

// === case: braced_body_comment_before_close_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBracedBodyCommentBeforeCloseBraceSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		/* trailing note */ } while (x > 0);
	}
}
// === end ===

// === case: braced_body_containing_char_with_comment_delimiters ===
class InputControlFlowBracesBracedBodyContainingCharWithCommentDelimitersSliceViolation {
	void m(int[] arr, int i, boolean cond) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			arr[i] = '/';
		} while (cond);
	}
}
// === end ===

// === case: braced_body_containing_string_with_line_comment_delimiters ===
class InputControlFlowBracesBracedBodyContainingStringWithLineCommentDelimitersSliceViolation {
	void m(String s, boolean cond) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			s = "// not a comment";
		} while (cond);
	}
}
// === end ===

// === case: braced_body_no_space_after_do ===
class InputControlFlowBracesBracedBodyNoSpaceAfterDoSliceViolation {
	void m(int x) {
		do{ // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}while (x > 0);
	}
}
// === end ===

// === case: braced_body_on_open_brace_line ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesBracedBodyOnOpenBraceLineSliceViolation {
	void m(int x) {
		if (x > 0) { --x; // violation: Remove unnecessary braces from single-line control flow body.
		}
	}
}
// === end ===

// === case: braced_body_while_word_in_close_line_comment ===
class InputControlFlowBracesBracedBodyWhileWordInCloseLineCommentSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} // meanwhile, keep counting
		while (x > 0);
	}
}
// === end ===

// === case: braced_body_with_comment_line_before_while ===
class InputControlFlowBracesBracedBodyWithCommentLineBeforeWhileSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
		// count down
		while (x > 0);
	}
}
// === end ===

// === case: braced_body_with_comment_on_do_brace ===
class InputControlFlowBracesBracedBodyWithCommentOnDoBraceSliceViolation {
	void m(int x) {
		do { // guard // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} while (x > 0);
	}
}
// === end ===

// === case: braced_body_with_line_comment_inside_block ===
class InputControlFlowBracesBracedBodyWithLineCommentInsideBlockSliceViolation {
	void m(int x, boolean cond) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			/* contains // tricky */ x = 5;
		} while (cond);
	}
}
// === end ===

// === case: braced_comment_bracketed_code ===
class InputControlFlowBracesBracedCommentBracketedCodeSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			/* pre */ x = 5; /* post */
		} while (x > 0);
	}
}
// === end ===

// === case: braced_simple_bare_method_call ===
class InputControlFlowBracesBracedSimpleBareMethodCallSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			next();
		} while (x > 0);
	}

	private void next() { }
}
// === end ===

// === case: braced_statement_with_line_comment_containing_block_opener ===
class InputControlFlowBracesBracedStatementWithLineCommentContainingBlockOpenerSliceViolation {
	void m(int x, boolean cond) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x; // contains /* note
		} while (cond);
	}
}
// === end ===

// === case: braced_statement_with_trailing_comment ===
class InputControlFlowBracesBracedStatementWithTrailingCommentSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x; // pending
		} while (x > 0);
	}
}
// === end ===

// === case: braced_statement_with_trailing_comment_while_on_separate_line ===
class InputControlFlowBracesBracedStatementWithTrailingCommentWhileOnSeparateLineSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x; // pending
		}
		while (x > 0);
	}
}
// === end ===

// === case: braced_tab_before_brace ===
class InputControlFlowBracesBracedTabBeforeBraceSliceViolation {
	void m(int x) {
		do	{ // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} while (x > 0);
	}
}
// === end ===

// === case: braced_tier2_dotted_method_call ===
// imports: java.util.List
class InputControlFlowBracesBracedTier2DottedMethodCallSliceViolation {
	private boolean hasNext() {
		return false;
	}

	void m() {
		final var list = List.of("a");
		final var item = "b";
		do { // violation: Remove unnecessary braces from single-line control flow body.
			list.add(item);
		} while (hasNext());
	}
}
// === end ===

// === case: braced_tier3_chained_call ===
// imports: java.util.List
class InputControlFlowBracesBracedTier3ChainedCallSliceViolation {
	void m(int x) {
		final var list = List.of("a");
		do { // violation: Remove unnecessary braces from single-line control flow body.
			list.stream().close();
		} while (x > 0);
	}
}
// === end ===

// === case: braced_tier3_complex_rhs ===
class InputControlFlowBracesBracedTier3ComplexRhsSliceViolation {
	void m(int x, int y) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			x += 5 * y;
		} while (x < 100);
	}
}
// === end ===

// === case: braced_tier3_new_object ===
class InputControlFlowBracesBracedTier3NewObjectSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			new Object();
		} while (x > 0);
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
		do { // violation: Remove unnecessary braces from single-line control flow body.
			this.helper().chain();
		} while (x > 0);
	}
}
// === end ===

// === case: braced_while_on_separate_line ===
class InputControlFlowBracesBracedWhileOnSeparateLineSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			++x;
		}
		while (x < 10);
	}
}
// === end ===

// === case: deep_indent ===
class InputControlFlowBracesDeepIndentSliceViolation {
	void m(int x) {
		if (true) {
			if (true) {
				do { // violation: Remove unnecessary braces from single-line control flow body.
					--x;
				} while (x > 0);
			}
		}
	}
}
// === end ===

// === case: do_while_tier2_assign_unary_minus_rhs ===
class InputControlFlowBracesDoWhileTier2AssignUnaryMinusRhsSliceViolation {
	void m(int x) {
		do x = -x; while (x > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: do_while_tier2_while_same_line_list_add ===
// imports: java.util.List
class InputControlFlowBracesDoWhileTier2WhileSameLineListAddSliceViolation {
	void m() {
		final var list = List.of("a");
		do list.add("b"); while (list.size() < 10); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: do_while_tier2_while_same_line_simple ===
class InputControlFlowBracesDoWhileTier2WhileSameLineSimpleSliceViolation {
	void m(int x) {
		do --x; while (x > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: do_while_tier3_as_tier2_complex_rhs ===
class InputControlFlowBracesDoWhileTier3AsTier2ComplexRhsSliceViolation {
	void m(int x, int y) {
		do x += 5 * y; // violation: Move control flow body to its own line (one-liners not allowed).
		while (x < 100);
	}
}
// === end ===

// === case: do_while_tier3_as_tier2_dotted_chain ===
// imports: java.util.List
class InputControlFlowBracesDoWhileTier3AsTier2DottedChainSliceViolation {
	void m() {
		final var list = List.of("a");
		do list.subList(0, 1).clear(); // violation: Move control flow body to its own line (one-liners not allowed).
		while (!list.isEmpty());
	}
}
// === end ===

// === case: do_while_tier3_assign_chained_rhs ===
// imports: java.util.List
class InputControlFlowBracesDoWhileTier3AssignChainedRhsSliceViolation {
	void m(int x) {
		final var list = List.of("a");
		do x = list.subList(0, 1).size(); while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_assign_new_rhs ===
class InputControlFlowBracesDoWhileTier3AssignNewRhsSliceViolation {
	void m(int x, Object o) {
		do o = new Object(); while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_assign ===
class InputControlFlowBracesDoWhileTier3OneLinerAssignSliceViolation {
	void m(int x, int y) {
		do x = x + y; while (x < 100); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_for ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedForSliceViolation {
	void m(int x) {
		do for (;;) { break; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_if ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedIfSliceViolation {
	void m(int x) {
		do if (x > 5) { --x; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_if_block_comment_brace ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedIfBlockCommentBraceSliceViolation {
	void m(int x) {
		do if (x > 5) { --x; } /* } */ while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_if_trailing_comment ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedIfTrailingCommentSliceViolation {
	void m(int x) {
		do if (x > 5) { --x; } // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 0);
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_switch ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedSwitchSliceViolation {
	void m(int x) {
		do switch (x) { default -> --x; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_synchronized ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedSynchronizedSliceViolation {
	void m(int x) {
		final var lock = new Object();
		do synchronized (lock) { --x; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_try_with_resources ===
// imports: java.io.StringReader
class InputControlFlowBracesDoWhileTier3OneLinerBracedTryWithResourcesSliceViolation {
	void m(int x) {
		do try (var r = new StringReader("a")) { --x; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_braced_while ===
class InputControlFlowBracesDoWhileTier3OneLinerBracedWhileSliceViolation {
	void m(int x) {
		do while (x > 5) { --x; } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_dotted_chain ===
// imports: java.util.List
class InputControlFlowBracesDoWhileTier3OneLinerDottedChainSliceViolation {
	void m(int x) {
		final var list = List.of("a");
		do list.subList(0, 1).clear(); while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_one_liner_new ===
class InputControlFlowBracesDoWhileTier3OneLinerNewSliceViolation {
	void m(int x) {
		do new Object(); while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: do_while_tier3_while_same_line_chained_call ===
class InputControlFlowBracesDoWhileTier3WhileSameLineChainedCallSliceViolation {
	void m(int x) {
		do // violation: Do-while while clause must be on its own line.
			x = text().trim().length(); while (x > 0);
	}

	private String text() {
		return " a ";
	}
}
// === end ===

// === case: do_while_tier3_while_same_line_complex_rhs ===
class InputControlFlowBracesDoWhileTier3WhileSameLineComplexRhsSliceViolation {
	void m(int x, int y) {
		do // violation: Do-while while clause must be on its own line.
			x = x + y; while (x < 100);
	}
}
// === end ===

// === case: missing_braces_control_body_then_catch_prefixed_identifier ===
class InputControlFlowBracesMissingBracesControlBodyThenCatchPrefixedIdentifierSliceViolation {
	int catchCount;

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			}
		catchCount = 1;
	}
}
// === end ===

// === case: missing_braces_control_body_then_else_prefixed_identifier ===
class InputControlFlowBracesMissingBracesControlBodyThenElsePrefixedIdentifierSliceViolation {
	boolean elseFlag;

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			}
		elseFlag = true;
	}
}
// === end ===

// === case: missing_braces_control_body_then_else_supplementary_identifier ===
class InputControlFlowBracesMissingBracesControlBodyThenElseSupplementaryIdentifierSliceViolation {
	boolean else𝐀;

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			}
		else𝐀 = true;
	}
}
// === end ===

// === case: missing_braces_control_body_then_finally_prefixed_identifier ===
class InputControlFlowBracesMissingBracesControlBodyThenFinallyPrefixedIdentifierSliceViolation {
	boolean finallyRan;

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			}
		finallyRan = true;
	}
}
// === end ===

// === case: missing_braces_control_body_with_array_init ===
class InputControlFlowBracesMissingBracesControlBodyWithArrayInitSliceViolation {
	void m(boolean cond, int[] arr) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (arr == null)
				arr = new int[]{1, 2};
		System.out.println(arr);
	}
}
// === end ===

// === case: missing_braces_control_body_with_multiline_anon_class ===
class InputControlFlowBracesMissingBracesControlBodyWithMultilineAnonClassSliceViolation {
	private abstract static class Task {
		abstract void go();

		abstract void stop();
	}

	private static void submit(Task task) {
		task.go();
	}

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			submit(new Task() {
				@Override
				void go() {
					System.out.println(x);
				}

				@Override
				void stop() {
					System.out.println(-x);
				}
			});
		System.out.println(x);
	}
}
// === end ===

// === case: missing_braces_control_body_with_multiline_lambda ===
class InputControlFlowBracesMissingBracesControlBodyWithMultilineLambdaSliceViolation {
	private static void run(Runnable action) {
		action.run();
	}

	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			run(() -> {
				System.out.println(x);
				System.out.println(-x);
			});
		System.out.println(x);
	}
}
// === end ===

// === case: missing_braces_control_body_with_split_block_comment ===
class InputControlFlowBracesMissingBracesControlBodyWithSplitBlockCommentSliceViolation {
	int x;

	void m(boolean cond) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			x = /* multi
				line */ x + 1;
	}
}
// === end ===

// === case: missing_braces_do_body_with_cuddled_while ===
class InputControlFlowBracesMissingBracesDoBodyWithCuddledWhileSliceViolation {
	void m(int x, int z) {
		do // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0)
				--x;
			else
				--z; while (x > 0);
		while (z > 0)
			--z;
	}
}
// === end ===

// === case: missing_braces_do_body_with_dotted_continuation ===
// imports: java.util.List
class InputControlFlowBracesMissingBracesDoBodyWithDottedContinuationSliceViolation {
	void m(List<Integer> list) {
		do // violation: Braceless control flow has multi-line body, add braces.
			list
					.add(1);
		while (list.size() < 10);
	}
}
// === end ===

// === case: missing_braces_do_body_with_inner_while ===
class InputControlFlowBracesMissingBracesDoBodyWithInnerWhileSliceViolation {
	void m(int x, boolean inner) {
		do // violation: Braceless control flow has multi-line body, add braces.
			while (inner)
				--x;
		while (x > 0);
	}
}
// === end ===

// === case: missing_braces_do_body_with_multiline_block_comment ===
class InputControlFlowBracesMissingBracesDoBodyWithMultilineBlockCommentSliceViolation {
	void m(int x) {
		do // violation: Braceless control flow has multi-line body, add braces.
			x = /* multi
				line */ x + 1;
		while (x > 0);
	}
}
// === end ===

// === case: missing_braces_do_with_if ===
class InputControlFlowBracesMissingBracesDoWithIfSliceViolation {
	void m(int x) {
		do // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0)
				--x;
		while (x > 0);
	}
}
// === end ===

// === case: missing_braces_else_with_for ===
class InputControlFlowBracesMissingBracesElseWithForSliceViolation {
	void m(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_for_with_braced_switch ===
class InputControlFlowBracesMissingBracesForWithBracedSwitchSliceViolation {
	void m(int x) {
		for (int i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			switch (i) {
				case 0 -> System.out.println(i);
				default -> System.out.println(-i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_for_with_braced_try_finally ===
class InputControlFlowBracesMissingBracesForWithBracedTryFinallySliceViolation {
	void m(int x) {
		for (int i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			try {
				System.out.println(i);
				System.out.println(-i);
			}
			finally {
				System.out.println(x);
				System.out.println(i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_for_with_if ===
class InputControlFlowBracesMissingBracesForWithIfSliceViolation {
	void m(int x) {
		for (int i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 0)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_for_with_if_braced_else ===
class InputControlFlowBracesMissingBracesForWithIfBracedElseSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 0)
				System.out.println(i);
			else {
				System.out.println(-i);
				System.out.println(0);
			}
	}
}
// === end ===

// === case: missing_braces_for_with_if_else ===
class InputControlFlowBracesMissingBracesForWithIfElseSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 0)
				System.out.println(i);
			else
				System.out.println(-i);
	}
}
// === end ===

// === case: missing_braces_for_with_if_else_if_chain ===
class InputControlFlowBracesMissingBracesForWithIfElseIfChainSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 5)
				System.out.println("big");
			else if (i > 0)
				System.out.println("small");
			else
				System.out.println("zero");
	}
}
// === end ===

// === case: missing_braces_for_with_nested_if_else ===
// multi-fix-expected
class InputControlFlowBracesMissingBracesForWithNestedIfElseSliceViolation {
	void m(int x) {
		for (var i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
			if (i > 5) // violation: Braceless control flow has multi-line body, add braces.
				if (i > 10)
					System.out.println("huge");
				else
					System.out.println("big");
			else
				System.out.println("small");
	}
}
// === end ===

// === case: missing_braces_foreach_with_if ===
// imports: java.util.List
class InputControlFlowBracesMissingBracesForeachWithIfSliceViolation {
	void m() {
		final var list = List.of("a");
		for (var item : list) // violation: Braceless control flow has multi-line body, add braces.
			if (item != null)
				System.out.println(item);
	}
}
// === end ===

// === case: missing_braces_foreach_with_if_else ===
// imports: java.util.List
class InputControlFlowBracesMissingBracesForeachWithIfElseSliceViolation {
	void m(List<String> list) {
		for (var item : list) // violation: Braceless control flow has multi-line body, add braces.
			if (item != null)
				System.out.println(item);
			else
				System.out.println("null");
	}
}
// === end ===

// === case: missing_braces_if_compact_multi_line_body ===
class InputControlFlowBracesMissingBracesIfCompactMultiLineBodySliceViolation {
	void m(int x) {
		if(x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_if_for_body_with_outer_else ===
class InputControlFlowBracesMissingBracesIfForBodyWithOuterElseSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (var i = 0; i < x; ++i)
				System.out.println(i);
		else
			System.out.println("done");
	}
}
// === end ===

// === case: missing_braces_if_keyword_line_unterminated_block_comment ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesMissingBracesIfKeywordLineUnterminatedBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* multi // violation: Braceless control flow has multi-line body, add braces.
		   line */
			if (x > 5)
				--x;
	}
}
// === end ===

// === case: missing_braces_if_with_block_comment_after_body_terminator ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesMissingBracesIfWithBlockCommentAfterBodyTerminatorSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x; /* multi
				        line */
	}
}
// === end ===

// === case: missing_braces_if_with_block_comment_containing_slash_slash ===
class InputControlFlowBracesMissingBracesIfWithBlockCommentContainingSlashSlashSliceViolation {
	void m(int x) {
		if (x > 0) /* contains // tricky */ // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_if_with_block_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithBlockCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			/* note */ for (int i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_braced_inner_then_trailing_stmt ===
class InputControlFlowBracesMissingBracesIfWithBracedInnerThenTrailingStmtSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_braced_while_then_trailing_stmt ===
class InputControlFlowBracesMissingBracesIfWithBracedWhileThenTrailingStmtSliceViolation {
	void m(boolean cond, int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			while (cond) {
				System.out.println(x);
				System.out.println(-x);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_braceless_do_then_else ===
// multi-fix-expected
class InputControlFlowBracesMissingBracesIfWithBracelessDoThenElseSliceViolation {
	void m(boolean c, int p, int q) {
		if (p > 0) // violation: Braceless control flow has multi-line body, add braces.
			do // violation: Braceless control flow has multi-line body, add braces.
				if (q > 0)
					--q;
			while (c);
		else
			++p;
	}
}
// === end ===

// === case: missing_braces_if_with_braceless_do_while_body ===
// imports: java.util.List
class InputControlFlowBracesMissingBracesIfWithBracelessDoWhileBodySliceViolation {
	void m(boolean a, int x, List<Integer> list) {
		if (a) // violation: Braceless control flow has multi-line body, add braces.
			do
				list.subList(0, 1).clear();
			while (x > 0);
	}
}
// === end ===

// === case: missing_braces_if_with_code_after_body_terminator ===
class InputControlFlowBracesMissingBracesIfWithCodeAfterBodyTerminatorSliceViolation {
	private void f(int a, int b) { }

	private void g() { }

	void m(boolean a) {
		if (a) // violation: Braceless control flow has multi-line body, add braces.
			f(
					1,
					2
			); g();
	}
}
// === end ===

// === case: missing_braces_if_with_comment_after_body_terminator ===
class InputControlFlowBracesMissingBracesIfWithCommentAfterBodyTerminatorSliceViolation {
	private void f(int a, int b) { }

	void m(boolean a) {
		if (a) // violation: Braceless control flow has multi-line body, add braces.
			f(
					1,
					2
			); // note
	}
}
// === end ===

// === case: missing_braces_if_with_comment_ending_in_semicolon ===
class InputControlFlowBracesMissingBracesIfWithCommentEndingInSemicolonSliceViolation {
	void m(int x) {
		if (x > 0) // note; // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
	}
}
// === end ===

// === case: missing_braces_if_with_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			// leading comment before the inner statement
			for (int i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_dotted_continuation ===
// imports: java.util.List
class InputControlFlowBracesMissingBracesIfWithDottedContinuationSliceViolation {
	void m(int x, List<Integer> list) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			list
					.add(1);
	}
}
// === end ===

// === case: missing_braces_if_with_for ===
class InputControlFlowBracesMissingBracesIfWithForSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_if_with_if_else ===
class InputControlFlowBracesMissingBracesIfWithIfElseSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
			else
				++x;
	}
}
// === end ===

// === case: missing_braces_if_with_if_suffixed_call_then_else ===
class InputControlFlowBracesMissingBracesIfWithIfSuffixedCallThenElseSliceViolation {
	void m(boolean cond, int x) {
		if (cond) // violation: Braceless control flow has multi-line body, add braces.
			while (x > 0)
				not𝐀if(x);
		else
			other();
	}

	private void not𝐀if(int x) { }

	private void other() { }
}
// === end ===

// === case: missing_braces_if_with_inner_braced_else_then_outer_else ===
class InputControlFlowBracesMissingBracesIfWithInnerBracedElseThenOuterElseSliceViolation {
	void m(int x, int y) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
			else {
				--x;
				++y;
			}
		else
			++y;
	}
}
// === end ===

// === case: missing_braces_if_with_inner_braced_for_then_outer_else ===
class InputControlFlowBracesMissingBracesIfWithInnerBracedForThenOuterElseSliceViolation {
	void m(int x, int y) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			for (var i = 0; i < x; ++i) {
				--x;
				++y;
			}
		else
			++y;
	}
}
// === end ===

// === case: missing_braces_if_with_inner_braced_if_then_outer_else ===
class InputControlFlowBracesMissingBracesIfWithInnerBracedIfThenOuterElseSliceViolation {
	void m(int x, int y) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5) {
				--x;
				++y;
			}
			else
				--x;
		else
			++y;
	}
}
// === end ===

// === case: missing_braces_if_with_multiline_block_comment_then_braced_inner ===
class InputControlFlowBracesMissingBracesIfWithMultilineBlockCommentThenBracedInnerSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			/*
			 * note line one
			 * note line two
			 */
			for (int i = 0; i < x; ++i) {
				System.out.println(i);
				System.out.println(-i);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_if_with_quote_char_literal ===
class InputControlFlowBracesMissingBracesIfWithQuoteCharLiteralSliceViolation {
	void m(char ch, int x) {
		if (ch == '"') // guard // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0)
				System.out.println('"');
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_block_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* guard */ // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_block_then_line_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingBlockThenLineCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* note */ // guard // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_if_with_trailing_comment ===
class InputControlFlowBracesMissingBracesIfWithTrailingCommentSliceViolation {
	void m(int x) {
		if (x > 0) // guard // violation: Braceless control flow has multi-line body, add braces.
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}
// === end ===

// === case: missing_braces_text_block_body ===
class InputControlFlowBracesMissingBracesTextBlockBodySliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			s = """
					text
					""";
		System.out.println(s);
	}
}
// === end ===

// === case: missing_braces_text_block_body_with_quote_in_content ===
class InputControlFlowBracesMissingBracesTextBlockBodyWithQuoteInContentSliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			s = """
					a " b
					""";
		System.out.println(s);
	}
}
// === end ===

// === case: missing_braces_while_with_braced_do_while ===
class InputControlFlowBracesMissingBracesWhileWithBracedDoWhileSliceViolation {
	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			do {
				System.out.println(x);
				System.out.println(-x);
			}
			while (x > 0);
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_while_with_braced_if_else ===
class InputControlFlowBracesMissingBracesWhileWithBracedIfElseSliceViolation {
	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			}
			else {
				System.out.println(0);
				System.out.println(1);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_while_with_braced_if_else_block_comment ===
class InputControlFlowBracesMissingBracesWhileWithBracedIfElseBlockCommentSliceViolation {
	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 0) {
				System.out.println(x);
				System.out.println(-x);
			} /* trailing note */
			else {
				System.out.println(0);
				System.out.println(1);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_while_with_braced_synchronized ===
class InputControlFlowBracesMissingBracesWhileWithBracedSynchronizedSliceViolation {
	void m(boolean cond, int x, Object lock) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			synchronized (lock) {
				System.out.println(x);
				System.out.println(-x);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_while_with_braced_try_catch ===
class InputControlFlowBracesMissingBracesWhileWithBracedTryCatchSliceViolation {
	void m(boolean cond, int x) {
		while (cond) // violation: Braceless control flow has multi-line body, add braces.
			try {
				System.out.println(x);
				System.out.println(-x);
			}
			catch (RuntimeException e) {
				System.out.println(e);
				System.out.println(x);
			}
		System.out.println("after");
	}
}
// === end ===

// === case: missing_braces_while_with_if ===
class InputControlFlowBracesMissingBracesWhileWithIfSliceViolation {
	void m(int x) {
		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
	}
}
// === end ===

// === case: missing_braces_while_with_if_else ===
class InputControlFlowBracesMissingBracesWhileWithIfElseSliceViolation {
	void m(int x) {
		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
			else
				x = 0;
	}
}
// === end ===

// === case: missing_braces_while_with_if_else_word_in_string ===
class InputControlFlowBracesMissingBracesWhileWithIfElseWordInStringSliceViolation {
	void m(int x, String s) {
		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				s = "a; else b";
			else
				s = "c";
	}
}
// === end ===

// === case: missing_braces_while_with_if_then_else_prefixed_identifier ===
class InputControlFlowBracesMissingBracesWhileWithIfThenElsePrefixedIdentifierSliceViolation {
	void m(int x, boolean elseFlag) {
		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
		elseFlag = false;
	}
}
// === end ===

// === case: missing_braces_while_with_if_then_else_supplementary_identifier ===
class InputControlFlowBracesMissingBracesWhileWithIfThenElseSupplementaryIdentifierSliceViolation {
	void m(int x, boolean else𝐀) {
		while (x > 0) // violation: Braceless control flow has multi-line body, add braces.
			if (x > 5)
				--x;
		else𝐀 = false;
	}
}
// === end ===

// === case: missing_braces_while_with_label_on_own_line ===
class InputControlFlowBracesMissingBracesWhileWithLabelOnOwnLineSliceViolation {
	void m(boolean c, int x) {
		while (c) // violation: Braceless control flow has multi-line body, add braces.
			loop:
			for (var i = 0; i < 2; ++i) {
				System.out.println(i);
				break loop;
			}
		System.out.println(x);
	}
}
// === end ===

// === case: missing_braces_while_with_labeled_for_then_trailing_stmt ===
class InputControlFlowBracesMissingBracesWhileWithLabeledForThenTrailingStmtSliceViolation {
	void m(boolean c, int x) {
		while (c) // violation: Braceless control flow has multi-line body, add braces.
			loop: for (var i = 0; i < 2; ++i) {
				System.out.println(i);
				break loop;
			}
		System.out.println(x);
	}
}
// === end ===

// === case: missing_braces_while_with_stacked_labels ===
class InputControlFlowBracesMissingBracesWhileWithStackedLabelsSliceViolation {
	void m(boolean c, int x) {
		while (c) // violation: Braceless control flow has multi-line body, add braces.
			outer: inner: for (var i = 0; i < 2; ++i) {
				System.out.println(i);
				break outer;
			}
		System.out.println(x);
	}
}
// === end ===

// === case: missing_braces_while_with_supplementary_label ===
class InputControlFlowBracesMissingBracesWhileWithSupplementaryLabelSliceViolation {
	void m(boolean c, int x) {
		while (c) // violation: Braceless control flow has multi-line body, add braces.
			loop𝐀: for (var i = 0; i < 2; ++i) {
				System.out.println(i);
				break loop𝐀;
			}
		System.out.println(x);
	}
}
// === end ===

// === case: multiline_header_missing_braces ===
// skip-reason: control-flow header spans lines
class InputControlFlowBracesMultilineHeaderMissingBracesSliceViolation {
	void m(boolean a, boolean b, int x) {
		if (a // violation: Braceless control flow has multi-line body, add braces.
				&& b)
			if (x > 0)
				--x;
	}
}
// === end ===

// === case: multiline_header_unnecessary_braces ===
// skip-reason: control-flow header spans lines
class InputControlFlowBracesMultilineHeaderUnnecessaryBracesSliceViolation {
	void m(boolean a, boolean b) {
		if (a // violation: Remove unnecessary braces from single-line control flow body.
				&& b) {
			System.out.println(a);
		}
	}
}
// === end ===

// === case: nested_inner_for_with_if ===
class InputControlFlowBracesNestedInnerForWithIfSliceViolation {
	void m(int x) {
		if (x > 0) {
			for (int i = 0; i < x; ++i) // violation: Braceless control flow has multi-line body, add braces.
				if (i > 0)
					System.out.println(i);
		}
	}
}
// === end ===

// === case: no_indent ===
class InputControlFlowBracesNoIndentSliceViolation {
void m(int x) {
do { // violation: Remove unnecessary braces from single-line control flow body.
	--x;
} while (x > 0);
}
}
// === end ===

// === case: non_do_while_one_liners ===
// imports: java.util.List
// multi-fix-expected
class InputControlFlowBracesNonDoWhileOneLinersSliceViolation {
	void elseOneLiner(int x) {
		if (x > 0)
			System.out.println("positive");
		else System.out.println("negative"); // violation: Move control flow body to its own line (one-liners not allowed).
	}

	void method(int x) {
		if (x > 0) System.out.println("positive"); // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
		for (int i = 0; i < x; ++i) System.out.println(i); // violation: Move control flow body to its own line (one-liners not allowed).
		final var list = List.of("a");
		for (var item : list) System.out.println(item); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: on_do_line_block_comment_opens ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesOnDoLineBlockCommentOpensSliceViolation {
	void m(int x) {
		do /* multi // violation: Do-while body must be on the do line.
		   line */
			x = 5;
		while (x > 0);
	}
}
// === end ===

// === case: on_do_line_body_call_ending_in_do_word ===
class InputControlFlowBracesOnDoLineBodyCallEndingInDoWordSliceViolation {
	void m(int x) {
		do undo(); while (x > 0); // violation: Do-while while clause must be on its own line.
	}

	private void undo() { }
}
// === end ===

// === case: on_do_line_body_call_starting_with_do_word ===
class InputControlFlowBracesOnDoLineBodyCallStartingWithDoWordSliceViolation {
	private void doWork() { }

	void m(int x) {
		do doWork(); while (x > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: on_do_line_body_call_supplementary_after_do_word ===
class InputControlFlowBracesOnDoLineBodyCallSupplementaryAfterDoWordSliceViolation {
	private void do𝐀() { }

	void m(int x) {
		do do𝐀(); while (x > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: on_do_line_body_call_supplementary_before_do_word ===
class InputControlFlowBracesOnDoLineBodyCallSupplementaryBeforeDoWordSliceViolation {
	void m(int x) {
		do 𝐀do(); while (x > 0); // violation: Do-while while clause must be on its own line.
	}

	private void 𝐀do() { }
}
// === end ===

// === case: on_do_line_body_runs_onto_while_line ===
// skip-reason: do-while body runs onto the line its closing while shares
// imports: java.util.List
class InputControlFlowBracesOnDoLineBodyRunsOntoWhileLineSliceViolation {
	void m(List<Integer> list) {
		do list // violation: Braceless control flow has multi-line body, add braces.
				.add(1); while (list.size() < 10);
	}
}
// === end ===

// === case: on_do_line_body_spans_lines ===
// skip-reason: do-while body does not end in a semicolon
class InputControlFlowBracesOnDoLineBodySpansLinesSliceViolation {
	void m(int x) {
		do x = x // violation: Braceless control flow has multi-line body, add braces.
				+ 1;
		while (x > 0);
	}
}
// === end ===

// === case: on_do_line_body_starting_with_while_word ===
class InputControlFlowBracesOnDoLineBodyStartingWithWhileWordSliceViolation {
	void m(int x) {
		var whileCount = 0;
		do whileCount = x + 1; // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 0);
	}
}
// === end ===

// === case: on_do_line_comment_line_before_while ===
class InputControlFlowBracesOnDoLineCommentLineBeforeWhileSliceViolation {
	void m(int x, int y) {
		do x = x + y; // violation: Move control flow body to its own line (one-liners not allowed).
		// keep going
		while (x > 0);
	}
}
// === end ===

// === case: on_do_line_false_while_in_body ===
class InputControlFlowBracesOnDoLineFalseWhileInBodySliceViolation {
	void m() {
		final int whileTimer = 5;
		var whileVar = 0;
		do ++whileVar; while (whileTimer > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: on_do_line_nested_do_while_terminator ===
class InputControlFlowBracesOnDoLineNestedDoWhileTerminatorSliceViolation {
	void m(int x) {
		do do { --x; ++x; } while (x > 5); while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: on_do_line_preceded_by_sibling_do_while ===
// skip-reason: do-while shares its line with another do-while
class InputControlFlowBracesOnDoLinePrecededBySiblingDoWhileSliceViolation {
	void m(int x, int y) {
		do; while (x > 0); do --y; while (y > 0); // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: on_do_line_sibling_while_loop_after_terminator ===
class InputControlFlowBracesOnDoLineSiblingWhileLoopAfterTerminatorSliceViolation {
	void m(int x, int y) {
		do --x; while (x > 0); while (y > 0) { --y; } // violation: Do-while while clause must be on its own line.
	}
}
// === end ===

// === case: on_do_line_while_deeper_indent ===
class InputControlFlowBracesOnDoLineWhileDeeperIndentSliceViolation {
	void m(int x, int y) {
		do x = x + y; // violation: Move control flow body to its own line (one-liners not allowed).
			while (x < 100);
	}
}
// === end ===

// === case: on_do_line_while_inside_comment_body ===
class InputControlFlowBracesOnDoLineWhileInsideCommentBodySliceViolation {
	void m(int x, int y) {
		do x = x + y; // ; while // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 0);
	}
}
// === end ===

// === case: on_do_line_while_inside_string_body ===
// imports: java.util.List
class InputControlFlowBracesOnDoLineWhileInsideStringBodySliceViolation {
	void m(List<String> list) {
		String s;
		do s = list.getFirst().replace("; while", ""); // violation: Move control flow body to its own line (one-liners not allowed).
		while (s != null);
	}
}
// === end ===

// === case: on_do_line_while_nested_in_braced_body ===
class InputControlFlowBracesOnDoLineWhileNestedInBracedBodySliceViolation {
	void m(int x, int d) {
		do if (d > 0) { --x; while (d > 0) { --d; } } while (x > 0); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: on_do_line_while_nested_in_condition_lambda ===
// imports: java.util.function.BooleanSupplier
class InputControlFlowBracesOnDoLineWhileNestedInConditionLambdaSliceViolation {
	private boolean a, c;

	private void b() { }

	void m() {
		do b(); while (test(() -> { if (a) { b(); } while (c) { b(); } return true; })); // violation: Do-while while clause must be on its own line.
	}

	private boolean test(BooleanSupplier s) {
		return s.getAsBoolean();
	}
}
// === end ===

// === case: on_do_line_while_word_inside_body ===
class InputControlFlowBracesOnDoLineWhileWordInsideBodySliceViolation {
	void m(int x) {
		final var whileCount = 0;
		do x = whileCount + 1; // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 0);
	}
}
// === end ===

// === case: one_liner_after_block_comment_close_reading_do ===
class InputControlFlowBracesOneLinerAfterBlockCommentCloseReadingDoSliceViolation {
	void m(int x) {
		/* note:
		do the decrement */ if (x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_body_spanning_lines ===
class InputControlFlowBracesOneLinerBodySpanningLinesSliceViolation {
	void m(int x) {
		if (x > 0) for (var i = 0; i < 10; ++i) // violation: Move control flow body to its own line (one-liners not allowed).
			System.out.println(i);
	}
}
// === end ===

// === case: one_liner_comments_and_header_braces ===
// multi-fix-expected
class InputControlFlowBracesOneLinerCommentsAndHeaderBracesSliceViolation {
	void m(int x) {
		for (var s : new String[]{"a"}) System.out.println(s); // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 0) /* pre */ --x; // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 1) --x; // note // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 2) --x; /* note */ // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 3) --x; ++x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_cuddled_else ===
class InputControlFlowBracesOneLinerCuddledElseSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		} else --x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_do_while_body ===
class InputControlFlowBracesOneLinerDoWhileBodySliceViolation {
	private InputControlFlowBracesOneLinerDoWhileBodySliceViolation helper() {
		return this;
	}

	void m(int x) {
		if (x > 0) do // violation: Move control flow body to its own line (one-liners not allowed).
			x = helper().value();
		while (x > 0);
	}

	private int value() {
		return 0;
	}
}
// === end ===

// === case: one_liner_dotted_continuation_body ===
// imports: java.util.List
class InputControlFlowBracesOneLinerDottedContinuationBodySliceViolation {
	void m(int x, List<Integer> list) {
		if (x > 0) list // violation: Move control flow body to its own line (one-liners not allowed).
				.add(1);
	}
}
// === end ===

// === case: one_liner_else_text_block_blank_line ===
// multi-fix-expected
class InputControlFlowBracesOneLinerElseTextBlockBlankLineSliceViolation {
	private boolean a;
	private String s;

	private void b() { }

	void m() {
		if (a) b(); // violation: Move control flow body to its own line (one-liners not allowed).
		else s = """

				t
				"""; // violation@opener: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_else_with_trailing_statement ===
class InputControlFlowBracesOneLinerElseWithTrailingStatementSliceViolation {
	void m(int x) {
		if (x > 0) {
			--x;
			++x;
		}
		else for (var i = 0; i < 2; ++i) // violation: Move control flow body to its own line (one-liners not allowed).
			--x; System.out.println(x);
	}
}
// === end ===

// === case: one_liner_for_loop ===
class InputControlFlowBracesOneLinerForLoopSliceViolation {
	void m() {
		for (int i = 0; i < 10; ++i) System.out.println(i); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_if_else_if_same_line ===
class InputControlFlowBracesOneLinerIfElseIfSameLineSliceViolation {
	void m(int x) {
		if (x > 0) --x; else if (x < 0) ++x; // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_if_else_same_line ===
class InputControlFlowBracesOneLinerIfElseSameLineSliceViolation {
	void m(int x) {
		if (x > 0) --x; else ++x; // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_if_statement ===
class InputControlFlowBracesOneLinerIfStatementSliceViolation {
	void m(int x) {
		if (x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_if_with_else_on_next_line ===
class InputControlFlowBracesOneLinerIfWithElseOnNextLineSliceViolation {
	void m(int x) {
		if (x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
		else
			++x;
	}
}
// === end ===

// === case: one_liner_lambda_block_body ===
class InputControlFlowBracesOneLinerLambdaBlockBodySliceViolation {
	private static void run(Runnable action) {
		action.run();
	}

	void m(int x) {
		if (x > 0) run(() -> { // violation: Move control flow body to its own line (one-liners not allowed).
			System.out.println(x);

			System.out.println(-x);
		});
	}
}
// === end ===

// === case: one_liner_multi_line_body_then_else ===
class InputControlFlowBracesOneLinerMultiLineBodyThenElseSliceViolation {
	void m(int x) {
		if (x > 0) for (var i = 0; i < 2; ++i) // violation: Move control flow body to its own line (one-liners not allowed).
			--x; else {
			++x;
			System.out.println(x);
		}
	}
}
// === end ===

// === case: one_liner_multi_line_body_then_statement ===
class InputControlFlowBracesOneLinerMultiLineBodyThenStatementSliceViolation {
	private static void log(int value) {
		System.out.println(value);
	}

	private static void run(Runnable action) {
		action.run();
	}

	void m(int x) {
		if (x > 0) run(() -> { // violation: Move control flow body to its own line (one-liners not allowed).
			log(x);
		}); log(-x);
	}
}
// === end ===

// === case: one_liner_multi_line_body_unterminated_block_comment ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesOneLinerMultiLineBodyUnterminatedBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) x = x // violation: Move control flow body to its own line (one-liners not allowed).
				+ 1; /* multi
				       line */
		System.out.println(x);
	}
}
// === end ===

// === case: one_liner_nested_for ===
class InputControlFlowBracesOneLinerNestedForSliceViolation {
	void m() {
		for (var i = 0; i < 3; ++i) for (var j = 0; j < 3; ++j) System.out.println(i + j); // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_nested_if ===
class InputControlFlowBracesOneLinerNestedIfSliceViolation {
	void m(int x) {
		if (x > 0) if (x > 5) --x; // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_nested_if_with_else ===
class InputControlFlowBracesOneLinerNestedIfWithElseSliceViolation {
	void m(int x) {
		if (x > 0) if (x > 5) --x; else ++x; // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_semicolon_on_next_line ===
class InputControlFlowBracesOneLinerSemicolonOnNextLineSliceViolation {
	void m(int x) {
		if (x > 0) System.out.println(x) // violation: Move control flow body to its own line (one-liners not allowed).
				;
	}
}
// === end ===

// === case: one_liner_statement_kinds ===
// multi-fix-expected
class InputControlFlowBracesOneLinerStatementKindsSliceViolation {
	void m(int x) {
		int[] arr = {1, 2};
		var name = "a";
		if (x > 0) return; // violation: Move control flow body to its own line (one-liners not allowed).
		if (x < 0) throw new IllegalStateException(); // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 5) arr = new int[]{3, 4}; // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 6) name = ";"; // violation: Move control flow body to its own line (one-liners not allowed).
		if (x > 7) System.out.println(/* ; */ name + arr.length); // violation: Move control flow body to its own line (one-liners not allowed).
		while (x > 8) break; // violation: Move control flow body to its own line (one-liners not allowed).
		for (var i = 0; i < x; ++i) continue; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_supplementary_char ===
class InputControlFlowBracesOneLinerSupplementaryCharSliceViolation {
	void m(int 𝐀) {
		if (𝐀 > 0) System.out.println(𝐀); // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_supplementary_char_before_else ===
class InputControlFlowBracesOneLinerSupplementaryCharBeforeElseSliceViolation {
	void m(int 𝐀) {
		if (𝐀 > 0) --𝐀; else ++𝐀; // violation: Move control flow body to its own line (one-liners not allowed). // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_switch_body ===
class InputControlFlowBracesOneLinerSwitchBodySliceViolation {
	void m(int x) {
		if (x > 0) switch (x) { // violation: Move control flow body to its own line (one-liners not allowed).
			case 1 -> System.out.println(1);
			default -> System.out.println(x);
		}
	}
}
// === end ===

// === case: one_liner_tab_after_keyword ===
class InputControlFlowBracesOneLinerTabAfterKeywordSliceViolation {
	void m(int x) {
		if	(x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: one_liner_text_block_body ===
class InputControlFlowBracesOneLinerTextBlockBodySliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) s = """
				text
				"""; // violation@opener: Move control flow body to its own line (one-liners not allowed).
		System.out.println(s);
	}
}
// === end ===

// === case: one_liner_try_body ===
class InputControlFlowBracesOneLinerTryBodySliceViolation {
	void m(int x) {
		if (x > 0) try { // violation: Move control flow body to its own line (one-liners not allowed).
			System.out.println(x);
		}
		finally {
			System.out.println(-x);
		}
	}
}
// === end ===

// === case: one_liner_while_loop ===
class InputControlFlowBracesOneLinerWhileLoopSliceViolation {
	void m(int x) {
		while (x > 0) --x; // violation: Move control flow body to its own line (one-liners not allowed).
	}
}
// === end ===

// === case: own_line_blank_line_before_while ===
class InputControlFlowBracesOwnLineBlankLineBeforeWhileSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			--x;

		while (x > 0);
	}
}
// === end ===

// === case: own_line_body_with_cuddled_while ===
class InputControlFlowBracesOwnLineBodyWithCuddledWhileSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			--x; while (x > 0);
	}
}
// === end ===

// === case: own_line_comment_before_multi_line_body ===
class InputControlFlowBracesOwnLineCommentBeforeMultiLineBodySliceViolation {
	void m(boolean c, int x) {
		do // violation: Braceless control flow has multi-line body, add braces.
			// note
			if (x > 0)
				--x;
		while (c);
	}
}
// === end ===

// === case: own_line_comment_bracketed_code ===
class InputControlFlowBracesOwnLineCommentBracketedCodeSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			/* pre */ x = 5; /* post */
		while (x > 0);
	}
}
// === end ===

// === case: own_line_comment_on_while_line ===
class InputControlFlowBracesOwnLineCommentOnWhileLineSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			--x;
		/* note */ while (x > 0);
	}
}
// === end ===

// === case: own_line_statement_with_trailing_comment ===
class InputControlFlowBracesOwnLineStatementWithTrailingCommentSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			--x; // pending
		while (x > 0);
	}
}
// === end ===

// === case: own_line_while_on_block_comment_close_line ===
// skip-reason: block comment or text block does not close on the line it opened
class InputControlFlowBracesOwnLineWhileOnBlockCommentCloseLineSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			x = 1; /* multi
			line */ while (x > 0);
	}
}
// === end ===

// === case: own_line_with_comment_on_do ===
class InputControlFlowBracesOwnLineWithCommentOnDoSliceViolation {
	void m(int x) {
		do // note // violation: Do-while body must be on the do line.
			--x;
		while (x > 0);
	}
}
// === end ===

// === case: text_block_body_refused ===
// skip-reason: do-while body contains a text block the fixer cannot reformat
class InputControlFlowTextBlockBodySliceViolation {
	void m(String s) {
		do s = """
				text
				"""; // violation@opener: Braceless control flow has multi-line body, add braces.
		while (true);
	}
}
// === end ===

// === case: tier_simple_body_on_own_line_decrement ===
class InputControlFlowBracesTierSimpleBodyOnOwnLineDecrementSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			--x;
		while (x > 0);
	}
}
// === end ===

// === case: tier_simple_body_on_own_line_list_add ===
// imports: java.util.List
class InputControlFlowBracesTierSimpleBodyOnOwnLineListAddSliceViolation {
	void m() {
		final var list = List.of("a");
		do // violation: Do-while body must be on the do line.
			list.add("b");
		while (list.size() < 10);
	}
}
// === end ===

// === case: tier_simple_body_on_own_line_method_call ===
class InputControlFlowBracesTierSimpleBodyOnOwnLineMethodCallSliceViolation {
	void m(int x) {
		do // violation: Do-while body must be on the do line.
			next(x);
		while (x > 0);
	}

	private int next(int x) {
		return x - 1;
	}
}
// === end ===

// === case: unnecessary_braces_close_brace_after_text_block ===
// skip-reason: the block does not end where the body does
class InputControlFlowBracesUnnecessaryBracesCloseBraceAfterTextBlockSliceViolation {
	void m(int x) {
		var s = "";
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			s = """
} """; }
		System.out.println(s);
	}
}
// === end ===

// === case: unnecessary_braces_close_brace_in_block_comment ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesUnnecessaryBracesCloseBraceInBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			--x; /* note
		} */ }
	}
}
// === end ===

// === case: unnecessary_braces_comment_before_close_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesUnnecessaryBracesCommentBeforeCloseBraceSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		/* trailing note */ }
	}
}
// === end ===

// === case: unnecessary_braces_do_while ===
class InputControlFlowBracesUnnecessaryBracesDoWhileSliceViolation {
	void m(int x) {
		do { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} while (x > 0);
	}
}
// === end ===

// === case: unnecessary_braces_else ===
class InputControlFlowBracesUnnecessaryBracesElseSliceViolation {
	void m(int x) {
		if (x > 0)
			++x;
		else { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_final_prefixed_identifier_body ===
class InputControlFlowBracesUnnecessaryBracesFinalPrefixedIdentifierBodySliceViolation {
	void m(int x) {
		var finalCount = 1;
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			finalCount = 2;
		}
		System.out.println(finalCount);
	}
}
// === end ===

// === case: unnecessary_braces_for_loops ===
// imports: java.util.List
// multi-fix-expected
class InputControlFlowBracesUnnecessaryBracesForLoopsSliceViolation {
	void m(int x) {
		for (int i = 0; i < x; ++i) { // violation: Remove unnecessary braces from single-line control flow body.
			System.out.println(i);
		}

		final var list = List.of("a", "b");
		for (var item : list) { // violation: Remove unnecessary braces from single-line control flow body.
			System.out.println(item);
		}
	}
}
// === end ===

// === case: unnecessary_braces_if ===
class InputControlFlowBracesUnnecessaryBracesIfSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			System.out.println("positive");
		}
	}
}
// === end ===

// === case: unnecessary_braces_if_cuddled_else ===
class InputControlFlowBracesUnnecessaryBracesIfCuddledElseSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		} else {
			++x;
			System.out.println(x);
		}
	}
}
// === end ===

// === case: unnecessary_braces_if_else_pair ===
// multi-fix-expected
class InputControlFlowBracesUnnecessaryBracesIfElsePairSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			System.out.println("positive");
		}
		else { // violation: Remove unnecessary braces from single-line control flow body.
			System.out.println("negative");
		}
	}
}
// === end ===

// === case: unnecessary_braces_statement_kinds ===
// multi-fix-expected
class InputControlFlowBracesUnnecessaryBracesStatementKindsSliceViolation {
	void asserts(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			assert x > 0;
		}
	}

	void labelledJumps(int x) {
		outer:
		for (var i = 0; i < x; ++i) {
			while (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
				break outer;
			}
		}
		inner:
		for (var i = 0; i < x; ++i) {
			while (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
				continue inner;
			}
		}
	}

	void loops(int x) {
		for (var i = 0; i < x; ++i) {
			if (i > 5) { // violation: Remove unnecessary braces from single-line control flow body.
				break;
			}
		}
		for (var i = 0; i < x; ++i) {
			if (i > 5) { // violation: Remove unnecessary braces from single-line control flow body.
				continue;
			}
		}
	}

	void returnsAndThrows(int x) {
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			return;
		}
		if (x < -1) { // violation: Remove unnecessary braces from single-line control flow body.
			throw new IllegalStateException();
		}
	}
}
// === end ===

// === case: unnecessary_braces_var_prefixed_identifier_body ===
class InputControlFlowBracesUnnecessaryBracesVarPrefixedIdentifierBodySliceViolation {
	void m(int x) {
		var varCount = 1;
		if (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			varCount = 2;
		}
		System.out.println(varCount);
	}
}
// === end ===

// === case: unnecessary_braces_while ===
class InputControlFlowBracesUnnecessaryBracesWhileSliceViolation {
	void m(int x) {
		while (x > 0) { // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_with_block_comment_on_brace ===
// skip-reason: cannot remove braces without losing other content on the brace line
class InputControlFlowBracesUnnecessaryBracesWithBlockCommentOnBraceSliceViolation {
	void m(int x) {
		if (x > 0) { /* guard */ // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_with_comment_on_brace ===
class InputControlFlowBracesUnnecessaryBracesWithCommentOnBraceSliceViolation {
	void m(int x) {
		if (x > 0) { // guard // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_with_comment_on_brace_after_block_comment ===
class InputControlFlowBracesUnnecessaryBracesWithCommentOnBraceAfterBlockCommentSliceViolation {
	void m(int x) {
		if (x > 0) /* note */ { // guard // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_with_comment_on_brace_and_char_literal ===
class InputControlFlowBracesUnnecessaryBracesWithCommentOnBraceAndCharLiteralSliceViolation {
	void m(char ch, int x) {
		if (ch == '"') { // guard // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_with_comment_on_brace_and_string_literal ===
class InputControlFlowBracesUnnecessaryBracesWithCommentOnBraceAndStringLiteralSliceViolation {
	void m(String s, int x) {
		if (s.equals("//")) { // guard // violation: Remove unnecessary braces from single-line control flow body.
			--x;
		}
	}
}
// === end ===

// === case: unnecessary_braces_yield_body ===
class InputControlFlowBracesUnnecessaryBracesYieldBodySliceViolation {
	void m(int k, int a, int b, boolean c) {
		final var v = switch (k) {
			default -> {
				if (c) { // violation: Remove unnecessary braces from single-line control flow body.
					yield a;
				}
				yield b;
			}
		};
		System.out.println(v);
	}
}
// === end ===