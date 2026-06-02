package com.etk2000.checkstyle.inputs.switchcaseorder;

// === case: charliteral ===
class InputSwitchOrderCharLiteralViolation {
	static final char CHAR_CONST = 'x';

	int charDigitBeforeNamed(char c) {
		return switch (c) {
			case '0' -> 0;
			case CHAR_CONST -> 99; // violation: Case 'CHAR_CONST' must appear before '0'.
			default -> -1;
		};
	}

	int charMixedCase(char c) {
		return switch (c) {
			case 'b' -> 2;
			case 'A' -> 1; // violation: Case 'A' must appear before 'b'.
			default -> 0;
		};
	}

	int charUnsorted(char c) {
		return switch (c) {
			case 'z' -> 26;
			case 'a' -> 1; // violation: Case 'a' must appear before 'z'.
			default -> 0;
		};
	}

	int letterBeforeDigit(char c) {
		return switch (c) {
			case 'a' -> 1;
			case '0' -> 0; // violation: Case '0' must appear before 'a'.
			default -> -1;
		};
	}

	int uppercaseBeforeDigit(char c) {
		return switch (c) {
			case 'A' -> 1;
			case '0' -> 0; // violation: Case '0' must appear before 'A'.
			default -> -1;
		};
	}
}
// === end ===

// === case: default ===
class InputSwitchOrderViolation {
	static final int ALPHA = 1;

	String alphaWrong(String s) {
		return switch (s) {
			case "beta" -> "b";
			case "alpha" -> "a"; // violation: Case 'alpha' must appear before 'beta'.
			default -> "?";
		};
	}

	int defaultNotLast(int x) {
		switch (x) {
			default:
				return -1;
			case 1: // violation: Case 'default' must appear before '1'.
				return 1;
		}
	}

	String letterBeforeDigitString(String s) {
		return switch (s) {
			case "abc" -> "letters";
			case "0123" -> "num"; // violation: Case '0123' must appear before 'abc'.
			default -> "?";
		};
	}

	int numericBeforeNamed(int x) {
		switch (x) {
			case 100:
				return 20;
			case ALPHA: // violation: Case 'ALPHA' must appear before '100'.
				return 10;
			default:
				return 0;
		}
	}

	int numericWrong(int x) {
		switch (x) {
			case 10:
				return 10;
			case 2: // violation: Case '2' must appear before '10'.
				return 2;
			default:
				return 0;
		}
	}
}
// === end ===

// === case: default_first ===
class InputSwitchOrderDefaultFirstViolation {
	int enhanced(int x) {
		return switch (x) {
			default -> -1;
			case 1 -> 1; // violation: Case 'default' must appear before '1'.
			case 2 -> 2;
		};
	}

	int traditional(int x) {
		switch (x) {
			default:
				return -1;
			case 1: // violation: Case 'default' must appear before '1'.
				return 1;
			case 2:
				return 2;
		}
	}
}
// === end ===

// === case: internal ===
class InputSwitchOrderInternalViolation {
	static final int ALPHA = 1;

	int fallThroughWrong(int x) {
		switch (x) {
			case 1: // violation: Label '2' must appear before '3'.
			case 3:
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
// === end ===

// === case: numeric_edge ===
class InputSwitchOrderNumericEdgeViolation {
	int binaryWrong(int x) {
		return switch (x) {
			case 0b1010 -> 10;
			case 0b0001 -> 1; // violation: Case '0b0001' must appear before '0b1010'.
			default -> 0;
		};
	}

	int hexWrong(int x) {
		return switch (x) {
			case 0xFF -> 255;
			case 0x0A -> 10; // violation: Case '0x0A' must appear before '0xFF'.
			default -> 0;
		};
	}

	long longWrong(long x) {
		return switch (x) {
			case 100L -> 100;
			case 10L -> 10; // violation: Case '10L' must appear before '100L'.
			default -> 0;
		};
	}

	int octalWrong(int x) {
		return switch (x) {
			case 017 -> 15;
			case 010 -> 8; // violation: Case '010' must appear before '017'.
			default -> 0;
		};
	}

	int underscoreWrong(int x) {
		return switch (x) {
			case 1_000 -> 1000;
			case 999 -> 999; // violation: Case '999' must appear before '1_000'.
			default -> 0;
		};
	}
}
// === end ===