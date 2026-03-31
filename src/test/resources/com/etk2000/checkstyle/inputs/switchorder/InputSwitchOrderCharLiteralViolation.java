package com.etk2000.checkstyle.inputs.switchorder;

class InputSwitchOrderCharLiteralViolation {
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
}