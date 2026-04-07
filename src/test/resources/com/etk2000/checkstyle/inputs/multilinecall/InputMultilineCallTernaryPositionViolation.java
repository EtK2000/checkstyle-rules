package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallTernaryPositionViolation {
	void colonOnQuestionLine() {
		method(true
				? "a" : "b" // violation: Ternary ':' must be on the line immediately after the true branch.
		);
	}

	void colonTwoLinesAfterTrue() {
		method(true
				? "a"

				: "b" // violation: Ternary ':' must be on the line immediately after the true branch.
		);
	}

	void method(Object a) {
	}

	void questionOnConditionLine() {
		method(true ? // violation: Ternary '?' must be on the line immediately after the condition.
				"a"
				: "b"
		);
	}

	void questionTwoLinesAfterCondition() {
		method(true

				? "a" // violation: Ternary '?' must be on the line immediately after the condition.
				: "b"
		);
	}
}