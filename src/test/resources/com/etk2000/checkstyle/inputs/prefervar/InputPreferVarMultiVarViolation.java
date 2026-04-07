package com.etk2000.checkstyle.inputs.prefervar;

import javax.annotation.Nonnull;

class InputPreferVarMultiVarViolation {
	void multiVarAnnotated() {
		@Nonnull
		final int x = 1, y = 2; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void multiVarForInit() {
		for (int i = 0, j = 10; i < j; ++i) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarForInitAnnotated() {
		for (@Nonnull int i = 0, j = 10; i < j; ++i) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarLocal() {
		final int x = 1, y = 2; // violation: Local variable must use 'var' instead of an explicit type.
		final String a = "a", b = "b"; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void multiVarMixedInit() {
		final int x = Integer.parseInt("5"), y = 2; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void multiVarPartialInit() {
		final int x = 1, y = x; // violation: Local variable must use 'var' instead of an explicit type.
	}
}