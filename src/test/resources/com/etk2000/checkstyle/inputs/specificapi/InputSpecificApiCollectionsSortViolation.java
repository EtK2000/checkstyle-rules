package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class InputSpecificApiCollectionsSortViolation {
	void sortNoComparator(List<String> list) {
		Collections.sort(list); // violation: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}

	void sortWithComparator(List<String> list) {
		Collections.sort(list, Comparator.naturalOrder()); // violation: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}