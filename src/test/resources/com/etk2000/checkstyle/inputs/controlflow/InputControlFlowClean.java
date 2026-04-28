package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowClean {
	void bracedMultiLineBody(int x) {
		if (x > 0) {
			for (int i = 0; i < x; ++i)
				System.out.println(i);
		}
	}

	void doWhileEmptyBody(int x) {
		do; while (x > 0);
	}

	void doWhileNested(int x) {
		do {
			do --x;
			while (x > 5);
			++x;
		} while (x > 0);
	}

	void doWhileNestedInFor(int x) {
		for (int i = 0; i < x; ++i) {
			do --x;
			while (x > 5);
			System.out.println(i);
		}
	}

	void doWhileTier2CompoundWhile(int x) {
		do --x;
		while (x > 0 && x < 100);

		do ++x;
		while (x > 0 || x < 100);

		do System.out.println(x);
		while (x > 0 && x < 100);
	}

	void doWhileTier2DottedBody(int x) {
		do System.out.println(x);
		while (x > 0);

		final var list = List.of("a");
		do list.add("b");
		while (list.size() < 10);
	}

	void doWhileTier2DottedRhs(int x) {
		final var list = List.of("a");
		do x = list.size();
		while (x > 0);
	}

	void doWhileTier2FieldAccessRhs(int x) {
		do x = System.out.hashCode();
		while (x > 0);
	}

	void doWhileTier2SimpleBody(int x) {
		do --x;
		while (x > 0);

		do ++x;
		while (x < 10);

		do x += 5;
		while (x < 100);

		do x -= 1;
		while (x > 0);

		do x *= 2;
		while (x < 100);

		do x /= 2;
		while (x > 1);

		do x %= 3;
		while (x > 0);

		do x &= 0xFF;
		while (x > 0);

		do x |= 1;
		while (x == 0);

		do x ^= 1;
		while (x > 0);

		do x <<= 1;
		while (x < 100);

		do x >>= 1;
		while (x > 0);

		do x >>>= 1;
		while (x > 0);

		do x = 0;
		while (x > 0);

		do next(x);
		while (x > 0);
	}

	void doWhileTier3Braced(int x) {
		do {
			System.out.println(x);
			--x;
		} while (x > 0);
	}

	void doWhileTier3ComplexRhsAssign(int x, int y) {
		do
			x = x + y;
		while (x < 100);
	}

	void doWhileTier3ComplexRhsCompound(int x, int y) {
		do
			x += 5 * y;
		while (x < 100);
	}

	void doWhileTier3NonSimple(int x) {
		final var list = List.of("a");
		do
			list.subList(0, 1).clear();
		while (!list.isEmpty());

		do
			new Object();
		while (x > 0);
	}

	void elseIfChain(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			System.out.println("negative");
		else
			System.out.println("zero");
	}

	void emptyStatement(int x) {
		for (; x > 0; --x)
			;
	}

	void forWithSemicolons() {
		for (int i = 0; i < 10; ++i)
			System.out.println(i);
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

		final var list = List.of("a", "b");
		for (var item : list) {
			System.out.println(item);
			System.out.println(item.length());
		}
	}

	void multiLineExpression(int x) {
		if (x > 0) {
			System.out.println(
					"hello"
			);
		}
	}

	void nestedIndependent(int x) {
		for (int i = 0; i < x; ++i) {
			if (i > 0)
				System.out.println(i);
		}
	}

	private int next(int x) {
		return x - 1;
	}

	void singleLineBodies(int x) {
		if (x > 0)
			System.out.println("positive");

		while (x > 0)
			--x;

		for (int i = 0; i < x; ++i)
			System.out.println(i);

		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
	}
}