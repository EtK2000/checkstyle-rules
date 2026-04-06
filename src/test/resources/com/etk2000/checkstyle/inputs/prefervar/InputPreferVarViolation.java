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

	void localVariables() {
		final int x = 42; // violation: local must use var
		final String s = "hello"; // violation: local must use var
		final List<Integer> list = List.of(1, 2, 3); // violation: local must use var
		final var names = new String[]{"a", "b"}; // violation: use implicit array init
		final String[] numbers = new String[]{"1"}; // violation: use implicit array init
		final int[][] matrix = new int[][]{{1}, {2}}; // violation: use implicit array init
		final Runnable complexAnon = new Runnable() { // violation: local must use var
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void tryWithResources() throws Exception {
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: try-with-resources must use var
			System.out.println(in.read());
		}
	}
}