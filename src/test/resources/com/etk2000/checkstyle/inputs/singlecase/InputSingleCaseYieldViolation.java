package com.etk2000.checkstyle.inputs.singlecase;

class InputSingleCaseYieldViolation {
	int method(int x) {
		return switch (x) {
			case 1:
				yield 1;

			case 2: // violation: No blank line between single-line switch cases.
				yield 2;
			default:
				yield 0;
		};
	}
}