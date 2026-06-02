// === case: braced_body_truncated ===
// target: col=2
		if (flag) {
			return true;
// === end ===

// === case: braced_else_truncated ===
// target: col=2
		if (flag) {
			return true;
		}
		else {
			return false;
// === end ===

// === case: empty_condition_skipped ===
// target: col=2
		if () return true;
		return false;
// === end ===

// === case: if_without_open_paren ===
// target: col=2
		if x return true;
		return false;
// === end ===

// === case: multiline_close_paren_never_found ===
// target: col=2
		if (a
				&& b
// === end ===

// === case: multiline_unicode_escape_in_cond_skipped ===
// target: col=2
		if (a
				&& b\u0029 c) return true;
		return false;
// === end ===

// === case: multiline_unterminated_block_comment_skipped ===
// target: col=2
		if (/* unterminated
				flag) return true;
		return false;
// === end ===

// === case: next_line_body_no_body_line_skipped ===
// target: col=2
		if (flag)
// === end ===

// === case: unicode_escape_in_cond_skipped ===
// target: col=2
		if (foo\u0029) return true;
		return false;
// === end ===

// === case: unterminated_block_comment_in_cond_skipped ===
// target: col=2
		if (/* unterm flag) return true;
		return false;
// === end ===