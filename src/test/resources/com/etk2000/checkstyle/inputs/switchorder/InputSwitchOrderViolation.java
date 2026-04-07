package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderViolation {
	static final int ALPHA = 1;

	String alphaWrong(String s) {
		return switch (s) {
			case "beta" -> "b";
			case "alpha" -> "a"; // violation: "alpha" must appear before "beta"
			default -> "?";
		};
	}

	int defaultNotLast(int x) {
		switch (x) {
			default:
				return -1;
			case 1: // violation: default must be last
				return 1;
		}
	}

	String letterBeforeDigitString(String s) {
		return switch (s) {
			case "abc" -> "letters";
			case "0123" -> "num"; // violation: "0123" must appear before "abc"
			default -> "?";
		};
	}

	// numeric before named constant (wrong, named constants must come first)
	int numericBeforeNamed(int x) {
		switch (x) {
			case 100:
				return 20;
			case ALPHA: // violation: ALPHA must appear before 100
				return 10;
			default:
				return 0;
		}
	}

	int numericWrong(int x) {
		switch (x) {
			case 10:
				return 10;
			case 2: // violation: case 2 must appear before case 10
				return 2;
			default:
				return 0;
		}
	}
}