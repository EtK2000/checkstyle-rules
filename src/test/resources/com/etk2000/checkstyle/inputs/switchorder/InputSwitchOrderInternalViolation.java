package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderInternalViolation {
	// traditional fall-through labels out of order internally
	int fallThroughWrong(int x) {
		switch (x) {
			case 1:
			case 3: // violation: fall-through labels out of order
			case 2:
				return 10;

			case 4:
				return 40;

			default:
				return 0;
		}
	}

	// second case also has internal disorder
	String internalAlphaWrong(String s) {
		return switch (s) {
			case "alpha", "beta" -> "ab";
			case "gamma", "delta" -> "gd"; // violation: "delta" must appear before "gamma"
			default -> "?";
		};
	}

	// comma-separated labels out of order internally
	int internalWrong(int x) {
		return switch (x) {
			case 3, 1 -> 10; // violation: 1 must appear before 3
			case 4, 5 -> 20;
			default -> 0;
		};
	}
}