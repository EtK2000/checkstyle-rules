package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallAnonClassViolation {
	void anonClassNotOnOpening() {
		method( // violation line 5 — anon class not on opening paren line
				new Runnable() {
					public void run() {
					}
				}
		); // violation line 10 — closing brace not on closing paren line
	}

	void anonClassClosingNotOnClosing() {
		method(new Runnable() {
			public void run() {
			}
		}
		); // violation line 18 — closing brace not on closing paren line
	}

	void method(Runnable r) {
	}
}