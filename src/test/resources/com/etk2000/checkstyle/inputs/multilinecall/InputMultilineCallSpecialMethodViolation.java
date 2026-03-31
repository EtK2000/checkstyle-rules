package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.List;

class InputMultilineCallSpecialMethodViolation {
	void getStringNotOnClosing() {
		method(requireContext().getString(
				1
		)
		); // violation: closing paren not on closing paren line
	}

	void listOfFqnNotOnOpening() {
		method( // violation: java.util.List.of not on opening paren line
				java.util.List.of(
						1, 2, 3
				)
		); // violation: closing paren not on closing paren line
	}

	void listOfNotOnOpening() {
		method( // violation: List.of not on opening paren line
				List.of(
						1, 2, 3
				)
		); // violation: closing paren not on closing paren line
	}

	void method(Object a) {
	}

	Object requireContext() {
		return null;
	}
}