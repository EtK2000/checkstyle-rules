package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderNumericEdgeCases {
	// binary out of order
	int binaryWrong(int x) {
		return switch (x) {
			case 0b1010 -> 10;
			case 0b0001 -> 1; // violation: 0b0001 must appear before 0b1010
			default -> 0;
		};
	}

	// char out of order
	int charWrong(char c) {
		return switch (c) {
			case 'z' -> 26;
			case 'a' -> 1; // violation: 'a' must appear before 'z'
			default -> 0;
		};
	}

	// hex out of order
	int hexWrong(int x) {
		return switch (x) {
			case 0xFF -> 255;
			case 0x0A -> 10; // violation: 0x0A must appear before 0xFF
			default -> 0;
		};
	}

	// long suffix out of order
	long longWrong(long x) {
		return switch (x) {
			case 100L -> 100;
			case 10L -> 10; // violation: 10L must appear before 100L
			default -> 0;
		};
	}

	// octal out of order
	int octalWrong(int x) {
		return switch (x) {
			case 017 -> 15;
			case 010 -> 8; // violation: 010 must appear before 017
			default -> 0;
		};
	}
}