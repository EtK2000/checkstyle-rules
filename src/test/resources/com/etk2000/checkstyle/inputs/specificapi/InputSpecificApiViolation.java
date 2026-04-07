package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiViolation {
	void getSizeMinusOne(List<String> list) {
		System.out.println(list.get(list.size() - 1)); // violation: Use '.getLast()' instead of '.get(size() - 1)'.
	}

	void getZero(List<String> list) {
		System.out.println(list.get(0)); // violation: Use '.getFirst()' instead of '.get(0)'.
	}
}