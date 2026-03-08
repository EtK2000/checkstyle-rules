package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallPostDelayedViolation {
	void lambdaNotOnOpeningLine() {
		handler.postDelayed( // violation line 5 — lambda not on opening paren line
				() -> {
					System.out.println("delayed");
				},
				1000
		); // violation line 10 — delay not on closing paren line
	}

	void delayNotOnClosingLine() {
		handler.postDelayed(() -> {
			System.out.println("delayed");
		},
				1000
		); // violation line 18 — delay not on closing paren line
	}

	void lambdaNotOnOpeningButDelayOnClosing() {
		handler.postDelayed( // violation line 22 — lambda not on opening paren line
				() -> {
					System.out.println("delayed");
				}, 1000);
	}
}