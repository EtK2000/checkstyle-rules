package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.Optional;

class InputPreferVarReflectionViolation {
	void staticCallGenericVar() {
		final var list = Collections.emptyList(); // violation: var with generic return type
		final var opt = Optional.empty(); // violation: var with generic return type
	}

	void staticCallNonGeneric() {
		final String s = String.valueOf(42); // violation: local must use var
	}

	void typeArgsOnReflectionGeneric() {
		final var list = Collections.<String>emptyList(); // violation: prefer explicit type over type args
	}
}