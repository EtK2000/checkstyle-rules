package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class InputPreferVarReflectionClean {
	List<String> field = List.of();

	void explicitTypeOnGeneric() {
		final List<String> list = Collections.emptyList();
		final Optional<Integer> opt = Optional.empty();
	}

	void inferableFromArgs() {
		final var list = List.of("a", "b");
		final var min = Collections.min(list);
	}

	void instanceCallClassLevelTypeParam(List<String> items) {
		final var first = items.getFirst();
	}

	void instanceCallViaField() {
		final var first = field.getFirst();
	}

	void instanceCallViaLocal() {
		final var list = List.of("a");
		final var first = list.getFirst();
	}

	void instanceCallViaParam(List<String> items) {
		final var size = items.size();
	}
}