package com.etk2000.checkstyle.inputs.arraycomma;

class InputArrayCommaViolation {
	int[] a = {1, 2, 3,};
	int[] b = new int[]{4, 5,};

	// nested array: inner trailing comma
	int[][] c = {{1, 2,}, {3, 4}};
}