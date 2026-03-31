package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.List;

class InputMultilineCallConstructorViolation {
	void constructorNotOnClosingLine() {
		// intentional FQN: simple name triggers special-method detection, changing formatting rules
		method(new java.util.ArrayList<>(
						java.util.Arrays.asList(1, 2, 3)
				)
		); // violation: closing paren not on closing paren line
	}

	void constructorNotOnOpeningLine() {
		// intentional FQN: simple name triggers special-method detection, changing formatting rules
		method( // violation: constructor not on opening paren line
				new java.util.ArrayList<>(
						java.util.Arrays.asList(1, 2, 3)
				)
		); // violation: closing paren not on closing paren line
	}

	void method(List<Integer> list) {
	}
}