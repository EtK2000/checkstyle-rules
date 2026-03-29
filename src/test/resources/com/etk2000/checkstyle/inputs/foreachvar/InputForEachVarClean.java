package com.etk2000.checkstyle.inputs.foreachvar;

import java.util.List;

class InputForEachVarClean {
	void method() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
		for (final var item : list)
			System.out.println(item);
	}
}