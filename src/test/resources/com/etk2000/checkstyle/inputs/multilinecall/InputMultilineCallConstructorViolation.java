package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class InputMultilineCallConstructorViolation {
	void constructorNotOnClosingLine() {
		method(new ArrayList<>(
				Collections.nCopies(3, 1)
		) // violation: In multiline calls/signatures, no arguments on the closing paren line.
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void constructorNotOnOpeningLine() {
		method( // violation: Inline block argument: must be on the opening paren line.
				new ArrayList<>(
						Collections.nCopies(3, 1)
				)
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void method(List<Integer> list) {
	}
}