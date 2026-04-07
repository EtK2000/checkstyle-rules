package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallThisTernaryViolation {
	void method(Object a, Object b) {
	}

	void resourceIdTernaryNotOnOpening() {
		method( // violation: Ternary argument: condition must be on the opening paren line.
				R.string.ok, true
						? "a"
						: "b"
		);
	}

	void thisSingleLineTernaryWrongClose() {
		method(this, true ? "a" : "b"
		); // violation: Single-line ternary argument: closing paren must be on the same line.
	}

	void thisTernaryNotOnOpening() {
		method( // violation: Ternary argument: condition must be on the opening paren line.
				this, true
						? "a"
						: "b"
		);
	}

	void thisTernaryOnClosing() {
		method(this, true
				? "a"
				: "b"); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}
}