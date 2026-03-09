package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowMissingBraces {
	void method(int x) {
		if (x > 0) // violation: missing braces on multi-line body
			for (int i = 0; i < x; ++i)
				System.out.println(i);

		while (x > 0) // violation: missing braces on multi-line body
			if (x > 5)
				--x;

		for (int i = 0; i < x; ++i) // violation: missing braces on multi-line body
			if (i > 0)
				System.out.println(i);

		do // violation: missing braces on multi-line body
			if (x > 0)
				--x;
		while (x > 0);
	}

	void elseMethod(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation: missing braces on multi-line body
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}