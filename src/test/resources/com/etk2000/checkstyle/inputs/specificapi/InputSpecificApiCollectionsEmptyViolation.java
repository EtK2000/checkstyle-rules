package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class InputSpecificApiCollectionsEmptyViolation {
	void emptyList() {
		List<String> list = Collections.emptyList(); // violation: use List.of()
	}

	void emptyMap() {
		Map<String, String> map = Collections.emptyMap(); // violation: use Map.of()
	}

	void emptySet() {
		Set<String> set = Collections.emptySet(); // violation: use Set.of()
	}

	void singleton() {
		Set<String> set = Collections.singleton("a"); // violation: use Set.of(...)
	}

	void singletonList() {
		List<String> list = Collections.singletonList("a"); // violation: use List.of(...)
	}

	void singletonMap() {
		Map<String, String> map = Collections.singletonMap("k", "v"); // violation: use Map.of(...)
	}
}