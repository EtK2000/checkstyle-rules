package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarGenericReturnViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	static String nonGeneric() {
		return "";
	}

	void autoDetectedGenericVar() {
		final var s = cast("hello"); // violation: var with generic return type
	}

	void nonGenericMethod() {
		final String s = nonGeneric(); // violation: local must use var
	}
}