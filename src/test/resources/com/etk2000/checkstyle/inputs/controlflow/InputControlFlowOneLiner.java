package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowOneLiner {
	void method(int x) {
		if (x > 0) System.out.println("positive"); // violation line 5
		while (x > 0) --x; // violation line 6
		for (int i = 0; i < x; ++i) System.out.println(i); // violation line 7
		do --x; while (x > 0); // violation line 8
	}

	void elseOneLiner(int x) {
		if (x > 0)
			System.out.println("positive");
		else System.out.println("negative"); // violation line 14
	}
}