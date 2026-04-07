package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiStreamViolation {
	void streamCount(List<String> list) {
		final var count = list.stream().count(); // violation: Use '.size()' instead of '.stream().count()'.
	}

	void streamFindFirstIsPresent(List<String> list) {
		if (list.stream().findFirst().isPresent()) // violation: Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.
			System.out.println("not empty");
	}

	void streamForEach(List<String> list) {
		list.stream().forEach(System.out::println); // violation: Use '.forEach(...)' instead of '.stream().forEach(...)'.
	}
}