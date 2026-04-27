package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiStripIsBlankViolation {
	void oneGreaterThanStripLength(String s) {
		if (1 > s.strip().length()) // violation: Use '.isBlank()' instead of '1 > .strip().length()'.
			System.out.println("blank");
	}

	void oneLessEqualStripLength(String s) {
		if (1 <= s.strip().length()) // violation: Use '!.isBlank()' instead of '1 <= .strip().length()'.
			System.out.println("not blank");
	}

	void stripIsEmpty(String s) {
		if (s.strip().isEmpty()) // violation: Use '.isBlank()' instead of '.strip().isEmpty()'.
			System.out.println("blank");
	}

	void stripLengthEqualsZero(String s) {
		if (s.strip().length() == 0) // violation: Use '.isBlank()' instead of '.strip().length() == 0'.
			System.out.println("blank");
	}

	void stripLengthGreaterEqualOne(String s) {
		if (s.strip().length() >= 1) // violation: Use '!.isBlank()' instead of '.strip().length() >= 1'.
			System.out.println("not blank");
	}

	void stripLengthGreaterThanZero(String s) {
		if (s.strip().length() > 0) // violation: Use '!.isBlank()' instead of '.strip().length() > 0'.
			System.out.println("not blank");
	}

	void stripLengthLessEqualZero(String s) {
		if (s.strip().length() <= 0) // violation: Use '.isBlank()' instead of '.strip().length() <= 0'.
			System.out.println("blank");
	}

	void stripLengthLessThanOne(String s) {
		if (s.strip().length() < 1) // violation: Use '.isBlank()' instead of '.strip().length() < 1'.
			System.out.println("blank");
	}

	void stripLengthNotEqualsZero(String s) {
		if (s.strip().length() != 0) // violation: Use '!.isBlank()' instead of '.strip().length() != 0'.
			System.out.println("not blank");
	}

	void zeroEqualsStripLength(String s) {
		if (0 == s.strip().length()) // violation: Use '.isBlank()' instead of '0 == .strip().length()'.
			System.out.println("blank");
	}

	void zeroGreaterEqualStripLength(String s) {
		if (0 >= s.strip().length()) // violation: Use '.isBlank()' instead of '0 >= .strip().length()'.
			System.out.println("blank");
	}

	void zeroLessThanStripLength(String s) {
		if (0 < s.strip().length()) // violation: Use '!.isBlank()' instead of '0 < .strip().length()'.
			System.out.println("not blank");
	}

	void zeroNotEqualsStripLength(String s) {
		if (0 != s.strip().length()) // violation: Use '!.isBlank()' instead of '0 != .strip().length()'.
			System.out.println("not blank");
	}
}