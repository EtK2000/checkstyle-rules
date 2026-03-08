package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowClean {
	void singleLineBodies(int x) {
		if (x > 0)
			System.out.println("positive");

		while (x > 0)
			--x;

		for (int i = 0; i < x; ++i)
			System.out.println(i);

		do
			--x;
		while (x > 0);
	}

	void multiLineBodiesWithBraces(int x) {
		if (x > 0) {
			System.out.println("positive");
			++x;
		}

		while (x > 0) {
			System.out.println(x);
			--x;
		}

		for (int i = 0; i < x; ++i) {
			System.out.println(i);
			System.out.println(i + 1);
		}

		do {
			System.out.println(x);
			--x;
		} while (x > 0);
	}

	void elseIfChain(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			System.out.println("negative");
		else
			System.out.println("zero");
	}

	void nestedIndependent(int x) {
		for (int i = 0; i < x; ++i) {
			if (i > 0)
				System.out.println(i);
		}
	}

	void multiLineExpression(int x) {
		if (x > 0) {
			System.out.println(
					"hello"
			);
		}
	}

	void bracedMultiLineBody(int x) {
		if (x > 0) {
			for (int i = 0; i < x; ++i)
				System.out.println(i);
		}
	}

	void emptyStatement(int x) {
		for (; x > 0; --x)
			;
	}

	void forWithSemicolons() {
		for (int i = 0; i < 10; ++i)
			System.out.println(i);
	}
}