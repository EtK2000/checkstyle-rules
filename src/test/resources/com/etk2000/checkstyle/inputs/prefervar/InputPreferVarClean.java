package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

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
		final var x = 42;
		final var s = "hello";
		final var list = List.of(1, 2, 3);
		final String nullStr = null;
		final int uninitialized;
		final int[] numbers = {1, 2, 3};
		final int[][] matrix = {{1, 2}, {3, 4}};
		final String[] names = {"a", "b"};
		final var sized = new String[5];
		final Runnable r = () -> System.out.println("hello");
		final Supplier<String> s2 = () -> "world";
		final Function<String, Integer> f = String::length;
		final Runnable anon = new Runnable() {
			@Override
			public void run() {
				System.out.println("anonymous");
			}
		};
		final var complexAnon = new Runnable() {
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}