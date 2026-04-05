package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiEqualsEmptyViolation {
	void equalsEmpty(String s) {
		if (s.equals("")) // violation: use .isEmpty()
			System.out.println("empty");
	}
}