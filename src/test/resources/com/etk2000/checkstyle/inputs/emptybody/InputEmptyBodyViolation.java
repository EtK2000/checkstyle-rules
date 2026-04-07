package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyBodyViolation {
	void emptyElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else { // violation: Empty else body, remove it.
		}
	}

	void emptyElseIfBlock(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyElseIfStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	void emptyElseIfStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
	}

	void emptyElseStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else; // violation: Empty else body, remove it.
	}

	void emptyElseStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: Empty else body, remove it.
			;
	}

	void emptyIfBlock(int x) {
		if (x > 0) { // violation: Empty if body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyIfStatement(int x) {
		if (x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}

	void emptyIfStatementNextLine(int x) {
		if (x > 0) // violation: Empty if body, remove it (preserve any side effects in the condition).
			;
	}

	void emptyIfWithSideEffects(int x) {
		if (++x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}