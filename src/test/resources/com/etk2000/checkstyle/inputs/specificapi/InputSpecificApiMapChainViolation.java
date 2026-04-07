package com.etk2000.checkstyle.inputs.specificapi;

import java.util.Map;

class InputSpecificApiMapChainViolation {
	void keySetContains(Map<String, String> map) {
		if (map.keySet().contains("key")) // violation: Use '.containsKey(...)' instead of '.keySet().contains(...)'.
			System.out.println("found");
	}

	void valuesContains(Map<String, String> map) {
		if (map.values().contains("value")) // violation: Use '.containsValue(...)' instead of '.values().contains(...)'.
			System.out.println("found");
	}
}