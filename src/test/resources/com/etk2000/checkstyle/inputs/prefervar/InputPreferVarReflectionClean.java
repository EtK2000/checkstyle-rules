package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class InputPreferVarReflectionClean {
	List<String> field = List.of();

	void explicitTypeOnGeneric() {
		List<String> list = Collections.emptyList();
		Optional<Integer> opt = Optional.empty();
	}

	void inferableFromArgs() {
		var list = List.of("a", "b");
		var min = Collections.min(list);
	}

	void instanceCallClassLevelTypeParam(List<String> items) {
		var first = items.getFirst();
	}

	void instanceCallViaField() {
		var first = field.getFirst();
	}

	void instanceCallViaLocal() {
		final var list = List.of("a");
		var first = list.getFirst();
	}

	void instanceCallViaParam(List<String> items) {
		var size = items.size();
	}
}