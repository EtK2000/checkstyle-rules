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
		var x = 42;
		final var s = "hello";
		var list = List.of(1, 2, 3);
		String nullStr = null;
		int uninitialized;
		final int[] numbers = {1, 2, 3};
		final int[][] matrix = {{1, 2}, {3, 4}};
		final String[] names = {"a", "b"};
		var sized = new String[5];
		Runnable r = () -> System.out.println("hello");
		Supplier<String> s2 = () -> "world";
		Function<String, Integer> f = String::length;
		Runnable anon = new Runnable() {
			@Override
			public void run() {
				System.out.println("anonymous");
			}
		};
	}

	void tryWithResources() throws Exception {
		try (var in = new ByteArrayInputStream(new byte[0])) {
			System.out.println(in.read());
		}
	}
}