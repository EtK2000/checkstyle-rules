package com.etk2000.checkstyle.inputs.prefervar;

import static java.util.Arrays.asList;

import java.util.List;

class InputStaticImportOwnersDonor {
	void m() {
		final List<String> names = asList("a", "b"); // violation: Local variable must use 'var' instead of an explicit type.
		System.out.println(names);
	}
}