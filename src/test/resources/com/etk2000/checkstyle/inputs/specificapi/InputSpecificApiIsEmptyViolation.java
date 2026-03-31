package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiIsEmptyViolation {
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
}