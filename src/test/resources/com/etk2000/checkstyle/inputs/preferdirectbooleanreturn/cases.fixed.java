// === case: blank_line_between_body_and_trailing_skipped ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnBlankLineBetweenBodyAndTrailingSkippedSliceViolation {
	boolean m(boolean flag) {
		if (flag)
			return true;

		return false;
	}
}
// === end ===

// === case: braced_body_close_brace_indent_mismatch ===
// skip-reason: if body is not a simple collapsible return
class InputPreferDirectBooleanReturnBracedBodyCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: braced_else_close_brace_indent_mismatch ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnBracedElseCloseBraceIndentMismatchSliceViolation {
	boolean m(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: trailing_return_indent_mismatch_skipped ===
// skip-reason: no simple collapsible else or trailing return
class InputPreferDirectBooleanReturnTrailingReturnIndentMismatchSkippedSliceViolation {
	boolean m(boolean flag) {
		if (flag)
			return true;
	return false;
	}
}
// === end ===