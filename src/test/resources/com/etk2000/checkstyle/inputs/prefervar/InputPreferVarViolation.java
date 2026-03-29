package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.List;

class InputPreferVarViolation {
	void forEach() {
		final var list = List.of("a", "b");
		for (String item : list) // violation: for-each must use var
			System.out.println(item);
		final var numbers = List.of(1, 2, 3);
		for (Integer num : numbers) // violation: for-each must use var
			System.out.println(num);
	}

	void tryWithResources() throws Exception {
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: try-with-resources must use var
			System.out.println(in.read());
		}
	}
}