package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowUnnecessaryBraces {
	void method(int x) {
		if (x > 0) { // violation: unnecessary braces
			System.out.println("positive");
		}

		while (x > 0) { // violation: unnecessary braces
			--x;
		}

		for (int i = 0; i < x; ++i) { // violation: unnecessary braces
			System.out.println(i);
		}

		do { // violation: unnecessary braces
			--x;
		} while (x > 0);

		if (x > 0) { // violation: unnecessary braces
			System.out.println("positive");
		}
		else { // violation: unnecessary braces
			System.out.println("negative");
		}
	}
}