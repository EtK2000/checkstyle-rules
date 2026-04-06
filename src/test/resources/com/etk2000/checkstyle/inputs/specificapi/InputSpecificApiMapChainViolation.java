package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Map;

class InputSpecificApiMapChainViolation {
	void keySetContains(Map<String, String> map) {
		if (map.keySet().contains("key")) // violation: use .containsKey(...)
			System.out.println("found");
	}

	void valuesContains(Map<String, String> map) {
		if (map.values().contains("value")) // violation: use .containsValue(...)
			System.out.println("found");
	}
}