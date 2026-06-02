package com.etk2000.checkstyle.inputs.emptybody;

// === case: body ===
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
// === end ===

// === case: initializer ===
class InputEmptyInitializerViolation {
	static {} // violation: Empty static initializer block, remove it.

	static { // violation: Empty static initializer block, remove it.
	}

	{} // violation: Empty instance initializer block, remove it.

	{ // violation: Empty instance initializer block, remove it.
	}
}
// === end ===

// === case: loop ===
// imports: java.util.List
class InputEmptyLoopViolation {
	void emptyDoWhileBlock(int x) {
		do { // violation (warning): Empty do-while body, remove it (preserve any side effects in the condition).
		} while (x > 0);
	}

	void emptyDoWhileStatement(int x) {
		do; // violation (warning): Empty do-while body, remove it (preserve any side effects in the condition).
		while (x > 0);
	}

	void emptyForBlock(int x) {
		for (int i = 0; i < x; ++i) { // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
		}
	}

	void emptyForEachStatement(List<String> list) {
		for (String s : list); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	void emptyForStatement(int x) {
		for (int i = 0; i < x; ++i); // violation (warning): Empty for body, remove it (preserve any side effects in the condition/update).
	}

	void emptyWhileBlock(int x) {
		while (x > 0) { // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
		}
	}

	void emptyWhileStatement(int x) {
		while (x > 0); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
	}
}
// === end ===

// === case: mixed_severity ===
class InputEmptyBodyMixedSeverityViolation {
	void m(int x) {
		while (x > 0); // violation (warning): Empty while body, remove it (preserve any side effects in the condition).
		if (x > 0); // violation: Empty if body, remove it (preserve any side effects in the condition).
	}
}
// === end ===