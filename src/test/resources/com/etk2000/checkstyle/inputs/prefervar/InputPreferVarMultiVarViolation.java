package com.etk2000.checkstyle.inputs.prefervar;

import javax.annotation.Nonnull;

class InputPreferVarMultiVarViolation {
	void multiVarAnnotated() {
		@Nonnull // violation (warning): Local variable should use 'var' instead of an explicit type.
		final int x = 1, y = 2;
	}

	void multiVarForInit() {
		for (int i = 0, j = 10; i < j; ++i) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarForInitAnnotated() {
		for (@Nonnull int i = 0, j = 10; i < j; ++i) // violation (warning): Local variable should use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void multiVarFourVariables() {
		final int w = 1, x = 2, y = 3, z = 4; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarLocal() {
		final int x = 1, y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
		final String a = "a", b = "b"; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarMixedInit() {
		final int x = Integer.parseInt("5"), y = 2; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarPartialInit() {
		final int x = 1, y = x; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}

	void multiVarThreeVariables() {
		final int x = 1, y = 2, z = 3; // violation (warning): Local variable should use 'var' instead of an explicit type.
	}
}