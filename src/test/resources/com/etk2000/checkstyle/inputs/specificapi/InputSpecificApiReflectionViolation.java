package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class InputSpecificApiReflectionViolation {
	void chainedCallGetZero() {
		System.out.println(getList().get(0)); // violation: unresolvable bare call, best-effort flags it
	}

	void chainedCallResolvedGetZero(List<String> list) {
		System.out.println(Collections.synchronizedList(list).get(0)); // violation: chain resolves to List, has getFirst
	}

	List<String> getList() {
		return List.of();
	}

	void listLocalGetZero() {
		final var list = List.of("a");
		System.out.println(list.get(0)); // violation: use getFirst()
	}

	void listParamGetZero(List<String> list) {
		System.out.println(list.get(0)); // violation: use getFirst()
	}

	void varLocalGetSizeMinusOne() {
		final var map = Map.of(0, "a", 1, "b");
		System.out.println(map.get(map.size() - 1)); // violation: var-typed, unresolvable
	}
}