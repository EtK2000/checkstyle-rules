// === case: keyword_followed_by_digit_is_stale ===
// target: line=1 col=5
	public void m();
	int static2 = 1;
// === end ===

// === case: keyword_inside_identifier_is_stale ===
// target: col=15
	public void isstatic();
// === end ===

// === case: keyword_prefix_of_identifier_is_stale ===
// target: line=1 col=5
	public void m();
	int final_count = 1;
// === end ===

// === case: not_letter_at_column ===
// target: col=1
	123 method();
// === end ===

// === case: remove_at_end_of_line_no_trailing_space ===
// target: col=4
int abstract
// === end ===
