package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallConstructorViolation {
	void constructorNotOnOpeningLine() {
		method( // violation: constructor not on opening paren line
				new java.util.ArrayList<>(
						java.util.Arrays.asList(1, 2, 3)
				)
		); // violation: closing paren not on closing paren line
	}

	void constructorNotOnClosingLine() {
		method(new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		)
		); // violation: closing paren not on closing paren line
	}

	void method(java.util.List<Integer> list) {
	}
}