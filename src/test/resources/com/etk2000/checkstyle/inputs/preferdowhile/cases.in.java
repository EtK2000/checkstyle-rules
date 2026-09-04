package com.etk2000.checkstyle.inputs.preferdowhile;

// === case: array_assign ===
class InputPreferDoWhileArrayAssignSliceViolation {
	void m(int[] arr, int i) {
		arr[i] = 0;
		while (i-- > 0) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			arr[i] = 0;
	}
}
// === end ===

// === case: assign_target_forms ===
// multi-fix-expected
class InputPreferDoWhileAssignTargetFormsSliceViolation {
	static class Holder {
		int value;
	}

	void arrayElement(int[] values, int i) {
		values[0] = 1;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			values[0] = 1;
	}

	void qualifiedField(Holder h, int i) {
		h.value = 1;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			h.value = 1;
	}
}
// === end ===

// === case: assignment_body ===
class InputPreferDoWhileAssignmentBodySliceViolation {
	void m(int i) {
		i = i + 1;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			i = i + 1;
	}
}
// === end ===

// === case: assignment_in_condition ===
class InputPreferDoWhileAssignmentInConditionSliceViolation {
	void m(int n, int i) {
		++i;
		while ((n = next()) != -1) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
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
		foo();
		while (cond()) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			foo();
	}
}
// === end ===

// === case: bare_method_call ===
class InputPreferDoWhileBareMethodCallSliceViolation {
	void m(int i) {
		System.out.println(i);
		while (--i > 0) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			System.out.println(i);
	}
}
// === end ===

// === case: blank_line_between ===
class InputPreferDoWhileBlankLineBetweenSliceViolation {
	void m(int i) {
		++i;

		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: block_comment_in_condition ===
class InputPreferDoWhileBlockCommentInConditionSliceViolation {
	void m(int i) {
		++i;
		while (i /* counts up */ < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: braced_body ===
class InputPreferDoWhileBracedBodySliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
		}
	}
}
// === end ===

// === case: braced_closing_shallow_indent ===
class InputPreferDoWhileBracedClosingShallowIndentSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
	}
}
// === end ===

// === case: braced_closing_trailing_comment ===
class InputPreferDoWhileBracedClosingTrailingCommentSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
		} // done
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
			case 2: ++i;
				while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
					++i;
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
		a = b = c;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			a = b = c;
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
		while (node != null) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			node = node.next();
	}
}
// === end ===

// === case: chained_call_body ===
class InputPreferDoWhileChainedCallBodySliceViolation {
	void m(StringBuilder sb, int i) {
		sb.append("a").append("b");
		while (--i > 0) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			sb.append("a").append("b");
	}
}
// === end ===

// === case: comment_before_body_terminator ===
class InputPreferDoWhileCommentBeforeBodyTerminatorSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i /* kept */; // moved
	}
}
// === end ===

// === case: comment_lookalike_in_literal ===
class InputPreferDoWhileCommentLookalikeInLiteralSliceViolation {
	void m(String s, int i) {
		s = "// not a comment";
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			s = "// not a comment";
	}
}
// === end ===

// === case: comment_on_pre_line ===
class InputPreferDoWhileCommentOnPreLineSliceViolation {
	void m(int i) {
		++i; // comment
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: compound_assign_body ===
class InputPreferDoWhileCompoundAssignBodySliceViolation {
	void m(int i) {
		i += 2;
		while (i < 100) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			i += 2;
	}
}
// === end ===

// === case: compound_assign_operators ===
// multi-fix-expected
class InputPreferDoWhileCompoundAssignOperatorsSliceViolation {
	void bitAnd(int n, int i) {
		n &= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n &= 2;
	}

	void bitOr(int n, int i) {
		n |= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n |= 2;
	}

	void bitXor(int n, int i) {
		n ^= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n ^= 2;
	}

	void divide(int n, int i) {
		n /= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n /= 2;
	}

	void minus(int n, int i) {
		n -= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n -= 2;
	}

	void modulo(int n, int i) {
		n %= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n %= 2;
	}

	void shiftLeft(int n, int i) {
		n <<= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n <<= 2;
	}

	void shiftRight(int n, int i) {
		n >>= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n >>= 2;
	}

	void shiftRightUnsigned(int n, int i) {
		n >>>= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n >>>= 2;
	}

	void times(int n, int i) {
		n *= 2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			n *= 2;
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
		while ((i < 10) && check(i)) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
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
				++i;
				while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
					++i;
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
		++i;
		while (true) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: line_budget_exceeded ===
// skip-reason: cannot convert to do-while: the collapsed loop would exceed the line length limit
class InputPreferDoWhileLineBudgetExceededSliceViolation {
	void m(int accumulatorValue, int firstOperandValue, int secondOperandValue, int thirdOperandValue) {
		accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
				+ firstOperandValue + secondOperandValue + thirdOperandValue;
		while (accumulatorValue < firstOperandValue) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
					+ firstOperandValue + secondOperandValue + thirdOperandValue;
	}
}
// === end ===

// === case: main ===
class InputPreferDoWhileViolation {
	void bracedBody(int i) {
		++i;
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
		}
	}

	void commentBetween(int i) {
		++i;
		// walking forward
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: method_call_body ===
// imports: java.util.List
class InputPreferDoWhileMethodCallBodySliceViolation {
	void m(List<Integer> list) {
		list.add(1);
		while (list.size() < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			list.add(1);
	}
}
// === end ===

// === case: multiline_body_statement ===
class InputPreferDoWhileMultilineBodyStatementSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			x = a
				+ b;
	}
}
// === end ===

// === case: multiline_condition_after_op ===
class InputPreferDoWhileMultilineConditionAfterOpSliceViolation {
	void m(int i, int j) {
		++i;
		while (i < 10 && // violation: Replace the pre-loop statement and 'while' with 'do-while'.
				j > 0)
			++i;
	}
}
// === end ===

// === case: multiline_condition_before_op ===
class InputPreferDoWhileMultilineConditionBeforeOpSliceViolation {
	void m(int i, int j) {
		++i;
		while (i < 10 // violation: Replace the pre-loop statement and 'while' with 'do-while'.
				&& j > 0)
			++i;
	}
}
// === end ===

// === case: multiline_condition_comment_skipped ===
// skip-reason: cannot convert to do-while: a text block or comment inside the loop cannot survive the collapse
class InputPreferDoWhileMultilineConditionCommentSkippedSliceViolation {
	void m(int i, int j) {
		++i;
		while (i < 10 // violation: Replace the pre-loop statement and 'while' with 'do-while'.
				// both bounds matter
				&& j > 0)
			++i;
	}
}
// === end ===

// === case: multiline_pre_statement ===
class InputPreferDoWhileMultilinePreStatementSliceViolation {
	void m(StringBuilder sb, String text, int i) {
		sb.append(
				text
		);
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			sb.append(text);
	}
}
// === end ===

// === case: nested_in_for_body ===
class InputPreferDoWhileNestedInForBodySliceViolation {
	void m(int n) {
		for (var k = 0; k < n; ++k) {
			var i = 0;
			++i;
			while (i < n) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
				++i;
		}
	}
}
// === end ===

// === case: new_expression_body ===
class InputPreferDoWhileNewExpressionBodySliceViolation {
	void m(int i) {
		StringBuilder sb;
		sb = new StringBuilder();
		while (--i > 0) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			sb = new StringBuilder();
	}
}
// === end ===

// === case: over_budget_line_shares_pre_statement ===
class InputPreferDoWhileOverBudgetLineSharesPreStatementSliceViolation {
	void m(StringBuilder destinationBuilder, String firstAppendedValue, String secondAppendedValue, int i) {
		destinationBuilder.append(firstAppendedValue).append(secondAppendedValue).append(firstAppendedValue).append(secondAppendedValue); ++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: pre_statement_deeper_mixed_indent ===
class InputPreferDoWhilePreStatementDeeperMixedIndentSliceViolation {
	void m(int i) {
		 	++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: pre_statement_shallower_indent ===
class InputPreferDoWhilePreStatementShallowerIndentSliceViolation {
	void m(int i) {
	++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: prefix_decrement ===
class InputPreferDoWhilePrefixDecrementSliceViolation {
	void m(int i) {
		--i;
		while (i > 0) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			--i;
	}
}
// === end ===

// === case: prefix_inc ===
class InputPreferDoWhilePrefixIncSliceViolation {
	void m(int i, String text) {
		++i;
		while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: prefix_increment ===
class InputPreferDoWhilePrefixIncrementSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: receiver_shapes ===
// multi-fix-expected
class InputPreferDoWhileReceiverShapesSliceViolation {
	void castReceiver(Object o, int i) {
		((String) o).length();
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			((String) o).length();
	}

	void indexReceiver(String[] parts, int i) {
		parts[0].length();
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			parts[0].length();
	}
}
// === end ===

// === case: shared_line_pre_and_while ===
class InputPreferDoWhileSharedLinePreAndWhileSliceViolation {
	void m(int i) {
		++i; while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: string_literal_lookalikes ===
class InputPreferDoWhileStringLiteralLookalikesSliceViolation {
	void m(String s, int i) {
		s = "while (x) /* still a string */";
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			s = "while (x) /* still a string */";
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
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
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
		++i;
		while (i < 10) { ++i; } // violation: Replace the pre-loop statement and 'while' with 'do-while'.
	}
}
// === end ===

// === case: tier2_call_with_call_argument ===
class InputPreferDoWhileTier2CallWithCallArgumentSliceViolation {
	void m(StringBuilder sb, String text, int i) {
		sb.append(text.trim());
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			sb.append(text.trim());
	}
}
// === end ===

// === case: tier2_unbraced_one_liner ===
class InputPreferDoWhileTier2UnbracedOneLinerSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) ++i; // violation: Replace the pre-loop statement and 'while' with 'do-while'.
	}
}
// === end ===

// === case: tier3_bare_new_statement ===
class InputPreferDoWhileTier3BareNewStatementSliceViolation {
	void m(int i) {
		new StringBuilder();
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			new StringBuilder();
	}
}
// === end ===

// === case: tier3_braced_body ===
class InputPreferDoWhileTier3BracedBodySliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) { // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			x = a + b;
		}
	}
}
// === end ===

// === case: tier3_braced_one_liner ===
class InputPreferDoWhileTier3BracedOneLinerSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) { x = a + b; } // violation: Replace the pre-loop statement and 'while' with 'do-while'.
	}
}
// === end ===

// === case: tier3_compound_assign_binary_rhs ===
class InputPreferDoWhileTier3CompoundAssignBinaryRhsSliceViolation {
	void m(int a, int b, int i) {
		i += a + b;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			i += a + b;
	}
}
// === end ===

// === case: tier3_unbraced_one_liner ===
class InputPreferDoWhileTier3UnbracedOneLinerSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) x = a + b; // violation: Replace the pre-loop statement and 'while' with 'do-while'.
	}
}
// === end ===

// === case: trailing_comment_after_body ===
class InputPreferDoWhileTrailingCommentAfterBodySliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i; // comment
	}
}
// === end ===

// === case: trailing_statement_after_while ===
class InputPreferDoWhileTrailingStatementAfterWhileSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) ++i; System.out.println(i); // violation: Replace the pre-loop statement and 'while' with 'do-while'.
	}
}
// === end ===

// === case: while_in_lambda_body ===
// imports: java.util.List
class InputPreferDoWhileWhileInLambdaBodySliceViolation {
	private int counter;

	void m(List<String> names) {
		names.forEach(v -> {
			++counter;
			while (counter < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
				++counter;
		});
	}
}
// === end ===

// === case: while_line_has_trailing_comment ===
class InputPreferDoWhileWhileLineHasTrailingCommentSliceViolation {
	void m(int i) {
		++i;
		while (i < 10) // loop // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			++i;
	}
}
// === end ===

// === case: whitespace_only_difference ===
class InputPreferDoWhileWhitespaceOnlyDifferenceSliceViolation {
	void m(int i) {
		i +=2;
		while (i < 10) // violation: Replace the pre-loop statement and 'while' with 'do-while'.
			i += 2;
	}
}
// === end ===