package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class InputSpecificApiCollectionsSortViolation {
	void sortNoComparator(List<String> list) {
		Collections.sort(list); // violation: use .sort(...)
	}

	void sortWithComparator(List<String> list) {
		Collections.sort(list, Comparator.naturalOrder()); // violation: use .sort(...)
	}
}