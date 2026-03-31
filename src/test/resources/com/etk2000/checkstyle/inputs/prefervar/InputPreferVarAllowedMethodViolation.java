package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarAllowedMethodViolation {
	static <T> T genericMethod(int id) {
		return null;
	}

	void allowedMethodTypeArgsExplicitType() {
		String s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation: prefer explicit type over type args
	}

	void allowedMethodTypeArgsVar() {
		var s = InputPreferVarAllowedMethodViolation.<String>genericMethod(1); // violation: prefer explicit type over type args
	}

	void allowedMethodVar() {
		var s = genericMethod(1); // violation: var with generic return type
	}

	void nonAllowedMethodCalls() {
		String s = String.valueOf(42); // violation: local must use var
	}
}