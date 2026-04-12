package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiToArrayViolation {
	static class Inner {}

	void toArrayInteger(List<Integer> list) {
		final var arr = list.toArray(new Integer[0]); // violation: Use 'Integer[]::new' instead of 'new Integer[0]'.
	}

	void toArrayMethodReceiver() {
		final var arr = getList().toArray(new String[0]); // violation: Use 'String[]::new' instead of 'new String[0]'.
	}

	void toArrayString(List<String> list) {
		final var arr = list.toArray(new String[0]); // violation: Use 'String[]::new' instead of 'new String[0]'.
	}
}