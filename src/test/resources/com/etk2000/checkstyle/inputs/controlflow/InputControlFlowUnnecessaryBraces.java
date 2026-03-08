package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowUnnecessaryBraces {
	void method(int x) {
		if (x > 0) { // violation line 5
			System.out.println("positive");
		}

		while (x > 0) { // violation line 9
			--x;
		}

		for (int i = 0; i < x; ++i) { // violation line 13
			System.out.println(i);
		}

		do { // violation line 17
			--x;
		} while (x > 0);

		if (x > 0) { // violation line 21
			System.out.println("positive");
		}
		else { // violation line 24
			System.out.println("negative");
		}
	}
}