package com.etk2000.checkstyle.inputs.infiniteemptyloop;

class InputInfiniteEmptyLoopClean {
	// Boundary partners for the comment-bearing pins: a comment is present in each, but one of the
	// two conditions for a hang is missing, so none of these may fire.
	void commentedNonEmptyWhileTrue() {
		while (true) { /* spin */
			return;
		}
	}

	void commentedNonInfiniteDoWhile(int x) {
		do /* spin */;
		while (x > 0);
	}

	void commentedNonInfiniteFor(int x) {
		for (; /* c */ x > 0;);
	}

	void commentedNonInfiniteWhile(int x) {
		while (x > 0) /* spin */;
	}

	// Comment-free partners for the two above: these are clean because the condition is not `true`,
	// not because a comment broke the walk. The `for` axis already has both halves.
	void emptyNonInfiniteDoWhile(int x) {
		do;
		while (x > 0);
	}

	void emptyNonInfiniteWhile(int x) {
		while (x > 0);
	}

	void nonEmptyDoWhileTrue() {
		do {
			return;
		} while (true);
	}

	void nonEmptyForEmpty() {
		for (;;) {
			break;
		}
	}

	void nonEmptyForTrue() {
		for (;true;) {
			break;
		}
	}

	void nonEmptyWhileTrue() {
		while (true) {
			return;
		}
	}

	void nonInfiniteFor(int x) {
		for (var i = 0; i < x; ++i)
			System.out.println(i);
	}

	void nonInfiniteForEmptyInit(int x) {
		for (; x > 0;);
	}

	void nonInfiniteWhile(int x) {
		while (x > 0)
			--x;
	}
}