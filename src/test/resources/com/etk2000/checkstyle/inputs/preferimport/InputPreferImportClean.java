package com.etk2000.checkstyle.inputs.preferimport;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
class InputPreferImportClean
		extends ArrayList<String>
		implements Serializable {
	List<String> field;
	List<Map<String, Integer>> nested;

	void castAndInstanceof(Object obj) {
		if (obj instanceof List)
			System.out.println((List<?>) obj);
	}

	List<String> method(Set<Integer> param)
			throws IOException {
		List<String> local = List.of("a");
		return local;
	}
}