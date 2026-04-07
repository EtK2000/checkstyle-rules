package com.etk2000.checkstyle.inputs.singlecase;

class InputSingleCaseViolation {
	int method(int x) {
		switch (x) {
			case 1:
				return 1;

			case 2: // violation: No blank line between single-line switch cases.
				return 2;
			default:
				return 0;
		}
	}
}