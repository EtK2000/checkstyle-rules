package com.etk2000.checkstyle.inputs.prefervar;

import java.util.List;

class InputForEachStringReceiver {
	void stripIsEmptyOnAnExplicitForEachVariable(List<String> names) {
		for (String name : names)
			System.out.println(name.strip().isEmpty());
	}
}