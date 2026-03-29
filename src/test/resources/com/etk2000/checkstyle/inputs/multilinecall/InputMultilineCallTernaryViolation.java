package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryViolation {
	void method(Object a) {
	}

	void singleLineTernaryWrongClose() {
		method(true ? "a" : "b"
		); // violation: single-line ternary close paren on wrong line
	}

	void ternaryNotOnOpening() {
		method( // violation: ternary condition not on opening paren line
				true
						? "a"
						: "b"
		);
	}

	void ternaryOnClosing() {
		method(true
				? "a"
				: "b"); // violation: arg on closing paren line
	}
}