package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryViolation {
	void method(Object a) {
	}

	void singleLineTernaryWrongClose() {
		method(true ? "a" : "b"
		); // violation: Single-line ternary argument: closing paren must be on the same line.
	}

	void ternaryNotOnOpening() {
		method( // violation: Ternary argument: condition must be on the opening paren line.
				true
						? "a"
						: "b"
		);
	}

	void ternaryOnClosing() {
		method(true
				? "a"
				: "b"); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}
}