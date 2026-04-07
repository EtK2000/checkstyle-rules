package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingArrayViolation {
	int[] arr;
	String name;
	int plain; // violation: Field 'plain' (type 'int') must appear before 'name' (type 'String').
}

class InputFieldSortingMultidimensionalArrayViolation {
	int[][] matrix;
	int[] vector; // violation: Field 'vector' (type 'int[]') must appear before 'matrix' (type 'int[][]').
	int scalar;   // violation: Field 'scalar' (type 'int') must appear before 'vector' (type 'int[]').
}

class InputFieldSortingMixedPrimitiveArrayViolation {
	double[] values;
	char letter; // violation: Field 'letter' (type 'char') must appear before 'values' (type 'double[]').
}