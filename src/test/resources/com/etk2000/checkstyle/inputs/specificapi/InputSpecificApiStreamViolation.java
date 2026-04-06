package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiStreamViolation {
	void streamCount(List<String> list) {
		long count = list.stream().count(); // violation: use .size()
	}

	void streamFindFirstIsPresent(List<String> list) {
		if (list.stream().findFirst().isPresent()) // violation: use !.isEmpty()
			System.out.println("not empty");
	}

	void streamForEach(List<String> list) {
		list.stream().forEach(System.out::println); // violation: use .forEach(...)
	}
}