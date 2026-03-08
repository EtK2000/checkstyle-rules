package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryPositionViolation {
	void questionOnConditionLine() {
		method(true ? // violation line 5 — ? on condition line
				"a"
				: "b"
		);
	}

	void questionTwoLinesAfterCondition() {
		method(true

				? "a" // violation line 14 — ? not immediately after condition
				: "b"
		);
	}

	void colonOnQuestionLine() {
		method(true
				? "a" : "b" // violation line 21 — : on ? line
		);
	}

	void colonTwoLinesAfterTrue() {
		method(true
				? "a"

				: "b" // violation line 29 — : not immediately after true branch
		);
	}

	void method(Object a) {
	}
}