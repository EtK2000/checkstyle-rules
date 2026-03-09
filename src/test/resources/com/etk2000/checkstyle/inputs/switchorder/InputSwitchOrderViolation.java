package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderViolation {
	// alpha out of order
	String alphaWrong(String s) {
		return switch (s) {
			case "beta" -> "b";
			case "alpha" -> "a"; // violation: "alpha" must appear before "beta"
			default -> "?";
		};
	}

	// default not last
	int defaultNotLast(int x) {
		switch (x) {
			default:
				return -1;
			case 1: // violation: default must be last
				return 1;
		}
	}

	// numeric out of order
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