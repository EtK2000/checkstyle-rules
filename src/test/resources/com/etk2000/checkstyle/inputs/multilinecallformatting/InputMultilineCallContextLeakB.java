package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallContextLeakB {
	void b(String ctx) {
		method(
				ctx.getString(
						1
				)
		);
	}

	void method(Object a) {
	}
}