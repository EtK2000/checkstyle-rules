package com.etk2000.checkstyle.inputs.prefervar;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;

class InputPreferVarViolation {
	void annotatedLocalVariable() {
		@Nonnull
		final String s = "hello"; // violation: Local variable must use 'var' instead of an explicit type.
	}

	void constructorCall() {
		final Object obj = new Object(); // violation: Local variable must use 'var' instead of an explicit type.
		final HashMap<String, Integer> map = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void forEach() {
		final var list = List.of("a", "b");
		for (String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(item);
		final var numbers = List.of(1, 2, 3);
		for (Integer num : numbers) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(num);
	}

	void forEachAnnotated() {
		final var list = List.of("a", "b");
		for (@Nonnull String item : list) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(item);
	}

	void forEachGenericType() {
		final var map = Map.of("a", 1);
		for (Entry<String, Integer> entry : map.entrySet()) // violation: For-each loop must use 'var' instead of an explicit type.
			System.out.println(entry);
	}

	void forLoopInit() {
		for (int i = 0; i < 10; ++i) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(i);
	}

	void forLoopInitReferenceType() {
		final var list = List.of("a", "b");
		for (Iterator<String> it = list.iterator(); it.hasNext(); ) // violation: Local variable must use 'var' instead of an explicit type.
			System.out.println(it.next());
	}

	void localVariables() {
		final int x = 42; // violation: Local variable must use 'var' instead of an explicit type.
		final String s = "hello"; // violation: Local variable must use 'var' instead of an explicit type.
		final List<Integer> list = List.of(1, 2, 3); // violation: Local variable must use 'var' instead of an explicit type.
		final var names = new String[]{"a", "b"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final String[] numbers = new String[]{"1"}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final int[][] matrix = new int[][]{{1}, {2}}; // violation: Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.
		final Runnable complexAnon = new Runnable() { // violation: Local variable must use 'var' instead of an explicit type.
			int count = 0;

			@Override
			public void run() {
				System.out.println(count);
			}
		};
	}

	void methodCallAndChain() {
		final String s = String.valueOf(42); // violation: Local variable must use 'var' instead of an explicit type.
		final String trimmed = "  hello  ".trim().toLowerCase(); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void nestedAndWildcardTypes() {
		final List<?> wildcard = List.of(1, 2); // violation: Local variable must use 'var' instead of an explicit type.
		final Map<String, List<Integer>> nested = new HashMap<>(); // violation: Local variable must use 'var' instead of an explicit type.
		final ArrayList<String> concrete = new ArrayList<>(); // violation: Local variable must use 'var' instead of an explicit type.
	}

	void tryWithResources() throws Exception {
		try (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(in.read());
		}
	}

	void tryWithResourcesAnnotated() throws Exception {
		try (@Nonnull ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) { // violation: Try-with-resources must use 'var' instead of an explicit type.
			System.out.println(in.read());
		}
	}
}