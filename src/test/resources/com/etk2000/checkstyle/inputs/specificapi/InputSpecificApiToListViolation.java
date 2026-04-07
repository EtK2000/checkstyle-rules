package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;
import java.util.stream.Collectors;

class InputSpecificApiToListViolation {
	void collectToList(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList()); // violation: Use '.toList()' instead of '.collect(Collectors.toList())'.
	}

	void collectToUnmodifiableList(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableList()); // violation: Use '.toList()' instead of '.collect(Collectors.toUnmodifiableList())'.
	}
}