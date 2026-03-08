package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallThisTernaryViolation {
	void thisTernaryNotOnOpening() {
		method( // violation line 5 — ternary not on opening paren line
				this, true
						? "a"
						: "b"
		);
	}

	void thisTernaryOnClosing() {
		method(this, true
				? "a"
				: "b"); // violation line 15 — arg on closing paren line
	}

	void thisSingleLineTernaryWrongClose() {
		method(this, true ? "a" : "b"
		); // violation line 20 — single-line ternary, closing paren on wrong line
	}

	void resourceIdTernaryNotOnOpening() {
		method( // violation line 24 — ternary not on opening paren line
				R.string.ok, true
						? "a"
						: "b"
		);
	}

	void method(Object a, Object b) {
	}
}