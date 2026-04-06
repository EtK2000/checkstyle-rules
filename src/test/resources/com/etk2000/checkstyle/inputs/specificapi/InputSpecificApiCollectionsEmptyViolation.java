package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class InputSpecificApiCollectionsEmptyViolation {
	void emptyList() {
		final List<String> list = Collections.emptyList(); // violation: use List.of()
	}

	void emptyMap() {
		final Map<String, String> map = Collections.emptyMap(); // violation: use Map.of()
	}

	void emptySet() {
		final Set<String> set = Collections.emptySet(); // violation: use Set.of()
	}

	void singleton() {
		final var set = Collections.singleton("a"); // violation: use Set.of(...)
	}

	void singletonList() {
		final var list = Collections.singletonList("a"); // violation: use List.of(...)
	}

	void singletonMap() {
		final var map = Collections.singletonMap("k", "v"); // violation: use Map.of(...)
	}
}