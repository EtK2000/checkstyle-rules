// === case: digit_inside_identifier_is_stale ===
// target: col=6
long x1l = 3000000000l;
// === end ===

// === case: identifier_ending_in_l_is_stale ===
// target: col=23
long x = 3000000000l + total;
// === end ===

// === case: no_literal_chars_at_column ===
// target: col=9
long x = ;
// === end ===

// === case: unsuffixed_literal_at_column_is_stale ===
// target: col=23
long x = 3000000000l + 1;
// === end ===

// === case: uppercase_suffix_at_column_is_stale ===
// target: col=23
long x = 3000000000l + 1L;
// === end ===
