package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallPostDelayedViolation {
	void delayNotOnClosingLine() {
		handler.postDelayed(() -> {
			System.out.println("delayed");
		},
				1000
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void lambdaNotOnOpeningButDelayOnClosing() {
		handler.postDelayed( // violation: Inline block argument: must be on the opening paren line.
				() -> {
					System.out.println("delayed");
				}, 1000);
	}

	void lambdaNotOnOpeningLine() {
		handler.postDelayed( // violation: Inline block argument: must be on the opening paren line.
				() -> {
					System.out.println("delayed");
				},
				1000
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}
}