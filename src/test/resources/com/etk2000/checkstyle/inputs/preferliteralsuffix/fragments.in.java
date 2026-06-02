// === case: column_not_open_paren ===
// target: col=1
(long) x * 100;
// === end ===

// === case: forward_subject_at_eol ===
// target: col=0
(long) x
// === end ===

// === case: malformed_cast_no_expression ===
// target: col=0
(long)
// === end ===