package com.etk2000.checkstyle.inputs.controlflow;

import java.util.List;

class InputControlFlowUnnecessaryBraces {
	void method(int x) {
		if (x > 0) { // violation: Single-line control flow body has unnecessary braces.
			System.out.println("positive");
		}

		while (x > 0) { // violation: Single-line control flow body has unnecessary braces.
			--x;
		}

		for (int i = 0; i < x; ++i) { // violation: Single-line control flow body has unnecessary braces.
			System.out.println(i);
		}

		final var list = List.of("a", "b");
		for (var item : list) { // violation: Single-line control flow body has unnecessary braces.
			System.out.println(item);
		}

		do { // violation: Single-line control flow body has unnecessary braces.
			--x;
		} while (x > 0);

		if (x > 0) { // violation: Single-line control flow body has unnecessary braces.
			System.out.println("positive");
		}
		else { // violation: Single-line control flow body has unnecessary braces.
			System.out.println("negative");
		}
	}
}