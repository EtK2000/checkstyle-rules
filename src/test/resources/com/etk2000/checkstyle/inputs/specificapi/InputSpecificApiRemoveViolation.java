package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiRemoveViolation {
	void removeFirst(List<String> list) {
		list.remove(0); // violation: use removeFirst()
	}

	void removeLast(List<String> list) {
		list.remove(list.size() - 1); // violation: use removeLast()
	}
}