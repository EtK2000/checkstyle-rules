package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowOneLiner {
	void method(int x) {
		if (x > 0) System.out.println("positive"); // violation: body on same line
		while (x > 0) --x; // violation: body on same line
		for (int i = 0; i < x; ++i) System.out.println(i); // violation: body on same line
		do --x; while (x > 0); // violation: body on same line
	}

	void elseOneLiner(int x) {
		if (x > 0)
			System.out.println("positive");
		else System.out.println("negative"); // violation: body on same line
	}
}