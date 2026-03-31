package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyInfiniteLoopViolation {
	void emptyDoWhileTrue() {
		do; // violation: empty infinite do-while
		while (true);
	}

	void emptyDoWhileTrueBlock() {
		do { // violation: empty infinite do-while
		} while (true);
	}

	void emptyForever() {
		for (;;); // violation: empty infinite for
	}

	void emptyForeverBlock() {
		for (;;) { // violation: empty infinite for
		}
	}

	void emptyWhileTrue() {
		while (true); // violation: empty infinite while
	}

	void emptyWhileTrueBlock() {
		while (true) { // violation: empty infinite while
		}
	}
}