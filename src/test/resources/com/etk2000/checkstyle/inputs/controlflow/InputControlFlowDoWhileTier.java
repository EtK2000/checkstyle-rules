package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowDoWhileTier {
	private int next(int x) {
		return x - 1;
	}

	void tier1BodyOnOwnLine(int x) {
		do // violation: Simple do-while must be on a single line.
			--x;
		while (x > 0);
	}

	void tier1WhileOnNextLine(int x) {
		do --x; // violation: Simple do-while must be on a single line.
		while (x > 0);

		do next(x); // violation: Simple do-while must be on a single line.
		while (x > 0);

		do x += 5; // violation: Simple do-while must be on a single line.
		while (x < 100);
	}

	void tier2BodyOnOwnLine(int x) {
		do // violation: Do-while body must be on the do line.
			System.out.println(x);
		while (x > 0);

		final var list = List.of("a");
		do // violation: Do-while body must be on the do line.
			list.add("b");
		while (list.size() < 10);
	}
}