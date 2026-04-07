package com.etk2000.checkstyle.inputs.singlecase;

class InputBracedCaseBlankLineClean {
	int method(int x) {
		switch (x) {
			case 1: {
				final var y = x + 1;
				return y;
			}
			case 2: {
				final var z = x + 2;
				return z;
			}
			default:
				return 0;
		}
	}

	// mix of braced and unbraced — blank line only after unbraced multi-line case
	int mixed(int x) {
		switch (x) {
			case 1:
				System.out.println("one");
				return 1;

			case 2: {
				final var z = x + 2;
				return z;
			}
			default:
				return 0;
		}
	}
}