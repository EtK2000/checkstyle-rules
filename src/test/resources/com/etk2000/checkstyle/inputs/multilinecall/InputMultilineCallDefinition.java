package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallDefinition {
	void cleanDef(
			int a,
			int b
	) {
	}

	void openingViolation(int a, // violation: param on opening paren line
			int b
	) {
	}

	void closingViolation(
			int a,
			int b) { // violation: param on closing paren line
	}

	InputMultilineCallDefinition(int a, // violation: param on opening paren line
			int b
	) {
	}

	InputMultilineCallDefinition(
			int a,
			int b) { // violation: param on closing paren line
	}
}