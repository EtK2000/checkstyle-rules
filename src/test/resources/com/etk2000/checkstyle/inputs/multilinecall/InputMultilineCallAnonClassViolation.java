package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallAnonClassViolation {
	void anonClassClosingNotOnClosing() {
		method(new Runnable() {
			public void run() {
			}
		}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void anonClassNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				new Runnable() {
					public void run() {
					}
				}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void method(Runnable r) {
	}
}