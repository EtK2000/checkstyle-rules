package com.etk2000.checkstyle.inputs.singlecase;

class InputSingleCaseThrowViolation {
	void method(int x) {
		switch (x) {
			case 1:
				throw new RuntimeException("one");

			case 2: // violation: No blank line between single-line switch cases.
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("default");
		}
	}
}