package com.etk2000.checkstyle.inputs.prefervar;

import java.util.List;
import java.util.Map;

class InputForEachReceiverScope {
	List<String> shared = List.of();

	void forEachMapGetZero(List<Map<Integer, String>> maps) {
		for (Map<Integer, String> m : maps)
			System.out.println(m.get(0));
	}

	void forInitMapGetZero(Map<Integer, String> source) {
		for (Map<Integer, String> m = source; m != null; m = null)
			System.out.println(m.get(0));
	}

	void shadowingForEachGetZero(List<Map<Integer, String>> maps) {
		for (Map<Integer, String> shared : maps)
			System.out.println(shared.get(0));
	}
}