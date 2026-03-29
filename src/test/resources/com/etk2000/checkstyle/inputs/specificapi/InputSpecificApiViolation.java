package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiViolation {
	void getSizeMinusOne(List<String> list) {
		System.out.println(list.get(list.size() - 1)); // violation: use getLast()
	}

	void getZero(List<String> list) {
		System.out.println(list.get(0)); // violation: use getFirst()
	}
}