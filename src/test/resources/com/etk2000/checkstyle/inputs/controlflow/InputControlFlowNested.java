package com.etk2000.checkstyle.inputs.controlflow;

class InputControlFlowNested {
	void outerNeedsBracesInnerDoesNot(int x) {
		for (int i = 0; i < x; ++i) {
			if (i > 0)
				System.out.println(i);
		}
	}

	void outerMissingBraces(int x) {
		for (int i = 0; i < x; ++i) // violation line 12
			if (i > 0)
				System.out.println(i);
	}

	void deepNesting(int x) {
		if (x > 0) { // correct — braced body is multi-line
			for (int i = 0; i < x; ++i) // violation (missing braces — body is multi-line)
				if (i > 0)
					System.out.println(i);
		}
	}
}