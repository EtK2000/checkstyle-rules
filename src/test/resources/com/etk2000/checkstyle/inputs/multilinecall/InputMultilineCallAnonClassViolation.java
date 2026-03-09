package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallAnonClassViolation {
	void anonClassNotOnOpening() {
		method( // violation: anon class not on opening paren line
				new Runnable() {
					public void run() {
					}
				}
		); // violation: closing brace not on closing paren line
	}

	void anonClassClosingNotOnClosing() {
		method(new Runnable() {
			public void run() {
			}
		}
		); // violation: closing brace not on closing paren line
	}

	void method(Runnable r) {
	}
}