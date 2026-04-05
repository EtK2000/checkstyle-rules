package com.etk2000.checkstyle.inputs.specificapi;

import java.util.List;

class InputSpecificApiStreamForEachViolation {
	void streamForEach(List<String> list) {
		list.stream().forEach(System.out::println); // violation: use .forEach(...)
	}
}