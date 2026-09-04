package com.etk2000.checkstyle.inputs.preferdowhile;

// === case: array_assign ===
class InputPreferDoWhileArrayAssignSliceViolation {
	void m(int[] arr, int i) {
		do arr[i] = 0;
		while (i-- > 0);
	}
}
// === end ===

// === case: assign_target_forms ===
class InputPreferDoWhileAssignTargetFormsSliceViolation {
	static class Holder {
		int value;
	}

	void arrayElement(int[] values, int i) {
		do values[0] = 1;
		while (i < 10);
	}

	void qualifiedField(Holder h, int i) {
		do h.value = 1;
		while (i < 10);
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

// === case: assignment_in_condition ===
class InputPreferDoWhileAssignmentInConditionSliceViolation {
	void m(int n, int i) {
		do ++i;
		while ((n = next()) != -1);
	}

	int next() {
		return 1;
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

// === case: blank_line_between ===
class InputPreferDoWhileBlankLineBetweenSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: block_comment_in_condition ===
class InputPreferDoWhileBlockCommentInConditionSliceViolation {
	void m(int i) {
		do ++i;
		while (i /* counts up */ < 10);
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

// === case: braced_closing_shallow_indent ===
class InputPreferDoWhileBracedClosingShallowIndentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: braced_closing_trailing_comment ===
class InputPreferDoWhileBracedClosingTrailingCommentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10); // done
	}
}
// === end ===

// === case: case_label_shares_pre_line ===
class InputPreferDoWhileCaseLabelSharesPreLineSliceViolation {
	void m(int x, int i) {
		switch (x) {
			case 1:
				System.out.println(i);
				// falls through
			case 2:
				do ++i;
				while (i < 10);
				break;

			default:
				System.out.println(i);
		}
	}
}
// === end ===

// === case: chained_assign_rhs ===
class InputPreferDoWhileChainedAssignRhsSliceViolation {
	void m(int a, int b, int c, int i) {
		do a = b = c;
		while (i < 10);
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

// === case: comment_before_body_terminator ===
class InputPreferDoWhileCommentBeforeBodyTerminatorSliceViolation {
	void m(int i) {
		do ++i /* kept */;
		while (i < 10); // moved
	}
}
// === end ===

// === case: comment_lookalike_in_literal ===
class InputPreferDoWhileCommentLookalikeInLiteralSliceViolation {
	void m(String s, int i) {
		do s = "// not a comment";
		while (i < 10);
	}
}
// === end ===

// === case: comment_on_pre_line ===
class InputPreferDoWhileCommentOnPreLineSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10); // comment
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

// === case: compound_assign_operators ===
class InputPreferDoWhileCompoundAssignOperatorsSliceViolation {
	void bitAnd(int n, int i) {
		do n &= 2;
		while (i < 10);
	}

	void bitOr(int n, int i) {
		do n |= 2;
		while (i < 10);
	}

	void bitXor(int n, int i) {
		do n ^= 2;
		while (i < 10);
	}

	void divide(int n, int i) {
		do n /= 2;
		while (i < 10);
	}

	void minus(int n, int i) {
		do n -= 2;
		while (i < 10);
	}

	void modulo(int n, int i) {
		do n %= 2;
		while (i < 10);
	}

	void shiftLeft(int n, int i) {
		do n <<= 2;
		while (i < 10);
	}

	void shiftRight(int n, int i) {
		do n >>= 2;
		while (i < 10);
	}

	void shiftRightUnsigned(int n, int i) {
		do n >>>= 2;
		while (i < 10);
	}

	void times(int n, int i) {
		do n *= 2;
		while (i < 10);
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

// === case: fallthrough_case_group ===
class InputPreferDoWhileFallthroughCaseGroupSliceViolation {
	void m(int x, int i) {
		switch (x) {
			case 1:
				++i;
				// falls through
			case 2:
				do ++i;
				while (i < 10);
				break;

			default:
				System.out.println(i);
		}
	}
}
// === end ===

// === case: infinite_condition ===
class InputPreferDoWhileInfiniteConditionSliceViolation {
	void m(int i) {
		do ++i;
		while (true);
	}
}
// === end ===

// === case: line_budget_exceeded ===
// skip-reason: cannot convert to do-while: the collapsed loop would exceed the line length limit
class InputPreferDoWhileLineBudgetExceededSliceViolation {
	void m(int accumulatorValue, int firstOperandValue, int secondOperandValue, int thirdOperandValue) {
		accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
				+ firstOperandValue + secondOperandValue + thirdOperandValue;
		while (accumulatorValue < firstOperandValue)
			accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
					+ firstOperandValue + secondOperandValue + thirdOperandValue;
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
		do ++i;
		while (i < 10); // walking forward
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

// === case: multiline_body_statement ===
class InputPreferDoWhileMultilineBodyStatementSliceViolation {
	void m(int x, int a, int b, int i) {
		do
			x = a + b;
		while (i < 10);
	}
}
// === end ===

// === case: multiline_condition_after_op ===
class InputPreferDoWhileMultilineConditionAfterOpSliceViolation {
	void m(int i, int j) {
		do ++i;
		while (i < 10 && j > 0);
	}
}
// === end ===

// === case: multiline_condition_before_op ===
class InputPreferDoWhileMultilineConditionBeforeOpSliceViolation {
	void m(int i, int j) {
		do ++i;
		while (i < 10 && j > 0);
	}
}
// === end ===

// === case: multiline_condition_comment_skipped ===
// skip-reason: cannot convert to do-while: a text block or comment inside the loop cannot survive the collapse
class InputPreferDoWhileMultilineConditionCommentSkippedSliceViolation {
	void m(int i, int j) {
		++i;
		while (i < 10
				// both bounds matter
				&& j > 0)
			++i;
	}
}
// === end ===

// === case: multiline_pre_statement ===
class InputPreferDoWhileMultilinePreStatementSliceViolation {
	void m(StringBuilder sb, String text, int i) {
		do sb.append(text);
		while (i < 10);
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

// === case: over_budget_line_shares_pre_statement ===
class InputPreferDoWhileOverBudgetLineSharesPreStatementSliceViolation {
	void m(StringBuilder destinationBuilder, String firstAppendedValue, String secondAppendedValue, int i) {
		destinationBuilder.append(firstAppendedValue).append(secondAppendedValue).append(firstAppendedValue).append(secondAppendedValue);
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: pre_statement_deeper_mixed_indent ===
class InputPreferDoWhilePreStatementDeeperMixedIndentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: pre_statement_shallower_indent ===
class InputPreferDoWhilePreStatementShallowerIndentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
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

// === case: receiver_shapes ===
class InputPreferDoWhileReceiverShapesSliceViolation {
	void castReceiver(Object o, int i) {
		do ((String) o).length();
		while (i < 10);
	}

	void indexReceiver(String[] parts, int i) {
		do parts[0].length();
		while (i < 10);
	}
}
// === end ===

// === case: shared_line_pre_and_while ===
class InputPreferDoWhileSharedLinePreAndWhileSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: string_literal_lookalikes ===
class InputPreferDoWhileStringLiteralLookalikesSliceViolation {
	void m(String s, int i) {
		do s = "while (x) /* still a string */";
		while (i < 10);
	}
}
// === end ===

// === case: text_block_body_skipped ===
// skip-reason: cannot convert to do-while: a text block or comment inside the loop cannot survive the collapse
class InputPreferDoWhileTextBlockBodySkippedSliceViolation {
	void m(StringBuilder sb, int i) {
		sb.append("""
				line
				""");
		while (i < 10) {
			sb.append("""
				line
				""");
		}
	}
}
// === end ===

// === case: tier2_braced_one_liner ===
class InputPreferDoWhileTier2BracedOneLinerSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: tier2_call_with_call_argument ===
class InputPreferDoWhileTier2CallWithCallArgumentSliceViolation {
	void m(StringBuilder sb, String text, int i) {
		do sb.append(text.trim());
		while (i < 10);
	}
}
// === end ===

// === case: tier2_unbraced_one_liner ===
class InputPreferDoWhileTier2UnbracedOneLinerSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: tier3_bare_new_statement ===
class InputPreferDoWhileTier3BareNewStatementSliceViolation {
	void m(int i) {
		do
			new StringBuilder();
		while (i < 10);
	}
}
// === end ===

// === case: tier3_braced_body ===
class InputPreferDoWhileTier3BracedBodySliceViolation {
	void m(int x, int a, int b, int i) {
		do
			x = a + b;
		while (i < 10);
	}
}
// === end ===

// === case: tier3_braced_one_liner ===
class InputPreferDoWhileTier3BracedOneLinerSliceViolation {
	void m(int x, int a, int b, int i) {
		do
			x = a + b;
		while (i < 10);
	}
}
// === end ===

// === case: tier3_compound_assign_binary_rhs ===
class InputPreferDoWhileTier3CompoundAssignBinaryRhsSliceViolation {
	void m(int a, int b, int i) {
		do
			i += a + b;
		while (i < 10);
	}
}
// === end ===

// === case: tier3_unbraced_one_liner ===
class InputPreferDoWhileTier3UnbracedOneLinerSliceViolation {
	void m(int x, int a, int b, int i) {
		do
			x = a + b;
		while (i < 10);
	}
}
// === end ===

// === case: trailing_comment_after_body ===
class InputPreferDoWhileTrailingCommentAfterBodySliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10); // comment
	}
}
// === end ===

// === case: trailing_statement_after_while ===
class InputPreferDoWhileTrailingStatementAfterWhileSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10); System.out.println(i);
	}
}
// === end ===

// === case: while_in_lambda_body ===
// imports: java.util.List
class InputPreferDoWhileWhileInLambdaBodySliceViolation {
	private int counter;

	void m(List<String> names) {
		names.forEach(v -> {
			do ++counter;
			while (counter < 10);
		});
	}
}
// === end ===

// === case: while_line_has_trailing_comment ===
class InputPreferDoWhileWhileLineHasTrailingCommentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10); // loop
	}
}
// === end ===

// === case: whitespace_only_difference ===
class InputPreferDoWhileWhitespaceOnlyDifferenceSliceViolation {
	void m(int i) {
		do i += 2;
		while (i < 10);
	}
}
// === end ===