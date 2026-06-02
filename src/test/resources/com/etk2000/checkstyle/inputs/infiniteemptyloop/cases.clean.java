package com.etk2000.checkstyle.inputs.infiniteemptyloop;

class InputInfiniteEmptyLoopClean {
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