package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyBodyViolation {
	void emptyElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else { // violation: empty else body
		}
	}

	void emptyElseIfBlock(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) { // violation: empty else-if body
		}
	}

	void emptyElseIfStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0); // violation: empty else-if body
	}

	void emptyElseIfStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) // violation: empty else-if body
			;
	}

	void emptyElseStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else; // violation: empty else body
	}

	void emptyElseStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: empty else body
			;
	}

	void emptyIfBlock(int x) {
		if (x > 0) { // violation: empty if body
		}
	}

	void emptyIfStatement(int x) {
		if (x > 0); // violation: empty if body
	}

	void emptyIfStatementNextLine(int x) {
		if (x > 0) // violation: empty if body
			;
	}

	void emptyIfWithSideEffects(int x) {
		if (++x > 0); // violation: empty if body (side effects in condition)
	}
}