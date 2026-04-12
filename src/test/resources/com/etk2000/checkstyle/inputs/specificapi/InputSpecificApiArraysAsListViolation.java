package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Arrays;

class InputSpecificApiArraysAsListViolation {
	void asListMultipleArgs() {
		final var list = Arrays.asList("a", "b", "c"); // violation: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}

	void asListNoArgs() {
		final var list = Arrays.asList(); // violation: Use 'List.of()' instead of 'Arrays.asList()'.
	}

	void asListSingleArg(String s) {
		final var list = Arrays.asList(s); // violation: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}
}