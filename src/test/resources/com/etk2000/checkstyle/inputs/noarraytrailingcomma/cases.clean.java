package com.etk2000.checkstyle.inputs.noarraytrailingcomma;

class InputArrayCommaClean {
	int[] a = {1, 2, 3};
	int[] b = new int[]{4, 5};
	int[] c = {};
	int[] d = new int[]{};

	// nested arrays: no trailing commas
	int[][] e = {{1, 2}, {3, 4}};
}