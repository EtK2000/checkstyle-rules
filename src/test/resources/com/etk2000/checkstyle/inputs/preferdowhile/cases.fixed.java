// === case: braced_closing_trailing_comment ===
class InputPreferDoWhileBracedClosingTrailingCommentSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
		// done
	}
}
// === end ===

// === case: line_budget_exceeded ===
// skip-reason: cannot convert to do-while: the collapsed loop would exceed the line length limit
class InputPreferDoWhileLineBudgetExceededSliceViolation {
	void m(int accumulatorValue, int firstOperandValue, int secondOperandValue, int thirdOperandValue) {
		accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
				+ firstOperandValue + secondOperandValue + thirdOperandValue;
		while (accumulatorValue < firstOperandValue) {
			accumulatorValue += firstOperandValue + secondOperandValue + thirdOperandValue
					+ firstOperandValue + secondOperandValue + thirdOperandValue;
		}
	}
}
// === end ===

// === case: trailing_statement_after_while ===
class InputPreferDoWhileTrailingStatementAfterWhileSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
		System.out.println(i);
	}
}
// === end ===