package com.etk2000.checkstyle.inputs.switchcaseorder;

// === case: charliteral ===
class InputSwitchOrderCharLiteralViolation {
	static final char CHAR_CONST = 'x';

	int charDigitBeforeNamed(char c) {
		return switch (c) {
			case '0' -> 0;
			case CHAR_CONST -> 99;
			default -> -1;
		};
	}

	int charMixedCase(char c) {
		return switch (c) {
			case 'b' -> 2;
			case 'A' -> 1;
			default -> 0;
		};
	}

	int charUnsorted(char c) {
		return switch (c) {
			case 'z' -> 26;
			case 'a' -> 1;
			default -> 0;
		};
	}

	int letterBeforeDigit(char c) {
		return switch (c) {
			case 'a' -> 1;
			case '0' -> 0;
			default -> -1;
		};
	}

	int uppercaseBeforeDigit(char c) {
		return switch (c) {
			case 'A' -> 1;
			case '0' -> 0;
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
			case "alpha" -> "a";
			default -> "?";
		};
	}

	int defaultNotLast(int x) {
		switch (x) {
			default:
				return -1;
			case 1:
				return 1;
		}
	}

	String letterBeforeDigitString(String s) {
		return switch (s) {
			case "abc" -> "letters";
			case "0123" -> "num";
			default -> "?";
		};
	}

	int numericBeforeNamed(int x) {
		switch (x) {
			case 100:
				return 20;
			case ALPHA:
				return 10;
			default:
				return 0;
		}
	}

	int numericWrong(int x) {
		switch (x) {
			case 10:
				return 10;
			case 2:
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
			case 1 -> 1;
			case 2 -> 2;
		};
	}

	int traditional(int x) {
		switch (x) {
			default:
				return -1;
			case 1:
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
			case 1:
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
			case "gamma", "delta" -> "gd";
			default -> "?";
		};
	}

	int internalLetterBeforeDigit(char c) {
		return switch (c) {
			case 'a', '0' -> 1;
			default -> 0;
		};
	}

	int internalNumericBeforeNamed(int x) {
		return switch (x) {
			case 100, ALPHA -> 10;
			default -> 0;
		};
	}

	int internalWrong(int x) {
		return switch (x) {
			case 3, 1 -> 10;
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
			case 0b0001 -> 1;
			default -> 0;
		};
	}

	int hexWrong(int x) {
		return switch (x) {
			case 0xFF -> 255;
			case 0x0A -> 10;
			default -> 0;
		};
	}

	long longWrong(long x) {
		return switch (x) {
			case 100 -> 100;
			case 10 -> 10;
			default -> 0;
		};
	}

	int octalWrong(int x) {
		return switch (x) {
			case 017 -> 15;
			case 010 -> 8;
			default -> 0;
		};
	}

	int underscoreWrong(int x) {
		return switch (x) {
			case 1_000 -> 1000;
			case 999 -> 999;
			default -> 0;
		};
	}
}
// === end ===