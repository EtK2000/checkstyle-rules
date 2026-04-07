package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiStringMethodViolation {
	void equalsEmpty(String s) {
		if (s.equals("")) // violation: Use '.isEmpty()' instead of '.equals(\"\")'.
			System.out.println("empty");
	}

	void replaceAllLiteral(String s) {
		final var result = s.replaceAll("foo", "bar"); // violation: Use '.replace(...)' instead of '.replaceAll(...)'.
	}
}