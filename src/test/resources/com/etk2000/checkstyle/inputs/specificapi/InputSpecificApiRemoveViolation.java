package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiRemoveViolation {
	void removeFirst(List<String> list) {
		list.remove(0); // violation: Use '.removeFirst()' instead of '.remove(0)'.
	}

	void removeLast(List<String> list) {
		list.remove(list.size() - 1); // violation: Use '.removeLast()' instead of '.remove(size() - 1)'.
	}
}