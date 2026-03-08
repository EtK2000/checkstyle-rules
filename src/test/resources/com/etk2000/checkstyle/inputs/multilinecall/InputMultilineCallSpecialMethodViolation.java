package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.List;

class InputMultilineCallSpecialMethodViolation {
	void listOfNotOnOpening() {
		method( // violation line 7 — List.of not on opening paren line
				List.of(
						1, 2, 3
				)
		); // violation line 11 — closing paren not on closing paren line
	}

	void getStringNotOnClosing() {
		method(requireContext().getString(
				1
		)
		); // violation line 18 — closing paren not on closing paren line
	}

	Object requireContext() {
		return null;
	}

	void method(Object a) {
	}
}