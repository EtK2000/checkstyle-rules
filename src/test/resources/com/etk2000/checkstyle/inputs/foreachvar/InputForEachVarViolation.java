package com.etk2000.checkstyle.inputs.foreachvar;

import java.util.List;

class InputForEachVarViolation {
	void method() {
		final var list = List.of("a", "b");
		for (String item : list) // violation: for-each must use var
			System.out.println(item);
		final var numbers = List.of(1, 2, 3);
		for (Integer num : numbers) // violation: for-each must use var
			System.out.println(num);
	}
}