package com.etk2000.checkstyle.inputs.methodalphabeticalorder;

// === case: alpha_before_zeta ===
class InputMethodOrderAlphaBeforeZetaSliceViolation {
	void zeta() {
	}

	void alpha() {
	}
}
// === end ===

// === case: beta_before_gamma ===
class InputMethodOrderBetaBeforeGammaSliceViolation {
	void gamma() {
	}

	void beta() {
	}
}
// === end ===

// === case: static_alpha_before_beta ===
class InputMethodOrderStaticAlphaBeforeBetaSliceViolation {
	static void beta() {
	}

	static void alpha() {
	}

	void bar() {
	}

	void foo() {
	}
}
// === end ===