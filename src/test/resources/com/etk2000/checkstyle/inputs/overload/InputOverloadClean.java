package com.etk2000.checkstyle.inputs.overload;

class InputOverloadClean {
	void method() {}

	void method(int a) {}

	void method(int a, int b) {}

	void other(int a) {}

	void differentMethod() {}

	void other(int a, int b) {}

	// base type, then array, then multi-dimensional array
	void arr(int a) {}

	void arr(int[] a) {}

	void arr(int[][] a) {}

	// primitive array sorts with primitives (before reference types)
	void arr(long[] a) {}

	void arr(String b) {}

	void arr(String[] b) {}

	// two params, first differs
	void multi(int a, String b) {}

	void multi(String a, int b) {}

	// same param count, alphabetical within reference types
	void ref(List a) {}

	void ref(String a) {}

	// same param count, primitive before reference type
	void same(int a) {}

	void same(String b) {}

	// same param count, alphabetical within primitives
	void typed(char a) {}

	void typed(int a) {}
}