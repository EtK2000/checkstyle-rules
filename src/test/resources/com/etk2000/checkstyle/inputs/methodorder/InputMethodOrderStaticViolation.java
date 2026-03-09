package com.etk2000.checkstyle.inputs.methodorder;

class InputMethodOrderStaticViolation {
	static void beta() {
	}

	static void alpha() { // violation: alpha must appear before beta
	}

	// instance methods are sorted correctly (separate tracking)
	void bar() {
	}

	void foo() {
	}
}