package com.etk2000.checkstyle.inputs.multilinecall;

import org.json.JSONObject;

class InputMultilineCallChainedConstructorViolation {
	void chainedConstructorClosingOnChainLine() {
		method(new JSONObject()
				.put("key", "value")
				.put("key2", "value2")); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void chainedConstructorNotOnOpeningLine() {
		method( // violation: Inline block argument: must be on the opening paren line.
				new JSONObject()
						.put("key", "value")
						.put("key2", "value2")
		);
	}

	void method(Object a) {
	}
}