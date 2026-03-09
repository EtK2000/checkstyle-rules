package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryPositionViolation {
	void questionOnConditionLine() {
		method(true ? // violation: ? on condition line
				"a"
				: "b"
		);
	}

	void questionTwoLinesAfterCondition() {
		method(true

				? "a" // violation: ? not immediately after condition
				: "b"
		);
	}

	void colonOnQuestionLine() {
		method(true
				? "a" : "b" // violation: : on ? line
		);
	}

	void colonTwoLinesAfterTrue() {
		method(true
				? "a"

				: "b" // violation: : not immediately after true branch
		);
	}

	void method(Object a) {
	}
}