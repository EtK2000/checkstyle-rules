package com.etk2000.checkstyle.inputs.singlecase;

class InputBracedCaseBlankLineViolation {
	int method(int x) {
		switch (x) {
			case 1: {
				var y = x + 1;
				return y;
			}

			case 2: { // violation: no blank line after braced case
				var z = x + 2;
				return z;
			}

			default: // violation: no blank line after braced case
				return 0;
		}
	}
}