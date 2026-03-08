package com.etk2000.checkstyle.inputs.multilinecall;

// getString calls that should NOT be treated as inline block args
class InputMultilineCallGetStringNotContext {
	void bareGetString() {
		method(
				getString(
						1
				)
		);
	}

	void unknownReceiverGetString() {
		final var bundle = getBundle();
		method(
				bundle.getString(
						"key"
				)
		);
	}

	void nonContextParameterGetString(String notContext) {
		method(
				notContext.getString(
						1
				)
		);
	}

	void nonContextAssignmentGetString() {
		final var res = getResources();
		method(
				res.getString(
						1
				)
		);
	}

	void dottedNonContextMethodGetString() {
		method(
				something.notAContextMethod().getString(
						1
				)
		);
	}

	Object getBundle() {
		return null;
	}

	String getString(int id) {
		return null;
	}

	Object getResources() {
		return null;
	}

	void method(Object a) {
	}
}