package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiIsEmptyViolation {
	void charSequenceLengthEqualsZero(CharSequence cs) {
		if (cs.length() == 0) // violation: Use '.isEmpty()' instead of '.length() == 0'.
			System.out.println("empty");
	}

	void lengthEqualsZero(String s) {
		if (s.length() == 0) // violation: Use '.isEmpty()' instead of '.length() == 0'.
			System.out.println("empty");
	}

	void lengthGreaterThanZero(String s) {
		if (s.length() > 0) // violation: Use '.!isEmpty()' instead of '.length() > 0'.
			System.out.println("not empty");
	}

	void oneGreaterThanSize(List<String> list) {
		if (1 > list.size()) // violation: use isEmpty()
			System.out.println("empty");
	}

	void oneLessThanOrEqualSize(List<String> list) {
		if (1 <= list.size()) // violation: use !isEmpty()
			System.out.println("not empty");
	}

	void sizeEqualsZero(List<String> list) {
		if (list.size() == 0) // violation: use isEmpty()
			System.out.println("empty");
	}

	void sizeGreaterThanOrEqualOne(List<String> list) {
		if (list.size() >= 1) // violation: use !isEmpty()
			System.out.println("not empty");
	}

	void sizeGreaterThanZero(List<String> list) {
		if (list.size() > 0) // violation: use !isEmpty()
			System.out.println("not empty");
	}

	void sizeLessThanOne(List<String> list) {
		if (list.size() < 1) // violation: use isEmpty()
			System.out.println("empty");
	}

	void sizeLessThanOrEqualZero(List<String> list) {
		if (list.size() <= 0) // violation: use isEmpty()
			System.out.println("empty");
	}

	void sizeNotEqualsZero(List<String> list) {
		if (list.size() != 0) // violation: use !isEmpty()
			System.out.println("not empty");
	}

	void zeroEqualsSize(List<String> list) {
		if (0 == list.size()) // violation: use isEmpty()
			System.out.println("empty");
	}

	void zeroGreaterThanOrEqualSize(List<String> list) {
		if (0 >= list.size()) // violation: use isEmpty()
			System.out.println("empty");
	}

	void zeroLessThanSize(List<String> list) {
		if (0 < list.size()) // violation: use !isEmpty()
			System.out.println("not empty");
	}

	void zeroNotEqualsSize(List<String> list) {
		if (0 != list.size()) // violation: Use '.!isEmpty()' instead of '0 != .size()'.
			System.out.println("not empty");
	}
}