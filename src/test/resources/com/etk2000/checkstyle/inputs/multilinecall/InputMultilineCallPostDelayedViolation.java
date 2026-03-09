package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallPostDelayedViolation {
	void lambdaNotOnOpeningLine() {
		handler.postDelayed( // violation: lambda not on opening paren line
				() -> {
					System.out.println("delayed");
				},
				1000
		); // violation: delay not on closing paren line
	}

	void delayNotOnClosingLine() {
		handler.postDelayed(() -> {
			System.out.println("delayed");
		},
				1000
		); // violation: delay not on closing paren line
	}

	void lambdaNotOnOpeningButDelayOnClosing() {
		handler.postDelayed( // violation: lambda not on opening paren line
				() -> {
					System.out.println("delayed");
				}, 1000);
	}
}