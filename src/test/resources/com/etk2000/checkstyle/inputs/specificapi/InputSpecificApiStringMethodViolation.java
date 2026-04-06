package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiStringMethodViolation {
	void equalsEmpty(String s) {
		if (s.equals("")) // violation: use .isEmpty()
			System.out.println("empty");
	}

	void replaceAllLiteral(String s) {
		final var result = s.replaceAll("foo", "bar"); // violation: use .replace(...)
	}
}