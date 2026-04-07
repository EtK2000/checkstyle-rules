package com.etk2000.checkstyle.inputs.prefervar;

class InputPreferVarGenericReturnViolation {
	static <T> T cast(Object obj) {
		return (T) obj;
	}

	static String nonGeneric() {
		return "";
	}

	void autoDetectedGenericVar() {
		final var s = cast("hello"); // violation: Using 'var' with 'cast' loses generic type information, consider using an explicit type.
	}

	void nonGenericMethod() {
		final String s = nonGeneric(); // violation: Local variable must use 'var' instead of an explicit type.
	}
}