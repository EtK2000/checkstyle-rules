package com.etk2000.checkstyle.inputs.multilinecall;

import org.json.JSONObject;

class InputMultilineCallLinesLeakB {
	Object payload;

	void fillerOne() {
	}

	void fillerThree() {
	}

	void fillerTwo() {
	}

	void m() {
		payload = new JSONObject()
				.put("k", 1);
	}
}