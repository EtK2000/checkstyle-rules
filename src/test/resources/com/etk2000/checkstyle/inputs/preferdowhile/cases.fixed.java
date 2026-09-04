// === case: body_formatting_skipped ===
// skip-reason: cannot convert to do-while: the loop body does not end in a semicolon
class InputPreferDoWhileBodyFormattingSkippedSliceViolation {
	void m(int x, int a, int b, int i) {
		x = a + b;
		while (i < 10) {
			x = a
				+ b;
		}
	}
}
// === end ===

// === case: braced_closing_indent_mismatch_skipped ===
// skip-reason: cannot convert to do-while: the braced body has more than one statement or an unusual closing brace
class InputPreferDoWhileBracedClosingIndentMismatchSkippedSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: braced_closing_trailing_content_skipped ===
// skip-reason: cannot convert to do-while: the braced body has more than one statement or an unusual closing brace
class InputPreferDoWhileBracedClosingTrailingContentSkippedSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
		// done
	}
}
// === end ===