package com.etk2000.checkstyle.inputs.fieldsorting;

import java.util.List;
import java.util.Map;

class InputFieldSortingClean {
	// static: final with inline value, primitives then reference, alphabetical
	static final int ALPHA = 1;
	static final int BETA = 2;
	// dependency ordering: NOW must come before FUTURE and PAST
	static final long NOW = System.currentTimeMillis();
	static final long FUTURE = NOW + 1000;
	static final long PAST = NOW - 1000;
	static final String GAMMA = "g";

	// static: non-final
	static int delta;

	// instance: final with inline value
	final int count = 0;
	final String name = "default";

	// instance: final without inline value
	final List<String> items;
	final Map<String, Integer> lookup;

	// instance: non-final, primitives then reference, alphabetical
	boolean active;
	char letter;
	double ratio;
	double[] ratios;
	int index;
	int[] indices;
	int[][] matrix;
	long timestamp;
	List<String> data;
	String label;
	String[] labels;

	InputFieldSortingClean(List<String> items, Map<String, Integer> lookup) {
		this.items = items;
		this.lookup = lookup;
	}
}