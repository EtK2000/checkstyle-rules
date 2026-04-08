package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowOneLiner {
	void doWhileTier2WhileOnSameLine(int x) {
		final var list = List.of("a");
		do list.add("b"); while (list.size() < 10); // violation: Do-while while clause must be on its own line.
		do System.out.println(x); while (x > 0); // violation: Do-while while clause must be on its own line.
		do --x; while (x > 0 && x < 100); // violation: Do-while while clause must be on its own line.
	}

	void doWhileTier3AsTier2(int x, int y) {
		final var list = List.of("a");
		do list.subList(0, 1).clear(); // violation: Control flow body must be on its own line, not a one-liner.
		while (!list.isEmpty());

		do x += 5 * y; // violation: Control flow body must be on its own line, not a one-liner.
		while (x < 100);
	}

	void doWhileTier3OneLiner(int x, int y) {
		final var list = List.of("a");
		do list.subList(0, 1).clear(); while (x > 0); // violation: Control flow body must be on its own line, not a one-liner.
		do new Object(); while (x > 0); // violation: Control flow body must be on its own line, not a one-liner.
		do x += 5 * y; while (x < 100); // violation: Control flow body must be on its own line, not a one-liner.
		do x = x + y; while (x < 100); // violation: Control flow body must be on its own line, not a one-liner.
	}

	void elseOneLiner(int x) {
		if (x > 0)
			System.out.println("positive");
		else System.out.println("negative"); // violation: Control flow body must be on its own line, not a one-liner.
	}

	void method(int x) {
		if (x > 0) System.out.println("positive"); // violation: Control flow body must be on its own line, not a one-liner.
		while (x > 0) --x; // violation: Control flow body must be on its own line, not a one-liner.
		for (int i = 0; i < x; ++i) System.out.println(i); // violation: Control flow body must be on its own line, not a one-liner.
		final var list = List.of("a");
		for (var item : list) System.out.println(item); // violation: Control flow body must be on its own line, not a one-liner.
	}
}