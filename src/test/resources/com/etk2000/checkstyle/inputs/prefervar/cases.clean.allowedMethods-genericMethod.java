package com.etk2000.checkstyle.inputs.prefervar;

import java.util.List;

class InputPreferVarAllowedMethodClean {
	static <T> T genericMethod(int id) {
		return null;
	}

	void allowedMethodExplicitType() {
		final String s = genericMethod(1);
		final List<Integer> list = genericMethod(2);
	}
}