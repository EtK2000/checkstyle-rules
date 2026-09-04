// === case: column_at_non_literal_char ===
// target: col=8
int x = ;
// === end ===

// === case: hex_digit_e_before_sign ===
// target: col=9
long y = 0x1E+2L;
long z = 1L;
// === end ===

// === case: sign_after_non_exponent_digit ===
// target: col=9
long y = 5+2L;
long z = 1L;
// === end ===
