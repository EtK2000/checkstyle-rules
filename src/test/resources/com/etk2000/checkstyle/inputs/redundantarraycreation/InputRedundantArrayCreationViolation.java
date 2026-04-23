package com.etk2000.checkstyle.inputs.redundantarraycreation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings("unused")
class InputRedundantArrayCreationViolation {
	void arraysAsList() {
		Arrays.asList(new Object[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'asList'.
	}

	void collectionsAddAll() {
		final var list = new ArrayList<String>();
		Collections.addAll(list, new String[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'addAll'.
	}

	void constructorVarargs() {
		new ProcessBuilder(new String[]{"cmd", "arg"}); // violation: Remove redundant array creation for varargs parameter of 'ProcessBuilder'.
	}

	void stringFormat() {
		String.format("%s%s", new Object[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'format'.
	}

	void stringJoin() {
		String.join(",", new CharSequence[]{"a", "b"}); // violation: Remove redundant array creation for varargs parameter of 'join'.
	}
}