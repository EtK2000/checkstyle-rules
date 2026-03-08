package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowMissingBraces {
	void method(int x) {
		if (x > 0) // violation line 5
			for (int i = 0; i < x; ++i)
				System.out.println(i);

		while (x > 0) // violation line 9
			if (x > 5)
				--x;

		for (int i = 0; i < x; ++i) // violation line 13
			if (i > 0)
				System.out.println(i);

		do // violation line 17
			if (x > 0)
				--x;
		while (x > 0);
	}

	void elseMethod(int x) {
		if (x > 0)
			System.out.println("positive");
		else // violation line 26
			for (int i = 0; i < x; ++i)
				System.out.println(i);
	}
}