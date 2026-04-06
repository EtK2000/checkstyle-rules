package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarAllowedMethodViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void allowedMethodTypeArgsExplicitType() {
		final String s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation: prefer explicit type over type args
	}

	void allowedMethodTypeArgsVar() {
		final var s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation: prefer explicit type over type args
	}

	void allowedMethodVar() {
		final var s = genericMethod(1); // violation: var with generic return type
	}

	void nonAllowedMethodCalls() {
		final String s = String.valueOf(42); // violation: local must use var
	}
}