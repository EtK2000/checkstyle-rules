package com.etk2000.checkstyle.inputs.prefervar;

import java.util.List;

class InputPreferVarAllowedMethodClean {
	static <T> T genericMethod(int id) {
		return null;
	}

	void allowedMethodExplicitType() {
		String s = genericMethod(1);
		List<Integer> list = genericMethod(2);
	}
}