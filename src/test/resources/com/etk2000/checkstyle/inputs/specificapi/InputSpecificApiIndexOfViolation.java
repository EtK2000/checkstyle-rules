package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiIndexOfViolation {
	void indexOfEqualNegOne(String s) {
		if (s.indexOf("baz") == -1) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void indexOfGreaterEqualZero(String s) {
		if (s.indexOf("bar") >= 0) // violation: use .contains(...)
			System.out.println("found");
	}

	void indexOfGreaterNegOne(String s) {
		if (s.indexOf("e") > -1) // violation: use .contains(...)
			System.out.println("found");
	}

	void indexOfLessEqualNegOne(String s) {
		if (s.indexOf("f") <= -1) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void indexOfLessThanZero(String s) {
		if (s.indexOf("qux") < 0) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void indexOfNotEqualNegOne(String s) {
		if (s.indexOf("foo") != -1) // violation: use .contains(...)
			System.out.println("found");
	}

	void negOneEqualIndexOf(String s) {
		if (-1 == s.indexOf("b")) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void negOneGreaterEqualIndexOf(String s) {
		if (-1 >= s.indexOf("h")) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void negOneLessThanIndexOf(String s) {
		if (-1 < s.indexOf("g")) // violation: use .contains(...)
			System.out.println("found");
	}

	void negOneNotEqualIndexOf(String s) {
		if (-1 != s.indexOf("a")) // violation: use .contains(...)
			System.out.println("found");
	}

	void zeroGreaterIndexOf(String s) {
		if (0 > s.indexOf("d")) // violation: use !.contains(...)
			System.out.println("not found");
	}

	void zeroLessEqualIndexOf(String s) {
		if (0 <= s.indexOf("c")) // violation: use .contains(...)
			System.out.println("found");
	}
}