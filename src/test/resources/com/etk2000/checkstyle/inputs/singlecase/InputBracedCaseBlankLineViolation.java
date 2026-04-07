package com.etk2000.checkstyle.inputs.singlecase;

class InputBracedCaseBlankLineViolation {
	int method(int x) {
		switch (x) {
			case 1: {
				final var y = x + 1;
				return y;
			}

			case 2: { // violation: No blank line after braced case, the closing brace provides separation.
				final var z = x + 2;
				return z;
			}

			default: // violation: No blank line after braced case, the closing brace provides separation.
				return 0;
		}
	}
}