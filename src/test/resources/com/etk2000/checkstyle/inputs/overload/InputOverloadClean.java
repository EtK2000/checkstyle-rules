package com.etk2000.checkstyle.inputs.overload;

class InputOverloadClean {
	void method() {}

	void method(int a) {}

	void method(int a, int b) {}

	void other(int a) {}

	void differentMethod() {}

	void other(int a, int b) {}

	void arr(int a) {}

	void arr(int[] a) {}

	void arr(int[][] a) {}

	void arr(long[] a) {}

	void arr(String b) {}

	void arr(String[] b) {}

	void multi(int a, String b) {}

	void multi(String a, int b) {}

	void secondParam(int a, char b) {}

	void secondParam(int a, int b) {}

	void ref(List a) {}

	void ref(String a) {}

	void same(int a) {}

	void same(String b) {}

	void typed(char a) {}

	void typed(int a) {}

	void vararg(int a) {}

	void vararg(int... a) {}
}