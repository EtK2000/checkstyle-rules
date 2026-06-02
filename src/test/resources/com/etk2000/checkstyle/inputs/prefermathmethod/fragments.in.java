// === case: if_no_body_line_returns_skip ===
// target: col=0
		if (a > b)
// === end ===

// === case: no_match_clamp_unbalanced_parens ===
// target: col=0
		return Math.max(a, Math.min(b, c);
// === end ===