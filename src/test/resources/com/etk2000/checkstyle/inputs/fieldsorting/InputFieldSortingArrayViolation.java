package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingArrayViolation {
	int[] arr;
	String name;
	int plain; // violation: primitive int after reference String
}

class InputFieldSortingMultidimensionalArrayViolation {
	int[][] matrix;
	int[] vector; // violation: int[] before int[][]
	int scalar;   // violation: int before int[]
}

class InputFieldSortingMixedPrimitiveArrayViolation {
	double[] values;
	char letter; // violation: char before double[]
}