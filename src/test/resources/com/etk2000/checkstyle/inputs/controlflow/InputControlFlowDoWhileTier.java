package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowDoWhileTier {
	private int next(int x) {
		return x - 1;
	}

	void tier2BodyOnOwnLine(int x) {
		do // violation: Do-while body must be on the do line.
			--x;
		while (x > 0);

		do // violation: Do-while body must be on the do line.
			next(x);
		while (x > 0);

		do // violation: Do-while body must be on the do line.
			x += 5;
		while (x < 100);

		do // violation: Do-while body must be on the do line.
			System.out.println(x);
		while (x > 0);

		final var list = List.of("a");
		do // violation: Do-while body must be on the do line.
			list.add("b");
		while (list.size() < 10);
	}
}