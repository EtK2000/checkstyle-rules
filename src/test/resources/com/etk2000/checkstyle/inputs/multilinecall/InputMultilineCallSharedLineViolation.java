package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallSharedLineViolation {
	void firstTwoSharing() {
		method(
				1, 2, // violation: args sharing line
				3
		);
	}

	void lastTwoSharing() {
		method(
				1,
				2, 3 // violation: args sharing line
		);
	}

	void defFirstTwoSharing(
			int a, int b, // violation: params sharing line
			int c
	) {
	}

	void defLastTwoSharing(
			int a,
			int b, int c // violation: params sharing line
	) {
	}

	void method(int a, int b, int c) {
	}
}