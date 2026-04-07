package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.Optional;

class InputPreferVarReflectionViolation {
	void staticCallGenericVar() {
		final var list = Collections.emptyList(); // violation: Using 'var' with 'emptyList' loses generic type information, consider using an explicit type.
		final var opt = Optional.empty(); // violation: Using 'var' with 'empty' loses generic type information, consider using an explicit type.
	}

	void staticCallNonGeneric() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void typeArgsOnReflectionGeneric() {
		final var list = Collections.<String>emptyList(); // violation: Prefer explicit type over type arguments on 'emptyList'.
	}
}