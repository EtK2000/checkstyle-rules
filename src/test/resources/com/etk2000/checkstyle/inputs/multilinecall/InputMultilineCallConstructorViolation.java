package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallConstructorViolation {
	void constructorNotOnOpeningLine() {
		method( // violation line 5 — constructor not on opening paren line
				new java.util.ArrayList<>(
						java.util.Arrays.asList(1, 2, 3)
				)
		); // violation line 9 — closing paren not on closing paren line
	}

	void constructorNotOnClosingLine() {
		method(new java.util.ArrayList<>(
				java.util.Arrays.asList(1, 2, 3)
		)
		); // violation line 16 — closing paren not on closing paren line
	}

	void method(java.util.List<Integer> list) {
	}
}