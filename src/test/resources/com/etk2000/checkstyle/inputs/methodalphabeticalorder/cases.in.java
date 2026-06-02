package com.etk2000.checkstyle.inputs.methodalphabeticalorder;

// === case: alpha_before_zeta ===
class InputMethodOrderAlphaBeforeZetaSliceViolation {
	void zeta() {
	}

	void alpha() { // violation: Method 'alpha' must appear before 'zeta' (alphabetical order).
	}
}
// === end ===

// === case: beta_before_gamma ===
class InputMethodOrderBetaBeforeGammaSliceViolation {
	void gamma() {
	}

	void beta() { // violation: Method 'beta' must appear before 'gamma' (alphabetical order).
	}
}
// === end ===

// === case: static_alpha_before_beta ===
class InputMethodOrderStaticAlphaBeforeBetaSliceViolation {
	static void beta() {
	}

	static void alpha() { // violation: Method 'alpha' must appear before 'beta' (alphabetical order).
	}

	void bar() {
	}

	void foo() {
	}
}
// === end ===