package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.List;

class InputPreferVarClean {
	String field = "not flagged";

	void forEach() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
		for (final var item : list)
			System.out.println(item);
	}

	void localVariables() {
		var x = 42;
		final var s = "hello";
		var list = List.of(1, 2, 3);
		String nullStr = null;
		int uninitialized;
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}