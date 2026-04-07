package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.List;
import java.util.Map;

enum InputFieldSortingCleanEnum {
	ALPHA,
	BETA,
	GAMMA;

	static final int MAX = 10;
	static final int MIN = 1;
}

enum InputFieldSortingCleanEnumOuter {
	ALPHA,
	BETA;

	enum Inner {
		FIRST,
		SECOND
	}
}

enum InputFieldSortingCleanEnumSingle {
	ONLY_ONE
}

enum InputFieldSortingCleanEnumWithBodies {
	ADD {
		@Override
		int apply(int a, int b) {
			return a + b;
		}
	},
	SUBTRACT {
		@Override
		int apply(int a, int b) {
			return a - b;
		}
	};

	abstract int apply(int a, int b);
}

enum InputFieldSortingCleanEnumWithMembers {
	APPLE("red"),
	BANANA("yellow"),
	CHERRY("red");

	final String color;

	InputFieldSortingCleanEnumWithMembers(String color) {
		this.color = color;
	}

	String getColor() {
		return color;
	}
}

enum InputFieldSortingCleanEnumWithSeparators {
	ALPHA,

	// Beta is the second letter
	BETA,

	@Deprecated
	GAMMA
}

class InputFieldSortingClean {
	enum InnerSorted {
		FIRST,
		SECOND,
		THIRD
	}

	// static: final with inline value, anonymous class first then regular
	static final Runnable STATIC_TASK = new Runnable() {
		@Override
		public void run() {
			System.out.println(GAMMA);
		}
	};
	static final int ALPHA = 1;
	static final int BETA = 2;
	// dependency ordering: NOW must come before FUTURE and PAST
	static final long NOW = System.currentTimeMillis();
	static final long FUTURE = NOW + 1000;
	static final long PAST = NOW - 1000;
	static final String GAMMA = "g";

	static int delta;

	// instance: final with inline value, anonymous classes first (sorted among themselves)
	final Comparable<String> comparator = new Comparable<>() {
		@Override
		public int compareTo(String o) {
			return name.compareTo(o);
		}
	};
	final Runnable task = new Runnable() {
		@Override
		public void run() {
			System.out.println(name);
		}
	};
	final int count = 0;
	final String name = "default";

	final List<String> items;
	final Map<String, Integer> lookup;

	// instance: non-final, primitives then reference, alphabetical
	boolean active;
	char letter;
	double ratio;
	double[] ratios;
	int index;
	int elements[];
	int[] indices;
	int[][] matrix;
	long timestamp;
	java.util.concurrent.atomic.AtomicInteger counter;
	List<String> data;
	String label;
	String[] labels;

	InputFieldSortingClean(List<String> items, Map<String, Integer> lookup) {
		this.items = items;
		this.lookup = lookup;
	}
}