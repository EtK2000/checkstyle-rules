package com.etk2000.checkstyle.inputs.arraycomma;

class InputArrayCommaViolation {
	int[] a = {1, 2, 3,}; // violation: No trailing comma in array initializer.
	int[] b = new int[]{4, 5,}; // violation: No trailing comma in array initializer.

	int[][] c = {{1, 2,}, {3, 4}}; // violation: No trailing comma in array initializer.
}