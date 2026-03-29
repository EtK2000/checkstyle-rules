package com.etk2000.checkstyle.inputs.multilinecall;

import org.json.JSONObject;

class InputMultilineCallChainedConstructorViolation {
	void chainedConstructorClosingOnChainLine() {
		method(new JSONObject()
				.put("key", "value")
				.put("key2", "value2")); // violation: closing paren on chain end line
	}

	void chainedConstructorNotOnOpeningLine() {
		method( // violation: chained constructor not on opening paren line
				new JSONObject()
						.put("key", "value")
						.put("key2", "value2")
		);
	}

	void method(Object a) {
	}
}