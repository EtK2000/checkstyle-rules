package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class InputMultilineCallConstructorViolation {
	void constructorNotOnClosingLine() {
		method(new ArrayList<>(
				Collections.nCopies(3, 1)
		)
		); // violation: closing paren not on closing paren line
	}

	void constructorNotOnOpeningLine() {
		method( // violation: constructor not on opening paren line
				new ArrayList<>(
						Collections.nCopies(3, 1)
				)
		); // violation: closing paren not on closing paren line
	}

	void method(List<Integer> list) {
	}
}