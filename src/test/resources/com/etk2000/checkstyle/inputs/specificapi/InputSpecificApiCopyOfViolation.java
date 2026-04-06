package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class InputSpecificApiCopyOfViolation {
	void unmodifiableAsList() {
		final var list = Collections.unmodifiableList(Arrays.asList("a", "b")); // violation: use List.of(...)
	}

	void unmodifiableList(List<String> list) {
		final var result = Collections.unmodifiableList(list); // violation: use List.copyOf(...)
	}

	void unmodifiableMap(Map<String, String> map) {
		final var result = Collections.unmodifiableMap(map); // violation: use Map.copyOf(...)
	}

	void unmodifiableSet(Set<String> set) {
		final var result = Collections.unmodifiableSet(set); // violation: use Set.copyOf(...)
	}
}