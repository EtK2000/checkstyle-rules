package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyBodyViolation {
	void emptyIfBlock(int x) {
		if (x > 0) { // violation: empty if body
		}
	}

	void emptyIfStatement(int x) {
		if (x > 0); // violation: empty if body
	}

	void emptyElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else { // violation: empty else body
		}
	}

	void emptyElseStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else; // violation: empty else body
	}

	void emptyIfWithSideEffects(int x) {
		if (++x > 0); // violation: empty if body (side effects in condition)
	}
}