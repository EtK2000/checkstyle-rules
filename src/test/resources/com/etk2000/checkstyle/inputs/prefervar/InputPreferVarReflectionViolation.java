package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.Optional;

class InputPreferVarReflectionViolation {
	void staticCallGenericVar() {
		var list = Collections.emptyList(); // violation: var with generic return type
		var opt = Optional.empty(); // violation: var with generic return type
	}

	void staticCallNonGeneric() {
		String s = String.valueOf(42); // violation: local must use var
	}

	void typeArgsOnReflectionGeneric() {
		var list = Collections.<String>emptyList(); // violation: prefer explicit type over type args
	}
}