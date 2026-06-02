// === case: dot_only_value_skipped ===
// target: col=8
	double d = .;
// === end ===

// === case: empty_value_returns_null ===
// target: col=5
	int x = ;
// === end ===

// === case: hex_prefix_only_value_skipped ===
// target: col=5
	int x = 0x;
// === end ===

// === case: no_semicolon ===
// target: col=5
	int x = 0
// === end ===

// === case: non_zero_exponent_no_digits_skipped ===
// target: col=8
	double d = 0.0e;
// === end ===

// === case: non_zero_exponent_nondigit_skipped ===
// target: col=8
	double d = 0.0e9x;
// === end ===

// === case: non_zero_exponent_sign_no_digits_skipped ===
// target: col=8
	double d = 0.0e+;
// === end ===

// === case: unterminated_block_comment_in_value ===
// target: col=5
	int x = 0 /* unterminated
// === end ===
