// === case: assignment_body ===
class InputPreferDoWhileAssignmentBodySliceViolation {
	void m(int i) {
		do
			i = i + 1;
		while (i < 10);
	}
}
// === end ===

// === case: body_formatting_skipped ===
// skip-reason: body formatting
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
// skip-reason: braced body multi-statement or unusual closing
class InputPreferDoWhileBracedClosingIndentMismatchSkippedSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
	}
}
// === end ===

// === case: braced_closing_trailing_content_skipped ===
// skip-reason: braced body multi-statement or unusual closing
class InputPreferDoWhileBracedClosingTrailingContentSkippedSliceViolation {
	void m(int i) {
		do ++i;
		while (i < 10);
		// done
	}
}
// === end ===