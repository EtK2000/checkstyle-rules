package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallThisTernaryViolation {
	void thisTernaryNotOnOpening() {
		method( // violation: ternary not on opening paren line
				this, true
						? "a"
						: "b"
		);
	}

	void thisTernaryOnClosing() {
		method(this, true
				? "a"
				: "b"); // violation: arg on closing paren line
	}

	void thisSingleLineTernaryWrongClose() {
		method(this, true ? "a" : "b"
		); // violation: single-line ternary, closing paren on wrong line
	}

	void resourceIdTernaryNotOnOpening() {
		method( // violation: ternary not on opening paren line
				R.string.ok, true
						? "a"
						: "b"
		);
	}

	void method(Object a, Object b) {
	}
}