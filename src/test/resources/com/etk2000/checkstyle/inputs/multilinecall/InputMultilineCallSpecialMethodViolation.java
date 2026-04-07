package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.List;

class InputMultilineCallSpecialMethodViolation {
	void getStringNotOnClosing() {
		method(requireContext().getString(
				1
		)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void listOfFqnNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				java.util.List.of(
						1, 2, 3
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void listOfNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				List.of(
						1, 2, 3
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void method(Object a) {
	}

	Object requireContext() {
		return null;
	}
}