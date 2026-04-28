package com.etk2000.checkstyle.inputs.specificapi;

class InputSpecificApiIndexOfCharViolation {
	void cleanCharArg(String s) {
		final var i = s.indexOf('x');
		System.out.println(i);
	}

	void cleanEmptyString(String s) {
		// empty string has no char-literal equivalent
		final var i = s.indexOf("");
		System.out.println(i);
	}

	void cleanMultiCharString(String s) {
		final var i = s.indexOf("xy");
		System.out.println(i);
	}

	void cleanVariableArg(String s, String needle) {
		final var i = s.indexOf(needle);
		System.out.println(i);
	}

	void doubleQuoteEscape(String s) {
		final var i = s.indexOf("\""); // violation: Use 'indexOf('"')' instead of 'indexOf("\"")'.
		System.out.println(i);
	}

	void escapeBackslash(String s) {
		final var i = s.indexOf("\\"); // violation: Use 'indexOf('\')' instead of 'indexOf("\")'.
		System.out.println(i);
	}

	void escapeNewline(String s) {
		final var i = s.indexOf("\n"); // violation: Use 'indexOf('\n')' instead of 'indexOf("\n")'.
		System.out.println(i);
	}

	void indexOfFromIndex(String s) {
		final var i = s.indexOf("x", 5); // violation: Use 'indexOf('x')' instead of 'indexOf("x")'.
		System.out.println(i);
	}

	void indexOfSingleChar(String s) {
		final var i = s.indexOf("x"); // violation: Use 'indexOf('x')' instead of 'indexOf("x")'.
		System.out.println(i);
	}

	void lastIndexOfSingleChar(String s) {
		final var i = s.lastIndexOf("/"); // violation: Use 'lastIndexOf('/')' instead of 'lastIndexOf("/")'.
		System.out.println(i);
	}

	void singleQuote(String s) {
		final var i = s.indexOf("'"); // violation: Use 'indexOf('\'')' instead of 'indexOf("'")'.
		System.out.println(i);
	}
}