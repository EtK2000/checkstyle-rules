package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyBodyClean {
	void bracedDoWhile(int x) {
		do {
			--x;
		} while (x > 0);
	}

	void bracedFor(int x) {
		for (int i = 0; i < x; ++i) {
			System.out.println(i);
		}
	}

	void bracedIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
	}

	void bracedIfElse(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else {
			System.out.println("non-positive");
		}
	}

	void bracedIfElseIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else if (x < 0) {
			System.out.println("negative");
		}
		else {
			System.out.println("zero");
		}
	}

	void normalIf(int x) {
		if (x > 0)
			System.out.println("positive");
	}

	void normalIfElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			System.out.println("non-positive");
	}

	void normalIfElseIf(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			System.out.println("negative");
		else
			System.out.println("zero");
	}

	void normalWhile(int x) {
		while (x > 0)
			--x;
	}

	void singleLineDoWhile(int x) {
		do
			--x;
		while (x > 0);
	}

	void singleLineFor(int x) {
		for (int i = 0; i < x; ++i)
			System.out.println(i);
	}
}