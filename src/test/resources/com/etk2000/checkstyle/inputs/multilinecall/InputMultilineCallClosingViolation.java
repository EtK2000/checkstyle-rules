package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallClosingViolation {
	void method() {
		System.out.println(
				1,
				2,
				3); // violation line 8 — arg on closing paren line

		method(
				1, 2); // violation line 11 — arg on closing paren line
	}

	void method(int a, int b) {
	}

	void method(int a, int b, int c) {
	}
}