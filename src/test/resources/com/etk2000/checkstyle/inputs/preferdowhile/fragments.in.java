// === case: braced_body_missing_closing_line ===
// target: line=1 col=16
		++i;
		while (i < 10) {
			++i;
// === end ===

// === case: while_is_last_line ===
// target: line=1 col=16
		++i;
		while (i < 10)
// === end ===

// === case: while_line_format_not_matched ===
// target: line=1 col=16
		++i;
		notAWhile (i < 10)
			++i;
// === end ===
