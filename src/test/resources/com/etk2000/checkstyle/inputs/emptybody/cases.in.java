package com.etk2000.checkstyle.inputs.emptybody;

// === case: body ===
// multi-fix-expected
class InputEmptyBodyViolation {
	void emptyElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else { // violation: Empty else body, remove it.
		}
	}

	void emptyElseIfBlock(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyElseIfStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	void emptyElseIfStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
	}

	void emptyElseStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else; // violation: Empty else body, remove it.
	}

	void emptyElseStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: Empty else body, remove it.
			;
	}

	void emptyIfBlock(int x) {
		if (x > 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyIfStatement(int x) {
		if (x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	void emptyIfStatementNextLine(int x) {
		if (x > 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
	}

	void emptyIfWithSideEffects(int x) {
		if (++x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: do_side_effect_condition_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyDoSideEffectConditionReturnsSkipSliceViolation {
	void m() {
		do; // violation (warning): Empty do-while body, remove it (preserve any side effects in the condition).
		while (next());
	}

	boolean next() {
		return false;
	}
}
// === end ===

// === case: else_if_chain_tail_removed ===
class InputEmptyBodyElseIfChainTailRemovedSliceViolation {
	void m(boolean a, boolean b, boolean c) {
		if (a)
			System.out.println("a");
		else if (b)
			System.out.println("b");
		else if (c) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}
}
// === end ===

// === case: else_if_condition_side_effect_returns_skip ===
// skip-reason: condition side effect would move out of the else branch and run unconditionally
class InputEmptyBodyElseIfConditionSideEffectReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (++x < 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}
}
// === end ===

// === case: else_in_braceless_then_returns_skip ===
// skip-reason: removing the else would let a later else bind to a nearer if
class InputEmptyBodyElseInBracelessThenReturnsSkipSliceViolation {
	void m(boolean a, boolean b) {
		if (a)
			if (b)
				System.out.println("both");
			else { // violation: Empty else body, remove it.
			}
		else
			System.out.println("neither");
	}
}
// === end ===

// === case: for_condition_call_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForConditionCallReturnsSkipSliceViolation {
	void m() {
		for (var i = 0; next(); ++i); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	boolean next() {
		return false;
	}
}
// === end ===

// === case: for_each_side_effecting_iterable_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
// imports: java.util.List
class InputEmptyBodyForEachSideEffectingIterableReturnsSkipSliceViolation {
	List<String> items() {
		return null;
	}

	void m() {
		for (var s : items()); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}
}
// === end ===

// === case: for_init_call_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForInitCallReturnsSkipSliceViolation {
	int compute() {
		return 0;
	}

	void m() {
		for (var i = compute(); i < 3; ++i); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}
}
// === end ===

// === case: for_init_expression_list_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForInitExpressionListReturnsSkipSliceViolation {
	void m() {
		int i;
		for (i = 0; i < 3; ++i); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}
}
// === end ===

// === case: for_iterator_array_element_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForIteratorArrayElementReturnsSkipSliceViolation {
	void m(int[] data) {
		for (var i = 0; i < data.length; ++data[i]); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}
}
// === end ===

// === case: for_iterator_calls_method_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForIteratorCallsMethodReturnsSkipSliceViolation {
	void m() {
		for (var i = 0; i < 3; step()); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	void step() {
		System.out.println("step");
	}
}
// === end ===

// === case: for_update_mutates_outer_variable_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyForUpdateMutatesOuterVariableReturnsSkipSliceViolation {
	void m(int x) {
		for (var i = 0; i < 3; ++x); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}
}
// === end ===

// === case: if_array_creation_dimension_hoists_call ===
class InputEmptyBodyIfArrayCreationDimensionHoistsCallSliceViolation {
	void m() {
		if (new int[size()].length > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	int size() {
		return 1;
	}
}
// === end ===

// === case: if_braced_with_block_comment_inside_returns_skip ===
// skip-reason: removing the construct would destroy a comment inside it
class InputEmptyBodyIfBracedWithBlockCommentInsideReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
			/* the caller already handled it */
		}
	}
}
// === end ===

// === case: if_braced_with_comment_before_close_brace_returns_skip ===
// skip-reason: removing the construct would destroy a comment inside it
class InputEmptyBodyIfBracedWithCommentBeforeCloseBraceReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		/* the caller already handled it */ }
	}
}
// === end ===

// === case: if_braced_with_line_comment_on_opener_returns_skip ===
// skip-reason: removing the construct would destroy a comment inside it
class InputEmptyBodyIfBracedWithLineCommentOnOpenerReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0) { // the caller already handled it // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}
}
// === end ===

// === case: if_condition_assignment_hoists_assignment ===
class InputEmptyBodyIfConditionAssignmentHoistsAssignmentSliceViolation {
	int compute() {
		return 1;
	}

	void m(int x) {
		if ((x = compute()) > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_condition_two_calls_hoists_both ===
class InputEmptyBodyIfConditionTwoCallsHoistsBothSliceViolation {
	int first() {
		return 1;
	}

	void m() {
		if (first() > second()); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	int second() {
		return 2;
	}
}
// === end ===

// === case: if_in_braced_block_with_siblings_removed ===
class InputEmptyBodyIfInBracedBlockWithSiblingsRemovedSliceViolation {
	void m(boolean busy, boolean ready) {
		if (ready) {
			if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
			System.out.println("first");
			System.out.println("second");
		}
	}
}
// === end ===

// === case: if_in_braceless_do_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyIfInBracelessDoReturnsSkipSliceViolation {
	void m(boolean busy, int x) {
		do
			if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
		while (x > 0);
	}
}
// === end ===

// === case: if_in_braceless_for_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyIfInBracelessForReturnsSkipSliceViolation {
	void m(boolean busy) {
		for (var i = 0; i < 3; ++i)
			if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_in_braceless_if_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyIfInBracelessIfReturnsSkipSliceViolation {
	void m(boolean busy, boolean ready) {
		if (ready)
			if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_in_braceless_while_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyIfInBracelessWhileReturnsSkipSliceViolation {
	void m(boolean busy, int x) {
		while (x > 0)
			if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_in_labelled_statement_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyIfInLabelledStatementReturnsSkipSliceViolation {
	void m(boolean busy) {
		skip:
		if (busy); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_multiline_side_effect_returns_skip ===
// skip-reason: condition side effect spans more than one line
class InputEmptyBodyIfMultilineSideEffectReturnsSkipSliceViolation {
	int count() {
		return 1;
	}

	void m() {
		if (self() // violation: Empty if body, remove it (preserve any side effects in the condition).
				.count() > 0);
	}

	InputEmptyBodyIfMultilineSideEffectReturnsSkipSliceViolation self() {
		return this;
	}
}
// === end ===

// === case: if_or_short_circuit_side_effect_returns_skip ===
// skip-reason: condition side effect sits under a short-circuit operator
class InputEmptyBodyIfOrShortCircuitSideEffectReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0 || next()); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	boolean next() {
		return false;
	}
}
// === end ===

// === case: if_semi_with_comment_above_still_violates ===
class InputEmptyBodyIfSemiWithCommentAboveStillViolatesSliceViolation {
	void m(int x) {
		// a comment above annotates the clause, it does not mark the body deliberate
		if (x > 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
	}
}
// === end ===

// === case: if_sharing_line_with_lambda_body_returns_skip ===
// skip-reason: removed construct shares its first or last line with other code
class InputEmptyBodyIfSharingLineWithLambdaBodyReturnsSkipSliceViolation {
	void m(int x) {
		final Runnable r = () -> { if (x > 0); }; // violation: Empty if body, remove it (preserve any side effects in the condition).
		r.run();
	}
}
// === end ===

// === case: if_short_circuit_side_effect_returns_skip ===
// skip-reason: condition side effect sits under a short-circuit operator
class InputEmptyBodyIfShortCircuitSideEffectReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0 && next()); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	boolean next() {
		return false;
	}
}
// === end ===

// === case: if_ternary_side_effect_returns_skip ===
// skip-reason: condition side effect sits under a short-circuit operator
class InputEmptyBodyIfTernarySideEffectReturnsSkipSliceViolation {
	int compute() {
		return 1;
	}

	void m(int x) {
		if (compute() > (x > 0 ? 1 : 2)); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: if_trailing_comment_after_close_brace ===
class InputEmptyBodyIfTrailingCommentAfterCloseBraceSliceViolation {
	void m(int x) {
		if (x > 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		} // the branch is unreachable
	}
}
// === end ===

// === case: if_with_else_branch_returns_skip ===
// skip-reason: empty if body whose else branch would have to be re-attached to a negated condition
class InputEmptyBodyIfWithElseBranchReturnsSkipSliceViolation {
	void m(int x) {
		if (x > 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
		else
			System.out.println(x);
	}
}
// === end ===

// === case: initializer ===
class InputEmptyInitializerViolation {
	static {} // violation: Empty static initializer block, remove it.

	static { // violation: Empty static initializer block, remove it.
	}

	{} // violation: Empty instance initializer block, remove it.

	{ // violation: Empty instance initializer block, remove it.
	}
}
// === end ===

// === case: loop ===
// multi-fix-expected
// imports: java.util.List
class InputEmptyLoopViolation {
	void emptyDoWhileBlock(int x) {
		do { // violation (warning): Empty do-while body, remove it (preserve any side effects in the condition).
		} while (x > 0);
	}

	void emptyDoWhileStatement(int x) {
		do; // violation (warning): Empty do-while body, remove it (preserve any side effects in the condition).
		while (x > 0);
	}

	void emptyForBlock(int x) {
		for (int i = 0; i < x; ++i) { // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
		}
	}

	void emptyForEachStatement(List<String> list) {
		for (String s : list); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	void emptyForStatement(int x) {
		for (int i = 0; i < x; ++i); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	void emptyWhileBlock(int x) {
		while (x > 0) { // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyWhileStatement(int x) {
		while (x > 0); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: mixed_severity ===
class InputEmptyBodyMixedSeverityViolation {
	void m(int x) {
		while (x > 0); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
		if (x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: stray_semi_alone ===
class InputEmptyBodyStraySemiAloneSliceViolation {
	void m(int x) {
		System.out.println(x);
		; // violation: Stray empty statement, remove it.
	}
}
// === end ===

// === case: stray_semi_in_nested_block ===
class InputEmptyBodyStraySemiInNestedBlockSliceViolation {
	void m(int x) {
		{
			System.out.println(x);
			; // violation: Stray empty statement, remove it.
		}
	}
}
// === end ===

// === case: stray_semi_in_static_init ===
class InputEmptyBodyStraySemiInStaticInitSliceViolation {
	static {
		System.out.println("init");
		; // violation: Stray empty statement, remove it.
	}
}
// === end ===

// === case: stray_semi_in_switch_rule_block ===
class InputEmptyBodyStraySemiInSwitchRuleBlockSliceViolation {
	void m(int x) {
		switch (x) {
			case 1 -> {
				System.out.println(x);
				System.out.println(-x);
				; // violation: Stray empty statement, remove it.
			}
			default -> System.out.println(0);
		}
	}
}
// === end ===

// === case: stray_semi_sharing_line_with_braces ===
class InputEmptyBodyStraySemiSharingLineWithBracesSliceViolation {
	void m() {
		{ ; } // violation: Stray empty statement, remove it.
	}
}
// === end ===

// === case: stray_semi_with_trailing_comment ===
class InputEmptyBodyStraySemiWithTrailingCommentSliceViolation {
	void m(int x) {
		System.out.println(x);
		; // a note that outlives the semicolon // violation: Stray empty statement, remove it.
	}
}
// === end ===

// === case: while_condition_call_returns_skip ===
// skip-reason: loop header side effect runs once per iteration and cannot be hoisted out
class InputEmptyBodyWhileConditionCallReturnsSkipSliceViolation {
	void m() {
		while (next()); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
	}

	boolean next() {
		return false;
	}
}
// === end ===

// === case: while_in_braceless_else_returns_skip ===
// skip-reason: removing the construct would leave its enclosing clause without a body
class InputEmptyBodyWhileInBracelessElseReturnsSkipSliceViolation {
	void m(boolean ready, int x) {
		if (ready)
			System.out.println("ready");
		else
			while (x > 0); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
	}
}
// === end ===