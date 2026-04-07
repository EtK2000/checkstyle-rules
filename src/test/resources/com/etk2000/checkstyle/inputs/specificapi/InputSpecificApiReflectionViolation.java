package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class InputSpecificApiReflectionViolation {
	void chainedCallGetZero() {
		System.out.println(getList().get(0)); // violation: Use '.getFirst()' instead of '.get(0)'.
	}

	void chainedCallResolvedGetZero(List<String> list) {
		System.out.println(Collections.synchronizedList(list).get(0)); // violation: Use '.getFirst()' instead of '.get(0)'.
	}

	List<String> getList() {
		return List.of();
	}

	void listLocalGetZero() {
		final var list = List.of("a");
		System.out.println(list.get(0)); // violation: Use '.getFirst()' instead of '.get(0)'.
	}

	void listParamGetZero(List<String> list) {
		System.out.println(list.get(0)); // violation: Use '.getFirst()' instead of '.get(0)'.
	}

	void varLocalGetSizeMinusOne() {
		final var map = Map.of(0, "a", 1, "b");
		System.out.println(map.get(map.size() - 1)); // violation: Use '.getLast()' instead of '.get(size() - 1)'.
	}
}