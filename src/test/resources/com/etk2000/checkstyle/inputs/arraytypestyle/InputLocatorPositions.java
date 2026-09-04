package com.etk2000.checkstyle.inputs.arraytypestyle;

import java.util.Map;
import java.util.function.IntFunction;

/**
 * Positions for {@code ArrayTypeStyleCheckTest} and {@code ArrayTypeStyleFixerTest}, which address
 * them directly rather than through a slice: a base type ending a line ahead of its declarators, where
 * a check on the declarators' own line cannot see the brackets the type already carries, and brackets
 * the check never reports. The file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	int[]
			alpha[], beta[];

	int
			gamma[], delta[];

	IntFunction<int[]> factory = int[]::new;
	String literal = "int a[] = x;";
	Map<String, int[]> lookup;
	String text = """
			int a[] = x;
			""";
	Class<?> type = int[].class;

	// int a[] = x;
	Byte[] cast(Object o) {
		final Byte bytes[] = (Byte[]) o;
		return bytes;
	}

	boolean isIntArray(Object o) {
		return o instanceof int[];
	}

	void varargs(int[]... xs) {
		System.out.println(xs.length);
	}
}