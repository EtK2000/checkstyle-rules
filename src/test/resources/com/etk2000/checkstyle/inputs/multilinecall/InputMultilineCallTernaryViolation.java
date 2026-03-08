package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryViolation {
	void ternaryNotOnOpening() {
		method( // violation line 5 — ternary condition not on opening paren line
				true
						? "a"
						: "b"
		);
	}

	void ternaryOnClosing() {
		method(true
				? "a"
				: "b"); // violation line 15 — arg on closing paren line
	}

	void singleLineTernaryWrongClose() {
		method(true ? "a" : "b"
		); // violation line 20 — single-line ternary close paren on wrong line
	}

	void method(Object a) {
	}
}