package com.etk2000.checkstyle.inputs.singlecase;

class InputSingleCaseThrowViolation {
	void method(int x) {
		switch (x) {
			case 1:
				throw new RuntimeException("one");

			case 2: // violation line 9
				throw new RuntimeException("two");
			default:
				throw new RuntimeException("default");
		}
	}
}