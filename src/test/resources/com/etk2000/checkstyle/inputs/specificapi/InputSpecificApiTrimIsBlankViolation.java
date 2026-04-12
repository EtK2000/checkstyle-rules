package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiTrimIsBlankViolation {
	void oneGreaterThanTrimLength(String s) {
		if (1 > s.trim().length()) // violation: Use '.isBlank()' instead of '1 > .trim().length()'.
			System.out.println("blank");
	}

	void oneLessEqualTrimLength(String s) {
		if (1 <= s.trim().length()) // violation: Use '!.isBlank()' instead of '1 <= .trim().length()'.
			System.out.println("not blank");
	}

	void trimIsEmpty(String s) {
		if (s.trim().isEmpty()) // violation: Use '.isBlank()' instead of '.trim().isEmpty()'.
			System.out.println("blank");
	}

	void trimLengthEqualsZero(String s) {
		if (s.trim().length() == 0) // violation: Use '.isBlank()' instead of '.trim().length() == 0'.
			System.out.println("blank");
	}

	void trimLengthGreaterEqualOne(String s) {
		if (s.trim().length() >= 1) // violation: Use '!.isBlank()' instead of '.trim().length() >= 1'.
			System.out.println("not blank");
	}

	void trimLengthGreaterThanZero(String s) {
		if (s.trim().length() > 0) // violation: Use '!.isBlank()' instead of '.trim().length() > 0'.
			System.out.println("not blank");
	}

	void trimLengthLessEqualZero(String s) {
		if (s.trim().length() <= 0) // violation: Use '.isBlank()' instead of '.trim().length() <= 0'.
			System.out.println("blank");
	}

	void trimLengthLessThanOne(String s) {
		if (s.trim().length() < 1) // violation: Use '.isBlank()' instead of '.trim().length() < 1'.
			System.out.println("blank");
	}

	void trimLengthNotEqualsZero(String s) {
		if (s.trim().length() != 0) // violation: Use '!.isBlank()' instead of '.trim().length() != 0'.
			System.out.println("not blank");
	}

	void zeroEqualsTrimLength(String s) {
		if (0 == s.trim().length()) // violation: Use '.isBlank()' instead of '0 == .trim().length()'.
			System.out.println("blank");
	}

	void zeroGreaterEqualTrimLength(String s) {
		if (0 >= s.trim().length()) // violation: Use '.isBlank()' instead of '0 >= .trim().length()'.
			System.out.println("blank");
	}

	void zeroLessThanTrimLength(String s) {
		if (0 < s.trim().length()) // violation: Use '!.isBlank()' instead of '0 < .trim().length()'.
			System.out.println("not blank");
	}

	void zeroNotEqualsTrimLength(String s) {
		if (0 != s.trim().length()) // violation: Use '!.isBlank()' instead of '0 != .trim().length()'.
			System.out.println("not blank");
	}
}