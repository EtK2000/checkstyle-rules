package com.etk2000.checkstyle.inputs.noarraytrailingcomma;

class InputArrayCommaClean {
	int[] a = {1, 2, 3};
	int[] b = new int[]{4, 5};
	int[] c = {};
	int[] d = new int[]{};

	// nested arrays: no trailing commas
	int[][] e = {{1, 2}, {3, 4}};
}

// an annotation's array value is an ANNOTATION_ARRAY_INIT, not an ARRAY_INIT
class InputAnnotationArrayCommaClean {
	@interface Anno {
		int[] value();
	}

	@Anno({1, 2})
	int f;

	@Anno({})
	int g;
}