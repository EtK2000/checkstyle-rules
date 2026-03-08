package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallDefinition {
	void cleanDef(
			int a,
			int b
	) {
	}

	void openingViolation(int a, // violation line 10 — param on opening paren line
			int b
	) {
	}

	void closingViolation(
			int a,
			int b) { // violation line 17 — param on closing paren line
	}

	InputMultilineCallDefinition(int a, // violation line 20 — param on opening paren line
			int b
	) {
	}

	InputMultilineCallDefinition(
			int a,
			int b) { // violation line 27 — param on closing paren line
	}
}