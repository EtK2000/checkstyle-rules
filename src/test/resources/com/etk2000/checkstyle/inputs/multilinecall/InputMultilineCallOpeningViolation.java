package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallOpeningViolation {
	void method() {
		System.out.println(1, // violation line 5 — arg on opening paren line
				2,
				3
		);

		method(1, // violation line 10 — arg on opening paren line
				2
		);
	}

	void method(int a, int b) {
	}

	void method(int a, int b, int c) {
	}
}