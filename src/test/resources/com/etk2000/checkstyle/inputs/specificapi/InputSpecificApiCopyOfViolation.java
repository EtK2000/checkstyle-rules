package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class InputSpecificApiCopyOfViolation {
	void unmodifiableList(List<String> list) {
		List<String> result = Collections.unmodifiableList(list); // violation: use List.copyOf(...)
	}

	void unmodifiableMap(Map<String, String> map) {
		Map<String, String> result = Collections.unmodifiableMap(map); // violation: use Map.copyOf(...)
	}

	void unmodifiableSet(Set<String> set) {
		Set<String> result = Collections.unmodifiableSet(set); // violation: use Set.copyOf(...)
	}
}