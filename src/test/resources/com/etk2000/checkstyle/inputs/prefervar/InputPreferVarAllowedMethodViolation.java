package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarAllowedMethodViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void allowedMethodTypeArgsExplicitType() {
		final String s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation (warning): Prefer explicit type over type arguments on 'genericMethod'.
	}

	void allowedMethodTypeArgsVar() {
		final var s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation (warning): Prefer explicit type over type arguments on 'genericMethod'.
	}

	void allowedMethodVar() {
		final var s = genericMethod(1); // violation (warning): Using 'var' with 'genericMethod' loses generic type information, consider using an explicit type.
	}

	void nonAllowedMethodCalls() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
	}
}