// === case: column_not_open_paren ===
// target: line=1 col=10
	long m(int x) {
		return (long) x * 100;
	}
// === end ===

// === case: forward_subject_at_eol ===
// target: col=0
(long) x
// === end ===

// === case: malformed_cast_no_expression ===
// target: col=0
(long)
// === end ===

// === case: stale_column_after_sibling_fix ===
// target: line=1 col=26
	long m(int a, int b) {
		return a * 100L + (long) b * 200;
	}
// === end ===