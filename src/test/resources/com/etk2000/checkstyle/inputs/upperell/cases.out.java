package com.etk2000.checkstyle.inputs.upperell;

// === case: cast_redundant_lowercase ===
class InputUpperEllCastRedundantLowercaseViolation {
	long m() {
		return (long) 100L;
	}
}
// === end ===

// === case: hex_literal ===
class InputUpperEllHexLiteralViolation {
	long m() {
		return 0x123456789L;
	}
}
// === end ===

// === case: replace_lowercase_l ===
class InputUpperEllReplaceLowercaseLViolation {
	long m() {
		return 3000000000L;
	}
}
// === end ===

// === case: small_magnitude ===
class InputUpperEllSmallMagnitudeViolation {
	long m() {
		return 0xFFL;
	}
}
// === end ===

// === case: underscore_literal ===
class InputUpperEllUnderscoreLiteralViolation {
	long m() {
		return 3_000_000_000L;
	}
}
// === end ===