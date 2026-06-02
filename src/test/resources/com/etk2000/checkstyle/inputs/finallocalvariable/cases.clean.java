package com.etk2000.checkstyle.inputs.finallocalvariable;

class InputFinalLocalVariableClean {
	int alreadyFinal() {
		final var x = 5;
		return x;
	}

	int reassigned() {
		var x = 5;
		x = 6;
		return x;
	}

	void splitDeclAlreadyFinal() {
		final Runnable
				r = () -> {};
		r.run();
	}
}