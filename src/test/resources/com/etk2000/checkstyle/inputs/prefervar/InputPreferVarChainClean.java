package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.List;

class InputPreferVarChainClean {
	void chainClassLevelTypeParam(List<String> list) {
		final var first = Collections.unmodifiableList(list).getFirst();
	}

	void chainNonGeneric(List<String> list) {
		final var size = Collections.unmodifiableList(list).size();
	}
}