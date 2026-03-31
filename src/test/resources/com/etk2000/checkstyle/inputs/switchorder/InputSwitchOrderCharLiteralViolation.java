package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderCharLiteralViolation {
	static final char CHAR_CONST = 'x';

	// digit char before named constant (wrong, named constants must come first)
	int charDigitBeforeNamed(char c) {
		return switch (c) {
			case '0' -> 0;
			case CHAR_CONST -> 99; // violation: CHAR_CONST must appear before '0'
			default -> -1;
		};
	}

	int charMixedCase(char c) {
		return switch (c) {
			case 'b' -> 2; // violation: 'A' must come before 'b'
			case 'A' -> 1;
			default -> 0;
		};
	}

	int charUnsorted(char c) {
		return switch (c) {
			case 'z' -> 26; // violation: 'a' must come before 'z'
			case 'a' -> 1;
			default -> 0;
		};
	}

	int letterBeforeDigit(char c) {
		return switch (c) {
			case 'a' -> 1;
			case '0' -> 0; // violation: '0' must come before 'a'
			default -> -1;
		};
	}

	int uppercaseBeforeDigit(char c) {
		return switch (c) {
			case 'A' -> 1;
			case '0' -> 0; // violation: '0' must come before 'A'
			default -> -1;
		};
	}
}