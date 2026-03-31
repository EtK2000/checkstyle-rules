package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderClean {
	static final int ALPHA = 1;
	static final int BETA = 2;
	static final char CHAR_CONST = 'x';

	// alpha sorted
	String alphaSorted(String s) {
		return switch (s) {
			case "alpha" -> "a";
			case "beta" -> "b";
			case "gamma" -> "g";
			default -> "?";
		};
	}

	// string digit content before letter content (mixed case)
	String alphaWithDigits(String s) {
		return switch (s) {
			case "0123" -> "num";
			case "999" -> "high";
			case "ABC" -> "upper";
			case "abc" -> "letters";
			default -> "?";
		};
	}

	// binary literals sorted numerically
	int binarySorted(int x) {
		return switch (x) {
			case 0b0001 -> 1;
			case 0b0010 -> 2;
			case 0b1010 -> 10;
			default -> 0;
		};
	}

	// char digit literals before letter literals (including uppercase)
	int charDigitBeforeLetter(char c) {
		return switch (c) {
			case '0' -> 0;
			case '9' -> 9;
			case 'A' -> 1;
			case 'b' -> 2;
			case 'z' -> 26;
			default -> 0;
		};
	}

	// named constant before digit and letter char literals
	int charNamedBeforeDigitBeforeLetter(char c) {
		return switch (c) {
			case CHAR_CONST -> 99;
			case '0' -> 0;
			case 'a' -> 1;
			default -> -1;
		};
	}

	// comma-separated with named constant before numeric
	int commaNamedBeforeNumeric(int x) {
		return switch (x) {
			case ALPHA, 100 -> 10;
			default -> 0;
		};
	}

	// enhanced with comma-separated, internally sorted
	int commaSorted(int x) {
		return switch (x) {
			case 1, 2 -> 10;
			case 3, 4, 5 -> 20;
			default -> 0;
		};
	}

	// comma-separated digit before letter chars
	int commaSortedDigitBeforeLetter(char c) {
		return switch (c) {
			case '0', 'a' -> 1;
			default -> 0;
		};
	}

	// empty switch
	void emptySwitch(int x) {
		switch (x) {
		}
	}

	// qualified enum refs sorted by constant name
	int enumSorted(Thread.State state) {
		return switch (state) {
			case BLOCKED -> 1;
			case NEW -> 2;
			case TERMINATED -> 3;
			default -> 0;
		};
	}

	// fall-through groups sorted by first label
	int fallThroughSorted(int x) {
		switch (x) {
			case 1:
			case 2:
				return 10;
			case 3:
			case 4:
				return 20;
			default:
				return 0;
		}
	}

	// hex literals sorted numerically
	int hexSorted(int x) {
		return switch (x) {
			case 0x0A -> 10;
			case 0x0F -> 15;
			case 0xFF -> 255;
			default -> 0;
		};
	}

	// long suffix sorted numerically
	long longSuffixSorted(long x) {
		return switch (x) {
			case 1L -> 1;
			case 10L -> 10;
			case 100L -> 100;
			default -> 0;
		};
	}

	// named constants before numbers
	int namedBeforeNumeric(int x) {
		switch (x) {
			case ALPHA:
			case BETA:
				return 10;
			case 100:
			case 200:
				return 20;
			default:
				return 0;
		}
	}

	// numeric sorted
	int numericSorted(int x) {
		switch (x) {
			case -2:
				return -2;
			case -1:
				return -1;
			case 0:
				return 0;
			case 1:
				return 1;
			case 10:
				return 10;
			default:
				return -99;
		}
	}

	// octal literals sorted numerically
	int octalSorted(int x) {
		return switch (x) {
			case 010 -> 8;
			case 012 -> 10;
			case 017 -> 15;
			default -> 0;
		};
	}

	// only default
	int onlyDefault(int x) {
		switch (x) {
			default:
				return 0;
		}
	}
}