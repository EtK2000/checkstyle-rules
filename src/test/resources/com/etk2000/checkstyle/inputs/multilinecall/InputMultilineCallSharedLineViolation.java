package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallSharedLineViolation {
	void firstTwoSharing() {
		method(
				1, 2, // violation line 6 — args sharing line
				3
		);
	}

	void lastTwoSharing() {
		method(
				1,
				2, 3 // violation line 14 — args sharing line
		);
	}

	void defFirstTwoSharing(
			int a, int b, // violation line 19 — params sharing line
			int c
	) {
	}

	void defLastTwoSharing(
			int a,
			int b, int c // violation line 26 — params sharing line
	) {
	}

	void method(int a, int b, int c) {
	}
}