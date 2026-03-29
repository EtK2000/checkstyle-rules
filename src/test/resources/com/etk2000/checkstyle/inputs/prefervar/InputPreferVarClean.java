package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.List;

class InputPreferVarClean {
	void forEach() {
		final var list = List.of("a", "b");
		for (var item : list)
			System.out.println(item);
		for (final var item : list)
			System.out.println(item);
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0]))  {
			System.out.println(in.read());
		}
	}
}