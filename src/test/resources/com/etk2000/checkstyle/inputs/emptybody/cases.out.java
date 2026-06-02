package com.etk2000.checkstyle.inputs.emptybody;

// === case: body ===
class InputEmptyBodyViolation {
	void emptyElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else {
		}
	}

	void emptyElseIfBlock(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0) {
		}
	}

	void emptyElseIfStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0);
	}

	void emptyElseIfStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			;
	}

	void emptyElseStatement(int x) {
		if (x > 0)
			System.out.println("positive");
		else;
	}

	void emptyElseStatementNextLine(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			;
	}

	void emptyIfBlock(int x) {
		if (x > 0) {
		}
	}

	void emptyIfStatement(int x) {
		if (x > 0);
	}

	void emptyIfStatementNextLine(int x) {
		if (x > 0)
			;
	}

	void emptyIfWithSideEffects(int x) {
		if (++x > 0);
	}
}
// === end ===

// === case: initializer ===
class InputEmptyInitializerViolation {
	static {}

	static {
	}

	{}

	{
	}
}
// === end ===

// === case: loop ===
// imports: java.util.List
class InputEmptyLoopViolation {
	void emptyDoWhileBlock(int x) {
		do {
		} while (x > 0);
	}

	void emptyDoWhileStatement(int x) {
		do;
		while (x > 0);
	}

	void emptyForBlock(int x) {
		for (var i = 0; i < x; ++i) {
		}
	}

	void emptyForEachStatement(List<String> list) {
		for (var s : list);
	}

	void emptyForStatement(int x) {
		for (var i = 0; i < x; ++i);
	}

	void emptyWhileBlock(int x) {
		while (x > 0) {
		}
	}

	void emptyWhileStatement(int x) {
		while (x > 0);
	}
}
// === end ===

// === case: mixed_severity ===
class InputEmptyBodyMixedSeverityViolation {
	void m(int x) {
		while (x > 0);
		if (x > 0);
	}
}
// === end ===