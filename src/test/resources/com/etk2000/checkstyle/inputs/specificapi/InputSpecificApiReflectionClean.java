package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Collections;
import java.util.Map;

class InputSpecificApiReflectionClean {
	Map<Integer, String> mapField = Map.of();

	void chainedCallResolvesToMap(Map<Integer, String> map) {
		System.out.println(Collections.unmodifiableMap(map).get(0));
	}

	void mapFieldGetZero() {
		System.out.println(mapField.get(0));
	}

	void mapParamGetZero(Map<Integer, String> map) {
		System.out.println(map.get(0));
	}
}