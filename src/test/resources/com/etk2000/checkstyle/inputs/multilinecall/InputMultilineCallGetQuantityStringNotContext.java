package com.etk2000.checkstyle.inputs.multilinecall;

// getQuantityString calls that should NOT be treated as inline block args
class InputMultilineCallGetQuantityStringNotContext {
	void bareGetResourcesReceiver() {
		method(
				getResources().getQuantityString(
						1,
						2
				)
		);
	}

	void unknownReceiverGetResources() {
		final var bundle = getBundle();
		method(
				bundle.getResources().getQuantityString(
						1,
						2
				)
		);
	}

	void nonContextParameterGetResources(String notContext) {
		method(
				notContext.getResources().getQuantityString(
						1,
						2
				)
		);
	}

	void noGetResourcesInChain(Context ctx) {
		method(
				ctx.getQuantityString(
						1,
						2
				)
		);
	}

	Object getBundle() {
		return null;
	}

	Object getResources() {
		return null;
	}

	void method(Object a) {
	}
}