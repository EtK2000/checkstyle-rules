package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderInternalViolation {
	static final int ALPHA = 1;

	int fallThroughWrong(int x) {
		switch (x) {
			case 1:
			case 3: // violation: Label '2' must appear before '3'.
			case 2:
				return 10;

			case 4:
				return 40;

			default:
				return 0;
		}
	}

	String internalAlphaWrong(String s) {
		return switch (s) {
			case "alpha", "beta" -> "ab";
			case "gamma", "delta" -> "gd"; // violation: Label 'delta' must appear before 'gamma'.
			default -> "?";
		};
	}

	int internalLetterBeforeDigit(char c) {
		return switch (c) {
			case 'a', '0' -> 1; // violation: Label '0' must appear before 'a'.
			default -> 0;
		};
	}

	int internalNumericBeforeNamed(int x) {
		return switch (x) {
			case 100, ALPHA -> 10; // violation: Label 'ALPHA' must appear before '100'.
			default -> 0;
		};
	}

	int internalWrong(int x) {
		return switch (x) {
			case 3, 1 -> 10; // violation: Label '1' must appear before '3'.
			case 4, 5 -> 20;
			default -> 0;
		};
	}
}