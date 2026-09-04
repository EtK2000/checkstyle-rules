package com.etk2000.checkstyle.inputs.prefervar;

import static java.lang.String.valueOf;

class InputStaticImportOwnersDonor {
	void m() {
		final String text = valueOf(1); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(text);
	}
}