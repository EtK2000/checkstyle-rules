package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;
import java.util.stream.Collectors;

class InputSpecificApiToListViolation {
	void collectToList(List<String> list) {
		List<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toList()); // violation: use .toList()
	}
}